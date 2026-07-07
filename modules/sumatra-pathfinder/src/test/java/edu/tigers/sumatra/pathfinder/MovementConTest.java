package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.tigers.sumatra.ids.ETeamColor.YELLOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MovementConTest
{
	@Test
	void defaultsMatchPreviousBehaviour()
	{
		MovementCon mc = MovementCon.DEFAULT;

		assertThat(mc.isPenaltyAreaOurObstacle()).isTrue();
		assertThat(mc.isPenaltyAreaTheirObstacle()).isTrue();
		assertThat(mc.isFieldBorderObstacle()).isTrue();
		assertThat(mc.isBallObstacle()).isTrue();
		assertThat(mc.isTheirBotsObstacle()).isTrue();
		assertThat(mc.isOurBotsObstacle()).isTrue();
		assertThat(mc.isGoalPostsObstacle()).isFalse();
		assertThat(mc.isGameStateObstacle()).isTrue();
		assertThat(mc.getDistanceToBall()).isNull();
		assertThat(mc.getObstacleAvoidanceMode()).isEqualTo(EObstacleAvoidanceMode.NORMAL);
		assertThat(mc.getIgnoredBots()).isEmpty();
		assertThat(mc.getCustomObstacles()).isEmpty();
	}


	@Test
	void withMethodsReturnNewInstanceLeavingOriginalUntouched()
	{
		MovementCon original = MovementCon.DEFAULT;
		MovementCon updated = original.withBallObstacle(false);

		assertThat(original.isBallObstacle()).isTrue();
		assertThat(updated.isBallObstacle()).isFalse();
		assertThat(updated).isNotSameAs(original);
	}


	@Test
	void withBotsObstacleFlipsBothFlags()
	{
		MovementCon mc = MovementCon.DEFAULT.withBotsObstacle(false);

		assertThat(mc.isOurBotsObstacle()).isFalse();
		assertThat(mc.isTheirBotsObstacle()).isFalse();

		MovementCon back = mc.withBotsObstacle(true);
		assertThat(back.isOurBotsObstacle()).isTrue();
		assertThat(back.isTheirBotsObstacle()).isTrue();
	}


	@Test
	void withPhysicalObstaclesOnlySetsExpectedFlags()
	{
		MovementCon mc = MovementCon.DEFAULT.withPhysicalObstaclesOnly();

		assertThat(mc.isFieldBorderObstacle()).isTrue();
		assertThat(mc.isPenaltyAreaOurObstacle()).isFalse();
		assertThat(mc.isPenaltyAreaTheirObstacle()).isFalse();
		assertThat(mc.isGoalPostsObstacle()).isTrue();
		assertThat(mc.isGameStateObstacle()).isFalse();
		assertThat(mc.isOurBotsObstacle()).isTrue();
		assertThat(mc.isTheirBotsObstacle()).isTrue();
		assertThat(mc.isBallObstacle()).isTrue();
	}


	@Test
	void withNoObstaclesClearsAllObstacleFlags()
	{
		MovementCon mc = MovementCon.DEFAULT.withNoObstacles();

		assertThat(mc.isFieldBorderObstacle()).isFalse();
		assertThat(mc.isPenaltyAreaOurObstacle()).isFalse();
		assertThat(mc.isPenaltyAreaTheirObstacle()).isFalse();
		assertThat(mc.isGoalPostsObstacle()).isFalse();
		assertThat(mc.isGameStateObstacle()).isFalse();
		assertThat(mc.isOurBotsObstacle()).isFalse();
		assertThat(mc.isTheirBotsObstacle()).isFalse();
		assertThat(mc.isBallObstacle()).isFalse();
	}


	@Test
	void distanceToBallNullIsAllowed()
	{
		MovementCon mc = MovementCon.builder().distanceToBall(null).build();
		assertThat(mc.getDistanceToBall()).isNull();
	}


	@Test
	void distanceToBallNegativeIsRejected()
	{
		MovementCon.MovementConBuilder movementConBuilder = MovementCon.builder().distanceToBall(-1.0);
		assertThatThrownBy(movementConBuilder::build)
				.isInstanceOf(IllegalArgumentException.class);
	}


	@Test
	void ignoredBotsIsDefensivelyCopied()
	{
		Set<BotID> mutable = new HashSet<>();
		mutable.add(BotID.createBotId(0, YELLOW));

		MovementCon mc = MovementCon.builder().ignoredBots(mutable).build();
		mutable.clear();

		Set<BotID> ignoredBots = mc.getIgnoredBots();
		assertThat(ignoredBots).hasSize(1);
		BotID anotherBotId = BotID.createBotId(1, YELLOW);
		assertThatThrownBy(() -> ignoredBots.add(anotherBotId))
				.isInstanceOf(UnsupportedOperationException.class);
	}


	@Test
	void customObstaclesIsDefensivelyCopied()
	{
		List<IObstacle> mutable = new java.util.ArrayList<>();
		MovementCon mc = MovementCon.builder().customObstacles(mutable).build();
		// adding to source after construction must not affect the instance
		mutable.add(null);

		List<IObstacle> customObstacles = mc.getCustomObstacles();
		assertThat(customObstacles).isEmpty();
		assertThatThrownBy(() -> customObstacles.add(null))
				.isInstanceOf(UnsupportedOperationException.class);
	}


	@Test
	void getConsideredPenAreasReflectsFlags()
	{
		assertThat(MovementCon.DEFAULT.getConsideredPenAreas()).isEqualTo(edu.tigers.sumatra.ids.ETeam.BOTH);
		assertThat(MovementCon.DEFAULT.withPenaltyAreaOurObstacle(false).getConsideredPenAreas())
				.isEqualTo(edu.tigers.sumatra.ids.ETeam.OPPONENTS);
		assertThat(MovementCon.DEFAULT.withPenaltyAreaTheirObstacle(false).getConsideredPenAreas())
				.isEqualTo(edu.tigers.sumatra.ids.ETeam.TIGERS);
		assertThat(MovementCon.DEFAULT.withPenaltyAreaOurObstacle(false).withPenaltyAreaTheirObstacle(false)
				.getConsideredPenAreas()).isEqualTo(edu.tigers.sumatra.ids.ETeam.UNKNOWN);
	}
}
