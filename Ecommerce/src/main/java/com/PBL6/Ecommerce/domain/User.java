package com.PBL6.Ecommerce.domain;
import jakarta.persistence.*;
import java.util.List;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String username;

    @Column(length = 100, unique = true, nullable = true)
    private String email;

    @Column(length = 100, unique = true, nullable = true)
    private String phoneNumber;

    @Column(length = 60, nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean activated = false;

    @Column(nullable = false)
    private Role role;

    @Column(length = 100, unique = true)
    private String facebookId;

    @Column(length = 100, unique = true)
    private String googleId;

    // Thêm quan hệ với Shop
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shop shop;

     // Thêm quan hệ với Address (một user có nhiều địa chỉ)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;

    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }
    
public void setActivated(boolean activated) {
    this.activated = activated;
}
public void setUsername(String username) {
    this.username = username;
}

public void setEmail(String email) {
    this.email = email;
}

public void setPassword(String password) {
    this.password = password;
}
public String getUsername() {
    return username;
}
public String getEmail() {
    return email;
}
public String getPassword() {
    return password;
}
public boolean isActivated() {
    return activated;
}
public Long getId() {
    return id;
}
public void setId(Long id) {
    this.id = id;
}
public Role getRole() {
    return role;
}
public void setRole(Role role) {
    this.role = role;
}
public String getFacebookId() {
    return facebookId;
}
public void setFacebookId(String facebookId) {
    this.facebookId = facebookId;
}
public String getGoogleId() {
    return googleId;
}
public void setGoogleId(String googleId) {
    this.googleId = googleId;
}
public String getPhoneNumber() {
    return phoneNumber;
}
public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
}
public Shop getShop() { return shop; }
public void setShop(Shop shop) { this.shop = shop; }

}
