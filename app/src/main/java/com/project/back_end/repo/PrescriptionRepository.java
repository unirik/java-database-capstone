package com.project.back_end.repo;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.project.back_end.models.Prescription;
import java.util.List;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String>{
    /**
     * This method retrieves a list of prescriptions associated with a specific appointment.
     * @param Long appointmentId
     * @return List<Prescription>
     */
    List<Prescription> findByAppointmentId(Long appointmentId);
}
