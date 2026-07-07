package edu.tigers.sumatra.pathfinder

import edu.tigers.sumatra.bot.MoveConstraints
import edu.tigers.sumatra.bot.params.BotMovementLimits
import edu.tigers.sumatra.math.vector.Vector2
import spock.lang.Specification

class TrajectoryGeneratorTest extends Specification {
    def "IsComeToAStopFaster"() {
        when:
        var mc = MoveConstraints.builder()
                .limits(BotMovementLimits.builder()
                        .velMax(3)
                        .accMax(2.5)
                        .brkMax(5)
                        .velMaxW(20)
                        .accMaxW(50)
                        .build())
                .build();

        var s0 = Vector2.zero()
        var v0 = Vector2.fromXY((double) v0x, (double) v0y)
        var s1 = Vector2.fromX((double) s1x)
        then:
        TrajectoryGenerator.isComeToAStopFasterTimed(mc, s0, v0, s1, (double) tt) == isComeToAStopFaster

        where:
        v0x  | v0y | s1x  | tt    || isComeToAStopFaster
        // Positive start velocity
        0.1  | 0   | 1000 | 100.0 || false
        0.5  | 0   | 1000 | 100.0 || false
        1.0  | 0   | 1000 | 100.0 || false
        1.5  | 0   | 1000 | 100.0 || false
        2.0  | 0   | 1000 | 100.0 || false
        2.5  | 0   | 1000 | 100.0 || true // Overshooting

        0.1  | 0   | 1000 | 1.0   || false
        0.5  | 0   | 1000 | 1.0   || false
        1.0  | 0   | 1000 | 1.0   || false
        1.5  | 0   | 1000 | 1.0   || false
        2.0  | 0   | 1000 | 1.0   || false
        2.5  | 0   | 1000 | 1.0   || true // Overshooting

        0.1  | 0   | 1000 | 0.2   || false
        0.5  | 0   | 1000 | 0.2   || false
        1.0  | 0   | 1000 | 0.2   || false
        1.5  | 0   | 1000 | 0.2   || false
        2.0  | 0   | 1000 | 0.2   || false
        2.5  | 0   | 1000 | 0.2   || false // Overshooting but not enough time to reach the point, so more overshoot is better

        0.1  | 0   | 1000 | 0.0   || false
        0.5  | 0   | 1000 | 0.0   || false
        1.0  | 0   | 1000 | 0.0   || false
        1.5  | 0   | 1000 | 0.0   || false
        2.0  | 0   | 1000 | 0.0   || false
        2.5  | 0   | 1000 | 0.0   || false // Overshooting but not enough time to reach the point, so more overshoot is better

        // Negative start velocity
        -0.1 | 0   | 1000 | 100.0 || true
        -0.5 | 0   | 1000 | 100.0 || true
        -1.0 | 0   | 1000 | 100.0 || true
        -1.5 | 0   | 1000 | 100.0 || true
        -2.0 | 0   | 1000 | 100.0 || true
        -2.5 | 0   | 1000 | 100.0 || true

        -0.1 | 0   | 1000 | 1.0   || true
        -0.5 | 0   | 1000 | 1.0   || true
        -1.0 | 0   | 1000 | 1.0   || true
        -1.5 | 0   | 1000 | 1.0   || true
        -2.0 | 0   | 1000 | 1.0   || true
        -2.5 | 0   | 1000 | 1.0   || true

        -0.1 | 0   | 1000 | 0.3   || true
        -0.5 | 0   | 1000 | 0.3   || true
        -1.0 | 0   | 1000 | 0.3   || true
        -1.5 | 0   | 1000 | 0.3   || true
        -2.0 | 0   | 1000 | 0.3   || true
        -2.5 | 0   | 1000 | 0.3   || true

        -0.1 | 0   | 1000 | 0.2   || false // Unwanted result, but difference can't grow enough in 0.2s
        -0.5 | 0   | 1000 | 0.2   || true
        -1.0 | 0   | 1000 | 0.2   || true
        -1.5 | 0   | 1000 | 0.2   || true
        -2.0 | 0   | 1000 | 0.2   || true
        -2.5 | 0   | 1000 | 0.2   || true

        -0.1 | 0   | 1000 | 0.1   || false // Unwanted result, but difference can't grow enough in 0.1s
        -0.5 | 0   | 1000 | 0.1   || false // Unwanted result, but difference can't grow enough in 0.1s
        -1.0 | 0   | 1000 | 0.1   || false // Unwanted result, but difference can't grow enough in 0.1s
        -1.5 | 0   | 1000 | 0.1   || false // Unwanted result, but difference can't grow enough in 0.1s
        -2.0 | 0   | 1000 | 0.1   || false // Unwanted result, but difference can't grow enough in 0.1s
        -2.5 | 0   | 1000 | 0.1   || false // Unwanted result, but difference can't grow enough in 0.1s

        -0.1 | 0   | 1000 | 0.0   || false // Unwanted result, but difference can't grow at all in 0s
        -0.5 | 0   | 1000 | 0.0   || false // Unwanted result, but difference can't grow at all in 0s
        -1.0 | 0   | 1000 | 0.0   || false // Unwanted result, but difference can't grow at all in 0s
        -1.5 | 0   | 1000 | 0.0   || false // Unwanted result, but difference can't grow at all in 0s
        -2.0 | 0   | 1000 | 0.0   || false // Unwanted result, but difference can't grow at all in 0s
        -2.5 | 0   | 1000 | 0.0   || false // Unwanted result, but difference can't grow at all in 0s
    }
}
