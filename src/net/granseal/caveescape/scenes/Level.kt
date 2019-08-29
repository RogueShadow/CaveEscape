package net.granseal.caveescape.scenes

import net.granseal.caveescape.*
import net.granseal.koLambda.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.KeyEvent
import java.awt.geom.Point2D
import java.io.File
import java.io.InputStream
import kotlin.math.cos
import kotlin.math.sin

object Level : Entity() {
    var shifted = false
    private var isLoadingLevel = false
    private var isDrawing = false
    var startPoint = point()

    var gameTime = 0f

    var lights = mutableListOf<Light>()

    fun floorColor() = Color(150, 77, 0)
    fun wallColor() = lerp(listOf(Color.DARK_GRAY, Color(100, 25, 0)), CaveEscape.timePassed.toFloat() * .25f % 1f, true)
    fun goldColor() = lerp(listOf(Color.ORANGE, Color.YELLOW), CaveEscape.timePassed.toFloat() * 3f % 1f, true)
    fun warpColor() = lerp(listOf(Color.BLUE, Color.CYAN, Color.GREEN), CaveEscape.timePassed.toFloat() * .25f % 1f, true)
    fun healthColor() = lerp(listOf(Color.GREEN, Color.WHITE, Color(0, 150, 50)), CaveEscape.timePassed.toFloat() * .5f % 1f, true)
    fun deathColor() = lerp(listOf(Color.RED, Color.BLACK, Color.ORANGE), CaveEscape.timePassed.toFloat() * 2f % 1f, true)
    fun bloodColor() = Color(0.25f, 0f, 0f)
    fun exitColor() = lerp(listOf(Color.MAGENTA,Color.WHITE), CaveEscape.timePassed.toFloat() * .25f % 1f,true)
    fun doorColor() = lerp(listOf(Color(150,50,0),Color(150,50,0).brighter()), CaveEscape.timePassed.toFloat()* 0.25f % 1f, true)
    val FLOOR = TileType("Floor", Level::floorColor)
    val BLOOD = TileType("Blood", Level::bloodColor)
    val WALL = TileType("Wall") { Color.DARK_GRAY }
    val GOLD = TileType("Gold", Level::goldColor)
    val WARP = TileType("Warp", Level::warpColor)
    val HEALTH = TileType("Health", Level::healthColor)
    val SPIKE = TileType("Death", Level::deathColor)
    val NOTHING = TileType("Nothing") { Color.WHITE }
    val PLAYER_START = TileType("Player Start", Level::floorColor)
    val DOOR = TileType("Door", Level::doorColor)
    val EXIT = TileType("Exit", Level::exitColor)
    val mapKey = mapOf('.' to FLOOR, 'w' to WALL, 'g' to GOLD, 'h' to HEALTH,
            's' to SPIKE, 'b' to BLOOD, ' ' to NOTHING, 'p' to PLAYER_START, 'd' to DOOR, 'e' to EXIT)

    data class TileType(val name: String, val color: () -> Color)
    data class Tile(val type: TileType) { var brightness = 1f; var hidden = false }
    data class Light(var pos: Point2D.Float, var intensity: Float = 9f, var color: Color = Color.WHITE)

    val walls = arrayOf(WALL, DOOR, SPIKE)
    val map = mutableListOf(mutableListOf<Tile>())
    var selectedTile = FLOOR

