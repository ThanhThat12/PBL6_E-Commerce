package com.PBL6.Ecommerce.domain.dto;

import java.util.List;

public class ShopReviewsGroupedDTO {
    private List<ProductReviewDTO> replied;
    private List<ProductReviewDTO> unreplied;

    public ShopReviewsGroupedDTO() {}

    public ShopReviewsGroupedDTO(List<ProductReviewDTO> replied, List<ProductReviewDTO> unreplied) {
        this.replied = replied;
        this.unreplied = unreplied;
    }

    // Getters and setters
    public List<ProductReviewDTO> getReplied() {
        return replied;
    }

    public void setReplied(List<ProductReviewDTO> replied) {
        this.replied = replied;
    }

    public List<ProductReviewDTO> getUnreplied() {
        return unreplied;
    }

    public void setUnreplied(List<ProductReviewDTO> unreplied) {
        this.unreplied = unreplied;
    }
}