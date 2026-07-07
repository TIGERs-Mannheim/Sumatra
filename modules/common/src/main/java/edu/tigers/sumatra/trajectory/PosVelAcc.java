package edu.tigers.sumatra.trajectory;

import lombok.Value;


/**
 * State class containing position, velocity and acceleration.
 *
 * @param <T>
 */
@Value
class PosVelAcc<T>
{
	/**
	 * [m]
	 */
	T pos;
	/**
	 * [m/s]
	 */
	T vel;
	/**
	 * [m/s^2]
	 */
	T acc;
}
