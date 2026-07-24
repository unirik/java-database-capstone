import {deleteDoctor} from "./doctorServices";
import {getPatientDetails} from "./patientServices.js";

export function createDoctorCard(doctor) {
    const card = document.createElement("div");
    card.className = "doctor-card";
    card.dataset.id = doctor.id;
    
    const role = localStorage.getItem("userRole");
    
    const infoContainer = document.createElement("div");
    infoContainer.className = "doctor-info";

    const docName = document.createElement("h3");
    docName.textContent = doctor.name || "Unknown Doctor";

    const docSpecialization = document.createElement("p");
    docSpecialization.className = "specialization";
    docSpecialization.textContent = `Specialty: ${doctor.specialization || "General"}`;

    const docEmail = document.createElement("p");
    docEmail.className = "email";
    docEmail.textContent = `Email: ${doctor.email || "N/A"}`;

    const timesContainer = document.createElement("div");
    timesContainer.className = "available-times";
    const timesTitle = document.createElement("span");
    timesTitle.textContent = "Available Times: ";
    timesContainer.appendChild(timesTitle);

    if (doctor.availableTimes && doctor.availableTimes.length > 0) {
        doctor.availableTimes.forEach((time) => {
            const timeBadge = document.createElement("span");
            timeBadge.className = "time-badge";
            timeBadge.textContent = time;
            timesContainer.appendChild(timeBadge);
        });
    } else {
        const noTimes = document.createElement("span");
        noTimes.textContent = "No availability";
        timesContainer.appendChild(noTimes);
    }

    infoContainer.appendChild(docName);
    infoContainer.appendChild(docSpecialization);
    infoContainer.appendChild(docEmail);
    infoContainer.appendChild(timesContainer);

    const actionsContainer = document.createElement("div");
    actionsContainer.className = "card-actions";

    if (role === "admin") {
        const deleteBtn = document.createElement("button");
        deleteBtn.className = "btn-delete";
        deleteBtn.textContent = "Delete Doctor";

        deleteBtn.addEventListener("click", async () => {
            if (confirm(`Are you sure you want to delete Dr. ${doctor.name}?`)) {
                const token = localStorage.getItem("token");
            
                try {
                    const response = await deleteDoctor(doctor.id, token);
                    if (response && response.success) {
                        alert("Doctor removed successfully.");
                        card.remove(); // Remueve el elemento directamente del DOM
                    } else {
                        alert("Failed to delete doctor. Please try again.");
                    }
                } catch (error) {
                console.error("Error deleting doctor:", error);
                alert("An error occurred while deleting the doctor.");
            }
        }});
        actionsContainer.appendChild(deleteBtn);
    }

    else if (role === "patient" || !role) {
        const bookNowBtn = document.createElement("button");
        bookNowBtn.className = "btn-book-now";
        bookNowBtn.textContent = "Book Now";

        bookNowBtn.addEventListener("click", () => {
            alert("Please log in before booking an appointment.");
        });
        actionsContainer.appendChild(bookNowBtn);
    }

    else if (role === "loggedPatient") {
        const bookNowLoggedBtn = document.createElement("button");
        bookNowLoggedBtn.className = "btn-book-now";
        bookNowLoggedBtn.textContent = "Book Now";

        bookNowLoggedBtn.addEventListener("click", async () => {
            const token = localStorage.getItem("token");

            if (!token) {
                alert("Session invalid. Redirecting to login...");
                window.location.href = "/";
                return;
            }

            try {
                const patientData = await getPatientDetails(token);

                if (patientData) {
                    showBookingOverlay(doctor, patientData);
                } else {
                    alert("Could not retrieve patient records.");
                }
            } catch (error) {
                console.error("Error fetching patient details:", error);
                alert("Error loading booking details.");
            }
        });
        actionsContainer.appendChild(bookNowLoggedBtn);
    }

    card.appendChild(infoContainer);
    card.appendChild(actionsContainer);

    return card;
}

function showBookingOverlay(doctor, patient) {
    console.log("Opening overlay with:", { doctor, patient });
    
    // Ejemplo conceptual: Asumiendo que existe una función global o elemento modal
    if (typeof window.openModal === "function") {
      // Guardamos temporalmente los datos en window o pasamos al modal
      window.currentBookingData = { doctor, patient };
      window.openModal("booking");
    } else {
      alert(`Booking modal opened for Patient: ${patient.name} with Dr. ${doctor.name}`);
    }
}
