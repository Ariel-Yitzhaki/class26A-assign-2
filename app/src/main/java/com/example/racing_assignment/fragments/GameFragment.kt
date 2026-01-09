package com.example.racing_assignment.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.racing_assignment.controllers.CarController
import com.example.racing_assignment.managers.GameManager
import com.example.racing_assignment.controllers.LivesController
import com.example.racing_assignment.controllers.LocationController
import com.example.racing_assignment.utils.ObjectAnimator
import com.example.racing_assignment.R
import com.example.racing_assignment.managers.ScoreManager
import com.example.racing_assignment.controllers.SensorController
import com.example.racing_assignment.controllers.SoundController
import com.example.racing_assignment.controllers.VibrationController

class GameFragment : Fragment(), SensorController.SensorCallback {

    private val handler = Handler(Looper.getMainLooper())
    private var canMove = true
    private var useButtons = true
    private lateinit var scoreText: TextView
    private lateinit var carController: CarController
    private lateinit var livesController: LivesController

    private lateinit var bombColumns: Array<Array<ImageView>>
    private lateinit var coinColumns: Array<Array<ImageView>>

    private val activeAnimators = mutableListOf<ObjectAnimator>()

    // Controllers
    private lateinit var gameManager: GameManager
    private lateinit var sensorController: SensorController
    private lateinit var soundController: SoundController
    private lateinit var vibrationController: VibrationController
    private lateinit var locationController: LocationController
    private lateinit var scoreManager: ScoreManager

    private val scoreRunnable = object : Runnable {
        override fun run() {
            if (!gameManager.gameEnded) {
                gameManager.incrementScore()
                handler.postDelayed(this, gameManager.getScoreDelay())
            }
        }
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!gameManager.gameEnded) {
                spawnObject()
                handler.postDelayed(this, 1500)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        initGameManager()
        initControllers()

        Glide.with(this)
            .load(R.drawable.background_road)
            .into(view.findViewById(R.id.background_road))

        useButtons = arguments?.getBoolean("useButtons", true) ?: true

        initializeViews(view)

        if (useButtons) {
            setupButtons(view)
        } else {
            view.findViewById<Button>(R.id.leftArrow).visibility = View.GONE
            view.findViewById<Button>(R.id.rightArrow).visibility = View.GONE
            sensorController.register()
        }

        carController.showCar(gameManager.currentLane)

        handler.post(gameLoop)
        handler.post(scoreRunnable)

        return view
    }

