package com.bank.itarch.service;

import com.bank.itarch.model.dto.LoginRequest;
import com.bank.itarch.model.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse getCurrentUser();
    void logout();
}
