package com.example.ragaitest.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${app.jwt.refresh-cookie-name:refresh_token}")
    private String refreshCookieName;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Value("${app.jwt.secure:false}")
    private boolean secure;

    public ResponseCookie createRefreshCookie(String token) {
        return ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(refreshExpirationMs / 1000)
                .build();
    }

    public ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }

    public String getRefreshCookieName() {
        return refreshCookieName;
    }
}
