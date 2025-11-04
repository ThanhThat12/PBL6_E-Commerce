// ...existing code...
package com.PBL6.Ecommerce.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // ...existing code...
    // replace Product reference with ProductVariant
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    private int quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    // replaced getter/setter for product with productVariant
    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public Product getProduct() {
        // convenience: return owning product if needed (nullable)
        return productVariant != null ? productVariant.getProduct() : null;
    }

    public Product getProductDirect() { return getProduct(); }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
    
}