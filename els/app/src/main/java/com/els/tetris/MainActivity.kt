package com.els.tetris

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), GameView.Listener {
    private lateinit var scoreText: TextView
    private lateinit var levelText: TextView
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreText = findViewById(R.id.scoreText)
        levelText = findViewById(R.id.levelText)
        gameView = findViewById(R.id.gameView)

        gameView.setListener(this)
    }

    override fun onScoreChanged(score: Int, level: Int) {
        scoreText.text = "Score: $score"
        levelText.text = "Level: $level"
    }

    override fun onGameOver(score: Int) {
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Final score: $score")
            .setCancelable(false)
            .setPositiveButton("Play Again") { _, _ ->
                gameView.startGame()
            }
            .show()
    }
}
