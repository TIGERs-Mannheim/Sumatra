/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class StraightBallTrajectory extends ABallTrajectory
{
	private final long timeSwitch;
	private final IVector3 posSwitch;
	private final IVector3 velSwitch;
	private final BallParameters ballParams;
	
	
	/**
	 * @param timestampNow
	 * @param posNow position in [mm]
	 * @param velNow velocity in [mm/s]
	 * @param switchTimestamp
	 */
	public StraightBallTrajectory(final long timestampNow, final IVector3 posNow, final IVector3 velNow,
			final long switchTimestamp)
	{
		super(timestampNow);
		ballParams = Geometry.getBallParameters();
		
		if (switchTimestamp == 0)
		{
			timeSwitch = timestampNow;
		} else
		{
			timeSwitch = switchTimestamp;
		}
		
		double timeToSwitch = (timeSwitch - timestampNow) * 1e-9;
		
		if (timestampNow < timeSwitch)
		{
			// ball is still in sliding phase
			IVector3 acc = velNow.normalizeNew().multiply(ballParams.getAccSlide());
			posSwitch = posNow.addNew(velNow.multiplyNew(timeToSwitch))
					.add(acc.multiplyNew(0.5 * timeToSwitch * timeToSwitch));
			velSwitch = velNow.addNew(acc.multiplyNew(timeToSwitch));
			
			kickVel = velSwitch.multiplyNew(1.0 / ballParams.getkSwitch());
			double timeToKick = (kickVel.getLength2() - velNow.getLength2()) / ballParams.getAccSlide();
			kickPos = posNow.addNew(velNow.multiplyNew(timeToKick)).add(acc.multiplyNew(0.5 * timeToKick * timeToKick));
			kickTimestamp = timestampNow + (long) (timeToKick * 1e9);
		} else
		{
			// ball is in rolling phase
			IVector3 acc = velNow.normalizeNew().multiply(ballParams.getAccRoll());
			posSwitch = posNow.addNew(velNow.multiplyNew(timeToSwitch))
					.add(acc.multiplyNew(0.5 * timeToSwitch * timeToSwitch));
			velSwitch = velNow.addNew(acc.multiplyNew(timeToSwitch));
			
			acc = velNow.normalizeNew().multiply(ballParams.getAccSlide());
			kickVel = velSwitch.multiplyNew(1.0 / ballParams.getkSwitch());
			double tSlide = (kickVel.getLength2() - velSwitch.getLength2()) / ballParams.getAccSlide(); // negative
			kickPos = posSwitch.addNew(velSwitch.multiplyNew(tSlide)).add(acc.multiplyNew(0.5 * tSlide * tSlide));
			kickTimestamp = timestampNow + (long) ((timeToSwitch + tSlide) * 1e9);
		}
	}
	
	
	/**
	 * Create a straight ball trajectory from a ball where kick position/velocity is known.
	 * 
	 * @param kickPos position in [mm]
	 * @param kickVel velocity in [mm/s]
	 * @param kickTimestamp
	 */
	public StraightBallTrajectory(final IVector2 kickPos, final IVector3 kickVel, final long kickTimestamp)
	{
		super(kickTimestamp);
		ballParams = Geometry.getBallParameters();
		
		this.kickPos = Vector3.from2d(kickPos, 0);
		this.kickVel = kickVel;
		this.kickTimestamp = kickTimestamp;
		
		double tSwitch = (kickVel.getLength2() * (ballParams.getkSwitch() - 1)) / ballParams.getAccSlide();
		timeSwitch = kickTimestamp + (long) (tSwitch * 1e9);
		IVector3 acc = kickVel.normalizeNew().multiply(ballParams.getAccSlide());
		posSwitch = this.kickPos.addNew(kickVel.multiplyNew(tSwitch)).add(acc.multiplyNew(0.5 * tSwitch * tSwitch));
		velSwitch = kickVel.addNew(acc.multiplyNew(tSwitch));
	}
	
	
	@Override
	public FilteredVisionBall getStateAtTimestamp(final long timestamp)
	{
		double tSwitch = (kickVel.getLength2() * (ballParams.getkSwitch() - 1)) / ballParams.getAccSlide();
		double vSwitch = kickVel.getLength2() * ballParams.getkSwitch();
		
		double tQuery = (timestamp - kickTimestamp) * 1e-9;
		if (tQuery < 0)
		{
			return FilteredVisionBall.Builder.create()
					.withPos(kickPos)
					.withVel(Vector3f.ZERO_VECTOR)
					.withAcc(Vector3f.ZERO_VECTOR)
					.withIsChipped(false)
					.withvSwitch(vSwitch)
					.withSpin(1.0)
					.build();
		}
		
		
		if (tQuery < tSwitch)
		{
			IVector3 accNow = kickVel.normalizeNew().multiply(ballParams.getAccSlide());
			IVector3 posNow = kickPos.addNew(kickVel.multiplyNew(tQuery)).add(accNow.multiplyNew(0.5 * tQuery * tQuery));
			IVector3 velNow = kickVel.addNew(accNow.multiplyNew(tQuery));
			
			return FilteredVisionBall.Builder.create()
					.withPos(posNow)
					.withVel(velNow)
					.withAcc(accNow)
					.withIsChipped(false)
					.withvSwitch(vSwitch)
					.withSpin(1.0)
					.build();
		}
		
		tQuery -= tSwitch;
		
		IVector3 acc = kickVel.normalizeNew().multiply(ballParams.getAccRoll());
		IVector3 posNow = posSwitch.addNew(velSwitch.multiplyNew(tQuery)).add(acc.multiplyNew(0.5 * tQuery * tQuery));
		IVector3 velNow = velSwitch.addNew(acc.multiplyNew(tQuery));
		
		return FilteredVisionBall.Builder.create()
				.withPos(posNow)
				.withVel(velNow)
				.withAcc(acc)
				.withIsChipped(false)
				.withvSwitch(vSwitch)
				.withSpin(1.0)
				.build();
	}
	
	
	public IVector3 getVelSwitch()
	{
		return velSwitch;
	}
}
