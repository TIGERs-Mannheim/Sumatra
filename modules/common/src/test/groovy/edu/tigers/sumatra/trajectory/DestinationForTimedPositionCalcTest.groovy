/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory

import edu.tigers.sumatra.math.AngleMath
import edu.tigers.sumatra.math.SumatraMath
import edu.tigers.sumatra.math.vector.Vector2
import net.jafama.DoubleWrapper
import net.jafama.FastMath
import spock.lang.Shared
import spock.lang.Specification

class DestinationForTimedPositionCalcTest extends Specification {

    @Shared
    var rand = new Random(0)

    def "DestinationForBangBang2D FindingAlpha"() {

        when:
        var generator = new DestinationForTimedPositionCalc()
        var distanceX = (float) ((double) s1x - (double) s0x)
        var distanceY = (float) ((double) s1y - (double) s0y)
        var s0 = Vector2.fromXY((double) s0x, (double) s0y)
        var s1 = Vector2.fromXY((double) s1x, (double) s1y)
        var v0 = Vector2.fromXY((double) v0x, (double) v0y)

        var testAlphas = SumatraMath.evenDistribution1D(0, AngleMath.PI_HALF, 1000)
        var bestX = null
        var bestY = null
        var bestDiff = Double.MAX_VALUE
        for (testAlpha in testAlphas) {
            DoubleWrapper cos = new DoubleWrapper()
            final float sA = (float) FastMath.sinAndCos(testAlpha, cos)
            final float cA = (float) cos.value

            var x = generator.getTimedPos1D((float) distanceX, (float) v0x, (float) (3.5 * cA), (float) (2.5 * cA), (float) targetTime)
            var y = generator.getTimedPos1D((float) distanceY, (float) v0y, (float) (3.5 * sA), (float) (2.5 * sA), (float) targetTime)

            var diff = Math.abs(x.time() - y.time())
            if (diff < bestDiff) {
                bestDiff = diff
                bestX = x
                bestY = y
            }
        }

        var sTest = generator.destinationForBangBang2D(
                s0,
                s1,
                v0,
                3.5f,
                2.5f,
                (float) targetTime,
                alpha -> alpha
        )
        var sCorrect = Vector2.fromXY(s0.x() + bestX.pos(), s0.y() + bestY.pos())

        then:
        sTest.isCloseTo(sCorrect, 0.01)

        where:
        v0alpha << SumatraMath.evenDistribution1D(0, AngleMath.PI_HALF, 100)
        s0x = rand.nextDouble() * 5
        s0y = rand.nextDouble() * 5
        s1x = rand.nextDouble() * 5
        s1y = rand.nextDouble() * 5
        v0len = rand.nextDouble() * 3.5
        v0x = Math.cos(v0alpha)
        v0y = Math.sin(v0alpha)
        targetTime = rand.nextDouble() * Math.max(Math.sqrt((s1x - s0x) * (s1x - s0x) + (s1y - s0y) * (s1y - s0y)), 2)


    }

