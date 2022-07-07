/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.traj;

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
import edu.tigers.sumatra.pathfinder.IPathFinderResult;
import edu.tigers.sumatra.pathfinder.PathFinderInput;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Stream;


/**
 * Implementation of the traj-based path finder.
 * It generates different paths in a rather systematic way and choices the best one based on some heuristic.
 */
@RequiredArgsConstructor
public class TrajPathFinder extends ATrajPathFinder
{
	private static final IColorPicker COLOR_PICKER_BW = ColorPickerFactory.scaledDouble(Color.WHITE, Color.BLACK);
	private static final IColorPicker COLOR_PICKER_GREEN_RED = ColorPickerFactory.scaledDouble(Color.green, Color.red);


	@Configurable(comment = "The step size in [s] to use for stepping from current position to sub destination", defValue = "0.2")
	private static double subDestinationStepSize = 0.2;

	@Configurable(comment = "The step size in [s] to use for stepping from current position to sub destination", defValue = "0.0625")
	private static double lastSubDestinationStepSize = 0.0625;

	@Configurable(comment = "A bonus time in [s] to add to the path score for paths with the same sub destination as last frame", defValue = "0.15")
	private static double lastSubDestinationBonus = 0.15;

	@Configurable(comment = "Generate (pseudo-) random sub destinations instead of systematic ones.", defValue = "false")
	private static boolean randomSubDestinations = false;

	@Configurable(comment = "Perform the path planning on a separate thread", defValue = "true")
	private static boolean asyncPlanning = true;


	static
	{
		ConfigRegistration.registerClass("sisyphus", TrajPathFinder.class);
	}

	private final List<Direction> directions = generateDirections();
	private final Random rnd = new Random();

	@Setter
	private ExecutorService executorService;
	private IVector2 lastSubDest = null;
	private List<IDrawableShape> debugShapes;
	private IVector2 lastDir;


	/**
	 * @param input all necessary input data for planning the path
	 * @return a path if one was found
	 */
	@Override
	public Future<IPathFinderResult> calcPath(final PathFinderInput input)
	{
		processDebug(input);
		TrajPathCollision directPathCollision = getPath(input, input.getDest());
		if (directPathCollision.isOk())
		{
			lastDir = directPathCollision.getTrajPath().getAcceleration(0);
			return CompletableFuture.completedFuture(directPathCollision);
		}

		if (asyncPlanning && executorService != null)
		{
			return executorService.submit(() -> {
				Thread.currentThread().setName("PathFinder");
				return generatePath(input, directPathCollision);
			});
		}
		return CompletableFuture.completedFuture(generatePath(input, directPathCollision));
	}


	private TrajPathCollision generatePath(PathFinderInput input, TrajPathCollision directPathCollision)
	{
		TrajPathCollision newBestPathCollision = findPath(input, directPathCollision);
		Optional<TrajPathCollision> lastSubDestPathCollision = getBestSubPathCollision(input);

		TrajPathCollision bestPathCollision = newBestPathCollision;
		if (lastSubDestPathCollision.isPresent()
				&& newBestPathCollision.getPenaltyScore() > lastSubDestPathCollision.get().getPenaltyScore()
				- lastSubDestinationBonus)
		{
			bestPathCollision = lastSubDestPathCollision.get();
		}

		updateLastSubDest(bestPathCollision);

		lastDir = bestPathCollision.getTrajPath().getAcceleration(0);
		return bestPathCollision;
	}


	private IVector2 getDirection(final PathFinderInput input)
	{
		if ((lastDir != null) && !lastDir.isZeroVector())
		{
			// towards current acceleration direction
			return lastDir;
		}
		IVector2 toDest = input.getDest().subtractNew(input.getPos());
		if (toDest.isZeroVector())
		{
			// towards center point
			return input.getDest().multiplyNew(-1);
		}
		// towards destination
		return toDest;
	}


	private TrajPathCollision findPath(final PathFinderInput input, final TrajPathCollision directPathCollision)
	{
		List<IVector2> subDestinations = getSubDestinations(input);

		final Stream<TrajPathCollision> pathCollisionStream = Stream.concat(Stream.of(directPathCollision),
				subDestinations.stream()
						.flatMap(d -> getPathCollisions(input, d, subDestinationStepSize).stream()));

		if (debugShapes != null)
		{
			List<TrajPathCollision> pathCollisions = pathCollisionStream
					.sorted(Comparator.comparing(TrajPathCollision::getPenaltyScore))
					.toList();
			drawDebugShapes(subDestinations, pathCollisions);
			return pathCollisions.get(0);
		}

		return pathCollisionStream.min(Comparator.comparing(TrajPathCollision::getPenaltyScore)).orElseThrow();
	}


