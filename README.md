# Profile Management App 

## Overview

The application allows users to create and manage personal profiles, log in securely, and maintain a simple diary of text or photo entries. It demonstrates key concepts in mobile application development, user data management, and basic UI/UX design.

---

## Features

- **User Registration & Login**
  - Unique username and strong password enforcement (min 8 chars, includes number, capital letter, special character)
  - Credentials and user profile data stored locally (with encryption)

- **Profile Management**
  - Full editable profile: name, date of birth, address, phone number, profile photo
  - Update credentials with re-authentication
  - Profile deletion with confirmation and credential re-entry

- **Diary Functionality**
  - Add, edit, and delete diary entries (text or image)
  - View diary entries directly from profile screen
  - Each entry includes a timestamp for last modification

- **User Flow**
  - Auto logout on app close
  - Secure logout and profile deletion flows
  - Local data storage using internal files

- **UI Testing**
  - Espresso-based automated testing implemented
  - Covers login, registration, profile editing, entry management, logout, and deletion
  - Hidden test cases passed to validate stability and completeness

---

## Technologies Used

- **Language:** Java  
- **Platform:** Android (SDK 31)  
- **IDE:** Android Studio  
- **Testing:** Espresso  
- **Storage:** Local file system (CSV/flat files with simulated encryption)
