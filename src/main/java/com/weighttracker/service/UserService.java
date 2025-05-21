package com.weighttracker.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.weighttracker.entity.User;
import com.weighttracker.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));
    
    // 基本的な権限（ROLE_USER）を追加
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    
    return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),
            true, true, true,
            authorities  // 空のArrayListではなく、権限リストを渡す
    );
}
    
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("このユーザー名は既に使用されています");
        }

        
        // パスワードをハッシュ化(セキュリティ対策)→新しいユーザーを作成
        User newUser = new User(username, passwordEncoder.encode(password) );
        return userRepository.save(newUser);
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String username = authentication.getName();
        Optional<User> currentUser = userRepository.findByUsername(username);
        return currentUser.orElse(null);
    }
    
    public Integer getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }
    
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定されたIDのユーザーが見つかりません: " + id));
    }
    
    public User updateTargetWeight(Integer userId, Double targetWeight) {
        User user = getUserById(userId);
        user.setTargetWeight(targetWeight);
        return userRepository.save(user);
    }

   

}
