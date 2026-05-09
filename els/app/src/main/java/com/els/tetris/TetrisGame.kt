package com.els.tetris

import kotlin.math.max
import kotlin.random.Random

class TetrisGame(
    private val width: Int = 10,
    private val height: Int = 20,
) {
    data class Piece(var matrix: Array<IntArray>, var x: Int = 0, var y: Int = 0)

    private val arena = Array(height) { IntArray(width) }

    var score: Int = 0
        private set

    var lines: Int = 0
        private set

    var level: Int = 1
        private set

    var isGameOver: Boolean = false
        private set

    private var currentPiece: Piece = spawnPiece()

    fun board(): Array<IntArray> {
        val copy = Array(height) { arena[it].clone() }
        currentPiece.matrix.forEachIndexed { py, row ->
            row.forEachIndexed { px, value ->
                if (value != 0) {
                    val bx = currentPiece.x + px
                    val by = currentPiece.y + py
                    if (by in copy.indices && bx in copy[by].indices) {
                        copy[by][bx] = value
                    }
                }
            }
        }
        return copy
    }

    fun reset() {
        for (row in arena) {
            row.fill(0)
        }
        score = 0
        lines = 0
        level = 1
        isGameOver = false
        currentPiece = spawnPiece()
    }

    fun move(dx: Int) {
        if (isGameOver) return
        currentPiece.x += dx
        if (collides(currentPiece)) {
            currentPiece.x -= dx
        }
    }

    fun rotate() {
        if (isGameOver) return
        val rotated = rotateMatrix(currentPiece.matrix)
        val old = currentPiece.matrix
        currentPiece.matrix = rotated
        if (collides(currentPiece)) {
            currentPiece.matrix = old
        }
    }

    fun tick() {
        if (isGameOver) return
        currentPiece.y++
        if (collides(currentPiece)) {
            currentPiece.y--
            mergePiece()
            sweepLines()
            currentPiece = spawnPiece()
            if (collides(currentPiece)) {
                isGameOver = true
            }
        }
    }

    fun hardDrop() {
        if (isGameOver) return
        do {
            currentPiece.y++
        } while (!collides(currentPiece))
        currentPiece.y--
        mergePiece()
        sweepLines()
        currentPiece = spawnPiece()
        if (collides(currentPiece)) {
            isGameOver = true
        }
    }

    fun speedMs(): Long = max(120L, 900L - ((level - 1) * 90L))

    private fun spawnPiece(): Piece {
        val matrix = randomPiece()
        val x = (width / 2) - (matrix[0].size / 2)
        return Piece(matrix = matrix, x = x, y = 0)
    }

    private fun mergePiece() {
        currentPiece.matrix.forEachIndexed { py, row ->
            row.forEachIndexed { px, value ->
                if (value != 0) {
                    arena[currentPiece.y + py][currentPiece.x + px] = value
                }
            }
        }
    }

    private fun sweepLines() {
        var cleared = 0
        var y = height - 1
        while (y >= 0) {
            if (arena[y].all { it != 0 }) {
                for (pullY in y downTo 1) {
                    arena[pullY] = arena[pullY - 1].clone()
                }
                arena[0] = IntArray(width)
                cleared++
                continue
            }
            y--
        }
        if (cleared > 0) {
            lines += cleared
            level = (lines / 10) + 1
            val points = when (cleared) {
                1 -> 40
                2 -> 100
                3 -> 300
                else -> 1200
            }
            score += points * level
        }
    }

    private fun collides(piece: Piece): Boolean {
        piece.matrix.forEachIndexed { py, row ->
            row.forEachIndexed { px, value ->
                if (value == 0) return@forEachIndexed
                val x = piece.x + px
                val y = piece.y + py
                if (x !in 0 until width || y !in 0 until height) {
                    return true
                }
                if (arena[y][x] != 0) {
                    return true
                }
            }
        }
        return false
    }

    private fun rotateMatrix(matrix: Array<IntArray>): Array<IntArray> {
        val size = matrix.size
        val out = Array(size) { IntArray(size) }
        for (y in 0 until size) {
            for (x in 0 until size) {
                out[x][size - 1 - y] = matrix[y][x]
            }
        }
        return out
    }

    private fun randomPiece(): Array<IntArray> {
        val pieces = listOf(
            arrayOf(
                intArrayOf(0, 1, 0, 0),
                intArrayOf(0, 1, 0, 0),
                intArrayOf(0, 1, 0, 0),
                intArrayOf(0, 1, 0, 0)
            ),
            arrayOf(
                intArrayOf(2, 2),
                intArrayOf(2, 2)
            ),
            arrayOf(
                intArrayOf(0, 3, 0),
                intArrayOf(0, 3, 0),
                intArrayOf(0, 3, 3)
            ),
            arrayOf(
                intArrayOf(0, 4, 0),
                intArrayOf(0, 4, 0),
                intArrayOf(4, 4, 0)
            ),
            arrayOf(
                intArrayOf(0, 5, 5),
                intArrayOf(5, 5, 0),
                intArrayOf(0, 0, 0)
            ),
            arrayOf(
                intArrayOf(6, 6, 0),
                intArrayOf(0, 6, 6),
                intArrayOf(0, 0, 0)
            ),
            arrayOf(
                intArrayOf(0, 7, 0),
                intArrayOf(7, 7, 7),
                intArrayOf(0, 0, 0)
            )
        )
        return pieces[Random.nextInt(pieces.size)].map { it.clone() }.toTypedArray()
    }
}
