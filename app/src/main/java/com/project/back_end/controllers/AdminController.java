package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.back_end.models.Admin;
import com.project.back_end.services.GeneralService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("${api.path}admin/login")
public class AdminController {
    private final GeneralService service;

    @Autowired
    public AdminController(GeneralService service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return service.validateAdmin(admin);
    }

    @GetMapping("/dashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Map<String, String> map = service.validateToken(token,"admin").getBody();
        if (map==null) {
            return "admin/adminDashboard";
        }
        return "redirect:http://localhost:8080/";
    }
}
