package com.example.kingpool.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OTPService {

    private static class OTPData {
        String otp;
        LocalDateTime expiry;

        OTPData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }

    private final Map<String, OTPData> otpStorage = new HashMap<>();

    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        otpStorage.put(email, new OTPData(otp, LocalDateTime.now().plusMinutes(5)));
        System.out.println("⚠️ OTP cho " + email + ": " + otp);
        return otp;
    }

    public boolean validateOtp(String email, String inputOtp) {
        OTPData data = otpStorage.get(email);
        if (data == null || LocalDateTime.now().isAfter(data.expiry)) {
            return false;
        }
        return data.otp.equals(inputOtp);
    }

    public void clearOtp(String email) {
        otpStorage.remove(email);
    }
}