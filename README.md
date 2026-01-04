**Racing Assignment**
An Android racing game developed as part of an Android Studio development course.

**Description**
A racing game where the player controls a car moving between 5 lanes, avoiding falling bombs and collecting coins. The game tracks high scores and saves the GPS location where each score was achieved.

**Features**

[Control Modes]

* Button Mode: On-screen arrow buttons to move left and right
* Sensor Mode: Accelerometer-based controls where tilting the phone moves the car. Tilting forward/backward adjusts game speed.

[Gameplay]

* Car moves between 5 lanes
* Bombs fall from the top of the screen - collision removes a life
* Coins fall randomly - collecting them adds 10 points
* Score increases over time (faster when game speed is higher)
* Player has 3 lives
* Game ends when all lives are lost

[Leaderboard]

* Stores top 10 high scores using SharedPreferences
* Each score saves the GPS location where it was achieved
* Tapping a score opens Google Maps showing the location


**Implementation Details**

[Activities and Fragments]

* MenuActivity: Main menu with buttons for both game modes and leaderboard
* GameFragment: Contains all game logic, rendering, and collision detection
* RecordsFragment: Displays the leaderboard
* MapFragment: Shows Google Maps with a marker for the selected score's location

[Technologies Used]

* Kotlin
* View Binding
* Glide for image loading
* Google Play Services Location for GPS
* Google Maps SDK for Android
* SharedPreferences for data persistence
* SoundPool for sound effects
* Accelerometer sensor for tilt controls

[Permissions]

* VIBRATE: Vibration feedback on collision
* INTERNET: Required for Google Maps
* ACCESS_FINE_LOCATION: GPS location tracking
* ACCESS_COARSE_LOCATION: Approximate location

[Setup]

* Open the project in Android Studio
* Add a Google Maps API key in AndroidManifest.xml
* Build and run on an emulator or physical device
