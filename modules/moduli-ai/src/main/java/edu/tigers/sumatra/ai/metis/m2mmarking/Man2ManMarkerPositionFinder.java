/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.m2mmarking;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Map;
import java.util.Optional;


public class Man2ManMarkerPositionFinder
{

	@Configurable(defValue = "70.0", comment = "Distance from bot center to protection line below with optimization on the protection line is allowed")
	private static double minDistToProtectionLine = 70.0;

	@Configurable(defValue = "true", comment = "Drive to ball line first, then towards marked bot")
	private static boolean moveToBallLineFirst = true;

	static
	{
		ConfigRegistration.registerClass("metis", Man2ManMarkerPositionFinder.class);
	}


	public IVector2 findMan2ManMarkerPosition(final Map<BotID, ITrackedBot> botMap, final BotID man2ManMarker,
			final IDefenseThreat threat)
	{
		final ITrackedBot man2ManMarkerBot = botMap.get(man2ManMarker);
		final ILineSegment protectionLine = threat.getProtectionLine().orElseThrow(IllegalStateException::new);
		IVector2 dest = findMan2ManMarkerPositionCandidate(protectionLine, man2ManMarkerBot);

		final double backOffDistance = Geometry.getBotRadius() * 3;
		final Optional<ITrackedBot> botBlockedBy = isBlockedByOwnBot(botMap, man2ManMarkerBot, dest, backOffDistance);

		return botBlockedBy.map(b -> makeRoomForAttacker(b, backOffDistance, protectionLine)).orElse(dest);
	}


	private IVector2 findMan2ManMarkerPositionCandidate(final ILineSegment protectionLine,
			final ITrackedBot man2ManMarkerBot)
	{
		if (!moveToBallLineFirst || protectionLine.distanceTo(man2ManMarkerBot.getPos()) < minDistToProtectionLine)
		{
			return protectionLine.getEnd();
		} else if (man2ManMarkerBot.getVel().getLength2() > 0.5)
		{
			final IHalfLine velLine = Lines.halfLineFromDirection(man2ManMarkerBot.getPos(), man2ManMarkerBot.getVel());
			double velAngle = velLine.directionVector().getAngle();
			double lineAngle = protectionLine.directionVector().getAngle();
			double angleDiff = Math.abs(AngleMath.difference(velAngle, lineAngle));
			if (angleDiff > AngleMath.DEG_045_IN_RAD && angleDiff < AngleMath.DEG_180_IN_RAD - AngleMath.DEG_045_IN_RAD)
			{

				var candidate = protectionLine.intersectHalfLine(velLine);
				if (candidate.isPresent())
				{
					return candidate.get();
				}
			}
		}
		return protectionLine.closestPointOnLine(man2ManMarkerBot.getPos());
	}


	private Optional<ITrackedBot> isBlockedByOwnBot(final Map<BotID, ITrackedBot> botMap,
			final ITrackedBot man2ManMarkerBot,
			final IVector2 idealProtectionDest, final double backOffDistance)
	{
		return botMap.values().stream()
				.filter(bot -> bot.getBotId() != man2ManMarkerBot.getBotId())
				.filter(b -> b.getPos().distanceTo(idealProtectionDest) < backOffDistance)
				.findAny();
	}


	private IVector2 makeRoomForAttacker(final ITrackedBot bot, final double backOffDistance,
			final ILineSegment protectionLine)
	{
		final IVector2 projectedBallPosOnProtectionLine = protectionLine.closestPointOnLine(bot.getPos());
		final IVector2 backOffPoint = projectedBallPosOnProtectionLine.addNew(
				protectionLine.directionVector().scaleToNew(backOffDistance));
		return protectionLine.closestPointOnLine(backOffPoint);
	}
}
