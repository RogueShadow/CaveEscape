package net.granseal.caveescape

import net.granseal.koLambda.Entity
import net.granseal.koLambda.keyHeld
import net.granseal.koLambda.lerp
import net.granseal.koLambda.point
import java.awt.Color

class Player : Entity() {
    var moveDelay = 0.05f
    var health = 10
    var gold = 0
    private var moveTimer = 0.0f
    val hurtColor = { lerp(listOf(Color.RED, Color.BLUE), CaveEscape.timePassed.toFloat() * 4f % 1, true) }
    val moveHere = { lerp(listOf(point(4f, 4f), point(27f, 4f), point(14f, 24f), point(28f, 28f), point(15f, 15f)), CaveEscape.timePassed.toFloat() * 0.25f % 1, true) }
    val myColors = { lerp(listOf(Color.BLUE, Color.CYAN), CaveEscape.timePassed.toFloat() % 1, true) }

    init {
        pos = point(14f, 14f)
        updaters.add {
            pos.x = pos.x.toInt().toFloat()
            pos.y = pos.y.toInt().toFloat()
            moveTimer += it
        }
        drawers.add {
            it.color = myColors()
            it.fillRect(0, 0, 1, 1)

            if (keyHeld('q')) {
                it.color = Color.orange
                it.drawText("gold $gold", -13, 9, CaveEscape.font.map)
            }

            it.color = Color.GREEN
            repeat(health) { v ->
                val v2 = (v / 10f).toInt()
                val v1 = v % 10
                it.fillRect(-13 + v1, -13 + v2, 1, 1)
            }
        }
    }

    fun canMove(): Boolean {
        return moveTimer >= moveDelay
    }
    fun resetMoveTimer() {
        moveTimer = 0f
    }

    fun hurt(value: Int = 1) {
        health -= value
        CaveEscape.hurt.play()
        if (isDead()) {
            CaveEscape.death.play()
            CaveEscape.nextScene()
        }
    }

    fun heal(value: Int = 1) {
        health += value
        CaveEscape.pickup.play()
    }

    fun isDead() = health <= 0
}