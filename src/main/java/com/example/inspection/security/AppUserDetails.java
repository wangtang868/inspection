package com.example.inspection.security;

import com.example.inspection.entity.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class AppUserDetails implements UserDetails {
    private final AppUser user;
    public AppUserDetails(AppUser user) { this.user = user; }
    public Long getId() { return user.getId(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return user.getStatus() == 1; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getStatus() == 1; }
}