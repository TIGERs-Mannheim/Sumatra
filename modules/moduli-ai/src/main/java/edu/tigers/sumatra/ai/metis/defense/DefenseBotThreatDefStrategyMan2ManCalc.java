/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefStrategyData;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBotThreatDefStrategy;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Calculate threats from ball to an opponent bot that not only can, but also should be man-marked.
 */
@RequiredArgsConstructor
public class DefenseBotThreatDefStrategyMan2ManCalc extends ADefenseThreatCalc
{
	@Configurable(comment = "minimal distance to ball (excluding bot+ball radii)", defValue = "100.0")
	private static double minDistanceToBall = 100.0;

	@Configurable(comment = "minimal distance to marked opponent (excluding bot radii)", defValue = "50.0")
	private static double minDistanceToOpponent = 50.0;

	@Configurable(comment = "maximum distance to marked opponent", defValue = "500.0")
	private static double maxDistanceToOpponent = 500.0;

	@Configurable(defValue = "true", comment = "Enable ball -> bot threats (man to man marking)")
	private static boolean enabled = true;

	private final Supplier<List<DefenseBotThreatDefStrategyData>> centerBackDefData;
	private final Supplier<BotDistance> opponentClosestToBall;

	@Getter
	private List<DefenseBotThreatDefStrategyData> man2ManDefData;


	@Override
	protected boolean isCalculationNecessary()
	{
		return enabled
				&& opponentClosestToBall.get().getBotId().isBot()
				&& getAiFrame().getGameState().isRunning();
	}


	@Override
	protected void reset()
	{
		man2ManDefData = Collections.emptyList();
	}


	@Override
	protected void doCalc()
	{
		final BotID nearestOpponentToBall = opponentClosestToBall.get().getBotId();

		man2ManDefData = centerBackDefData.get().stream()
				.map(DefenseBotThreatDefStrategyData::threatID)
				.filter(threatID -> threatID != nearestOpponentToBall)
				.map(this::buildDefenseBotThreatDefStrategyData)
				.flatMap(Optional::stream)
				.toList();
	}


	private ILineSegment threatLine(final ITrackedBot bot)
	{
		return Lines.segmentFromPoints(getBall().getPos(), predictedOpponentPos(bot));
	}


	private Optional<ILineSegment> protectionLine(final ILineSegment threatLine)
	{
		final double minDistToBall = getAiFrame().getGameState().isRunning()
				? minDistanceToBall
				: Math.max(minDistanceToBall, RuleConstraints.getStopRadius());
		final double startMargin = minDistToBall + Geometry.getBallRadius() + Geometry.getBotRadius();
		final double endMargin = minDistanceToOpponent + Geometry.getBotRadius() * 2;
		IVector2 start = threatLine.getStart().addNew(threatLine.directionVector().scaleToNew(startMargin));
		IVector2 end = threatLine.getEnd().addNew(threatLine.directionVector().scaleToNew(-endMargin));
		end = adaptEndToFieldBoundaries(start, end);
		start = reduceStartToMaxDistanceToOpponent(start, end);

		final ILineSegment threatDefendingLine = Lines.segmentFromPoints(start, end);

		if (hindersBallPlacement(threatDefendingLine)
				|| nonPositiveProtectionLineLength(threatLine, threatDefendingLine)
				|| Geometry.getPenaltyAreaOur().isIntersectingWithLine(threatDefendingLine)
				|| Geometry.getPenaltyAreaOur().isPointInShape(threatDefendingLine.getStart()))
		{
			return Optional.empty();
		}

		return Optional.of(threatDefendingLine);
	}


	private IVector2 adaptEndToFieldBoundaries(final IVector2 start, IVector2 end)
	{
		final IRectangle field = Geometry.getField().withMargin(-Geometry.getBotRadius() * 3);
		if (!field.isPointInShape(end))
		{
			final List<IVector2> points = field.lineIntersections(Lines.segmentFromPoints(start, end));
			if (!points.isEmpty())
			{
				end = end.nearestTo(points);
			}
		}
		return end;
	}


	private IVector2 reduceStartToMaxDistanceToOpponent(final IVector2 start, IVector2 end)
	{
		if (start.distanceTo(end) > maxDistanceToOpponent)
		{
			IVector2 dir = start.subtractNew(end).scaleTo(maxDistanceToOpponent);
			return end.addNew(dir);
		}
		return start;
	}


	private boolean nonPositiveProtectionLineLength(final ILineSegment threatLine,
			final ILineSegment threatDefendingLine)
	{
		return threatDefendingLine.getLength() < 1 ||
				!SumatraMath.isEqual(
						threatLine.directionVector().getAngle(0),
						threatDefendingLine.directionVector().getAngle(0));
	}


	private boolean hindersBallPlacement(final ILineSegment threatDefendingLine)
	{
		final IVector2 placePos = getAiFrame().getGameState().getBallPlacementPositionForUs();
		if (getAiFrame().getGameState().isBallPlacement() && placePos != null)
		{
			final IVector2 ballPos = getBall().getPos();
			final double radius = RuleConstraints.getStopRadius() + Geometry.getBotRadius();
			ITube placementTube = Tube.create(ballPos, placePos, radius);
			return placementTube.isPointInShape(threatDefendingLine.getEnd())
					|| placementTube.isIntersectingWithLine(threatDefendingLine);
		}
		return false;
	}


	private Optional<DefenseBotThreatDefStrategyData> buildDefenseBotThreatDefStrategyData(BotID threatID)
	{
		var bot = getWFrame().getBot(threatID);
		var threatLine = threatLine(getWFrame().getBot(threatID));
		var protectionLine = protectionLine(threatLine);

		return protectionLine.map(pl ->
				new DefenseBotThreatDefStrategyData(
						bot.getBotId(),
						threatLine,
						pl,
						threatLine.getEnd(),
						getWFrame().getBall().getVel(),
						pl.getEnd(),
						EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER
				)
		);
	}
}
