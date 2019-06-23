/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;


/**
 * Simple data holder describing balls that are recognized and tracked by the {@link AWorldPredictor}
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see ATrackedObject
 * @author Gero
 * 
 */
public class TrackedBall extends ATrackedObject
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -4610674696000287580L;
	
	private final Vector3f		pos3;
	private final Vector3f		vel3;
	private final Vector3f		acc3;
	
	private final boolean		onCam;
	
	
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
		
		this.pos3 = new Vector3f(original.pos3());
		this.vel3 = new Vector3f(original.vel3());
		this.acc3 = new Vector3f(original.acc3());
		this.onCam = original.onCam;
	}
	

	public TrackedBall(int id, IVector3 pos, IVector3 vel, IVector3 acc, float confidence, boolean onCam)
	{
		super(id, pos, vel, acc, confidence);
		
		this.pos3 = new Vector3f(pos);
		this.vel3 = new Vector3f(vel);
		this.acc3 = new Vector3f(acc);
		this.onCam = onCam;
	}
	

	public TrackedBall(int id, float xPos, float xVel, float xAcc, float yPos, float yVel, float yAcc, float zPos,
			float zVel, float zAcc, float confidence, boolean onCam)
	{
		super(id, xPos, xVel, xAcc, yPos, yVel, yAcc, confidence);
		
		this.pos3 = new Vector3f(xPos, yPos, zPos);
		this.vel3 = new Vector3f(xVel, yVel, zVel);
		this.acc3 = new Vector3f(xAcc, yAcc, zAcc);
		this.onCam = onCam;
	}
	
	public static TrackedBall motionToTrackedBall(BallMotionResult motion)
	{
		int id = 0;
		float xPos = (float) (motion.x   / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT);
		float xVel = (float) (motion.vx / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		float xAcc = (float) (motion.ax / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A);
		float yPos = (float) (motion.y  / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT);
		float yVel = (float) (motion.vy / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		float yAcc = (float) (motion.ay / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A);
		float zPos = (float) (motion.z  / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT);
		float zVel = (float) (motion.vz / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		float zAcc = (float) (motion.az / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A);
		float confidence = (float) motion.confidence;
		boolean isOnCam = motion.onCam;
		
		return new TrackedBall(id, xPos, xVel, xAcc, yPos, yVel, yAcc, zPos, zVel, zAcc, confidence, isOnCam);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public Vector3f pos3()
	{
		return pos3;
	}
	

	public Vector3f vel3()
	{
		return vel3;
	}
	

	public Vector3f acc3()
	{
		return acc3;
	}
	
	public boolean isOnCam()
	{
		return onCam;
	}
}
