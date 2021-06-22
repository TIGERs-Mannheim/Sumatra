/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.botskills.BotSkillKeeper;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Simple keeper skill that makes use of a bot skill.
 */
public class KeeperSkill extends AMoveSkill
{
	@Override
	public void doUpdate()
	{
		IVector2 ballPos = getBall().getPos().multiplyNew(getWorldFrame().isInverted() ? -1 : 1);
		IVector2 ballVel = getBall().getVel();
		double penAreaDepth = Geometry.getPenaltyAreaDepth();
		double goalWidth = Geometry.getGoalOur().getWidth();
		double goalOffset = -Geometry.getFieldLength() / 2 * (getWorldFrame().isInverted() ? -1 : 1);

		BotSkillKeeper skill = new BotSkillKeeper(ballPos, ballVel, penAreaDepth, goalWidth, goalOffset,
				defaultMoveConstraints());
		getMatchCtrl().setSkill(skill);
	}
}
