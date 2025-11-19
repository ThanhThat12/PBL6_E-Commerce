package com.PBL6.Ecommerce.domain.dto;

import com.PBL6.Ecommerce.domain.ProductAttribute;

public class ProductAtributeDTO {
    private Long id;
    private String name;

    // Constructors
    public ProductAtributeDTO() {}

    public ProductAtributeDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
