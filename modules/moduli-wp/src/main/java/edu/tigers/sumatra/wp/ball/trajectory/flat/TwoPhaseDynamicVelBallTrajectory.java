/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.flat;

import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class TwoPhaseDynamicVelBallTrajectory extends ATwoPhaseBallTrajectory
{
	private final TwoPhaseDynamicVelParameters params;
	
	
	private TwoPhaseDynamicVelBallTrajectory(final IVector kickPos, final IVector kickVel, final double tSwitch,
			final double tKickToNow,
			final TwoPhaseDynamicVelParameters params)
	{
		super(kickPos, kickVel, tSwitch, params.getAccSlide(), params.getAccRoll(), tKickToNow);
		
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
	public static TwoPhaseDynamicVelBallTrajectory fromKick(final IVector2 kickPos, final IVector2 kickVel,
			final TwoPhaseDynamicVelParameters params)
	{
		double tSwitch = (kickVel.getLength2() * (params.getKSwitch() - 1)) / params.getAccSlide();
		
		return new TwoPhaseDynamicVelBallTrajectory(Vector3.from2d(kickPos, 0), kickVel, tSwitch, 0, params);
	}
	
	
	/**
	 * @param posNow
	 * @param velNow
	 * @param vSwitch
	 * @param params
	 * @return
	 */
	public static TwoPhaseDynamicVelBallTrajectory fromState(final IVector2 posNow, final IVector2 velNow,
			final double vSwitch, final TwoPhaseDynamicVelParameters params)
	{
		double tSwitch;
		double tKickToNow;
		IVector2 kickPos;
		IVector2 kickVel;
		
		if (velNow.getLength2() > vSwitch)
		{
			// ball is still in sliding phase
			double timeToSwitch = -(velNow.getLength2() - vSwitch) / params.getAccSlide();
			IVector2 acc = velNow.normalizeNew().multiply(params.getAccSlide());
			IVector2 velSwitch = velNow.addNew(acc.multiplyNew(timeToSwitch));
			
			kickVel = velSwitch.multiplyNew(1.0 / params.getKSwitch());
			double timeToKick = (kickVel.getLength2() - velNow.getLength2()) / params.getAccSlide();
			kickPos = posNow.addNew(velNow.multiplyNew(timeToKick)).add(acc.multiplyNew(0.5 * timeToKick * timeToKick));
			tKickToNow = -timeToKick;
		} else
		{
			// ball is in rolling phase
			double timeToSwitch = (vSwitch - velNow.getLength2()) / params.getAccRoll();
			IVector2 acc = velNow.normalizeNew().multiply(params.getAccRoll());
			IVector2 posSwitch = posNow.addNew(velNow.multiplyNew(timeToSwitch))
					.add(acc.multiplyNew(0.5 * timeToSwitch * timeToSwitch));
			IVector2 velSwitch = velNow.addNew(acc.multiplyNew(timeToSwitch));
			
			acc = velNow.normalizeNew().multiply(params.getAccSlide());
			kickVel = velSwitch.multiplyNew(1.0 / params.getKSwitch());
			double tSlide = (kickVel.getLength2() - velSwitch.getLength2()) / params.getAccSlide(); // negative
			kickPos = posSwitch.addNew(velSwitch.multiplyNew(tSlide)).add(acc.multiplyNew(0.5 * tSlide * tSlide));
			tKickToNow = -(timeToSwitch + tSlide);
		}
		
		tSwitch = (kickVel.getLength2() * (params.getKSwitch() - 1)) / params.getAccSlide();
		
		return new TwoPhaseDynamicVelBallTrajectory(kickPos, kickVel, tSwitch, tKickToNow, params);
	}
	
	
	@Override
	public ABallTrajectory mirrored()
	{
		IVector2 vel = kickVel.getXYVector().multiplyNew(-1);
		IVector2 pos = kickPos.getXYVector().multiplyNew(-1);
		
		return TwoPhaseDynamicVelBallTrajectory.fromKick(pos, vel, params);
	}
	
	/**
	 * Parameter class
	 */
	public static class TwoPhaseDynamicVelParameters
	{
		private final double accSlide;
		private final double accRoll;
		private final double kSwitch;
		
		
		/**
		 * Constructor with global parameters.
		 */
		public TwoPhaseDynamicVelParameters()
		{
			BallParameters ballParams = Geometry.getBallParameters();
			accSlide = ballParams.getAccSlide();
			accRoll = ballParams.getAccRoll();
			kSwitch = ballParams.getkSwitch();
		}
		
		
		/**
		 * @param accSlide
		 * @param accRoll
		 * @param kSwitch
		 */
		public TwoPhaseDynamicVelParameters(final double accSlide, final double accRoll, final double kSwitch)
		{
			this.accSlide = accSlide;
			this.accRoll = accRoll;
			this.kSwitch = kSwitch;
		}
		
		
		public double getAccSlide()
		{
			return accSlide;
		}
		
		
		public double getAccRoll()
		{
			return accRoll;
		}
		
		
		public double getKSwitch()
		{
			return kSwitch;
		}
	}
}
