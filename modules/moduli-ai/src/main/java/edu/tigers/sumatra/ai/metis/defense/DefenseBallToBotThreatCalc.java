package edu.tigers.sumatra.ai.metis.defense;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallToBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
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


/**
 * Calculate threats from ball to an opponent bot that should be man-marked.
 */
public class DefenseBallToBotThreatCalc extends ADefenseThreatCalc
{
	@Configurable(comment = "minimal distance to ball (excluding bot+ball radii)", defValue = "100.0")
	private static double minDistanceToBall = 100.0;

	@Configurable(comment = "minimal distance to marked opponent (excluding bot radii)", defValue = "50.0")
	private static double minDistanceToOpponent = 50.0;

	@Configurable(comment = "maximum distance to marked opponent", defValue = "500.0")
	private static double maxDistanceToOpponent = 500.0;

	@Configurable(defValue = "true", comment = "Enable ball -> bot threats (man to man marking)")
	private static boolean enabled = true;

	@Configurable(defValue = "1", comment = "Number of man markers to assign")
	private static int numMarkers = 1;


	@Override
	protected void doCalc()
	{
		if (!enabled
				|| getNewTacticalField().getEnemiesToBallDist().isEmpty()
				|| getAiFrame().getGamestate().isStandardSituationForUs()
				|| getAiFrame().getGamestate().isBallPlacement())
		{
			return;
		}

		final BotID nearestOpponentToBall = getNewTacticalField().getEnemiesToBallDist().get(0).getBot().getBotId();
		final List<DefenseBotThreat> consideredThreats = getNewTacticalField().getDefenseBotThreats().stream()
				.filter(t -> t.getBotID() != nearestOpponentToBall)
				.collect(Collectors.toList());

		final List<DefenseBallToBotThreat> threats = new ArrayList<>();
		int maxMarkers = Math.min(consideredThreats.size(), numMarkers);
		for (int i = 0; i < maxMarkers; i++)
		{
			final BotID nextOpponent = consideredThreats.get(i).getBotID();
			final ITrackedBot bot = getWFrame().getBot(nextOpponent);
			final DefenseBallToBotThreat threat = defenseBotThreat(bot);

			if (threat.getProtectionLine().isPresent())
			{
				threats.add(threat);
			}
		}

		getNewTacticalField().setDefenseBallToBotThreats(threats);
		threats.forEach(this::drawThreat);
	}


	private DefenseBallToBotThreat defenseBotThreat(ITrackedBot bot)
	{
		final ILineSegment threatLine = threatLine(bot);
		final ILineSegment protectionLine = protectionLine(threatLine);
		return new DefenseBallToBotThreat(bot, threatLine, protectionLine, getBall().getVel());
	}


	private ILineSegment threatLine(final ITrackedBot bot)
	{
		return Lines.segmentFromPoints(getBall().getPos(), predictedOpponentPos(bot));
	}


	private IVector2 predictedOpponentPos(final ITrackedBot bot)
	{
		return bot.getPosByTime(DefenseConstants.getLookaheadBotThreats(bot.getVel().getLength()));
	}


	private ILineSegment protectionLine(final ILineSegment threatLine)
	{
		final double minDistToBall = getAiFrame().getGamestate().isRunning()
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
				|| Geometry.getPenaltyAreaOur().isIntersectingWithLine(threatDefendingLine))
		{
			return null;
		}

		return threatDefendingLine;
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
		final IVector2 placePos = getAiFrame().getGamestate().getBallPlacementPositionForUs();
		if (getAiFrame().getGamestate().isBallPlacement() && placePos != null)
		{
			final IVector2 ballPos = getBall().getPos();
			final double radius = RuleConstraints.getStopRadius() + Geometry.getBotRadius();
			ITube placementTube = Tube.create(ballPos, placePos, radius);
			return placementTube.isPointInShape(threatDefendingLine.getEnd())
					|| placementTube.isIntersectingWithLine(threatDefendingLine);
		}
		return false;
	}
}
