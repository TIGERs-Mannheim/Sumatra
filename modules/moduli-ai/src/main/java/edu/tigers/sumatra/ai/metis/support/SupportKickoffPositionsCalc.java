/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SupportKickoffPositionsCalc extends ACalculator
{
	@Configurable(comment = "Number of Sampling Points in X direction", defValue = "4")
	private static int nSamplingPointsX = 4;

	@Configurable(comment = "Number of Sampling Points in Y direction", defValue = "6")
	private static int nSamplingPointsY = 6;

	@Configurable(comment = "Number of other TIGERs allowed next to a new position", defValue = "0")
	private static int maxTigers = 0;

	@Configurable(comment = "Number of Opponents allowed next to a new Position", defValue = "1")
	private static int maxOpponents = 1;

	@Configurable(comment = "Area to look for other bots next to the new position", defValue = "1000.0")
	private static double searchCircleSize = 1000.0;

	@Configurable(comment = "X-offset to the center for the search area", defValue = "-1000.0")
	private static double searchOffset = -1000.0;

	@Getter
	private List<IVector2> kickoffPositions = List.of();


	@Override
	protected boolean isCalculationNecessary()
	{
		boolean isPreKickoff = getAiFrame().getGameState().isNextKickoffOrPrepareKickoff();

		return getAiFrame().getGameState().isKickoffOrPrepareKickoff() || isPreKickoff;
	}


	@Override
	protected void reset()
	{
		kickoffPositions = List.of();
	}


	@Override
	protected void doCalc()
	{
		double centerX = searchOffset - 3 * Geometry.getBotRadius();
		IRectangle searchRect = Rectangle.fromCenter(Vector2.fromX(centerX), -2 * searchOffset,
				Geometry.getFieldWidth());
		getShapes(EAiShapesLayer.SUPPORT_KICKOFF)
				.add(new DrawableRectangle(searchRect, Color.CYAN));

		List<Double> xCoords = SumatraMath.evenDistribution1D(centerX + searchOffset,
				centerX - searchOffset, nSamplingPointsX);
		List<Double> yCoords = SumatraMath.evenDistribution1D(-Geometry.getFieldWidth() / 2, Geometry.getFieldWidth() / 2,
				nSamplingPointsY);

		kickoffPositions = Collections.unmodifiableList(searchKickoffPositions(xCoords, yCoords));
	}


	private List<IVector2> searchKickoffPositions(List<Double> xCoords, List<Double> yCoords)
	{
		List<IVector2> positions = new ArrayList<>();
		for (Double x : xCoords)
		{
			for (Double y : yCoords)
			{
				IVector2 samplePoint = Vector2.fromXY(x, y);
				if (pointIsReasonable(samplePoint))
				{
					positions.add(samplePoint);
					getShapes(EAiShapesLayer.SUPPORT_KICKOFF)
							.add(new DrawablePoint(samplePoint, Color.GREEN));
				} else
				{
					getShapes(EAiShapesLayer.SUPPORT_KICKOFF)
							.add(new DrawablePoint(samplePoint, Color.RED));
				}
			}
		}
		return positions;
	}


	private boolean pointIsReasonable(final IVector2 samplePoint)
	{
		ICircle searchCircle = Circle.createCircle(samplePoint, searchCircleSize);

		long nTigers = getWFrame().getTigerBotsAvailable().values().stream()
				.filter(bot -> searchCircle.isPointInShape(bot.getPos()))
				.count();
		long nOpponents = getWFrame().getOpponentBots().values().stream()
				.filter(bot -> searchCircle.isPointInShape(bot.getPos()))
				.count();

		return maxOpponents >= nOpponents && maxTigers >= nTigers;
	}
}
