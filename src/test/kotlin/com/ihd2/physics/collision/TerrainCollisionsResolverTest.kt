package com.ihd2.physics.collision

import org.dyn4j.geometry.Rotation
import org.dyn4j.geometry.Vector2
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TerrainCollisionsResolverTest {
    @Test
    fun testVectorRotate() {
        val xAxis = Vector2(1.0, 0.0)
        xAxis.rotate(Rotation.ofDegrees(90.0))
        assertEquals(xAxis.y, 1.0)
    }
}