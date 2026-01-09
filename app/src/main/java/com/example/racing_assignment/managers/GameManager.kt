package com.example.racing_assignment.managers

class GameManager(
    private val onScoreChanged: (Int) -> Unit,
    private val onLivesChanged: (Int) -> Unit,
    private val onGameOver: () -> Unit,
    private val onCoinCollected: () -> Unit,
    private val onBombHit: () -> Unit
) {
    var currentLane = 2
        private set
    var score = 0
        private set
    var lives = 3
        private set
    var gameSpeed = 3000L
    var gameEnded = false
        private set

    private val animatingColumns = mutableSetOf<Int>()
    private val objectsAtBottom = mutableMapOf<Int, Boolean>() // lane -> isCoin

    fun moveLeft(): Boolean {
        if (currentLane > 0) {
            currentLane--
            checkCollisionAfterMove()
            return true
        }
        return false
    }

    fun moveRight(): Boolean {
        if (currentLane < 4) {
            currentLane++
            checkCollisionAfterMove()
            return true
        }
        return false
    }

    fun moveToLane(lane: Int): Boolean {
        if (lane in 0..4 && lane != currentLane) {
            currentLane = lane
            checkCollisionAfterMove()
            return true
        }
        return false
    }

    fun incrementScore() {
        if (!gameEnded) {
            score++
            onScoreChanged(score)
        }
    }

    fun getScoreDelay(): Long {
        return (gameSpeed / 3).coerceIn(300L, 1700L)
    }

    fun getAvailableColumn(): Int? {
        val available = (0..4).filter { it !in animatingColumns }
        if (available.isEmpty()) return null
        return available.random()
    }

    fun shouldSpawnCoin(): Boolean {
        return (0..10).random() > 7  // 30% chance
    }

    fun startAnimatingColumn(column: Int) {
        animatingColumns.add(column)
    }

    fun onObjectReachedBottom(column: Int, isCoin: Boolean) {
        objectsAtBottom[column] = isCoin
        checkCollision(column, isCoin)
    }

    fun onObjectFinished(column: Int) {
        objectsAtBottom.remove(column)
        animatingColumns.remove(column)
    }

    private fun checkCollisionAfterMove() {
        objectsAtBottom[currentLane]?.let { isCoin ->
            handleCollision(isCoin)
            objectsAtBottom.remove(currentLane)
        }
    }

    private fun checkCollision(column: Int, isCoin: Boolean) {
        if (column == currentLane) {
            handleCollision(isCoin)
            objectsAtBottom.remove(column)
        }
    }

    private fun handleCollision(isCoin: Boolean) {
        if (gameEnded) return

        if (isCoin) {
            score += 10
            onScoreChanged(score)
            onCoinCollected()
        } else {
            if (lives <= 0) return
            lives--
            onLivesChanged(lives)
            onBombHit()

            if (lives == 0) {
                onGameOver()
            }
        }
    }

    fun endGame() {
        gameEnded = true
    }
}