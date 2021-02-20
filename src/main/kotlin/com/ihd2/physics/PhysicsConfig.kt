package com.ihd2.physics

enum class PhysicsConfig(
    val groundHeight: Double,
    val surfaceFriction: Double,
    val surfaceReflection: Double,
    val modelReflection: Double,
    val speedLimit: Double,
    val energyLeft: Double
) {
    CLASSIC(
        groundHeight = 0.0,
        surfaceFriction = 0.1,
        surfaceReflection = -0.75,
        modelReflection = -0.75,
        speedLimit = 10.0,
        energyLeft = 0.9
    )
}