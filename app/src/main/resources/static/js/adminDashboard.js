import { getDoctors, saveDoctor, filterDoctors } from "../services/doctorServices.js";
import { createDoctorCard } from "../components/doctorCard.js";
import { openModal, closeModal } from "../utils/modal.js";

const getContentArea = () => document.getElementById("doctorsContainer") || document.getElementById("content-area");

function setupAddDoctorButton() {
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) {
      addDocBtn.addEventListener("click", () => {
        openModal("addDoctor");
      });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    setupAddDoctorButton();
    setupFilterListeners();

    loadDoctorCards();

    const addDoctorForm = document.getElementById("addDoctorForm");
    if (addDoctorForm) {
        addDoctorForm.addEventListener("submit", adminAddDoctor);
    }
});

async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();

        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Error loading doctor cards on admin dashboard:", error);
    }
}

function setupFilterListeners() {
    const searchBar = document.getElementById("searchBar");
    const timeFilter = document.getElementById("timeFilter");
    const specialtyFilter = document.getElementById("specialtyFilter");

    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
    if (timeFilter) timeFilter.addEventListener("change", filterDoctorsOnChange);
    if (specialtyFilter) specialtyFilter.addEventListener("change", filterDoctorsOnChange);
}

async function filterDoctorsOnChange() {
    try {
        const searchBar = document.getElementById("searchBar");
        const timeFilter = document.getElementById("timeFilter");
        const specialtyFilter = document.getElementById("specialtyFilter");

        const name = searchBar && searchBar.value.trim() !== "" ? searchBar.value.trim() : null;
        const time = timeFilter && timeFilter.value !== "" ? timeFilter.value : null;
        const specialty = specialtyFilter && specialtyFilter.value !== "" ? specialtyFilter.value : null;

        const response = await filterDoctors(name, time, specialty);
        const filteredDoctors = response.doctors || [];

        const contentArea = getContentArea();
        if (!contentArea) return;

        contentArea.innerHTML = "";

        if (filteredDoctors.length > 0) {
            renderDoctorCards(filteredDoctors);
        } else {
            const noResultsMsg = document.createElement("p");
            noResultsMsg.className = "no-results-message";
            noResultsMsg.textContent = "No doctors found with the given filters.";
            contentArea.appendChild(noResultsMsg);
        }
    } catch (error) {
        console.error("Filtering error:", error);
        alert("An error occurred while filtering the doctors list.");
    }
}

function renderDoctorCards(doctors) {
    const contentArea = getContentArea();
    if (!contentArea) return;

    contentArea.innerHTML = "";

    doctors.forEach((doctor) => {
        const cardElement = createDoctorCard(doctor);
        contentArea.appendChild(cardElement);
    });
}

async function adminAddDoctor(event) {
    if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
    }

    const token = localStorage.getItem("token");

    if (!token) {
        alert("Authentication token not found. Please log in again.");
        window.location.href = "/";
        return;
    }

    try {
        const nameInput = document.getElementById("newDocName");
        const emailInput = document.getElementById("newDocEmail");
        const phoneInput = document.getElementById("newDocPhone");
        const passwordInput = document.getElementById("newDocPassword");
        const specialtyInput = document.getElementById("newDocSpecialty");
        const timesInput = document.getElementById("newDocTimes"); // Puede ser un input separado por comas o select múltiple

        let availableTimes = [];
        if (timesInput && timesInput.value) {
            availableTimes = timesInput.value.split(",").map(t => t.trim()).filter(t => t !== "");
        }

        const newDoctor = {
            name: nameInput ? nameInput.value.trim() : "",
            email: emailInput ? emailInput.value.trim() : "",
            phone: phoneInput ? phoneInput.value.trim() : "",
            password: passwordInput ? passwordInput.value : "",
            specialization: specialtyInput ? specialtyInput.value : "",
            availableTimes: availableTimes
        };

        if (!newDoctor.name || !newDoctor.email || !newDoctor.password) {
            alert("Please fill in all mandatory fields (Name, Email, and Password).");
            return;
        }

        const result = await saveDoctor(newDoctor, token);

        if (result && result.success) {
            alert(result.message || "Doctor added successfully!");

            closeModal("addDoctor");
            window.location.reload();
        } else {
            alert(result.message || "Failed to add the doctor. Please verify the information.");
        }
    } catch (error) {
        console.error("Error inside adminAddDoctor execution:", error);
        alert("A critical error occurred while communicating with the server.");
    }
}

window.adminAddDoctor = adminAddDoctor;
