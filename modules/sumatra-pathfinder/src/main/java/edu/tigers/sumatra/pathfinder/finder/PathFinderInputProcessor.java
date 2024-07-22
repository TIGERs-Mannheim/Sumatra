/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.finder;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.math.AcceptablePosFinder;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;


public class PathFinderInputProcessor
{
	@Configurable(defValue = "100", comment = "The step distance [mm] to between points to check for an acceptable position")
	private static double acceptablePosFinderDistanceStepSize = 100;

	@Configurable(defValue = "1000", comment = "The maximum number of iterations to search for an acceptable position")
	private static int acceptablePosFinderMaxIterations = 1000;

	static
	{
		ConfigRegistration.registerClass("sisyphus", PathFinderInputProcessor.class);
	}

	private final AcceptablePosFinder acceptablePosFinder = new AcceptablePosFinder(
			acceptablePosFinderDistanceStepSize,
			acceptablePosFinderMaxIterations
	);


	public PathFinderInput processInput(PathFinderInput input)
	{
		// Sort obstacles. This is mainly relevant for adapting the destination for the robot position.
		List<IObstacle> orderedObstacles = input.getObstacles().stream()
				.sorted(Comparator.comparing(IObstacle::orderId))
				.toList();

		// Do not adapt destination for obstacles that have priority over the robot
		List<IObstacle> orderedAndFiltered = orderedObstacles.stream()
				.filter(o -> o.orderId() != IObstacle.BOT_OUR_ORDER_ID || o.hasPriority())
				.toList();
		Function<IVector2, Optional<IObstacle>> collisionChecker = p -> orderedAndFiltered.stream()
				.filter(o -> o.isCollidingAt(p)).findFirst();

		// Adapt destination in case robot is inside an obstacle
		IVector2 adaptedDestination = adaptDestinationForRobotPos(collisionChecker, input.getPos(), input.getDest())
				.orElse(input.getDest());

		// Adapt the destination such that it is not inside an obstacle
		IVector2 dest = adaptDestination(collisionChecker, adaptedDestination, input.getPos());

		return input.toBuilder()
				.obstacles(filterObstacles(orderedObstacles, input.getPos(), dest))
				.dest(dest)
				.build();
	}


	private List<IObstacle> filterObstacles(List<IObstacle> obstacles, IVector2 robotPos, IVector2 dest)
	{
		List<IObstacle> consideredObstacles = new ArrayList<>();
		for (IObstacle obstacle : obstacles)
		{
			if (!obstacle.isCollidingAt(robotPos) && !obstacle.isCollidingAt(dest))
			{
				consideredObstacles.add(obstacle);
			} else
			{
				obstacle.setColor(Color.red);
			}
		}
		return consideredObstacles;
	}


	private Optional<IVector2> adaptDestinationForRobotPos(
			Function<IVector2, Optional<IObstacle>> collisionFinder,
			IVector2 robotPos,
			IVector2 originalDestination
	)
	{
		Predicate<IVector2> noCollisionChecker = p -> collisionFinder.apply(p).isEmpty();
		Optional<IObstacle> collision = collisionFinder.apply(robotPos);

		if (collision.isEmpty())
		{
			return Optional.empty();
		}

		Optional<IVector2> adaptedDest = collision
				.map(o -> o.adaptDestinationForRobotPos(robotPos))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(noCollisionChecker);
		if (adaptedDest.isPresent())
		{
			if (adaptedDest.get().distanceToSqr(originalDestination) < robotPos.distanceToSqr(originalDestination))
			{
				// The original Pos is anyway in the same direction as the adaptedPos, the adaption is not strictly necessary
				return Optional.empty();
			}
			return adaptedDest;
		}

		return acceptablePosFinder.findAcceptablePosWithReference(robotPos, noCollisionChecker, robotPos);
	}


	private IVector2 adaptDestination(
			Function<IVector2, Optional<IObstacle>> collisionFinder,
			IVector2 dest,
			IVector2 robotPos
	)
	{
		Predicate<IVector2> noCollisionChecker = p -> collisionFinder.apply(p).isEmpty();
		Optional<IObstacle> collision = collisionFinder.apply(dest);

		if (collision.isEmpty())
		{
			return dest;
		}

		Optional<IVector2> adaptedDest = collision
				.map(o -> o.adaptDestination(dest))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(noCollisionChecker);

		return adaptedDest.orElseGet(
				() -> acceptablePosFinder.findAcceptablePosWithReference(dest, noCollisionChecker, robotPos)
						.orElse(dest));
	}
}
