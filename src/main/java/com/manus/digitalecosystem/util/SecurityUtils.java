package com.manus.digitalecosystem.util;

import com.manus.digitalecosystem.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        return userDetails;
    }

    public static String getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public static boolean hasRole(String role) {
        String expectedAuthority = "ROLE_" + role;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (expectedAuthority.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}

