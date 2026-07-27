package com.project.back_end.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {
    private final TokenValidationService tokenValidationService;

    @Autowired
    public DashboardController(TokenValidationService tokenValidationService) {
        this.tokenValidationService = tokenValidationService;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable("token") String token) {
        boolean isValid = tokenValidationService.validateToken(token, "admin");
        
        if (isValid) {
            return "admin/adminDashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable("token") String token) {
        boolean isValid = tokenValidationService.validateToken(token, "doctor");
        
        if (isValid) {
            return "doctor/doctorDashboard";
        }
        return "redirect:/";
    }
}
