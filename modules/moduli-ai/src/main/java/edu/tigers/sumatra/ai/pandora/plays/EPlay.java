/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.ai.pandora.plays.standard.KickoffPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.MaintenancePlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.PenaltyThemPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.PenaltyWePlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.RobotInterchangePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.BallPlacementTestPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.GuiTestPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.PositioningPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.SnapshotPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ARedirectPlay.EReceiveMode;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.PassAroundACirclePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.PassInACirclePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.PassToEachOtherPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.PassingTechChallengePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.RedirectTrianglePlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ReproducibleKickLyingPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ReproducibleKickRollingBallPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ReproducibleRedirectPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.move.AroundTheBallPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.move.InitPlay;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.RedirectTestRole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.github.g3force.instanceables.InstanceableClass.ic;


/**
 * All available plays
 */
@Getter
@AllArgsConstructor
public enum EPlay implements IInstanceableEnum
{
	KEEPER(new InstanceableClass<>(KeeperPlay.class), 1),
	PENALTY_THEM(new InstanceableClass<>(PenaltyThemPlay.class), 5),
	PENALTY_WE(new InstanceableClass<>(PenaltyWePlay.class), 5),
	BALL_PLACEMENT(new InstanceableClass<>(BallPlacementPlay.class), 10),
	KICKOFF(new InstanceableClass<>(KickoffPlay.class), 10),
	OFFENSIVE(new InstanceableClass<>(OffensivePlay.class), 20),
	DEFENSIVE(new InstanceableClass<>(DefensePlay.class), 30),
	SUPPORT(new InstanceableClass<>(SupportPlay.class), 40),
	INTERCHANGE(new InstanceableClass<>(RobotInterchangePlay.class)),
	MAINTENANCE(ic(MaintenancePlay.class)
			.setterParam(IVector2.class, "startingPos", "0,0", MaintenancePlay::setStartingPos)
			.setterParam(IVector2.class, "direction", "0,250", MaintenancePlay::setDirection)
			.setterParam(Double.TYPE, "orientation", "0", MaintenancePlay::setOrientation)),
	CHEERING(new InstanceableClass<>(CheeringPlay.class)),

	GUI_TEST(new InstanceableClass<>(GuiTestPlay.class)),

	AROUND_THE_BALL(new InstanceableClass<>(AroundTheBallPlay.class,
			new InstanceableParameter(Double.TYPE, "radius", "500")
	)),
	INIT(new InstanceableClass<>(InitPlay.class)),
	POSITIONING_PLAY(new InstanceableClass<>(PositioningPlay.class)),
	SNAPSHOT(new InstanceableClass<>(SnapshotPlay.class)
			.setterParam(String.class, "snapshot file", "data/snapshots/hwc1s01.json", SnapshotPlay::setSnapshotFile)
	),

