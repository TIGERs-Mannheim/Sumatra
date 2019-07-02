/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.gameevent;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.SslGameEvent.GameEventType;


/**
 * AutoRef game events and mapping to human readable texts and referee protocol enum
 */
public enum EGameEvent implements IInstanceableEnum
{
	// Match Proceeding
	PREPARED(GameEventType.PREPARED, Prepared.class, EGameEventType.MATCH_PROCEEDING),
	NO_PROGRESS_IN_GAME(GameEventType.NO_PROGRESS_IN_GAME, NoProgressInGame.class, EGameEventType.MATCH_PROCEEDING),
	PLACEMENT_FAILED(GameEventType.PLACEMENT_FAILED, PlacementFailed.class, EGameEventType.MATCH_PROCEEDING),
	PLACEMENT_SUCCEEDED(GameEventType.PLACEMENT_SUCCEEDED, PlacementSucceeded.class, EGameEventType.MATCH_PROCEEDING),
	BOT_SUBSTITUTION(GameEventType.BOT_SUBSTITUTION, BotSubstitution.class, EGameEventType.MATCH_PROCEEDING),
	TOO_MANY_ROBOTS(GameEventType.TOO_MANY_ROBOTS, TooManyRobots.class, EGameEventType.MATCH_PROCEEDING),


	// Ball out of field
	BALL_LEFT_FIELD_GOAL_LINE(GameEventType.BALL_LEFT_FIELD_GOAL_LINE, BallLeftFieldGoalLine.class,
			EGameEventType.BALL_LEFT_FIELD),
	BALL_LEFT_FIELD_TOUCH_LINE(GameEventType.BALL_LEFT_FIELD_TOUCH_LINE, BallLeftFieldTouchLine.class,
			EGameEventType.BALL_LEFT_FIELD),
	GOAL(GameEventType.GOAL, Goal.class, EGameEventType.BALL_LEFT_FIELD),
	POSSIBLE_GOAL(GameEventType.POSSIBLE_GOAL, PossibleGoal.class, EGameEventType.BALL_LEFT_FIELD),
	INDIRECT_GOAL(GameEventType.INDIRECT_GOAL, IndirectGoal.class, EGameEventType.BALL_LEFT_FIELD),
	CHIP_ON_GOAL(GameEventType.CHIPPED_GOAL, ChippedGoal.class, EGameEventType.BALL_LEFT_FIELD),


	// Minor offense
	AIMLESS_KICK(GameEventType.AIMLESS_KICK, AimlessKick.class, EGameEventType.MINOR_OFFENSE),
	KICK_TIMEOUT(GameEventType.KICK_TIMEOUT, KickTimeout.class, EGameEventType.MINOR_OFFENSE),
	KEEPER_HELD_BALL(GameEventType.KEEPER_HELD_BALL, KeeperHeldBall.class, EGameEventType.MINOR_OFFENSE),
	ATTACKER_DOUBLE_TOUCHED_BALL(GameEventType.ATTACKER_DOUBLE_TOUCHED_BALL, AttackerDoubleTouchedBall.class,
			EGameEventType.MINOR_OFFENSE),
	ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA(
			GameEventType.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA,
			AttackerTouchedBallInDefenseArea.class,
			EGameEventType.MINOR_OFFENSE),
	BOT_DRIBBLED_BALL_TOO_FAR(GameEventType.BOT_DRIBBLED_BALL_TOO_FAR, BotDribbledBallTooFar.class,
			EGameEventType.MINOR_OFFENSE),
	BOT_KICKED_BALL_TOO_FAST(GameEventType.BOT_KICKED_BALL_TOO_FAST, BotKickedBallToFast.class,
			EGameEventType.MINOR_OFFENSE),


