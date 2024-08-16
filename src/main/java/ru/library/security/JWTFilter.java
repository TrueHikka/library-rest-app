package ru.library.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.library.util.JWTUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final PersonDetailsService personDetailsService;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil, PersonDetailsService personDetailsService) {
        this.jwtUtil = jwtUtil;
        this.personDetailsService = personDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader!= null &&
                !authHeader.isBlank() &&
                authHeader.startsWith("Bearer ")
        ) {
            String token = authHeader.substring(7);

            if (token.isBlank()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is empty");
            } else {
                try {
                    DecodedJWT decodedJWT = jwtUtil.validateToken(token);

                    String username = decodedJWT.getClaim("username").asString();
                    String role = decodedJWT.getClaim("role").asString();

                    UserDetails userDetails = personDetailsService.loadUserByUsername(username);

                    Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            userDetails.getPassword(),
                            authorities
                    );

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }

                } catch (JWTVerificationException | UsernameNotFoundException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT-token");
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}