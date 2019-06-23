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
 * Simple data holder describing TIGER-bots recognized and tracked by the {@link AWorldPredictor}
 * 
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see TrackedBot
 * @see ATrackedObject
 * @author Gero
 * 
 */
public class TrackedTigerBot extends TrackedBot
{
	/**  */
	private static final long	serialVersionUID	= -8452911236731387383L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** 0-1, 1 = max */
	public final float			kickerCharge;
	
	/** 0-1, 1 = max */
	public final float			accuCharge;
	
	/** true = (bot not ready for operation) */
	public final boolean			defect;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param original
	 */
	public TrackedTigerBot(TrackedTigerBot o)
	{
		super(o.id, o.pos, o.vel, o.acc, o.height, o.angle, o.aVel, o.aAcc, o.confidence);
		this.accuCharge = o.accuCharge;
		this.defect = o.defect;
		this.kickerCharge = o.kickerCharge;
	}
	

	public TrackedTigerBot(int id, IVector2 pos, IVector2 vel, IVector2 acc, int height, float angle, float aVel,
			float aAcc, float confidence, float accuCharge, float kickerCharge, boolean defect)
	{
		super(id, pos, vel, acc, height, angle, aVel, aAcc, confidence);
		this.accuCharge = accuCharge;
		this.defect = defect;
		this.kickerCharge = kickerCharge;
	}
	
	public static TrackedTigerBot motionToTrackedTigerBot(int id, RobotMotionResult motion, int height, float accuCharge, float kickerCharge, boolean defect)
	{
		Vector2f pos = new Vector2f(	(float)(motion.x /WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT),
												(float)(motion.y /WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT));
		float vt = (float)(motion.vt/WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		float vo = (float)(motion.vo/WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
				
		double sinX = Math.sin(motion.movementAngle);
		double cosX = Math.cos(motion.movementAngle);
			
		double xVel = cosX*vt - sinX * vo;
		double yVel = sinX*vt + cosX * vo;
		Vector2f vel = new Vector2f((float) xVel, (float) yVel);
		Vector2f acc = new Vector2f(0.0f, 0.0f);
		
		float angle = (float) motion.orientation;
		float aVel  = (float)(motion.angularVelocity/WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal);
		float aAcc  = 0f;
		
		float confidence = (float) motion.confidence;
		return new TrackedTigerBot(id, pos, vel, acc, height, angle, aVel, aAcc, confidence, accuCharge, kickerCharge, defect);
	}
	
	public static TrackedTigerBot motionToTrackedTigerBot(int id, RobotMotionResult_V2 motion, int height, float accuCharge, float kickerCharge, boolean defect)
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
		return new TrackedTigerBot(id, pos, vel, acc, height, angle, aVel, aAcc, confidence, accuCharge, kickerCharge, defect);
	}
	
	public String toString()
	{
		String s = "id="+id+"\n";
		s += "pos: "+pos+"\n";
		s += "vel: "+vel+"\n";
		s += "acc: "+acc;
		return s;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
