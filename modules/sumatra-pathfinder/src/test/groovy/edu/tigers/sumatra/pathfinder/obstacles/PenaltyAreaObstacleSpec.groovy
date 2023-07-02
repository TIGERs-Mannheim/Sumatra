/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles

import edu.tigers.sumatra.math.penaltyarea.PenaltyArea
import edu.tigers.sumatra.math.vector.Vector2
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInputStatic
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class PenaltyAreaObstacleSpec extends Specification {
    @Subject
    @Shared
    IObstacle obstacle = new PenaltyAreaObstacle(
            new PenaltyArea(Vector2.fromX(10), 2, 6)
    )

    def "Can collide"() {
        expect:
        obstacle.canCollide(null)
    }

    def "Distance from #robotPos to obstacle is #expectedDistance"() {
        given:
        var robotVel = Vector2.fromXY(Double.NaN, Double.NaN) // should not be used
        var timeOffset = Double.NaN // should not be used
        var CollisionInput collisionInput = new CollisionInputStatic(robotPos, robotVel, Vector2.zero(), timeOffset)

        when:
        def actualDistance = obstacle.distanceTo(collisionInput)

        then:
        actualDistance == expectedDistance

        where:
        robotPos               || expectedDistance
        Vector2.zero()         || 8d
        Vector2.fromX(10)      || 0d
        Vector2.fromX(11)      || 0d
        Vector2.fromX(8)       || 0d
        Vector2.fromX(6)       || 2d
        Vector2.fromXY(10, 3)  || 0d
        Vector2.fromXY(10, 4)  || 1d
        Vector2.fromXY(10, -4) || 1d
    }
}
