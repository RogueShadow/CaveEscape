package net.granseal.caveescape

import net.granseal.caveescape.scenes.Level
import net.granseal.koLambda.*
import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

fun main(args: Array<String>) {
    CaveEscape.start()
}

object CaveEscape : ApplicationAdapter("30x30", 30 * pixelScale, 30 * pixelScale, false) {
    val font = PixelFont()
    val plr = Player()
    var hue = 0f
    var won = false
    var lightEnabled = true
    val gold = Sound( getStream("gold.wav").buffered())
    val blip = Sound(getStream("blip.wav").buffered())
    val hurt = Sound(getStream("hurt.wav").buffered())
    val death = Sound(getStream("death.wav").buffered())
    val error =  Sound(getStream("error.wav").buffered())
    val pickup = Sound(getStream("pickup.wav").buffered())

    override fun init() {
        fixedFPS = 120
        sceneRoot = Menu
        Level.add(plr)
    }

    override fun update(delta: Float) {
        hue += delta * 0.15f
        backgroundColor = if (sceneRoot != Level) hsb(hue, 1f, .2f) else Color.BLACK

        super.update(delta)
    }

    fun nextScene() {
        when (sceneRoot) {
            Menu -> {
                Level.map.withIndex().forEach {
                    it.value.withIndex().forEach {t ->
                        if (t.value.type == Level.PLAYER_START){
                            plr.pos = point(t.index.toFloat(),it.index.toFloat())
                            plr.gold = 0
                            plr.health = 5
                        }
                        t.value.hidden = false
                    }
                }
                sceneRoot = Text("Hello child I have trapped you in this cave I dare you to escape", Level)
            }
            Level -> sceneRoot = if (won) Text("You have beaten me this time child for now", Menu) else Text("You have lost ha ha ha ha", Menu)
        }
    }

    override fun mousePressed(e: MouseEvent) {
        if (sceneRoot != Level)return
        super.mousePressed(e)
        if (e.button == MouseEvent.BUTTON3){
            Level.cycleSelectedTile()
            println(Level.selectedTile)
        }
        if (e.button == MouseEvent.BUTTON2){
            Level.cycleSelectedTile(false)
            println(Level.selectedTile)
        }
    }

    override fun keyTyped(e: KeyEvent) {
        if (sceneRoot != Level) return
        super.keyTyped(e)
        if (e.isAltDown) {
            if (e.keyChar in SAVE_LEVEL) Level.saveLevel("level1.map")
            if (e.keyChar in LOAD_LEVEL) {
                Level.loadLevel("level1.map")
                plr.pos = Level.startPoint
            }
            if (e.keyChar in TOGGLE_LIGHTS) lightEnabled = !lightEnabled
            if (e.keyChar in INSTANT_WIN) {
                won = true
                nextScene()
            }
        }
    }

}