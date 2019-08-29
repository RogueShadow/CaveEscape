package net.granseal.caveescape

import net.granseal.koLambda.Entity
import net.granseal.koLambda.keyHeld
import net.granseal.koLambda.keyPressed
import java.awt.Color
import java.awt.event.KeyEvent

class Text(msg: String, val lastScene: Entity): Entity(){
    val t =  msg.split(' ').map{ it to CaveEscape.font.getLength(it)}.iterator()
    val final = mutableListOf<String>("")
    init {
        while (t.hasNext()){
            var s = t.next()
            if (CaveEscape.font.getLength(final.last()) + s.second + 1 < 30){
                final[final.size-1] += " " + s.first
            }else{
                final.add(s.first)
            }
        }
    }
    val text = final
    val xPos = text.map { 15 - (CaveEscape.font.getLength(it) / 2) }
    var timer = 0f
    var yPos = 31
    init {
        scale = pixelScale.toDouble()
        updaters.add {
            if (keyHeld(KeyEvent.VK_ESCAPE)) CaveEscape.sceneRoot = lastScene
            if (keyHeld(KeyEvent.VK_SPACE))timer += it*5
            timer += it
            if (timer > 0.10) {
                yPos--
                timer = 0f
                if (yPos < (-(text.size * 7)) || keyPressed(KeyEvent.VK_ESCAPE)) {
                    CaveEscape.sceneRoot = lastScene
                }
            }
        }
        drawers.add {
            it.color = Color.ORANGE
            text.withIndex().forEach { t ->
                it.drawText(t.value, xPos[t.index], yPos + t.index * 7, CaveEscape.font.map)
            }
        }
    }
}