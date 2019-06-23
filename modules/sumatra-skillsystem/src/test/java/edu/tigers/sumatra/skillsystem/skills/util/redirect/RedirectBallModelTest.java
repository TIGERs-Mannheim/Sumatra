/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.redirect;

import static edu.tigers.sumatra.math.AngleMath.PI;
import static edu.tigers.sumatra.math.AngleMath.PI_HALF;
import static edu.tigers.sumatra.math.AngleMath.PI_QUART;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectBallModelTest
{
	@Test
	public void testBallCollision() throws Exception
	{
		IVector2 normal = AVector2.X_AXIS;
		assertThat(RedirectBallModel.ballCollision(Vector2.fromXY(0, 0), normal, 0.0)).isEqualTo(Vector2.zero());
		assertThat(RedirectBallModel.ballCollision(Vector2.fromXY(-1, 0), normal, 1.0)).isEqualTo(Vector2.zero());
		assertThat(RedirectBallModel.ballCollision(Vector2.fromXY(-1, 0), normal, 0.0).getLength2()).isEqualTo(1);
		assertThat(RedirectBallModel.ballCollision(Vector2.fromXY(-2, 0), normal, 0.0).getLength2()).isEqualTo(2);
		assertThat(RedirectBallModel.ballCollision(Vector2.fromXY(-1, 0), normal, 0.5).getLength2()).isEqualTo(0.5);
		assertThat(RedirectBallModel.ballCollision(Vector2.fromAngle(PI_HALF), normal, 0.3).getLength2()).isEqualTo(1.0);
		assertThat(RedirectBallModel.ballCollision(Vector2.fromAngle(-PI_HALF), normal, 0.3).getLength2()).isEqualTo(1.0);
		// assertThat(RedirectBallModel.ballCollision(Vector2.fromAngle(PI + PI_QUART), normal, 0.4).getLength2())
		// .isEqualTo(0.8);
		// assertThat(RedirectBallModel.ballCollision(Vector2.fromAngle(PI - PI_QUART), normal, 0.4).getLength2())
		// .isEqualTo(0.8);
		
		assertThat(RedirectBallModel.ballCollision(Vector2.fromXY(-1, 0), normal, 0.0)).isEqualTo(normal);
		assertThat(RedirectBallModel.ballCollision(Vector2.fromXY(-2, 0), normal, 0.0)).isEqualTo(normal.multiplyNew(2));
		assertThat(RedirectBallModel.ballCollision(Vector2.fromAngle(PI_HALF), normal, 0.3)).isEqualTo(Vector2.fromY(-1));
		assertThat(RedirectBallModel.ballCollision(Vector2.fromAngle(-PI_HALF), normal, 0.3)).isEqualTo(Vector2.fromY(1));
		// assertThat(RedirectBallModel.ballCollision(Vector2.fromAngle(PI + PI_QUART), normal, 0.3))
		// .isEqualTo(Vector2.fromAngle(-PI_QUART).scaleTo(0.85));
		// assertThat(RedirectBallModel.ballCollision(Vector2.fromAngle(PI - PI_QUART), normal, 0.6))
		// .isEqualTo(Vector2.fromAngle(PI_QUART).scaleTo(0.7));
	}
	
	
	@Test
	public void testKickerRedirect()
	{
		IVector2 normal = AVector2.Y_AXIS;
		assertThat(RedirectBallModel.kickerRedirect(AVector2.ZERO_VECTOR, normal, 0.0, 1)).isEqualTo(normal);
		assertThat(RedirectBallModel.kickerRedirect(Vector2.fromXY(0, -1), normal, 1.0, 1)).isEqualTo(normal);
		assertThat(RedirectBallModel.kickerRedirect(Vector2.fromXY(0, -1), normal, 0.0, 1))
				.isEqualTo(normal.multiplyNew(2));
		// assertThat(RedirectBallModel.kickerRedirect(Vector2.fromAngle(-PI_HALF - PI_QUART), normal, 1.0,
		// 0).getLength2())
		// .isEqualTo(0.5);
	}
	
	
	@Test
	public void testCalcTargetAngle()
	{
		validateTargetAngle(Vector2.fromAngle(PI).scaleTo(1), 1, 0, 1, 0);
		validateTargetAngle(Vector2.fromAngle(PI).scaleTo(1), 1, 0, 0.5, 0);
		validateTargetAngle(Vector2.fromAngle(PI).scaleTo(1), 1, 0, 0, 0);
		validateTargetAngle(Vector2.fromAngle(PI - PI_QUART).scaleTo(1), 0, PI_QUART, 0.4, 0);
	}
	
	
	private void validateTargetAngle(final IVector2 ballVel,
			final double shootSpeed,
			final double ballTargetDir,
			final double ballDampFactor,
			final double expectedDir)
	{
		for (double initOrientation = -PI; initOrientation < PI; initOrientation += 0.1)
		{
			double result = RedirectBallModel.calcTargetOrientation(ballVel, shootSpeed, ballTargetDir, initOrientation,
					ballDampFactor);
			assertThat(result)
					.isCloseTo(expectedDir, Offset.offset(0.005));
		}
	}
	
	
	@Test
	public void testCalcKickSpeed()
	{
		assertThat(RedirectBallModel.calcKickSpeed(
				Vector2.fromXY(-1, 0),
				Vector2.fromXY(1, 0),
				1,
				0.5)).isCloseTo(0.5, Offset.offset(0.05));
	}
}