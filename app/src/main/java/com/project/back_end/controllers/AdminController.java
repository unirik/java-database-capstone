package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.services.GeneralService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {
    private final GeneralService service;

    public AdminController(GeneralService service) {
        this.service = service;
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody String username, @RequestBody String password) {
        Map<String, Object> response = new HashMap<>();

        ResponseEntity<Object> isAuthenticated = service.validateAdmin(username, password);

        if (isAuthenticated.getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
