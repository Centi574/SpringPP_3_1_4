package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserDetailsServiceImpl;

import javax.validation.Valid;
import java.util.Set;

@Controller
@RequestMapping(value = "/user")
public class UsersController {

    private final UserDetailsServiceImpl userService;

    @Autowired
    public UsersController(UserDetailsServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping()
    public String defaultUserPage(Model model, Authentication authentication) {
        String name = authentication.getName();
        User user = userService.findByName(name);
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping(value = "/{id}")
    public String showUserById(Model model, @PathVariable(value = "id") int id, Authentication authentication) {
        checkUsersAccess(id, authentication);
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("authenticatedUser", userService.getAuthenticatedUser());
        model.addAttribute("userList", userService.getAllUsers());
        return "show";
    }

    @PatchMapping(value = "/{id}")
    public String updateUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                             @PathVariable(value = "id") int id,
                             Authentication authentication, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authenticatedUser", userService.getAuthenticatedUser());
            model.addAttribute("userList", userService.getAllUsers());
            return "show";
        }
        userService.updateById(user, id);
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/allUsers";
        } else {
            return "redirect:/user";
        }
    }

    @DeleteMapping("/{id}")
    public String removeUserById(@PathVariable(value = "id") int id, Authentication authentication) {
        userService.removeUser(id);
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/allUsers";
        } else {
            return "redirect:/";
        }
    }

    private void checkUsersAccess(int id, Authentication authentication) {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String name = authentication.getName();
        User user = userService.findByName(name);
        if (user.getId() != id && !roles.contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }
    }
}