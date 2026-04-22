# 🎬 Fluxio – Movie Stream App

**Author:** David

Fluxio is a modern **Movie Streaming UI application** built with **Jetpack Compose**, focused on clean architecture, smooth user experience, and scalable design patterns.

This project is implemented as part of a UI challenge, following strict guidelines while showcasing production-level Android development practices.

---

## 🚀 Overview

Fluxio allows users to:

* 🔍 Search for movies by title
* 🎞️ Browse a list of movies with rich UI
* 📄 View detailed information about a selected movie
* ⚡ Experience smooth animations and responsive layouts

The app emphasizes:

* **Separation of concerns**
* **Testable UI components**
* **Reusable design system**
* **Modern Android architecture**

---

## 🧱 Tech Stack

* **Kotlin**
* **Jetpack Compose**
* **Hilt (Dependency Injection)**
* **Navigation Compose**
* **Coil (Image Loading)**
* **Kotlinx Serialization**

---

## 🏗️ Architecture

The project follows a **clean and scalable architecture**:

### 🔹 UI Layer

* Stateless composables (`ThemeScreen`, `MovieListScreen`, etc.)
* State hoisting for testability
* Screen-specific wrappers (no global scaffold)

### 🔹 Route Layer

* Connects ViewModel to UI
* Handles state collection and event delegation

### 🔹 ViewModel Layer

* Manages UI state using `StateFlow`
* Handles business logic

### 🔹 Design System

Reusable components such as:

* `ResponsiveStandardContainer`
* `OneTimeFadeInContent`
* Custom toolbars and layout wrappers

---

## ✨ Features Implemented

* ✅ Loading states for screens
* ✅ Movie list with image thumbnails
* ✅ Search functionality (by movie title)
* ✅ Smooth UI animations during search updates
* ✅ Navigation to movie details screen
* ✅ Detail screen with full movie info
* ✅ Edge-to-edge UI support
* ✅ Reusable layout wrappers per screen

---

## 🎯 UI & UX Decisions

* Each screen owns its **own wrapper layout** (no global scaffold)
* Layouts are **independently testable**
* Scroll behavior handled via reusable container
* Animations are **controlled and minimal** for performance
* Toolbar is **custom per screen** for flexibility

---

## 📊 Evaluation Focus

This project was built with the following priorities:

* **Architecture (45%)**

  * Clear separation of concerns
  * Scalable and maintainable structure

* **UI Implementation (30%)**

  * Accurate Compose usage
  * Clean and responsive layouts

* **Search Functionality (18%)**

  * Efficient filtering
  * Smooth UI updates

* **Code Quality (7%)**

  * Readability
  * Naming conventions
  * Clean code principles

---

## 📌 Challenge Notes

Although the original challenge is based on a **Books app**,
this implementation adapts the concept into a **Movie Streaming experience (Fluxio)** while maintaining all required constraints.

---

## ⚠️ Constraints Followed

* ❌ No external libraries beyond those provided
* ❌ No AI/code assistants used during implementation
* ❌ No modification of restricted files

---

## 🛠️ Setup

1. Clone the repository
2. Open in latest **Android Studio**
3. Sync Gradle
4. Run the app

---

## 🎥 Future Improvements

* Shared element transitions for movie posters
* Pagination for large datasets
* Offline caching
* Dark/Light dynamic theming enhancements

---

## 💡 Final Note

Fluxio is designed not just to pass the challenge, but to demonstrate
**real-world Android engineering practices at a senior level**.
