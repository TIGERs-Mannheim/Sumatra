/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * All sorted game event detectors
 */
@Getter
@AllArgsConstructor
public enum EGameEventDetectorType implements IInstanceableEnum
{
	ATTACKER_TO_DEFENSE_AREA_DISTANCE(new InstanceableClass<>(AttackerToDefenseAreaDistanceDetector.class)),
	BOT_IN_DEFENSE_AREA(new InstanceableClass<>(BotInDefenseAreaDetector.class)),
	DRIBBLING(new InstanceableClass<>(DribblingDetector.class)),

	BALL_SPEEDING(new InstanceableClass<>(BallSpeedingDetector.class)),
	BOT_COLLISION(new InstanceableClass<>(BotCollisionDetector.class)),

	DEFENDER_TO_KICK_POINT_DISTANCE(new InstanceableClass<>(DefenderToKickPointDistanceDetector.class)),
	BOT_STOP_SPEED(new InstanceableClass<>(BotStopSpeedDetector.class)),
	BALL_PLACEMENT_INTERFERENCE(new InstanceableClass<>(BallPlacementInterferenceDetector.class)),

	GOAL(new InstanceableClass<>(GoalDetector.class)),

	DOUBLE_TOUCH(new InstanceableClass<>(DoubleTouchDetector.class)),
	BALL_PLACEMENT_SUCCEEDED(new InstanceableClass<>(BallPlacementSucceededDetector.class)),

	BALL_LEFT_FIELD(new InstanceableClass<>(BallLeftFieldDetector.class)),
	BOUNDARY_CROSSING(new InstanceableClass<>(BoundaryCrossingDetector.class)),

	PUSHING(new InstanceableClass<>(PushingDetector.class), false),

	PENALTY_KICK_FAILED(new InstanceableClass<>(PenaltyKickFailedDetector.class)),
	;

	private final InstanceableClass<?> instanceableClass;
	private final boolean enabledByDefault;


	EGameEventDetectorType(final InstanceableClass<?> instanceableClass)
	{
		this(instanceableClass, true);
	}


	public static Set<EGameEventDetectorType> valuesEnabledByDefault()
	{
		return Arrays.stream(values())
				.filter(EGameEventDetectorType::isEnabledByDefault)
				.collect(Collectors.toSet());
	}


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
