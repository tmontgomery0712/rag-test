package com.example.ragaitest.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SpaWebFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        String path = requestURI.substring(contextPath.length());

        if (isApiRequest(path) || path.contains(".")) {
            chain.doFilter(request, response);
            return;
        }

        httpRequest.getRequestDispatcher("/index.html").forward(request, response);
    }

    private boolean isApiRequest(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/user/") ||
               path.startsWith("/api/streaks") ||
               path.equals("/api/register") ||
               path.equals("/api/health") ||
               path.startsWith("/api/error");
    }
}