    def "DestinationForBangBang2D GetTimedPos1D"() {
        when:
        var generator = new DestinationForTimedPositionCalc()
        var timedPos = generator.getTimedPos1D((float) s, (float) v0, (float) vMax, (float) aMax, (float) tt)
        var timedPosMirrored = generator.getTimedPos1D((float) -s, (float) -v0, (float) vMax, (float) aMax, (float) tt)
        then:
        SumatraMath.isEqual((double) timedPos.time(), (double) time)
        SumatraMath.isEqual((double) timedPos.pos(), (double) pos)

        SumatraMath.isEqual((double) timedPosMirrored.time(), (double) time)
        SumatraMath.isEqual((double) timedPosMirrored.pos(), (double) -pos)
        where:
        s   | v0   | tt                | vMax | aMax || time              | pos
        // Straight too slow
        1.0 | 2.5  | 0.0               | 1.75 | 1.25 || 2.0               | 2.5

        // Trapezoidal finishing early
        // https://www.wolframalpha.com/input?i=solve+s%3D0.5*%28v%2Bv_1%29*t_1%2B+v_1+*+t_2+%2B+0.5*v_1*t_3%2C+v_1+%3D+v%2Ba*t_1%2C+0%3Dv_1-a*t_3%2C+t%3Dt_1%2Bt_2%2Bt_3%2C+v_1%3D3.5%2C+a%3D2.5%2C+s%3D5%2C+v%3D0%2C+for+t
        5.0 | 0.0  | 2.9               | 3.5  | 2.5  || 2.828571428571428 | 5.0
        5.0 | 1.0  | 2.5               | 3.5  | 2.5  || 2.485714285714285 | 5.0
        5.0 | -1.0 | 3.3               | 3.5  | 2.5  || 3.285714285714285 | 5.0
        3.5 | 2.75 | 2.5               | 1.75 | 1.25 || 2.471428571428571 | 3.5

        // Trapezoidal too slow
        // https://www.wolframalpha.com/input?i=solve+s%3Dv*t_1+%2B+0.5*a*t_1**2+%2B+v_1+*+t_2%2C+v_1+%3D+v%2Ba*t_1%2C+t%3Dt_1%2Bt_2%2C+v_1%3D3.5%2C+a%3D2.5%2C+s%3D3%2C+v%3D0+for+t
        // https://www.wolframalpha.com/input?i=solve+s%3D0.5*v*t%2C+t%3Dv%2Fa%2C+a%3D2.5%2C+v+%3D+3.5
        3.0 | 0.0  | 1.557142857142857 | 3.5  | 2.5  || 2.957142857142857 | 5.45
        3.0 | 0.0  | 1.5               | 3.5  | 2.5  || 2.957142857142857 | 5.45
        3.0 | 1.0  | 1.2               | 3.5  | 2.5  || 2.614285714285714 | 5.45
        3.0 | -1.0 | 2.0               | 3.5  | 2.5  || 3.414285714285714 | 5.45
        3.5 | 2.75 | 1.7               | 1.75 | 1.25 || 3.171428571428571 | 4.725

        // Trapezoidal direct
        // https://www.wolframalpha.com/input?i=solve+s%3D0.5*%28v%2Bv_1%29*t_1%2B+v_1+*+t_2+%2B+0.5*%28v_1%2Bv_2%29*t_3%2C+v_1+%3D+v%2Ba*t_1%2C+v_2%3Dv_1-a*t_3%2C+2.7%3Dt_1%2Bt_2%2Bt_3%2C+v_1%3D3.5%2C+a%3D2.5%2C+s%3D5%2C+v%3D0%2C+for+v_2
        // https://www.wolframalpha.com/input?i=solve+s%3D0.5*v*t%2C+t%3Dv%2Fa%2C+a%3D2.5%2C+v+%3D+0.33772233983162066800
        5.0 | 0.0  | 2.7               | 3.5  | 2.5  || 2.835089          | 5.0228113
        5.0 | 0.0  | 2.6               | 3.5  | 2.5  || 2.851087          | 5.0788061
        5.0 | 0.0  | 2.5               | 3.5  | 2.5  || 2.880196          | 5.180686
        5.0 | 1.0  | 2.2               | 3.5  | 2.5  || 2.522967          | 5.130385
        5.0 | -1.0 | 2.8               | 3.5  | 2.5  || 3.425403          | 5.488912
        3.5 | 2.75 | 1.9               | 1.75 | 1.25 || 2.7               | 3.9

        //Triangular finishing early
        // https://www.wolframalpha.com/input?i=solve+s%3Dv*t_1+%2B+0.5*a*t_1**2+%2B+v_1+*+t_2+-+0.5*a*t_2**2%2C+t%3Dt_1%2Bt_2%2C+t_2%3DAbs%5Bv_1%2Fa%5D%2C+v_1%3Dv+%2B+a+*+t_1%2C+a%3D2.5%2C+s%3D1%2C+v%3D-1
        1.0 | 0.0  | 1.3               | 3.5  | 2.5  || 1.264911064067351 | 1.0
        1.0 | 0.0  | 1.264911064067351 | 3.5  | 2.5  || 1.264911064067351 | 1.0
        1.0 | 1.0  | 1.0               | 3.5  | 2.5  || 0.985640646055101 | 1.0
        1.0 | -1.0 | 1.8               | 3.5  | 2.5  || 1.785640646055101 | 1.0

        // Triangular too slow
        // https://www.wolframalpha.com/input?i=solve+s%3Dv*t_1+%2B+0.5*a*t_1**2%2C+s_1%3Ds+%2B+v_1+*+t_2+-+0.5*a*t_2**2%2C+t%3Dt_1%2Bt_2%2C+v_1%3Dv+%2B+a+*+t_1%2C+t_2%3Dv_1%2Fa%2C+a%3D2.5%2C+s%3D1%2C+v%3D0
        1.0 | 0.0  | 0.0               | 3.5  | 2.5  || 1.788854381999831 | 2.0
        1.0 | 1.0  | 0.0               | 3.5  | 2.5  || 1.559591794226543 | 2.2
        1.0 | -1.0 | 0.0               | 3.5  | 2.5  || 2.359591794226543 | 2.2

        // Triangular direct
        // https://www.wolframalpha.com/input?i=solve+s%3Dv*t_1+%2B+0.5*a*t_1**2+%2B+v_1+*+t_2+-+0.5*a*t_2**2%2C+t%3Dt_1%2Bt_2%2C+t%3D1.5%2C+v_1%3Dv+%2B+a+*+t_1%2C+a%3D2.5%2C+s%3D1%2C+v%3D-1%2Cv_2%3Dv_1-a*t_2
        // https://www.wolframalpha.com/input?i=solve+s%3D0.5*v*t%2C+t%3Dv%2Fa%2C+a%3D2.5%2C+v+%3D+1%2F4+%2811+-+5+sqrt%282%29%29
        1.0 | 0.0  | 1.0               | 3.5  | 2.5  || 1.367544467966324 | 1.16886116991581
        1.0 | 1.0  | 0.8               | 3.5  | 2.5  || 1.020204          | 1.0606123
        1.0 | -1.0 | 1.5               | 3.5  | 2.5  || 1.892893          | 1.192956
    }
}
