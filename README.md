# Zorvyn - Smart Financial Transaction Manager

Zorvyn is a high-performance, cross-platform financial management application built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. It allows users to track their incomes and expenses with an offline-first architecture, cloud synchronization, and AI-powered receipt scanning.

## 🚀 Features

-   **Dashboard**: Real-time spending analysis, budget tracking, and balance visualization.
-   **AI Receipt Scanning**: Instantly extract amounts, merchants, and transaction types from photos/screenshots of receipts (UPI, retail, etc.).
-   **Transaction History**: Search, filter, and manage your financial records with ease.
-   **Offline-First**: All data is stored locally in a type-safe SQL database for zero-latency access.
-   **Cloud Backup**: Seamless integration with **Firebase Firestore** for cross-device data continuity.
-   **Smart Savings**: Goal-based saving streaks and persistent budget alerts.
-   **Data Export**: Preview and share your transaction history as professionally formatted **CSV files**.
-   **Security**: Integrated **Biometric Authentication** (FaceID/Fingerprint) for your financial privacy.

## 🛠️ Tech Stack

-   **Core**: Kotlin Multiplatform (KMP)
-   **UI**: Compose Multiplatform (shared between Android & iOS)
-   **Database**: SqlDelight (Local persistence)
-   **Backend**: Firebase Firestore (Cloud persistence)
-   **Dependency Injection**: Koin
-   **Reactive Programming**: Kotlin Coroutines & StateFlow
-   **AI/OCR**: Google ML Kit (Android) & Apple Vision Framework (iOS)
-   **Images**: Peekaboo (Image picking & processing)
-   **Time**: kotlinx-datetime

## 🏛️ Architecture

Zorvyn follows a **Clean MVVM (Model-View-ViewModel)** architecture:
-   **Presentation Layer**: Compose Multiplatform views with state-driven ViewModels.
-   **Domain Layer**: Pure Kotlin business logic, interfaces, and models.
-   **Data Layer**: Offline-first repository pattern managing SqlDelight and Firestore syncing.

## ⚙️ Setup & Installation

### Prerequisites
-   **Android Studio** (Koala or newer)
-   **Xcode** (15.0+ for iOS targets)
-   **JDK 17** or 21

### Running the App
1.  **Clone the repository**:
    ```bash
    git clone [repository-url]
    cd Zorvyn_Assignment
    ```
2.  **Open in Android Studio**:
    Open the root directory and wait for Gradle sync to complete.
3.  **Run Android**:
    Select the `composeApp` configuration and pick an Android Emulator/Device.
4.  **Run iOS**:
    - Select the `iosApp` configuration in Android Studio.
    - Alternatively, open `iosApp/iosApp.xcworkspace` in Xcode.

## 📈 Technical Decisions

-   **Why KMP?**: To maintain 90%+ code sharing efficiency while keeping the performance and reliability of native platform APIs.
-   **Why SQLDelight?**: Type-safe SQL ensures financial calculations are accurate at the database level.
-   **AI Choice**: On-device OCR (ML Kit/Vision) was chosen over cloud-based AI to ensure 100% user privacy and offline functionality.

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
