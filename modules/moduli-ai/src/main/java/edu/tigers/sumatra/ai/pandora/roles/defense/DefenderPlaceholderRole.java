/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.statemachine.AState;


/**
 * A placeholder role for new defenders
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DefenderPlaceholderRole extends ADefenseRole
{
	private IVector2 target = null;


	/**
	 * Default instance
	 */
	public DefenderPlaceholderRole()
	{
		super(ERole.DEFENDER_PLACEHOLDER);
		setInitialState(new MoveState());
	}


	public IVector2 getTarget()
	{
		return target;
	}


	public void setTarget(final IVector2 target)
	{
		this.target = target;
	}


	private class MoveState extends AState
	{
		private MoveToSkill skill;


		@Override
		public void doEntryActions()
		{
			skill = MoveToSkill.createMoveToSkill();
			setNewSkill(skill);
			skill.getMoveCon().setGameStateObstacle(!getAiFrame().getGameState().isStop());
			skill.getMoveCon().setBallObstacle(false);
		}


		@Override
		public void doUpdate()
		{
			if (target != null)
			{
				skill.updateDestination(target);
				double targetAngle = target.subtractNew(Geometry.getGoalOur().getCenter()).getAngle();
				skill.updateTargetAngle(targetAngle);
			}
		}
	}
}
