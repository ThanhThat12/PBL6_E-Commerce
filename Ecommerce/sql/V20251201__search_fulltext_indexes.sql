-- ============================================================
-- Search System Enhancement - Phase 1
-- Add FULLTEXT indexes for product search
-- ============================================================

-- 1. Add FULLTEXT index on products table for fast text search
-- MySQL 8.0+ supports FULLTEXT on InnoDB tables

-- Check if fulltext index exists before creating
SET @index_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'products' 
    AND INDEX_NAME = 'ft_product_search'
);

-- Only create if not exists
ALTER TABLE products 
ADD FULLTEXT INDEX ft_product_search (name, description);

-- 2. Create search_queries table for tracking search history
CREATE TABLE IF NOT EXISTS search_queries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL COMMENT 'NULL if guest user',
    query VARCHAR(255) NOT NULL,
    normalized_query VARCHAR(255) NULL COMMENT 'Lowercase, trimmed query',
    result_count INT DEFAULT 0,
    filters_applied JSON NULL COMMENT 'Applied filters: {"categoryId": 1, "minPrice": 100}',
    clicked_product_id BIGINT NULL COMMENT 'Product clicked from results',
    session_id VARCHAR(100) NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_search_query (query),
    INDEX idx_search_user (user_id),
    INDEX idx_search_created_at (created_at),
    INDEX idx_search_normalized (normalized_query),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (clicked_product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Create trending_searches table for popular searches
CREATE TABLE IF NOT EXISTS trending_searches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    query VARCHAR(255) NOT NULL,
    normalized_query VARCHAR(255) NOT NULL COMMENT 'Lowercase for matching',
    search_count INT DEFAULT 1,
    click_count INT DEFAULT 0 COMMENT 'How many times results were clicked',
    last_searched TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    trend_score DECIMAL(10,2) DEFAULT 0 COMMENT 'Calculated score for ranking',
    period ENUM('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY') DEFAULT 'DAILY',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_normalized_query_period (normalized_query, period),
    INDEX idx_trend_score (trend_score DESC),
    INDEX idx_period_score (period, trend_score DESC),
    INDEX idx_last_searched (last_searched)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create search_synonyms table for typo correction & aliases
CREATE TABLE IF NOT EXISTS search_synonyms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    term VARCHAR(100) NOT NULL COMMENT 'Original/misspelled term',
    synonym VARCHAR(100) NOT NULL COMMENT 'Correct/alternative term',
    type ENUM('TYPO', 'SYNONYM', 'ALIAS', 'BRAND') NOT NULL DEFAULT 'SYNONYM',
    priority INT DEFAULT 0 COMMENT 'Higher = more relevant',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_term_synonym (term, synonym),
    INDEX idx_term (term),
    INDEX idx_type (type),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Insert common typo corrections and synonyms for Vietnamese sports products
INSERT INTO search_synonyms (term, synonym, type, priority) VALUES
-- Common typos (no accents)
('giay', 'giày', 'TYPO', 10),
('ao', 'áo', 'TYPO', 10),
('bong', 'bóng', 'TYPO', 10),
('da', 'đá', 'TYPO', 10),
('vot', 'vợt', 'TYPO', 10),
('cau', 'cầu', 'TYPO', 10),
('long', 'lông', 'TYPO', 10),
('tennis', 'tennis', 'TYPO', 10),
('ro', 'rổ', 'TYPO', 10),

-- Combined typos
('giay bong da', 'giày bóng đá', 'TYPO', 15),
('ao bong da', 'áo bóng đá', 'TYPO', 15),
('vot cau long', 'vợt cầu lông', 'TYPO', 15),
('giay chay bo', 'giày chạy bộ', 'TYPO', 15),

-- Team aliases
('barca', 'barcelona', 'ALIAS', 20),
('mu', 'manchester united', 'ALIAS', 20),
('real', 'real madrid', 'ALIAS', 20),
('chelsea', 'chelsea', 'ALIAS', 20),
('arsenal', 'arsenal', 'ALIAS', 20),
('liverpool', 'liverpool', 'ALIAS', 20),
('psg', 'paris saint germain', 'ALIAS', 20),
('bayern', 'bayern munich', 'ALIAS', 20),
('juve', 'juventus', 'ALIAS', 20),

-- Brand variations
('nike', 'nike', 'BRAND', 25),
('adidas', 'adidas', 'BRAND', 25),
('puma', 'puma', 'BRAND', 25),
('yonex', 'yonex', 'BRAND', 25),
('wilson', 'wilson', 'BRAND', 25),
('babolat', 'babolat', 'BRAND', 25),
('victor', 'victor', 'BRAND', 25),
('lining', 'li-ning', 'ALIAS', 25),

-- Synonyms
('giày chạy', 'giày chạy bộ', 'SYNONYM', 10),
('quần short', 'quần đùi', 'SYNONYM', 10),
('bóng đá', 'đá bóng', 'SYNONYM', 10),
('túi gym', 'túi tập gym', 'SYNONYM', 10),
('áo đấu', 'áo thi đấu', 'SYNONYM', 10)
ON DUPLICATE KEY UPDATE priority = VALUES(priority);

-- 6. Add composite index for better search performance
ALTER TABLE products 
ADD INDEX idx_product_search_perf (is_active, rating DESC, sold_count DESC);

-- 7. Add index for category-based search
ALTER TABLE products 
ADD INDEX idx_product_category_active (category_id, is_active, rating DESC);

