/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles

import edu.tigers.sumatra.math.rectangle.Rectangle
import edu.tigers.sumatra.math.vector.Vector2
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInputStatic
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class FieldBorderObstacleSpec extends Specification {

    @Subject
    @Shared
    IObstacle obstacle = new FieldBorderObstacle(Rectangle.fromCenter(Vector2.zero(), 2000, 1000))

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
        robotPos                 || expectedDistance
        Vector2.zero()           || 500d
        Vector2.fromX(900)       || 100d
        Vector2.fromX(1500)      || 0d
        Vector2.fromX(-1500)     || 0d
        Vector2.fromY(1500)      || 0d
        Vector2.fromY(-1500)     || 0d
        Vector2.fromXY(800, 400) || 100d
    }
}
