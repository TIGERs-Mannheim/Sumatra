/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot


import edu.tigers.sumatra.math.circle.ICircle
import edu.tigers.sumatra.math.vector.IVector2
import edu.tigers.sumatra.math.vector.Vector2f
import spock.lang.Specification

import static org.hamcrest.Matchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

class StoppingRobotFactorySpec extends Specification {
    def "should have correct offset and radius"(
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

        IMovingRobot movingRobot = StoppingRobotFactory.create(
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
