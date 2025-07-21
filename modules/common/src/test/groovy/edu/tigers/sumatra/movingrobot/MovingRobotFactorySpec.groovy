/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot

import edu.tigers.sumatra.math.circle.ICircle
import edu.tigers.sumatra.math.vector.IVector2
import edu.tigers.sumatra.math.vector.Vector2f
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory
import edu.tigers.sumatra.trajectory.ITrajectory
import spock.lang.Specification

import static org.hamcrest.Matchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

class MovingRobotFactorySpec extends Specification {
    BangBangTrajectoryFactory factory = new BangBangTrajectoryFactory()

    def "Accelerating bot trajectory close to moving horizon after #t s with #botSpeed m/s"(double t, double botSpeed) {
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
        IMovingRobot movingRobot = MovingRobotFactory.acceleratingRobot(
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

    def "stoppingRobot should have correct offset and radius"(
            double botSpeed,
            double t,
            BigDecimal expectedOffset,
            BigDecimal expectedRadius
    ) {
        given:
        IVector2 pCur = Vector2f.fromXY(70, 30)
        IVector2 vCur = Vector2f.fromX(botSpeed)
        double vLimit = 3
        double aLimit = 4
        double botRadius = 90
        double opponentBotReactionTime = 0

        IMovingRobot movingRobot = MovingRobotFactory.stoppingRobot(
                pCur,
                vCur,
                vLimit,
                aLimit,
                aLimit,
                botRadius,
                opponentBotReactionTime
        )

        when:
        ICircle circle = movingRobot.getMovingHorizon(t).withMargin(-botRadius)
        BigDecimal offset = circle.center().subtractNew(pCur).x()
        BigDecimal radius = circle.radius()
        println("offset: " + offset + ", radius: " + radius)

        then:
        expect offset, closeTo(expectedOffset, 0.001)
        expect radius, closeTo(expectedRadius, 0.001)

        where:
        t   | botSpeed | expectedOffset | expectedRadius
        0   | 2        | 0              | 0
        1   | 2        | 1000           | 750
        2   | 2        | 1500           | 3250
        1   | 0        | 0              | 1000
        0.5 | 3        | 1000           | 0
        1   | 4        | 2000           | 0
        2   | 4        | 3500           | 2500
    }
}
