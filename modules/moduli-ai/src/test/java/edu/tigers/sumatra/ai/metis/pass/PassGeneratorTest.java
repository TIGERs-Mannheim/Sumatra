/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class PassGeneratorTest
{
	@Test
	public void testBallAccessibilityMath()
	{
		List<AngleRange> angles = new ArrayList<>();
		angles.add(AngleRange.fromAngles(0, 0.2));

		assertThat(PassGenerator.isAngleAccessible(angles, 0.1)).isFalse();
		assertThat(PassGenerator.isAngleAccessible(angles, 0.3)).isTrue();

		angles.add(AngleRange.fromAngles(0.25, 0.4));
		assertThat(PassGenerator.isAngleAccessible(angles, 0.3)).isFalse();
		assertThat(PassGenerator.isAngleAccessible(angles, -0.1)).isTrue();
		assertThat(PassGenerator.isAngleAccessible(angles, -0.4)).isTrue();
		assertThat(PassGenerator.isAngleAccessible(angles, -0.7)).isTrue();
		assertThat(PassGenerator.isAngleAccessible(angles, -0.5)).isTrue();
		assertThat(PassGenerator.isAngleAccessible(angles, Math.PI / 2.0)).isTrue();

		angles = new ArrayList<>();
		angles.add(AngleRange.fromAngles(-0.1, 0.1));
		assertThat(PassGenerator.isAngleAccessible(angles, 0.0)).isFalse();
		assertThat(PassGenerator.isAngleAccessible(angles, 0.3)).isTrue();

		angles = new ArrayList<>();
		angles.add(AngleRange.fromAngles(-Math.PI / 2.0, Math.PI / 2.0));
		assertThat(PassGenerator.isAngleAccessible(angles, 0.0)).isFalse();
		assertThat(PassGenerator.isAngleAccessible(angles, Math.PI / 2.0 + 0.1)).isTrue();
		assertThat(PassGenerator.isAngleAccessible(angles, -Math.PI / 2.0 - 0.1)).isTrue();
	}
}