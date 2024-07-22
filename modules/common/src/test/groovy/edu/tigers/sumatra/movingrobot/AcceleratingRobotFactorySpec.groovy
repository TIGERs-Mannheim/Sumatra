/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot

import edu.tigers.sumatra.math.vector.IVector2
import edu.tigers.sumatra.math.vector.Vector2f
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory
import edu.tigers.sumatra.trajectory.ITrajectory
import spock.lang.Specification

import static org.hamcrest.Matchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

class AcceleratingRobotFactorySpec extends Specification {
    BangBangTrajectoryFactory factory = new BangBangTrajectoryFactory()

    def "Bot trajectory close to moving horizon after #t s with #botSpeed m/s"(double t, double botSpeed) {
        given:
        double velMax = 3
        double accMax = 4
        double radius = 90
        double opponentBotReactionTime = 0.0
        ITrajectory<IVector2> trajectory = factory.sync(
                Vector2f.fromX(1),
                Vector2f.fromX(100),
                Vector2f.fromX(botSpeed),
                velMax,
                accMax,
        )
        IMovingRobot movingRobot = AcceleratingRobotFactory.create(
                trajectory.getPositionMM(0),
                trajectory.getVelocity(0),
                velMax,
                accMax
                ,
                radius,
                opponentBotReactionTime
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
