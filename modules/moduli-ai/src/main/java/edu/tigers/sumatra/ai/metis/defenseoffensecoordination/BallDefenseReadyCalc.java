/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defenseoffensecoordination;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class BallDefenseReadyCalc extends ACalculator
{
	@Configurable(comment = "[m/s] Defender needs to be slower than this to be considered ready", defValue = "1.0")
	private static double defenderMaxAllowedSpeed = 1.0;
	@Configurable(comment = "[mm] Defender needs to be closer than this to be considered ready", defValue = "250.0")
	private static double maxDistanceDefenderIsConsideredClose = 250.0;

	private final Supplier<Set<BotID>> bestBallDefenderCandidates;
	private final Supplier<DefenseBallThreat> ballThreat;

	@Getter
	private boolean ballDefenseIsReady = false;


	@Override
	protected void doCalc()
	{
		ballDefenseIsReady = isDefenseReady();
		var offset = getAiFrame().getTeamColor() == ETeamColor.BLUE ? Vector2.zero() : Vector2.fromY(1.1);
		getShapes(EAiShapesLayer.BALL_DEFENSE_READY).add(new DrawableBorderText(Vector2.fromXY(1, 7.7).add(offset),
				String.format("BallDefenseReady: %b", ballDefenseIsReady))
				.setColor(getAiFrame().getTeamColor().getColor()));
	}


	private boolean isDefenseReady()
	{
		return bestBallDefenderCandidates.get().stream()
				.map(botID -> getWFrame().getBot(botID))
				.filter(this::botFollowsSpeedLimit)
				.filter(this::botIsBetweenBallAndGoal)
				.filter(this::botIsCloseToThreatLine)
				.count() >= bestBallDefenderCandidates.get().size() - 1;
	}


	private boolean botFollowsSpeedLimit(ITrackedBot bot)
	{
		return bot.getVel().getLength() < defenderMaxAllowedSpeed;
	}


	private boolean botIsBetweenBallAndGoal(ITrackedBot bot)
	{
		var goalLine = Geometry.getGoalOur().getGoalLine();
		return goalLine.distanceTo(bot.getPos()) + Geometry.getBotRadius() * 2 < goalLine.distanceTo(
				getBall().getPos());
	}


	private boolean botIsCloseToThreatLine(ITrackedBot bot)
	{
		return ballThreat.get().getThreatLine().distanceTo(bot.getPos()) < maxDistanceDefenderIsConsideredClose;
	}


}
