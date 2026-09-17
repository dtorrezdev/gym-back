package com.dtorrez.gym.security.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/**
 *
 * @author alepaco.maton
 */
@Component
public class JwtUserDetailsService implements UserDetailsService, Serializable {

    private static final long serialVersionUID = 1L;
    
    /*@Autowired
    private IUsuarioRepository userRepository;*/
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
        return new User(username, "$2y$11$V.E4/XV6cC2ws7Xm7pojBuDRZQZjJFDbgAW1usXIZroYfAbBWIuCu", authorities);
    }

    /*@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = userRepository.findByNombreUsuario(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getNombreUsuario(), user.getContrasena(), new ArrayList<>());
    }*/
}
