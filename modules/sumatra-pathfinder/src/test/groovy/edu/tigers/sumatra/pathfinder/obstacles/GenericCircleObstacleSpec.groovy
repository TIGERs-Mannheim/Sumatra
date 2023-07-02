/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles

import edu.tigers.sumatra.math.circle.Circle
import edu.tigers.sumatra.math.vector.Vector2
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInputStatic
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class GenericCircleObstacleSpec extends Specification {
    @Subject
    @Shared
    IObstacle obstacle = new GenericCircleObstacle("test", Circle.createCircle(Vector2.zero(), 2))

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
        robotPos          || expectedDistance
        Vector2.zero()    || -2d
        Vector2.fromX(2)  || 0d
        Vector2.fromX(3)  || 1d
        Vector2.fromY(3)  || 1d
        Vector2.fromY(-3) || 1d
        Vector2.fromY(-1) || -1d
    }
}
