/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.pose;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.Vector3;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class PoseTest
{
	@Test
	public void interpolate()
	{
		Pose pose1 = Pose.from(Vector3.fromXYZ(2, 10, 0));
		Pose pose2 = Pose.from(Vector3.fromXYZ(4, 12, 2));
		assertThat(pose1.interpolate(pose2, 0.0)).isEqualTo(pose1);
		assertThat(pose2.interpolate(pose1, 0.0)).isEqualTo(pose2);
		assertThat(pose1.interpolate(pose2, 1.0)).isEqualTo(pose2);
		assertThat(pose2.interpolate(pose1, 1.0)).isEqualTo(pose1);
		assertThat(pose1.interpolate(pose2, 0.5)).isEqualTo(Pose.from(Vector3.fromXYZ(3, 11, 1)));
	}
	
	
	@Test
	public void interpolateOrientation()
	{
		Pose pose1 = Pose.from(Vector3.fromXYZ(0, 0, AngleMath.PI + AngleMath.PI_HALF + AngleMath.PI_QUART));
		Pose pose2 = Pose.from(Vector3.fromXYZ(0, 0, AngleMath.PI_HALF + AngleMath.PI_QUART));
		assertThat(pose1.interpolate(pose2, 0.5)).isEqualTo(Pose.from(Vector3.fromXYZ(0, 0, AngleMath.PI_QUART)));
		Pose pose3 = Pose.from(Vector3.fromXYZ(0, 0, -AngleMath.PI_QUART));
		assertThat(pose3.interpolate(pose2, 0.5)).isEqualTo(Pose.from(Vector3.fromXYZ(0, 0, AngleMath.PI_QUART)));
	}
}