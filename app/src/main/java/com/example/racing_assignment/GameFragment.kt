package com.example.racing_assignment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.SoundPool
import com.bumptech.glide.Glide
import androidx.core.content.edit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class GameFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private var currentLane = 2
    private var canMove = true
    private lateinit var lives: LivesController
    private var liveCounter = 3
    private var useButtons = true
    private var gameEnded = false
    private var score = 0
    private lateinit var gameContainer: RelativeLayout
    private lateinit var raceCar: ImageView
    private lateinit var scoreText: TextView

    private var screenWidth = 0
    private var laneWidth = 0
    private val laneCount = 5
    private var gameSpeed = 3000L
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0
    private lateinit var soundPool: SoundPool
    private var coinSound: Int = 0
    private var bombSound: Int = 0
    // Sensor properties
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (gameEnded) return
            val x = event.values[0]
            val y = event.values[1]  // forward/backward tilt

            // Map tilt to lane: -10 to 10 -> 0 to 4
            val targetLane = when {
                x < -6 -> 4
                x < -3 -> 3
                x > 6 -> 0
                x > 3 -> 1
                else -> 2  // center
            }

            if (targetLane != currentLane) {
                currentLane = targetLane
                moveCarToLane(currentLane)
            }

            // Map forward/backward tilt to speed
            // y positive = tilt forward (faster), y negative = tilt backward (slower)
            gameSpeed = when {
                y < -6 -> 1500L  // very fast
                y < -3 -> 2000L  // fast
                y > 6 -> 5000L   // very slow
                y > 3 -> 4000L   // slow
                else -> 3000L    // normal
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val scoreRunnable = object : Runnable {
        override fun run() {
            if (!gameEnded) {
                score++
                scoreText.text = score.toString()
                // Faster game = faster score (scale 1000ms based on gameSpeed)
                val scoreDelay = (gameSpeed / 3).coerceIn(300L, 1700L)
                handler.postDelayed(this, scoreDelay)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requestLocation()

        Glide.with(this)
            .load(R.drawable.background_road)
            .into(view.findViewById(R.id.background_road))

        useButtons = arguments?.getBoolean("useButtons", true) ?: true

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()
        coinSound = soundPool.load(requireContext(), R.raw.coin_collect, 1)
        bombSound = soundPool.load(requireContext(), R.raw.bomb_hit, 1)

        initializeViews(view)

        view.post {
            screenWidth = view.width
            laneWidth = screenWidth / laneCount
            moveCarToLane(currentLane)

            if (useButtons) {
                setupButtons(view)
            } else {
                view.findViewById<Button>(R.id.leftArrow).visibility = View.GONE
                view.findViewById<Button>(R.id.rightArrow).visibility = View.GONE
                setupSensors()
            }

            handler.post(gameLoop)
            handler.post(scoreRunnable)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (!useButtons) {
            accelerometer?.let {
                sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!useButtons) {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
    }

    private fun initializeViews(view: View) {
        gameContainer = view.findViewById(R.id.gameContainer)
        raceCar = view.findViewById(R.id.raceCar)
        scoreText = view.findViewById(R.id.scoreText)

        lives = LivesController(
            view.findViewById(R.id.heart1_3),
            view.findViewById(R.id.heart1_2),
            view.findViewById(R.id.heart1_1)
        )
    }

    private fun moveCarToLane(lane: Int) {
        val targetX = (lane * laneWidth) + (laneWidth / 2) - (raceCar.width / 2)
        raceCar.animate()
            .x(targetX.toFloat())
            .setDuration(100)
            .start()
    }

    private fun setupButtons(view: View) {
        val buttonLeft = view.findViewById<Button>(R.id.leftArrow)
        val buttonRight = view.findViewById<Button>(R.id.rightArrow)

        buttonLeft.setOnClickListener {
            if (canMove && currentLane > 0) {
                canMove = false
                currentLane--
                moveCarToLane(currentLane)
                handler.postDelayed({ canMove = true }, 100)
            }
        }

        buttonRight.setOnClickListener {
            if (canMove && currentLane < 4) {
                canMove = false
                currentLane++
                moveCarToLane(currentLane)
                handler.postDelayed({ canMove = true }, 100)
            }
        }
    }

    private fun setupSensors() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!gameEnded) {
                spawnObject()
                handler.postDelayed(this, 1500)
            }
        }
    }

    private fun spawnObject() {
        val lane = (0 until laneCount).random()
        val isCoin = (0..10).random() > 7  // 30% chance for coin

        val imageView = ImageView(requireContext()).apply {
            setImageResource(if (isCoin) R.drawable.ic_coin else R.drawable.ic_bomb)
            layoutParams = RelativeLayout.LayoutParams(
                if (isCoin) 80 else 100,
                if (isCoin) 80 else 100
            )
        }

        gameContainer.addView(imageView)

        val startX = (lane * laneWidth) + (laneWidth / 2) - (if (isCoin) 40 else 50)
        imageView.x = startX.toFloat()
        imageView.y = 0f

        val endY = gameContainer.height.toFloat()
        val carY = raceCar.y

        imageView.animate()
            .y(endY)
            .setDuration(gameSpeed)
            .setUpdateListener { animation ->
                if (gameEnded) {
                    animation.cancel()
                    return@setUpdateListener
                }

                // Check collision when object reaches car level
                val objectY = imageView.y
                val objectLane = ((imageView.x + (if (isCoin) 40 else 50)) / laneWidth).toInt()

                if (objectY >= carY - 60 && objectY <= carY + 60 && objectLane == currentLane) {
                    if (isCoin) {
                        soundPool.play(coinSound, 1f, 1f, 1, 0, 1f)
                        score += 10
                        scoreText.text = score.toString()
                        gameContainer.removeView(imageView)
                        animation.cancel()
                    } else {
                        handleCollision()
                        gameContainer.removeView(imageView)
                        animation.cancel()
                    }
                }
            }
            .withEndAction {
                gameContainer.removeView(imageView)
            }
            .start()
    }

    private fun handleCollision() {
        if (gameEnded || !isAdded || liveCounter <= 0) return

        liveCounter--
        lives.removeLives(liveCounter)
        soundPool.play(bombSound, 1f, 1f, 1, 0, 1f)
        vibrate()

        if (liveCounter == 0) {
            Toast.makeText(requireContext(), "Game Over", Toast.LENGTH_SHORT).show()
            handler.postDelayed({ endGame() }, 1000)
        } else {
            Toast.makeText(requireContext(), "Watch Out!", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        if (!isAdded) return
        val context = requireContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(500)
        }
    }

    private fun endGame() {
        if (gameEnded) return
        gameEnded = true
        handler.removeCallbacksAndMessages(null)
        if (isAdded) {
            saveScoreIfHighScore()
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveScoreIfHighScore() {
        if (!isAdded) return
        val prefs = requireContext().getSharedPreferences("records", Context.MODE_PRIVATE)

        for (i in 1..10) {
            val existingScore = prefs.getInt("record$i", 0)
            if (score > existingScore) {
                for (j in 10 downTo i + 1) {
                    val scoreToMove = prefs.getInt("record${j - 1}", 0)
                    prefs.edit { putInt("record$j", scoreToMove) }
                }
                prefs.edit {
                    putInt("record$i", score)
                        .putFloat("lat$i", lastLatitude.toFloat())
                        .putFloat("lon$i", lastLongitude.toFloat())
                }
                break
            }
        }
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lastLatitude = location.latitude
                    lastLongitude = location.longitude
                }
            }
        }
    }
}

