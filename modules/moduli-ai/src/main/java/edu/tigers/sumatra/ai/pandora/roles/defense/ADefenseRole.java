/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.DefenseConstants;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;


/**
 * Abstract defense role
 */
public abstract class ADefenseRole extends ARole implements IDefenderRole
{
	@Configurable(comment = "Dribbling speed for defenders", defValue = "10000.0")
	private static double dribblerSpeed = 10_000;
	@Configurable(comment = "Kicker speed to chip the ball (note: this is not adapted to robot speed)", defValue = "3.0")
	private static double configurableKickSpeed = 3.0;


	public ADefenseRole(final ERole type)
	{
		super(type);
	}


	@Override
	public IVector2 getValidPositionByIcing(final IVector2 pos)
	{
		if (getAiFrame().getTacticalField().isBallLeavingFieldGood())
		{
			ITube forbiddenZone = Tube.fromLineSegment(getBall().getTrajectory().getTravelLineSegment(),
					RuleConstraints.getStopRadius());
			if (forbiddenZone.isPointInShape(pos))
			{
				IVector2 possiblePos = forbiddenZone.nearestPointOutside(pos);
				if (Geometry.getPenaltyAreaOur().withMargin(DefenseConstants.getMinGoOutDistance())
						.isPointInShape(possiblePos))
				{
					IVector2 leadPoint = Line.fromPoints(forbiddenZone.startCenter(), forbiddenZone.endCenter())
							.nearestPointOnLine(possiblePos);
					possiblePos = LineMath.stepAlongLine(possiblePos, leadPoint, forbiddenZone.radius() * 2);
				}
				return possiblePos;
			}
		}
		return pos;
	}


	protected KickParams calcKickParams()
	{
		final double kickSpeed = calculateArmChipSpeedDuringDefense();
		return KickParams.chip(kickSpeed).withDribbleSpeed(kickSpeed > 0 ? dribblerSpeed : 0);
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
