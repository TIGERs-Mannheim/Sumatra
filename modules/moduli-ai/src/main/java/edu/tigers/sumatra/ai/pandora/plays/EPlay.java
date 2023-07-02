/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;
import edu.tigers.sumatra.ai.pandora.plays.match.DefensePlay;
import edu.tigers.sumatra.ai.pandora.plays.match.KeeperPlay;
import edu.tigers.sumatra.ai.pandora.plays.match.OffensivePlay;
import edu.tigers.sumatra.ai.pandora.plays.match.SupportPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.BallPlacementPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.MaintenancePlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.PenaltyThemPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.RobotInterchangePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.BallPlacementTestPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.GuiTestPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.SnapshotPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ARedirectPlay.EReceiveMode;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.PassAroundACirclePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.PassInACirclePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.PassToEachOtherPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.PassingTechChallengePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.RedirectTrianglePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ReproducibleKickLyingPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ReproducibleKickRollingBallPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.move.AroundTheBallPlay;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.github.g3force.instanceables.InstanceableClass.ic;


/**
 * All available plays
 */
@SuppressWarnings("java:S1192") // duplicate string literals
@Getter
@AllArgsConstructor
public enum EPlay implements IInstanceableEnum
{
	KEEPER(new InstanceableClass<>(KeeperPlay.class)),
	PENALTY_THEM(new InstanceableClass<>(PenaltyThemPlay.class)),
	BALL_PLACEMENT(new InstanceableClass<>(BallPlacementPlay.class)),
	OFFENSIVE(new InstanceableClass<>(OffensivePlay.class)),
	DEFENSIVE(new InstanceableClass<>(DefensePlay.class)),
	SUPPORT(new InstanceableClass<>(SupportPlay.class)),
	INTERCHANGE(new InstanceableClass<>(RobotInterchangePlay.class)),
	MAINTENANCE(ic(MaintenancePlay.class)
			.setterParam(Double.TYPE, "startingXPos", "0.5", MaintenancePlay::setStartingXPos)
			.setterParam(IVector2.class, "direction", "0,250", MaintenancePlay::setDirection)
			.setterParam(Double.TYPE, "orientation", "0", MaintenancePlay::setOrientation)
	),
	CHEERING(new InstanceableClass<>(CheeringPlay.class)),

	GUI_TEST(new InstanceableClass<>(GuiTestPlay.class)),

	AROUND_THE_BALL(new InstanceableClass<>(AroundTheBallPlay.class,
			new InstanceableParameter(Double.TYPE, "radius", "500")
	)),
	SNAPSHOT(new InstanceableClass<>(SnapshotPlay.class)
			.setterParam(String.class, "snapshot file", "data/snapshots/hwc1s01.json", SnapshotPlay::setSnapshotFile)
	),

