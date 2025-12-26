package com.example.racing_assignment

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build.VERSION_CODES
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.arrayOf



class MainActivity : AppCompatActivity() {
    private lateinit var columns: Array<Array<ImageView>>
    private val handler = Handler(Looper.getMainLooper())
    private val animatingColumns = mutableSetOf<Int>()
    private var currentLane = 2
    private lateinit var carController: CarController
    private var canMove = true
    private lateinit var lives: LivesController
    private var liveCounter = 3
    //array to track the bomb at the bottom of a column, used for collision checks
    private val bombsAtBottom = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupButtons()

        handler.post(gameLoop)

        // add finish()
    }
    // Animates the columns
    private fun runColumn() {
        //Checking if the column is currently running
        val availableColumns = columns.indices.filter { it !in animatingColumns }
        if (availableColumns.isEmpty()) return
        // The column to be animated
        val randomColumn = availableColumns.random()
        animatingColumns.add(randomColumn)
        // The images in the randomColumn
        val selectedColumn = columns[randomColumn]

        val animator = ImageAnimator(
            selectedColumn[0],
            selectedColumn[1],
            selectedColumn[2],
            selectedColumn[3],
            selectedColumn[4],
            selectedColumn[5]
        ) { // Tracks what column's bomb has reached the car's row
            bombsAtBottom.add(randomColumn)
            checkCollision(randomColumn)
            // Removes the bomb from the last row quicker than the default time
            handler.postDelayed({
                bombsAtBottom.remove(randomColumn)
            }, 400)
        }
        animator.start()
        handler.postDelayed({ animatingColumns.remove(randomColumn) }, 3200)
    }

    val gameLoop = object : Runnable {
        override fun run() {
            runColumn()
            handler.postDelayed(this, 1600)
        }
    }

    private fun initializeViews() {
        carController = CarController(
            findViewById(R.id.raceCar1_1),
            findViewById(R.id.raceCar1_2),
            findViewById(R.id.raceCar1_3),
            findViewById(R.id.raceCar1_4),
            findViewById(R.id.raceCar1_5)
        )

        columns = arrayOf(
            arrayOf(
                findViewById(R.id.bomb1_1),
                findViewById(R.id.bomb2_1),
                findViewById(R.id.bomb3_1),
                findViewById(R.id.bomb4_1),
                findViewById(R.id.bomb5_1),
                findViewById(R.id.bomb6_1)
            ),
            arrayOf(
                findViewById(R.id.bomb1_2),
                findViewById(R.id.bomb2_2),
                findViewById(R.id.bomb3_2),
                findViewById(R.id.bomb4_2),
                findViewById(R.id.bomb5_2),
                findViewById(R.id.bomb6_2)
            ),
            arrayOf(
                findViewById(R.id.bomb1_3),
                findViewById(R.id.bomb2_3),
                findViewById(R.id.bomb3_3),
                findViewById(R.id.bomb4_3),
                findViewById(R.id.bomb5_3),
                findViewById(R.id.bomb6_3)
            ),
            arrayOf(
                findViewById(R.id.bomb1_4),
                findViewById(R.id.bomb2_4),
                findViewById(R.id.bomb3_4),
                findViewById(R.id.bomb4_4),
                findViewById(R.id.bomb5_4),
                findViewById(R.id.bomb6_4)
            ),
            arrayOf(
                findViewById(R.id.bomb1_5),
                findViewById(R.id.bomb2_5),
                findViewById(R.id.bomb3_5),
                findViewById(R.id.bomb4_5),
                findViewById(R.id.bomb5_5),
                findViewById(R.id.bomb6_5)
            )
        )
        lives = LivesController(
            findViewById(R.id.heart1_3),
            findViewById(R.id.heart1_2),
            findViewById(R.id.heart1_1)
        )
    }

    private fun setupButtons() {
        val buttonLeft = findViewById<Button>(R.id.leftArrow)
        val buttonRight = findViewById<Button>(R.id.rightArrow)

        buttonLeft.setOnClickListener {
            if (canMove && currentLane > 0) {
                canMove = false
                carController.moveCar(currentLane, currentLane - 1)
                currentLane--

                if (bombsAtBottom.contains(currentLane)){
                    checkCollision(currentLane)
                }
                handler.postDelayed({ canMove = true }, 100)
            }
        }

        buttonRight.setOnClickListener {
            if (canMove && currentLane < 4) {
                canMove = false
                carController.moveCar(currentLane, currentLane + 1)
                currentLane++

                if (bombsAtBottom.contains(currentLane)){
                    checkCollision(currentLane)
                }

                handler.postDelayed({ canMove = true }, 100)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
            // Android 12+ (API 31)
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
            // Android 8+ (API 26)
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // Android 7 and below
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(500)
        }
    }

    private fun checkCollision(column: Int) {
        if (column == currentLane) {
            lives.removeLives(liveCounter - 1)
            liveCounter--
            vibrate()
            if (liveCounter == 0) {
                Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show()
                lives.resetLives() // Game resets
                liveCounter = 3
            } else {
                Toast.makeText(this, "Watch Out!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}




