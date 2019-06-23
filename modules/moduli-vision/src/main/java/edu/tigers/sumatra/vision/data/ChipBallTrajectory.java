/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR
 */
public class ChipBallTrajectory extends ABallTrajectory
{
	/**
	 * Create new chipped ball trajectory from a ball that is already in the air.
	 * 
	 * @param timestampNow
	 * @param p position in [mm]
	 * @param v velocity in [mm/s]
	 */
	public ChipBallTrajectory(final long timestampNow, final IVector3 p, final IVector3 v)
	{
		super(timestampNow);
		
		double pz1 = p.z();
		double vz1 = v.z();
		double g = 9810;
		
		IVector3 a = Vector3.fromXYZ(0, 0, -g);
		
		double tInAir = -(Math.sqrt((vz1 * vz1) + (2.0 * g * pz1)) - vz1) / g;
		double vKickZ = Math.sqrt((vz1 * vz1) + (2.0 * g * pz1));
		
		kickPos = p.addNew(v.multiplyNew(tInAir)).addNew(a.multiplyNew(0.5 * tInAir * tInAir));
		kickVel = Vector3.from2d(v.getXYVector(), vKickZ);
		kickTimestamp = timestampNow + (long) (tInAir * 1e9);
	}
	
	
	/**
	 * Create new chipped ball trajectory from a ball that is already in the air.
	 * 
	 * @param timestampNow
	 * @param ball
	 */
	public ChipBallTrajectory(final long timestampNow, final FilteredVisionBall ball)
	{
		this(timestampNow, ball.getPos(), ball.getVel());
	}
	
	
	/**
	 * Create a chipped ball trajectory from a ball where kick position/velocity is known.
	 * 
	 * @param kickPos kick position in [mm]
	 * @param kickVel kick velocity in [mm/s]
	 * @param kickTimestamp
	 */
	public ChipBallTrajectory(final IVector2 kickPos, final IVector3 kickVel, final long kickTimestamp)
	{
		super(kickTimestamp);
		
		this.kickPos = Vector3.from2d(kickPos, 0);
		this.kickVel = kickVel;
		this.kickTimestamp = kickTimestamp;
	}
	
	
	@Override
	public FilteredVisionBall getStateAtTimestamp(final long timestamp)
	{
		BallParameters ballParams = Geometry.getBallParameters();
		Vector3 accNow = Vector3.fromXYZ(0, 0, -9810);
		
		double tQuery = (timestamp - kickTimestamp) * 1e-9;
		if (tQuery < 0)
		{
			return FilteredVisionBall.Builder.create()
					.withPos(kickPos)
					.withVel(kickVel)
					.withAcc(accNow)
					.withIsChipped(true)
					.build();
		}
		
		Vector3 posNow = Vector3.copy(kickPos);
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = 0;
		
		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * 9810)) > ballParams.getMinHopHeight())
		{
			double tFly = (2 * velNow.z()) / 9810;
			
			if ((tNow + tFly) > tQuery)
			{
				double t = tQuery - tNow;
				posNow.add(velNow.multiplyNew(t)).add(Vector3.fromXYZ(0, 0, -0.5 * 9810 * t * t));
				velNow.add(Vector3.fromXYZ(0, 0, -9810 * t));
				
				return FilteredVisionBall.Builder.create()
						.withPos(posNow)
						.withVel(velNow)
						.withAcc(accNow)
						.withIsChipped(true)
						.build();
			}
			
			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(Vector3.fromXYZ(ballParams.getChipDampingXY(),
					ballParams.getChipDampingXY(), ballParams.getChipDampingZ()));
			tNow += tFly;
		}
		
		velNow.set(2, 0);
		
		// ball is below 10mm and assumed to be rolling
		double t = tQuery - tNow;
		double tStop = -velNow.getLength2() / ballParams.getAccRoll();
		if (t > tStop)
		{
			t = tStop;
		}
		
		accNow = velNow.normalizeNew().multiply(ballParams.getAccRoll());
		posNow.add(velNow.multiplyNew(t))
				.add(velNow.normalizeNew().multiply(0.5 * ballParams.getAccRoll() * t * t));
		velNow.add(velNow.normalizeNew().multiply(ballParams.getAccRoll() * t));
		
		return FilteredVisionBall.Builder.create()
				.withPos(posNow)
				.withVel(velNow)
				.withAcc(accNow)
				.withIsChipped(false)
				.build();
	}
}
