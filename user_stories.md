# 📋 User Stories - Medical Management System

This document contains the User Stories for the project, organized by system roles. Each story follows the standard Agile format (*As a, I want to, So that*) and includes its respective acceptance criteria and technical notes aligned with the system's polyglot architecture (Spring Boot + MySQL + MongoDB).

---

## 👤 1. Module: Administrator (Admin User Stories)

### US-01: Medical Staff Account Management
* **Description:**  
  **As an** System Administrator,  
  **I want to** register, edit, and deactivate medical staff accounts,  
  **So that** I can ensure only active and authorized personnel have access to the platform.

* **Acceptance Criteria:**
  - [ ] When registering a doctor, mandatory fields must be validated: Name, Specialty, License Number, and Email Address.
  - [ ] The system must automatically send an email to the doctor with their temporary access credentials.
  - [ ] Deleting a doctor account is not allowed if they have active appointments; instead, the account status must change to "Inactive".

* **Technical Notes:**  
  * All medical staff information is stored and managed within the relational database (**MySQL**).

---

### US-02: Global System Configuration
* **Description:**  
  **As an** System Administrator,  
  **I want to** define the standard duration of medical appointments and the clinic's operating hours,  
  **So that** I can standardize the global schedule across the platform.

* **Acceptance Criteria:**
  - [ ] The administrator can configure appointment intervals of 15, 30, 45, or 60 minutes via the Thymeleaf Admin dashboard.
  - [ ] The system must automatically block appointment bookings outside the defined global clinic operating hours.

---

## 👤 2. Module: Patients (Patient User Stories)

### US-03: Booking Medical Appointments
* **Description:**  
  **As a** Registered Patient,  
  **I want to** search for doctors by specialty and select an available date and time,  
  **So that** I can book my medical consultation autonomously.

* **Acceptance Criteria:**
  - [ ] The system must display a calendar showing the real-time availability of the selected doctor.
  - [ ] Upon booking confirmation, the appointment status changes to "Confirmed" and the time slot is instantly locked in the doctor's schedule.
  - [ ] The patient must receive a visual confirmation notification within the web interface.

* **Technical Notes:**  
  * This functionality consumes endpoints exposed by the **REST API**. Both availability queries and appointment creation are handled in **MySQL**.

---

### US-04: Canceling and Rescheduling Appointments
* **Description:**  
  **As a** Registered Patient,  
  **I want to** cancel or reschedule my upcoming appointments from my dashboard,  
  **So that** I can free up the time slot if I am unable to attend.

* **Acceptance Criteria:**
  - [ ] Patients can only cancel or reschedule appointments with a minimum of 24 hours' notice.
  - [ ] Upon cancellation, the appointment status changes to "Canceled" and the time slot must become instantly available for other patients.

---

## 👤 3. Module: Doctors (Doctor User Stories)

### US-05: Calendar and Availability Management
* **Description:**  
  **As a** Doctor,  
  **I want to** define and modify my weekly working hours and block specific holidays,  
  **So that** I can control the days and times patients can book appointments with me.

* **Acceptance Criteria:**
  - [ ] The doctor's Thymeleaf dashboard must display a programmable weekly grid from Monday to Sunday.
  - [ ] If the doctor blocks a specific time slot or range, it must be hidden from patient search results.

---

### US-06: Schedule Consultation and Medical Prescriptions
* **Description:**  
  **As a** Doctor,  
  **I want to** view my daily appointment list and record the diagnosis along with the patient's prescription,  
  **So that** I can maintain a digitalized clinical history.

* **Acceptance Criteria:**
  - [ ] Clicking on a daily appointment from the Thymeleaf dashboard must open an interactive form to input the diagnosis and prescription details.
  - [ ] Upon saving, the appointment status must be updated to "Attended" in the relational database.
  - [ ] Specific prescription details (medications, dosage, instructions) must be saved seamlessly.

* **Technical Notes:**  
  * This user story interacts with both databases through the common **Service Layer**: it updates the workflow status in **MySQL** and inserts the clinical prescription as a new document in **MongoDB**