package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class DisruptOpponentRole extends ARole
{
	@Configurable(defValue = "1.5", comment = "val < 1: very aggressive, val > 1: less aggressive, val > 2: no contact!, val == 0: broken robots", unit = EConfigUnit.BOT_RADIUS)
	private static double aggressiveness = 1.5;


	public DisruptOpponentRole()
	{
		super(ERole.DISRUPT_OPPONENT);
		setInitialState(new DefaultState());
	}


	private class DefaultState extends MoveState
	{
		PositionValidator positionValidator = new PositionValidator();


		@Override
		protected void onEntry()
		{
			skill.setMoveCon(skill.getMoveCon().withBallObstacle(false));
			positionValidator = new PositionValidator();
			positionValidator.getMarginToPenArea().put(ETeam.BOTH, Geometry.getBotRadius() * 2.5);
		}


		@Override
		protected void onUpdate()
		{
			super.onUpdate();
			positionValidator.update(getWFrame(), skill.getMoveCon());
			skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE));

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
