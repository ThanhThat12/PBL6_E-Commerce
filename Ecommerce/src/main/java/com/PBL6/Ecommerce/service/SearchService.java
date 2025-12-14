package com.PBL6.Ecommerce.service;

import com.PBL6.Ecommerce.domain.dto.search.FacetedSearchDTO;
import com.PBL6.Ecommerce.domain.dto.search.FacetedSearchDTO.*;
import com.PBL6.Ecommerce.domain.dto.search.SearchSuggestionDTO;
import com.PBL6.Ecommerce.domain.dto.search.SearchSuggestionDTO.*;
import com.PBL6.Ecommerce.domain.dto.search.TrendingSearchDTO;
import com.PBL6.Ecommerce.domain.dto.search.TrendingSearchDTO.TrendingItem;
import com.PBL6.Ecommerce.domain.entity.product.Category;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import com.PBL6.Ecommerce.domain.entity.search.SearchQuery;
import com.PBL6.Ecommerce.domain.entity.search.SearchSynonym;
import com.PBL6.Ecommerce.domain.entity.search.TrendingSearch;
import com.PBL6.Ecommerce.domain.entity.search.TrendingSearch.TrendPeriod;
import com.PBL6.Ecommerce.domain.entity.shop.Shop;
import com.PBL6.Ecommerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for search functionality including:
 * - Search suggestions (autocomplete)
 * - Trending searches
 * - Typo correction
 * - Search history tracking
 */
@Service
@Transactional
public class SearchService {
    
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private SearchQueryRepository searchQueryRepository;
    
    @Autowired
    private TrendingSearchRepository trendingSearchRepository;
    
    @Autowired
    private SearchSynonymRepository searchSynonymRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    /**
     * Get search suggestions based on query
     * Returns query suggestions, product suggestions, and category suggestions
     */
    @Transactional(readOnly = true)
    public SearchSuggestionDTO getSuggestions(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return getDefaultSuggestions(limit);
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        log.debug("Getting suggestions for: {}", normalizedQuery);
        
        SearchSuggestionDTO dto = new SearchSuggestionDTO();
        
        // 1. Check for typo correction
        String correctedQuery = correctTypos(normalizedQuery);
        if (!correctedQuery.equals(normalizedQuery)) {
            dto.setDidYouMean(correctedQuery);
        }
        
        // 2. Get query suggestions from product names
        List<QuerySuggestionItem> querySuggestions = getQuerySuggestions(normalizedQuery, limit);
        dto.setQueries(querySuggestions);
        
        // 3. Get product suggestions (mini cards)
        List<ProductSuggestionItem> productSuggestions = getProductSuggestions(normalizedQuery, limit);
        dto.setProducts(productSuggestions);
        
        // 4. Get category suggestions
        List<CategorySuggestionItem> categorySuggestions = getCategorySuggestions(normalizedQuery, limit);
        dto.setCategories(categorySuggestions);
        
        // 5. Get trending searches matching prefix
        List<TrendingSuggestionItem> trendingSuggestions = getTrendingSuggestionsForQuery(normalizedQuery, limit);
        dto.setTrending(trendingSuggestions);
        
        // 6. Get shop suggestions
        List<ShopSuggestionItem> shopSuggestions = getShopSuggestions(normalizedQuery, limit);
        dto.setShops(shopSuggestions);
        
        return dto;
    }
    
    /**
     * Get default suggestions when no query (trending + popular)
     */
    @Transactional(readOnly = true)
    public SearchSuggestionDTO getDefaultSuggestions(int limit) {
        SearchSuggestionDTO dto = new SearchSuggestionDTO();
        
        // Get trending searches
        List<TrendingSearch> trending = trendingSearchRepository.findTopByPeriod(
            TrendPeriod.DAILY, PageRequest.of(0, limit));
        
        AtomicInteger rank = new AtomicInteger(1);
        List<TrendingSuggestionItem> trendingItems = trending.stream()
            .map(ts -> new TrendingSuggestionItem(
                ts.getQuery(),
                rank.getAndIncrement(),
                (long) ts.getSearchCount()
            ))
            .collect(Collectors.toList());
        
        dto.setTrending(trendingItems);
        dto.setQueries(new ArrayList<>());
        dto.setProducts(new ArrayList<>());
        dto.setCategories(new ArrayList<>());
        dto.setShops(new ArrayList<>());
        
        return dto;
    }
    
