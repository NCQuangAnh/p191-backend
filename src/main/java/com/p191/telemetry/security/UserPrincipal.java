package com.p191.telemetry.security;

import com.p191.telemetry.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(String username, String password, boolean enabled,
                          Collection<? extends GrantedAuthority> authorities) {
        this.username = username; this.password = password;
        this.enabled = enabled; this.authorities = authorities;
    }

    /** User (P-191) → UserDetails. Authority "ROLE_"+role để hasRole(...) chạy. */
    public static UserPrincipal fromUser(User u) {
        var authority = new SimpleGrantedAuthority("ROLE_" + u.getRole().getName().name());
        return new UserPrincipal(u.getUsername(), u.getPassword(), u.isEnabled(), List.of(authority));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}