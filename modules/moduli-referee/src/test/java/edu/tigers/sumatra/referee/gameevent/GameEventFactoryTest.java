package edu.tigers.sumatra.referee.gameevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


@SuppressWarnings("OptionalGetWithoutIsPresent") // false positives with assertJ
public class GameEventFactoryTest
{

	@Test
	public void testFromProtobuf()
	{
		List<IGameEvent> gameEvents = new ArrayList<>();
		gameEvents.add(new AimlessKick(botId(), location(), location()));
		gameEvents.add(new AttackerDoubleTouchedBall(botId(), location()));
		gameEvents.add(new AttackerTouchedBallInDefenseArea(botId(), location(), number()));
		gameEvents.add(new AttackerTooCloseToDefenseArea(botId(), location(), number()));
		gameEvents.add(new AttackerTouchedOpponentInDefenseArea(botId(), botId(), location()));
		gameEvents.add(
				new AttackerTouchedOpponentInDefenseArea(EGameEvent.ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA_SKIPPED,
						botId(),
						botId(), location()));
		gameEvents.add(new BallLeftFieldGoalLine(botId(), location()));
		gameEvents.add(new BallLeftFieldTouchLine(botId(), location()));
		gameEvents.add(new BotCrashDrawn(yellowBot(), blueBot(), location(), number(), number(), number()));
		gameEvents.add(new BotCrashUnique(botId(), botId(), location(), number(), number(), number()));
		gameEvents.add(new BotCrashUnique(EGameEvent.BOT_CRASH_UNIQUE_SKIPPED, botId(), botId(), location(), number(),
				number(), number()));
		gameEvents.add(new BotDribbledBallTooFar(botId(), location(), location()));
		gameEvents.add(new BotHeldBallDeliberately(botId(), location(), number()));
		gameEvents.add(new BotInterferedPlacement(botId(), location()));
		gameEvents.add(new BotKickedBallToFast(botId(), location(), number(), BotKickedBallToFast.EKickType.STRAIGHT));
		gameEvents.add(new BotPushedBot(botId(), botId(), location(), number()));
		gameEvents.add(new BotPushedBot(EGameEvent.BOT_PUSHED_BOT_SKIPPED, botId(), botId(), location(), number()));
		gameEvents.add(new BotSubstitution(team()));
		gameEvents.add(new BotTippedOver(botId(), location()));
		gameEvents.add(new BotTooFastInStop(botId(), location(), number()));
		gameEvents.add(new ChippedGoal(botId(), location(), location(), number()));
		gameEvents.add(new DefenderInDefenseArea(botId(), location(), number()));
		gameEvents.add(new DefenderInDefenseAreaPartially(botId(), location(), number()));
		gameEvents.add(new DefenderTooCloseToKickPoint(botId(), location(), number()));
		gameEvents.add(new Goal(team(), botId(), location(), location()));
		gameEvents.add(new IndirectGoal(botId(), location(), location()));
		gameEvents.add(new KeeperHeldBall(team(), location(), number()));
		gameEvents.add(new KickTimeout(team(), location(), number()));
		gameEvents.add(new MultipleCards(team()));
		gameEvents.add(new MultipleFouls(team()));
		gameEvents.add(new MultiplePlacementFailures(team()));
		gameEvents.add(new NoProgressInGame(location(), number()));
		gameEvents.add(new PlacementFailed(team(), number()));
		gameEvents.add(new PlacementSucceeded(team(), number(), number(), number()));
		gameEvents.add(new PossibleGoal(team(), botId(), location(), location()));
		gameEvents.add(new Prepared(number()));
		gameEvents.add(new TooManyRobots(team()));
		gameEvents.add(new UnsportingBehaviorMajor(team(), "reason"));
		gameEvents.add(new UnsportingBehaviorMinor(team(), "reason"));

		for (IGameEvent gameEvent : gameEvents)
		{
			final SslGameEvent.GameEvent protoGameEvent = gameEvent.toProtobuf();
			final Optional<IGameEvent> convertedGameEvent = GameEventFactory.fromProtobuf(protoGameEvent);
			assertThat(convertedGameEvent).isPresent();
			assertThat(convertedGameEvent.get()).isEqualTo(gameEvent);
		}

		assertThat(gameEvents.stream().map(IGameEvent::getType))
				.containsAll(Arrays.asList(EGameEvent.valuesNonDeprecated()));
	}


	private ETeamColor team()
	{
		return ETeamColor.BLUE;
	}


	private BotID botId()
	{
		return BotID.createBotId(2, ETeamColor.YELLOW);
	}


	private BotID blueBot()
	{
		return BotID.createBotId(2, ETeamColor.BLUE);
	}


	private BotID yellowBot()
	{
		return BotID.createBotId(2, ETeamColor.YELLOW);
	}


	private IVector2 location()
	{
		return Vector2.fromXY(4200, 2000);
	}


	private double number()
	{
		return 2000.0;
	}
}
