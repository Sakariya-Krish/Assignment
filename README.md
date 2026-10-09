# 🍎 FoodTrack – Food Expiry & Waste Tracker

**FoodTrack** is an Android application designed to help users manage their food inventory, track expiry dates, reduce food waste, plan meals, and organize shopping lists. It provides a smart and user-friendly way to manage household food efficiently.

## 📱 About the Project

Food waste often occurs because people forget expiry dates, purchase duplicate items, or fail to plan meals around the ingredients they already have.

FoodTrack addresses these problems by bringing food inventory management, expiry tracking, recipe suggestions, shopping lists, and waste statistics into one application.

The application follows an **offline-first approach**, allowing users to manage their food data locally without requiring a constant internet connection.

## ✨ Features

### 🏠 Smart Home Dashboard

* Personalized greeting and dashboard.
* Food inventory summary.
* Expiring-soon and expired food alerts.
* Smart **Use First** recommendations.
* Quick access to important features.
* Food-saving insights and progress summaries.

### 🥗 Food Inventory Management

* Add, update, view, and manage food items.
* Organize food by category.
* Search and filter inventory.
* Track quantities and measurement units.
* Monitor expiry dates and food status.
* Consume food or record wasted items.
* Switch between grid and list views.

### ⏰ Expiry Date Tracking

* Identify expired food and items expiring soon.
* View expiry dates in a calendar.
* Browse upcoming expiry dates.
* Access food details directly from relevant lists.
* Receive expiry-related reminders when configured.

### 🍳 Smart Recipe Suggestions

* Discover recipes using available ingredients.
* Get recommendations based on food expiry priority.
* View recipe details and required ingredients.
* Identify missing ingredients.
* Add missing ingredients to the shopping list.
* Update inventory after cooking when supported.

### 🛒 Smart Shopping List

* Add and manage shopping items.
* Organize items by category.
* Search, filter, and sort the list.
* Mark items as purchased.
* Track pending and purchased items.
* Add purchased food to the inventory.
* View shopping progress and estimated costs when available.

### 📊 Statistics & Reports

* Monitor food consumption and waste.
* Review food-saving progress.
* Track inventory and shopping activity.
* View available trends and summaries.
* Support better decisions for reducing household food waste.

### 🎯 Goals & Achievements

* Set personal food-saving goals.
* Track progress toward targets.
* View achievements based on recorded activity.
* Encourage responsible food management.

### ⚙️ More Features

* Profile and preferences.
* Light and dark themes.
* Notification settings.
* Food activity timeline.
* Backup and restore options.
* Data management.
* Help, FAQs, privacy information, and app details.

## 🛠️ Tech Stack

| Technology               | Purpose                                    |
| ------------------------ | ------------------------------------------ |
| Kotlin                   | Primary programming language               |
| Jetpack Compose          | Modern Android UI                          |
| Material 3               | UI components and design system            |
| MVVM Architecture        | Separation of UI and application logic     |
| Room Database            | Local database storage                     |
| Kotlin Coroutines & Flow | Asynchronous operations and reactive data  |
| Navigation Compose       | Navigation between screens                 |
| DataStore                | Local preference storage                   |
| WorkManager              | Scheduled background work where configured |
| Gradle                   | Build and dependency management            |

> **Note:** This list describes the project's intended technology stack. Update it if your current implementation differs.

## 🏗️ Architecture

FoodTrack uses the Model–View–ViewModel (MVVM) architecture to keep the application organized and maintainable.

```text
FoodTrack
│
├── UI Layer
│   ├── Screens
│   ├── Components
│   └── Navigation
│
├── ViewModel Layer
│   ├── UI State
│   └── Business Logic
│
├── Repository Layer
│   └── Data Management
│
└── Data Layer
    ├── Room Database
    ├── DAO Interfaces
    ├── Entities
    └── DataStore Preferences
```

The exact folder structure may vary depending on the implementation.

## 📋 Requirements

* Android Studio
* Android SDK
* Kotlin support
* Gradle
* Android device or emulator
* Minimum Android version: API 24 (Android 7.0), if configured as planned

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
```

Replace `YOUR_USERNAME` and `YOUR_REPOSITORY` with your GitHub details.

### 2. Open the Project

1. Open Android Studio.
2. Select **Open**.
3. Choose the cloned FoodTrack project folder.
4. Wait for Gradle synchronization to finish.

### 3. Build the Application

From Android Studio, select:

**Build → Make Project**

Alternatively, run this command from the project root.

Windows:

```bash
gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

### 4. Run the Application

1. Connect an Android device or start an emulator.
2. Enable USB debugging if using a physical device.
3. Select the device in Android Studio.
4. Click **Run ▶**.

## 📂 Project Structure

A possible project structure is shown below. Actual package and folder names may differ.

```text
FoodTrack/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── .../
│   │       │       ├── data/
│   │       │       ├── database/
│   │       │       ├── repository/
│   │       │       ├── viewmodel/
│   │       │       ├── ui/
│   │       │       └── navigation/
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🔐 Data & Privacy

* Food records are designed to be stored locally using Room.
* Application preferences can be stored using DataStore.
* The offline-first design helps users access core functionality without continuous internet access.
* Backup and restore behavior depends on the implemented backup functionality.
* Review permissions and data-handling behavior in the source code before distributing the application.

## 🎯 Project Objectives

* Reduce household food waste.
* Help users track food expiry dates.
* Improve food inventory organization.
* Encourage meal planning using available ingredients.
* Simplify shopping list management.
* Promote cost-conscious and sustainable food habits.

## 🔮 Future Enhancements

* Barcode scanning for faster food entry.
* Optional cloud backup and synchronization.
* More personalized meal recommendations.
* Advanced monthly and yearly reports.
* Additional accessibility and localization options.
* Smart integrations with grocery and nutrition services.

## 👨‍💻 Developer

**Krish Sakariya**

B.Tech – Information Technology

## 🤝 Contributing

Contributions, suggestions, and bug reports are welcome.

1. Fork the repository.
2. Create a new branch.
3. Make your changes.
4. Test your implementation.
5. Submit a pull request.

## 📄 License

This project is currently available for educational and development purposes. Add a suitable open-source license, such as the MIT License, if you intend to permit reuse under those terms.

---

**FoodTrack — Track Freshness. Save Food. Reduce Waste. 🌱**
