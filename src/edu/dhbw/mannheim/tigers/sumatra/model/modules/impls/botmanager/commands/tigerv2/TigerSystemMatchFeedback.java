/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Synopsis of all relevant data from one bot.
 * 
 * @author AndreR
 */
@Persistent
public class TigerSystemMatchFeedback extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [mm], [mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private int						curPosition[]	= new int[3];
	
	/** [mm/s], [mrad/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int						curVelocity[]	= new int[3];
	
	/** [V] */
	@SerialData(type = ESerialDataType.UINT8)
	private int						kickerLevel;
	
	/** [rpm] */
	@SerialData(type = ESerialDataType.INT16)
	private int						dribblerSpeed;
	
	/** [mV] */
	@SerialData(type = ESerialDataType.UINT16)
	private int						batteryLevel;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						barrierKickCounter;
	
	@SerialData(type = ESerialDataType.UINT16)
	private int						features;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						hardwareId;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int						dribblerTemp;
	
	private static final int	UNUSED_FIELD	= 0x7FFF;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public TigerSystemMatchFeedback()
	{
		super(ECommand.CMD_SYSTEM_MATCH_FEEDBACK);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Get velocity.
	 * 
	 * @return Velocity in [m/s]
	 */
	public Vector2 getVelocity()
	{
		return new Vector2(curVelocity[0] / 1000.0f, curVelocity[1] / 1000.0f);
	}
	
	
	/**
	 * Get angular velocity.
	 * 
	 * @return aV in [rad/s]
	 */
	public float getAngularVelocity()
	{
		return curVelocity[2] / 1000.0f;
	}
	
	
	/**
	 * Get acceleration.
	 * 
	 * @return Acceleration in [m/s^2]
	 */
	public Vector2 getAcceleration()
	{
		return new Vector2();
	}
	
	
	/**
	 * Get angular acceleration.
	 * 
	 * @return acceleration in [rad/s^2]
	 */
	public float getAngularAcceleration()
	{
		return 0;
	}
	
	
	/**
	 * Get position.
	 * 
	 * @return Position in [m]
	 */
	public Vector2 getPosition()
	{
		return new Vector2(curPosition[0] / 1000.0f, curPosition[1] / 1000.0f);
	}
	
	
	/**
	 * Get orientation.
	 * 
	 * @return orientation in [rad]
	 */
	public float getOrientation()
	{
		return curPosition[2] / 1000f;
	}
	
	
	/**
	 * @return
	 */
	public boolean isPositionValid()
	{
		if (curPosition[0] == UNUSED_FIELD)
		{
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * @return
	 */
	public boolean isVelocityValid()
	{
		if (curVelocity[0] == UNUSED_FIELD)
		{
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * @return
	 */
	public boolean isAccelerationValid()
	{
		return false;
	}
	
	
	/**
	 * @return the kickerLevel in [V]
	 */
	public float getKickerLevel()
	{
		return kickerLevel;
	}
	
	
	/**
	 * @return the dribblerSpeed in [rpm]
	 */
	public float getDribblerSpeed()
	{
		return dribblerSpeed;
	}
	
	
	/**
	 * @return the batteryLevel in [V]
	 */
	public float getBatteryLevel()
	{
		return batteryLevel * 0.001f;
	}
	
	
	/**
	 * @return
	 */
	public boolean isBarrierInterrupted()
	{
		if ((barrierKickCounter & 0x80) == 0x80)
		{
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Counter is incremented for every executed kick.
	 * 
	 * @return
	 */
	public int getKickCounter()
	{
		return barrierKickCounter & 0x7F;
	}
	
	
	/**
	 * @return the hardwareId
	 */
	public int getHardwareId()
	{
		return hardwareId;
	}
	
	
	/**
	 * @return
	 */
	public float getDribblerTemp()
	{
		return dribblerTemp * 0.5f;
	}
	
	
	/**
	 * @param feature
	 * @return
	 */
	public boolean isFeatureWorking(final EFeature feature)
	{
		if ((features & feature.getId()) != 0)
		{
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * @param feature
	 * @param working
	 */
	public void setFeature(final EFeature feature, final boolean working)
	{
		if (working)
		{
			features |= feature.getId();
		} else
		{
			features &= (feature.getId() ^ 0xFFFF);
		}
	}
}
