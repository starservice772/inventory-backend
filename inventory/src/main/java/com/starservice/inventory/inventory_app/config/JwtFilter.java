package com.starservice.inventory.inventory_app.config;

import com.starservice.inventory.inventory_app.enums.Company;
import com.starservice.inventory.inventory_app.service.CustomUserDetailsService;
import com.starservice.inventory.inventory_app.utility.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip auth endpoints
        String path = request.getServletPath();
        if (path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String companyStr = null;
        String token = null;

        try {

            System.out.println("PATH: " + path);
            System.out.println("AUTH HEADER: " + authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);

                username = jwtUtil.extractUsername(token);
                companyStr = jwtUtil.extractCompany(token);

                System.out.println("USERNAME: " + username);
                System.out.println("COMPANY: " + companyStr);
            }

            if (username != null && companyStr != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                Company company = Company.valueOf(companyStr);
                System.out.println("ENUM CONVERTED: " + company);

                UserDetails userDetails = userDetailsService.loadUserByUsernameAndCompany(username, company);

                System.out.println("USER LOADED: " + userDetails.getUsername());

                boolean isValid = jwtUtil.validateToken(token, username);
                System.out.println("TOKEN VALID: " + isValid);

                if (isValid) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

//                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    authToken.setDetails(company);

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("AUTH SUCCESS");

                } else {
                    System.out.println("TOKEN INVALID");
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR IN JWT FILTER");
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}