package edu.tigers.sumatra.movingrobot

import edu.tigers.sumatra.math.circle.ICircle
import edu.tigers.sumatra.math.vector.IVector2
import edu.tigers.sumatra.math.vector.Vector2f
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory
import edu.tigers.sumatra.trajectory.ITrajectory
import spock.lang.Shared
import spock.lang.Specification

import static org.hamcrest.Matchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

class MovingRobotFactorySpec extends Specification {
    BangBangTrajectoryFactory factory = new BangBangTrajectoryFactory()
    @Shared
    Random random = new Random(42)

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
                MovingRobotParams.builder()
                        .position(trajectory.getPositionMM(0))
                        .velocity(trajectory.getVelocity(0))
                        .vLimit(velMax)
                        .aLimit(accMax)
                        .brkLimit(accMax)
                        .reactionTime(opponentBotReactionTime)
                        .radius(radius)
                        .build()
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
        double botRadius = 90
        double opponentBotReactionTime = 0

        IMovingRobot movingRobot = MovingRobotFactory.stoppingRobot(
                MovingRobotParams.builder()
                        .position(pCur)
                        .velocity(vCur)
                        .vLimit(vLimit)
                        .aLimit(aLimit)
                        .brkLimit(brkLimit)
                        .radius(botRadius)
                        .reactionTime(opponentBotReactionTime)
                        .build()
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
        t   | botSpeed | expectedOffset | expectedRadius | vLimit | aLimit | brkLimit
        0   | 2        | 0              | 0              | 3      | 4      | 4
        1   | 2        | 1000           | 750            | 3      | 4      | 4
        2   | 2        | 1500           | 3250           | 3      | 4      | 4
        1   | 0        | 0              | 1000           | 3      | 4      | 4
        0.5 | 3        | 1000           | 0              | 3      | 4      | 4
        1   | 4        | 2000           | 0              | 3      | 4      | 4
        2   | 4        | 3000           | 2000           | 3      | 4      | 4
        1   | 4        | 2000           | 0              | 3      | 2      | 4
        1   | 2        | 1000           | 1000 * 2 / 3   | 3      | 2      | 4
    }

