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
