/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;
import lombok.Setter;


/**
 * A placeholder role for new defenders
 */
public class DefenderPlaceholderRole extends ADefenseRole
{
	@Getter
	@Setter
	private IVector2 target = null;


	/**
	 * Default instance
	 */
	public DefenderPlaceholderRole()
	{
		super(ERole.DEFENDER_PLACEHOLDER);
		setInitialState(new DefaultState());
	}


	private class DefaultState extends MoveState
	{
		@Override
		protected void onInit()
		{
			skill.getMoveCon().setGameStateObstacle(!getAiFrame().getGameState().isStop());
			skill.getMoveCon().setBallObstacle(false);
		}


		@Override
		protected void onUpdate()
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
