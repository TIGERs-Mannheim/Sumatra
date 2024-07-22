/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPenAreaRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPlaceholderRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.Man2ManMarkerRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.PassDisruptionRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DelayedAttackRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DisruptOpponentRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.FreeSkirmishRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KeepDistToBallRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OneOnOneShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OpponentInterceptionRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.PassReceiverRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.SupportiveAttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.placement.BallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ai.pandora.roles.test.DribbleKickTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.CrookedKickSamplerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.IdentifyBotModelRole;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.StraightChipKickSamplerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.KickTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.move.AroundCircleMoveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.move.CurvedMoveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.move.EnduranceRole;
import edu.tigers.sumatra.ai.pandora.roles.test.move.MoveTestRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.github.g3force.instanceables.InstanceableClass.ic;


/**
 * Enumeration that represents the different {@link ARole}s
 */
@SuppressWarnings("squid:S1192") // duplicated strings
@Getter
@AllArgsConstructor
public enum ERole implements IInstanceableEnum
{
	// offense

	ATTACKER(ic(AttackerRole.class)),
	SUPPORTIVE_ATTACKER(ic(SupportiveAttackerRole.class)),
	KEEP_DIST_TO_BALL(ic(KeepDistToBallRole.class)),
	OPPONENT_INTERCEPTION(ic(OpponentInterceptionRole.class)),
	FREE_SKIRMISH(ic(FreeSkirmishRole.class)),
	DELAYED_ATTACK(ic(DelayedAttackRole.class)),
	PASS_RECEIVER(ic(PassReceiverRole.class)),
	DISRUPT_OPPONENT(ic(DisruptOpponentRole.class)),

	// support

	SUPPORT(ic(SupportRole.class)),

	// defense

	KEEPER(ic(KeeperRole.class)),
	MAN_2_MAN_MARKER(ic(Man2ManMarkerRole.class)),
	PASS_DISRUPTION_DEFENDER(ic(PassDisruptionRole.class)),
	CENTER_BACK(ic(CenterBackRole.class)),
	DEFENDER_PLACEHOLDER(ic(DefenderPlaceholderRole.class)),
	DEFENDER_PEN_AREA(ic(DefenderPenAreaRole.class)
			.setterParam(IVector2.class, "destination", "0.0", DefenderPenAreaRole::setDestination)
	),

	// standards and common match roles

	ONE_ON_ONE_SHOOTER(ic(OneOnOneShooterRole.class)),
	BALL_PLACEMENT(ic(BallPlacementRole.class)
			.setterParam(IVector2.class, "placementPos", "0,0", BallPlacementRole::setBallTargetPos)
	),
	MOVE(ic(MoveRole.class)
			.setterParam(IVector2.class, "destination", "1000,0", MoveRole::updateDestination)
			.setterParam(Double.TYPE, "orientation", "0.0", MoveRole::updateTargetAngle)
	),

	// other non-match roles

