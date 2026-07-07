package edu.tigers.sumatra.vision.data;

/**
 * Internal ball state for vision filter.
 */
public enum EBallState
{
	/**
	 * Rolling on the floor, no kick as background state
	 */
	ROLLING,
	/**
	 * Ball on the floor with a kick background state
	 */
	KICKED,
	/**
	 * Ball is in the air or doing some hops
	 */
	AIRBORNE,
}