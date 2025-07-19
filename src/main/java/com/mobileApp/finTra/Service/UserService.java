package com.mobileApp.finTra.Service;

import com.mobileApp.finTra.Entity.UserModel;
import com.mobileApp.finTra.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public UserModel createUser(UserModel user) {
        return userRepository.save(user);
    }


    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }


    public Optional<UserModel> getUserById(Long id) {
        return userRepository.findById(id);
    }


    public Optional<UserModel> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
