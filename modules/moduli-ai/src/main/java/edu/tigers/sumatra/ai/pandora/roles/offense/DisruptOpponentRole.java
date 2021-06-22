/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class DisruptOpponentRole extends ARole
{
	@Configurable(defValue = "1.5", comment = "val < 1: very aggressive, val > 1: less aggressive, val > 2: no contact!, val == 0: broken robots")
	private static double aggressiveness = 1.5;


	public DisruptOpponentRole()
	{
		super(ERole.DISRUPT_OPPONENT);
		setInitialState(new DefaultState());
	}


	private class DefaultState extends RoleState<MoveToSkill>
	{
		PositionValidator positionValidator = new PositionValidator();


		DefaultState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			super.onInit();
			skill.getMoveCon().setBallObstacle(false);
			positionValidator = new PositionValidator();
			positionValidator.getMarginToPenArea().put(ETeam.BOTH, Geometry.getBotRadius() * 2.5);
		}


		@Override
		protected void onUpdate()
		{
			super.onUpdate();
			positionValidator.update(getWFrame(), skill.getMoveCon());

			var information = getAiFrame().getTacticalField().getRedirectorDetectionInformation();
			if (information.getOpponentReceiver() == null)
			{
				return;
			}

			ITrackedBot receiverBot = getWFrame().getOpponentBot(information.getOpponentReceiver());
			IVector2 dest = receiverBot.getPos();

			// move dest a bit to avoid collisions
			IVector2 opponentToMe = getPos().subtractNew(dest);
			dest = dest.addNew(opponentToMe.scaleToNew(Geometry.getBotRadius() * aggressiveness));

			// validate
			dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
			skill.updateDestination(dest);
		}
	}
}
