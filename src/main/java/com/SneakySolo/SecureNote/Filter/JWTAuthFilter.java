package com.SneakySolo.SecureNote.Filter;

import com.SneakySolo.SecureNote.Service.CustomUserDetailsService;
import com.SneakySolo.SecureNote.Util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// so after login, we do generate a JWT token so that we do not have to pass our credential again
// for that we need something that validates our token, this is that

// There exists SecurityContextHolder which is local storage for spring security to keep user info
// when spring security validates a token, it sets authentication into the SecurityContextHolder
// using that every controller, service, etc can know who has logged in.

@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter { // extends OncePerRequestFilter so that it only runs once for each request

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    // validation workflow - 1. Extract Authorization header
    //                       2. If null or doesn't start with "Bearer " → skip to filterChain.doFilter and return
    //                       3. Extract token (substring 7) and username from token
    //                       4. If username is not null AND SecurityContext has no auth yet:
    //                              → load UserDetails from DB
    //                              → validate token
    //                              → if valid, build auth token and set in SecurityContext
    //                       5. Always call filterChain.doFilter at the end

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username =  jwtUtil.extractUserName(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails user = customUserDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, user)) {

                // the arguments we pass are - userdetail, credentials, role
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // this attaches the IP address and session info to the authentication object
                SecurityContextHolder.getContext().setAuthentication(authToken); // setting in the info to the SecurityContextHolder finally
            }
        }
        filterChain.doFilter(request,response); // passes the request to the next filter in the chain
                                                // we must call it even if authentication fails or else request dies here
    }
}