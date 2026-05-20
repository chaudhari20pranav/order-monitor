package com.ordermonitor.controller;

import com.ordermonitor.dto.LoginRequest;
import com.ordermonitor.dto.RegisterRequest;
import com.ordermonitor.entity.User;
import com.ordermonitor.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles authentication pages: register, login, logout.
 * Uses HTTP session for state – no JWT or Spring Security.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------------
    // Root redirect
    // ---------------------------------------------------------------

    @GetMapping("/")
    public String root(HttpSession session) {
        if (userService.isLoggedIn(session)) {
            return userService.isAdmin(session) ? "redirect:/admin/dashboard" : "redirect:/subscriber/dashboard";
        }
        return "redirect:/login";
    }

    // ---------------------------------------------------------------
    // Register
    // ---------------------------------------------------------------

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        if (userService.isLoggedIn(session)) return "redirect:/";
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute RegisterRequest request,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(request);
            redirect.addFlashAttribute("successMsg", "Account created! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/register";
        }
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (userService.isLoggedIn(session)) return "redirect:/";
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@Valid @ModelAttribute LoginRequest request,
                          BindingResult result,
                          HttpSession session,
                          Model model) {
        if (result.hasErrors()) {
            return "auth/login";
        }
        try {
            User user = userService.login(request, session);
            return "ADMIN".equals(user.getRole()) ? "redirect:/admin/dashboard" : "redirect:/subscriber/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/login";
        }
    }

    // ---------------------------------------------------------------
    // Logout
    // ---------------------------------------------------------------

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirect) {
        userService.logout(session);
        redirect.addFlashAttribute("successMsg", "You have been logged out.");
        return "redirect:/login";
    }
}