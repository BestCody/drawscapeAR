package com.example.eureka

import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.math.Position
import io.github.sceneview.node.CylinderNode
import kotlin.math.sqrt

class StrokeRenderer(private val engine: Engine) {

    private val renderedStrokes = mutableMapOf<String, List<CylinderNode>>()

    fun renderStroke(stroke: Stroke): List<CylinderNode> {
        removeStroke(stroke.id)
        val nodes = mutableListOf<CylinderNode>()
        for (i in 0 until stroke.points.size - 1) {
            val start = stroke.points[i]
            val end   = stroke.points[i + 1]
            val node  = createSegmentNode(
                start  = Float3(start[0], start[1], start[2]),
                end    = Float3(end[0], end[1], end[2]),
                color  = stroke.color,
                radius = stroke.width * 0.001f
            )
            nodes.add(node)
        }
        renderedStrokes[stroke.id] = nodes
        return nodes
    }

    private fun createSegmentNode(
        start  : Float3,
        end    : Float3,
        color  : Color,
        radius : Float
    ): CylinderNode {
        val mid    = Float3((start.x + end.x) / 2f, (start.y + end.y) / 2f, (start.z + end.z) / 2f)
        val diff   = Float3(end.x - start.x, end.y - start.y, end.z - start.z)
        val length = sqrt(diff.x * diff.x + diff.y * diff.y + diff.z * diff.z)
        return CylinderNode(
            engine = engine,
            radius = radius,
            height = length
        ).apply {
            worldPosition = Position(mid.x, mid.y, mid.z)
        }
    }

    fun removeStroke(strokeId: String) {
        renderedStrokes.remove(strokeId)
    }

    fun getNodes(strokeId: String): List<CylinderNode>? = renderedStrokes[strokeId]
}