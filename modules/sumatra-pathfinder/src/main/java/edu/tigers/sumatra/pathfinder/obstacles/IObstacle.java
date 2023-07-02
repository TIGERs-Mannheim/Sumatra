/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;

import java.awt.Color;
import java.util.List;
import java.util.Optional;


/**
 * Interface for all obstacles for path planning
 */
public interface IObstacle
{
	/**
	 * Calculate the squared distance to the obstacle.
	 *
	 * @param input collision input with some information about the current robot
	 * @return squared distance
	 */
	double distanceTo(CollisionInput input);


	/**
	 * Check if the robot can collide at this input.
	 *
	 * @param input collision input with some information about the current robot
	 * @return
	 */
	default boolean canCollide(CollisionInput input)
	{
		return true;
	}


	/**
	 * @return true, if the obstacle is static (can not move)
	 */
	default boolean isMotionLess()
	{
		return true;
	}

	/**
	 * @return true, if the obstacle can move
	 */
	default boolean canMove()
	{
		return !isMotionLess();
	}

	/**
	 * @return list of shapes to visualize obstacle
	 */
	List<IDrawableShape> getShapes();

	/**
	 * @return the max speed of this obstacle that it could currently reach
	 */
	default double getMaxSpeed()
	{
		return 0;
	}

	/**
	 * Allow the obstacle to adapt the destination.
	 * This is for example intended for the penArea to avoid letting the robot drive through it, once it is inside.
	 *
	 * @param robotPos
	 * @param destination
	 * @return
	 */
	default Optional<IVector2> adaptDestination(IVector2 robotPos, IVector2 destination)
	{
		return Optional.empty();
	}

	default void setColor(Color color)
	{
		// noop
	}

	default String getIdentifier()
	{
		return this.getClass().getSimpleName();
	}
}
