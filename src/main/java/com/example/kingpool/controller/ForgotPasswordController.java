package com.example.kingpool.controller;

import com.example.kingpool.entity.User;
import com.example.kingpool.repository.UserRepository;
import com.example.kingpool.service.EmailService;
import com.example.kingpool.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/forgot")
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String forgotForm() {
        return "auth/forgot-password";
    }

    @PostMapping
    public String sendOtp(@RequestParam String email, Model model) {
        if (!userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email không tồn tại.");
            return "auth/forgot-password";
        }

        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);
        model.addAttribute("email", email);
        model.addAttribute("message", "Mã OTP đã được gửi tới email của bạn.");
        return "auth/verify-otp";
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        if (!otpService.validateOtp(email, otp)) {
            model.addAttribute("error", "Mã OTP không đúng hoặc đã hết hạn.");
            model.addAttribute("email", email);
            return "auth/verify-otp";
        }
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String email, @RequestParam String password, Model model) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Không tìm thấy người dùng.");
            return "auth/forgot-password";
        }
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        otpService.clearOtp(email);
        model.addAttribute("success", "Mật khẩu đã được cập nhật thành công.");
        return "auth/login";
    }

    @PostMapping("/resend")
    public String resendOtp(@RequestParam String email, Model model) {
        if (!userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email không tồn tại.");
            return "auth/forgot-password";
        }

        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);
        model.addAttribute("email", email);
        model.addAttribute("message", "Mã OTP mới đã được gửi lại.");
        return "auth/verify-otp";
    }

}
