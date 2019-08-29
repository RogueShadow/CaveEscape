package net.granseal.caveescape

import java.awt.event.KeyEvent

const val pixelScale = 28

const val INSTRUCTIONS = "Press q to see gold     hold shift to move slower  find 5 gold to open doors avoid spikes"
const val EDIT_INSTRUCTIONS = "To Edit levels open in console use middle and right mouse button to select tile left click + alt to write alt + 1 to save alt + l to load alt + i to turn off lights"

val MOVE_UP = listOf(KeyEvent.VK_W,KeyEvent.VK_UP)
val MOVE_DOWN = listOf(KeyEvent.VK_S,KeyEvent.VK_DOWN)
val MOVE_LEFT = listOf(KeyEvent.VK_A,KeyEvent.VK_LEFT)
val MOVE_RIGHT = listOf(KeyEvent.VK_D,KeyEvent.VK_RIGHT)

val SAVE_LEVEL = listOf('1')
val LOAD_LEVEL = listOf('l')

val TOGGLE_LIGHTS = listOf('i')

val INSTANT_WIN = listOf('u')