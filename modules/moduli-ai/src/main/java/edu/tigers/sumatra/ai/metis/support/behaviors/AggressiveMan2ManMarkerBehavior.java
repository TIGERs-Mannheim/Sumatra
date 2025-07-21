/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatRater;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.m2mmarking.Man2ManMarkerPositionFinder;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Map;
import java.util.function.Supplier;

import static edu.tigers.sumatra.ai.metis.defense.data.EDefenseBallThreatSourceType.PASS_RECEIVE;


@RequiredArgsConstructor
public class AggressiveMan2ManMarkerBehavior extends ASupportBehavior
{
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean enabled = true;
	@Configurable(comment = "If validity is higher than this, the Behavior will be used", defValue = "0.3")
	private static double minValidity = 0.3;
	@Configurable(comment = "Weight for the redirect threat", defValue = "1.0")
	private static double weightRedirectThreat = 1.0;
	@Configurable(comment = "Weight for the PassDistance", defValue = "1.0")
	private static double weightPassDistance = 1.0;
	@Configurable(comment = "Constant to divide by to calculate validity", defValue = "2000.0")
	private static double validityDivisor = 2000.0;

	private final Supplier<BallPossession> ballPossession;
	private final Supplier<Map<BotID, DefenseBotThreat>> supporterToBotThreatMapping;
	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Man2ManMarkerPositionFinder man2ManMarkerPositionFinder = new Man2ManMarkerPositionFinder();


	@Override
	public SupportBehaviorPosition calculatePositionForRobot(BotID botID)
	{
		if (!supporterToBotThreatMapping.get().containsKey(botID))
		{
			return SupportBehaviorPosition.notAvailable();
		}
		if (!(ballPossession.get().getEBallPossession() == EBallPossession.THEY
				|| ballPossession.get().getEBallPossession() == EBallPossession.BOTH))
		{
			return SupportBehaviorPosition.notAvailable();
		}

		var threat = supporterToBotThreatMapping.get().get(botID);
		var dest = man2ManMarkerPositionFinder.findMan2ManMarkerPosition(getWFrame().getBots(), botID, threat);

		double validity = calcValidityScore(threat);

		var color = validity >= getMinValidity() ? Color.GREEN : Color.RED;
		getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_AGGRESSIVE_MAN_MARKER)
				.add(new DrawableCircle(Circle.createCircle(threat.getPos(), 1.5 * Geometry.getBotRadius()), color));
		getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_AGGRESSIVE_MAN_MARKER)
				.add(new DrawableLine(getWFrame().getBot(botID).getPos(), dest, color));
		if (validity >= getMinValidity())
		{
			return SupportBehaviorPosition.fromDestinationAndRotationTarget(
					dest, getWFrame().getBall().getPos(),
					validity
			);
		}

		return SupportBehaviorPosition.notAvailable();
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}


	private double calcValidityScore(DefenseBotThreat threat)
	{
		var validityDistanceScore = new DefenseThreatRater().calcPassDistanceScore(
				defenseBallThreat.get().getPos()
				, threat.getPos()
		);
		var validityRedirectAngleScore = new DefenseThreatRater().scoreRedirectTriangle(
				getWFrame().getBall().getPos(),
				defenseBallThreat.get().getPos(), threat.getPos()
		);

		if (defenseBallThreat.get().getSourceType() == PASS_RECEIVE)
		{
			return (weightPassDistance * validityDistanceScore + weightRedirectThreat * validityRedirectAngleScore) /
					(weightPassDistance + weightRedirectThreat);
		} else
		{
			return (weightPassDistance * validityDistanceScore) / (weightPassDistance);
		}
	}


	private double getMinValidity()
	{
		var gameState = getAiFrame().getGameState();
		if (gameState.isNextStandardSituationForThem() || gameState.isFreeKickForThem())
		{
			return 0;
		}
		return minValidity;
	}
}