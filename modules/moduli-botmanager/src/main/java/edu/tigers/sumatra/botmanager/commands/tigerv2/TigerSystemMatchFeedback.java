/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.tigerv2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Synopsis of all relevant data from one bot.
 * 
 * @author AndreR
 */
@Persistent
public class TigerSystemMatchFeedback extends ACommand implements IExportable
{
	private static final int UNUSED_FIELD = 0x7FFF;
	private static final double BAT_CELL_COUNT_THRESHOLD_VOLTAGE = 13.0;
	private static final double BAT_3S_MIN_VOLTAGE = 10.5;
	private static final double BAT_3S_MAX_VOLTAGE = 12.6;
	private static final double BAT_4S_MIN_VOLTAGE = 13.6;
	private static final double BAT_4S_MAX_VOLTAGE = 16.8;
	/** [mm], [mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private final int[] curPosition = new int[3];
	/** [mm/s], [mrad/s] */
	@SerialData(type = ESerialDataType.INT16)
	private final int[] curVelocity = new int[3];
	/** [V] */
	@SerialData(type = ESerialDataType.UINT8)
	private int kickerLevel;
	/** [rpm] */
	@SerialData(type = ESerialDataType.INT16)
	private int dribblerSpeed;
	/** [mV] */
	@SerialData(type = ESerialDataType.UINT16)
	private int batteryLevel;
	@SerialData(type = ESerialDataType.UINT8)
	private int barrierKickCounter;
	@SerialData(type = ESerialDataType.UINT16)
	private int features = 0x001F;
	@SerialData(type = ESerialDataType.UINT8)
	private int hardwareId;
	@SerialData(type = ESerialDataType.UINT8)
	private int dribblerTemp;
	
	
	/**
	 * Constructor.
	 */
	public TigerSystemMatchFeedback()
	{
		super(ECommand.CMD_SYSTEM_MATCH_FEEDBACK);
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> nbrs = new ArrayList<>();
		nbrs.addAll(getPosition().getNumberList());
		nbrs.add(getOrientation());
		nbrs.addAll(getVelocity().getNumberList());
		nbrs.add(getAngularVelocity());
		nbrs.add(isPositionValid() ? 1 : 0);
		nbrs.add(isVelocityValid() ? 1 : 0);
		nbrs.add(getKickerLevel());
		nbrs.add(getDribblerSpeed());
		nbrs.add(getBatteryPercentage());
		nbrs.add(isBarrierInterrupted() ? 1 : 0);
		nbrs.add(getKickCounter());
		nbrs.add(getDribblerTemp());
		nbrs.add(features);
		return nbrs;
	}
	
	
	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "pos_valid", "vel_valid",
				"kickerLevel", "dribbleSpeed", "batteryPercentage", "barrierInterrupted", "kickCounter", "dribblerTemp",
				"features");
	}
	
	
	/**
	 * Get velocity.
	 * 
	 * @return Velocity in [m/s]
	 */
	public Vector2 getVelocity()
	{
		return Vector2.fromXY(curVelocity[0] / 1000.0, curVelocity[1] / 1000.0);
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
	public IVector2 getAcceleration()
	{
		return Vector2f.ZERO_VECTOR;
	}
	
	
	/**
	 * Get position.
	 * 
	 * @return Position in [m]
	 */
	public Vector2 getPosition()
	{
		return Vector2.fromXY(curPosition[0] / 1000.0, curPosition[1] / 1000.0);
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
		return curPosition[0] != UNUSED_FIELD;
		
	}
	
	
	/**
	 * @return
	 */
	public boolean isVelocityValid()
	{
		return curVelocity[0] != UNUSED_FIELD;
		
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
			
			return (bat - BAT_3S_MIN_VOLTAGE) / (BAT_3S_MAX_VOLTAGE - BAT_3S_MIN_VOLTAGE);
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
		
		return (bat - BAT_4S_MIN_VOLTAGE) / (BAT_4S_MAX_VOLTAGE - BAT_4S_MIN_VOLTAGE);
	}
	
	
	/**
	 * @return
	 */
	public boolean isBarrierInterrupted()
	{
		return (barrierKickCounter & 0x80) == 0x80;
	}
	
	
	/**
	 * @param interrupted
	 */
	public void setBarrierInterrupted(boolean interrupted)
	{
		if (interrupted)
		{
			barrierKickCounter |= 0x80;
		} else
		{
			barrierKickCounter &= (0x80 ^ 0xFFFF);
		}
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
		return (features & feature.getId()) != 0;
		
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
	
	
	/**
	 * @return
	 */
	public ERobotMode getRobotMode()
	{
		int mode = features >> 12;
		
		return ERobotMode.getRobotModeConstant(mode);
	}
	
	
	/**
	 * @param xy [mm]
	 * @param z [rad]
	 */
	public void setCurPosition(final IVector2 xy, final double z)
	{
		curPosition[0] = (int) xy.x();
		curPosition[1] = (int) xy.y();
		curPosition[2] = (int) (z * 1e3);
	}
	
	
	/**
	 * @param vel [m/s]
	 */
	public void setCurVelocity(final IVector3 vel)
	{
		curVelocity[0] = (int) (vel.x() * 1e3);
		curVelocity[1] = (int) (vel.y() * 1e3);
		curVelocity[2] = (int) (vel.z() * 1e3);
	}
	
	
	public void setKickerLevel(final double kickerLevel)
	{
		this.kickerLevel = (int) kickerLevel;
	}
	
	
	/**
	 * @param dribblerSpeed [RPM]
	 */
	public void setDribblerSpeed(final double dribblerSpeed)
	{
		this.dribblerSpeed = (int) dribblerSpeed;
	}
	
	
	public void setBatteryLevel(final double batteryLevel)
	{
		this.batteryLevel = (int) (batteryLevel * 1e3);
	}
	
	
	public void setHardwareId(final int hardwareId)
	{
		this.hardwareId = hardwareId;
	}
	
	
	public void setDribblerTemp(final double dribblerTemp)
	{
		this.dribblerTemp = (int) (dribblerTemp * 2.0);
	}
}
