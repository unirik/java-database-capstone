import { getAllAppointments } from "../services/appointmentServices.js";
import { createPatientRow } from "../components/patientRow.js";

let appointmentsTableBody = null;
let selectedDate = null;
let token = null;
let patientName = null;

function initializeState() {
    appointmentsTableBody = document.getElementById("appointmentsTableBody");

    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    selectedDate = `${year}-${month}-${day}`;

    token = localStorage.getItem("token");

    patientName = "null"; // Inicializado como string "null" tal como lo espera el backend si está vacío

    const datePicker = document.getElementById("datePicker");
    if (datePicker) {
        datePicker.value = selectedDate;
    }
}

function setupEventListeners() {
    const searchBar = document.getElementById("searchBar");
    const todayBtn = document.getElementById("todayBtn");
    const datePicker = document.getElementById("datePicker");

    if (searchBar) {
        searchBar.addEventListener("input", (event) => {
            const value = event.target.value.trim();

            patientName = value !== "" ? value : "null";

            loadAppointments();
        });
    }

    if (todayBtn) {
        todayBtn.addEventListener("click", () => {
            const today = new Date();
            const year = today.getFullYear();
            const month = String(today.getMonth() + 1).padStart(2, '0');
            const day = String(today.getDate()).padStart(2, '0');

            selectedDate = `${year}-${month}-${day}`;

            if (datePicker) {
                datePicker.value = selectedDate;
            }

            loadAppointments();
        });
    }

    if (datePicker) {
        datePicker.addEventListener("change", (event) => {
            selectedDate = event.target.value;

            loadAppointments();
        });
    }
}

async function loadAppointments() {
    if (!appointmentsTableBody) return;

    if (!token) {
        appointmentsTableBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: red;">Authentication token missing. Please log in.</td></tr>`;
        return;
    }

    try {
        const appointments = await getAllAppointments(selectedDate, patientName, token);


        // Step 3: If no appointments are returned
          <tr>
            <td colspan="4" style="text-align: center; font-style: italic;">
              No Appointments found for today.
            </td>
          </tr>`;
            return;
        }

        // Step 4: If appointments exist
        appointments.forEach((appointment) => {
                id: appointment.patientId || appointment.id,
                name: appointment.patientName |                email: appointment.patientEmail || "N/A"
            };

            // Call createPatientRow to generate a table row for the appointment (pasa el objeto paciente o la cita según requiera tu componente)
            const rowElement = createPatientRow(patient, appointment);

            // Append each row to the table body

    } catch (error) {
ointments:", error);
        appointmentsTableBody.innerHTML = `
        <tr>
          <td colspan="4" style="text-align: center; color: var(--danger-color, red); font-weight: bold;">
            Error loa        </tr>`;
    }
}
/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
 

function renderContent() {
    console.log("Doctor layout rendered safely.");
}

document.addEventListener("DOMContentLoaded", () => {
    initializeState();
    setupEventListeners();

    renderContent();

    loadAppointments();
}); Initialize selectedDate with today's date in 'YYYY-MM-DD' format
