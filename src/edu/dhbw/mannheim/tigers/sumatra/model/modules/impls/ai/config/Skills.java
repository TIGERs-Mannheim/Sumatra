/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.02.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2.AccelerationFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2.PIDFacade;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerConfig;


/**
 * Configuration object for the skills.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class Skills
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String		nodePath	= "skillSystem.";
	
	// Rotate
	private final float		maxTurnSpeed;
	private final float		rotationSpeedThreshold;
	private final float		rotateKP;
	private final float		rotateKI;
	private final float		rotateKD;
	
	// Dribble
	private final int			refRPM;
	
	// Kicker
	private final float		ballFrictionSlide;
	private final float		ballFrictionRoll;
	private final int			refFiringDuration;
	private final float		refVelocity;
	private final float		edgeFactor;
	
	// Move
	private final float		maxVelocity;
	private final float		maxDeceleration;
	private final float		maxBreakingDistance;
	private final float		moveSpeedThreshold;
	private final boolean	useSplines;
	private final boolean	ballAsObstacle;
	private final float		accelerationInc;
	
	private AccelerationFacade acceleration = new AccelerationFacade();
	private PIDFacade			pids = new PIDFacade();
	
	private XMLConfiguration config;	// used to store changed values 
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public Skills(XMLConfiguration configFile)
	{
		this.config = configFile;
		
		maxTurnSpeed = configFile.getFloat(nodePath + "rotate.maxTurnSpeed");
		rotationSpeedThreshold = configFile.getFloat(nodePath + "rotate.rotationSpeedThreshold");
		rotateKP = configFile.getFloat(nodePath + "rotate.kP");
		rotateKI = configFile.getFloat(nodePath + "rotate.kI");
		rotateKD = configFile.getFloat(nodePath + "rotate.kD");
		
		refRPM = configFile.getInt(nodePath + "dribble.refRPM");
		
		ballFrictionSlide = configFile.getFloat(nodePath + "kicker.ballFrictionSlide");
		ballFrictionRoll = configFile.getFloat(nodePath + "kicker.ballFrictionRoll");
		refFiringDuration = configFile.getInt(nodePath + "kicker.refFiringDuration");
		refVelocity = configFile.getFloat(nodePath + "kicker.refVelocity");
		edgeFactor = configFile.getFloat(nodePath + "kicker.edgeFactor");
		
		maxVelocity = configFile.getFloat(nodePath + "moveTo.maxVelocity");
		maxDeceleration = configFile.getFloat(nodePath + "moveTo.maxDeceleration");
		maxBreakingDistance = (maxVelocity * maxVelocity) / (2 * maxDeceleration);
		moveSpeedThreshold = configFile.getFloat(nodePath + "moveTo.moveSpeedThreshold");
		useSplines = configFile.getBoolean(nodePath + "moveTo.useSplines");
		ballAsObstacle = configFile.getBoolean(nodePath + "moveTo.ballAsObstacle");
		accelerationInc = configFile.getFloat(nodePath + "moveTo.accelerationInc");
		
		acceleration.maxXAccelBeg.setSavedString(configFile.getString(nodePath + "move.maxXAccel.beg"));
		acceleration.maxXAccelMid.setSavedString(configFile.getString(nodePath + "move.maxXAccel.mid"));
		acceleration.maxXAccelEnd.setSavedString(configFile.getString(nodePath + "move.maxXAccel.end"));
		acceleration.maxYAccelBeg.setSavedString(configFile.getString(nodePath + "move.maxYAccel.beg"));
		acceleration.maxYAccelMid.setSavedString(configFile.getString(nodePath + "move.maxYAccel.mid"));
		acceleration.maxYAccelEnd.setSavedString(configFile.getString(nodePath + "move.maxYAccel.end"));
		acceleration.maxXDeccelBeg.setSavedString(configFile.getString(nodePath + "move.maxXDeccel.beg"));
		acceleration.maxXDeccelMid.setSavedString(configFile.getString(nodePath + "move.maxXDeccel.mid"));
		acceleration.maxXDeccelEnd.setSavedString(configFile.getString(nodePath + "move.maxXDeccel.end"));
		acceleration.maxYDeccelBeg.setSavedString(configFile.getString(nodePath + "move.maxYDeccel.beg"));
		acceleration.maxYDeccelMid.setSavedString(configFile.getString(nodePath + "move.maxYDeccel.mid"));
		acceleration.maxYDeccelEnd.setSavedString(configFile.getString(nodePath + "move.maxYDeccel.end"));
		
		pids.velocity.p = configFile.getFloat(nodePath + "move.pidVelo.kp");
		pids.velocity.i = configFile.getFloat(nodePath + "move.pidVelo.ki");
		pids.velocity.d = configFile.getFloat(nodePath + "move.pidVelo.kd");
		pids.velocity.maxOutput = configFile.getFloat(nodePath + "move.pidVelo.max");
		pids.velocity.slewRate = configFile.getFloat(nodePath + "move.pidVelo.slew");
		pids.orientation.p = configFile.getFloat(nodePath + "move.pidOrient.kp");
		pids.orientation.i = configFile.getFloat(nodePath + "move.pidOrient.ki");
		pids.orientation.d = configFile.getFloat(nodePath + "move.pidOrient.kd");
		pids.orientation.maxOutput = configFile.getFloat(nodePath + "move.pidOrient.max");
		pids.orientation.slewRate = configFile.getFloat(nodePath + "move.pidOrient.slew");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the rotate maxTurnSpeed
	 */
	public float getMaxTurnSpeed()
	{
		return maxTurnSpeed;
	}
	

	/**
	 * @return the rotationSpeedThreshold
	 */
	public float getRotationSpeedThreshold()
	{
		return rotationSpeedThreshold;
	}
	

	public PIDControllerConfig getRotatePIDConf()
	{
		return new PIDControllerConfig(rotateKP, rotateKI, rotateKD, maxTurnSpeed);
	}
	

	/**
	 * @return the dribble refRPM
	 */
	public int getRefRPM()
	{
		return refRPM;
	}
	

	/**
	 * @return the refFiringDuration
	 */
	public int getRefFiringDuration()
	{
		return refFiringDuration;
	}
	

	/**
	 * @return the ballFrictionSlide
	 */
	public float getBallFrictionSlide()
	{
		return ballFrictionSlide;
	}
	

	/**
	 * @return the ballFrictionRoll
	 */
	public float getBallFrictionRoll()
	{
		return ballFrictionRoll;
	}
	

	/**
	 * @return the refVelocity
	 */
	public float getRefVelocity()
	{
		return refVelocity;
	}
	

	/**
	 * @return the edgeFactor
	 */
	public float getEdgeFactor()
	{
		return edgeFactor;
	}
	

	/**
	 * @return the maxVelocity
	 */
	public float getMaxVelocity()
	{
		return maxVelocity;
	}
	

	/**
	 * @return the maxDeceleration
	 */
	public float getMaxDeceleration()
	{
		return maxDeceleration;
	}
	

	/**
	 * @return the maxBreakingDistance
	 */
	public float getMaxBreakingDistance()
	{
		return maxBreakingDistance;
	}
	

	/**
	 * @return the moveSpeedThreshold
	 */
	public float getMoveSpeedThreshold()
	{
		return moveSpeedThreshold;
	}
	

	/**
	 * @return the useSplines
	 */
	public boolean getUseSplines()
	{
		return useSplines;
	}
	

	/**
	 * @return the ballAsObstacle
	 */
	public boolean getBallAsObstacle()
	{
		return ballAsObstacle;
	}
	

	/**
	 * @return the accelerationInc
	 */
	public float getAccelerationInc()
	{
		return accelerationInc;
	}
	
	public AccelerationFacade getAcceleration()
	{
		return acceleration;
	}
	
	public PIDFacade getPids()
	{
		return pids;
	}
	
	public void setAcceleration(AccelerationFacade accel)
	{
		this.acceleration = accel;
		
		config.setProperty(nodePath + "move.maxXAccel.beg", accel.maxXAccelBeg.getSaveableString());
		config.setProperty(nodePath + "move.maxXAccel.mid", accel.maxXAccelMid.getSaveableString());
		config.setProperty(nodePath + "move.maxXAccel.end", accel.maxXAccelEnd.getSaveableString());
		config.setProperty(nodePath + "move.maxYAccel.beg", accel.maxYAccelBeg.getSaveableString());
		config.setProperty(nodePath + "move.maxYAccel.mid", accel.maxYAccelMid.getSaveableString());
		config.setProperty(nodePath + "move.maxYAccel.end", accel.maxYAccelEnd.getSaveableString());
		config.setProperty(nodePath + "move.maxXDeccel.beg", accel.maxXDeccelBeg.getSaveableString());
		config.setProperty(nodePath + "move.maxXDeccel.mid", accel.maxXDeccelMid.getSaveableString());
		config.setProperty(nodePath + "move.maxXDeccel.end", accel.maxXDeccelEnd.getSaveableString());
		config.setProperty(nodePath + "move.maxYDeccel.beg", accel.maxYDeccelBeg.getSaveableString());
		config.setProperty(nodePath + "move.maxYDeccel.mid", accel.maxYDeccelMid.getSaveableString());
		config.setProperty(nodePath + "move.maxYDeccel.end", accel.maxYDeccelEnd.getSaveableString());
	}
	
	public void setPids(PIDFacade pids)
	{
		this.pids = pids;
		
		config.setProperty(nodePath + "move.pidVelo.kp", pids.velocity.p);
		config.setProperty(nodePath + "move.pidVelo.ki", pids.velocity.i);
		config.setProperty(nodePath + "move.pidVelo.kd", pids.velocity.d);
		config.setProperty(nodePath + "move.pidVelo.max", pids.velocity.maxOutput);
		config.setProperty(nodePath + "move.pidVelo.slew", pids.velocity.slewRate);
		config.setProperty(nodePath + "move.pidOrient.kp", pids.orientation.p);
		config.setProperty(nodePath + "move.pidOrient.ki", pids.orientation.i);
		config.setProperty(nodePath + "move.pidOrient.kd", pids.orientation.d);
		config.setProperty(nodePath + "move.pidOrient.max", pids.orientation.maxOutput);
		config.setProperty(nodePath + "move.pidOrient.slew", pids.orientation.slewRate);
	}
	
}
