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

import org.apache.commons.configuration.Configuration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.Function1DFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;


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
	private static final String	NODE_PATH	= "skillSystem.";
	
	// Rotate
	private float						maxTurnSpeed;
	private float						rotationSpeedThreshold;
	/** [rad/s^2] */
	private float						maxRotateVelocity;
	/** [rad/s^2] */
	private float						maxRotateAcceleration;
	private IFunction1D				normalAngleToSpeed;
	
	// Dribble
	private int							refRPM;
	
	// Kicker
	private float						ballFrictionSlide;
	private float						ballFrictionRoll;
	private int							refFiringDuration;
	private float						refVelocity;
	private float						edgeFactor;
	/** [mm/s] */
	private float						maxShootVelocity;
	private int							kickerDischargeTreshold;
	private IFunction1D				kickDribbleFunc;
	
	// Chip kicker
	private IFunction1D				chipDistanceFunc;
	private IFunction1D				chipDribbleFunc;
	
	// Move
	private float						moveSpeedThreshold;
	/** [m/s] */
	public float						maxLinearVelocity;
	/** [m/s�] */
	public float						maxLinearAcceleration;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * empty constructor, all attributes will remain uninitialized
	 */
	private Skills()
	{
		
	}
	
	
	/**
	 * @param configFile
	 */
	public Skills(Configuration configFile)
	{
		this(configFile, new Skills());
	}
	
	
	/**
	 * @param configFile
	 * @param base
	 */
	public Skills(Configuration configFile, final Skills base)
	{
		maxTurnSpeed = configFile.getFloat(NODE_PATH + "rotate.maxTurnSpeed", base.maxTurnSpeed);
		rotationSpeedThreshold = configFile.getFloat(NODE_PATH + "rotate.rotationSpeedThreshold",
				base.rotationSpeedThreshold);
		maxRotateVelocity = configFile.getFloat(NODE_PATH + "rotate.maxRotateVelocity", base.maxRotateVelocity);
		maxRotateAcceleration = configFile.getFloat(NODE_PATH + "rotate.maxRotateAcceleration",
				base.maxRotateAcceleration);
		normalAngleToSpeed = Function1DFactory.createFunctionFromString(configFile.getString(NODE_PATH
				+ "rotate.normalAngleToSpeed", Function1DFactory.createStringFromFunction(base.normalAngleToSpeed)));
		
		refRPM = configFile.getInt(NODE_PATH + "dribble.refRPM", base.refRPM);
		
		ballFrictionSlide = configFile.getFloat(NODE_PATH + "kicker.ballFrictionSlide", base.ballFrictionSlide);
		ballFrictionRoll = configFile.getFloat(NODE_PATH + "kicker.ballFrictionRoll", base.ballFrictionRoll);
		refFiringDuration = configFile.getInt(NODE_PATH + "kicker.refFiringDuration", base.refFiringDuration);
		refVelocity = configFile.getFloat(NODE_PATH + "kicker.refVelocity", base.refVelocity);
		edgeFactor = configFile.getFloat(NODE_PATH + "kicker.edgeFactor", base.edgeFactor);
		maxShootVelocity = configFile.getFloat(NODE_PATH + "kicker.maxShootVelocity", base.maxShootVelocity);
		kickerDischargeTreshold = configFile.getInt(NODE_PATH + "kicker.kickerDischargeTreshold",
				base.kickerDischargeTreshold);
		chipDistanceFunc = Function1DFactory.createFunctionFromString(configFile.getString(NODE_PATH
				+ "kicker.chipDistanceFunc", Function1DFactory.createStringFromFunction(base.chipDistanceFunc)));
		chipDribbleFunc = Function1DFactory.createFunctionFromString(configFile.getString(NODE_PATH
				+ "kicker.chipDribbleFunc", Function1DFactory.createStringFromFunction(base.chipDribbleFunc)));
		kickDribbleFunc = Function1DFactory.createFunctionFromString(configFile.getString(NODE_PATH
				+ "kicker.kickDribbleFunc", Function1DFactory.createStringFromFunction(base.kickDribbleFunc)));
		
		moveSpeedThreshold = configFile.getFloat(NODE_PATH + "move.moveSpeedThreshold", base.moveSpeedThreshold);
		maxLinearVelocity = configFile.getFloat(NODE_PATH + "move.maxLinearVelocity", base.maxLinearVelocity);
		maxLinearAcceleration = configFile.getFloat(NODE_PATH + "move.maxLinearAcceleration", base.maxLinearAcceleration);
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the kickerChargeLowTreshold
	 */
	public final int getKickerDischargeTreshold()
	{
		return kickerDischargeTreshold;
	}
	
	
	/**
	 * @return the maxShootVelocity [mm/s]
	 */
	public float getMaxShootVelocity()
	{
		return maxShootVelocity;
	}
	
	
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
	public float getRefKickVelocity()
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
	 * @return the chipDistanceFunc
	 */
	public final IFunction1D getChipDistanceFunc()
	{
		return chipDistanceFunc;
	}
	
	
	/**
	 * @return the chipDribbleFunc
	 */
	public final IFunction1D getChipDribbleFunc()
	{
		return chipDribbleFunc;
	}
	
	
	/**
	 * @return
	 */
	public IFunction1D getKickDribbleFunc()
	{
		return kickDribbleFunc;
	}
	
	
	/**
	 * @return the maxRotateVelocity
	 */
	public float getMaxRotateVelocity()
	{
		return maxRotateVelocity;
	}
	
	
	/**
	 * @return the maxRotateAcceleration
	 */
	public float getMaxRotateAcceleration()
	{
		return maxRotateAcceleration;
	}
	
	
	/**
	 * @return the normalAngleToSpeed
	 */
	public IFunction1D getNormalAngleToSpeed()
	{
		return normalAngleToSpeed;
	}
	
	
	/**
	 * @return the moveSpeedThreshold
	 */
	public float getMoveSpeedThreshold()
	{
		return moveSpeedThreshold;
	}
	
	
	/**
	 * @return the maxLinearVelocity
	 */
	public float getMaxLinearVelocity()
	{
		return maxLinearVelocity;
	}
	
	
	/**
	 * @return the maxLinearAcceleration
	 */
	public float getMaxLinearAcceleration()
	{
		return maxLinearAcceleration;
	}
	
	
}
