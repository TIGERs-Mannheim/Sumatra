package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Set;


/**
 * Conditions on path planning.
 */
@Value
@Builder(toBuilder = true)
@With
public class MovementCon
{
	@Builder.Default
	boolean penaltyAreaOurObstacle = true;
	@Builder.Default
	boolean penaltyAreaTheirObstacle = true;
	@Builder.Default
	boolean fieldBorderObstacle = true;
	@Builder.Default
	boolean ballObstacle = true;
	@Builder.Default
	boolean theirBotsObstacle = true;
	@Builder.Default
	boolean ourBotsObstacle = true;
	@Builder.Default
	boolean goalPostsObstacle = false;
	@Builder.Default
	boolean gameStateObstacle = true;

	@Builder.Default
	Double distanceToBall = null;
	@Builder.Default
	@NonNull
	EObstacleAvoidanceMode obstacleAvoidanceMode = EObstacleAvoidanceMode.NORMAL;

	@NonNull
	@Builder.Default
	Set<BotID> ignoredBots = Set.of();
	@NonNull
	@Builder.Default
	List<IObstacle> customObstacles = List.of();


	public static final MovementCon DEFAULT = MovementCon.builder().build();


	@SuppressWarnings("java:S107")
		// accepting too many parameters for builder
	MovementCon(
			boolean penaltyAreaOurObstacle,
			boolean penaltyAreaTheirObstacle,
			boolean fieldBorderObstacle,
			boolean ballObstacle,
			boolean theirBotsObstacle,
			boolean ourBotsObstacle,
			boolean goalPostsObstacle,
			boolean gameStateObstacle,
			Double distanceToBall,
			@NonNull EObstacleAvoidanceMode obstacleAvoidanceMode,
			@NonNull Set<BotID> ignoredBots,
			@NonNull List<IObstacle> customObstacles
	)
	{
		Validate.isTrue(
				distanceToBall == null || distanceToBall >= 0,
				"distanceToBall must be null or >=0: ", distanceToBall == null ? "null" : distanceToBall
		);
		this.penaltyAreaOurObstacle = penaltyAreaOurObstacle;
		this.penaltyAreaTheirObstacle = penaltyAreaTheirObstacle;
		this.fieldBorderObstacle = fieldBorderObstacle;
		this.ballObstacle = ballObstacle;
		this.theirBotsObstacle = theirBotsObstacle;
		this.ourBotsObstacle = ourBotsObstacle;
		this.goalPostsObstacle = goalPostsObstacle;
		this.gameStateObstacle = gameStateObstacle;
		this.distanceToBall = distanceToBall;
		this.obstacleAvoidanceMode = obstacleAvoidanceMode;
		this.ignoredBots = Set.copyOf(ignoredBots);
		this.customObstacles = List.copyOf(customObstacles);
	}


	public MovementCon withBotsObstacle(boolean obstacle)
	{
		return toBuilder()
				.theirBotsObstacle(obstacle)
				.ourBotsObstacle(obstacle)
				.build();
	}


	public MovementCon withPhysicalObstaclesOnly()
	{
		return toBuilder()
				.fieldBorderObstacle(true)
				.penaltyAreaOurObstacle(false)
				.penaltyAreaTheirObstacle(false)
				.goalPostsObstacle(true)
				.gameStateObstacle(false)
				.theirBotsObstacle(true)
				.ourBotsObstacle(true)
				.ballObstacle(true)
				.build();
	}


	public MovementCon withNoObstacles()
	{
		return toBuilder()
				.fieldBorderObstacle(false)
				.penaltyAreaOurObstacle(false)
				.penaltyAreaTheirObstacle(false)
				.goalPostsObstacle(false)
				.gameStateObstacle(false)
				.theirBotsObstacle(false)
				.ourBotsObstacle(false)
				.ballObstacle(false)
				.build();
	}


	public ETeam getConsideredPenAreas()
	{
		if (penaltyAreaOurObstacle && penaltyAreaTheirObstacle)
		{
			return ETeam.BOTH;
		} else if (penaltyAreaTheirObstacle)
		{
			return ETeam.OPPONENTS;
		} else if (penaltyAreaOurObstacle)
		{
			return ETeam.TIGERS;
		}
		return ETeam.UNKNOWN;
	}
}
