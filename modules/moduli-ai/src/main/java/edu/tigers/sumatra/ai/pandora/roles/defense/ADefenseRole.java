/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.util.Optional;

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
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;


/**
 * Abstract defense role
 */
public abstract class ADefenseRole extends ARole implements IDefenderRole
{
	
	@Configurable(comment = "Dribbling speed for defenders", defValue = "10000.0")
	private static double dribblerSpeed = 10_000;
	@Configurable(comment = "Kicker speed to chip the ball", defValue = "4.0")
	private static double configurableKickSpeed = 4;
	

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
	
	
	protected void armDefenders(AMoveSkill skill)
	{
		final double kickSpeed = calculateArmChipSpeedDuringDefense();
		skill.getMoveCon().setArmChip(kickSpeed);
		if (kickSpeed > 0)
		{
			skill.getMoveCon().setDribblerSpeed(dribblerSpeed);
		} else
		{
			skill.getMoveCon().setDribblerSpeed(0);
		}
	}
	
	
	private double calculateArmChipSpeedDuringDefense()
	{
		double redirectTargetAngle = RedirectConsultantFactory.createDefault(
				getBall().getVel(),
				Vector2.fromAngleLength(
						getBot().getOrientation(),
						RuleConstraints.getMaxBallSpeed()))
				.getTargetAngle();
		IHalfLine ballTravel = Lines.halfLineFromDirection(getBot().getPos(), Vector2.fromAngle(redirectTargetAngle));
		
		Optional<IVector2> target;
		target = ballTravel.intersectSegment(Geometry.getGoalTheir().getLineSegment());
		if (target.isPresent())
		{
			return RuleConstraints.getMaxBallSpeed();
		}
		target = ballTravel.intersectSegment(Geometry.getGoalTheir().getGoalLine());
		if (target.isPresent())
		{
			return configurableKickSpeed;
		}
		return 0;
	}
}
