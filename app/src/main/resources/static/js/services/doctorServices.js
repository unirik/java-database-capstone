import { BASE_URL } from "../config/config.js";

const DOCTOR_API = `${BASE_URL}/doctor`;

export async function getDoctors() {
    try {
        const response = await fetch(DOCTOR_API);
        const data = await response.json();
        return data.doctors;
    } catch (error) {
        console.error("Error fetching doctors:", error);
        return [];
    }
}

export async function deleteDoctor(doctorId, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${doctorId}/${token}`, {
            method: "DELETE"
        });
        const data = await response.json();
        return {
            success: response.ok,
            message: data.message
        };
    } catch (error) {
        console.error(`Error deleting doctor with ID ${doctorId}:`, error);
        return {
            success: false,
            message: "An error occurred while deleting the doctor."
        };
    }
}

export async function saveDoctor(doctorData, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${token}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(doctorData)
        });
        const data = await response.json();
        return {
            success: response.ok,
            message: data.message
        };
    } catch (error) {
        console.error("Error saving new doctor:", error);
        return {
            success: false,
            message: "An error occurred while saving the doctor."
        };
    }
}

export async function filterDoctors(name, time, specialty) {
    try {
        const response = await fetch(`${DOCTOR_API}/filter/${name}/${time}/${specialty}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });
        if (response.ok) {
            const data = await response.json();
            return data;
        } else {
            console.error(`Failed to filter doctors. Status: ${response.status}`);
            return { doctors: [] };
        }
    } catch (error) {
        console.error("Critical error in filterDoctors:", error);
        alert("There was an unexpected problem filtering the doctors list.");
        return { doctors: [] };
    }
}
