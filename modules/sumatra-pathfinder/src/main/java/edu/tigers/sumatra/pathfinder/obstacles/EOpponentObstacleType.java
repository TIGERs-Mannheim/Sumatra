package edu.tigers.sumatra.pathfinder.obstacles;

public enum EOpponentObstacleType
{
	/**
	 * Assume that opponents accelerate up to the maximum speed.
	 */
	ACCELERATING_OPPONENTS,
	/**
	 * Assume that opponents stop at the position where a collision is checked.
	 */
	STOPPING_OPPONENTS,
	/**
	 * Assume that opponents move with constant velocity.
	 */
	CONST_VEL_OPPONENTS,
	/**
	 * Assume that opponents brake to a stop immediately.
	 */
	BRAKING_OPPONENTS,
}
