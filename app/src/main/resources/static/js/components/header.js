function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (window.location.pathname.endsWith("/")) {
        localStorage.removeItem("userRole");
        headerDiv.innerHTML = `
          <header class="header">
            <div class="logo-section">
              <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
              <span class="logo-title">Hospital CMS</span>
            </div>
          </header>`;
        return;
    }
    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");
    let headerContent = `<header class="header">
        <div class="logo-section">
          <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
        <nav>`;
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }
    else if (role === "admin") {
        headerContent += `
          <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
          <a href="#" onclick="logout()">Logout</a>`;
    } else if (role === "doctor") {
        headerContent += `
          <button class="adminBtn"  onclick="selectRole('doctor')">Home</button>
          <a href="#" onclick="logout()">Logout</a>`;
    } else if (role === "patient") {
        headerContent += `
        <button id="patientLogin" class="adminBtn">Login</button>
        <button id="patientSignup" class="adminBtn">Sign Up</button>`;
    } else if (role === "loggedPatient") {
        headerContent += `
        <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
        <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
        <a href="#" onclick="logoutPatient()">Logout</a>`;
    }
    headerContent += `</nav></header>`;
    headerDiv.innerHTML = headerContent;
    attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
  const loginBtn = document.getElementById("patientLogin");
  const signupBtn = document.getElementById("patientSignup");

  // Enlaza eventos de manera segura si los botones existen en el DOM
  if (loginBtn) {
    loginBtn.addEventListener("click", () => {
      // Lógica o modal para el Login del paciente
      console.log("Patient Login clicked");
    });
  }

  if (signupBtn) {
    signupBtn.addEventListener("click", () => {
      // Lógica o modal para el Registro del paciente
      console.log("Patient Sign Up clicked");
    });
  }
}

function logout(event) {
  if (event) event.preventDefault(); // Previene el comportamiento nativo del enlace <a>
  localStorage.removeItem("userRole");
  localStorage.removeItem("token");
  window.location.href = "/";
}

function logoutPatient(event) {
  if (event) event.preventDefault(); // Previene el comportamiento nativo del enlace <a>
  localStorage.removeItem("token");
  // Opcional: Si necesitas limpiar el rol al salir, descomenta la siguiente línea:
  // localStorage.removeItem("userRole"); 
  window.location.href = "/pages/loggedPatientDashboard.html";
}

document.addEventListener("DOMContentLoaded", renderHeader);
