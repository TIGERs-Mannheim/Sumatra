/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.BallHandlingAdvise;
import edu.tigers.sumatra.skillsystem.skills.EBallHandlingSkillMoveAdvise;
import edu.tigers.sumatra.skillsystem.skills.EBallHandlingSkillTurnAdvise;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Determines in which direction the kickSkill or any other ballHandling skill should
 * turn to avoid being blocked by opponent bots.
 */
@RequiredArgsConstructor
public class BallHandlingSkillMovementCalc extends ACalculator
{
	@Configurable(defValue = "300.0", comment = "[mm] max dist to consider opponent bots")
	private static double maxOpponentDist = 300.0;

	private final Supplier<DribbleToPos> dribbleToPos;
	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;
	private final Supplier<List<BotID>> ballHandlingBots;

	@Getter
	private BallHandlingAdvise ballHandlingAdvise;


	private EBallHandlingSkillMoveAdvise calcMoveAdvise()
	{
		var botId = ballHandlingBots.get().getFirst();
		var bot = getWFrame().getBot(botId);
		var botLookDir = Vector2.fromAngle(bot.getOrientation());
		var opponentPos = dribbleToPos.get().getProtectFromPos();
		var bot2Opponent = opponentPos.subtractNew(bot.getPos());

		double dot = botLookDir.normalizeNew().scalarProduct(bot2Opponent.normalizeNew());
		if (dot > 0)
		{
			return EBallHandlingSkillMoveAdvise.PULL;
		}
		return EBallHandlingSkillMoveAdvise.PUSH;
	}


	private EBallHandlingSkillTurnAdvise calcTurnAdvise()
	{
		var botId = ballHandlingBots.get().getFirst();
		var target = getKickTarget(botId);
		if (target.isEmpty())
		{
			return EBallHandlingSkillTurnAdvise.NONE;
		}

		var bot = getWFrame().getBot(botId);
		var bot2Opponent = dribbleToPos.get().getProtectFromPos().subtractNew(bot.getPos());
		var botLookDir = Vector2.fromAngle(bot.getOrientation());
		double opponentAngle = botLookDir.angleTo(bot2Opponent).orElse(0.0);
		var bot2Target = target.get().subtractNew(bot.getPos());
		double targetAngle = botLookDir.angleTo(bot2Target).orElse(0.0);
		boolean opponentIsInBetween = SumatraMath.isBetween(
				opponentAngle,
				Math.min(0, targetAngle),
				Math.max(0, targetAngle));

		if (Math.abs(targetAngle) > AngleMath.deg2rad(15) &&
				opponentIsInBetween &&
				Math.signum(targetAngle) == Math.signum(opponentAngle))
		{
			if (targetAngle > 0)
			{
				return EBallHandlingSkillTurnAdvise.RIGHT;
			} else
			{
				return EBallHandlingSkillTurnAdvise.LEFT;
			}
		}
		return EBallHandlingSkillTurnAdvise.NONE;
	}


	@Override
	public void doCalc()
	{
		// determine ball handling advise
		ballHandlingAdvise = determineBallHandlingAdvise();

		// draw debug shapes
		getShapes(EAiShapesLayer.OFFENSE_KICK_MOVEMENT).add(
				new DrawableAnnotation(getBall().getPos(),
						"turn: " + ballHandlingAdvise.getTurnAdvise() + " move:" + ballHandlingAdvise.getMoveAdvise())
		);
	}


	private BallHandlingAdvise determineBallHandlingAdvise()
	{
		var closestOpponent = getWFrame().getOpponentBots().values().stream()
				.map(ITrackedBot::getBotKickerPos)
				.min(Comparator.comparingDouble(bot -> bot.distanceTo(getBall().getPos())));

		if (closestOpponent.isEmpty() ||
				closestOpponent.get().distanceTo(getBall().getPos()) > maxOpponentDist ||
				ballHandlingBots.get().isEmpty())
		{
			// no specific ball handling needed. Create default behavior.
			return new BallHandlingAdvise();
		}

		// determine situational aware behavior
		return new BallHandlingAdvise(calcMoveAdvise(), calcTurnAdvise());
	}


	private Optional<IVector2> getKickTarget(BotID botId)
	{
		if (!offensiveActions.get().containsKey(botId))
		{
			return Optional.empty();
		}
		var kick = offensiveActions.get().get(botId).getAction().getKick();
		return Optional.ofNullable(kick).map(Kick::getTarget);
	}
}
