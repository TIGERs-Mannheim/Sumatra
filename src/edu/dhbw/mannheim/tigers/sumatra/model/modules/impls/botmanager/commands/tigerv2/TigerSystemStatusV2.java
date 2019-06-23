/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Status message from tiger bot v2013.
 * Contains periodic info sent at high rate.
 */
public class TigerSystemStatusV2 extends ACommand
{
	/** [Ve-1] */
	@SerialData(type = ESerialDataType.UINT16)
	private int	kickerLevel;
	
	/** 1 or 0 */
	@SerialData(type = ESerialDataType.UINT8)
	private int	barrierInterrupted;
	
	/** pos, vel, acc */
	@SerialData(type = ESerialDataType.UINT8)
	private int	stateUpdated;
	
	/** [mm] */
	@SerialData(type = ESerialDataType.INT16)
	private int	pos[]	= new int[3];
	
	/** [mm/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int	vel[]	= new int[3];
	
	/** [mm/s^2] */
	@SerialData(type = ESerialDataType.INT16)
	private int	acc[]	= new int[3];
	
	
	/**
	 * Default constructor.
	 */
	public TigerSystemStatusV2()
	{
		super(ECommand.CMD_SYSTEM_STATUS_V2);
	}
	
	
	/**
	 * 
	 * @param cmd
	 */
	protected TigerSystemStatusV2(ECommand cmd)
	{
		super(cmd);
	}
	
	
	/**
	 * @return
	 */
	public List<Float> getAllValues()
	{
		List<Float> values = new ArrayList<Float>(18);
		values.add(getPosition().x);
		values.add(getPosition().y);
		values.add(getOrientation());
		values.add(getVelocity().x);
		values.add(getVelocity().y);
		values.add(getAngularVelocity());
		values.add(getAcceleration().x);
		values.add(getAcceleration().y);
		values.add(getAngularAcceleration());
		return values;
	}
	
	
	/**
	 * Get velocity.
	 * 
	 * @return Velocity in [m/s]
	 */
	public Vector2 getVelocity()
	{
		return new Vector2(vel[0] / 1000.0f, vel[1] / 1000.0f);
	}
	
	
	/**
	 * Get angular velocity.
	 * 
	 * @return aV in [rad/s]
	 */
	public float getAngularVelocity()
	{
		return vel[2] / 1000.0f;
	}
	
	
	/**
	 * Get acceleration.
	 * 
	 * @return Acceleration in [m/s�]
	 */
	public Vector2 getAcceleration()
	{
		return new Vector2(acc[0] / 1000.0f, acc[1] / 1000.0f);
	}
	
	
	/**
	 * Get angular acceleration.
	 * 
	 * @return acceleration in [rad/s�]
	 */
	public float getAngularAcceleration()
	{
		return acc[2] / 1000.0f;
	}
	
	
	/**
	 * Get position.
	 * 
	 * @return Position in [m]
	 */
	public Vector2 getPosition()
	{
		return new Vector2(pos[0] / 1000.0f, pos[1] / 1000.0f);
	}
	
	
	/**
	 * Get orientation.
	 * 
	 * @return orientation in [rad]
	 */
	public float getOrientation()
	{
		return pos[2] / 1000f;
	}
	
	
	/**
	 * 
	 * @return true if position content is updated
	 */
	public boolean isPositionUpdated()
	{
		return (stateUpdated & 0x01) == 0x01 ? true : false;
	}
	
	
	/**
	 * 
	 * @return true if velocity content is updated
	 */
	public boolean isVelocityUpdated()
	{
		return (stateUpdated & 0x02) == 0x02 ? true : false;
	}
	
	
	/**
	 * 
	 * @return true if acceleration content is updated
	 */
	public boolean isAccelerationUpdated()
	{
		return (stateUpdated & 0x04) == 0x04 ? true : false;
	}
	
	
	/**
	 * 
	 * @return true if the dribbler reached the desired speed
	 */
	public boolean isDribblerSpeedReached()
	{
		return (stateUpdated & 0x08) == 0x08 ? true : false;
	}
	
	
	/**
	 * 
	 * @return true if the dribbler is not able to reach the desired value
	 */
	public boolean isDribblerOverloaded()
	{
		return (stateUpdated & 0x10) == 0x10 ? true : false;
	}
	
	
	/**
	 * Get kicker level.
	 * 
	 * @return kicker level in [V]
	 */
	public float getKickerLevel()
	{
		return kickerLevel / 10.0f;
	}
	
	
	/**
	 * Barrier interrupted.
	 * 
	 * @return
	 */
	public boolean isBarrierInterrupted()
	{
		return (barrierInterrupted == 0) ? false : true;
	}
}
