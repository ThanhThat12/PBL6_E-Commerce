-- =====================================================
-- VALIDATE GHN ADDRESSES IN DATABASE
-- =====================================================

-- 1. CHECK ALL ADDRESSES - VALIDATION STATUS
SELECT 
    id,
    user_id,
    type_address,
    CASE 
        WHEN district_id IS NULL THEN '❌ MISSING district_id'
        WHEN ward_code IS NULL THEN '❌ MISSING ward_code'
        WHEN district_id = 0 THEN '❌ INVALID district_id (0)'
        WHEN ward_code = '' OR ward_code = '0' THEN '❌ INVALID ward_code'
        ELSE '✅ VALID GHN data'
    END as validation_status,
    province_name,
    district_name,
    ward_name,
    district_id,
    ward_code,
    full_address,
    primary_address,
    created_at
FROM addresses
ORDER BY 
    user_id,
    CASE 
        WHEN district_id IS NOT NULL AND ward_code IS NOT NULL 
            AND district_id != 0 AND ward_code != '' 
        THEN 0 
        ELSE 1 
    END,
    primary_address DESC;

-- 2. COUNT VALID vs INVALID ADDRESSES
SELECT 
    type_address,
    COUNT(*) as total,
    SUM(CASE 
        WHEN district_id IS NOT NULL AND ward_code IS NOT NULL 
            AND district_id != 0 AND ward_code != '' 
        THEN 1 ELSE 0 
    END) as valid_for_ghn,
    SUM(CASE 
        WHEN district_id IS NULL OR ward_code IS NULL 
            OR district_id = 0 OR ward_code = '' 
        THEN 1 ELSE 0 
    END) as invalid_for_ghn
FROM addresses
GROUP BY type_address
ORDER BY type_address;

-- 3. INVALID ADDRESSES - NEED FIXING
SELECT 
    a.id,
    a.user_id,
    u.username,
    u.email,
    a.type_address,
    CASE 
        WHEN a.district_id IS NULL THEN 'Missing district_id'
        WHEN a.ward_code IS NULL THEN 'Missing ward_code'
        WHEN a.district_id = 0 THEN 'Invalid district_id (0)'
        WHEN a.ward_code = '' THEN 'Invalid ward_code (empty)'
        ELSE 'Unknown issue'
    END as issue,
    a.province_name,
    a.district_name,
    a.ward_name,
    a.full_address
FROM addresses a
JOIN users u ON u.id = a.user_id
WHERE a.district_id IS NULL 
   OR a.ward_code IS NULL 
   OR a.district_id = 0 
   OR a.ward_code = ''
ORDER BY a.type_address, u.username;

-- 4. SHOP ADDRESSES (STORE) - CRITICAL FOR SHIPPING
SELECT 
    a.id as address_id,
    s.id as shop_id,
    s.name as shop_name,
    u.username as owner_username,
    CASE 
        WHEN a.district_id IS NULL OR a.ward_code IS NULL 
            OR a.district_id = 0 OR a.ward_code = '' 
        THEN '❌ INVALID - Cannot ship'
        ELSE '✅ VALID'
    END as shipping_status,
    a.province_name,
    a.district_name,
    a.ward_name,
    a.district_id,
    a.ward_code,
    a.full_address
FROM shops s
JOIN users u ON u.id = s.owner_id
LEFT JOIN addresses a ON a.user_id = u.id AND a.type_address = 'STORE'
ORDER BY shipping_status DESC, s.name;

-- 5. PRIMARY ADDRESSES BY USER (for quick testing)
SELECT 
    u.id as user_id,
    u.username,
    u.role,
    CASE 
        WHEN a.district_id IS NOT NULL AND a.ward_code IS NOT NULL 
            AND a.district_id != 0 AND a.ward_code != '' 
        THEN '✅ Can use GHN'
        ELSE '❌ Cannot use GHN'
    END as ghn_ready,
    a.type_address,
    a.province_name,
    a.district_name,
    a.ward_name,
    a.district_id,
    a.ward_code
FROM users u
LEFT JOIN addresses a ON a.user_id = u.id AND a.primary_address = 1
WHERE u.role IN ('BUYER', 'SELLER')
ORDER BY u.role, ghn_ready, u.username;

-- 6. ADDRESSES BY PROVINCE - Coverage Analysis
SELECT 
    province_name,
    COUNT(*) as total_addresses,
    SUM(CASE 
        WHEN district_id IS NOT NULL AND ward_code IS NOT NULL 
            AND district_id != 0 AND ward_code != '' 
        THEN 1 ELSE 0 
    END) as valid_count,
    ROUND(100.0 * SUM(CASE 
        WHEN district_id IS NOT NULL AND ward_code IS NOT NULL 
            AND district_id != 0 AND ward_code != '' 
        THEN 1 ELSE 0 
    END) / COUNT(*), 2) as valid_percentage
FROM addresses
GROUP BY province_name
ORDER BY valid_percentage DESC, total_addresses DESC;

-- 7. QUICK FIX TEMPLATE (Update invalid addresses)
-- Uncomment and customize as needed

-- Example: Fix addresses with missing ward_code for a specific district
/*
UPDATE addresses 
SET ward_code = '20308',  -- Correct ward code
    ward_name = 'Phường Điện Biên'  -- Correct ward name
WHERE district_id = 1444  -- Ba Đình, Hà Nội
  AND (ward_code IS NULL OR ward_code = '' OR ward_code = '0')
  AND district_name LIKE '%Ba Đình%';
*/

-- Example: Fix addresses with invalid district_id
/*
UPDATE addresses
SET district_id = 1444,  -- Correct district ID
    district_name = 'Quận Ba Đình'  -- Correct district name
WHERE province_name = 'Hà Nội'
  AND district_name LIKE '%Ba Đình%'
  AND (district_id IS NULL OR district_id = 0);
*/

-- 8. TEST QUERY - Find your own addresses
-- Replace USER_ID with your actual user ID
/*
SELECT 
    id,
    type_address,
    CASE 
        WHEN district_id IS NOT NULL AND ward_code IS NOT NULL 
            AND district_id != 0 AND ward_code != '' 
        THEN '✅ VALID'
        ELSE '❌ INVALID'
    END as status,
    province_name,
    district_name,
    ward_name,
    district_id,
    ward_code,
    full_address,
    primary_address
FROM addresses
WHERE user_id = YOUR_USER_ID
ORDER BY primary_address DESC, type_address;
*/

-- =====================================================
-- INTERPRETATION
-- =====================================================
/*
Query 1: Shows all addresses with validation status
Query 2: Summary by address type
Query 3: Lists problematic addresses that need fixing
Query 4: Critical check for STORE addresses (needed for shipping)
Query 5: Quick view of users' primary addresses
Query 6: Coverage analysis by province
Query 7: Template SQL to fix common issues
Query 8: Test your own addresses

ACTION ITEMS:
1. Run Query 3 to find invalid addresses
2. Run Query 4 to ensure all shops have valid STORE addresses
3. For any invalid addresses:
   - If it's a buyer: They need to re-add address via UI
   - If it's a shop STORE: Critical - fix immediately using Query 7 template
4. Run Query 5 to verify primary addresses are GHN-ready
*/
