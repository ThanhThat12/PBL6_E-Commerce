package com.PBL6.Ecommerce.domain.dto;

public class UpdateUserStatusDTO {
    private boolean activated; // true: đang hoạt động, false: tạm ngưng

    public UpdateUserStatusDTO() {
    }

    public UpdateUserStatusDTO(boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}