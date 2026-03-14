package com.splitwise.service;

import com.splitwise.dto.request.LoginRequest;
import com.splitwise.dto.request.RegisterRequest;
import com.splitwise.dto.response.AuthResponse;
import com.splitwise.model.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    User getCurrentUser(String email);
    User updateProfile(String email, String name, String avatar);
}
