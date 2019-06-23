/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR
 */
public class ChipBallTrajectory extends ABallTrajectory
{
	private final double dampingXYFirstHop;
	private final double dampingXYOtherHops;
	private final double dampingZ;
	private final double accRoll;
	private final double initialSpin;
	
	
	/**
	 * Create new chipped ball trajectory from a ball that is already in the air.
	 * 
	 * @param timestampNow
	 * @param p position in [mm]
	 * @param v velocity in [mm/s]
	 * @param spin
	 */
	public ChipBallTrajectory(final long timestampNow, final IVector3 p, final IVector3 v, final double spin)
	{
		super(timestampNow);
		
		BallParameters ballParams = Geometry.getBallParameters();
		dampingXYFirstHop = ballParams.getChipDampingXYFirstHop();
		dampingXYOtherHops = ballParams.getChipDampingXYOtherHops();
		dampingZ = ballParams.getChipDampingZ();
		accRoll = ballParams.getAccRoll();
		initialSpin = spin;
		
		double pz1 = p.z();
		double vz1 = v.z();
		double g = 9810;
		
		IVector3 a = Vector3.fromXYZ(0, 0, -g);
		
		double tInAir = -(SumatraMath.sqrt((vz1 * vz1) + (2.0 * g * pz1)) - vz1) / g;
		double vKickZ = SumatraMath.sqrt((vz1 * vz1) + (2.0 * g * pz1));
		
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
		this(timestampNow, ball.getPos(), ball.getVel(), ball.getSpin());
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
		
		BallParameters ballParams = Geometry.getBallParameters();
		dampingXYFirstHop = ballParams.getChipDampingXYFirstHop();
		dampingXYOtherHops = ballParams.getChipDampingXYOtherHops();
		dampingZ = ballParams.getChipDampingZ();
		accRoll = ballParams.getAccRoll();
		initialSpin = 0;
		
		this.kickPos = Vector3.from2d(kickPos, 0);
		this.kickVel = kickVel;
		this.kickTimestamp = kickTimestamp;
	}
	
	
	/**
	 * Copy constructor with new accRoll.
	 * 
	 * @param original
	 * @param accRoll
	 */
	public ChipBallTrajectory(final ChipBallTrajectory original, final double accRoll)
	{
		this(original.getKickPos(), original.getKickVel(), original.getKickTimestamp(), original.getDampingXY(),
				original.getDampingZ(), accRoll);
	}
	
	
	/**
	 * Create a chipped ball trajectory from a ball where kick position/velocity is known.
	 * 
	 * @param kickPos kick position in [mm]
	 * @param kickVel kick velocity in [mm/s]
	 * @param kickTimestamp
	 * @param dampingXY
	 * @param dampingZ
	 */
	public ChipBallTrajectory(final IVector2 kickPos, final IVector3 kickVel, final long kickTimestamp,
			final double dampingXY, final double dampingZ)
	{
		super(kickTimestamp);
		
		BallParameters ballParams = Geometry.getBallParameters();
		dampingXYFirstHop = dampingXY;
		dampingXYOtherHops = ballParams.getChipDampingXYOtherHops();
		this.dampingZ = dampingZ;
		accRoll = Geometry.getBallParameters().getAccRoll();
		initialSpin = 0;
		
		this.kickPos = Vector3.from2d(kickPos, 0);
		this.kickVel = kickVel;
		this.kickTimestamp = kickTimestamp;
	}
	
	
	/**
	 * Create a chipped ball trajectory from a ball where kick position/velocity is known.
	 * 
	 * @param kickPos kick position in [mm]
	 * @param kickVel kick velocity in [mm/s]
	 * @param kickTimestamp
	 * @param dampingXY
	 * @param dampingZ
	 * @param accRoll
	 */
	public ChipBallTrajectory(final IVector2 kickPos, final IVector3 kickVel, final long kickTimestamp,
			final double dampingXY, final double dampingZ, final double accRoll)
	{
		super(kickTimestamp);
		
		BallParameters ballParams = Geometry.getBallParameters();
		dampingXYFirstHop = dampingXY;
		dampingXYOtherHops = ballParams.getChipDampingXYOtherHops();
		this.dampingZ = dampingZ;
		this.accRoll = accRoll;
		initialSpin = 0;
		
		this.kickPos = Vector3.from2d(kickPos, 0);
		this.kickVel = kickVel;
		this.kickTimestamp = kickTimestamp;
	}
	
	
	/**
	 * Create a chipped ball trajectory from a ball where kick position/velocity is known.
	 * 
	 * @param kickPos kick position in [mm]
	 * @param kickVel kick velocity in [mm/s]
	 * @param kickTimestamp
	 * @param dampingXYFirst
	 * @param dampingXYOthers
	 * @param dampingZ
	 * @param accRoll
	 */
	public ChipBallTrajectory(final IVector2 kickPos, final IVector3 kickVel, final long kickTimestamp,
			final double dampingXYFirst, final double dampingXYOthers, final double dampingZ, final double accRoll)
	{
		super(kickTimestamp);
		
		dampingXYFirstHop = dampingXYFirst;
		dampingXYOtherHops = dampingXYOthers;
		this.dampingZ = dampingZ;
		this.accRoll = accRoll;
		initialSpin = 0;
		
		this.kickPos = Vector3.from2d(kickPos, 0);
		this.kickVel = kickVel;
		this.kickTimestamp = kickTimestamp;
	}
	
	
	@Override
	public FilteredVisionBall getStateAtTimestamp(final long timestamp)
	{
		Vector3 accNow = Vector3.fromXYZ(0, 0, -9810);
		
		double tQuery = (timestamp - kickTimestamp) * 1e-9;
		if (tQuery < 0)
		{
			return FilteredVisionBall.Builder.create()
					.withPos(kickPos)
					.withVel(kickVel)
					.withAcc(accNow)
					.withIsChipped(true)
					.withSpin(initialSpin)
					.build();
		}
		
		Vector3 posNow = Vector3.copy(kickPos);
		Vector3 velNow = Vector3.copy(kickVel);
		double tNow = 0;
		
		double minHopHeight = Geometry.getBallParameters().getMinHopHeight();
		
		double realDampingXY = dampingXYFirstHop;
		if (initialSpin > 0)
		{
			realDampingXY = dampingXYOtherHops;
		}
		
		double spin = initialSpin;
		
		// go through hops while max. height is above 10mm
		while (((velNow.z() * velNow.z()) / (2.0 * 9810)) > minHopHeight)
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
						.withSpin(spin)
						.build();
			}
			
