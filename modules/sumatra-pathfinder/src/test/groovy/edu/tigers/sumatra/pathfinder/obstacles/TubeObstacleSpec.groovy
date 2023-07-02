/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles


import edu.tigers.sumatra.math.tube.Tube
import edu.tigers.sumatra.math.vector.Vector2
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInputStatic
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class TubeObstacleSpec extends Specification {
    @Subject
    @Shared
    IObstacle obstacle = new TubeObstacle("test", Tube.create(Vector2.zero(), Vector2.fromX(2), 3))

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
        Vector2.zero()    || 0d
        Vector2.fromX(-3) || 0d
        Vector2.fromX(-4) || 1d
        Vector2.fromX(3)  || 0d
        Vector2.fromX(5)  || 0d
        Vector2.fromY(1)  || 0d
        Vector2.fromY(3)  || 0d
    }
}
