/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv2;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.math.Vector2;


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
	private final int					curPosition[]							= new int[3];
	
	/** [mm/s], [mrad/s] */
	@SerialData(type = ESerialDataType.INT16)
	private final int					curVelocity[]							= new int[3];
	
	/** [V] */
	@SerialData(type = ESerialDataType.UINT8)
	private int							kickerLevel;
	
	/** [rpm] */
	@SerialData(type = ESerialDataType.INT16)
	private int							dribblerSpeed;
	
	/** [mV] */
	@SerialData(type = ESerialDataType.UINT16)
	private int							batteryLevel;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int							barrierKickCounter;
	
	@SerialData(type = ESerialDataType.UINT16)
	private int							features									= 0x001F;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int							hardwareId;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int							dribblerTemp;
	
	private static final int		UNUSED_FIELD							= 0x7FFF;
	
	private static final double	BAT_CELL_COUNT_THRESHOLD_VOLTAGE	= 13.0;
	private static final double	BAT_3S_MIN_VOLTAGE					= 10.5;
	private static final double	BAT_3S_MAX_VOLTAGE					= 12.6;
	private static final double	BAT_4S_MIN_VOLTAGE					= 13.6;
	private static final double	BAT_4S_MAX_VOLTAGE					= 16.8;
	
	
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
		return new Vector2(curVelocity[0] / 1000.0, curVelocity[1] / 1000.0);
	}
	
	
	/**
	 * Get angular velocity.
	 * 
	 * @return aV in [rad/s]
	 */
	public double getAngularVelocity()
	{
		return curVelocity[2] / 1000.0;
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
	public double getAngularAcceleration()
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
		return new Vector2(curPosition[0] / 1000.0, curPosition[1] / 1000.0);
	}
	
	
	/**
	 * Get orientation.
	 * 
	 * @return orientation in [rad]
	 */
	public double getOrientation()
	{
		return curPosition[2] / 1000.0;
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
	public double getKickerLevel()
	{
		return kickerLevel;
	}
	
	
	/**
	 * @return the dribblerSpeed in [rpm]
	 */
	public double getDribblerSpeed()
	{
		return dribblerSpeed;
	}
	
	
	/**
	 * @return the batteryLevel in [V]
	 */
	public double getBatteryLevel()
	{
		return batteryLevel * 0.001;
	}
	
	
	/**
	 * Distinguishes between 3S and 4S batteries and returns the remaining battery level.
	 * 
	 * @return Battery level in percentage (0-1)
	 */
	public double getBatteryPercentage()
	{
		double bat = getBatteryLevel();
		
		if (bat < BAT_CELL_COUNT_THRESHOLD_VOLTAGE)
		{
			// That's a 3S battery
			if (bat > BAT_3S_MAX_VOLTAGE)
			{
				return 1.0;
			}
			
			if (bat < BAT_3S_MIN_VOLTAGE)
			{
				return 0.0;
			}
			
			return ((bat - BAT_3S_MIN_VOLTAGE)) / (BAT_3S_MAX_VOLTAGE - BAT_3S_MIN_VOLTAGE);
		}
		
		// And that's a 4S
		if (bat > BAT_4S_MAX_VOLTAGE)
		{
			return 1.0;
		}
		
		if (bat < BAT_4S_MIN_VOLTAGE)
		{
			return 0.0;
		}
		
		return ((bat - BAT_4S_MIN_VOLTAGE)) / (BAT_4S_MAX_VOLTAGE - BAT_4S_MIN_VOLTAGE);
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
	public double getDribblerTemp()
	{
		return dribblerTemp * 0.5;
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
