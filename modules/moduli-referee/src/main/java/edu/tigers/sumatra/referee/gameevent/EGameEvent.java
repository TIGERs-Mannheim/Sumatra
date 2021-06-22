/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.gameevent;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent.GameEvent.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

import static edu.tigers.sumatra.referee.gameevent.EGameEventType.BALL_LEFT_FIELD;
import static edu.tigers.sumatra.referee.gameevent.EGameEventType.FOUL;
import static edu.tigers.sumatra.referee.gameevent.EGameEventType.OTHER;


/**
 * AutoRef game events and mapping to human readable texts and referee protocol enum
 */
@Getter
@AllArgsConstructor
public enum EGameEvent implements IInstanceableEnum
{
	// Ball out of field events (stopping)
	BALL_LEFT_FIELD_GOAL_LINE(Type.BALL_LEFT_FIELD_GOAL_LINE, BallLeftFieldGoalLine.class, BALL_LEFT_FIELD),
	BALL_LEFT_FIELD_TOUCH_LINE(Type.BALL_LEFT_FIELD_TOUCH_LINE, BallLeftFieldTouchLine.class, BALL_LEFT_FIELD),
	AIMLESS_KICK(Type.AIMLESS_KICK, AimlessKick.class, BALL_LEFT_FIELD),

	// Stopping Fouls
	ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA(Type.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA, AttackerTooCloseToDefenseArea.class,
			FOUL),
	DEFENDER_IN_DEFENSE_AREA(Type.DEFENDER_IN_DEFENSE_AREA, DefenderInDefenseArea.class, FOUL),
	BOUNDARY_CROSSING(Type.BOUNDARY_CROSSING, BoundaryCrossing.class, FOUL),
	KEEPER_HELD_BALL(Type.KEEPER_HELD_BALL, KeeperHeldBall.class, FOUL),
	BOT_DRIBBLED_BALL_TOO_FAR(Type.BOT_DRIBBLED_BALL_TOO_FAR, BotDribbledBallTooFar.class, FOUL),

	BOT_PUSHED_BOT(Type.BOT_PUSHED_BOT, BotPushedBot.class, FOUL),
	BOT_HELD_BALL_DELIBERATELY(Type.BOT_HELD_BALL_DELIBERATELY, BotHeldBallDeliberately.class, FOUL),
	BOT_TIPPED_OVER(Type.BOT_TIPPED_OVER, BotTippedOver.class, FOUL),

	// Non-Stopping Fouls
	ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA(Type.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA,
			AttackerTouchedBallInDefenseArea.class, FOUL),
	BOT_KICKED_BALL_TOO_FAST(Type.BOT_KICKED_BALL_TOO_FAST, BotKickedBallToFast.class, FOUL),
	BOT_CRASH_UNIQUE(Type.BOT_CRASH_UNIQUE, BotCrashUnique.class, FOUL),
	BOT_CRASH_DRAWN(Type.BOT_CRASH_DRAWN, BotCrashDrawn.class, FOUL),

	// Fouls while ball out of play
	DEFENDER_TOO_CLOSE_TO_KICK_POINT(Type.DEFENDER_TOO_CLOSE_TO_KICK_POINT, DefenderTooCloseToKickPoint.class, FOUL),
	BOT_TOO_FAST_IN_STOP(Type.BOT_TOO_FAST_IN_STOP, BotTooFastInStop.class, FOUL),
	BOT_INTERFERED_PLACEMENT(Type.BOT_INTERFERED_PLACEMENT, BotInterferedPlacement.class, FOUL),

	// Scoring goals
	GOAL(Type.GOAL, Goal.class, BALL_LEFT_FIELD),
	POSSIBLE_GOAL(Type.POSSIBLE_GOAL, PossibleGoal.class, BALL_LEFT_FIELD),
	INVALID_GOAL(Type.INVALID_GOAL, InvalidGoal.class, BALL_LEFT_FIELD),

	// Other events
	ATTACKER_DOUBLE_TOUCHED_BALL(Type.ATTACKER_DOUBLE_TOUCHED_BALL, AttackerDoubleTouchedBall.class, OTHER),
	PLACEMENT_SUCCEEDED(Type.PLACEMENT_SUCCEEDED, PlacementSucceeded.class, OTHER),
	PENALTY_KICK_FAILED(Type.PENALTY_KICK_FAILED, PenaltyKickFailed.class, OTHER),

	NO_PROGRESS_IN_GAME(Type.NO_PROGRESS_IN_GAME, NoProgressInGame.class, OTHER),
	PLACEMENT_FAILED(Type.PLACEMENT_FAILED, PlacementFailed.class, OTHER),
	MULTIPLE_CARDS(Type.MULTIPLE_CARDS, MultipleCards.class, OTHER),
	MULTIPLE_FOULS(Type.MULTIPLE_FOULS, MultipleFouls.class, OTHER),
	BOT_SUBSTITUTION(Type.BOT_SUBSTITUTION, BotSubstitution.class, OTHER),
	TOO_MANY_ROBOTS(Type.TOO_MANY_ROBOTS, TooManyRobots.class, OTHER),

	UNSPORTING_BEHAVIOR_MINOR(Type.UNSPORTING_BEHAVIOR_MINOR, UnsportingBehaviorMinor.class, OTHER),
	UNSPORTING_BEHAVIOR_MAJOR(Type.UNSPORTING_BEHAVIOR_MAJOR, UnsportingBehaviorMajor.class, OTHER),

	;


	private final InstanceableClass<?> instanceableClass;
	private final Type protoType;
	private final EGameEventType type;

	private static final Map<Type, EGameEvent> EVENT_TYPE_MAP = new EnumMap<>(Type.class);


	EGameEvent(final Type protoType, final Class<? extends IGameEvent> wrapperImpl, final EGameEventType type)
	{
		this.instanceableClass = new InstanceableClass<>(wrapperImpl,
				new InstanceableParameter(SslGcGameEvent.GameEvent.class, "", null));
		this.protoType = protoType;
		this.type = type;
	}


	public static EGameEvent fromProto(Type type)
	{
		return EVENT_TYPE_MAP.computeIfAbsent(type, e -> searchProto(type));
	}


	private static EGameEvent searchProto(Type type)
	{
		for (EGameEvent event : values())
		{
			if (event.getProtoType() == type)
			{
				return event;
			}
		}
		return null;
	}


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
