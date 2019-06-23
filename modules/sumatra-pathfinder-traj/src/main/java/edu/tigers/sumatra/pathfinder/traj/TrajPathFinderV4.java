/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.traj;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajPathFinderInput;


/**
 * Implementation of the traj-based path finder
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderV4 extends ATrajPathFinder
{
	private static final IColorPicker COLOR_PICKER_BW = ColorPickerFactory.scaledDouble(Color.WHITE, Color.BLACK);
	private static final IColorPicker COLOR_PICKER_GREEN_RED = ColorPickerFactory.scaledDouble(Color.green, Color.red);
	@Configurable(defValue = "false")
	private static boolean debug = false;
	@Configurable(comment = "The step size in [s] to use for stepping from current position to sub destination", defValue = "0.2")
	private static double subDestinationStepSize = 0.2;
	@Configurable(comment = "The step size in [s] to use for stepping from current position to sub destination", defValue = "0.0625")
	private static double lastSubDestinationStepSize = 0.0625;
	@Configurable(comment = "A bonus time in [s] to add to the path score for paths with the same sub destination as last frame", defValue = "0.15")
	private static double lastSubDestinationBonus = 0.15;
	
	static
	{
		ConfigRegistration.registerClass("sisyphus", TrajPathFinderV4.class);
	}
	
	private final List<Direction> directions = generateDirections();
	private IVector2 lastSubDest = null;
	private List<IDrawableShape> debugShapes;
	
	
	@Override
	protected PathCollision generatePath(final TrajPathFinderInput input)
	{
		processDebug();
		PathCollision directPathCollision = getPath(input, input.getDest());
		if (lastSubDest != null && directPathCollision.isOk())
		{
			return directPathCollision;
		}
		
		PathCollision newBestPathCollision = findPath(input, directPathCollision);
		Optional<PathCollision> lastSubDestPathCollision = getBestSubPathCollision(input);
		
		PathCollision bestPathCollision = newBestPathCollision;
		if (lastSubDestPathCollision.isPresent()
				&& newBestPathCollision.getPenaltyScore() > lastSubDestPathCollision.get().getPenaltyScore()
						- lastSubDestinationBonus)
		{
			bestPathCollision = lastSubDestPathCollision.get();
		}
		
		updateLastSubDest(bestPathCollision);
		
		return bestPathCollision;
	}
	
	
	private PathCollision findPath(final TrajPathFinderInput input, final PathCollision directPathCollision)
	{
		List<IVector2> subDestinations = getSubDestinations(input);
		
		final Stream<PathCollision> pathCollisionStream = Stream.concat(Stream.of(directPathCollision),
				subDestinations.stream()
						.parallel()
						.flatMap(d -> getPathCollisions(input, d, subDestinationStepSize).stream()));
		
		if (debugShapes != null)
		{
			List<PathCollision> pathCollisions = pathCollisionStream
					.sorted(Comparator.comparing(PathCollision::getPenaltyScore))
					.collect(Collectors.toList());
			drawDebugShapes(subDestinations, pathCollisions);
			return pathCollisions.get(0);
		}
		
		return pathCollisionStream.min(Comparator.comparing(PathCollision::getPenaltyScore))
				.orElseThrow(IllegalStateException::new);
	}
	
	
	private void drawDebugShapes(final List<IVector2> subDestinations, final List<PathCollision> pathCollisions)
	{
		int max = Math.min(20, pathCollisions.size());
		for (int i = 0; i < max; i++)
		{
			double relValue = (double) i / (max - 1);
			Color color = COLOR_PICKER_GREEN_RED.getColor(relValue);
			PathCollision p = pathCollisions.get(i);
			debugShapes.add(new DrawableTrajectoryPath(p.getTrajectory(), color));
		}
		for (int i = 0; i < pathCollisions.size(); i++)
		{
			double relValue = (double) i / pathCollisions.size();
			Color color = COLOR_PICKER_GREEN_RED.getColor(relValue);
			PathCollision p = pathCollisions.get(i);
			IVector2 subDestination = p.getTrajPath().getNextDestination(0);
			long layer = Math.round(p.getTrajPath().gettEnd() / subDestinationStepSize);
			debugShapes.add(new DrawableCircle(Circle.createCircle(subDestination, 10 + layer * 10.0), color));
		}
		for (IVector2 sd : subDestinations)
		{
			debugShapes.add(new DrawablePoint(sd, Color.BLUE));
		}
	}
	
	
	private List<IVector2> getSubDestinations(final TrajPathFinderInput input)
	{
		IVector2 dir = getDirection(input);
		return directions.stream()
				.map(d -> dir.turnNew(d.angle).scaleTo(d.length).add(input.getPos()))
				.collect(Collectors.toList());
	}
	
	
	private List<Direction> generateDirections()
	{
		List<Double> angles = new ArrayList<>();
		double angleStep = 0.4;
		for (double a = angleStep; a < AngleMath.PI; a += angleStep)
		{
			angles.add(a);
		}
		angles.addAll(angles.stream().map(a -> -a).collect(Collectors.toList()));
		angles.add(0.0);
		
		List<Direction> dirList = new ArrayList<>();
		for (Double a : angles)
		{
			for (double len = 100; len < 3101; len += 1000)
			{
				dirList.add(new Direction(a, len));
			}
		}
		return dirList;
	}
	
	
	private void updateLastSubDest(final PathCollision bestPathCollision)
	{
		if (bestPathCollision.getTrajPath().getChild() != null)
		{
			lastSubDest = bestPathCollision.getTrajPath().getNextDestination(0.0);
		} else
		{
			lastSubDest = null;
		}
	}
	
	
	private Optional<PathCollision> getBestSubPathCollision(final TrajPathFinderInput input)
	{
		if (lastSubDest != null)
		{
			List<PathCollision> lastSubDestPathCollisions = getPathCollisions(input, lastSubDest,
					lastSubDestinationStepSize);
			
			if (debugShapes != null)
			{
				lastSubDestPathCollisions.sort(Comparator.comparing(PathCollision::getPenaltyScore));
				drawSubDestPathCollisions(lastSubDestPathCollisions);
				return Optional.ofNullable(lastSubDestPathCollisions.isEmpty() ? null
						: lastSubDestPathCollisions.get(0));
			} else
			{
				return lastSubDestPathCollisions.stream()
						.min(Comparator.comparing(PathCollision::getPenaltyScore));
			}
			
		}
		return Optional.empty();
	}
	
	
	private void drawSubDestPathCollisions(final List<PathCollision> lastSubDestPathCollisions)
	{
		for (int i = 0; i < lastSubDestPathCollisions.size(); i++)
		{
			double relValue = (double) i / lastSubDestPathCollisions.size();
			Color color = COLOR_PICKER_BW.getColor(relValue);
			PathCollision p = lastSubDestPathCollisions.get(i);
			debugShapes.add(new DrawableTrajectoryPath(p.getTrajectory(), color));
		}
		debugShapes.add(new DrawablePoint(lastSubDest, Color.CYAN));
	}
	
	
	private void processDebug()
	{
		if (debug)
		{
			debugShapes = new ArrayList<>();
		} else
		{
			debugShapes = null;
		}
	}
	
	
	private List<PathCollision> getPathCollisions(
			final TrajPathFinderInput input,
			final IVector2 subDest,
			final double stepSize)
	{
		List<PathCollision> pathCollisions = new ArrayList<>();
		PathCollision subPathCollision = getPath(input, subDest);
		PathCollision pc = subPathCollision;
		for (double t = stepSize; t <= subPathCollision.getLastValidTime(); t += stepSize)
		{
			TrajPathV2 path = gen.append(subPathCollision.getTrajPath(), input.getMoveConstraints(), t,
					input.getDest());
			pc = getCollision(input, path, pc, t);
			pathCollisions.add(pc);
		}
		return pathCollisions;
	}
	
	
	public List<IDrawableShape> getDebugShapes()
	{
		return debugShapes == null ? Collections.emptyList() : debugShapes;
	}
	
	private static final class Direction
	{
		double angle;
		double length;
		
		
		public Direction(final double angle, final double length)
		{
			this.angle = angle;
			this.length = length;
		}
	}
}
