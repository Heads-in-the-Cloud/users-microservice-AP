package com.ss.utopia.restapi.services;

import com.ss.utopia.restapi.dao.UserRepository;
import com.ss.utopia.restapi.models.User;
import com.ss.utopia.restapi.models.UserPrincipal;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserPrincipalDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    public UserPrincipalDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(s).orElseThrow(
            () -> new UsernameNotFoundException("No user was found with this username.")
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        return userPrincipal;
    }
}