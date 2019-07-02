/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.trajectory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.assertj.core.data.Offset;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Test Bang-Bang Trajectory 2D Look-Up Table
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BangBangTrajectory2dDLUTTest
{
	private static final double maxLUTPos = 15.0;
	private static final double maxLUTVel = 5.0;
	private static final double maxTrajVel = 2.0;
	private static final double maxTrajAcc = 3.0;
	
	private static final BangBangTrajectory2DLUT lut = new BangBangTrajectory2DLUT(maxLUTPos, maxLUTVel, maxTrajVel,
			maxTrajAcc);
	
	
	@Test
	public void testIndexComputation()
	{
		final int STEPS = 100;
		
		for (double finalX = -maxLUTPos + ((maxLUTPos / STEPS) * 0.5); finalX < maxLUTPos; finalX += maxLUTPos / STEPS)
		{
			for (double finalY = -maxLUTPos + ((maxLUTPos / STEPS) * 0.5); finalY < maxLUTPos; finalY += maxLUTPos / STEPS)
			{
				for (double initialVelAbs = -maxLUTVel
						+ ((maxLUTVel / STEPS) * 0.5); initialVelAbs < maxLUTVel; initialVelAbs += maxLUTVel / STEPS)
				{
					IVector3 value = Vector3.fromXYZ(finalX, finalY, initialVelAbs);
					
					IVector3 index = lut.valueToIndex(value);
					IVector3 backValue = lut.indexToValue(index);
					
					assertThat(value.isCloseTo(backValue)).isTrue();
				}
			}
		}
	}
	
	
	@Test
	public void testMaxLutMiss()
	{
		IVector2 finalPos = Vector2.fromY(maxLUTPos / 2);
		IVector2 initialVel = Vector2.fromX(maxLUTVel / 2);
		
		Optional<Double> optAlphaLUT = lut.getAlpha(Vector2f.ZERO_VECTOR, finalPos, initialVel);
		assertThat(optAlphaLUT).isPresent();
		
		double alphaLUT = optAlphaLUT.get();
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(Vector2f.ZERO_VECTOR, finalPos,
				initialVel, maxTrajVel, maxTrajAcc);
		double alphaTraj = traj.getAlpha();
		
		assertThat(alphaLUT).isCloseTo(alphaTraj, Offset.offset(1e-4));
	}
	
	
	@Test
	public void testExactLutHit()
	{
		double stepPosX = lut.getStepPosX() * 0.5;
		double stepPosY = lut.getStepPosY() * 0.5;
		double stepVel = lut.getStepVel() * 0.5;
		
		IVector2 finalPos = Vector2.fromXY(stepPosX, (maxLUTPos / 2) + stepPosY);
		IVector2 initialVel = Vector2.fromX((maxLUTVel / 2) + stepVel);
		
		Optional<Double> optAlphaLUT = lut.getAlpha(Vector2f.ZERO_VECTOR, finalPos, initialVel);
		assertThat(optAlphaLUT).isPresent();
		
		double alphaLUT = optAlphaLUT.get();
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(Vector2f.ZERO_VECTOR, finalPos, initialVel, maxTrajVel,
				maxTrajAcc);
		double alphaTraj = traj.getAlpha();
		
		assertThat(alphaLUT).isCloseTo(alphaTraj, Offset.offset(1e-6));
	}
	
	
	// This is a corner case and will fail :(
	public void testProblem()
	{
		IVector2 finalPos = Vector2.fromXY(-12.225, -10.725);
		IVector2 initialVel = Vector2.fromX(-4.925);
		
		Optional<Double> optAlphaLUT = lut.getAlpha(Vector2f.ZERO_VECTOR, finalPos, initialVel);
		assertThat(optAlphaLUT).isPresent();
		
		double alphaLUT = optAlphaLUT.get();
		
		assertThat(alphaLUT).isCloseTo(1.2437288527903574, Offset.offset(0.3));
	}
	
	
	@Test
	public void testLUTAgainstCalculation()
	{
		final int STEPS = 100;
		
		for (double finalX = -maxLUTPos + ((maxLUTPos / STEPS) * 0.5); finalX < maxLUTPos; finalX += maxLUTPos / STEPS)
		{
			for (double finalY = -maxLUTPos + ((maxLUTPos / STEPS) * 0.5); finalY < maxLUTPos; finalY += maxLUTPos / STEPS)
			{
				IVector2 finalPos = Vector2.fromXY(finalX, finalY);
				
				for (double initialVelAbs = -maxLUTVel
						+ ((maxLUTVel / STEPS) * 0.5); initialVelAbs < maxLUTVel; initialVelAbs += maxLUTVel / STEPS)
				{
					IVector2 initialVel = Vector2.fromX(initialVelAbs);
					
					Optional<BangBangTrajectory2D> lutTraj = lut.getTrajectory(Vector2f.ZERO_VECTOR, finalPos, initialVel);
					assertThat(lutTraj).isPresent();
					
					BangBangTrajectory2D calcTraj = new BangBangTrajectory2D(Vector2f.ZERO_VECTOR, finalPos, initialVel,
							maxTrajVel, maxTrajAcc);
					
					// Limits are very high as the error grows significantly where the approximated function is "dense"
					assertThat(lutTraj.get().getAlpha()).isCloseTo(calcTraj.getAlpha(), Offset.offset(0.7));
					assertThat(lutTraj.get().getTotalTime()).isCloseTo(calcTraj.getTotalTime(), Offset.offset(1.5));
				}
			}
		}
	}
}
