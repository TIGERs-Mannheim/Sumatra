package edu.tigers.sumatra.math.vector;

/**
 * @author nicolai.ommer
 */
@FunctionalInterface
public interface IEuclideanDistance
{
	/**
	 * @param point target point
	 * @return euclidean distance to the target point
	 */
	double distanceTo(IVector2 point);
}
