/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BallID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.BallMotionResult;


/**
 * Simple data holder describing balls that are recognized and tracked by the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see ATrackedObject
 * @author Gero
 */
@Persistent
public class TrackedBall extends ATrackedObject
{
	/** mm */
	private Vector3	pos;
	/** m/s */
	private Vector3	vel;
	/** m/s^2 */
	private Vector3	acc;
	
	/** not final for ObjectDB */
	private boolean	onCam;
	
	
	@SuppressWarnings("unused")
	private TrackedBall()
	{
		super(new BallID(), 0);
	}
	
	
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * 
	 * @param original
	 */
	public TrackedBall(final TrackedBall original)
	{
		super(original);
		onCam = original.onCam;
		pos = new Vector3(original.pos);
		vel = new Vector3(original.vel);
		acc = new Vector3(original.acc);
	}
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param confidence
	 * @param onCam
	 */
	public TrackedBall(final IVector3 pos, final IVector3 vel, final IVector3 acc, final float confidence,
			final boolean onCam)
	{
		super(new BallID(), confidence);
		this.pos = new Vector3(pos);
		this.vel = new Vector3(vel);
		this.acc = new Vector3(acc);
		this.onCam = onCam;
	}
	
	
	/**
	 * @param motion
	 * @return
	 */
	public static TrackedBall motionToTrackedBall(final BallMotionResult motion)
	{
		final float xPos = (float) (motion.x / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT);
		final float xVel = (float) (motion.vx / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		final float xAcc = (float) (motion.ax / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A);
		final float yPos = (float) (motion.y / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT);
		final float yVel = (float) (motion.vy / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		final float yAcc = (float) (motion.ay / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A);
		final float zPos = (float) (motion.z / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT);
		final float zVel = (float) (motion.vz / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		final float zAcc = (float) (motion.az / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A);
		final float confidence = (float) motion.confidence;
		final boolean isOnCam = motion.onCam;
		
		IVector3 pos = new Vector3f(xPos, yPos, zPos);
		IVector3 vel = new Vector3f(xVel, yVel, zVel);
		IVector3 acc = new Vector3f(xAcc, yAcc, zAcc);
		return new TrackedBall(pos, vel, acc, confidence, isOnCam);
	}
	
	
	/**
	 * Mirror position, velocity and acceleration over x and y axis.
	 * 
	 * @return
	 */
	public TrackedBall mirror()
	{
		TrackedBall tb = new TrackedBall(this);
		tb.pos.set(new Vector3(-pos.x(), -pos.y(), pos.z()));
		tb.vel.set(new Vector3(-vel.x(), -vel.y(), vel.z()));
		tb.acc.set(new Vector3(-acc.x(), -acc.y(), acc.z()));
		return tb;
	}
	
	
	/**
	 * gets the theoretical position of the ball after a given time
	 * 
	 * @param time [s]
	 * @return the position
	 */
	public IVector2 getPosByTime(final float time)
	{
		return AIConfig.getBallModel().getPosByTime(getPos(), getVel(), time);
	}
	
	
	/**
	 * gets the theoretical position of the ball when it reaches a given velocity
	 * 
	 * @param velocity [m/s]
	 * @return the position
	 */
	public IVector2 getPosByVel(final float velocity)
	{
		return AIConfig.getBallModel().getPosByVel(getPos(), getVel(), velocity);
	}
	
	
	/**
	 * gets the theoretical needed time for the ball to reach pos,
	 * pos should be on the balls movementPath.
	 * 
	 * @param pos
	 * @return the time [s]
	 */
	public float getTimeByPos(final IVector2 pos)
	{
		return AIConfig.getBallModel().getTimeByDist(getVel().getLength2(), GeoMath.distancePP(pos, getPos()));
	}
	
	
	/**
	 * gets the theoretical time where the ball reaches a given velocity
	 * 
	 * @param velocity [m/s]
	 * @return the time [s]
	 */
	public float getTimeByVel(final float velocity)
	{
		return AIConfig.getBallModel().getTimeByVel(getVel().getLength2(), velocity);
	}
	
	
	/**
	 * gets the theoretical velocity of the ball at a given position
	 * 
	 * @param pos
	 * @return the velocity [m/s]
	 */
	public float getVelByPos(final IVector2 pos)
	{
		return AIConfig.getBallModel().getVelByDist(getVel().getLength2(), GeoMath.distancePP(pos, getPos()));
	}
	
	
	/**
	 * gets the theoretical velocity of the ball after a given time
	 * 
	 * @param time [s]
	 * @return the velocity [m/s]
	 */
	public float getVelByTime(final float time)
	{
		return AIConfig.getBallModel().getVelByTime(getVel().getLength2(), time);
	}
	
	
	@Override
	public BallID getId()
	{
		return (BallID) id;
	}
	
	
	/**
	 * @return
	 */
	public boolean isOnCam()
	{
		return onCam;
	}
	
	
	@Override
	public IVector2 getPos()
	{
		return new Vector2f(pos.x(), pos.y());
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return new Vector2f(vel.x(), vel.y());
	}
	
	
	@Override
	public IVector2 getAcc()
	{
		return new Vector2f(acc.x(), acc.y());
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getPos3()
	{
		return pos;
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getVel3()
	{
		return vel;
	}
	
	
	/**
	 * @return
	 */
	public IVector3 getAcc3()
	{
		return acc;
	}
}
