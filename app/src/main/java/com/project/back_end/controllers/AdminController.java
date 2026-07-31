package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {
    private final Service service;

    public AdminController(Service service) {
        this.service = service;
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody String username, @RequestBody String password) {
        Map<String, Object> response = new HashMap<>();

        boolean isAuthenticated = service.validateAdmin(username, password);

        if (isAuthenticated) {
            response.put("success", true);
            response.put("message", "Login exitoso.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
