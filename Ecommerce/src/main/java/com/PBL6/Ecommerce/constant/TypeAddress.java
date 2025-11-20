package com.PBL6.Ecommerce.constant;

public enum TypeAddress {
    HOME("Home"),
    STORE("Store"),
    SHIPPING("Shipping"),
    OTHER("Other");

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
        throw new IllegalArgumentException("Unknown TypeAddress: " + value);
    }
}