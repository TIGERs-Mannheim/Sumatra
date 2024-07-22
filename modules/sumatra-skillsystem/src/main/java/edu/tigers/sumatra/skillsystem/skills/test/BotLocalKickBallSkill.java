/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.botmanager.botskills.BotSkillKickBall;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


@Log4j2
@RequiredArgsConstructor
public class BotLocalKickBallSkill extends AMoveSkill
{
	@Setter
	private IVector2 targetPos = Vector2.zero();

	@Setter
	private KickParams desiredKickParams = KickParams.disarm();


	@Override
	protected void doUpdate()
	{
		setKickParams(desiredKickParams);

		super.doUpdate();

		BotSkillKickBall botSkill = new BotSkillKickBall(getTBot().getMoveConstraints());
		botSkill.setBallPos(getWorldFrame().isInverted() ? getBall().mirrored().getPos() : getBall().getPos());
		botSkill.setFieldSize(Vector2.fromXY(Geometry.getFieldLength(), Geometry.getFieldWidth()));
		botSkill.setTargetPos(targetPos);
		getMatchCtrl().setSkill(botSkill);
	}


	/**
	 * Setter for instanceables. Setting desiredKickParams directly should be preferred.
	 *
	 * @param kickSpeed
	 */
	public void setKickSpeed(double kickSpeed)
	{
		desiredKickParams = KickParams.of(EKickerDevice.STRAIGHT, kickSpeed)
				.withDribblerMode(desiredKickParams.getDribblerMode());
	}


	/**
	 * Setter for instanceables. Setting desiredKickParams directly should be preferred.
	 *
	 * @param enable
	 */
	public void setDribbler(boolean enable)
	{
		desiredKickParams = desiredKickParams.withDribblerMode(enable ? EDribblerMode.HIGH_POWER : EDribblerMode.OFF);
	}
}
