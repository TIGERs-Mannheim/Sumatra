package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;


public class ProtectState extends AAttackerRoleState<ProtectiveGetBallSkill>
{
	@Configurable(defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean requireStrongDribblerContactForProtect = false;

	@Configurable(defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean useAggressiveObstacleAvoidance = false;

	static
	{
		ConfigRegistration.registerClass("roles", ProtectState.class);
	}

	private IVector2 protectionPos;


	public ProtectState(AttackerRole role)
	{
		super(ProtectiveGetBallSkill::new, role, EAttackerState.PROTECT);
	}


	@Override
	protected void doStandardUpdate()
	{
		if (useAggressiveObstacleAvoidance)
		{
			skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE));
		}
		var action = getRole().getAction();
		if (action != null)
		{
			skill.setStrongDribblerContactNeeded(
					action.getType() == EOffensiveActionType.DRIBBLE_KICK ||
							(requireStrongDribblerContactForProtect && action.getType() == EOffensiveActionType.PROTECT)
			);
		}

		if (protectionPos == null || !getRole().getBot().getBallContact().hasContact())
		{
			protectionPos = calcProtectionTarget();
		} // else keep old pos

		skill.setTarget(protectionPos);
	}


	private IVector2 calcProtectionTarget()
	{
		if (getRole().getBot().getBotKickerPos().distanceTo(getRole().getBall().getPos()) < Geometry.getBotRadius()
		&& getRole().getBot().getBallContact().hasContact())
		{
			// we are already very close to the ball. Just get it and dont try to turn around
			var botPos = getRole().getPos();
			var ballPos = getRole().getBall().getPos();
			return ballPos.addNew(ballPos.subtractNew(botPos));
		}
		var closestEnemyBotID = getRole().getAiFrame().getTacticalField().getOpponentClosestToBall().getBotId();
		IVector2 opponentPos;
		if (getRole().getWFrame().getOpponentBots().containsKey(closestEnemyBotID))
		{
			opponentPos = getRole().getWFrame().getOpponentBot(closestEnemyBotID).getPos();
		} else
		{
			opponentPos = Geometry.getGoalTheir().getCenter();
		}
		return opponentPos;
	}
}

