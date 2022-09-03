package net.m127.vpm.repo.jpa.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "username", nullable = false, unique = true)
    private String name;
    
    @Column(name = "VRC_id")
    private String vrcId;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @OneToMany(targetEntity = Package.class, mappedBy = "author")
    private List<Package> userPackages;
    
    public User() {}
    
    public User(String name, String vrcId, String email) {
        this.name = name;
        this.vrcId = vrcId;
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVrcId() {
        return vrcId;
    }
    
    public void setVrcId(String vrcId) {
        this.vrcId = vrcId;
    }
    
    public List<Package> getUserPackages() {
        return userPackages;
    }
    
    public void setUserPackages(List<Package> userPackages) {
        this.userPackages = userPackages;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
