package com.PBL6.Ecommerce.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Utility class for making HTTP requests to MoMo API
 */
public class MoMoHttpUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoHttpUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send POST request to MoMo API
     * 
     * @param url The API endpoint URL
     * @param requestBody The request body object
     * @param responseType The expected response class type
     * @param <T> Response type
     * @return Response object
     * @throws Exception if request fails
     */
    public static <T> T sendPostRequest(String url, Object requestBody, Class<T> responseType) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            // Convert request body to JSON string for logging
            String requestJson = objectMapper.writeValueAsString(requestBody);
            logger.info("Sending POST request to MoMo API: {}", url);
            logger.debug("Request body: {}", requestJson);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "Mozilla/5.0");
            
            // Create HTTP entity
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            
            // Send request
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    responseType
            );
            
            // Log response
            logger.info("Received response from MoMo API. Status: {}", response.getStatusCode());
            logger.debug("Response body: {}", objectMapper.writeValueAsString(response.getBody()));
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error while calling MoMo API. Status: {}, Response: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("MoMo API client error: " + e.getMessage(), e);
            
        } catch (HttpServerErrorException e) {
            logger.error("Server error while calling MoMo API. Status: {}, Response: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("MoMo API server error: " + e.getMessage(), e);
            
        } catch (Exception e) {
            logger.error("Error while calling MoMo API: {}", e.getMessage(), e);
            throw new Exception("Failed to call MoMo API: " + e.getMessage(), e);
        }
    }

    /**
     * Send POST request with Map body
     * 
     * @param url The API endpoint URL
     * @param requestBody The request body as Map
     * @param responseType The expected response class type
     * @param <T> Response type
     * @return Response object
     * @throws Exception if request fails
     */
    public static <T> T sendPostRequestWithMap(String url, Map<String, Object> requestBody, Class<T> responseType) throws Exception {
        return sendPostRequest(url, requestBody, responseType);
    }

    /**
     * Send GET request to MoMo API
     * 
     * @param url The API endpoint URL with query parameters
     * @param responseType The expected response class type
     * @param <T> Response type
     * @return Response object
     * @throws Exception if request fails
     */
    public static <T> T sendGetRequest(String url, Class<T> responseType) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            logger.info("Sending GET request to MoMo API: {}", url);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0");
            
            // Create HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Send request
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    responseType
            );
            
            // Log response
            logger.info("Received response from MoMo API. Status: {}", response.getStatusCode());
            logger.debug("Response body: {}", objectMapper.writeValueAsString(response.getBody()));
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.error("Client error while calling MoMo API. Status: {}, Response: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("MoMo API client error: " + e.getMessage(), e);
            
        } catch (HttpServerErrorException e) {
            logger.error("Server error while calling MoMo API. Status: {}, Response: {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("MoMo API server error: " + e.getMessage(), e);
            
        } catch (Exception e) {
            logger.error("Error while calling MoMo API: {}", e.getMessage(), e);
            throw new Exception("Failed to call MoMo API: " + e.getMessage(), e);
        }
    }

    /**
     * Parse JSON string to object
     * 
     * @param json JSON string
     * @param clazz Target class
     * @param <T> Target type
     * @return Parsed object
     * @throws Exception if parsing fails
     */
    public static <T> T parseJson(String json, Class<T> clazz) throws Exception {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("Error parsing JSON: {}", e.getMessage(), e);
            throw new Exception("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convert object to JSON string
     * 
     * @param object Object to convert
     * @return JSON string
     * @throws Exception if conversion fails
     */
    public static String toJson(Object object) throws Exception {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("Error converting to JSON: {}", e.getMessage(), e);
            throw new Exception("Failed to convert to JSON: " + e.getMessage(), e);
        }
    }
}
