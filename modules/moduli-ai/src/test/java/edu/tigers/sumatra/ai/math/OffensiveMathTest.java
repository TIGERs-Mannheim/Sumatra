/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math;

import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


/**
 * @author Mark Geiger <MarkGeiger@posteo.de>
 */
public class OffensiveMathTest
{
	
	@Test
	public void testBallAccessibilityMath()
	{
		List<AngleRange> unaccessibleAngles = new ArrayList<>();
		unaccessibleAngles.add(new AngleRange(0,0.2));

		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, 0.1)).isFalse();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, 0.3)).isTrue();

		unaccessibleAngles.add(new AngleRange(0.25,0.4));
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, 0.3)).isFalse();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, -0.1)).isTrue();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, -0.4)).isTrue();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, -0.7)).isTrue();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, -0.5)).isTrue();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, Math.PI/2.0)).isTrue();

		unaccessibleAngles = new ArrayList<>();
		unaccessibleAngles.add(new AngleRange(-0.1,0.1));
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, 0.0)).isFalse();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, 0.3)).isTrue();

		unaccessibleAngles = new ArrayList<>();
		unaccessibleAngles.add(new AngleRange(-Math.PI/2.0,Math.PI/2.0));
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, 0.0)).isFalse();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, Math.PI/2.0 + 0.1)).isTrue();
		assertThat(OffensiveMath.isAngleAccessible(unaccessibleAngles, -Math.PI/2.0 - 0.1)).isTrue();
	}
	
}
