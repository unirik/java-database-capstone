package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.GeneralService;
import com.project.back_end.DTO.Login;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final GeneralService service;

    @Autowired
    public DoctorController(DoctorService doctorService, GeneralService service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user, @PathVariable Long doctorId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        LocalDateTime dateTime = date.atStartOfDay();
        ResponseEntity<Map<String,String>> temp = service.validateToken(token, user);
        if (!temp.getBody().isEmpty()) {
            response.putAll(temp.getBody());
            return new ResponseEntity<>(response, temp.getStatusCode());
        }
        response.put("message", doctorService.getDoctorAvailability(doctorId, dateTime));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.getDoctors();
        
        response.put("doctors", doctors);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(@RequestBody @Valid Doctor doctor, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        ResponseEntity<Map<String,String>> temp = service.validateToken(token, "admin");
        if (!temp.getBody().isEmpty()) return temp;
        int isSaved = doctorService.saveDoctor(doctor);
        if (isSaved == 1) {
            response.put("message", "Doctor has been saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (isSaved == -1) {
            response.put("message", "Doctor already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        response.put("message", "An Unexpected Error Occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody @Valid Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@RequestBody @Valid Doctor doctor, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        ResponseEntity<Map<String, String>> temp = service.validateToken(token, "admin");
        if (!temp.getBody().isEmpty()) return temp;
        int isUpdated = doctorService.updateDoctor(doctor);
        if (isUpdated == 1) {
            response.put("message", "Doctor has been updated successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else if (isUpdated == -1) {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("message", "An Unexpected Error Occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        ResponseEntity<Map<String, String>> temp = service.validateToken(token, "admin");
        if (!temp.getBody().isEmpty()) return temp;
        int isDeleted = doctorService.deleteDoctor(id);
        if (isDeleted == 1) {
            response.put("message", "Doctor has been deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else if (isDeleted == -1) {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("message", "An Unexpected Error Occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(@PathVariable String name, @PathVariable String time, @PathVariable String speciality) {
        Map<String, Object> response = new HashMap<>();
        response = service.filterDoctor(name, speciality, time);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