	private void drawDebugShapes(final List<IVector2> subDestinations, final List<TrajPathCollision> pathCollisions)
	{
		int max = Math.min(20, pathCollisions.size());
		for (int i = 0; i < max; i++)
		{
			double relValue = (double) i / Math.max(1, max - 1);
			Color color = COLOR_PICKER_GREEN_RED.getColor(relValue);
			TrajPathCollision p = pathCollisions.get(i);
			debugShapes.add(new DrawableTrajectoryPath(p.getTrajectory(), color));
		}
		for (int i = 0; i < pathCollisions.size(); i++)
		{
			double relValue = (double) i / pathCollisions.size();
			Color color = COLOR_PICKER_GREEN_RED.getColor(relValue);
			TrajPathCollision p = pathCollisions.get(i);
			IVector2 subDestination = p.getTrajPath().getNextDestination(0);
			long layer = Math.round(p.getTrajPath().gettEnd() / subDestinationStepSize);
			debugShapes.add(new DrawableCircle(Circle.createCircle(subDestination, 10 + layer * 10.0), color));
		}
		for (IVector2 sd : subDestinations)
		{
			debugShapes.add(new DrawablePoint(sd, Color.BLUE));
		}
	}


	private List<IVector2> getSubDestinations(final PathFinderInput input)
	{
		if (randomSubDestinations)
		{
			directions.clear();
			directions.addAll(generateRandomDirections());
		}
		IVector2 dir = getDirection(input);
		return directions.stream()
				.map(d -> dir.turnNew(d.angle).scaleTo(d.length).add(input.getPos()))
				.map(IVector2.class::cast)
				.toList();
	}


	private List<Direction> generateRandomDirections()
	{
		List<Double> angles = new ArrayList<>();
		int numAngles = 15;
		for (int a = 0; a < numAngles; a++)
		{
			angles.add((rnd.nextDouble() * AngleMath.PI_TWO) - AngleMath.PI);
		}

		List<Direction> dirList = new ArrayList<>();
		for (Double a : angles)
		{
			for (int i = 0; i < 4; i++)
			{
				double len = rnd.nextDouble() * 3000 + 100;
				dirList.add(new Direction(a, len));
			}
		}
		return dirList;
	}


	private List<Direction> generateDirections()
	{
		List<Double> angles = new ArrayList<>();
		double angleStep = 0.4;
		for (double a = angleStep; a < AngleMath.PI; a += angleStep)
		{
			angles.add(a);
		}
		angles.addAll(angles.stream().map(a -> -a).toList());
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


	private void updateLastSubDest(final TrajPathCollision bestPathCollision)
	{
		if (bestPathCollision.getTrajPath().getChild() != null)
		{
			lastSubDest = bestPathCollision.getTrajPath().getNextDestination(0.0);
		} else
		{
			lastSubDest = null;
		}
	}


	private Optional<TrajPathCollision> getBestSubPathCollision(final PathFinderInput input)
	{
		if (lastSubDest != null)
		{
			List<TrajPathCollision> lastSubDestPathCollisions = getPathCollisions(input, lastSubDest,
					lastSubDestinationStepSize);

			if (debugShapes != null)
			{
				lastSubDestPathCollisions.sort(Comparator.comparing(TrajPathCollision::getPenaltyScore));
				drawSubDestPathCollisions(lastSubDestPathCollisions);
				return Optional.ofNullable(lastSubDestPathCollisions.isEmpty() ? null
						: lastSubDestPathCollisions.get(0));
			} else
			{
				return lastSubDestPathCollisions.stream()
						.min(Comparator.comparing(TrajPathCollision::getPenaltyScore));
			}

		}
		return Optional.empty();
	}


	private void drawSubDestPathCollisions(final List<TrajPathCollision> lastSubDestPathCollisions)
	{
		for (int i = 0; i < lastSubDestPathCollisions.size(); i++)
		{
			double relValue = (double) i / lastSubDestPathCollisions.size();
			Color color = COLOR_PICKER_BW.getColor(relValue);
			TrajPathCollision p = lastSubDestPathCollisions.get(i);
			debugShapes.add(new DrawableTrajectoryPath(p.getTrajectory(), color));
		}
		debugShapes.add(new DrawablePoint(lastSubDest, Color.CYAN));
	}


	private void processDebug(final PathFinderInput input)
	{
		if (input.isDebug())
		{
			debugShapes = new ArrayList<>();
		} else
		{
			debugShapes = null;
		}
	}


	private List<TrajPathCollision> getPathCollisions(
			final PathFinderInput input,
			final IVector2 subDest,
			final double stepSize
	)
	{
		List<TrajPathCollision> pathCollisions = new ArrayList<>();
		TrajPathCollision subPathCollision = getPath(input, subDest);
		TrajPathCollision pc = subPathCollision;
		for (double t = stepSize; t <= subPathCollision.getLastValidTime(); t += stepSize)
		{
			TrajPath path = subPathCollision.getTrajPath().append(input.getMoveConstraints(), t, input.getDest());
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
