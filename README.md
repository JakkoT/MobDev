# IoT Bazaar Manager

**IoT Bazaar Manager** is an Android application designed to streamline the management of electronic components for the IoT class by focusing on an exceptional user experience. The app’s core function revolves around using the device camera for QR code scanning to quickly check items in and out. This simple, intuitive workflow ensures that students can easily log their transactions without a cumbersome process.

## Project Overview
A key UX requirement is a transparent and clean interface. The app uses a backend database to maintain a real-time inventory and provides each student with a clear view of exactly what they have borrowed.

### Main Features
- **QR Code Scanning:** Use the device camera to scan QR codes on components for quick check-in/out.
- **User Authentication:** Login and Registration via Email or Google Sign-In (Firebase).
- **Inventory Management:** Real-time view of component availability.
- **User Dashboard:** Clear view of borrowed items and their return deadlines.
- **Notifications:** Reminders for due dates on borrowed items.
- **Item Catalog:** Browse all available items and their stock status.

## Team Members
- **Jakko Turro** (@JakkoT) - Project Leader, Backend, Camera, UI
- **Kristjan Orrin** (@KristjanOrrin) - Frontend, UI/UX, Dashboard, Notifications
- **Artur Tamm** (@libakoer) - Lead Dev, Camera Integration, Auth, Inventory

## Installation & Build Instructions

### Prerequisites
- **Android Studio** (Latest version recommended)
- **JDK 11** or higher
- **Android Device** or Emulator running Android 14 (API 34) or higher
- **Firebase Configuration:** You must provide your own `google-services.json` file.

### Setup Steps
1.  **Clone the Repository:**
    ```bash
    git clone <repository-url>
    cd MobDev
    ```
2.  **Firebase Setup:**
    -   Create a project in the Firebase Console.
    -   Add an Android app with package name `ee.ut.cs.iotbazaar`.
    -   Download the `google-services.json` file.
    -   Place it in the `app/` directory: `MobDev/app/google-services.json`.
3.  **Build the Project:**
    Open the project in Android Studio and sync Gradle files, or run:
    ```bash
    ./gradlew assembleDebug
    ```
4.  **Run the App:**
    Connect a device or start an emulator and run:
    ```bash
    ./gradlew installDebug
    ```

## Usage Guide

1.  **Login/Register:** Start by creating an account or logging in with Google.
2.  **Home Screen:** View your currently borrowed items and scan history.
    -   Use the **Scan QR** button to borrow items (must be at the Delta building location).
    -   Use the **Return Item** button to return items.
3.  **Catalog:** Browse the full list of available components. Long-press an item to see details or delete (if admin).
4.  **Profile:** Access your account settings, dark mode toggle, and logout via the profile icon.

### Prototype Preview
*(Paper prototypes showing early design concepts)*
<p float="left">
  <img src="docs/paper-prototype/4060DDCB-7817-44AF-A4A5-AAAA30C620F7.JPG" width="200" />
  <img src="docs/paper-prototype/6D2770EB-61F4-49C4-9A6B-F5C3F5DD7F31.JPG" width="200" />
</p>

## Project Structure & File Index

The project follows the MVVM (Model-View-ViewModel) architecture. Below is a detailed explanation of every source file.

### **Core Application**
*   **`MainActivity.kt`**: The entry point of the application. It handles the bottom navigation setup, requests runtime permissions (Camera, Notifications), and schedules the background worker for return reminders.

### **UI Layer (Views & ViewModels)**

#### **Authentication**
*   **`ui/Login/LoginActivity.kt`**: Handles user login via Email/Password and Google Sign-In. Checks internet connectivity before attempting auth.
*   **`ui/Login/RegisterActivity.kt`**: Handles new user registration via Email/Password.
*   **`ui/Login/FirebaseUtils.kt`**: Contains helper functions, specifically `saveUserToFirestore` to ensure user data exists in the database after login.

#### **Home & Navigation**
*   **`ui/home/HomeFragment.kt`**: The main dashboard. Displays the user's borrowed items and scan history. Implements the location check (Delta building) logic before allowing scans.
*   **`ui/home/HomeViewModel.kt`**: Placeholder ViewModel for the home screen.
*   **`ui/home/ProfilePopupFragment.kt`**: A BottomSheetDialogFragment that provides a menu for navigation to Account, Inbox, Settings, and Logout.
*   **`ui/home/ScanHistoryAdapter.kt`**: RecyclerView adapter for displaying the user's scan history (Borrowed/Returned events).

