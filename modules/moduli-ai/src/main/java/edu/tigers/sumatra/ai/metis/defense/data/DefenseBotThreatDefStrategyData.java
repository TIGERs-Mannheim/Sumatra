/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatReductionRater;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.List;


public record DefenseBotThreatDefStrategyData(
		EDefenseBotThreatDefStrategy type,
		BotID threatID,
		IVector2 threatPos,
		IVector2 threatVel,
		ILineSegment threatLine,
		IVector2 protectionPos,
		ILineSegment protectionLine,
		ILineSegment futureProtectionLine,
		double maxDistToProtectLine,
		double bestDistToThreat
)
{
	private static final List<Color> SHAPE_COLORS = List.of(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE,
			Color.MAGENTA);

	public static DefenseBotThreatDefStrategyData create(
			EDefenseBotThreatDefStrategy type,
			ITrackedBot bot,
			IVector2 threatPos,
			ILineSegment threatLine,
			ILineSegment protectionLine,
			DefenseBallThreat ball
	)
	{
		var bestBlockDist = switch (type)
		{
			case CENTER_BACK -> DefenseMath.calculateGoalDefDistance(threatPos, Geometry.getBotRadius());
			default -> 0;
		};
		var maxDist =
				threatPos.distanceTo(ball.getPos()) / DefenseThreatReductionRater.getOpponentPassingSpeedAdvantage();

		if (protectionLine == null)
		{
			return new DefenseBotThreatDefStrategyData(
					type,
					bot.getBotId(),
					threatPos,
					bot.getVel(),
					threatLine,
					null,
					null,
					null,
					maxDist,
					bestBlockDist
			);
		}

		var offset = bot.getVel().multiplyNew(1000 * DefenseThreatReductionRater.getDefenderPosLookahead());
		var futureProtectLine = Lines.segmentFromPoints(
				protectionLine.getPathStart().addNew(offset),
				protectionLine.getPathEnd().addNew(offset)
		);
		return new DefenseBotThreatDefStrategyData(
				type,
				bot.getBotId(),
				threatPos,
				bot.getVel(),
				threatLine,
				protectionLine.getPathStart(),
				protectionLine,
				futureProtectLine,
				maxDist,
				bestBlockDist
		);
	}


	public boolean isComplete()
	{
		return threatLine != null && protectionLine != null && protectionPos != null && futureProtectionLine != null;
	}


	public List<IDrawableShape> drawShapes(int index)
	{
		var color = SHAPE_COLORS.get(index % SHAPE_COLORS.size());
		if (protectionLine != null && futureProtectionLine != null)
		{
			return List.of(
					new DrawableTube(Tube.create(futureProtectionLine, maxDistToProtectLine), color.darker().darker()),
					new DrawableTube(Tube.create(protectionLine, maxDistToProtectLine), color)
			);
		}
		return List.of();
	}
}
