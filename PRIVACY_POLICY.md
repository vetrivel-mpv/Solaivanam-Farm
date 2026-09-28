# Privacy Policy for SOLAIVANAM [சோலைவனம்]

**Last Updated:** September 27, 2026  
**Application Name:** SOLAIVANAM [சோலைவனம்]  
**Application ID:** `com.aistudio.solaivanam.orgfm`  
**Developer Contact:** vetrivelmpv02@gmail.com  

---

## 1. Introduction
SOLAIVANAM ("we", "our", or "us") operates the SOLAIVANAM mobile application, designed to facilitate farm-to-home organic produce ordering, customer delivery matrix management, and Bluetooth thermal receipt printing for sustainable farm initiatives.

We value your privacy and are committed to protecting your personal information. This Privacy Policy details what information we collect, why we collect it, how it is handled, and your rights concerning your personal data in accordance with Google Play Developer Program Policies.

---

## 2. Information We Collect

### A. Personal & Contact Information
When placing an order or using the ordering matrix, the application collects:
- **Name:** Used to label customer delivery packages and invoices.
- **Phone Number:** Used for order coordination, delivery contact, and generating user-initiated WhatsApp delivery notifications.
- **Address & Apartment Community:** Used to cluster and consolidate orders for community-based group deliveries.

### B. Transaction & Order Information
- **Order Details:** Selected organic farm items (e.g., Keerai varieties, cold-pressed oils, traditional grains, podis), quantities, order status, total price, and timestamps.
- **Invoice Logs:** Local records of invoices generated for billing and harvesting tracking.

### C. Technical & Device Information
- **Bluetooth State:** Detected paired Bluetooth POS thermal printers (e.g., Niyama BT-58, Everycom, Zjiang) to facilitate receipt printing.

---

## 3. Permissions & Device Hardware

### Bluetooth Permissions (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`)
- **Purpose:** The app requests Bluetooth permissions strictly to search for and connect to paired 58mm ESC/POS Bluetooth thermal printers to print paper receipts and farm harvest sheets.
- **`neverForLocation` Flag:** We explicitly declare `android:usesPermissionFlags="neverForLocation"` in our Android manifest. We **do not** use Bluetooth to derive, track, or record your physical GPS location.

---

## 4. How We Use Information
We use the collected information exclusively for:
1. Processing customer grocery orders and maintaining inventory levels.
2. Grouping orders by apartment complex to plan delivery routes.
3. Rendering receipt typography (including Unicode Tamil script rasterization) on Bluetooth thermal printers.
4. Enabling users to generate and share invoices directly through their preferred messaging apps (e.g., WhatsApp).

---

## 5. Data Storage & Security
- **Local On-Device Storage:** All order records, customer entries, and inventory levels are stored locally on your device in a secure SQLite database using Android Jetpack Room.
- **No Third-Party Advertising:** We do not embed ad networks, data brokers, or third-party trackers.
- **No Data Monetization:** We never sell, rent, or trade your personal data to any external parties.

---

## 6. Third-Party Services
- **Google Identity Services:** Optional sign-in integration to authenticate farm administrators and customers.
- **WhatsApp Intent:** Initiated only by explicit user interaction to send order summaries.

---

## 7. Data Retention and Deletion
You retain complete control over your data:
- **In-App Data Reset:** You can purge all locally stored orders and history anytime directly from the **Privacy Policy** dialog via the "Clear Local Data" button or by clearing the app data in Android System Settings.
- **Data Deletion Inquiries:** To request any further data assistance, contact the developer at **vetrivelmpv02@gmail.com**.

---

## 8. Children's Privacy
SOLAIVANAM does not address anyone under the age of 13. We do not knowingly collect personally identifiable information from children under 13.

---

## 9. Changes to This Privacy Policy
We may update our Privacy Policy periodically. Any revisions will be reflected in updated versions of the application and documented here.

---

## 10. Contact Us
If you have any questions or suggestions regarding our Privacy Policy or data handling practices, please contact:

- **Email:** vetrivelmpv02@gmail.com  
- **App:** SOLAIVANAM [சோலைவனம்]  
- **Package:** `com.aistudio.solaivanam.orgfm`
