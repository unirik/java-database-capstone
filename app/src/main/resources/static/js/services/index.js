import { openModal } from "../components/modals.js";
import { BASE_URL } from "../config/config.js";

const ADMIN_API = `${BASE_URL}/admin/login`;
const DOCTOR_API = `${BASE_URL}/doctor/login`;

window.onload = function () {
    const adminLoginBtn = document.getElementById("adminLogin");
    const doctorLoginBtn = document.getElementById("doctorLogin");

    if (adminLoginBtn) {
      adminLoginBtn.addEventListener("click", () => {
        openModal("adminLogin");
      });
    }

    if (doctorLoginBtn) {
      doctorLoginBtn.addEventListener("click", () => {
        openModal("doctorLogin");
      });
    }
};

window.adminLoginHandler = async function (event) {
    if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
    }

    try {
        const usernameInput = document.getElementById("adminUsername");
        const passwordInput = document.getElementById("adminPassword");

        if (!usernameInput || !passwordInput) {
            alert("Required login fields are missing from the DOM.");
            return;
        }

        const username = usernameInput.value.trim();
        const password = passwordInput.value;

        if (!username || !password) {
            alert("Please fill in all fields.");
            return;
        }

        const adminCredentials = { username, password };

        const response = await fetch(ADMIN_API, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(adminCredentials),
        });

        if (response.ok) {
            const data = await response.json();

            if (data && data.token) {
                localStorage.setItem("token", data.token);
                localStorage.setItem("userRole", "admin");

                if (typeof window.selectRole === "function") {
                    window.selectRole("admin");
                } else {
                    window.location.href = "/pages/adminDashboard.html";
                }
            } else {
                alert("Login successful, but no token was returned from the server.");
            }
        } else {
            const errorData = await response.json().catch(() => ({}));
            const errorMsg = errorData.message || "Invalid admin credentials.";
            alert(errorMsg);
        }
    } catch (error) {
        console.error("Admin login error:", error);
        alert("A network or server error occurred. Please try again later.");
    }
};

window.doctorLoginHandler = async function (event) {
    if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
    }

    try {
        const emailInput = document.getElementById("doctorEmail");
        const passwordInput = document.getElementById("doctorPassword");

        if (!emailInput || !passwordInput) {
            alert("Required login fields are missing from the DOM.");
            return;
        }

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        if (!email || !password) {
            alert("Please fill in all fields.");
            return;
        }

        const doctorCredentials = { email, password };

        const response = await fetch(DOCTOR_API, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(doctorCredentials),
        });

        if (response.ok) {
            const data = await response.json();

            if (data && data.token) {
                localStorage.setItem("token", data.token);
                localStorage.setItem("userRole", "doctor");

                if (typeof window.selectRole === "function") {
                    window.selectRole("doctor");
                } else {
                    window.location.href = "/pages/doctorDashboard.html";
                }
            } else {
                alert("Login successful, but no token was returned from the server.");
            }
        } else {
            const errorData = await response.json().catch(() => ({}));
            const errorMsg = errorData.message || "Invalid doctor credentials.";
            alert(errorMsg);
        }
    } catch (error) {
        console.error("Doctor login error:", error);
        alert("A network or server error occurred. Please try again later.");
    }
};
