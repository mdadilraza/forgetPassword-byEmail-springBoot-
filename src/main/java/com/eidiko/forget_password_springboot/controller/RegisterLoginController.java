package com.eidiko.forget_password_springboot.controller;

import com.eidiko.forget_password_springboot.dto.UserDto;
import com.eidiko.forget_password_springboot.dto.UserLoginDto;
import com.eidiko.forget_password_springboot.model.PasswordResetToken;
import com.eidiko.forget_password_springboot.model.User;
import com.eidiko.forget_password_springboot.repository.TokenRepository;
import com.eidiko.forget_password_springboot.repository.UserRepository;
import com.eidiko.forget_password_springboot.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class RegisterLoginController {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "registration";
    }

    @PostMapping("/register")
    public String saveUser(@ModelAttribute UserDto userDTO) {
        User user = userDetailsService.save(userDTO);
        if (user != null)
            return "redirect:/login";
        else
            return "redirect:/register";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute UserLoginDto userLoginDTO, Model model) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userLoginDTO.getUsername());
        if (userDetails != null)
            return "redirect:/userDashboard";

        return "redirect:/login";
    }


    @GetMapping("/userDashboard")
    public String showUserDashboardForm() {
        return "userDashboard";
    }

    @GetMapping("/forgotPassword")
    public String forgotPassword() {
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassordProcess(@ModelAttribute UserDto userDTO) {
        String output = "";
        User user = (User) userDetailsService.loadUserByUsername(userDTO.getEmail());
        if (user != null) {
            output = userDetailsService.sendEmail(user);
        }
        if (output.equals("success")) {
            return "redirect:/forgotPassword?success";
        }
        return "redirect:/login?error";
    }

    @GetMapping("/resetPassword/{token}")
    public String resetPasswordForm(@PathVariable String token, Model model) {
        PasswordResetToken reset = tokenRepository.findByToken(token);
        if (reset != null && userDetailsService.hasExipred(reset.getExpiryDateTime())) {
            model.addAttribute("email", reset.getUser().getEmail());
            return "resetPassword";
        }
        return "redirect:/forgotPassword?error";
    }

    @PostMapping("/resetPassword")
    public String passwordResetProcess(@ModelAttribute UserDto userDTO, User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getEmail());
        if (userDetails != null) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            userRepository.save(user);
        }

        return "redirect:/login";
    }

}
