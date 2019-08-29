package net.granseal.caveescape

import java.awt.Graphics2D

fun Graphics2D.drawText(text: String, x: Int, y: Int, font: Map<Char, List<String>>) {
    var offset = 0
    text.toLowerCase().withIndex().forEach {
        font[it.value]?.withIndex()?.forEach { fy ->
            fy.value.withIndex().forEach { fx ->
                if (fx.value != '.') fillRect(x + fx.index + offset, y + fy.index, 1, 1)
            }
        }
        offset += font[it.value]?.first()?.length ?: 0
    }
}