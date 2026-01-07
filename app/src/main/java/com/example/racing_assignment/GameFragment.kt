package com.example.racing_assignment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide

class GameFragment : Fragment(), SensorController.SensorCallback {

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

    // Controllers
    private lateinit var sensorController: SensorController
    private lateinit var soundController: SoundController
    private lateinit var vibrationController: VibrationController
    private lateinit var locationController: LocationController
    private lateinit var scoreManager: ScoreManager

    private val scoreRunnable = object : Runnable {
        override fun run() {
            if (!gameEnded) {
                score++
                scoreText.text = score.toString()
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

        initControllers()

        Glide.with(this)
            .load(R.drawable.background_road)
            .into(view.findViewById(R.id.background_road))

        useButtons = arguments?.getBoolean("useButtons", true) ?: true

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
                sensorController.register()
            }

            handler.post(gameLoop)
            handler.post(scoreRunnable)
        }

        return view
    }

    private fun initControllers() {
        val context = requireContext()
        sensorController = SensorController(context).apply { setCallback(this@GameFragment) }
        soundController = SoundController(context)
        vibrationController = VibrationController(context)
        locationController = LocationController(requireActivity()).apply { requestLocation() }
        scoreManager = ScoreManager(context)
    }

    override fun onTiltChanged(targetLane: Int, gameSpeed: Long) {
        if (gameEnded) return

        this.gameSpeed = gameSpeed

        if (targetLane != currentLane) {
            currentLane = targetLane
            moveCarToLane(currentLane)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!useButtons) {
            sensorController.register()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!useButtons) {
            sensorController.unregister()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        soundController.release()
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
        val isCoin = (0..10).random() > 7

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

                val objectY = imageView.y
                val objectLane = ((imageView.x + (if (isCoin) 40 else 50)) / laneWidth).toInt()

                if (objectY >= carY - 60 && objectY <= carY + 60 && objectLane == currentLane) {
                    if (isCoin) {
                        soundController.playCoinSound()
                        score += 10
                        scoreText.text = score.toString()
                    } else {
                        handleCollision()
                    }
                    gameContainer.removeView(imageView)
                    animation.cancel()
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
        soundController.playBombSound()
        vibrationController.vibrate()

        if (liveCounter == 0) {
            Toast.makeText(requireContext(), "Game Over", Toast.LENGTH_SHORT).show()
            handler.postDelayed({ endGame() }, 1000)
        } else {
            Toast.makeText(requireContext(), "Watch Out!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun endGame() {
        if (gameEnded) return
        gameEnded = true
        handler.removeCallbacksAndMessages(null)
        if (isAdded) {
            scoreManager.saveScore(score, locationController.latitude, locationController.longitude)
            parentFragmentManager.popBackStack()
        }
    }
}