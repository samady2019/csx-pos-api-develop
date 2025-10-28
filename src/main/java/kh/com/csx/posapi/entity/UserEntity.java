package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "v_users")
public class UserEntity implements UserDetails {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "username")
    private String username;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "language")
    private String language;

    @Column(name = "billers")
    private String billers;

    @Column(name = "warehouses")
    private String warehouses;

    @Column(name = "view_right")
    private Integer viewRight;

    @Column(name = "commission_rate")
    private Double commissionRate;

    @Column(name = "status")
    private String status;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "v_user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "v_user_permission", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<PermissionEntity> permissions = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id", insertable = false, updatable = false)
    private EmployeeEntity employee;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<RoleEntity> sortedRoles = this.getRoles().stream().sorted((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName())).toList();
        for (RoleEntity role : sortedRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            List<PermissionEntity> sortedPermissions = role.getPermissions().stream().sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName())).toList();
            for (PermissionEntity permission : sortedPermissions) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        List<PermissionEntity> sortedUserPermissions = this.getPermissions().stream().sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName())).toList();
        for (PermissionEntity permission : sortedUserPermissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getName()));
        }

        return authorities;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
