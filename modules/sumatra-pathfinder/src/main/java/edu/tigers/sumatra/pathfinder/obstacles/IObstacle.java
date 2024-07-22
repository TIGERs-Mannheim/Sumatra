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
	int BOT_THEIR_ORDER_ID = 20;
	int BOT_OUR_ORDER_ID = 30;
	int BALL_ORDER_ID = 40;

	/**
	 * Calculate the squared distance to the obstacle.
	 *
	 * @param input collision input with some information about the current robot
	 * @return squared distance
	 */
	double distanceTo(CollisionInput input);

	/**
	 * Is the obstacle colliding at this position?
	 *
	 * @param pos the position to check
	 * @return true, if the obstacle is colliding at this position
	 */
	boolean isCollidingAt(IVector2 pos);

	/**
	 * Check if the robot can collide at this input.
	 * If this is false, distanceTo() will not be called.
	 *
	 * @param input collision input with some information about the current robot
	 * @return
	 */
	boolean canCollide(CollisionInput input);


	/**
	 * @return true, if the obstacle is static (can not move)
	 */
	boolean isMotionLess();

	/**
	 * @return list of shapes to visualize obstacle
	 */
	List<IDrawableShape> getShapes();

	/**
	 * @return the max speed of this obstacle that it could currently reach
	 */
	double getMaxSpeed();

	/**
	 * Allow the obstacle to adapt the destination, if it is inside the obstacle.
	 *
	 * @param destination
	 * @return
	 */
	Optional<IVector2> adaptDestination(IVector2 destination);

	/**
	 * Allow the obstacle to adapt the destination based on the robot pos.
	 * This is for example intended for the penArea to avoid letting the robot drive through it, once it is inside.
	 *
	 * @param robotPos
	 * @return
	 */
	Optional<IVector2> adaptDestinationForRobotPos(IVector2 robotPos);

	/**
	 * Change the color of the obstacle for visualization purposes.
	 *
	 * @param color new color
	 */
	void setColor(Color color);

	/**
	 * @return the identifier of this obstacle for debugging purposes
	 */
	default String getIdentifier()
	{
		return this.getClass().getSimpleName();
	}

	/**
	 * Is a collision likely at this time and position?
	 * If not, the obstacle may be ignored for this time step.
	 *
	 * @param t   time in the future
	 * @param pos robot position at that time
	 * @return true, if the collision is rather likely
	 */
	boolean collisionLikely(double t, IVector2 pos);

	/**
	 * @return true, if this obstacle has priority over current bot
	 */
	boolean hasPriority();

	/**
	 * Obstacle velocity at this position and time.
	 *
	 * @param pos potential position of the obstacle (i.e. the pot. collision pos)
	 * @param t   time in the future
	 * @return velocity of the obstacle at this position and time [m/s]
	 */
	IVector2 velocity(IVector2 pos, double t);

	/**
	 * @return the order of this obstacle. Lower values are considered first.
	 */
	int orderId();


	/**
	 * @return true, if a dynamic margin (increased by robots velocity) should be used towards this obstacle
	 */
	boolean useDynamicMargin();
}
