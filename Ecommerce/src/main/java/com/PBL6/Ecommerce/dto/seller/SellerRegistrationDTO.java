package com.PBL6.Ecommerce.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Seller Registration Request (Buyer upgrade to Seller)
 * Following Shopee model: buyer must have account before becoming seller
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegistrationDTO {

    @NotBlank(message = "Tên shop không được để trống")
    @Size(max = 255, message = "Tên shop không được vượt quá 255 ký tự")
    private String shopName;

    @Size(max = 1000, message = "Mô tả shop không được vượt quá 1000 ký tự")
    private String shopDescription;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
        regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$",
        message = "Số điện thoại không hợp lệ. Định dạng: 0xxxxxxxxx hoặc +84xxxxxxxxx"
    )
    private String shopPhone;

    // Optional: if provided, client can pass an existing address id saved in addresses table
    private Long pickupAddressId;

    @Size(max = 500, message = "Địa chỉ shop không được vượt quá 500 ký tự")
    private String shopAddress; // optional when pickupAddressId is provided
}