    init {
        bounds = rect(0f, 0f, 30f * pixelScale, 30f * pixelScale)
        scale = pixelScale.toDouble()

        loadLevel("level1.map")

        updaters.add {

            gameTime += it

            if (mouseButton(1) && keyHeld(KeyEvent.VK_ALT)) {
                val mp = mousePos()
                val clickx = (mp.x - pos.x) / pixelScale
                val clicky = (mp.y - pos.y) / pixelScale
                changeTile(clickx.toInt(), clicky.toInt(), selectedTile)
            }



            if (CaveEscape.plr.canMove()) {

                val newPos = point(CaveEscape.plr.pos.x, CaveEscape.plr.pos.y)
                if (!keyHeld(KeyEvent.VK_SHIFT)) {
                    if (keyHeld(MOVE_UP)) newPos.y -= 1
                    if (keyHeld(MOVE_LEFT)) newPos.x -= 1
                    if (keyHeld(MOVE_DOWN)) newPos.y += 1
                    if (keyHeld(MOVE_RIGHT)) newPos.x += 1
                }else{
                    if (keyPressed(MOVE_UP)) newPos.y -= 1
                    if (keyPressed(MOVE_LEFT)) newPos.x -= 1
                    if (keyPressed(MOVE_DOWN)) newPos.y += 1
                    if (keyPressed(MOVE_RIGHT)) newPos.x += 1
                }

                checkDoor(newPos)
                if (isSpike(newPos)) CaveEscape.plr.hurt()
                if (!onWall(newPos.x, CaveEscape.plr.pos.y)){
                    CaveEscape.plr.pos.x = newPos.x
                    CaveEscape.plr.resetMoveTimer()
                }
                if (!onWall(CaveEscape.plr.pos.x, newPos.y)){
                    CaveEscape.plr.resetMoveTimer()
                    CaveEscape.plr.pos.y = newPos.y
                }

                // Now that we've safely moved to a nonwall tile, check if we are standing on anything of interest.
                val x = CaveEscape.plr.pos.x.toInt()
                val y = CaveEscape.plr.pos.y.toInt()

                val tile = map[y][x]
                when (tile.type) {
                    HEALTH ->{
                        if (!tile.hidden) {
                            CaveEscape.plr.heal()
                            CaveEscape.pickup.play()
                            tile.hidden = true
                        }                    }
                    GOLD -> {
                        if (!tile.hidden) {
                            CaveEscape.plr.gold++
                            CaveEscape.gold.play()
                            tile.hidden = true
                        }
                }
                    EXIT -> {
                        CaveEscape.won = true
                        CaveEscape.nextScene()
                    }
                    else -> {}
                }

            }

            pos = ((CaveEscape.plr.pos - point(14f, 14f)) * -pixelScale.toFloat())

            if (CaveEscape.lightEnabled) {
                clearLights()
                calculateShadows(Light(CaveEscape.plr.pos, 17f))
            } else clearLights(true)
        }

        drawers.add {
            drawLevel(it)
        }

    }

    fun List<MutableList<Tile>>.set(point: Point2D.Float, c: Tile): Boolean {
        return if (this.get(point) == c) {
            false
        } else {
            this[point.y.toInt()][point.x.toInt()] = c
            true
        }
    }

    fun clearLights(on: Boolean = false) {
        if (isLoadingLevel)return
        lights.clear()
        map.flatten().forEach { it.brightness = if (on) 1f else 0f }
    }

    fun calculateShadows(light: Light) {
        if (isLoadingLevel)return
        val intensitySqr = light.intensity * light.intensity
        data class TileWithCoord(val tile: Tile, val x: Int, val y: Int)
        val tiles = mutableSetOf<TileWithCoord>()
        for (a in 0 until 360 step 2) {
            val angle = Math.toRadians(a.toDouble())
            step@ for (step in 0..light.intensity.toInt()) {
                val testx = light.pos.x + cos((angle)).toFloat() * step
                val testy = light.pos.y + sin((angle)).toFloat() * step
                val tile = map[testy.toInt()][testx.toInt()]
                if (tile.type !in listOf(WALL, DOOR) || tile.hidden) {
                    tiles.add(TileWithCoord(map[testy.toInt()][testx.toInt()],testx.toInt(),testy.toInt()))
                } else {
                    if (isValidMapCoordinate(testx, testy))tiles.add(TileWithCoord(map[testy.toInt()][testx.toInt()],testx.toInt(),testy.toInt()))
                    break@step
                }
            }
        }

        tiles.forEach {
            val intensity = clamp(1 - (point(it.x.toFloat(), it.y.toFloat()).distanceSq(light.pos).toFloat() / intensitySqr), 0f, 1f)
            it.tile.brightness = intensity
        }
    }

    fun blendMultiply(c1: Color, m: Float): Color {
        val v1 = c1.getRGBColorComponents(null).map{it*m}
        return Color(v1[0],v1[1],v1[2])
    }

    fun getHealth(point: Point2D.Float): Boolean {
        if (!isValidMapCoordinate(point))return false
        val tile = map[point.y.toInt()][point.x.toInt()]
        return if (tile.type == HEALTH) {
            map[point.y.toInt()][point.x.toInt()] = Tile(FLOOR)
            true
        } else false
    }


    fun isValidMapCoordinate(point: Point2D.Float) = isValidMapCoordinate(point.x, point.y)
    fun isValidMapCoordinate(x: Float, y: Float) = isValidMapCoordinate(x.toInt(), y.toInt())
    fun isValidMapCoordinate(x: Int, y: Int): Boolean {
        if (x < 0 || y < 0)return false
        if (y >= map.size)return false
        if (x >= map[y].size)return false
        return true
    }

