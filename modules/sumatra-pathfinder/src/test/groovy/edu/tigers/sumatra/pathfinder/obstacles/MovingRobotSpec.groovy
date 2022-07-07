/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles

import edu.tigers.sumatra.bot.IMoveConstraints
import edu.tigers.sumatra.bot.MoveConstraints
import edu.tigers.sumatra.math.vector.IVector2
import edu.tigers.sumatra.math.vector.Vector2f
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator
import edu.tigers.sumatra.trajectory.ITrajectory
import spock.lang.Specification

import static org.hamcrest.Matchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

class MovingRobotSpec extends Specification {
    IMoveConstraints mc = new MoveConstraints(velMax: 3, accMax: 4)
    double tMax = 2.0
    double radius = 90

    def "Bot trajectory close to moving horizon after #t s with #botSpeed m/s"(double t, double botSpeed) {
        given:
        ITrajectory<IVector2> trajectory = TrajectoryGenerator.generatePositionTrajectory(
                mc,
                Vector2f.fromX(1000),
                Vector2f.fromX(botSpeed),
                Vector2f.fromX(100000)
        )
        MovingRobot movingRobot = new MovingRobot(
                trajectory.getPositionMM(0),
                trajectory.getVelocity(0),
                mc.getVelMax(),
                mc.getAccMax(),
                tMax,
                radius
        )

        when:
        var circle = movingRobot.getMovingHorizon(t).withMargin(-radius)
        var pos = trajectory.getPositionMM(t)
        BigDecimal distance = circle.distanceTo(pos)

        then:
        expect distance, closeTo(0.0, 0.001)

        where:
        t    | botSpeed
        0d   | 0
        0.1d | 0
        0.5d | 0
        1d   | 0
        2d   | 0
        0d   | 0.4
        0.5d | 0.4
        0d   | -5
        0.5d | -5
    }
}
