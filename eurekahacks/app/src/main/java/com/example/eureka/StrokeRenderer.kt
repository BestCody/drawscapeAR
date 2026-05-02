package com.example.eureka

import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.cross
import dev.romainguy.kotlin.math.normalize
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.CylinderNode
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt

class StrokeRenderer(private val engine: Engine) {

    private val renderedStrokes = mutableMapOf<String, List<CylinderNode>>()

    fun renderStroke(stroke: Stroke): List<CylinderNode> {
        removeStroke(stroke.id)
        val nodes = stroke.points
            .zipWithNext { start, end ->
                createSegmentNode(
                    start  = Float3(start[0], start[1], start[2]),
                    end    = Float3(end[0],   end[1],   end[2]),
                    color  = stroke.color,
                    radius = stroke.width * 0.001f
                )
            }
        renderedStrokes[stroke.id] = nodes
        return nodes
    }

    /**
     * Creates a cylinder aligned along the vector from [start] to [end].
     *
     * Previously the cylinder was never rotated, so every segment pointed straight up (Y-axis)
     * regardless of the stroke direction. Now a quaternion derived from the cross-product between
     * the Y-axis and the stroke direction is applied to orient it correctly.
     */
    private fun createSegmentNode(
        start : Float3,
        end   : Float3,
        color : Color,
        radius: Float
    ): CylinderNode {
        val diff   = end - start
        val length = sqrt(diff.x * diff.x + diff.y * diff.y + diff.z * diff.z)
        val mid    = (start + end) * 0.5f

        val dir = normalize(diff)
        val up  = Float3(0f, 1f, 0f)

        // Angle between the cylinder's default up-axis and the desired direction
        val dot   = (up.x * dir.x + up.y * dir.y + up.z * dir.z).coerceIn(-1f, 1f)
        val angle = acos(dot) * (180f / PI.toFloat())  // degrees for SceneView Rotation

        val axis = cross(up, dir)
        val axisLen = sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z)

        return CylinderNode(
            engine = engine,
            radius = radius,
            height = length
        ).apply {
            worldPosition = Position(mid.x, mid.y, mid.z)

            // Only apply rotation when axis is non-zero (i.e. dir ≠ ±up)
            if (axisLen > 0.0001f) {
                val normAxis = axis / axisLen
                worldRotation = Rotation(
                    x = normAxis.x * angle,
                    y = normAxis.y * angle,
                    z = normAxis.z * angle
                )
            } else if (dot < 0f) {
                // dir is exactly opposite to up — rotate 180° around X
                worldRotation = Rotation(x = 180f, y = 0f, z = 0f)
            }
            // else dir == up → no rotation needed
        }
    }

    fun removeStroke(strokeId: String) {
        renderedStrokes.remove(strokeId)?.forEach { it.destroy() }
    }

    fun getNodes(strokeId: String): List<CylinderNode>? = renderedStrokes[strokeId]
}