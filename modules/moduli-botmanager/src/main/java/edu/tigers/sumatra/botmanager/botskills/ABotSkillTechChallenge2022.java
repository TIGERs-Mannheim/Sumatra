/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;


import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.DriveKickerDribbler;
import edu.tigers.sumatra.botmanager.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Find a ball with onboard camera and get it on the dribbler.
 *
 * @author AndreR
 */
public abstract class ABotSkillTechChallenge2022 extends ABotSkill
{

	@SerialData(type = SerialData.ESerialDataType.INT16)
	protected final int[] targetPos = new int[2];

	@SerialData(type = SerialData.ESerialDataType.INT16)
	protected final int[] startPos = new int[2];

	@SerialData(type = SerialData.ESerialDataType.EMBEDDED)
	protected DriveKickerDribbler driveKickerDribbler = new DriveKickerDribbler();

	@SerialData(type = SerialData.ESerialDataType.UINT8)
	protected int rotationSpeed;


	protected ABotSkillTechChallenge2022(EBotSkill skill)
	{
		super(skill);
	}


	@SuppressWarnings("squid:S00107") // required for UI
	protected ABotSkillTechChallenge2022(final EBotSkill skill, final IVector2 startPos, final IVector2 targetPos,
			final double velMax, final double velMaxW, final double accMax, final double accMaxW,
			final double dribblerSpeed, final double dribblerCurrent, final double rotationSpeed)
	{
		this(skill);
		this.startPos[0] = (int) (startPos.x());
		this.startPos[1] = (int) (startPos.y());
		this.targetPos[0] = (int) (targetPos.x());
		this.targetPos[1] = (int) (targetPos.y());

		driveKickerDribbler.setVelMax(velMax);
		driveKickerDribbler.setVelMaxW(velMaxW);
		driveKickerDribbler.setAccMax(accMax);
		driveKickerDribbler.setAccMaxW(accMaxW);

		driveKickerDribbler.getKickerDribbler().setDribbler(dribblerSpeed, dribblerCurrent);

		setRotationSpeed(rotationSpeed);
	}


	/**
	 * Get ball, with move constraints.
	 */
	protected ABotSkillTechChallenge2022(final EBotSkill skill, final IVector2 startPos, final IVector2 targetPos,
			final IMoveConstraints mc,
			final double dribblerSpeed, final double dribblerCurrent, final double rotationSpeed)
	{
		this(skill, startPos, targetPos, mc.getVelMax(), mc.getVelMaxW(), mc.getAccMax(), mc.getAccMaxW(), dribblerSpeed,
				dribblerCurrent, rotationSpeed);
	}


	/**
	 * Get ball, with move constraints, unlimited search radius.
	 */
	protected ABotSkillTechChallenge2022(final EBotSkill skill, final IMoveConstraints mc, final double dribblerSpeed,
			final double dribblerCurrent, final double rotationSpeed)
	{
		this(skill, Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR, mc, dribblerSpeed, dribblerCurrent, rotationSpeed);
	}


	public final void setRotationSpeed(final double val)
	{
		rotationSpeed = DriveLimits.toUInt8(val, DriveLimits.MAX_VEL_W);
	}


	@Override
	public KickerDribblerCommands getKickerDribbler()
	{
		return driveKickerDribbler.getKickerDribbler();
	}


	@Override
	public void setKickerDribbler(final KickerDribblerCommands kickerDribbler)
	{
		driveKickerDribbler.setKickerDribbler(kickerDribbler);
	}

	@Override
	public MoveConstraints getMoveConstraints()
	{
		return driveKickerDribbler.getMoveConstraints();
	}
}