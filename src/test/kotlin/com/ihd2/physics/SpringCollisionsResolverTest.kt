package com.ihd2.physics

import com.ihd2.log.NoopLogger
import com.ihd2.model.Mass
import com.ihd2.model.Model
import com.ihd2.model.Spring
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SpringCollisionsResolverTest {

    private lateinit var leftMass: Mass
    private lateinit var rightSpring: Spring
    private lateinit var rightModel: Model
    private val massesToRevert = HashSet<Mass>()
    private val collisionInfo = CollisionInfo()

    @BeforeEach
    fun setup() {
        rightModel = Model()
        rightSpring = Spring(1)
        rightSpring.mass1 = Mass(1)
        rightSpring.mass2 = Mass(2)
        rightModel.springs.add(rightSpring)
        leftMass = Mass(0)
    }

    @Test
    fun `only mass moving`() {
        rightSpring.mass1.apply {
            position.x = 1.0
            position.y = 0.0
        }
        rightSpring.mass2.apply {
            position.x = 0.0
            position.y = 1.0
        }
        leftMass.apply {
            position.x = 1.0
            position.y = 1.0
            lastPosition.x = 0.0
            lastPosition.y = 0.0
            velocity.x = 1.0
            velocity.y = 1.0
        }

        SpringCollisionsResolver.checkForSpringCollisions(
            leftMass,
            rightModel,
            massesToRevert,
            PhysicsConfig.CLASSIC,
            collisionInfo,
            true,
            NoopLogger())
        assert(massesToRevert.contains(leftMass))
        assert(massesToRevert.contains(rightSpring.mass1))
        assert(massesToRevert.contains(rightSpring.mass2))
    }

    @Test
    fun `only spring moving`() {
        rightSpring.mass1.apply {
            position.x = 1.0
            position.y = 0.0
            lastPosition.x = 1.0
            lastPosition.y = 0.0
        }
        rightSpring.mass2.apply {
            position.x = -1.0
            position.y = 1.0
            lastPosition.x = -1.0
            lastPosition.y = 0.0
        }
        leftMass.apply {
            position.x = 0.4
            position.y = 0.0
            lastPosition.x = 0.4
            lastPosition.y = 0.0
        }

        SpringCollisionsResolver.checkForSpringCollisions(
            leftMass,
            rightModel,
            massesToRevert,
            PhysicsConfig.CLASSIC,
            collisionInfo,
            true,
            NoopLogger())
        assert(massesToRevert.contains(leftMass))
        assert(massesToRevert.contains(rightSpring.mass1))
        assert(massesToRevert.contains(rightSpring.mass2))
    }

    @Test
    fun `mass on line, only spring moving`() {
        rightSpring.mass1.apply {
            position.x = 1.0
            position.y = 0.0
            lastPosition.x = 1.0
            lastPosition.y = 0.0
        }
        rightSpring.mass2.apply {
            position.x = -1.0
            position.y = 0.9
            lastPosition.x = -1.0
            lastPosition.y = 1.0
        }
        leftMass.apply {
            position.x = 0.0
            position.y = 0.5
            lastPosition.x = 0.0
            lastPosition.y = 0.5
        }

        SpringCollisionsResolver.checkForSpringCollisions(
            leftMass,
            rightModel,
            massesToRevert,
            PhysicsConfig.CLASSIC,
            collisionInfo,
            true,
            NoopLogger())
        assert(massesToRevert.contains(leftMass))
        assert(massesToRevert.contains(rightSpring.mass1))
        assert(massesToRevert.contains(rightSpring.mass2))
    }

    @Test
    fun `mass on line, only spring moving2`() {
        rightSpring.mass1.apply {
            position.x = 1.0
            position.y = 0.0
            lastPosition.x = 1.0
            lastPosition.y = 0.0
        }
        rightSpring.mass2.apply {
            position.x = -1.0
            position.y = 1.1
            lastPosition.x = -1.0
            lastPosition.y = 0.9
        }
        leftMass.apply {
            position.x = 0.0
            position.y = 0.5
            lastPosition.x = 0.0
            lastPosition.y = 0.5
        }

        SpringCollisionsResolver.checkForSpringCollisions(
            leftMass,
            rightModel,
            massesToRevert,
            PhysicsConfig.CLASSIC,
            collisionInfo,
            true,
            NoopLogger())
        assert(massesToRevert.contains(leftMass))
        assert(massesToRevert.contains(rightSpring.mass1))
        assert(massesToRevert.contains(rightSpring.mass2))
    }
}