/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */





package edu.tigers.sumatra.math

import spock.lang.Specification

class SumatraMathSpec extends Specification {

    def "#number has digits after decimal: #hasDigits"() {
        expect:
        SumatraMath.hasDigitsAfterDecimalPoint(number) == hasDigits

        where:
        number | hasDigits
        1.0    | false
        1.22   | true
        -1.02  | true
        -1.0   | false
    }

    def "even distribution with #n in [#min, #max] is #result"() {
        expect:
        SumatraMath.evenDistribution1D(min, max, n) == result

        where:
        min | max | n | result
        0   | 3   | 3 | [0.5d, 1.5, 2.5]
        0   | 0   | 0 | []
        0   | 0   | 1 | [0]
        0   | 0   | 2 | [0, 0]
        0   | 1   | 0 | []
        0   | 1   | 1 | [0.5]
        0   | 2   | 2 | [0.5, 1.5]
    }

    def "square of #x is #x2"() {
        expect:
        SumatraMath.square(x) == x2

        where:
        x  | x2
        0  | 0
        1  | 1
        2  | 4
        3  | 9
        -1 | 1
        -2 | 4
    }
}
