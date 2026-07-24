function renderFooter() {
    const footerDiv = document.getElementById("footer");
    let footerContent = `
    <footer class="footer">
        <div class="footer-container">
    `;
    footerContent += `
        <div class="footer-logo">
          <img src="../assets/images/logo/logo.png" alt="Hospital CMS Logo">
          <p>© Copyright 2025. All Rights Reserved by Hospital CMS.</p>
        </div>
    `;
    footerContent += `
        <div class="footer-links">
    `;
    footerContent += `
        <div class="footer-column">
          <h4>Company</h4>
          <a href="#">About</a>
          <a href="#">Careers</a>
          <a href="#">Press</a>
        </div>
    `;
    footerContent += `
        <div class="footer-column">
          <h4>Support</h4>
          <a href="#">Account</a>
          <a href="#">Help Center</a>
          <a href="#">Contact Us</a>
        </div>
    `;
    footerContent += `
        <div class="footer-column">
          <h4>Legals</h4>
          <a href="#">Terms & Conditions</a>
          <a href="#">Privacy Policy</a>
          <a href="#">Licensing</a>
        </div>
    `;
    footerContent += `
        </div>
    `;
    footerContent += `
        </div>
    </footer>
    `;
    footerDiv.innerHTML = footerContent;
}
document.addEventListener("DOMContentLoaded", renderFooter);
