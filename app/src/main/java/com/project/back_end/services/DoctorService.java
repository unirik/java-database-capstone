package com.project.back_end.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Appointment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    private final List<LocalTime> STANDARD_SLOTS = List.of(
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0),
            LocalTime.of(16, 0), LocalTime.of(17, 0)
    );

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<LocalTime> getDoctorAvailability(Long doctorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // Se obtienen las citas del médico en ese día (forzando eager load por repositorio)
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        // Extraer solo la hora de las citas ocupadas
        List<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(appointment -> appointment.getAppointmentTimeOnly())
                .collect(Collectors.toList());

        // Filtrar los bloques estándar quitando los que ya están ocupados
        /*return STANDARD_SLOTS.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .collect(Collectors.toList());*/
        return bookedTimes;
    };

    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            Doctor existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
            if (existingDoctor != null) {
                return -1; // Conflicto: Email ya registrado
            }
            doctorRepository.save(doctor);
            return 1; // Éxito
        } catch (Exception e) {
            return 0; // Error interno
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null || !doctorRepository.existsById(doctor.getId())) {
                return -1; // No existe
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        // Inicialización explícita para evitar LazyInitializationException en relaciones complejas
        doctors.forEach(d -> {
            if (d.getAvailableTimes() != null) {
                d.getAvailableTimes().size(); 
            }
        });
        return doctors;
    }

    @Transactional
    public int deleteDoctor(Long doctorId) {
        try {
            if (!doctorRepository.existsById(doctorId)) {
                return -1;
            }
            // 1. Eliminar citas del médico primero para mantener integridad referencial
            appointmentRepository.deleteAllByDoctorId(doctorId);
            // 2. Eliminar el registro del médico
            doctorRepository.deleteById(doctorId);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public String validateDoctor(String email, String password) {
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) {
            return "Error: Médico no encontrado.";
        }
        
        // Verificación de contraseña (en producción usar BCryptPasswordEncoder)
        if (!doctor.getPassword().equals(password)) {
            return "Error: Contraseña incorrecta.";
        }

        // Generación del token si la autenticación es correcta
        return tokenService.generateToken(doctor.getEmail(), "DOCTOR");
    }

    @Transactional(readOnly = true)
    public List<Doctor> findDoctorByName(String name) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        doctors.forEach(d -> {
            if (d.getAvailableTimes() != null) d.getAvailableTimes().size();
        });
        return doctors;
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String timePeriod) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return filterDoctorByTime(doctors, timePeriod);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String timePeriod) {
        if (doctors == null || timePeriod == null) return Collections.emptyList();

        return doctors.stream().filter(doctor -> {
            List<LocalTime> availableTimes = doctor.getAvailableTimes();
            if (availableTimes == null || availableTimes.isEmpty()) return false;

            if ("AM".equalsIgnoreCase(timePeriod)) {
                return availableTimes.stream().anyMatch(time -> time.isBefore(LocalTime.of(12, 0)));
            } else if ("PM".equalsIgnoreCase(timePeriod)) {
                return availableTimes.stream().anyMatch(time -> !time.isBefore(LocalTime.of(12, 0)));
            }
            return false;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndTime(String name, String timePeriod) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        return filterDoctorByTime(doctors, timePeriod);
    }

    /**
     * Filtra médicos validando estrictamente el nombre (case-insensitive) y la especialidad.
     */
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
        return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByTimeAndSpecility(String timePeriod, String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return filterDoctorByTime(doctors, timePeriod);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorBySpecility(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByTime(String timePeriod) {
        List<Doctor> allDoctors = doctorRepository.findAll();
        return filterDoctorByTime(allDoctors, timePeriod);
    }
}
