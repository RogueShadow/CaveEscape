package net.granseal.caveescape

import net.granseal.koLambda.Entity
import net.granseal.koLambda.keyPressed
import java.awt.Color
import java.awt.event.KeyEvent
import kotlin.system.exitProcess


object Menu: Entity() {
    var selected = 3
    val title = listOf("Cave","Escape","","Start","Exit","info","Editing")

    init {
        scale = pixelScale.toDouble()
        updaters.add {
            if (keyPressed('w') || keyPressed(KeyEvent.VK_UP)){
                selected = if (selected <= 3) title.size -1 else selected -1
                CaveEscape.blip.play()
            }
            if (keyPressed('s') || keyPressed(KeyEvent.VK_DOWN)){
                selected = if (selected >= title.size-1) 3 else selected +1
                CaveEscape.blip.play()
            }
            if (keyPressed(KeyEvent.VK_ENTER) || keyPressed(KeyEvent.VK_SPACE)){
                when (selected){
                    3 -> {
                        CaveEscape.nextScene();CaveEscape.pickup.play()}
                    4 -> {
                        CaveEscape.death.play();exitProcess(0)}
                    5 -> {
                        CaveEscape.pickup.play();CaveEscape.sceneRoot = Text(INSTRUCTIONS, Menu)
                    }
                    6 -> {
                        CaveEscape.pickup.play();CaveEscape.sceneRoot = Text(EDIT_INSTRUCTIONS, Menu)
                    }
                    else -> println("Unsupported menu option.")
                }
            }
        }

        drawers.add {
            var yOffset = 0
            title.withIndex().forEach { t ->
                it.color = if (selected == t.index) Color.WHITE else Color.ORANGE
                val offset = (30 - CaveEscape.font.getLength(t.value))/2
                yOffset = if (selected in listOf(5,6)) -16 else 0
                it.drawText(t.value,offset, (t.index * 6) + yOffset, CaveEscape.font.map)
            }
        }
    }

}