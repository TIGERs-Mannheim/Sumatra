/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
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


public class SupportMidfieldPositionsCalc extends ACalculator
{
	@Configurable(comment = "Distance to search in both directions from mid line", defValue = "1500.0")
	private static double searchDistance = 1500.0;

	@Configurable(comment = "Number of Sampling Points in X direction", defValue = "3")
	private static int nSamplingPointsX = 3;

	@Configurable(comment = "Number of Sampling Points in Y direction", defValue = "4")
	private static int nSamplingPointsY = 4;

	@Configurable(comment = "Number of Opponents allowed next to a new Position", defValue = "2")
	private static int maxOpponents = 2;

	@Configurable(comment = "Number of other TIGERs allowed next to a new position", defValue = "0")
	private static int maxTigers = 0;

	@Configurable(comment = "Area to look for other bots next to the new position", defValue = "1500.0")
	private static double searchCircleSize = 1500.0;

	@Configurable(comment = "X-offset to the center for the search area", defValue = "500.0")
	private static double searchOffset = 500.0;

	@Getter
	private List<IVector2> midfieldPositions;


	@Override
	protected void doCalc()
	{
		IRectangle searchRect = Rectangle.fromCenter(Vector2.fromX(searchOffset), 2 * searchDistance,
				Geometry.getFieldWidth());
		getShapes(EAiShapesLayer.SUPPORT_MIDFIELD)
				.add(new DrawableRectangle(searchRect, Color.CYAN));

		List<Double> xCoords = SumatraMath.evenDistribution1D((-searchDistance + searchOffset),
				(searchDistance + searchOffset), nSamplingPointsX);
		List<Double> yCoords = SumatraMath.evenDistribution1D(-Geometry.getFieldWidth() / 2, Geometry.getFieldWidth() / 2,
				nSamplingPointsY);

		midfieldPositions = new ArrayList<>();

		for (Double x : xCoords)
		{
			for (Double y : yCoords)
			{
				IVector2 samplePoint = Vector2.fromXY(x, y);
				if (pointIsReasonable(samplePoint))
				{
					midfieldPositions.add(samplePoint);
					getShapes(EAiShapesLayer.SUPPORT_MIDFIELD)
							.add(new DrawablePoint(samplePoint, Color.GREEN));
				} else
				{
					getShapes(EAiShapesLayer.SUPPORT_MIDFIELD)
							.add(new DrawablePoint(samplePoint, Color.RED));
				}
			}
		}

		midfieldPositions = Collections.unmodifiableList(midfieldPositions);
	}


	private boolean pointIsReasonable(final IVector2 samplePoint)
	{
		ICircle searchCircle = Circle.createCircle(samplePoint, searchCircleSize);
		long nOpponents = getWFrame().getOpponentBots().values().stream()
				.filter(bot -> searchCircle.isPointInShape(bot.getPos()))
				.count();
		long nTIGERs = getWFrame().getTigerBotsAvailable().values().stream()
				.filter(bot -> searchCircle.isPointInShape(bot.getPos()))
				.count();

		return maxTigers >= nTIGERs && maxOpponents >= nOpponents;
	}
}
