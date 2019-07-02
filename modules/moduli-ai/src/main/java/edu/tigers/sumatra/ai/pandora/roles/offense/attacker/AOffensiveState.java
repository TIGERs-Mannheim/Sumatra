/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ARoleState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public abstract class AOffensiveState extends ARoleState
{
	@Configurable(defValue = "30.0", comment = "When distance to ball (without bot/ball radius) is small than this, protection is stopped")
	private static double minDistToOpponent = 60.0;

	@Configurable(defValue = "true")
	private static boolean avoidOpponents = true;

	@Configurable(defValue = "100.0", comment = "Threshold from penArea to opponent when bot is considered for extra margin to penArea")
	private static double marginForOpponent = 100;

	@Configurable(defValue = "300.0", comment = "Threshold from bot to opponent when extra margin is applied")
	private static double marginToOpponent = 300;

	@Configurable(defValue = "20.0", comment = "Margin to penArea to add to 3*botRadius")
	private static double extraMarginToPenArea = 20;

	@Configurable(defValue = "0.3", comment = "Lookahead on the bot trajectory")
	private static double attackerPosLookahead = 0.3;

	static
	{
		ConfigRegistration.registerClass("roles", AOffensiveState.class);
	}


	public AOffensiveState(final ARole role)
	{
		super(role);
	}


	public Optional<EOffensiveActionMove> getCurrentOffensiveActionMove()
	{
		return Optional.empty();
	}


	private double getOpponentToBallDist()
	{
		ITrackedBot opponentBot = getAiFrame().getTacticalField().getEnemyClosestToBall().getBot();
		if (opponentBot == null)
		{
			return 1e10;
		}
		return opponentBot.getPos().distanceTo(getBall().getPos()) - Geometry.getBotRadius() - Geometry.getBallRadius();
	}


	protected boolean ballPossessionIsThreatened(final double protectionDistanceGain)
	{
		double opponentToBallDist = getOpponentToBallDist();
		if (opponentToBallDist < minDistToOpponent
				|| getPos().distanceTo(getBall().getPos()) < Geometry.getBotRadius() + Geometry.getBallRadius() + 20)
		{
			// protection not reasonable anymore
			return false;
		}
		double protectorToBallDist = getPos().distanceTo(getBall().getPos());
		return protectorToBallDist * protectionDistanceGain > opponentToBallDist;
	}


	protected double getTimeBallNeedsToReachMe()
	{
		double ballDistToMe = getBot().getBotKickerPos().distanceTo(getBall().getPos());
		return getBall().getTrajectory().getTimeByDist(ballDistToMe);
	}


	protected boolean opponentInWay(double distance)
	{
		if (!avoidOpponents)
		{
			return false;
		}
		IVector2 p1 = getBall().getTrajectory().getTravelLine().closestPointOnLine(getPos());
		IVector2 p2 = getBall().getPos();
		ILineSegment line = Lines.segmentFromPoints(p1, p2);
		return getWFrame().getFoeBots().values().stream().anyMatch(b -> line.distanceTo(b.getPos()) < distance);
	}


	protected double getMarginToTheirPenArea()
	{
		double minMarginForOpponent = Geometry.getBotRadius() + marginForOpponent;
		double minMarginToOpponent = Geometry.getBotRadius() * 2 + marginToOpponent;
		double marginToPenArea = Geometry.getBotRadius() * 3 + extraMarginToPenArea;
		for (ITrackedBot opponentBot : getWFrame().getFoeBots().values())
		{
			final IPenaltyArea penaltyArea = Geometry.getPenaltyAreaTheir().withMargin(minMarginForOpponent);
			if (!penaltyArea.isPointInShape(opponentBot.getPos()))
			{
				continue;
			}
			final double distance = getBot().getPosByTime(attackerPosLookahead).distanceTo(opponentBot.getPos());
			if (distance < minMarginToOpponent)
			{
				return marginToPenArea;
			}
		}
		return 0;
	}
}
