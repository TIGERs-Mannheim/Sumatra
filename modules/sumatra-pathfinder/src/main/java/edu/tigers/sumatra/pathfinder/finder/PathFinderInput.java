package edu.tigers.sumatra.pathfinder.finder;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;


/**
 * Data holder for path planning information
 */
@Builder(toBuilder = true)
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PathFinderInput
{
	long timestamp;
	MoveConstraints moveConstraints;
	IVector2 pos;
	IVector2 vel;
	IVector2 dest;
	List<IObstacle> obstacles;


	/**
	 * Create the input from a tracked bot
	 *
	 * @param state
	 * @return
	 */
	public static PathFinderInput.PathFinderInputBuilder fromBot(State state)
	{
		return PathFinderInput.builder()
				.pos(state.getPos())
				.vel(state.getVel2());
	}
}
