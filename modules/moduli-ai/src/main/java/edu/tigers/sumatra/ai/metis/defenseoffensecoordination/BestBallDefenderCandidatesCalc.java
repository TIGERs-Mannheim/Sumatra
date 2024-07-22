/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defenseoffensecoordination;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.DesiredDefendersCalcUtil;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Set;
import java.util.function.Supplier;


/**
 * This calculator creates a list of potential ball defenders which is one larger than the wanted amount of ball defender
 * The offense is only allowed to take one of those robots for it's purposes
 */
@RequiredArgsConstructor
public class BestBallDefenderCandidatesCalc extends ACalculator
{
	private final Supplier<Integer> numDefenderForBall;
	private final Supplier<DefenseBallThreat> ballThreat;
	private final DesiredDefendersCalcUtil util = new DesiredDefendersCalcUtil();

	@Getter
	private Set<BotID> bestBallDefenderCandidates;


	@Override
	protected void doCalc()
	{
		util.update(getAiFrame());
		var remainingDefenders = getWFrame().getTigerBotsAvailable().values().stream()
				.map(ITrackedBot::getBotId)
				.filter(bot -> getAiFrame().getKeeperId() != bot)
				.toList();
		bestBallDefenderCandidates = util.nextBestDefenders(ballThreat.get(), remainingDefenders,
				numDefenderForBall.get() + 1);
		var shapes = getShapes(EAiShapesLayer.DO_COORD_BEST_BALL_DEFENDER_CANDIDATES);
		bestBallDefenderCandidates.stream()
				.map(botID -> getWFrame().getBot(botID))
				.map(ITrackedBot::getPos)
				.map(pos -> new DrawableCircle(pos, Geometry.getBotRadius() + 25, Color.CYAN))
				.forEach(shapes::add);
	}
}
