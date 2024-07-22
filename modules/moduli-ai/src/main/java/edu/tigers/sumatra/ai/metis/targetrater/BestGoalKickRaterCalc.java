/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.referee.gameevent.AttackerTouchedBallInDefenseArea;
import edu.tigers.sumatra.referee.gameevent.BotCrashUnique;
import edu.tigers.sumatra.referee.gameevent.BotKickedBallTooFast;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Calculates the best goal kick target on the goal for from the point where the ball is or will be when received or redirected.
 * The result is a target in the opponents goal and a probability a shot to this target will score a goal.
 */
@RequiredArgsConstructor
public class BestGoalKickRaterCalc extends ACalculator
{
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;

	@Getter
	private Map<BotID, GoalKick> bestGoalKickPerBot = Collections.emptyMap();

	@Getter
	private GoalKick bestGoalKick;

	private BestGoalKickRater rater = new BestGoalKickRater();


	@Override
	public void doCalc()
	{

		rater.update(getWFrame(), bestGoalKickPerBot);
		rater.setWaitTime(waitTimeToNextValidGoal());
		bestGoalKickPerBot = kickOrigins.get().values().stream()
				.map(rater::rateKickOrigin)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toUnmodifiableMap(e -> e.getKickOrigin().getShooter(), e -> e));


		bestGoalKick = bestGoalKickPerBot.values().stream()
				.max(Comparator.comparingDouble(g -> g.getRatedTarget().getScore()))
				.orElse(null);

		getShapes(EAiShapesLayer.AI_BEST_GOAL_KICK).addAll(rater.getShapes());
	}


	private double waitTimeToNextValidGoal()
	{
		for (var event : getAiFrame().getRefereeMsg().getGameEvents())
		{
			var team = switch (event.getType())
					{
						case ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA -> ((AttackerTouchedBallInDefenseArea) event).getTeam();
						case BOT_KICKED_BALL_TOO_FAST -> ((BotKickedBallTooFast) event).getTeam();
						case BOT_CRASH_UNIQUE -> ((BotCrashUnique) event).getTeam();
						case BOT_CRASH_DRAWN -> getWFrame().getTeamColor();
						default -> getWFrame().getTeamColor().opposite();
					};
			if (team.equals(getWFrame().getTeamColor()))
			{
				long created = event.getCreatedTimestamp();
				long now = getWFrame().getTimestamp();
				double alreadyWaited = (now - created) * 1e-9;
				if (alreadyWaited > RuleConstraints.getGracePeriod())
				{
					continue;
				}
				return SumatraMath.max(RuleConstraints.getGracePeriod() - alreadyWaited, 0);
			}
		}
		return 0;
	}


}
