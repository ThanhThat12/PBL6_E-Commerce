package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO for creating a new product review
 */
public class CreateReviewRequestDTO {
    // rating bắt buộc: 1..5
    @NotNull(message = "Rating là bắt buộc")
    @Min(value = 1, message = "Rating phải từ 1 đến 5")
    @Max(value = 5, message = "Rating phải từ 1 đến 5")
    private Integer rating;

    // comment không bắt buộc, nếu có thì giới hạn độ dài
    @Size(max = 2000, message = "Bình luận không vượt quá 2000 ký tự")
    private String comment;

    // images không bắt buộc, nếu có thì tối đa 5 ảnh, mỗi phần tử không rỗng
    @Size(max = 5, message = "Tối đa 5 ảnh")
    private List<@NotBlank(message = "Url ảnh không được rỗng") String> images;

    // getters / setters
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}