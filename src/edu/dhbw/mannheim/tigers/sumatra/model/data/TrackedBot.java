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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;


/**
 * Simple data holder describing bots recognized and tracked by the {@link AWorldPredictor}
 * 
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see ATrackedObject
 * @author Gero
 * 
 */
public class TrackedBot extends ATrackedObject
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	//TODO Malte asks: "Shouldn't these fields be final!?!?!?!"
	
	/**  */
	private static final long	serialVersionUID	= 3617073411160054690L;
	
	/** mm */
	public int						height				= 150;
	
	/** rad */
	public float					angle					= 0.0f;
	
	/** rad/s */
	public float					aVel					= 0.0f;
	
	/** rad/s^2 */
	public float					aAcc					= 0.0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public TrackedBot(TrackedBot o)
	{
		super(o.height, o.pos, o.vel, o.acc, o.aAcc);
		this.height = o.height;
		this.aVel = o.aVel;
		this.aAcc = o.aAcc;
		this.angle = o.angle;
	}
	

	public TrackedBot(int id, IVector2 pos, IVector2 vel, IVector2 acc, int height, float angle, float aVel, float aAcc,
			float confidence)
	{
		super(id, pos, vel, acc, confidence);
		this.height = height;
		this.angle = angle;
		this.aVel = aVel;
		this.aAcc = aAcc;
	}
	
	
	public static TrackedBot motionToTrackedBot(int id, RobotMotionResult motion, int height)
	{
		Vector2f pos = new Vector2f(	(float)(motion.x /WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT),
												(float)(motion.y /WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT));
		float vt = (float)(motion.vt/WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		float vo = (float)(motion.vo/WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		
		double sinX = Math.sin(motion.orientation);
		double cosX = Math.cos(motion.orientation);
		double xVel = cosX*vt - sinX * vo;
		double yVel = sinX*vt + cosX * vo;
		Vector2f vel = new Vector2f((float) xVel, (float) yVel);
		Vector2f acc = new Vector2f(0.0f, 0.0f);
		
		float angle = (float) motion.orientation;
		float aVel  = (float)(motion.angularVelocity/WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal);
		float aAcc  = 0f;
		
		float confidence = (float) motion.confidence;
		return new TrackedBot(id, pos, vel, acc, height, angle, aVel, aAcc, confidence);
	}
	
	public static TrackedBot motionToTrackedBot(int id, RobotMotionResult_V2 motion, int height)
	{
		Vector2f pos = new Vector2f(	(float)(motion.x /WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT),
												(float)(motion.y /WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT));
		float v = (float)(motion.v/WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);

		double xVel = v * Math.cos(motion.movementAngle);
		double yVel = v * Math.sin(motion.movementAngle);
		Vector2f vel = new Vector2f((float) xVel, (float) yVel);
		Vector2f acc = new Vector2f(0.0f, 0.0f);
		
		float angle = (float) motion.orientation;
		float aVel  = (float)((motion.angularVelocity+motion.trackSpeed)/WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal);
		float aAcc  = 0f;
		
		float confidence = (float) motion.confidence;
		return new TrackedBot(id, pos, vel, acc, height, angle, aVel, aAcc, confidence);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

}
