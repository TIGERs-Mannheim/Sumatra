/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.checker;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.drawable.DrawablePlanarCurve;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.EPathFinderShapesLayer;
import edu.tigers.sumatra.pathfinder.acceptor.MotionLessObstacleResultAcceptor;
import edu.tigers.sumatra.pathfinder.acceptor.MovingObstacleResultAcceptor;
import edu.tigers.sumatra.pathfinder.acceptor.PathFinderResultAcceptor;
import edu.tigers.sumatra.pathfinder.finder.PathFinderCollision;
import edu.tigers.sumatra.pathfinder.finder.PathFinderInput;
import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;
import edu.tigers.sumatra.pathfinder.finder.TrajPath;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Log4j2
@RequiredArgsConstructor
public class SubPathCollisionChecker
{
	@Configurable(comment = "The step size in [s] to use for stepping from current position to sub destination", defValue = "0.2")
	private static double stepSizeOnSubPath = 0.2;

	static
	{
		ConfigRegistration.registerClass("sisyphus", SubPathCollisionChecker.class);
	}

	private final PathFinderResultAcceptor motionLessObstacleResultAcceptor;
	private final PathFinderResultAcceptor movingObstacleResultAcceptor;

	private final List<IObstacle> motionLessObstacles;
	private final List<IObstacle> movingObstacles;

	private final ShapeMap shapeMap;
	private final IMoveConstraints moveConstraints;
	private final IVector2 dest;
	private final List<PathFinderResult> rejectedResults = new ArrayList<>();


	public static SubPathCollisionChecker of(PathFinderInput input, ShapeMap shapeMap)
	{
		List<IObstacle> motionLessObstacles = input.getObstacles().stream().filter(IObstacle::isMotionLess).toList();
		List<IObstacle> movingObstacles = input.getObstacles().stream().filter(o -> !o.isMotionLess()).toList();
		return new SubPathCollisionChecker(
				new MotionLessObstacleResultAcceptor(),
				new MovingObstacleResultAcceptor(input.getMoveConstraints()),
				motionLessObstacles,
				movingObstacles,
				shapeMap,
				input.getMoveConstraints(),
				input.getDest()
		);
	}


	public Optional<PathFinderResult> getBestRejectedResult()
	{
		return rejectedResults.stream()
				.min(Comparator.comparing(r -> distanceToClosestMovingCollision(r, dest)));
	}


	private double distanceToClosestMovingCollision(PathFinderResult result, IVector2 dest)
	{
		return result.getCollisions().stream()
				.filter(c -> !c.getObstacle().isMotionLess())
				.map(PathFinderCollision::getFirstCollisionTime)
				.min(Double::compareTo)
				.map(t -> result.getTrajectory().getPositionMM(t).distanceToSqr(dest))
				.orElse(Double.MAX_VALUE);
	}


	/**
	 * Check if the given (complete) path is accepted by the collision checkers and return the result, if so.
	 *
	 * @param path              a complete path
	 * @param initialTimeOffset the initial time offset to start checking the path
	 * @return the result, if the path is accepted
	 */
	public Optional<PathFinderResult> getAcceptablePath(TrajPath path, double initialTimeOffset)
	{
		var motionLessSubPathCollisionChecker = PathCollisionChecker.ofPath(
				path, motionLessObstacles, shapeMap, initialTimeOffset
		);
		var movingSubPathCollisionChecker = PathCollisionChecker.ofPath(
				path, movingObstacles, shapeMap, initialTimeOffset
		);

		if (shapeMap != null)
		{
			shapeMap.get(EPathFinderShapesLayer.PATHS_CHECKED).add(
					new DrawablePlanarCurve(path).setColor(Color.cyan).setStrokeWidth(2)
			);
		}

		motionLessSubPathCollisionChecker.checkUntil(path.getTotalTime());
		var motionLessResult = motionLessSubPathCollisionChecker.getPathFinderResult();
		if (motionLessObstacleResultAcceptor.accept(motionLessResult))
		{
			movingSubPathCollisionChecker.checkUntil(path.getTotalTime());
			PathFinderResult movingResult = movingSubPathCollisionChecker.getPathFinderResult();
			PathFinderResult mergedResult = movingResult.merge(motionLessResult);
			if (movingObstacleResultAcceptor.accept(movingResult))
			{
				return Optional.of(mergedResult);
			}
			rejectedResults.add(mergedResult);
		}
		return Optional.empty();
	}


	/**
	 * Find an acceptable path based on given sub path.
	 *
	 * @param subPath           a path to an intermediate destination
	 * @param initialTimeOffset the initial time offset to start checking the path
	 * @return the result, if the path is accepted
	 */
	public Optional<PathFinderResult> findAcceptablePath(TrajPath subPath, double initialTimeOffset)
	{
		var motionLessSubPathCollisionChecker = PathCollisionChecker.ofPath(
				subPath, motionLessObstacles, shapeMap, initialTimeOffset
		);
		var movingSubPathCollisionChecker = PathCollisionChecker.ofPath(
				subPath, movingObstacles, shapeMap, initialTimeOffset
		);

		double subTotalTime = motionLessSubPathCollisionChecker.getPath().getTotalTime();
		if (subTotalTime > 100)
		{
			log.warn("Sub path is too long: {}", motionLessSubPathCollisionChecker.getPath());
			return Optional.empty();
		}

		int subDestCtr = 0;
		for (double switchTime = stepSizeOnSubPath + initialTimeOffset;
		     switchTime < subTotalTime;
		     switchTime += stepSizeOnSubPath
		)
		{
			motionLessSubPathCollisionChecker.checkUntil(switchTime);
			PathCollisionChecker motionLessPathCollisionChecker = motionLessSubPathCollisionChecker.append(
					moveConstraints,
					switchTime,
					dest
			);

			if (shapeMap != null)
			{
				shapeMap.get(EPathFinderShapesLayer.PATHS_CHECKED).add(
						new DrawablePlanarCurve(motionLessPathCollisionChecker.getPath()).setColor(Color.magenta)
								.setStrokeWidth(1)
				);
			}

			double totalTime = motionLessPathCollisionChecker.getPath().getTotalTime();
			motionLessPathCollisionChecker.checkUntil(totalTime);

			var motionLessResult = motionLessPathCollisionChecker.getPathFinderResult();
			if (motionLessObstacleResultAcceptor.accept(motionLessResult))
			{
				movingSubPathCollisionChecker.checkUntil(switchTime);
				PathCollisionChecker movingPathCollisionChecker = movingSubPathCollisionChecker.append(
						moveConstraints,
						switchTime,
						dest
				);
				movingPathCollisionChecker.checkUntil(totalTime);
				PathFinderResult movingResult = movingPathCollisionChecker.getPathFinderResult();
				PathFinderResult mergedResult = movingResult.merge(motionLessResult);
				if (movingObstacleResultAcceptor.accept(movingResult))
				{
					return Optional.of(mergedResult);
				}
				rejectedResults.add(mergedResult);
			}

			if (subDestCtr++ > 1000)
			{
				throw new IllegalStateException(
						"Possible endless loop detected in SubPathCollisionChecker.findAcceptablePath(): " +
								switchTime + " " + subTotalTime + " " + initialTimeOffset);
			}
		}
		return Optional.empty();
	}
}
