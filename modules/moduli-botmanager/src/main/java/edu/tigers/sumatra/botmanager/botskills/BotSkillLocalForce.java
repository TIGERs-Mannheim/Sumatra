/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author AndreR
 */
public class BotSkillLocalForce extends AMoveBotSkill
{
	@SerialData(type = ESerialDataType.INT16)
	private final int[] force = new int[3];

	@SerialData(type = ESerialDataType.EMBEDDED)
	private KickerDribblerCommands kickerDribbler = new KickerDribblerCommands();


	public BotSkillLocalForce()
	{
		super(EBotSkill.LOCAL_FORCE);
	}


	/**
	 * Set force in bot local frame.
	 *
	 * @param xy [N]
	 * @param w [Nm]
	 */
	public BotSkillLocalForce(final IVector2 xy, final double w)
	{
		this();

		force[0] = (int) (xy.x() * 100.0);
		force[1] = (int) (xy.y() * 100.0);
		force[2] = (int) (w * 1000.0);
	}


	/**
	 * Set force in bot local frame.
	 * Used by bot skills panel.
	 *
	 * @param xy
	 * @param w
	 * @param dribbleSpeed
	 * @param dribbleCurrent
	 * @param kickSpeed
	 * @param kickDevice
	 * @param kickMode
	 */
	@SuppressWarnings({ "squid:S00107", "unused" })
	public BotSkillLocalForce(final IVector2 xy, final double w,
			final double dribbleSpeed, final double dribbleCurrent, final double kickSpeed, final EKickerDevice kickDevice,
			final EKickerMode kickMode)
	{
		this();

		force[0] = (int) (xy.x() * 100.0);
		force[1] = (int) (xy.y() * 100.0);
		force[2] = (int) (w * 1000.0);

		kickerDribbler.setDribbler(dribbleSpeed, dribbleCurrent);
		kickerDribbler.setKick(kickSpeed, kickDevice, kickMode);
	}


	/**
	 * @return
	 */
	public double getX()
	{
		return force[0] / 100.0;
	}


	/**
	 * @return
	 */
	public double getY()
	{
		return force[1] / 100.0;
	}


	/**
	 * @return
	 */
	public double getW()
	{
		return force[2] / 1000.0;
	}


	@Override
	public MoveConstraints getMoveConstraints()
	{
		return new MoveConstraints(new BotMovementLimits());
	}


	/**
	 * @return the kickerDribbler
	 */
	@Override
	public KickerDribblerCommands getKickerDribbler()
	{
		return kickerDribbler;
	}


	/**
	 * @param kickerDribbler the kickerDribbler to set
	 */
	@Override
	public void setKickerDribbler(final KickerDribblerCommands kickerDribbler)
	{
		this.kickerDribbler = kickerDribbler;
	}
}
