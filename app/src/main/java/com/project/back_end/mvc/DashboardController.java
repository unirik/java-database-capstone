package com.project.back_end.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.project.back_end.services.GeneralService;
import java.util.Map;

@Controller
public class DashboardController {
    @Autowired
    GeneralService service;

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Map<String,String> response = service.validateToken(token, "admin").getBody();
        
        if (response.isEmpty()) {
            return "admin/adminDashboard";
        }
        return "redirect:http://localhost:8080";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        Map<String,String> response = service.validateToken(token, "doctor").getBody();
        
        if (response.isEmpty()) {
            return "doctor/doctorDashboard";
        }
        return "redirect:http://localhost:8080";
    }
}