    fun onWall(point: Point2D.Float) = onWall(point.x, point.y)
    fun onWall(x: Float, y: Float) = onWall(x.toInt(), y.toInt())
    fun onWall(x: Int, y: Int): Boolean {
        if (isLoadingLevel)return true
        if (!isValidMapCoordinate(x, y))return true
        val type = map[y][x].type
        if (map[y][x].hidden)return false
        return type in walls
    }

    fun isSpike(point: Point2D.Float) = isSpike(point.x, point.y)
    fun isSpike(x: Float, y: Float) = isSpike(x.toInt(), y.toInt())
    fun isSpike(x: Int, y: Int): Boolean {
        return map[y][x].type == SPIKE
    }

    fun checkDoor(pos: Point2D.Float) = checkDoor(pos.x, pos.y)
    fun checkDoor(x: Float, y: Float) = checkDoor(x.toInt(), y.toInt())
    fun checkDoor(x: Int, y: Int) {
        if (!isValidMapCoordinate(x, y))return
        if (map[y][x].type == DOOR && !map[y][x].hidden){
            if (CaveEscape.plr.gold >= 5) {
                map[y][x].hidden = true
                CaveEscape.pickup.play()
                CaveEscape.plr.gold -= 5
            }else{
                CaveEscape.error.play()
                CaveEscape.sceneRoot = Text("5 gold to open", Level)
            }
        }
    }

    fun drawLevel(g: Graphics2D) {
        if (isLoadingLevel)return
        isDrawing = true
        map.withIndex().forEach { y ->
            y.value.withIndex().forEach { x ->
                if (x.value.type != NOTHING) {
                    if (x.value.hidden){
                        g.color = blendMultiply(FLOOR.color(), x.value.brightness)
                        g.fillRect(x.index,y.index,1,1)
                    }else {
                        g.color = blendMultiply(x.value.type.color(), x.value.brightness)
                        g.fillRect(x.index, y.index, 1, 1)
                    }
                }
            }
        }
        isDrawing = false
    }

    fun cycleSelectedTile(dir: Boolean = true) {
        val tiles = mapKey.values.toList()
        if (dir) {
            var newTile = tiles.indexOf(selectedTile) + 1
            if (newTile > tiles.size - 1) newTile = 0
            selectedTile = tiles[newTile]
        }else{
            var newTile = tiles.indexOf(selectedTile) - 1
            if (newTile < 0) newTile = tiles.size - 1
            selectedTile = tiles[newTile]
        }
    }
    fun changeTile(x: Int, y: Int, tile: TileType){
        if (isDrawing)return
        while (map.size-1 <= y) map.add(mutableListOf())
        while (map[y].size-1 <= x) map[y].add(Tile(NOTHING))
        val cy = clamp(y, 0, map.size-1)
        val cx = clamp(x,0, map[y].size-1)
        map[cy][cx] = Tile(tile)
    }

    fun loadLevel(file: String){
        val stats = mutableMapOf<Char,Int>()
        mapKey.keys.forEach{ stats[it] = 0}
        isLoadingLevel = true
        val inStream: InputStream = if (File(file).exists())File(file).inputStream() else getStream(file)
        Thread.sleep(10)
        println("Loading level $file")
        map.clear()
        inStream.reader().readLines().withIndex().forEach { y ->
            map.add(mutableListOf())
            y.value.withIndex().forEach { x ->
                var count = stats[x.value] ?: 0
                count++
                stats[x.value] = count
                val type = mapKey[x.value] ?: WALL
                if (type == PLAYER_START){
                    startPoint = point(x.index.toFloat(),y.index.toFloat())
                    println("Start Point is $startPoint")
                }
                map[y.index].add(Tile(type))
            }
        }
        isLoadingLevel = false
        println(stats)
    }

    fun saveLevel(file: String){
        println("Saving level $file")
        val path = file
        var saveFile = File(path)
        if (saveFile.exists()){
            saveFile.delete()
            saveFile = File(path)
        }
        val writer = saveFile.writer()
        val key = mutableMapOf<TileType,Char>()
        mapKey.forEach{
            key[it.value] = it.key
        }

        map.forEach{ y ->
            y.forEach{ tile ->
                writer.write(key[tile.type].toString())
            }
            writer.write("\n")
        }
        writer.close()
    }
}