    /**
     * Get trending searches
     */
    @Transactional(readOnly = true)
    public TrendingSearchDTO getTrendingSearches(int limit) {
        List<TrendingSearch> trending = trendingSearchRepository.findTopByPeriod(
            TrendPeriod.DAILY, PageRequest.of(0, limit));
        
        AtomicInteger rank = new AtomicInteger(1);
        List<TrendingItem> items = trending.stream()
            .map(ts -> new TrendingItem(
                ts.getQuery(),
                rank.getAndIncrement(),
                (long) ts.getSearchCount(),
                ts.getTrendScore() != null ? ts.getTrendScore().doubleValue() : 0.0
            ))
            .collect(Collectors.toList());
        
        return new TrendingSearchDTO(items);
    }
    
    /**
     * Track a search query for analytics and trending
     */
    public void trackSearch(String query, Long userId, int resultCount, String sessionId, 
                           String ipAddress, String userAgent) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        
        // 1. Save search query for history
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(query);
        searchQuery.setNormalizedQuery(normalizedQuery);
        searchQuery.setResultCount(resultCount);
        searchQuery.setSessionId(sessionId);
        searchQuery.setIpAddress(ipAddress);
        searchQuery.setUserAgent(userAgent);
        
        if (userId != null) {
            userRepository.findById(userId).ifPresent(searchQuery::setUser);
        }
        
        searchQueryRepository.save(searchQuery);
        
