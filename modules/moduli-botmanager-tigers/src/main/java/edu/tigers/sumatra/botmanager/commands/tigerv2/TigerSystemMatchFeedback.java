/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.tigerv2;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.bot.EDribblerState;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Synopsis of all relevant data from one bot.
 *
 * @author AndreR
 */
@Persistent
public class TigerSystemMatchFeedback extends ACommand implements IExportable
{
	private static final int UNUSED_FIELD = 0x7FFF;
	/**
	 * [mm], [mrad]
	 */
	@SerialData(type = ESerialDataType.INT16)
	private final int[] curPosition = new int[3];
	/**
	 * [mm/s], [mrad/s]
	 */
	@SerialData(type = ESerialDataType.INT16)
	private final int[] curVelocity = new int[3];
	/**
	 * [V]
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int kickerLevel;
	/**
	 * [V]
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int kickerMax;
	/**
	 * [rpm/100]
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int dribblerState;
	/**
	 * [dV]
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int batteryLevel;
	/**
	 * 0-255 = 0-100%
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int batteryPercent;
	/**
	 * Flags for: barrier interrupted, kick counter, dribbler temperature
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int flags;
	@SerialData(type = ESerialDataType.UINT16)
	private int features = 0x001F;
	@SerialData(type = ESerialDataType.UINT8)
	private int hardwareId;
	/**
	 * [ms] since frame capture
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int ballPosAge;
	/**
	 * [mm]
	 */
	@SerialData(type = ESerialDataType.INT16)
	private final int[] ballPosition = new int[2];


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
		List<Number> nbrs = new ArrayList<>(getPosition().getNumberList());
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
		nbrs.add(getDribblerState().getId());
		nbrs.add(features);
		nbrs.addAll(getBallPosition().getNumberList());
		nbrs.add(isBallPositionValid() ? 1 : 0);
		nbrs.add(getKickerPercentage());
		nbrs.add(getDribblerCurrent());
		return nbrs;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "pos_valid", "vel_valid",
				"kickerLevel", "dribbleSpeed", "batteryPercentage", "barrierInterrupted", "kickCounter", "dribblerState",
				"features", "ball_x", "ball_y", "ball_valid", "kickerPercentage", "dribbleCurrent");
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
	 * Get ball position as seen by the robot's camera.
	 *
	 * @return Position in [m] in global frame
	 */
	public Vector2 getBallPosition()
	{
		return Vector2.fromXY(ballPosition[0] / 1000.0, ballPosition[1] / 1000.0);
	}


	/**
	 * Age of ball position measurement.
	 *
	 * @return Age in [s]
	 */
	public double getBallAge()
	{
		return ballPosAge / 1000.0;
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
	public boolean isBallPositionValid()
	{
		return ballPosition[0] != UNUSED_FIELD;
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
	 * @return maximum kicker level in [V]
	 */
	public double getKickerMax()
	{
		return kickerMax;
	}


	/**
	 * Get kicker capacitor fill percentage.
	 *
	 * @return Kicker level in percentage (0-1)
	 */
	public double getKickerPercentage()
	{
		return (double) kickerLevel / (double) kickerMax;
	}


	/**
	 * @return the dribblerSpeed in [rpm]
	 */
	public double getDribblerSpeed()
	{
		return (dribblerState >> 2) * 500.0;
	}


	/**
	 * @return dribbler current in [A]
	 */
	public double getDribblerCurrent()
	{
		return (((flags & 0x0F) << 2) | (dribblerState & 0x03)) * 0.25;
	}


	public void setDribblerCurrent(final double current)
	{
		int bits = (int) ((current + 0.125) * 4.0);

		dribblerState &= 0xFC;
		dribblerState |= bits & 0x03;
		flags &= 0xF0;
		flags |= (bits >> 2) & 0x0F;
	}


	/**
	 * @return the batteryLevel in [V]
	 */
	public double getBatteryLevel()
	{
		return batteryLevel * 0.1;
	}


	/**
	 * Get remaining battery level.
	 *
	 * @return Battery level in percentage (0-1)
	 */
	public double getBatteryPercentage()
	{
		return batteryPercent / 255.0;
	}


	/**
	 * @return
	 */
	public boolean isBarrierInterrupted()
	{
		return (flags & 0x80) == 0x80;
	}


	/**
	 * @param interrupted
	 */
	public void setBarrierInterrupted(boolean interrupted)
	{
		if (interrupted)
		{
			flags |= 0x80;
		} else
		{
			flags &= (0x80 ^ 0xFFFF);
		}
	}


	/**
	 * Counter is flipped for every executed kick.
	 *
	 * @return
	 */
	public int getKickCounter()
	{
		return flags & 0x10;
	}


	/**
	 * @return the hardwareId
	 */
	public int getHardwareId()
	{
		return hardwareId;
	}


	/**
	 * @return state/temperature of dribbler
	 */
	public EDribblerState getDribblerState()
	{
		int state = (flags & 0x60) >> 5;

		return EDribblerState.getDribblerStateConstant(state);
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
	 * @param z  [rad]
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


	/**
	 * @param xy [mm]
	 */
	public void setBallPosition(final IVector2 xy)
	{
		ballPosition[0] = (int) xy.x();
		ballPosition[1] = (int) xy.y();
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
		dribblerState &= 0x03;
		dribblerState |= ((int) ((dribblerSpeed + 250.0) / 500.0)) << 2;
	}


	public void setBatteryLevel(final double batteryLevel)
	{
		this.batteryLevel = (int) (batteryLevel * 10.0);
	}


	public void setHardwareId(final int hardwareId)
	{
		this.hardwareId = hardwareId;
	}
}
