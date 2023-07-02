/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles

import edu.tigers.sumatra.ball.trajectory.IBallTrajectory
import edu.tigers.sumatra.math.vector.IVector2
import edu.tigers.sumatra.math.vector.Vector2
import edu.tigers.sumatra.math.vector.Vector3
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInputStatic
import spock.lang.Specification
import spock.lang.Subject

class SimpleTimeAwareBallObstacleSpec extends Specification {

    IBallTrajectory ballTrajectory = Mock()

    @Subject
    IObstacle obstacle = new SimpleTimeAwareBallObstacle(ballTrajectory, 2, 5)

    def "Can collide"() {
        given:
        var CollisionInput collisionInput = new CollisionInputStatic(
                null,
                null,
                null,
                0
        )
        ballTrajectory.isInterceptableByTime(0) >> interceptable

        expect:
        obstacle.canCollide(collisionInput) == interceptable

        where:
        interceptable << [true, false]
    }

    def "Distance from #robotPos to obstacle after #timeOffset is #expectedDistance"() {
        given:
        IVector2 robotVel = Vector2.fromXY(Double.NaN, Double.NaN) // should not be used
        var CollisionInput collisionInput = new CollisionInputStatic(robotPos, robotVel, Vector2.zero(), timeOffset)
        ballTrajectory.getPosByTime(timeOffset) >> Vector3.fromXY(3, 0)
        ballTrajectory.getPosByTime(5.0) >> Vector3.fromXY(5, 0)

        when:
        def actualDistance = obstacle.distanceTo(collisionInput)

        then:
        actualDistance == expectedDistance

        where:
        robotPos         | timeOffset || expectedDistance
        Vector2.fromX(0) | 1.0d       || 1d
        Vector2.fromX(0) | 0.0d       || 1d
        Vector2.fromX(1) | 0.0d       || 0d
        Vector2.fromX(2) | 0.0d       || -1d
        Vector2.fromX(3) | 0.0d       || -2d
        Vector2.fromX(4) | 0.0d       || -1d
        Vector2.fromX(5) | 0.0d       || 0d
        Vector2.fromX(6) | 0.0d       || 1d
        Vector2.fromX(6) | 6.0d       || -1d
    }
}
