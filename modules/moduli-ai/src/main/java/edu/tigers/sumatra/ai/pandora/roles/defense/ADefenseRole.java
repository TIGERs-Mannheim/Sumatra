/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;


/**
 * Abstract defense role
 */
public abstract class ADefenseRole extends ARole
{
	@Configurable(comment = "Kicker speed to chip the ball (note: this is not adapted to robot speed)", defValue = "3.0")
	private static double configurableKickSpeed = 3.0;


	public ADefenseRole(final ERole type)
	{
		super(type);
	}


	protected KickParams calcKickParams()
	{
		final double kickSpeed = calculateArmChipSpeedDuringDefense();
		return KickParams.chip(kickSpeed).withDribblerMode(kickSpeed > 0 ? EDribblerMode.DEFAULT : EDribblerMode.OFF);
	}


	private double calculateArmChipSpeedDuringDefense()
	{
		double redirectTargetAngle = RedirectConsultantFactory.createDefault().getTargetAngle(
				getBall().getVel(),
				Vector2.fromAngleLength(
						getBot().getOrientation(),
						RuleConstraints.getMaxKickSpeed()));
		IHalfLine ballTravel = Lines.halfLineFromDirection(getBot().getPos(), Vector2.fromAngle(redirectTargetAngle));

		if (ballTravel.intersectSegment(Geometry.getGoalTheir().getLineSegment()).isPresent() ||
				ballTravel.intersectSegment(Geometry.getGoalTheir().getGoalLine()).isPresent())
		{
			return configurableKickSpeed;
		}

		return 0;
	}
}
