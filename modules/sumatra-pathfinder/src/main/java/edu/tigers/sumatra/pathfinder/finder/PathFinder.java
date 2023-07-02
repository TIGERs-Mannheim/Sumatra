/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.finder;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.EPathFinderShapesLayer;
import edu.tigers.sumatra.pathfinder.IPathFinder;
import edu.tigers.sumatra.pathfinder.checker.SubPathCollisionChecker;
import edu.tigers.sumatra.pathfinder.subdestgen.SubDestGenerator;
import edu.tigers.sumatra.pathfinder.subdestgen.SubDestRndGenerator;
import lombok.Setter;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


public class PathFinder implements IPathFinder
{
	private static final double TIME_CORRECTION_RANGE = 0.02;

	@Configurable(comment = "The initial offset to start with checking for collisions", defValue = "0.1")
	private static double initialTimeOffset = 0.1;

	static
	{
		ConfigRegistration.registerClass("sisyphus", PathFinder.class);
	}

	private final SubDestGenerator subDestinationGenerator = new SubDestRndGenerator();

	@Setter
	private ShapeMap shapeMap;

	private long lastTimestamp;
	private double timeCorrection;


	@Override
	public PathFinderResult calcPath(PathFinderInput input)
	{
		PathFinderResult result = findPath(input);
		updateTimeCorrection(input.getTimestamp());
		return result;
	}


	/**
	 * Maintain a negative time correction that compensates the time steps of the world frames.
	 * Without it, the previous switch time will never be matched, because the step size on the trajectories
	 * is larger.
	 *
	 * @param currentTimestamp
	 */
	private void updateTimeCorrection(long currentTimestamp)
	{
		double dt = (currentTimestamp - lastTimestamp) / 1e9;
		lastTimestamp = currentTimestamp;
		timeCorrection -= dt;
		if (timeCorrection < 0)
		{
			timeCorrection = TIME_CORRECTION_RANGE;
		}
	}


	private TrajPath createPath(PathFinderInput input, IVector2 dest)
	{
		return TrajPath.with(
				input.getMoveConstraints(),
				input.getPos(),
				input.getVel(),
				dest
		);
	}


	private PathFinderResult findPath(PathFinderInput input)
	{
		double timeOffset = initialTimeOffset + timeCorrection;
		var collisionChecker = SubPathCollisionChecker.of(input, shapeMap);

		Optional<PathFinderResult> validDirectPath = collisionChecker.findDirectPath(
				createPath(input, input.getDest()),
				timeOffset
		);
		if (validDirectPath.isPresent())
		{
			return validDirectPath.get();
		}

		int subDestCtr = 0;
		for (Iterator<IVector2> subDestIterator = subDestinationGenerator.subDestIterator(input);
		     subDestIterator.hasNext(); )
		{
			IVector2 subDest = subDestIterator.next();

			if (shapeMap != null)
			{
				shapeMap.get(EPathFinderShapesLayer.SUB_DEST_TRIED)
						.add(new DrawableCircle(Circle.createCircle(subDest, 30)).setColor(Color.magenta));
				shapeMap.get(EPathFinderShapesLayer.SUB_DEST_TRIED).add(
						new DrawableAnnotation(subDest, String.valueOf(subDestCtr))
								.withCenterHorizontally(true)
								.withFontHeight(15)
								.setColor(Color.magenta)
				);
			}

			TrajPath subPath = createPath(input, subDest);
			Optional<PathFinderResult> validPath = collisionChecker.findValidPath(subPath, timeOffset);
			if (validPath.isPresent())
			{
				return validPath.get();
			}
			subDestCtr++;
		}

		return new PathFinderResult(createPath(input, input.getPos()), List.of());
	}
}