	// Fouls
	ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA(GameEventType.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA,
			AttackerTooCloseToDefenseArea.class, EGameEventType.FOUL),
	BOT_INTERFERED_PLACEMENT(GameEventType.BOT_INTERFERED_PLACEMENT, BotInterferedPlacement.class, EGameEventType.FOUL),
	BOT_CRASH_DRAWN(GameEventType.BOT_CRASH_DRAWN, BotCrashDrawn.class, EGameEventType.FOUL),
	BOT_CRASH_UNIQUE(GameEventType.BOT_CRASH_UNIQUE, BotCrashUnique.class, EGameEventType.FOUL),
	BOT_CRASH_UNIQUE_SKIPPED(GameEventType.BOT_CRASH_UNIQUE_SKIPPED, BotCrashUnique.class, EGameEventType.FOUL),
	BOT_PUSHED_BOT(GameEventType.BOT_PUSHED_BOT, BotPushedBot.class, EGameEventType.FOUL),
	BOT_PUSHED_BOT_SKIPPED(GameEventType.BOT_PUSHED_BOT_SKIPPED, BotPushedBot.class, EGameEventType.FOUL),
	BOT_HELD_BALL_DELIBERATELY(GameEventType.BOT_HELD_BALL_DELIBERATELY, BotHeldBallDeliberately.class,
			EGameEventType.FOUL),
	BOT_TIPPED_OVER(GameEventType.BOT_TIPPED_OVER, BotTippedOver.class, EGameEventType.FOUL),
	BOT_TOO_FAST_IN_STOP(GameEventType.BOT_TOO_FAST_IN_STOP, BotTooFastInStop.class, EGameEventType.FOUL),
	DEFENDER_TOO_CLOSE_TO_KICK_POINT(GameEventType.DEFENDER_TOO_CLOSE_TO_KICK_POINT, DefenderTooCloseToKickPoint.class,
			EGameEventType.FOUL),
	DEFENDER_IN_DEFENSE_AREA_PARTIALLY(GameEventType.DEFENDER_IN_DEFENSE_AREA_PARTIALLY,
			DefenderInDefenseAreaPartially.class, EGameEventType.FOUL),
	DEFENDER_IN_DEFENSE_AREA(GameEventType.DEFENDER_IN_DEFENSE_AREA, DefenderInDefenseArea.class, EGameEventType.FOUL),
	ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA(
			GameEventType.ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA,
			AttackerTouchedOpponentInDefenseArea.class,
			EGameEventType.FOUL),
	ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA_SKIPPED(
			GameEventType.ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA_SKIPPED,
			AttackerTouchedOpponentInDefenseArea.class,
			EGameEventType.FOUL),


	// Repeated
	MULTIPLE_CARDS(GameEventType.MULTIPLE_CARDS, MultipleCards.class, EGameEventType.REPEATED),
	MULTIPLE_PLACEMENT_FAILURES(GameEventType.MULTIPLE_PLACEMENT_FAILURES, MultiplePlacementFailures.class,
			EGameEventType.REPEATED),
	MULTIPLE_FOULS(GameEventType.MULTIPLE_FOULS, MultipleFouls.class, EGameEventType.REPEATED),

	// Unsporting behaviors
	UNSPORTING_BEHAVIOR_MINOR(GameEventType.UNSPORTING_BEHAVIOR_MINOR, UnsportingBehaviorMinor.class,
			EGameEventType.UNSPORTING),
	UNSPORTING_BEHAVIOR_MAJOR(GameEventType.UNSPORTING_BEHAVIOR_MAJOR, UnsportingBehaviorMajor.class,
			EGameEventType.UNSPORTING),

	@Deprecated // only for compatibility with older Berkeley DBs
	ATTACKER_IN_DEFENSE_AREA(GameEventType.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA, AttackerTouchedBallInDefenseArea.class,
			EGameEventType.MINOR_OFFENSE),
	@Deprecated // only for compatibility with older Berkeley DBs
	ATTACKER_TOUCH_KEEPER(
			GameEventType.ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA,
			AttackerTouchedOpponentInDefenseArea.class,
			EGameEventType.FOUL),
			;


	private final InstanceableClass impl;
	private final GameEventType protoType;
	private final EGameEventType type;

	private static final Map<GameEventType, EGameEvent> EVENT_TYPE_MAP = new EnumMap<>(
			SslGameEvent.GameEventType.class);


	EGameEvent(final GameEventType protoType, final Class<? extends IGameEvent> wrapperImpl, final EGameEventType type)
	{
		this.impl = new InstanceableClass(wrapperImpl, new InstanceableParameter(SslGameEvent.GameEvent.class, "", null));
		this.protoType = protoType;
		this.type = type;
	}


	@Override
	public InstanceableClass getInstanceableClass()
	{
		return impl;
	}


	public GameEventType getProtoType()
	{
		return protoType;
	}


	public EGameEventType getType()
	{
		return type;
	}


	public static EGameEvent fromProto(GameEventType type)
	{
		return EVENT_TYPE_MAP.computeIfAbsent(type, e -> searchProto(type));
	}


	/**
	 * Note: This has to be kept in sync with the actually marked values.
	 *
	 * @return all non deprecated values
	 */
	public static EGameEvent[] valuesNonDeprecated()
	{
		return Arrays.copyOf(values(), values().length - 2);
	}


	private static EGameEvent searchProto(GameEventType type)
	{
		for (EGameEvent event : values())
		{
			if (event.getProtoType() == type)
			{
				return event;
			}
		}
		throw new IllegalArgumentException("Could not map type: " + type);
	}
}
