/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.List;
import java.util.Optional;


public abstract class ADefenseThreatCalc extends ACalculator
{
	protected void drawThreat(IDefenseThreat threat)
	{
		final List<IDrawableShape> shapes = getShapes(EAiShapesLayer.DEFENSE_THREATS);
		final Optional<ILineSegment> protectionLine = threat.getProtectionLine();
		if (protectionLine.isPresent())
		{
			DrawableLine dProtectionLine = new DrawableLine(protectionLine.get(), Color.red);
			dProtectionLine.setStrokeWidth(30);
			shapes.add(dProtectionLine);
		}

		shapes.add(
				new DrawableLine(threat.getThreatLine(), Color.BLACK));
		shapes.add(
				new DrawableLine(threat.getPos(), Geometry.getGoalOur().getLeftPost(), Color.BLACK));
		shapes.add(
				new DrawableLine(threat.getPos(), Geometry.getGoalOur().getRightPost(), Color.BLACK));
	}


	protected Optional<ILineSegment> centerBackProtectionLine(final ILineSegment threatLine, double margin)
	{
		if (centerBacksMustStayOnPenArea())
		{
			return Optional.empty();
		}
		final ILineSegment threatDefendingLine = DefenseMath.getProtectionLine(
				threatLine,
				margin,
				DefenseConstants.getMinGoOutDistance(),
				DefenseConstants.getMaxGoOutDistance());
		if (threatDefendingLine.getLength() < 1)
		{
			return Optional.empty();
		}
		return Optional.of(threatDefendingLine);
	}


	private boolean centerBacksMustStayOnPenArea()
	{
		return getAiFrame().getGameState().isPenaltyOrPreparePenalty();
	}


	protected IVector2 predictedOpponentPos(final ITrackedBot bot)
	{
		return bot.getPosByTime(DefenseConstants.getLookaheadBotThreats(bot.getVel().getLength()));
	}
}
