/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 20, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Extended status command
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class TigerSystemStatusExt extends TigerSystemStatusV2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/** [mm] */
	@SerialData(type = ESerialDataType.INT16)
	private int	targetPos[]	= new int[3];
	
	/** [mm/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int	targetVel[]	= new int[3];
	
	/** [mm/s^2] */
	@SerialData(type = ESerialDataType.INT16)
	private int	targetAcc[]	= new int[3];
	
	/** [mm/s^2] */
	@SerialData(type = ESerialDataType.INT16)
	private int	motorSet[]	= new int[4];
	
	/** [mm/s^2] */
	@SerialData(type = ESerialDataType.INT16)
	private int	motorCur[]	= new int[4];
	
	/** [mm/s^2] */
	@SerialData(type = ESerialDataType.INT16)
	private int	motorOut[]	= new int[4];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public TigerSystemStatusExt()
	{
		super(ECommand.CMD_SYSTEM_STATUS_EXT);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public List<Float> getAllValues()
	{
		List<Float> values = super.getAllValues();
		values.add(getTargetPosition().x);
		values.add(getTargetPosition().y);
		values.add(getTargetOrientation());
		values.add(getTargetVelocity().x);
		values.add(getTargetVelocity().y);
		values.add(getTargetAngularVelocity());
		values.add(getTargetAcceleration().x);
		values.add(getTargetAcceleration().y);
		values.add(getTargetAngularAcceleration());
		for (int element : motorSet)
		{
			values.add(element / 100f);
		}
		for (int element : motorCur)
		{
			values.add(element / 100f);
		}
		for (int element : motorOut)
		{
			values.add(element / 100f);
		}
		return values;
	}
	
	
	/**
	 * Get velocity.
	 * 
	 * @return Velocity in [m/s]
	 */
	public Vector2 getTargetVelocity()
	{
		return new Vector2(targetVel[0] / 100.0f, targetVel[1] / 100.0f);
	}
	
	
	/**
	 * Get angular velocity.
	 * 
	 * @return aV in [rad/s]
	 */
	public float getTargetAngularVelocity()
	{
		return targetVel[2] / 100.0f;
	}
	
	
	/**
	 * Get acceleration.
	 * 
	 * @return Acceleration in [m/s�]
	 */
	public Vector2 getTargetAcceleration()
	{
		return new Vector2(targetAcc[0] / 100.0f, targetAcc[1] / 100.0f);
	}
	
	
	/**
	 * Get angular acceleration.
	 * 
	 * @return acceleration in [rad/s�]
	 */
	public float getTargetAngularAcceleration()
	{
		return targetAcc[2] / 100.0f;
	}
	
	
	/**
	 * Get position.
	 * 
	 * @return Position in [m]
	 */
	public Vector2 getTargetPosition()
	{
		return new Vector2(targetPos[0] / 100.0f, targetPos[1] / 100.0f);
	}
	
	
	/**
	 * Get orientation.
	 * 
	 * @return orientation in [rad]
	 */
	public float getTargetOrientation()
	{
		return targetPos[2] / 100f;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the motor
	 */
	public final float[] getMotor()
	{
		float[] out = new float[4];
		for (int i = 0; i < 4; i++)
		{
			out[i] = motorSet[i] / 100.0f;
		}
		return out;
	}
	
	
	/**
	 * @return the motor
	 */
	public final float[] getMotorVel()
	{
		float[] out = new float[4];
		for (int i = 0; i < 4; i++)
		{
			out[i] = motorCur[i] / 100.0f;
		}
		return out;
	}
}
