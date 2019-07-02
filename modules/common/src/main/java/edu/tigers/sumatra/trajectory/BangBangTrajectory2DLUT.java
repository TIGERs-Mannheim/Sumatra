/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.trajectory;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BangBangTrajectory2DLUT
{
	private static final int POS_SAMPLES = 100;
	private static final int VEL_SAMPLES = 100;
	private static final int NUM_SAMPLES = POS_SAMPLES * POS_SAMPLES * VEL_SAMPLES;
	
	private static final int POS_X_STRIDE = 1;
	private static final int POS_Y_STRIDE = POS_SAMPLES;
	private static final int VEL_STRIDE = POS_SAMPLES * POS_SAMPLES;
	
	private final double maxLUTPos;
	private final double maxLUTVel;
	
	private final double maxTrajVel;
	private final double maxTrajAcc;
	
	private final double stepVel;
	private final double stepPosX;
	private final double stepPosY;
	
	private final double[] samples = new double[NUM_SAMPLES];
	
	
	public BangBangTrajectory2DLUT(final double maxLUTPos, final double maxLUTVel, final double maxTrajVel,
			final double maxTrajAcc)
	{
		this.maxLUTPos = maxLUTPos;
		this.maxLUTVel = maxLUTVel;
		this.maxTrajVel = maxTrajVel;
		this.maxTrajAcc = maxTrajAcc;
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D();
		
		stepVel = maxLUTVel / (VEL_SAMPLES - 1);
		stepPosX = (2.0 * maxLUTPos) / (POS_SAMPLES - 1);
		stepPosY = maxLUTPos / (POS_SAMPLES - 1);
		
		double minPos = -maxLUTPos;
		
		for (int velIndex = 0; velIndex < VEL_SAMPLES; velIndex++)
		{
			double velX = velIndex * stepVel;
			for (int posXIndex = 0; posXIndex < POS_SAMPLES; posXIndex++)
			{
				double posX = minPos + (posXIndex * stepPosX);
				for (int posYIndex = 0; posYIndex < POS_SAMPLES; posYIndex++)
				{
					double posY = posYIndex * stepPosY;
					traj.updateTrajectory(Vector2f.ZERO_VECTOR, Vector2.fromXY(posX, posY),
							Vector2.fromX(velX), maxTrajVel, maxTrajAcc);
					samples[(POS_X_STRIDE * posXIndex) + (POS_Y_STRIDE * posYIndex) + (VEL_STRIDE * velIndex)] = traj
							.getAlpha();
				}
			}
		}
	}
	
	
	/**
	 * Test function.
	 * 
	 * @param index
	 * @return
	 */
	public IVector3 indexToValue(final IVector3 index)
	{
		double posX = -maxLUTPos + (index.x() * stepPosX);
		double posY = index.y() * stepPosY;
		double vel = index.z() * stepVel;
		
		return Vector3.fromXYZ(posX, posY, vel);
	}
	
	
	/**
	 * Test function.
	 * 
	 * @param value
	 * @return
	 */
	public IVector3 valueToIndex(final IVector3 value)
	{
		double posXIndexF = (value.x() + maxLUTPos) / stepPosX;
		double posYIndexF = value.y() / stepPosY;
		double velIndexF = value.z() / stepVel;
		
		return Vector3.fromXYZ(posXIndexF, posYIndexF, velIndexF);
	}
	
	
	/**
	 * @param initialPos
	 * @param finalPos
	 * @param initialVel
	 * @return
	 */
	public Optional<BangBangTrajectory2D> getTrajectory(final IVector2 initialPos,
			final IVector2 finalPos,
			final IVector2 initialVel)
	{
		Optional<Double> alpha = getAlpha(initialPos, finalPos, initialVel);
		
		if (alpha.isPresent())
		{
			return Optional
					.of(new BangBangTrajectory2D(initialPos, finalPos, initialVel, maxTrajVel, maxTrajAcc, alpha.get()));
		}
		
		return Optional.empty();
	}
	
	
	public Optional<Double> getAlpha(final IVector2 initialPos,
			final IVector2 finalPos,
			final IVector2 initialVel)
	{
		// move initial position to (0,0) and rotate to align initial velocity along X axis
		double angle = initialVel.getAngle(0.0);
		
		IVector2 destination = finalPos.subtractNew(initialPos).turn(-angle);
		
		// get values for lookup
		double posX = destination.x();
		double posY = Math.abs(destination.y());
		double vel = initialVel.turnNew(-angle).x();
		
		boolean posXInvalid = (posX > maxLUTPos) || (posX < -maxLUTPos);
		boolean posYInvalid = (posY > maxLUTPos) || (posY < 0);
		boolean velInvalid = (vel > maxLUTVel) || (vel < 0);
		
		if (posXInvalid || posYInvalid || velInvalid)
		{
			return Optional.empty();
		}
		
		// compute indices
		double posXIndexF = (posX + maxLUTPos) / stepPosX;
		double posYIndexF = posY / stepPosY;
		double velIndexF = vel / stepVel;
		
		// perform trilinear interpolation
		int posXIndexFloor = (int) posXIndexF;
		int posXIndexCeil = posXIndexFloor + 1;
		double posXCeilFactor = posXIndexF - posXIndexFloor;
		
		int posYIndexFloor = (int) posYIndexF;
		int posYIndexCeil = posYIndexFloor + 1;
		double posYCeilFactor = posYIndexF - posYIndexFloor;
		
		int velIndexFloor = (int) velIndexF;
		int velIndexCeil = velIndexFloor + 1;
		double velCeilFactor = velIndexF - velIndexFloor;
		
		double c000 = samples[(POS_X_STRIDE * posXIndexFloor) + (POS_Y_STRIDE * posYIndexFloor)
				+ (VEL_STRIDE * velIndexFloor)];
		double c001 = samples[(POS_X_STRIDE * posXIndexFloor) + (POS_Y_STRIDE * posYIndexFloor)
				+ (VEL_STRIDE * velIndexCeil)];
		double c010 = samples[(POS_X_STRIDE * posXIndexFloor) + (POS_Y_STRIDE * posYIndexCeil)
				+ (VEL_STRIDE * velIndexFloor)];
		double c011 = samples[(POS_X_STRIDE * posXIndexFloor) + (POS_Y_STRIDE * posYIndexCeil)
				+ (VEL_STRIDE * velIndexCeil)];
		double c100 = samples[(POS_X_STRIDE * posXIndexCeil) + (POS_Y_STRIDE * posYIndexFloor)
				+ (VEL_STRIDE * velIndexFloor)];
		double c101 = samples[(POS_X_STRIDE * posXIndexCeil) + (POS_Y_STRIDE * posYIndexFloor)
				+ (VEL_STRIDE * velIndexCeil)];
		double c110 = samples[(POS_X_STRIDE * posXIndexCeil) + (POS_Y_STRIDE * posYIndexCeil)
				+ (VEL_STRIDE * velIndexFloor)];
		double c111 = samples[(POS_X_STRIDE * posXIndexCeil) + (POS_Y_STRIDE * posYIndexCeil)
				+ (VEL_STRIDE * velIndexCeil)];
		
		double c00 = (c000 * (1.0 - posXCeilFactor)) + (c100 * posXCeilFactor);
		double c01 = (c001 * (1.0 - posXCeilFactor)) + (c101 * posXCeilFactor);
		double c10 = (c010 * (1.0 - posXCeilFactor)) + (c110 * posXCeilFactor);
		double c11 = (c011 * (1.0 - posXCeilFactor)) + (c111 * posXCeilFactor);
		
		double c0 = (c00 * (1.0 - posYCeilFactor)) + (c10 * posYCeilFactor);
		double c1 = (c01 * (1.0 - posYCeilFactor)) + (c11 * posYCeilFactor);
		
		return Optional.of((c0 * (1.0 - velCeilFactor)) + (c1 * velCeilFactor));
	}
	
	
	/**
	 * @return the stepVel
	 */
	public double getStepVel()
	{
		return stepVel;
	}
	
	
	/**
	 * @return the stepPos
	 */
	public double getStepPosX()
	{
		return stepPosX;
	}
	
	
	public double getStepPosY()
	{
		return stepPosY;
	}
	
}
