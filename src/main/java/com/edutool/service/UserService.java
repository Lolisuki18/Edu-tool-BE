package com.edutool.service;

import org.springframework.stereotype.Service;

import com.edutool.model.User;
import com.edutool.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }
}
