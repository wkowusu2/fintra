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

    public Optional<UserModel> editUser(Long id, UserModel updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            if (updatedUser.getFirst_name() != null) {
                existingUser.setFirst_name(updatedUser.getFirst_name());
            }
            if (updatedUser.getPhone() != null) {
                existingUser.setPhone(updatedUser.getPhone());
            }
            if (updatedUser.getEmail() != null) {
                existingUser.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getLast_name() != null) {
                existingUser.setLast_name(updatedUser.getLast_name());
            }
            if (updatedUser.getMiddle_name() != null) {
                existingUser.setMiddle_name(updatedUser.getMiddle_name());
            }
            return userRepository.save(existingUser);
        });
    }
}
