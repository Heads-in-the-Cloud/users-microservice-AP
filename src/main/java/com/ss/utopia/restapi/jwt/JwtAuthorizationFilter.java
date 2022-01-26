package com.ss.utopia.restapi.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.ss.utopia.restapi.dao.UserRepository;
import com.ss.utopia.restapi.models.User;
import com.ss.utopia.restapi.models.UserPrincipal;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String header = request.getHeader(JwtProperties.HEADER_STRING);
        System.out.println("Header: " + header);

        if (header != null && header.startsWith(JwtProperties.TOKEN_PREFIX)) {
            Authentication authentication = getUsernamePasswordAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request) {
        String token = request.getHeader(JwtProperties.HEADER_STRING).replace(JwtProperties.TOKEN_PREFIX, "");
        UsernamePasswordAuthenticationToken auth = null;

        if (token != null) {
            String userName = JWT.require(HMAC512(JwtProperties.SECRET.getBytes()))
                .build()
                .verify(token)
                .getSubject();

            if (userName != null) {
                User user = userRepository.findByUsername(userName).orElseThrow(
                    () -> new UsernameNotFoundException("No user was found with this username.")
                );
                UserPrincipal principal = new UserPrincipal(user);
                auth = new UsernamePasswordAuthenticationToken(userName, null, principal.getAuthorities());
            }
        }
        return auth;
    }
}
