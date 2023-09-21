package com.emedlogix.service;

import com.emedlogix.entity.User;
import com.emedlogix.entity.UserVO;
import com.emedlogix.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JwtUserDetailsService implements UserDetailsService {
   @Autowired
   private UserRepository userDao;

   @Autowired
   private PasswordEncoder bcryptEncoder;

   @Override
   public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
      User user = userDao.findByEmail(email);
      if (user == null) {
         throw new UsernameNotFoundException("User not found with username: " + email);
      }
      return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
            new ArrayList<>());
   }

   public User save(UserVO user) {
      User newUser = new User();
      newUser.setUsername(user.getUsername());
      newUser.setEmail(user.getEmail());
      newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
      return userDao.save(newUser);
   }
}