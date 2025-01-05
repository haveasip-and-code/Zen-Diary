# SE-Mobile-app

# 📝 Journal Application  

Welcome to our **Journal Application** project! This app is designed to provide users with a seamless journaling experience, enhanced by advanced features like sentiment analysis, multimedia support, and more.

---

## ✨ Key Features

### 1. **Core Journal Features**  
Our app offers everything you’d expect from a journal application:  
- **Sign Up & Log In**: Users can sign up and log in using their credentials (fake info is accepted for testing purposes).  
- **Create, Edit, and Delete Entries**: Manage your journal entries effortlessly.  
- **Preview Entries**: Quickly view a summary of your journal entries.  
- **Edit Profile Information**: Update your profile details anytime.

---

### 2. **Cloud Database Integration**  
- The app leverages **Firebase** for secure, real-time cloud storage of journal entries and user data.  

---

### 3. **AI-Powered Sentiment Analysis**  
- Sentiment analysis is powered by **VADER Sentiment** and hosted via an API on **PythonAnywhere**.  
- **Sentiment Results**:  
  - View daily sentiment results after journaling.  
  - Analyze mood trends with a **line graph** for a selected time period.  

---

### 4. **Multimedia Journaling**  
- Make your entries more expressive! Users can:  
  - **Draw sketches** directly in the app.  
  - **Insert images** into their journal entries.  
  *(Note: Drawings and inserted images are not saved.)*

---

### 5. **Advanced Search**  
- Easily search for journal entries by their **header** or **content**.  

---

### 6. **Embellishment Store**  
- Personalize your journaling experience by purchasing decorations, themes, and stickers from the **Embellishment Store**.  
- Use **coins** to buy items (purchases only, no selling).  

---

## 🛠️ Technology Stack  

- **Android Studio**: Primary development environment.  
- **Firebase**: Cloud database and authentication.  
- **Python**: For backend sentiment analysis API (hosted on PythonAnywhere).  
- **VADER Sentiment**: Natural Language Processing (NLP) tool for analyzing sentiment.

---

## 🚀 How to Get Started  

### 1. **Clone the Repository**  
```bash
git clone https://github.com/your-repo-link/journal-app.git
```

### 2. **Open in Android Studio** 

- Open Android Studio.
- Navigate to File > Open and select the cloned repository folder.
- Let Gradle sync and build the project.

### 3. **Setup Firebase**

- Create a project in Firebase Console.
- Add your Android app to the project by entering the app’s package name.
- Download the google-services.json file and move it into the app/ directory.
- Enable Firestore Database and Authentication in Firebase.

### 4. **Run the App**

- Use an Android emulator or connect a physical device.
- Press the "Run" button in Android Studio.
- Explore the app!

---

## 📚 Documentation
We provide detailed documentation to help you understand and work with the app:

- **API Integration**: Instructions for connecting to the Sentiment Analysis API.
- **Database Design**: Information on the Firebase Firestore schema and rules.
- **Feature Tutorials**: Guides on how to use the app's primary features.
- **Troubleshooting**: Solutions to common issues.