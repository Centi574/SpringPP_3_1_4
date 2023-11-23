package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RegistrationService;
import ru.kata.spring.boot_security.demo.service.UserDetailsServiceImpl;
import javax.validation.Valid;

@Controller
@RequestMapping(value = "/admin")
public class AdminsController {

    private final RegistrationService registrationService;

    private final UserDetailsServiceImpl userService;

    @Autowired
    public AdminsController(RegistrationService registrationService, UserDetailsServiceImpl userService) {
        this.registrationService = registrationService;
        this.userService = userService;
    }

    @GetMapping("/allUsers")
    public String printUserList(ModelMap model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        model.addAttribute("userList", userService.getAllUsers());
        model.addAttribute("name", name);
        return "allUsers";
    }

    @GetMapping(value = "/new")
    public String addUser(@ModelAttribute("user") User user, Model model) {
        model.addAttribute("roles", registrationService.getAllRoles());
        model.addAttribute("userList", userService.getAllUsers());
        model.addAttribute("authenticatedUser", userService.getAuthenticatedUser());
        return "new";
    }

    @PostMapping()
    public String saveUserByController(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", registrationService.getAllRoles());
            model.addAttribute("userList", userService.getAllUsers());
            model.addAttribute("authenticatedUser", userService.getAuthenticatedUser());
            return "new";
        }
        registrationService.saveUser(user);
        return "redirect:/admin/allUsers";
    }
}
