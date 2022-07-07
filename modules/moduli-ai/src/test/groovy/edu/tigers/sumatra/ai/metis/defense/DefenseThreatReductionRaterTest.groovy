/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense

import edu.tigers.sumatra.math.SumatraMath
import spock.lang.Specification

class DefenseThreatReductionRaterTest extends Specification {
    def "TestCombineThreatRatings"() {
        given:
        var rater = new DefenseThreatReductionRater()

        when:
        def result = rater.combineThreatRatings(input)

        then:
        SumatraMath.isEqual(result, expected)

        where:
        input                                || expected
        [1.0d]                               || 1.0d
        [0.0d]                               || 0.0d
        [0.5d]                               || 0.5d
        [0.5d, 0.5d]                         || 0.75d
        [0.33d, 0.67d]                       || 0.7789d
        [0.0d, 0.2d, 0.4d, 0.6d, 0.8d, 1.0d] || 1.0d
        [0.2d, 0.4d, 0.6d, 0.8d]             || 0.9616d
        [0.0d, 0.2d, 0.4d, 0.6d, 0.8d]       || 0.9616d
    }
}