	MOVE_TEST(ic(MoveTestRole.class,
			new InstanceableParameter(MoveTestRole.EMoveMode.class, "mode", "TRAJ_VEL"),
			new InstanceableParameter(IVector2.class, "initPos", "1400,1400"),
			new InstanceableParameter(IVector2.class, "finalPos", "-1400,-1400"),
			new InstanceableParameter(IVector2.class, "primaryDir", "0,0"),
			new InstanceableParameter(Double.TYPE, "startAngleDeg", "0"),
			new InstanceableParameter(Double.TYPE, "stopAngleDeg", "180"),
			new InstanceableParameter(Double.TYPE, "stepAngleDeg", "10"),
			new InstanceableParameter(Double.TYPE, "angleTurn", "0"),
			new InstanceableParameter(Boolean.TYPE, "fastMove", "false"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"),
			new InstanceableParameter(Boolean.TYPE, "rollOut", "false"),
			new InstanceableParameter(Boolean.TYPE, "continuousCapture", "false"))
	),

	ENDURANCE(ic(EnduranceRole.class)
			.setterParam(IVector2[].class, "destinations", "1000,1000;-1000,1000;-1000,-1000;1000,-1000",
					EnduranceRole::setDestinations)
			.setterParam(Double.TYPE, "nearDestTolerance", "10", EnduranceRole::setNearDestTolerance)
	),

	AROUND_CIRCLE_MOVE_TEST(ic(AroundCircleMoveTestRole.class,
			new InstanceableParameter(IVector2.class, "initPos", "-1200,0"),
			new InstanceableParameter(IVector2.class, "obstacleCenter", "0,0"),
			new InstanceableParameter(Double.TYPE, "obstacleRadius", "500"),
			new InstanceableParameter(Boolean.TYPE, "rotate", "true"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"),
			new InstanceableParameter(Boolean.TYPE, "continuousCapture", "true"))
	),

	CURVED_MOVE_TEST(ic(CurvedMoveTestRole.class,
			new InstanceableParameter(IVector2.class, "initPos", "1000,0"),
			new InstanceableParameter(IVector2.class, "intermediatePos", "0,0"),
			new InstanceableParameter(Double.TYPE, "intermediateDuration", "0.5"),
			new InstanceableParameter(IVector2.class, "finalPos", "0,1000"),
			new InstanceableParameter(Double.TYPE, "initOrientation", "0"),
			new InstanceableParameter(Double.TYPE, "intermediateOrientation", "0"),
			new InstanceableParameter(Double.TYPE, "finalOrientation", "0"),
			new InstanceableParameter(IVector2.class, "primaryDir", "0,0"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"),
			new InstanceableParameter(Boolean.TYPE, "continuousCapture", "true"))
	),

	IDENTIFY_BOT_MODEL(ic(IdentifyBotModelRole.class,
			new InstanceableParameter(IVector2.class, "startPos", "-1500,0"),
			new InstanceableParameter(IVector2.class, "endPos", "1500,0"),
			new InstanceableParameter(Double.TYPE, "accMaxXY", "2"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "12"),
			new InstanceableParameter(Double[].class, "velXY", "0.5; 1.0; 1.25; 1.5"),
			new InstanceableParameter(Double[].class, "velW", "4; 6; 8; 10"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"))
	),

	DRIBBLE_KICK_TEST(ic(DribbleKickTestRole.class)
			.setterParam(IVector2.class, "initial look at target", "0,0", DribbleKickTestRole::setTarget)
			.setterParam(IVector2.class, "kickTarget", "6000,0", DribbleKickTestRole::setKickTarget)
			.setterParam(IVector2.class, "dribbleToPos", "4000,1000", DribbleKickTestRole::setDribbleToPos)),


	KICK_TEST(ic(KickTestRole.class,
			new InstanceableParameter(DynamicPosition.class, "passTarget", "4500,0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "6.5"))
	),

	STRAIGHT_CHIP_KICK_SAMPLER(ic(StraightChipKickSamplerRole.class,
			new InstanceableParameter(Boolean.TYPE, "onlyOurHalf", "false"),
			new InstanceableParameter(Boolean.TYPE, "chipFromSide", "false"),
			new InstanceableParameter(IVector2.class, "kickCorner", "1,1"),
			new InstanceableParameter(Double.TYPE, "minDurationMs", "1.0"),
			new InstanceableParameter(Double.TYPE, "maxDurationMs", "10.0"),
			new InstanceableParameter(Double.TYPE, "stepSize", "1"),
			new InstanceableParameter(Boolean.TYPE, "continueSampling", "false"),
			new InstanceableParameter(Boolean.TYPE, "doChip", "true"),
			new InstanceableParameter(Boolean.TYPE, "doStraight", "true"))
	),

	CROOKED_KICK_SAMPLER(ic(CrookedKickSamplerRole.class,
			new InstanceableParameter(IVector2.class, "kickPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "orientation", "0.0"),
			new InstanceableParameter(Double.TYPE, "kickDurationMs", "4.0"),
			new InstanceableParameter(Double.TYPE, "offsetCenter", "20.0"),
			new InstanceableParameter(Integer.TYPE, "numSamples", "5"),
			new InstanceableParameter(Boolean.TYPE, "continue", "true"))
	),


	;

	private final InstanceableClass<?> instanceableClass;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