	PASSING_TECH_CHALLENGE(ic(PassingTechChallengePlay.class)
			.setterParam(IVector2.class, "center", "0.0;0.0", PassingTechChallengePlay::setCenter)
			.setterParam(Double.TYPE, "radius", "1000", PassingTechChallengePlay::setRadius)
			.setterParam(Double.TYPE, "maxReceivingBallSpeed", "2.5", PassingTechChallengePlay::setMaxReceivingBallSpeed)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", PassingTechChallengePlay::setMinPassDuration)
	),
	PASS_TO_EACH_OTHER(ic(PassToEachOtherPlay.class)
			.setterParam(IVector2.class, "p1", "-1000.0;1000.0", PassToEachOtherPlay::setP1)
			.setterParam(IVector2.class, "p2", "1000.0;-1000.0", PassToEachOtherPlay::setP2)
			.setterParam(EReceiveMode.class, "receiveMode1", "RECEIVE", PassToEachOtherPlay::setReceiveMode1)
			.setterParam(EReceiveMode.class, "receiveMode2", "REDIRECT", PassToEachOtherPlay::setReceiveMode2)
			.setterParam(Double.TYPE, "maxReceivingBallSpeed", "2.5", PassToEachOtherPlay::setMaxReceivingBallSpeed)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", PassToEachOtherPlay::setMinPassDuration)
	),
	PASS_AROUND_A_CIRCLE(ic(PassAroundACirclePlay.class)
			.setterParam(IVector2.class, "center", "0.0;0.0", PassAroundACirclePlay::setCenter)
			.setterParam(Double.TYPE, "radius", "3000", PassAroundACirclePlay::setRadius)
			.setterParam(Double.TYPE, "maxReceivingBallSpeed", "2.5", PassAroundACirclePlay::setMaxReceivingBallSpeed)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", PassAroundACirclePlay::setMinPassDuration)
	),
	PASS_IN_A_CIRCLE(ic(PassInACirclePlay.class)
			.setterParam(IVector2.class, "center", "0.0;0.0", PassInACirclePlay::setCenter)
			.setterParam(Double.TYPE, "radius", "3000", PassInACirclePlay::setRadius)
			.setterParam(Double.TYPE, "maxReceivingBallSpeed", "2.5", PassInACirclePlay::setMaxReceivingBallSpeed)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", PassInACirclePlay::setMinPassDuration)
			.setterParam(EReceiveMode.class, "receiveMode", "false", PassInACirclePlay::setReceiveMode)
			.setterParam(Boolean.TYPE, "moveOnCircle", "false", PassInACirclePlay::setMoveOnCircle)
			.setterParam(Double.TYPE, "rotationSpeed [m/s]", "1.0", PassInACirclePlay::setRotationSpeed)
	),
	REDIRECT_TRIANGLE(ic(RedirectTrianglePlay.class)
			.setterParam(IVector2.class, "redirectPos", "1500.0;-1200.0", RedirectTrianglePlay::setRedirectPos)
			.setterParam(IVector2.class, "direction", "-4.0;-1.0", RedirectTrianglePlay::setDir)
			.setterParam(Double.TYPE, "distance", "3000", RedirectTrianglePlay::setDistance)
			.setterParam(Double.TYPE, "minPassDuration", "0.0", RedirectTrianglePlay::setMinPassDuration)
			.setterParam(Double.TYPE, "angleMin [deg]", "15", RedirectTrianglePlay::setAngleDegMin)
			.setterParam(Double.TYPE, "angleMax [deg]", "70", RedirectTrianglePlay::setAngleDegMax)
			.setterParam(Double.TYPE, "angleDegChangeSpeed [deg/min]", "10", RedirectTrianglePlay::setAngleDegChangeSpeed)
			.setterParam(Double.TYPE, "maxReceivingBallSpeedMin [m/s]", "1.0",
					RedirectTrianglePlay::setMaxReceivingBallSpeedMin)
			.setterParam(Double.TYPE, "maxReceivingBallSpeedMax [m/s]", "3.0",
					RedirectTrianglePlay::setMaxReceivingBallSpeedMax)
			.setterParam(Double.TYPE, "maxReceivingBallSpeedChangeSpeed [m/s/min]", "0.4",
					RedirectTrianglePlay::setMaxReceivingBallSpeedChangeSpeed)
			.setterParam(Boolean.TYPE, "doGoalKick", "false", RedirectTrianglePlay::setGoalKick)
	),

	REPRODUCIBLE_REDIRECT(new InstanceableClass<>(ReproducibleRedirectPlay.class,
			new InstanceableParameter(IVector2.class, "ballPos", "1500.0;-1200.0"),
			new InstanceableParameter(IVector2.class, "receivingPos", "-500;0"),
			new InstanceableParameter(IVector2.class, "passTarget", "-500;0"),
			new InstanceableParameter(DynamicPosition.class, "redirectTarget", "1975.0;0.0"),
			new InstanceableParameter(Boolean.TYPE, "receive", "false"),
			new InstanceableParameter(RedirectTestRole.EKickMode.class, "kickMode", "NORMAL")
	)),
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
	private final int priority;


	EPlay(final InstanceableClass<?> instanceableClass)
	{
		this(instanceableClass, Integer.MAX_VALUE);
	}


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
