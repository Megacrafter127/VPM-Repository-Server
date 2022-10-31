package net.m127.vpm.repo.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;
    
    @Column(name = "username", nullable = false, unique = true)
    private String name;
    
    @Column(name = "VRC_id")
    private String VRCId;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "validated", nullable = false)
    private boolean validated;
    
    @Column(name = "approved", nullable = false)
    private boolean approved;
    
    @Column(name = "admin", nullable = false)
    private boolean admin;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Package> userPackages;
}
