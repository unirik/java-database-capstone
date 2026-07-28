package com.project.back_end.repo;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.back_end.models.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>{
    /**
     * This method retrieves a Patient by their email address.
     * @param String email
     * @return Patient
     */
    Patient findByEmail(String email);

    /**
     * This method retrieves a Patient by either their email or phone number, allowing flexibility for the search.
     * @param String email
     * @param String phone
     * @return Patient
     */
    Patient findByEmailOrPhone(String email, String phone);
}