        // 2. Update trending search
        updateTrendingSearch(normalizedQuery, query);
    }
    
    /**
     * Track when user clicks a product from search results
     */
    public void trackSearchClick(String query, Long productId) {
        String normalizedQuery = query.toLowerCase().trim();
        trendingSearchRepository.incrementClickCount(normalizedQuery, TrendPeriod.DAILY);
    }
    
    /**
     * Get faceted search filters with counts
     * Returns available filters and their product counts
     */
    @Transactional(readOnly = true)
    public FacetedSearchDTO getFacetedFilters(String keyword, Long selectedCategoryId, 
            Double selectedMinPrice, Double selectedMaxPrice, Integer selectedMinRating) {
        
        log.debug("Getting faceted filters for keyword: {}", keyword);
        
        FacetedSearchDTO dto = new FacetedSearchDTO();
        
        // Get all active products matching keyword (or all if no keyword)
        List<Product> matchingProducts;
        if (keyword != null && !keyword.trim().isEmpty()) {
            matchingProducts = productRepository.findProductsForSuggestion(
                keyword.toLowerCase(), PageRequest.of(0, 10000));
        } else {
            matchingProducts = productRepository.findByIsActiveTrue(PageRequest.of(0, 10000)).getContent();
        }
        
        dto.setTotalCount(matchingProducts.size());
        
        // 1. Category facets
        List<CategoryFacet> categoryFacets = getCategoryFacets(matchingProducts, selectedCategoryId);
        dto.setCategories(categoryFacets);
        
        // 2. Price range facets
        List<PriceRangeFacet> priceRangeFacets = getPriceRangeFacets(matchingProducts, 
            selectedMinPrice, selectedMaxPrice);
        dto.setPriceRanges(priceRangeFacets);
        
        // 3. Rating facets
        List<RatingFacet> ratingFacets = getRatingFacets(matchingProducts, selectedMinRating);
        dto.setRatings(ratingFacets);
        
        // 4. Brand/Shop facets
        List<BrandFacet> brandFacets = getBrandFacets(matchingProducts);
        dto.setBrands(brandFacets);
        
        return dto;
    }
    
    /**
     * Get category facets with product counts
     */
    private List<CategoryFacet> getCategoryFacets(List<Product> products, Long selectedCategoryId) {
        Map<Long, Long> categoryCounts = products.stream()
            .filter(p -> p.getCategory() != null)
            .collect(Collectors.groupingBy(
                p -> p.getCategory().getId(),
                Collectors.counting()
            ));
        
        List<Category> allCategories = categoryRepository.findAll();
        
        return allCategories.stream()
            .map(cat -> new CategoryFacet(
                cat.getId(),
                cat.getName(),
                categoryCounts.getOrDefault(cat.getId(), 0L),
                cat.getId().equals(selectedCategoryId)
            ))
            .filter(f -> f.getProductCount() > 0)
            .sorted((a, b) -> Long.compare(b.getProductCount(), a.getProductCount()))
            .limit(20)
            .collect(Collectors.toList());
    }
    
    /**
     * Get price range facets with product counts
     */
    private List<PriceRangeFacet> getPriceRangeFacets(List<Product> products, 
            Double selectedMin, Double selectedMax) {
        
        // Define price ranges (in VND)
        double[][] ranges = {
            {0, 100000},
            {100000, 500000},
            {500000, 1000000},
            {1000000, 5000000},
            {5000000, Double.MAX_VALUE}
        };
        
        String[] labels = {
            "Dưới 100.000đ",
            "100.000đ - 500.000đ",
            "500.000đ - 1.000.000đ",
            "1.000.000đ - 5.000.000đ",
            "Trên 5.000.000đ"
        };
        
        List<PriceRangeFacet> facets = new ArrayList<>();
        
        for (int i = 0; i < ranges.length; i++) {
            double min = ranges[i][0];
            double max = ranges[i][1];
            
            long count = products.stream()
                .filter(p -> {
                    double price = p.getBasePrice() != null ? p.getBasePrice().doubleValue() : 0;
                    return price >= min && price < max;
                })
                .count();
            
            boolean selected = (selectedMin != null && selectedMin.equals(min)) ||
                              (selectedMax != null && max < Double.MAX_VALUE && selectedMax.equals(max));
            
            facets.add(new PriceRangeFacet(
                labels[i],
                min,
                max == Double.MAX_VALUE ? null : max,
                count,
                selected
            ));
        }
        
        return facets;
    }
    
    /**
     * Get rating facets with product counts
     */
    private List<RatingFacet> getRatingFacets(List<Product> products, Integer selectedMinRating) {
        List<RatingFacet> facets = new ArrayList<>();
        
        int[] ratings = {5, 4, 3};
        String[] labels = {"5 sao", "4 sao trở lên", "3 sao trở lên"};
        
        for (int i = 0; i < ratings.length; i++) {
            int minRating = ratings[i];
            
            long count = products.stream()
                .filter(p -> {
                    double rating = p.getRating() != null ? p.getRating().doubleValue() : 0;
                    return rating >= minRating;
                })
                .count();
            
            facets.add(new RatingFacet(
                minRating,
                labels[i],
                count,
                selectedMinRating != null && selectedMinRating == minRating
            ));
        }
        
        return facets;
    }
    
    /**
     * Get brand/shop facets with product counts
     */
    private List<BrandFacet> getBrandFacets(List<Product> products) {
        Map<Long, List<Product>> byShop = products.stream()
            .filter(p -> p.getShop() != null)
            .collect(Collectors.groupingBy(p -> p.getShop().getId()));
        
        return byShop.entrySet().stream()
            .map(entry -> {
                Shop shop = entry.getValue().get(0).getShop();
                return new BrandFacet(
                    shop.getId(),
                    shop.getName(),
                    (long) entry.getValue().size(),
                    false
                );
            })
            .sorted((a, b) -> Long.compare(b.getProductCount(), a.getProductCount()))
            .limit(15)
            .collect(Collectors.toList());
    }
    
    /**
     * Get user's recent search history
     */
    @Transactional(readOnly = true)
    public List<String> getUserSearchHistory(Long userId, int limit) {
        return searchQueryRepository.findDistinctRecentQueriesByUserId(
            userId, PageRequest.of(0, limit));
    }
    
    /**
     * Clear all search history for a user
     */
    public void clearUserSearchHistory(Long userId) {
        searchQueryRepository.deleteByUserId(userId);
        log.info("Cleared search history for user: {}", userId);
    }
    
    /**
     * Delete a specific query from user's search history
     */
    public void deleteFromUserHistory(Long userId, String query) {
        String normalizedQuery = query.toLowerCase().trim();
        searchQueryRepository.deleteByUserIdAndNormalizedQuery(userId, normalizedQuery);
        log.info("Deleted query '{}' from history for user: {}", query, userId);
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    /**
     * Correct typos in query using synonym table + Levenshtein distance
     */
    private String correctTypos(String query) {
        String[] words = query.split("\\s+");
        StringBuilder corrected = new StringBuilder();
        
        for (String word : words) {
            // First try exact match in synonym table
            Optional<SearchSynonym> correction = searchSynonymRepository.findBestMatch(word);
            if (correction.isPresent()) {
                corrected.append(correction.get().getSynonym()).append(" ");
            } else {
                // Try fuzzy matching with Levenshtein distance
                String fuzzyMatch = findFuzzyMatch(word);
                if (fuzzyMatch != null && !fuzzyMatch.equals(word)) {
                    corrected.append(fuzzyMatch).append(" ");
                } else {
                    corrected.append(word).append(" ");
                }
            }
        }
        
        return corrected.toString().trim();
    }
    
    /**
     * Find fuzzy match for a word using Levenshtein distance
     * Searches common product names and categories
     */
    private String findFuzzyMatch(String word) {
        if (word.length() < 3) {
            return null; // Skip very short words
        }
        
        // Get common terms from synonyms
        List<SearchSynonym> allSynonyms = searchSynonymRepository.findAll();
        
        int minDistance = Integer.MAX_VALUE;
        String bestMatch = null;
        int maxAllowedDistance = Math.max(1, word.length() / 3); // Allow 1 error per 3 chars
        
        for (SearchSynonym synonym : allSynonyms) {
            int distance = levenshteinDistance(word, synonym.getSynonym().toLowerCase());
            if (distance < minDistance && distance <= maxAllowedDistance) {
                minDistance = distance;
                bestMatch = synonym.getSynonym();
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Get query suggestions from product names
     */
    private List<QuerySuggestionItem> getQuerySuggestions(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<String> productNames = productRepository.findProductNameSuggestions(query, pageable);
        
        return productNames.stream()
            .map(name -> new QuerySuggestionItem(
                name,
                highlightMatch(name, query),
                null // Count can be added later
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Get product suggestions (mini cards)
     */
    private List<ProductSuggestionItem> getProductSuggestions(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findProductsForSuggestion(query, pageable);
        
        return products.stream()
            .map(p -> new ProductSuggestionItem(
                p.getId(),
                p.getName(),
                highlightMatch(p.getName(), query),
                p.getMainImage(),
                p.getBasePrice(),
                p.getRating(),
                p.getSoldCount(),
                p.getShop() != null ? p.getShop().getName() : null
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Get category suggestions
     */
    private List<CategorySuggestionItem> getCategorySuggestions(String query, int limit) {
        List<Category> allCategories = categoryRepository.findAll();
        
        return allCategories.stream()
            .filter(c -> c.getName().toLowerCase().contains(query))
            .limit(limit)
            .map(c -> new CategorySuggestionItem(
                c.getId(),
                c.getName(),
                highlightMatch(c.getName(), query),
                productRepository.countByCategoryId(c.getId())
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Get trending suggestions matching query prefix
     */
    private List<TrendingSuggestionItem> getTrendingSuggestionsForQuery(String query, int limit) {
        List<TrendingSearch> trending = trendingSearchRepository.findByPrefixAndPeriod(
            query, TrendPeriod.DAILY, PageRequest.of(0, limit));
        
        AtomicInteger rank = new AtomicInteger(1);
        return trending.stream()
            .map(ts -> new TrendingSuggestionItem(
                ts.getQuery(),
                rank.getAndIncrement(),
                (long) ts.getSearchCount()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Get shop suggestions matching query
     */
    private List<ShopSuggestionItem> getShopSuggestions(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Shop> shops = shopRepository.findByNameContaining(query, pageable);
        
        return shops.stream()
            .map(s -> new ShopSuggestionItem(
                s.getId(),
                s.getName(),
                highlightMatch(s.getName(), query),
                s.getLogoUrl(),
                productRepository.countActiveByShopId(s.getId()),
                s.getStatus().name()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Update trending search entry
     */
    private void updateTrendingSearch(String normalizedQuery, String originalQuery) {
        Optional<TrendingSearch> existing = trendingSearchRepository
            .findByNormalizedQueryAndPeriod(normalizedQuery, TrendPeriod.DAILY);
        
        if (existing.isPresent()) {
            trendingSearchRepository.incrementSearchCount(existing.get().getId());
        } else {
            TrendingSearch trending = new TrendingSearch();
            trending.setQuery(originalQuery);
            trending.setNormalizedQuery(normalizedQuery);
            trending.setSearchCount(1);
            trending.setPeriod(TrendPeriod.DAILY);
            trending.setTrendScore(BigDecimal.ONE);
            trendingSearchRepository.save(trending);
        }
    }
    
    /**
     * Highlight matching text with <b> tags
     */
    private String highlightMatch(String text, String query) {
        if (text == null || query == null) {
            return text;
        }
        
        // Case-insensitive replace with highlighting
        String pattern = "(?i)(" + Pattern.quote(query) + ")";
        return text.replaceAll(pattern, "<b>$1</b>");
    }
}
