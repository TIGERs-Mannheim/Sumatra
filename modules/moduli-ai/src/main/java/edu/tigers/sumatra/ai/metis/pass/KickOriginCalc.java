/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class KickOriginCalc extends ACalculator
{
	private static final DecimalFormat DF = new DecimalFormat("0.00");
	private final Supplier<Map<BotID, RatedBallInterception>> ballInterceptions;
	private final Supplier<Boolean> ballStopped;
	private final Supplier<List<BotID>> ballHandlingBots;

	@Getter
	private Map<BotID, KickOrigin> kickOrigins;


	@Override
	protected void doCalc()
	{
		kickOrigins = findKickOrigins();

		kickOrigins.values().forEach(origin -> getShapes(EAiShapesLayer.KICK_ORIGIN)
				.add(new DrawableCircle(Circle.createCircle(origin.getPos(), 30)).setColor(Color.cyan)));
		kickOrigins.values().forEach(origin -> getShapes(EAiShapesLayer.KICK_ORIGIN)
				.add(new DrawableAnnotation(origin.getPos(), DF.format(origin.getImpactTime()))
						.withOffset(Vector2f.fromY(100)).setColor(Color.cyan)));
	}


	private Map<BotID, KickOrigin> findKickOrigins()
	{
		if (Boolean.TRUE.equals(ballStopped.get()))
		{
			if (canBallBeKicked())
			{
				return ballHandlingBots.get().stream()
						.collect(Collectors.toUnmodifiableMap(
								id -> id,
								id -> new KickOrigin(getBall().getPos(), id, Double.POSITIVE_INFINITY)
						));
			}
			return Collections.emptyMap();
		}
		return findInterceptingOrigins();
	}


	private Map<BotID, KickOrigin> findInterceptingOrigins()
	{
		return ballHandlingBots.get().stream()
				.map(id -> ballInterceptions.get().get(id))
				.filter(Objects::nonNull)
				.collect(Collectors.toUnmodifiableMap(
						e -> e.getBallInterception().getBotID(),
						bi -> new KickOrigin(bi.getBallInterception().getPos(), bi.getBallInterception().getBotID(),
								getImpactTime(bi.getBallInterception().getPos()))
				));
	}


	private boolean canBallBeKicked()
	{
		return !Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2)
				.isPointInShapeOrBehind(getBall().getPos())
				&& !Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBallRadius())
				.isPointInShapeOrBehind(getBall().getPos())
				&& Geometry.getField().isPointInShape(getBall().getPos());
	}


	private double getImpactTime(IVector2 pos)
	{
		return getBall().getTrajectory().getTimeByPos(pos);
	}
}
