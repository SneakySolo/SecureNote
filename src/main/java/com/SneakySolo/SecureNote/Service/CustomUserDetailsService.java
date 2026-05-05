package com.SneakySolo.SecureNote.Service;

import com.SneakySolo.SecureNote.Entity.User;
import com.SneakySolo.SecureNote.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // so for checking spring security needs the user but it has no idea about out USER entity,
    // it only recognizes data of type 'UserDetails'. So we do the translation here

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found!"));

        // this is needed as spring security looks for the 'ROLE_' prefix for any role so we add it here
        String userRole = "ROLE_" + user.getRole().name();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(userRole)))
                .build();

        // Note - A user ay have multiple role so we mush return collection but here we only return singleton version
        //      - the SimpleGrantedAuthority wraps your role string into a Spring Security object
    }
}
