/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.util.Optional;
import java.util.Set;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Defender that protects near an opponent (marking the threat).
 */
public class ManToManMarkerRole extends AOuterDefenseRole
{
	@Configurable(defValue = "70.0", comment = "Distance from bot center to protection line below with optimization on the protection line is allowed")
	private static double minDistToProtectionLine = 70.0;

	@Configurable(defValue = "true", comment = "Drive to ball line first, then towards marked bot")
	private static boolean moveToBallLineFirst = true;


	public ManToManMarkerRole(final IDefenseThreat threat)
	{
		super(ERole.MAN_TO_MAN_MARKER, threat);

		setInitialState(new DefendState());
	}


	@Override
	protected Set<BotID> ignoredBots(IVector2 dest)
	{
		return closeOpponentBots(dest);
	}


	@Override
	protected IVector2 findDest()
	{
		final ILineSegment protectionLine = protectionLine();
		IVector2 dest = protectionLine.closestPointOnLine(getPos());
		if (!moveToBallLineFirst || protectionLine.distanceTo(getPos()) < minDistToProtectionLine)
		{
			dest = protectionLine.getEnd();
		} else if (getBot().getVel().getLength2() > 0.5)
		{
			final IHalfLine velLine = Lines.halfLineFromDirection(getPos(), getBot().getVel());
			double velAngle = velLine.directionVector().getAngle();
			double lineAngle = protectionLine.directionVector().getAngle();
			double angleDiff = Math.abs(AngleMath.difference(velAngle, lineAngle));
			if (angleDiff > AngleMath.DEG_045_IN_RAD && angleDiff < AngleMath.DEG_180_IN_RAD - AngleMath.DEG_045_IN_RAD)
			{
				dest = protectionLine.intersectHalfLine(velLine).orElse(dest);
			}
		}

		final double backOffDistance = Geometry.getBotRadius() * 3;
		final Optional<ITrackedBot> botBlockedBy = isBlockedByOwnBot(dest, backOffDistance);
		if (botBlockedBy.isPresent())
		{
			return makeRoomForAttacker(botBlockedBy.get(), backOffDistance);
		}
		return dest;
	}


	private Optional<ITrackedBot> isBlockedByOwnBot(final IVector2 idealProtectionDest, final double backOffDistance)
	{
		return getWFrame().getBots().values().stream()
				.filter(bot -> bot.getBotId() != getBotID())
				.filter(b -> b.getPos().distanceTo(idealProtectionDest) < backOffDistance)
				.findAny();
	}


	private IVector2 makeRoomForAttacker(final ITrackedBot bot, final double backOffDistance)
	{
		final ILineSegment protectionLine = protectionLine();
		final IVector2 projectedBallPosOnProtectionLine = protectionLine.closestPointOnLine(bot.getPos());
		final IVector2 backOffPoint = projectedBallPosOnProtectionLine.addNew(
				protectionLine.directionVector().scaleToNew(backOffDistance));
		return protectionLine.closestPointOnLine(backOffPoint);
	}
}
