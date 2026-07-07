package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatReductionRater;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Builder;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Builder
public record DefenseBotThreatDefStrategyData(
		EDefenseBotThreatDefStrategy type,
		BotID threatID,
		IVector2 threatPos,
		IVector2 threatVel,
		ILineSegment threatLine,
		IVector2 protectionPos,
		ILineSegment protectionLine,
		ILineSegment protectionLineIfGetBallThreat,
		ILineSegment futureProtectionLineIfGetsBallThreat,
		double maxDistToProtectLine,
		double bestDistToThreat
)
{
	private static final List<Color> SHAPE_COLORS = List.of(
			Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE,
			Color.MAGENTA
	);


	public static DefenseBotThreatDefStrategyData create(
			EDefenseBotThreatDefStrategy type,
			ITrackedBot bot,
			IVector2 threatPos,
			ILineSegment threatLine,
			ILineSegment protectionLine,
			ILineSegment protectionLineIfGetsBallThreat,
			DefenseBallThreat ball
	)
	{
		var bestBlockDist = switch (type)
		{
			case CENTER_BACK -> DefenseMath.calculateGoalDefDistance(threatPos, Geometry.getBotRadius());
			default -> 0;
		};
		var maxDist = SumatraMath.min(
				Optional.ofNullable(protectionLine).map(pl -> pl.distanceTo(ball.getPos()))
						.orElse(Double.POSITIVE_INFINITY),
				threatPos.distanceTo(ball.getPos())
		) / DefenseThreatReductionRater.getOpponentPassingSpeedAdvantage();

		var builder = DefenseBotThreatDefStrategyData.builder()
				.type(type)
				.threatID(bot.getBotId())
				.threatPos(threatPos)
				.threatVel(bot.getVel())
				.threatLine(threatLine)
				.maxDistToProtectLine(maxDist)
				.bestDistToThreat(bestBlockDist);

		var offset = bot.getVel().multiplyNew(1000 * DefenseThreatReductionRater.getDefenderPosLookahead());
		if (protectionLine != null)
		{
			builder.protectionPos(protectionLine.getPathStart());
			builder.protectionLine(protectionLine);
		}
		if (protectionLineIfGetsBallThreat != null)
		{
			builder.protectionLineIfGetBallThreat(protectionLineIfGetsBallThreat);
			builder.futureProtectionLineIfGetsBallThreat(Lines.segmentFromPoints(
					protectionLineIfGetsBallThreat.getPathStart().addNew(offset),
					protectionLineIfGetsBallThreat.getPathEnd().addNew(offset)
			));
		}

		return builder.build();
	}


	public boolean isComplete()
	{
		return threatLine != null && protectionLine != null && protectionPos != null
				&& futureProtectionLineIfGetsBallThreat != null;
	}


	public Stream<IDrawableShape> drawShapes(int index)
	{
		var color = SHAPE_COLORS.get(index % SHAPE_COLORS.size());
		if (protectionLine != null && protectionLineIfGetBallThreat != null
				&& futureProtectionLineIfGetsBallThreat != null)
		{
			return Stream.of(
					new DrawableTube(
							Tube.create(futureProtectionLineIfGetsBallThreat, maxDistToProtectLine), color.darker().darker()),
					new DrawableLine(protectionLine, color),
					new DrawableTube(Tube.create(protectionLineIfGetBallThreat, maxDistToProtectLine), color),
					new DrawableArrow(
							protectionLine.getPathStart(), protectionLine.directionVector().scaleToNew(-maxDistToProtectLine),
							color
					)
			);
		}
		return Stream.of();
	}
}