    private fun initGameManager() {
        gameManager = GameManager(
            onScoreChanged = { score ->
                scoreText.text = score.toString()
            },
            onLivesChanged = { lives ->
                livesController.removeLives(lives)
            },
            onGameOver = {
                Toast.makeText(requireContext(), "Game Over", Toast.LENGTH_SHORT).show()
                handler.postDelayed({ endGame() }, 1000)
            },
            onCoinCollected = {
                soundController.playCoinSound()
            },
            onBombHit = {
                soundController.playBombSound()
                vibrationController.vibrate()
                if (gameManager.lives > 0) {
                    Toast.makeText(requireContext(), "Watch Out!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun initControllers() {
        val context = requireContext()
        sensorController = SensorController(context).apply { setCallback(this@GameFragment) }
        soundController = SoundController(context)
        vibrationController = VibrationController(context)
        locationController = LocationController(requireActivity()).apply { requestLocation() }
        scoreManager = ScoreManager(context)
    }

    private fun initializeViews(view: View) {
        scoreText = view.findViewById(R.id.scoreText)

        carController = CarController(
            view.findViewById(R.id.car_1),
            view.findViewById(R.id.car_2),
            view.findViewById(R.id.car_3),
            view.findViewById(R.id.car_4),
            view.findViewById(R.id.car_5)
        )

        livesController = LivesController(
            view.findViewById(R.id.heart1_3),
            view.findViewById(R.id.heart1_2),
            view.findViewById(R.id.heart1_1)
        )

        bombColumns = arrayOf(
            arrayOf(
                view.findViewById(R.id.bomb1_1), view.findViewById(R.id.bomb2_1),
                view.findViewById(R.id.bomb3_1), view.findViewById(R.id.bomb4_1),
                view.findViewById(R.id.bomb5_1), view.findViewById(R.id.bomb6_1)
            ),
            arrayOf(
                view.findViewById(R.id.bomb1_2), view.findViewById(R.id.bomb2_2),
                view.findViewById(R.id.bomb3_2), view.findViewById(R.id.bomb4_2),
                view.findViewById(R.id.bomb5_2), view.findViewById(R.id.bomb6_2)
            ),
            arrayOf(
                view.findViewById(R.id.bomb1_3), view.findViewById(R.id.bomb2_3),
                view.findViewById(R.id.bomb3_3), view.findViewById(R.id.bomb4_3),
                view.findViewById(R.id.bomb5_3), view.findViewById(R.id.bomb6_3)
            ),
            arrayOf(
                view.findViewById(R.id.bomb1_4), view.findViewById(R.id.bomb2_4),
                view.findViewById(R.id.bomb3_4), view.findViewById(R.id.bomb4_4),
                view.findViewById(R.id.bomb5_4), view.findViewById(R.id.bomb6_4)
            ),
            arrayOf(
                view.findViewById(R.id.bomb1_5), view.findViewById(R.id.bomb2_5),
                view.findViewById(R.id.bomb3_5), view.findViewById(R.id.bomb4_5),
                view.findViewById(R.id.bomb5_5), view.findViewById(R.id.bomb6_5)
            )
        )

        coinColumns = arrayOf(
            arrayOf(
                view.findViewById(R.id.coin1_1), view.findViewById(R.id.coin2_1),
                view.findViewById(R.id.coin3_1), view.findViewById(R.id.coin4_1),
                view.findViewById(R.id.coin5_1), view.findViewById(R.id.coin6_1)
            ),
            arrayOf(
                view.findViewById(R.id.coin1_2), view.findViewById(R.id.coin2_2),
                view.findViewById(R.id.coin3_2), view.findViewById(R.id.coin4_2),
                view.findViewById(R.id.coin5_2), view.findViewById(R.id.coin6_2)
            ),
            arrayOf(
                view.findViewById(R.id.coin1_3), view.findViewById(R.id.coin2_3),
                view.findViewById(R.id.coin3_3), view.findViewById(R.id.coin4_3),
                view.findViewById(R.id.coin5_3), view.findViewById(R.id.coin6_3)
            ),
            arrayOf(
                view.findViewById(R.id.coin1_4), view.findViewById(R.id.coin2_4),
                view.findViewById(R.id.coin3_4), view.findViewById(R.id.coin4_4),
                view.findViewById(R.id.coin5_4), view.findViewById(R.id.coin6_4)
            ),
            arrayOf(
                view.findViewById(R.id.coin1_5), view.findViewById(R.id.coin2_5),
                view.findViewById(R.id.coin3_5), view.findViewById(R.id.coin4_5),
                view.findViewById(R.id.coin5_5), view.findViewById(R.id.coin6_5)
            )
        )
    }

    private fun setupButtons(view: View) {
        val buttonLeft = view.findViewById<Button>(R.id.leftArrow)
        val buttonRight = view.findViewById<Button>(R.id.rightArrow)

        buttonLeft.setOnClickListener {
            if (canMove) {
                val oldLane = gameManager.currentLane
                if (gameManager.moveLeft()) {
                    canMove = false
                    carController.moveCar(oldLane, gameManager.currentLane)
                    handler.postDelayed({ canMove = true }, 100)
                }
            }
        }

        buttonRight.setOnClickListener {
            if (canMove) {
                val oldLane = gameManager.currentLane
                if (gameManager.moveRight()) {
                    canMove = false
                    carController.moveCar(oldLane, gameManager.currentLane)
                    handler.postDelayed({ canMove = true }, 100)
                }
            }
        }
    }

    override fun onTiltChanged(targetLane: Int, gameSpeed: Long) {
        if (gameManager.gameEnded) return

        gameManager.gameSpeed = gameSpeed

        if (canMove) {
            val oldLane = gameManager.currentLane
            if (gameManager.moveToLane(targetLane)) {
                carController.moveCar(oldLane, gameManager.currentLane)
            }
        }
    }

    private fun spawnObject() {
        val column = gameManager.getAvailableColumn() ?: return
        val isCoin = gameManager.shouldSpawnCoin()

        gameManager.startAnimatingColumn(column)

        val animator = ObjectAnimator(
            bombs = bombColumns[column],
            coins = coinColumns[column],
            isCoin = isCoin,
            getSpeed = { gameManager.gameSpeed },
            onReachedBottom = {
                gameManager.onObjectReachedBottom(column, isCoin)
            },
            onFinished = {
                gameManager.onObjectFinished(column)
            }
        )

        activeAnimators.add(animator)
        animator.start()
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
        activeAnimators.forEach { it.stop() }
        soundController.release()
    }

    private fun endGame() {
        if (gameManager.gameEnded) return
        gameManager.endGame()
        handler.removeCallbacksAndMessages(null)
        activeAnimators.forEach { it.stop() }
        if (isAdded) {
            scoreManager.saveScore(gameManager.score, locationController.latitude, locationController.longitude)
            parentFragmentManager.popBackStack()
        }
    }
}