			posNow.add(velNow.multiplyNew(tFly));
			posNow.set(2, 0);
			velNow = velNow.multiplyNew(Vector3.fromXYZ(realDampingXY,
					realDampingXY, dampingZ));
			tNow += tFly;
			
			// reduce damping for all subsequent hops
			realDampingXY = dampingXYOtherHops;
			
			// and assume we have some forward spin
			spin = 1.0;
		}
		
		velNow.set(2, 0);
		
		// ball is below 10mm and assumed to be rolling
		double t = tQuery - tNow;
		double tStop = -velNow.getLength2() / accRoll;
		if (t > tStop)
		{
			t = tStop;
		}
		
		accNow = velNow.normalizeNew().multiply(accRoll);
		posNow.add(velNow.multiplyNew(t))
				.add(velNow.normalizeNew().multiply(0.5 * accRoll * t * t));
		velNow.add(velNow.normalizeNew().multiply(accRoll * t));
		
		return FilteredVisionBall.Builder.create()
				.withPos(posNow)
				.withVel(velNow)
				.withAcc(accNow)
				.withIsChipped(false)
				.withSpin(spin)
				.build();
	}
	
	
	public IVector2 getFirstTouchdownLocation()
	{
		double tFly = (2 * kickVel.z()) / 9810;
		
		return kickPos.getXYVector().addNew(kickVel.getXYVector().multiplyNew(tFly));
	}
	
	
	public IVector3 getVelocityAfterFirstTouchdown()
	{
		return Vector3.from2d(kickVel.getXYVector().multiplyNew(dampingXYFirstHop), kickVel.z() * dampingZ);
	}
	
	
	public long getFirstTouchdownTimestamp()
	{
		double tFly = (2 * kickVel.z()) / 9810;
		
		return kickTimestamp + (long) (tFly * 1e9);
	}
	
	
	/**
	 * @return the dampingXY
	 */
	public double getDampingXY()
	{
		return dampingXYFirstHop;
	}
	
	
	/**
	 * @return the dampingZ
	 */
	public double getDampingZ()
	{
		return dampingZ;
	}
	
	
	/**
	 * @return the accRoll
	 */
	public double getAccRoll()
	{
		return accRoll;
	}
}
