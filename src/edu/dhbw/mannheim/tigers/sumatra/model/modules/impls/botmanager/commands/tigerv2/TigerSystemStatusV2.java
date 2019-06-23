/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Status message from tiger bot v2013.
 * Contains periodic info sent at high rate.
 */
public class TigerSystemStatusV2 extends ACommand
{
	/** [Ve-1] */
	private int	kickerLevel;
	/** 1 or 0 */
	private int	barrierInterrupted;
	/** pos, vel, acc */
	private int	stateUpdated;
	/** [mm] */
	private int	pos[]	= new int[3];
	/** [mm/s] */
	private int	vel[]	= new int[3];
	/** [mm/s�] */
	private int	acc[]	= new int[3];
	
	
	/**
	 * Default constructor.
	 */
	public TigerSystemStatusV2()
	{
	}
	
	
	@Override
	public void setData(byte[] data)
	{
		kickerLevel = byteArray2UShort(data, 0);
		barrierInterrupted = byteArray2UByte(data, 2);
		stateUpdated = byteArray2UByte(data, 3);
		
		pos[0] = byteArray2Short(data, 4);
		pos[1] = byteArray2Short(data, 6);
		pos[2] = byteArray2Short(data, 8);
		
		vel[0] = byteArray2Short(data, 10);
		vel[1] = byteArray2Short(data, 12);
		vel[2] = byteArray2Short(data, 14);
		
		acc[0] = byteArray2Short(data, 16);
		acc[1] = byteArray2Short(data, 18);
		acc[2] = byteArray2Short(data, 20);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, kickerLevel);
		byte2ByteArray(data, 2, barrierInterrupted);
		byte2ByteArray(data, 3, stateUpdated);
		
		short2ByteArray(data, 4, pos[0]);
		short2ByteArray(data, 6, pos[1]);
		short2ByteArray(data, 8, pos[2]);
		
		short2ByteArray(data, 10, vel[0]);
		short2ByteArray(data, 12, vel[1]);
		short2ByteArray(data, 14, vel[2]);
		
		short2ByteArray(data, 16, acc[0]);
		short2ByteArray(data, 18, acc[1]);
		short2ByteArray(data, 20, acc[2]);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_STATUS_V2;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 22;
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