	PASSING_TECH_CHALLENGE(ic(PassingTechChallengePlay.class)
			.setterParam(IVector2.class, "center", "0.0;0.0", PassingTechChallengePlay::setCenter)
			.setterParam(Double.TYPE, "radius", "1000", PassingTechChallengePlay::setRadius)
			.setterParam(Double.TYPE, "maxReceivingBallSpeed", "2.5", PassingTechChallengePlay::setReceiveBallSpeed)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", PassingTechChallengePlay::setMinPassDuration)
			.setterParam(EKickerDevice.class, "kickerDevice", "STRAIGHT", PassingTechChallengePlay::setKickerDevice)
	),
	PASS_TO_EACH_OTHER(ic(PassToEachOtherPlay.class)
			.setterParam(IVector2.class, "p1", "-1000.0;1000.0", PassToEachOtherPlay::setP1)
			.setterParam(IVector2.class, "p2", "1000.0;-1000.0", PassToEachOtherPlay::setP2)
			.setterParam(EReceiveMode.class, "receiveMode1", "RECEIVE", PassToEachOtherPlay::setReceiveMode1)
			.setterParam(EReceiveMode.class, "receiveMode2", "REDIRECT", PassToEachOtherPlay::setReceiveMode2)
			.setterParam(Double.TYPE, "maxReceivingBallSpeed", "2.5", PassToEachOtherPlay::setReceiveBallSpeed)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", PassToEachOtherPlay::setMinPassDuration)
	),
	PASS_AROUND_A_CIRCLE(ic(PassAroundACirclePlay.class)
			.setterParam(IVector2.class, "center", "0.0;0.0", PassAroundACirclePlay::setCenter)
			.setterParam(Double.TYPE, "radius", "3000", PassAroundACirclePlay::setRadius)
			.setterParam(Double.TYPE, "maxReceivingBallSpeed", "2.5", PassAroundACirclePlay::setReceiveBallSpeed)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", PassAroundACirclePlay::setMinPassDuration)
	),
	PASS_IN_A_CIRCLE(ic(PassInACirclePlay.class)
			.setterParam(IVector2.class, "center", "0.0;0.0", PassInACirclePlay::setCenter)
			.setterParam(Double.TYPE, "radius", "3000", PassInACirclePlay::setRadius)
			.setterParam(Double.TYPE, "maxReceivingBallSpeed", "2.5", PassInACirclePlay::setReceiveBallSpeed)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", PassInACirclePlay::setMinPassDuration)
			.setterParam(EReceiveMode.class, "receiveMode", "false", PassInACirclePlay::setReceiveMode)
			.setterParam(EKickerDevice.class, "kickerDevice", "STRAIGHT", PassInACirclePlay::setKickerDevice)
			.setterParam(Boolean.TYPE, "moveOnCircle", "false", PassInACirclePlay::setMoveOnCircle)
			.setterParam(Double.TYPE, "rotationSpeed [m/s]", "1.0", PassInACirclePlay::setRotationSpeed)
	),
	REDIRECT_TRIANGLE(ic(RedirectTrianglePlay.class)
			.setterParam(IVector2.class, "redirectPos", "1500.0;0.0", RedirectTrianglePlay::setRedirectPos)
			.setterParam(IVector2.class, "direction", "-1.0;0.0", RedirectTrianglePlay::setDir)
			.setterParam(Double.TYPE, "distance", "2000", RedirectTrianglePlay::setDistance)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", RedirectTrianglePlay::setMinPassDuration)
			.setterParam(Double.TYPE, "angleMin [deg]", "30", RedirectTrianglePlay::setAngleDegMin)
			.setterParam(Double.TYPE, "angleMax [deg]", "70", RedirectTrianglePlay::setAngleDegMax)
			.setterParam(Double.TYPE, "angleDegChangeSpeed [deg/min]", "10", RedirectTrianglePlay::setAngleDegChangeSpeed)
			.setterParam(Double.TYPE, "maxReceiveBallSpeedMin [m/s]", "2.0",
					RedirectTrianglePlay::setMaxReceiveBallSpeedMin)
			.setterParam(Double.TYPE, "maxReceiveBallSpeedMax [m/s]", "2.0",
					RedirectTrianglePlay::setMaxReceiveBallSpeedMax)
			.setterParam(Double.TYPE, "maxReceiveBallSpeedChangeSpeed [m/s/min]", "0.4",
					RedirectTrianglePlay::setMaxReceiveBallSpeedChangeSpeed)
			.setterParam(Double.TYPE, "maxRedirectBallSpeedMin [m/s]", "2.0",
					RedirectTrianglePlay::setMaxRedirectBallSpeedMin)
			.setterParam(Double.TYPE, "maxRedirectBallSpeedMax [m/s]", "2.0",
					RedirectTrianglePlay::setMaxRedirectBallSpeedMax)
			.setterParam(Double.TYPE, "maxRedirectBallSpeedChangeSpeed [m/s/min]", "0.4",
					RedirectTrianglePlay::setMaxRedirectBallSpeedChangeSpeed)
			.setterParam(Boolean.TYPE, "doGoalKick", "false", RedirectTrianglePlay::setGoalKick)
	),

	REPRODUCIBLE_KICK_LYING_BALL(new InstanceableClass<>(ReproducibleKickLyingPlay.class,
			new InstanceableParameter(IVector2.class, "ballTargetPos", "1000.0;0.0"),
			new InstanceableParameter(IVector2.class, "botOffset", "500.0;0.0"),
			new InstanceableParameter(IVector2.class, "kickTarget", "-1950.0;0.0"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "3.0")
	)),
	REPRODUCIBLE_KICK_ROLLING_BALL(new InstanceableClass<>(ReproducibleKickRollingBallPlay.class,
			new InstanceableParameter(IVector2.class, "ballTargetPos", "-700.0;-700.0"),
			new InstanceableParameter(IVector2.class, "passTarget", "800.0;0.0"),
			new InstanceableParameter(IVector2.class, "botTargetPos", "-700.0;0.0"),
			new InstanceableParameter(Double.TYPE, "passSpeed", "2.0")
	)),
	BALL_PLACEMENT_TEST(new InstanceableClass<>(BallPlacementTestPlay.class,
			new InstanceableParameter(IVector2[].class, "placement positions", "-1000,-1000;1000,1000"))
	),

	;

	private final InstanceableClass<?> instanceableClass;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
