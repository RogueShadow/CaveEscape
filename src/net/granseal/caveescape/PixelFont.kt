package net.granseal.caveescape

import net.granseal.koLambda.getReader
import java.io.File


class PixelFont {
    fun loadPixelFont(): Map<Char, List<String>> {
        var map = mutableMapOf<Char, List<String>>()
        val file = getReader("thirty.fnt").readLines()
        val chars = file.first()
        val data = file.map { it.split("//").first() }.subList(1, file.size).joinToString("\n")
        val chunks = data.split(",").map { it.trim() }
        chunks.withIndex().forEach { c ->
            map[chars[c.index]] = c.value.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        }
        loaded = true
        return map
    }

    var loaded = false

    var map = loadPixelFont()

    fun getLength(text: String): Int {
        if (!loaded) {
            println("PixelFont wasn't loaded.")
            return 0
        }
        var len = 0
        text.toLowerCase().forEach {
            len += map[it]?.first()?.length!!
        }
        return len
    }
}