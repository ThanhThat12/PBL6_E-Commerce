package com.PBL6.Ecommerce.controller;

import com.PBL6.Ecommerce.domain.Shop;
import com.PBL6.Ecommerce.domain.User;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.search.FacetedSearchDTO;
import com.PBL6.Ecommerce.domain.dto.search.SearchSuggestionDTO;
import com.PBL6.Ecommerce.domain.dto.search.SearchSuggestionDTO.ShopSuggestionItem;
import com.PBL6.Ecommerce.domain.dto.search.TrendingSearchDTO;
import com.PBL6.Ecommerce.repository.ProductRepository;
import com.PBL6.Ecommerce.repository.ShopRepository;
import com.PBL6.Ecommerce.repository.UserRepository;
import com.PBL6.Ecommerce.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for search functionality
 * - Search suggestions (autocomplete)
 * - Trending searches
 * - Search history
 */
@Tag(name = "Search", description = "Product search, suggestions, trending keywords")
@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Get search suggestions for autocomplete
     * Returns query suggestions, product matches, category matches
     * 
     * @param q Search query (can be partial)
     * @param limit Max number of suggestions per type (default: 5)
     */
    @GetMapping("/suggestions")
    public ResponseEntity<ResponseDTO<SearchSuggestionDTO>> getSuggestions(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "5") int limit) {
        
        SearchSuggestionDTO suggestions = searchService.getSuggestions(q, limit);
        return ResponseDTO.success(suggestions, "Lấy gợi ý tìm kiếm thành công");
    }
    
    /**
     * Get trending/popular searches
     * 
     * @param limit Max number of trending searches (default: 10)
     */
    @GetMapping("/trending")
    public ResponseEntity<ResponseDTO<TrendingSearchDTO>> getTrendingSearches(
            @RequestParam(defaultValue = "10") int limit) {
        
        TrendingSearchDTO trending = searchService.getTrendingSearches(limit);
        return ResponseDTO.success(trending, "Lấy tìm kiếm phổ biến thành công");
    }
    
    /**
     * Track a search query (for analytics and trending)
     * Called when user performs a search
     */
    @PostMapping("/track")
    public ResponseEntity<ResponseDTO<String>> trackSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int resultCount,
            Authentication authentication,
            HttpServletRequest request) {
        
        Long userId = getUserId(authentication);
        
        String sessionId = request.getSession().getId();
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        
        searchService.trackSearch(q, userId, resultCount, sessionId, ipAddress, userAgent);
        
        return ResponseDTO.success("tracked", "Đã ghi nhận tìm kiếm");
    }
    
    /**
     * Track when user clicks a product from search results
     */
    @PostMapping("/track-click")
    public ResponseEntity<ResponseDTO<String>> trackClick(
            @RequestParam String q,
            @RequestParam Long productId) {
        
        searchService.trackSearchClick(q, productId);
        return ResponseDTO.success("tracked", "Đã ghi nhận click");
    }
    
    /**
     * Get faceted search filters with product counts
     * Returns available filters (categories, price ranges, ratings) with counts
     * 
     * @param keyword Search keyword (optional)
     * @param categoryId Currently selected category (optional)
     * @param minPrice Currently selected min price (optional)
     * @param maxPrice Currently selected max price (optional)
     * @param minRating Currently selected min rating (optional)
     */
    @GetMapping("/facets")
    public ResponseEntity<ResponseDTO<FacetedSearchDTO>> getFacetedFilters(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minRating) {
        
        FacetedSearchDTO facets = searchService.getFacetedFilters(
            keyword, categoryId, minPrice, maxPrice, minRating);
        return ResponseDTO.success(facets, "Lấy bộ lọc thành công");
    }
    
    /**
     * Get user's search history (requires authentication)
     */
    @GetMapping("/history")
    public ResponseEntity<ResponseDTO<List<String>>> getSearchHistory(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseDTO.success(List.of(), "Cần đăng nhập để xem lịch sử");
        }
        
        Long userId = getUserId(authentication);
        if (userId == null) {
            return ResponseDTO.success(List.of(), "Không tìm thấy người dùng");
        }
        
        List<String> history = searchService.getUserSearchHistory(userId, limit);
        return ResponseDTO.success(history, "Lấy lịch sử tìm kiếm thành công");
    }
    
    /**
     * Clear user's search history (requires authentication)
     */
    @DeleteMapping("/history")
    public ResponseEntity<ResponseDTO<String>> clearSearchHistory(Authentication authentication) {
        if (authentication == null) {
            return ResponseDTO.error(401, "UNAUTHORIZED", "Cần đăng nhập để xóa lịch sử");
        }
        
        Long userId = getUserId(authentication);
        if (userId == null) {
            return ResponseDTO.error(404, "NOT_FOUND", "Không tìm thấy người dùng");
        }
        
        searchService.clearUserSearchHistory(userId);
        return ResponseDTO.success("cleared", "Đã xóa lịch sử tìm kiếm");
    }
    
    /**
     * Delete a specific search from history
     */
    @DeleteMapping("/history/{query}")
    public ResponseEntity<ResponseDTO<String>> deleteFromHistory(
            @PathVariable String query,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseDTO.error(401, "UNAUTHORIZED", "Cần đăng nhập");
        }
        
        Long userId = getUserId(authentication);
        if (userId == null) {
            return ResponseDTO.error(404, "NOT_FOUND", "Không tìm thấy người dùng");
        }
        
        searchService.deleteFromUserHistory(userId, query);
        return ResponseDTO.success("deleted", "Đã xóa khỏi lịch sử");
    }
    
    /**
     * Search for shops by name (public endpoint)
     * Returns shops matching the keyword with product counts
     * 
     * @param keyword Search keyword
     * @param limit Max number of shops to return (default: 10)
     */
    @GetMapping("/shops")
    public ResponseEntity<ResponseDTO<List<ShopSuggestionItem>>> searchShops(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseDTO.success(List.of(), "Vui lòng nhập từ khóa tìm kiếm");
        }
        
        List<Shop> shops = shopRepository.findByNameContaining(
            keyword.trim().toLowerCase(), PageRequest.of(0, limit));
        
        List<ShopSuggestionItem> shopItems = shops.stream()
            .map(s -> new ShopSuggestionItem(
                s.getId(),
                s.getName(),
                highlightMatch(s.getName(), keyword),
                s.getLogoUrl(),
                productRepository.countActiveByShopId(s.getId()),
                s.getStatus().name()
            ))
            .collect(Collectors.toList());
        
        return ResponseDTO.success(shopItems, "Tìm kiếm shop thành công");
    }
    
    /**
     * Helper to highlight matching text
     */
    private String highlightMatch(String text, String query) {
        if (text == null || query == null) {
            return text;
        }
        String pattern = "(?i)(" + java.util.regex.Pattern.quote(query) + ")";
        return text.replaceAll(pattern, "<b>$1</b>");
    }
    
    /**
     * Helper to get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
    
    /**
     * Helper to get user ID from authentication
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        
        Optional<User> user = userRepository.findByUsername(authentication.getName());
        return user.map(User::getId).orElse(null);
    }
}
