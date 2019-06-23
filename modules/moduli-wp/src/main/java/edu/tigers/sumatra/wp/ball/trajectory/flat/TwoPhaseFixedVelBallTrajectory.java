/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.flat;

import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class TwoPhaseFixedVelBallTrajectory extends ATwoPhaseBallTrajectory
{
	private final TwoPhaseFixedVelParameters params;
	
	
	private TwoPhaseFixedVelBallTrajectory(final IVector2 kickPos, final IVector2 kickVel, final double tSwitch,
			final TwoPhaseFixedVelParameters params)
	{
		super(kickPos, kickVel, tSwitch, params.getAccSlide(), params.getAccRoll(), 0);
		
		this.params = params;
	}
	
	
	/**
	 * Create a straight ball trajectory from a ball where kick position/velocity is known.
	 * 
	 * @param kickPos position in [mm]
	 * @param kickVel velocity in [mm/s]
	 * @param params
	 * @return
	 */
	public static TwoPhaseFixedVelBallTrajectory fromKick(final IVector2 kickPos, final IVector2 kickVel,
			final TwoPhaseFixedVelParameters params)
	{
		double tSwitch = -(kickVel.getLength2() - params.getVSwitch()) / params.getAccSlide();
		if (tSwitch < 0)
		{
			tSwitch = 0;
		}
		
		return new TwoPhaseFixedVelBallTrajectory(kickPos, kickVel, tSwitch, params);
	}
	
	
	/**
	 * @param posNow
	 * @param velNow
	 * @param params
	 * @return
	 */
	public static TwoPhaseFixedVelBallTrajectory fromState(final IVector2 posNow, final IVector2 velNow,
			final TwoPhaseFixedVelParameters params)
	{
		double tSwitch = 0;
		
		if (velNow.getLength2() > params.vSwitch)
		{
			// ball is still in sliding phase
			tSwitch = -(velNow.getLength2() - params.vSwitch) / params.getAccSlide();
		}
		
		return new TwoPhaseFixedVelBallTrajectory(posNow, velNow, tSwitch, params);
	}
	
	
	@Override
	public ABallTrajectory mirrored()
	{
		IVector2 vel = kickVel.getXYVector().multiplyNew(-1);
		IVector2 pos = kickPos.getXYVector().multiplyNew(-1);
		
		return TwoPhaseFixedVelBallTrajectory.fromKick(pos, vel, params);
	}
	
	/**
	 * Parameter class
	 */
	public static class TwoPhaseFixedVelParameters
	{
		private final double	accSlide;
		private final double	accRoll;
		private final double	vSwitch;
		
		
		/**
		 * Constructor with global parameters.
		 */
		public TwoPhaseFixedVelParameters()
		{
			BallParameters ballParams = Geometry.getBallParameters();
			accSlide = ballParams.getAccSlide();
			accRoll = ballParams.getAccRoll();
			vSwitch = ballParams.getvSwitch();
		}
		
		
		/**
		 * @param accSlide
		 * @param accRoll
		 * @param vSwitch
		 */
		public TwoPhaseFixedVelParameters(final double accSlide, final double accRoll, final double vSwitch)
		{
			this.accSlide = accSlide;
			this.accRoll = accRoll;
			this.vSwitch = vSwitch;
		}
		
		
		public double getAccSlide()
		{
			return accSlide;
		}
		
		
		public double getAccRoll()
		{
			return accRoll;
		}
		
		
		public double getVSwitch()
		{
			return vSwitch;
		}
	}
	
}
