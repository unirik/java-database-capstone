import {deleteDoctor} from "../services/doctorServices";
import {getPatientData} from "../services/patientServices.js";
import {showBookingOverlay} from "../loggedPatient.js";

export function createDoctorCard(doctor) {
    const card = document.createElement("div");
    card.classList.add("doctor-card");
    
    const role = localStorage.getItem("userRole");
    
    const infoContainer = document.createElement("div");
    infoContainer.classList.add("doctor-info");

    const docName = document.createElement("h3");
    docName.textContent = `${doctor.name}`;

    const docSpecialization = document.createElement("p");
    docSpecialization.textContent = `Specialty: ${doctor.specialty}`;

    const docEmail = document.createElement("p");
    docEmail.textContent = `Email: ${doctor.email}`;

    const availableSlots = document.createElement("p");
    availableSlots.textContent = `Available: ${doctor.availableTimes.join(", ")}`;

    infoContainer.appendChild(docName);
    infoContainer.appendChild(docSpecialization);
    infoContainer.appendChild(docEmail);
    infoContainer.appendChild(availableSlots);

    const actionsContainer = document.createElement("div");
    actionsContainer.classList.add("card-actions");

    if (role === "admin") {
        const deleteBtn = document.createElement("button");
        deleteBtn.textContent = "Delete Doctor";

        deleteBtn.addEventListener("click", async () => {
            const confirmDelete = confirm(`Are you sure you want to delete Dr. ${doctor.name}?`);
            if (!confirmDelete) return;
            const token = localStorage.getItem("token");
            if (!token) {
                alert("Admin token not found. Please log in again.");
                return;
            }
            try {
                const {success, message} = await deleteDoctor(doctor.id, token);
                if (success) {
                    alert("Doctor removed successfully.");
                    card.remove();
                } else {
                    alert("Failed to delete doctor. Please try again.");
                }
            } catch (error) {
                console.error("Error deleting doctor:", error);
                alert("An error occurred while deleting the doctor.");
            }
        });
        actionsContainer.appendChild(deleteBtn);
    }

    else if (role === "patient") {
        const bookNowBtn = document.createElement("button");
        bookNowBtn.textContent = "Book Now";

        bookNowBtn.addEventListener("click", () => {
            alert("Please log in before booking an appointment.");
        });
        actionsContainer.appendChild(bookNowBtn);
    }

    else if (role === "loggedPatient") {
        const bookNowLoggedBtn = document.createElement("button");
        const token = localStorage.getItem("token");
        bookNowLoggedBtn.textContent = "Book Now";

        bookNowLoggedBtn.addEventListener("click", async (e) => {
            if (!token) {
                alert("Session invalid. Redirecting to login...");
                localStorage.setItem("userRole", "patient")
                window.location.href = "/pages/patientDashboard.html";
            }
            const patientData = await getPatientData(token);
            if (!patientData) {
                alert("Failed to fetch patient data");
                return;
            }
            showBookingOverlay(e, doctor, patientData);
        });
        actionsContainer.appendChild(bookNowLoggedBtn);
    }

    card.appendChild(infoContainer);
    card.appendChild(actionsContainer);

    return card;
}
