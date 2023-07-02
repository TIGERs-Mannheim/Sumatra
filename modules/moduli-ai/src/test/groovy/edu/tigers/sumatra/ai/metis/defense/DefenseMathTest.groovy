/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense

import edu.tigers.sumatra.geometry.Geometry
import edu.tigers.sumatra.math.line.Lines
import edu.tigers.sumatra.math.vector.Vector2
import spock.lang.Specification

class DefenseMathTest extends Specification {

    def "GetProtectionLine"() {
        when:
        def goalX = Geometry.getGoalOur().getCenter().x()
        def penAreaX = goalX + Geometry.getPenaltyAreaDepth()

        def threat = Vector2.fromX(penAreaX + threatX)
        def target = Vector2.fromX(goalX)

        def threatLine = Lines.segmentFromPoints(threat, target)
        def maxGoOutDistance = Geometry.getPenaltyAreaDepth() + maxGoOutX
        def pl = DefenseMath.getProtectionLine(threatLine, marginToThreat, marginToPenArea, maxGoOutDistance)

        def start = Vector2.fromX(penAreaX + startX)
        def end = Vector2.fromX(penAreaX + endX)

        then:
        pl.getPathStart().isCloseTo(start)
        pl.getPathEnd().isCloseTo(end)

        where:
        marginToThreat | marginToPenArea | maxGoOutX | threatX || startX | endX
        0              | 0               | 0         | 100     || 0      | 0

        0              | 0               | 100       | 100     || 100    | 0
        50             | 0               | 100       | 100     || 50     | 0
        0              | 50              | 100       | 100     || 100    | 50
        25             | 25              | 100       | 100     || 75     | 25
        50             | 50              | 100       | 100     || 50     | 50
        51             | 51              | 100       | 100     || 51     | 51

        0              | 0               | 70        | 100     || 70     | 0
        50             | 0               | 70        | 100     || 50     | 0
        0              | 50              | 70        | 100     || 70     | 50
        35             | 25              | 70        | 100     || 65     | 25
    }
}
