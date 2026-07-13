# 🗄️ Database Schema Design - Medical Management System

This document details the polyglot persistence layer design for the platform, combining a relational database (**MySQL**) for structured transactional data and a NoSQL document database (**MongoDB**) for flexible, semi-structured data.

---

## 1. MySQL Relational Database Design

The relational database ensures strict consistency, referential integrity, and ACID compliance for core administrative entities. All primary keys use auto-incremented integers, and foreign keys enforce cascading updates and restrictions.

### 📊 Entity-Relationship Quick Map
* `admins` (Independent)
* `doctors` (Independent)
* `patients` (Independent)
* `appointments` (Many-to-One with `patients`, Many-to-One with `doctors`)

---

### 📋 Table Details

#### 1.1 `admins` Table
Stores authentication and profile information for system administrators.

| Column Name | Data Type | Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for the administrator. |
| `email` | VARCHAR(100) | UNIQUE, NOT NULL | Administrative email used for login. |
| `password` | VARCHAR(255) | NOT NULL | Hashed password (e.g., BCrypt). |
| `first_name` | VARCHAR(50) | NOT NULL | Administrator's first name. |
| `last_name` | VARCHAR(50) | NOT NULL | Administrator's last name. |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp. |

#### 1.2 `doctors` Table
Stores professional profiles and metadata for medical specialists.

| Column Name | Data Type | Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for the doctor. |
| `email` | VARCHAR(100) | UNIQUE, NOT NULL | Professional email used for login. |
| `password` | VARCHAR(255) | NOT NULL | Hashed password. |
| `first_name` | VARCHAR(50) | NOT NULL | Doctor's first name. |
| `last_name` | VARCHAR(50) | NOT NULL | Doctor's last name. |
| `specialty` | VARCHAR(100) | NOT NULL | Medical specialty (e.g., Cardiology). |
| `license_number`| VARCHAR(50) | UNIQUE, NOT NULL | Official medical board license. |
| `is_active` | BOOLEAN | DEFAULT TRUE | System status (Soft-delete alternative). |

#### 1.3 `patients` Table
Stores contact information and demographic details for registered patients.

| Column Name | Data Type | Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for the patient. |
| `email` | VARCHAR(100) | UNIQUE, NOT NULL | Patient's email address. |
| `first_name` | VARCHAR(50) | NOT NULL | Patient's first name. |
| `last_name` | VARCHAR(50) | NOT NULL | Patient's last name. |
| `phone_number` | VARCHAR(20) | NOT NULL | Contact telephone number. |
| `birth_date` | DATE | NOT NULL | Patient's date of birth. |

#### 1.4 `appointments` Table
Manages the scheduling links between patients and doctors.

| Column Name | Data Type | Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for the appointment. |
| `patient_id` | INT | FOREIGN KEY, NOT NULL | References `patients(id)`. |
| `doctor_id` | INT | FOREIGN KEY, NOT NULL | References `doctors(id)`. |
| `appointment_date`| DATETIME | NOT NULL | Scheduled date and time. |
| `status` | VARCHAR(20) | DEFAULT 'SCHEDULED' | Status: SCHEDULED, ATTENDED, CANCELED. |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Log date of booking. |

* **Foreign Key Constraints:**
  * `FK_appointment_patient`: `patient_id` references `patients(id)` ON DELETE RESTRICT ON UPDATE CASCADE.
  * `FK_appointment_doctor`: `doctor_id` references `doctors(id)` ON DELETE RESTRICT ON UPDATE CASCADE.

---

## 2. MongoDB Document Collection Design

To store unstructured clinical outputs that scale rapidly and require a flexible schema, we utilize **MongoDB**. The chosen collection is **`prescriptions`**. 

### 💡 Justification
Prescriptions can vary heavily between consultations (some have one medication, others have complex therapies, notes, or follow-ups). Storing this as an embedded document avoids expensive table joins and supports seamless updates without schema migrations.

### 📄 Realistic JSON Document Example (`prescriptions` collection)

This document embeds patient/doctor reference metadata and utilizes **arrays of objects** to track multiple prescribed items dynamically.

```json
{
  "_id": {"\$oid": "64b0f3e29f1b2c3d4e5f6a7b"},
  "appointment_id": 1045,
  "issued_date": "2026-07-13T14:00:00Z",
  "doctor": {
    "doctor_id": 14,
    "full_name": "Dr. Sarah Jenkins",
    "license_number": "MED-998822"
  },
  "patient": {
    "patient_id": 482,
    "full_name": "John Doe"
  },
  "clinical_summary": {
    "diagnosis": "Acute Bronchitis",
    "symptoms": ["Persistent cough", "Mild fever", "Fatigue"],
    "notes": "Patient should rest for 3 days and maintain high fluid intake."
  },
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "Every 8 hours",
      "duration_days": 7,
      "instructions": "Take orally after meals."
    },
    {
      "name": "Dextromethorphan",
      "dosage": "15mg/5mL",
      "frequency": "Every 6 hours as needed",
      "duration_days": 5,
      "instructions": "For cough suppression. Do not drive if drowsy."
    }
  ],
  "follow_up": {
    "required": true,
    "recommended_weeks": 2,
    "instructions": "Book a routine check-up if cough persists past day 7."
  }
}
```