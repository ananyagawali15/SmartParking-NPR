🚗 Smart Parking System with Number Plate Recognition (NPR)

An intelligent Android-based Smart Parking Management System developed using Kotlin, Jetpack Compose, Flask, OpenCV, EasyOCR, and Retrofit.
The application automates vehicle parking by detecting number plates, recommending parking slots, managing slot availability in real time, and integrating smart navigation & payment functionality.

📌 Project Overview

The main objective of this project is to reduce parking congestion and manual parking management by using:

OCR-based Number Plate Recognition
Real-time Parking Slot Monitoring
Smart Slot Allocation
Automated Slot Reservation
Digital Payment Integration

The system detects vehicle number plates from uploaded images and automatically manages parking slots using backend APIs.

✨ Features
🚘 Number Plate Detection
Upload vehicle image
Detects vehicle number using OCR
Implemented using:
OpenCV
EasyOCR

🅿 Smart Parking Slot Booking
Vehicle-based slot recommendation
Real-time slot availability
Occupied slots shown dynamically
Slot auto-selection & confirmation

⏳ Slot Timer & Auto Release
Countdown timer after booking
Automatic slot release after expiry
Manual slot release option

💳 Payment Module
Razorpay integration
Secure payment simulation
Booking confirmation after payment

🗺 Google Maps Navigation
Navigate directly to parking location
Google Maps integration

📡 REST API Communication

Backend APIs created using Flask:

/detect
/book
/slots
/release

🛠 Tech Stack
Frontend
Kotlin
Jetpack Compose
Material 3
Backend
Python
Flask
OCR & Image Processing
OpenCV
EasyOCR
Networking
Retrofit
Payment Gateway
Razorpay

⚙ System Workflow
User logs into the app
Uploads vehicle image
OCR detects number plate
User selects vehicle type
Available parking slots are displayed
User books a slot
Payment process starts
Navigation opens in Google Maps
Slot remains occupied until:
Timer expires OR
User manually releases slot

| API        | Method | Purpose              |
| ---------- | ------ | -------------------- |
| `/detect`  | POST   | Detect number plate  |
| `/book`    | POST   | Reserve parking slot |
| `/slots`   | GET    | Fetch slot status    |
| `/release` | POST   | Release parking slot |

🚀 Future Enhancements
Admin Dashboard
Cloud Database Integration
Live Camera Detection
QR-based Parking Entry
AI-based Slot Prediction
Email/SMS Notifications

📂 Project Structure
smartparking/
│
├── app/                  # Android frontend
├── backend/              # Flask backend
├── yolo_ocr_app.py       # OCR + Slot APIs
├── RetrofitClient.kt
├── ApiService.kt
└── README.md

▶ How to Run
Backend
cd backend
venv\Scripts\activate
python yolo_ocr_app.py

Backend runs on:
http://127.0.0.1:5000

Android App
Open project in Android Studio
Connect emulator/device
Run the app

🎯 Project Outcome

The Smart Parking System successfully automates:

Vehicle identification
Slot allocation
Parking management
Payment flow
Slot tracking

This project demonstrates practical implementation of:

OCR
REST APIs
Android App Development
Backend Integration
Real-time Parking Automation

👩‍💻 Developed By

Ananya Gawali
MCA Student | Android & Python Developer
Project: Smart Parking System with NPR
