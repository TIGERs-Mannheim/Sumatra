/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles

import edu.tigers.sumatra.ids.BotID
import edu.tigers.sumatra.ids.ETeamColor
import edu.tigers.sumatra.math.vector.IVector2
import edu.tigers.sumatra.math.vector.IVector3
import edu.tigers.sumatra.math.vector.Vector2
import edu.tigers.sumatra.math.vector.Vector3
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInputStatic
import edu.tigers.sumatra.trajectory.ITrajectory
import spock.lang.Specification
import spock.lang.Subject

class TrajAwareRobotObstacleSpec extends Specification {

    ITrajectory<IVector3> trajectory = Mock()

    @Subject
    IObstacle obstacle = new TrajAwareRobotObstacle(BotID.createBotId(0, ETeamColor.YELLOW), trajectory, 2, 0.5, 200).setMaxSpeed(trajectory.getMaxSpeed())

    def "Can collide"() {
        expect:
        obstacle.canCollide(null)
    }

    def "Distance from #robotPos to obstacle after #timeOffset is #expectedDistance"() {
        given:
        IVector2 robotVel = Vector2.fromXY(Double.NaN, Double.NaN) // should not be used
        var CollisionInput collisionInput = new CollisionInputStatic(robotPos, robotVel, Vector2.zero(), timeOffset)

        and:
        trajectory.getPositionMM(_ as Double) >> Vector3.fromXY(0, 0)
        trajectory.getVelocity(_ as Double) >> Vector3.fromXY(0, 0)

        when:
        def actualDistance = obstacle.distanceTo(collisionInput)

        then:
        actualDistance == expectedDistance

        where:
        robotPos         | timeOffset || expectedDistance
        Vector2.fromX(0) | 0.0d       || -2d
        Vector2.fromX(2) | 0.0d       || 0d
    }
}
