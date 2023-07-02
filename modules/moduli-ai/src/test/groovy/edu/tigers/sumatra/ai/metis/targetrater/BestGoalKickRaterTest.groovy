/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater

import edu.tigers.sumatra.math.SumatraMath
import spock.lang.Specification

class RotationHelperTest extends Specification {
    def "TestCalcRotationTime"() {
        when:
        def result = RotationTimeHelper.calcRotationTime(v0, 0.0, s1, vMax, aMax)
        def resultMirrored = RotationTimeHelper.calcRotationTime(-v0, 0.0, -s1, vMax, aMax)

        then:
        SumatraMath.isEqual(result, expected)
        SumatraMath.isEqual(resultMirrored, expected)

        where:
        v0   | s1  | vMax | aMax || expected
        // https://www.wolframalpha.com/input?i=solve+s%3Dv_0+*+t_1+%2B+1%2F2+*+a+*+t_1*t_1%2C+v_1%3Dv_0%2Ba*t%2C+t%3Dt_1%2C+a%3D1%2C+s%3D1%2C+v_0%3D-0.1+for+t%2C+v_1
        0.0  | 1.0 | 2.0  | 1.0  || 1.41421356237309
        0.1  | 1.0 | 2.0  | 1.0  || 1.31774468787578
        -0.1 | 1.0 | 2.0  | 1.0  || 1.51774468787578

        // https://www.wolframalpha.com/input?i=solve+s%3Ds_1%2Bs_2%2C+t_1%3D%28v_1-v_0%29%2Fa%2C+s_1%3D0.5*%28v_0%2Bv_1%29*t_1%2C+s_2%3Dv_1*t_2%2C+a%3D5%2C+s%3D1%2C+v_0%3D-3%2C+v_1%3D2+for+t_1%2C+t_2
        0.0  | 1.0 | 2.0  | 5.0  || 0.7
        0.1  | 1.0 | 2.0  | 5.0  || 0.6805
        -0.1 | 1.0 | 2.0  | 5.0  || 0.7205

        3.0  | 1.0 | 2.0  | 5.0  || 0.45
        -3.0 | 1.0 | 2.0  | 5.0  || 1.75
    }
}