    def "slowingDownRobot should have correct offset and radius"(
            double botSpeed,
            double t,
            BigDecimal expectedOffset,
            BigDecimal expectedRadius
    ) {
        given:
        IVector2 pCur = Vector2f.fromXY(70, 30)
        IVector2 vCur = Vector2f.fromX(botSpeed)
        double botRadius = 90
        double opponentBotReactionTime = 0

        IMovingRobot movingRobot = MovingRobotFactory.slowingDownRobot(
                MovingRobotParams.builder()
                        .position(pCur)
                        .velocity(vCur)
                        .vLimit(vLimit)
                        .aLimit(aLimit)
                        .brkLimit(brkLimit)
                        .radius(botRadius)
                        .reactionTime(opponentBotReactionTime)
                        .build(),
                vLimitAtHorizon
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
        t   | botSpeed | expectedOffset | expectedRadius | vLimit | aLimit | brkLimit | vLimitAtHorizon
        0   | 2        | 0              | 0              | 3      | 4      | 4        | 2
        1   | 2        | 1375           | 1375           | 3      | 4      | 4        | 2
        2   | 2        | 1500           | 4250           | 3      | 4      | 4        | 2
        1   | 0        | 0              | 1750           | 3      | 4      | 4        | 2
        0.5 | 3        | 1187.5         | 187.5          | 3      | 4      | 4        | 2
        1   | 4        | 2500           | 500            | 3      | 4      | 4        | 2
        2   | 4        | 3125           | 2875           | 3      | 4      | 4        | 2
        1   | 4        | 2500           | 500            | 3      | 2      | 4        | 2
        1   | 2        | 1437.5         | 1187.5         | 3      | 2      | 4        | 2
    }

    def "stopping robot should match BangBangTrajectory1D"() {
        given:

        // This is necessary, as BangBangTrajectoryFactory clamps vCur slightly above vLimit
        if (botSpeed > vLimit && botSpeed < vLimit + 0.2)
            botSpeed = vLimit

        IVector2 pCur = Vector2f.fromXY(30, 70)
        IVector2 vCur = Vector2f.fromY(botSpeed)

        IMovingRobot movingRobot = MovingRobotFactory.stoppingRobot(
                MovingRobotParams.builder()
                        .position(pCur)
                        .velocity(vCur)
                        .vLimit(vLimit)
                        .aLimit(aLimit)
                        .brkLimit(aLimit)
                        .radius(botRadius)
                        .reactionTime(opponentBotReactionTime)
                        .build()
        )

        when:
        ICircle circle = movingRobot.getMovingHorizon(t).withMargin(-botRadius)
        double offset = circle.center().subtractNew(pCur).y()
        double radius = circle.radius()
        double minExtend = offset - radius
        double maxExtend = offset + radius

        var minTraj = factory.single(0, minExtend * 1e-3, botSpeed, vLimit, aLimit)
        var maxTraj = factory.single(0, maxExtend * 1e-3, botSpeed, vLimit, aLimit)

        then:
        expect minTraj.getPositionMM(t), closeTo(minExtend, 1)
        expect maxTraj.getPositionMM(t), closeTo(maxExtend, 1)

        where:
        iteration << (1..100)
        botSpeed = (random.nextDouble() - 0.5) * 8 // [-4 ; 4]
        t = random.nextDouble() * 2

        vLimit = 3
        aLimit = 4
        botRadius = 90
        opponentBotReactionTime = 0
    }

    def "slowing down robot should match StoppingRobot for vLimitAtHorizon = 0"() {
        given:

        // This is necessary, as BangBangTrajectoryFactory clamps vCur slightly above vLimit
        if (botSpeed > vLimit && botSpeed < vLimit + 0.2)
            botSpeed = vLimit

        IVector2 pCur = Vector2f.fromXY(30.0, 70.0)
        IVector2 vCur = Vector2f.fromY(botSpeed)

        IMovingRobot slowingDownRobot = MovingRobotFactory.slowingDownRobot(
                MovingRobotParams.builder()
                        .position(pCur)
                        .velocity(vCur)
                        .vLimit(vLimit)
                        .aLimit(aLimit)
                        .brkLimit(aLimit)
                        .radius(botRadius)
                        .reactionTime(opponentBotReactionTime)
                        .build(),
                0
        )

        IMovingRobot stoppingRobot = MovingRobotFactory.stoppingRobot(
                MovingRobotParams.builder()
                        .position(pCur)
                        .velocity(vCur)
                        .vLimit(vLimit)
                        .aLimit(aLimit)
                        .brkLimit(aLimit)
                        .radius(botRadius)
                        .reactionTime(opponentBotReactionTime)
                        .build()
        )

        when:
        ICircle circleSlowingDown = slowingDownRobot.getMovingHorizon(t).withMargin(-botRadius)
        ICircle circleStopping = stoppingRobot.getMovingHorizon(t).withMargin(-botRadius)

        BigDecimal slowingDownX = circleSlowingDown.center().subtractNew(pCur).x()
        BigDecimal slowingDownY = circleSlowingDown.center().subtractNew(pCur).y()
        BigDecimal slowingDownRadius = circleSlowingDown.radius()

        BigDecimal stoppingX = circleStopping.center().subtractNew(pCur).x()
        BigDecimal stoppingY = circleStopping.center().subtractNew(pCur).y()
        BigDecimal stoppingRadius = circleStopping.radius()

        then:
        expect slowingDownX, closeTo(stoppingX, 0.001)
        expect slowingDownY, closeTo(stoppingY, 0.001)
        expect slowingDownRadius, closeTo(stoppingRadius, 0.001)

        where:
        iteration << (1..100)
        botSpeed = (random.nextDouble() - 0.5) * 8 // [-4 ; 4]
        t = random.nextDouble() * 2

        vLimit = 3
        aLimit = 4
        botRadius = 90
        opponentBotReactionTime = 0
    }
}