#### **Camera & Scanning**
*   **`ui/camera/QrCodeScanner.kt`**: Integrates the Google Code Scanner (Play Services). Handles the actual scanning process and triggers item reservation or return logic based on the scanned result.
*   **`ui/camera/CameraViewModel.kt`**: Placeholder ViewModel for camera logic.
*   **`ui/camera/QrCodeResult.kt`**: A fragment used to display raw QR code results.

#### **Inventory & Items**
*   **`ui/catalog/CatalogFragment.kt`**: Displays the full list of available items. Allows users to view stock and add new items.
*   **`ui/item/ItemViewModel.kt`**: The core ViewModel for item management. Bridges the UI with `ItemRepository`. Handles fetching items, reserving/returning items, and updating stock.
*   **`ui/item/ItemAdapter.kt`**: A flexible RecyclerView adapter that can display items in two modes: "Catalog Mode" (showing stock status) and "Borrowed Mode" (showing return dates).

#### **User & Account**
*   **`ui/account/AccountFragment.kt`**: Displays user account details and provides a logout button.
*   **`ui/user/UserViewModel.kt`**: ViewModel for managing User data operations.
*   **`ui/inbox/InboxFragment.kt`**: Displays a list of other users (mock inbox) and fetches a "Quote of the Day" from DummyJSON API.
*   **`ui/inbox/InboxAdapter.kt`**: Adapter for displaying the list of users in the Inbox.

#### **Settings & Notifications**
*   **`ui/settings/SettingsFragment.kt`**: Allows users to toggle Dark Mode and trigger test notifications manually.
*   **`ui/notifications/NotificationsFragment.kt`**:  Fragment for notifications.
*   **`ui/notifications/NotificationsViewModel.kt`**: Notification ViewModel.
*   **`ui/dashboard/DashboardFragment.kt`** & **`DashboardViewModel.kt`**: Components for a dashboard view.

### **Data Layer (Repositories & Models)**

#### **Repositories**
*   **`repository/ItemRepository.kt`**: Manages all interactions with the `items` collection in Firestore. Handles complex transactional logic for borrowing and returning items (updating stock and user records simultaneously).
*   **`repository/UserRepository.kt`**: Manages interactions with the `users_real` collection in Firestore.

#### **Entities & Models**
*   **`data/entities/User.kt`**: Represents the User data structure stored in Firestore.
*   **`data/entities/TakenItem.kt`**: Represents an item currently borrowed by a user.
*   **`model/Item.kt`**: Domain model for an inventory item (Name, Stock, ID).
*   **`model/ScanHistoryItem.kt`**: Model for logging user actions (Borrow vs Return) in Firestore.
*   **`model/Quote.kt`**: Data class for parsing the JSON response from the Quotes API.

### **Infrastructure & Utilities**

#### **Networking**
*   **`api/RetrofitClient.kt`**: Singleton that provides the Retrofit instance configured for the external API.
*   **`api/QuoteApiService.kt`**: Interface defining the HTTP GET request for fetching random quotes.

#### **Background Work**
*   **`worker/ReturnNotificationWorker.kt`**: A WorkManager worker that runs periodically (every 24h) to check if any borrowed items are due for return. Sends local system notifications if deadlines are approaching (7, 3, 1, or 0 days left).

#### **Theming**
*   **`theme/ThemePreferences.kt`**: Helper object for saving and retrieving the user's preferred theme (Dark/Light) using SharedPreferences.

## Tools and Frameworks
-   **Kotlin** & **Android Studio**
-   **Firebase** (Auth, Firestore, Analytics)
-   **Jetpack Components** (ViewBinding, Navigation, ViewModel, LiveData, WorkManager)
-   **CameraX** & **ML Kit** (Google Code Scanner)
-   **Retrofit** (Networking)

## Demo
A demo video showcasing the app's features is available [here](https://drive.google.com/file/d/1g7U8DulLNrSha0HmBw1a1ZtHoXrrJd-g/view?usp=sharing).
