/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.pathfinder.obstacles.GenericCircleObstacle;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;


/**
 * Highly "coachable" supporter role, trigger different support behavior with different states.
 */
@Log4j2
public class SupportRole extends ARole
{
	@Setter
	private SupportBehaviorPosition target;
	@Setter
	private ESupportBehavior behavior;


	public SupportRole()
	{
		super(ERole.SUPPORT);
		setInitialState(new MoveToTargetState());
	}


	private class MoveToTargetState extends RoleState<MoveToSkill>
	{
		public MoveToTargetState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			double distanceToBall = RuleConstraints.getStopRadius() + Geometry.getBotRadius();
			var customBallObstacle = new GenericCircleObstacle(Circle.createCircle(getBall().getPos(), distanceToBall));
			skill.getMoveCon().setCustomObstacles(List.of(customBallObstacle));

			if (target != null)
			{
				skill.updateDestination(target.getPosition());
				target.getLookAt().ifPresent(skill::updateLookAtTarget);
			}
		}


		@Override
		public String getIdentifier()
		{
			if (behavior != null)
			{
				return behavior.toString();
			}
			return super.getIdentifier();
		}
	}
}
