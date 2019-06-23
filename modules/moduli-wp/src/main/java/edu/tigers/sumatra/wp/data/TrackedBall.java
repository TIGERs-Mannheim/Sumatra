/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;


/**
 * Simple data holder describing balls that are recognized and tracked by the
 * {@link edu.tigers.sumatra.wp.AWorldPredictor}
 * 
 * @see ATrackedObject
 * @author Gero
 */
@Persistent
public class TrackedBall extends ATrackedObject
{
	private final BallID		id				= new BallID();
	
	/** mm */
	private final IVector2	pos;
	/** m/s */
	private final IVector2	vel;
	
	private final IVector3	acc;
	
	private final double		height;
	private final double		zVel;
	
	private double				confidence	= 0;
	private boolean			onCam			= true;
	
	
	@SuppressWarnings("unused")
	private TrackedBall()
	{
		super();
		pos = AVector2.ZERO_VECTOR;
		vel = AVector2.ZERO_VECTOR;
		acc = AVector3.ZERO_VECTOR;
		height = 0;
		zVel = 0;
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	public TrackedBall(final TrackedBall original)
	{
		super(original);
		pos = new Vector2(original.pos);
		vel = new Vector2(original.vel);
		acc = new Vector3(original.acc);
		height = original.height;
		zVel = original.zVel;
	}
	
	
	/**
	 * @param pos
	 * @param height
	 * @param vel
	 * @param zVel
	 * @param acc
	 */
	public TrackedBall(final IVector2 pos, final double height, final IVector2 vel, final double zVel,
			final IVector3 acc)
	{
		super();
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
		this.height = height;
		this.zVel = zVel;
	}
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 */
	public TrackedBall(final IVector3 pos, final IVector3 vel, final IVector3 acc)
	{
		this.pos = pos.getXYVector();
		this.vel = vel.getXYVector();
		this.acc = acc;
		height = pos.z();
		zVel = vel.z();
	}
	
	
	/**
	 * @return
	 */
	public static TrackedBall defaultInstance()
	{
		return new TrackedBall(AVector2.ZERO_VECTOR, 0, AVector2.ZERO_VECTOR, 0, AVector3.ZERO_VECTOR);
	}
	
	
	/**
	 * Mirror position, velocity and acceleration over x and y axis.
	 * 
	 * @return
	 */
	public TrackedBall mirrorNew()
	{
		return new TrackedBall(pos.multiplyNew(-1), height, vel.multiplyNew(-1), zVel, new Vector3(acc.getXYVector()
				.multiplyNew(-1), acc.z()));
	}
	
	
	/**
	 * gets the theoretical position of the ball after a given time
	 * 
	 * @param time [s]
	 * @return the position
	 */
	public IVector2 getPosByTime(final double time)
	{
		return Geometry.getBallModel().getPosByTime(getPos(), getVel(), time);
	}
	
	
	/**
	 * gets the theoretical position of the ball when it reaches a given velocity
	 * 
	 * @param velocity [m/s]
	 * @return the position
	 */
	public IVector2 getPosByVel(final double velocity)
	{
		return Geometry.getBallModel().getPosByVel(getPos(), getVel(), velocity);
	}
	
	
	/**
	 * gets the theoretical needed time for the ball to reach pos,
	 * pos should be on the balls movementPath.
	 * 
	 * @param pos
	 * @return the time [s]
	 */
	public double getTimeByPos(final IVector2 pos)
	{
		return Geometry.getBallModel().getTimeByDist(getVel().getLength2(), GeoMath.distancePP(pos, getPos()));
	}
	
	
	/**
	 * gets the theoretical time where the ball reaches a given velocity
	 * 
	 * @param velocity [m/s]
	 * @return the time [s]
	 */
	public double getTimeByVel(final double velocity)
	{
		return Geometry.getBallModel().getTimeByVel(getVel().getLength2(), velocity);
	}
	
	
	/**
	 * gets the theoretical velocity of the ball at a given position
	 * 
	 * @param pos
	 * @return the velocity [m/s]
	 */
	public double getVelByPos(final IVector2 pos)
	{
		return Geometry.getBallModel().getVelByDist(getVel().getLength2(), GeoMath.distancePP(pos, getPos()));
	}
	
	
	/**
	 * gets the theoretical velocity of the ball after a given time
	 * 
	 * @param time [s]
	 * @return the velocity [m/s]
	 */
	public double getVelByTime(final double time)
	{
		return Geometry.getBallModel().getVelByTime(getVel().getLength2(), time);
	}
	
	
	@Override
	public BallID getBotId()
	{
		return id;
	}
	
	
	@Override
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	@Override
	public IVector2 getAcc()
	{
		return acc.getXYVector();
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getPos3()
	{
		return new Vector3(pos, height);
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getVel3()
	{
		return new Vector3(vel, zVel);
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getAcc3()
	{
		return acc;
	}
	
	
	/**
	 * @return the confidence
	 */
	public double getConfidence()
	{
		return confidence;
	}
	
	
	/**
	 * @param confidence the confidence to set
	 */
	public void setConfidence(final double confidence)
	{
		this.confidence = confidence;
	}
	
	
	/**
	 * @return the onCam
	 */
	public boolean isOnCam()
	{
		return onCam;
	}
	
	
	/**
	 * @param onCam the onCam to set
	 */
	public void setOnCam(final boolean onCam)
	{
		this.onCam = onCam;
	}
}
