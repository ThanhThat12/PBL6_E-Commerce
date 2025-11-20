package com.PBL6.Ecommerce.domain.dto;

public class MonthlyOrderStatsDTO {
    private int year;
    private int month;
    private Long orderCount;

    public MonthlyOrderStatsDTO() {
    }

    public MonthlyOrderStatsDTO(int year, int month, Long orderCount) {
        this.year = year;
        this.month = month;
        this.orderCount = orderCount;
    }

    // Getters and Setters
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }
}
