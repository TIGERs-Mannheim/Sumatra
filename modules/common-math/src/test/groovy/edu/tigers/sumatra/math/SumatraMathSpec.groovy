/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */


package edu.tigers.sumatra.math

import spock.lang.Specification

import static org.hamcrest.Matchers.hasSize

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

    def "roots of quadratic equation"() {
        given:
        def calculated = SumatraMath.quadraticFunctionRoots(a2, a1, a0).stream().sorted().toList()
        def expected = result.stream().sorted().toList()
        expect:
        calculated hasSize(expected.size())
        for (int i = 0; i < calculated.size(); ++i) {
            assert SumatraMath.isEqual(calculated.get(i), expected.get(i))
        }

        where:
        a2 | a1 | a0 | result
        0  | 0  | 1  | []
        0  | 2  | 1  | [-0.5d]
        2  | 2  | 1  | []
        1  | 2  | 1  | [-1.0d]
        1  | 6  | 5  | [-5d, -1d]
    }

    def "roots of cubic equation"() {
        given:
        def calculated = SumatraMath.cubicFunctionRoots(a3, a2, a1, a0).stream().sorted().toList()
        def expected = result.stream().sorted().toList()
        expect:
        calculated hasSize(expected.size())
        for (int i = 0; i < calculated.size(); ++i) {
            assert SumatraMath.isEqual(calculated.get(i), expected.get(i))
        }

        where:
        a3 | a2  | a1 | a0  | result
        0  | 1   | 6  | 5   | [-5d, -1d]
        1  | 2   | 3  | 4   | [-1.6506d]
        1  | -8  | 3  | 4   | [-0.5311d, 1d, 7.5311d]
        1  | -8  | 0  | 0   | [0d, 8d]
        2  | 14  | 32 | 24  | [-3d, -2d]
        2  | -11 | 20 | -12 | [1.5d, 2d]
    }

    def "roots of quartic equation"() {
        given:
        def calculated = SumatraMath.quarticFunctionRoots(a4, a3, a2, a1, a0).stream().sorted().toList()
        def expected = result.stream().sorted().toList()
        expect:
        calculated hasSize(expected.size())
        for (int i = 0; i < calculated.size(); ++i) {
            assert SumatraMath.isEqual(calculated.get(i), expected.get(i))
        }

        where:
        a4 | a3 | a2 | a1 | a0 | result
        0  | 1  | -8 | 3  | 4  | [-0.5311d, 1d, 7.5311d]
        1  | 1  | 1  | 1  | 0  | [-1d, 0d] // https://www.wolframalpha.com/input?i=x%5E4+%2B+x%5E3+%2B+x%5E2+%2B+x+%3D+0
        1  | -1 | 1  | 2  | -3 | [-1.2134d, 1d] // https://www.wolframalpha.com/input?i=x%5E4+-+x%5E3+%2B+x%5E2+%2B+2x+-+3+%3D+0
        1  | -2 | 3  | -4 | -5 | [-0.6841d, 2.0591d] //https://www.wolframalpha.com/input?i=x%5E4+-+2+x%5E3+-+3+x%5E2+-+4+x++-+5+%3D+0
        1  | 0  | -5 | 0  | 4  | [-2, -1, 1, 2] //https://www.wolframalpha.com/input?i=%28x-1%29%28x%2B1%29%28x-2%29%28x%2B2%29+%3D+0
    }
}
