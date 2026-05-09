package com.els.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface Listener {
        fun onScoreChanged(score: Int, level: Int)
        fun onGameOver(score: Int)
    }

    private val game = TetrisGame()
    private var listener: Listener? = null

    private val bgPaint = Paint().apply { color = Color.parseColor("#0B0F14") }
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#1A2530")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val colors = mapOf(
        1 to Color.parseColor("#00BCD4"),
        2 to Color.parseColor("#FFEB3B"),
        3 to Color.parseColor("#4CAF50"),
        4 to Color.parseColor("#3F51B5"),
        5 to Color.parseColor("#FF5722"),
        6 to Color.parseColor("#E91E63"),
        7 to Color.parseColor("#9C27B0")
    )

    private var touchStartY = 0f

    private val loop = object : Runnable {
        override fun run() {
            game.tick()
            listener?.onScoreChanged(game.score, game.level)
            if (game.isGameOver) {
                listener?.onGameOver(game.score)
            } else {
                postDelayed(this, game.speedMs())
            }
            invalidate()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
        this.listener?.onScoreChanged(game.score, game.level)
    }

    fun startGame() {
        removeCallbacks(loop)
        game.reset()
        listener?.onScoreChanged(game.score, game.level)
        invalidate()
        postDelayed(loop, game.speedMs())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startGame()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(loop)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val board = game.board()
        val rows = board.size
        val cols = board[0].size

        val cell = minOf(width / cols.toFloat(), height / rows.toFloat())
        val offsetX = (width - (cols * cell)) / 2f
        val offsetY = (height - (rows * cell)) / 2f

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val left = offsetX + (x * cell)
                val top = offsetY + (y * cell)
                val right = left + cell
                val bottom = top + cell

                val value = board[y][x]
                if (value != 0) {
                    val paint = Paint().apply {
                        color = colors[value] ?: Color.WHITE
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(left + 2f, top + 2f, right - 2f, bottom - 2f, paint)
                }
                canvas.drawRect(left, top, right, bottom, gridPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val deltaY = event.y - touchStartY
                if (deltaY > 100f) {
                    game.hardDrop()
                } else {
                    when {
                        event.x < width * 0.33f -> game.move(-1)
                        event.x > width * 0.67f -> game.move(1)
                        else -> game.rotate()
                    }
                }
                listener?.onScoreChanged(game.score, game.level)
                if (game.isGameOver) {
                    listener?.onGameOver(game.score)
                }
                invalidate()
            }
        }
        return true
    }
}
