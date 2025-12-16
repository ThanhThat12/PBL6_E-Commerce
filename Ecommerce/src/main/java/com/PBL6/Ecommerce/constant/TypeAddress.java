package com.PBL6.Ecommerce.constant;

/**
 * TypeAddress Enum - Chỉ 2 loại địa chỉ:
 * - HOME: Địa chỉ của buyer (người mua) - dùng để nhận hàng
 * - STORE: Địa chỉ kho/cửa hàng của seller - dùng làm địa chỉ gửi hàng
 * 
 * Business Rules:
 * - Buyer có thể có nhiều địa chỉ HOME, chọn 1 làm primary
 * - Seller chỉ có DUY NHẤT 1 địa chỉ STORE
 * - Primary address CHỈ áp dụng cho HOME, không áp dụng cho STORE
 */
public enum TypeAddress {
    HOME("Home"),
    STORE("Store"),
    ORTHER("Other");

    private final String label;

    TypeAddress(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static TypeAddress fromString(String value) {
        if (value == null) {
            return null;
        }
        for (TypeAddress type : TypeAddress.values()) {
            if (type.name().equalsIgnoreCase(value) || type.label.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TypeAddress: " + value + ". Valid values: HOME, STORE");
    }
}