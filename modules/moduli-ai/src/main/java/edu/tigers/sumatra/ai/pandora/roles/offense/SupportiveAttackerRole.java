/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import org.apache.commons.lang.Validate;


/**
 * Support the primary attacker by moving to supportive positions.
 */
public class SupportiveAttackerRole extends ARole
{
	@Configurable(defValue = "20.0", comment = "[mm] moves block point inside opponent pos. Higher number, more aggressive")
	private static double aggressiveness = 20;


	public SupportiveAttackerRole()
	{
		super(ERole.SUPPORTIVE_ATTACKER);

		setInitialState(new DefaultState());
	}


	private class DefaultState extends MoveState
	{
		@Override
		protected void onUpdate()
		{
			IVector2 movePos = getTacticalField().getSupportiveAttackers().get(getBotID());
			Validate.notNull(movePos);

			if (getPos().distanceTo(movePos) < Geometry.getBotRadius() * 2)
			{
				// start to push against the opponent
				var opponentPos = getWFrame().getBot(getTacticalField().getOpponentClosestToBall().getBotId()).getPos();
				var opponentToBlockPoint = movePos.subtractNew(opponentPos)
						.scaleTo(Geometry.getBotRadius() * 2 - aggressiveness);
				movePos = opponentPos.addNew(opponentToBlockPoint);
			}

			movePos = adjustMovePositionWhenItsInvalid(movePos);
			skill.updateDestination(movePos);
			skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.NORMAL);
			skill.updateLookAtTarget(getWFrame().getBall());
		}
	}
}
