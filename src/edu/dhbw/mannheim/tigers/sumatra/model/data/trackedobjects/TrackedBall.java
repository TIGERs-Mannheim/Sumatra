/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BallID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.BallMotionResult;


/**
 * Simple data holder describing balls that are recognized and tracked by the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see ATrackedObject
 * @author Gero
 * 
 */
@Embeddable
public class TrackedBall extends ATrackedObject
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -4610674696000287580L;
	
	/** mm */
	private IVector3				pos;
	/** m/s */
	private IVector3				vel;
	/** m/s^2 */
	private IVector3				acc;
	
	/** not final for ObjectDB */
	private boolean				onCam;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// -------------------------------------------------------------------------
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public TrackedBall(TrackedBall original)
	{
		super(original);
		onCam = original.onCam;
		pos = original.pos;
		vel = original.vel;
		acc = original.acc;
	}
	
	
	/**
	 * 
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param confidence
	 * @param onCam
	 */
	public TrackedBall(IVector3 pos, IVector3 vel, IVector3 acc, float confidence, boolean onCam)
	{
		super(new BallID(), confidence);
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
		this.onCam = onCam;
	}
	
	
	/**
	 * 
	 * @param motion
	 * @return
	 */
	public static TrackedBall motionToTrackedBall(BallMotionResult motion)
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public BallID getId()
	{
		return (BallID) id;
	}
	
	
	/**
	 * 
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
	 * 
	 * @return
	 */
	public IVector3 getPos3()
	{
		return pos;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IVector3 getVel3()
	{
		return vel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IVector3 getAcc3()
	{
		return acc;
	}
}
