/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPenAreaRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPlaceholderRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.ManToManMarkerRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.PenaltyKeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.path.MoveAlongPathRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DelayedAttackRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DisruptOpponentRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.EpicPenaltyShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.FreeSkirmishRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KeepDistToBallRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OneOnOneShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OpponentInterceptionRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.PassReceiverRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.SupportiveAttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.placement.BallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ai.pandora.roles.test.ContestedPossessionChallengeRole;
import edu.tigers.sumatra.ai.pandora.roles.test.DribbleChallengeAdvancedRole;
import edu.tigers.sumatra.ai.pandora.roles.test.DribbleChallengeSimpleRole;
import edu.tigers.sumatra.ai.pandora.roles.test.DribbleTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.CrookedKickSamplerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.IdentifyBotModelRole;
import edu.tigers.sumatra.ai.pandora.roles.test.calibrate.StraightChipKickSamplerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.KickTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.RedirectTestRole;
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
	// offensive roles

	ATTACKER(new InstanceableClass<>(AttackerRole.class)),
	SUPPORTIVE_ATTACKER(new InstanceableClass<>(SupportiveAttackerRole.class)),
	KEEP_DIST_TO_BALL(new InstanceableClass<>(KeepDistToBallRole.class)),
	OPPONENT_INTERCEPTION(new InstanceableClass<>(OpponentInterceptionRole.class)),
	FREE_SKIRMISH(new InstanceableClass<>(FreeSkirmishRole.class)),
	DELAYED_ATTACK(new InstanceableClass<>(DelayedAttackRole.class)),
	PASS_RECEIVER(new InstanceableClass<>(PassReceiverRole.class)),
	DISRUPT_OPPONENT(new InstanceableClass<>(DisruptOpponentRole.class)),

	SUPPORT(new InstanceableClass<>(SupportRole.class)),

	// defense

	KEEPER(new InstanceableClass<>(KeeperRole.class)),

	MAN_TO_MAN_MARKER(new InstanceableClass<>(ManToManMarkerRole.class,
			new InstanceableParameter(DynamicPosition.class, "opponentBot", "0 B"))),

	CENTER_BACK(new InstanceableClass<>(CenterBackRole.class,
			new InstanceableParameter(DynamicPosition.class, "Threat", "0 B"),
			new InstanceableParameter(CenterBackRole.CoverMode.class, "coverMode", "CENTER"))),

	DEFENDER_PLACEHOLDER(new InstanceableClass<>(DefenderPlaceholderRole.class)),

	DEFENDER_PEN_AREA(new InstanceableClass<>(DefenderPenAreaRole.class,
			new InstanceableParameter(Boolean.class, "ballAsReference", "true"))),

	// Standards
	PENALTY_KEEPER(new InstanceableClass<>(PenaltyKeeperRole.class)),
	PENALTY_ATTACKER(new InstanceableClass<>(OneOnOneShooterRole.class)),
	EPIC_PENALTY_SHOOTER(new InstanceableClass<>(EpicPenaltyShooterRole.class)),

	BALL_PLACEMENT(ic(BallPlacementRole.class)
			.setterParam(IVector2.class, "placementPos", "0,0", BallPlacementRole::setBallTargetPos)
	),

	DRIBBLE_TEST(new InstanceableClass<>(DribbleTestRole.class)
			.setterParam(IVector2.class, "target", "0,0", DribbleTestRole::setTarget)
			.setterParam(Double.TYPE, "velMax", "3", DribbleTestRole::setVelMax)),

	MOVE_TEST(new InstanceableClass<>(MoveTestRole.class,
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
			new InstanceableParameter(Boolean.TYPE, "continuousCapture", "false"))),

	ENDURANCE(ic(EnduranceRole.class)
			.setterParam(IVector2[].class, "destinations", "1000,1000;-1000,1000;-1000,-1000;1000,-1000",
					EnduranceRole::setDestinations)
			.setterParam(Double.TYPE, "nearDestTolerance", "10", EnduranceRole::setNearDestTolerance)),

	AROUND_CIRCLE_MOVE_TEST(new InstanceableClass<>(AroundCircleMoveTestRole.class,
			new InstanceableParameter(IVector2.class, "initPos", "-1200,0"),
			new InstanceableParameter(IVector2.class, "obstacleCenter", "0,0"),
			new InstanceableParameter(Double.TYPE, "obstacleRadius", "500"),
			new InstanceableParameter(Boolean.TYPE, "rotate", "true"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"),
			new InstanceableParameter(Boolean.TYPE, "continuousCapture", "true"))),

	CURVED_MOVE_TEST(new InstanceableClass<>(CurvedMoveTestRole.class,
			new InstanceableParameter(IVector2.class, "initPos", "1000,0"),
			new InstanceableParameter(IVector2.class, "intermediatePos", "0,0"),
			new InstanceableParameter(Double.TYPE, "intermediateDuration", "0.5"),
			new InstanceableParameter(IVector2.class, "finalPos", "0,1000"),
			new InstanceableParameter(Double.TYPE, "initOrientation", "0"),
			new InstanceableParameter(Double.TYPE, "intermediateOrientation", "0"),
			new InstanceableParameter(Double.TYPE, "finalOrientation", "0"),
			new InstanceableParameter(IVector2.class, "primaryDir", "0,0"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"),
			new InstanceableParameter(Boolean.TYPE, "continuousCapture", "true"))),

	IDENTIFY_BOT_MODEL(new InstanceableClass<>(IdentifyBotModelRole.class,
			new InstanceableParameter(IVector2.class, "startPos", "-1500,0"),
			new InstanceableParameter(IVector2.class, "endPos", "1500,0"),
			new InstanceableParameter(Double.TYPE, "accMaxXY", "2"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "12"),
			new InstanceableParameter(Double[].class, "velXY", "0.5; 1.0; 1.25; 1.5"),
			new InstanceableParameter(Double[].class, "velW", "4; 6; 8; 10"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"))),

	KICK_TEST(new InstanceableClass<>(KickTestRole.class,
			new InstanceableParameter(DynamicPosition.class, "passTarget", "4500,0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "6.5"))),

	REDIRECT_TEST(new InstanceableClass<>(RedirectTestRole.class,
			new InstanceableParameter(DynamicPosition.class, "target", "0,0"))),

	STRAIGHT_CHIP_KICK_SAMPLER(new InstanceableClass<>(StraightChipKickSamplerRole.class,
			new InstanceableParameter(Boolean.TYPE, "onlyOurHalf", "false"),
			new InstanceableParameter(Boolean.TYPE, "chipFromSide", "false"),
			new InstanceableParameter(IVector2.class, "kickCorner", "1,1"),
			new InstanceableParameter(Double.TYPE, "minDurationMs", "1.0"),
			new InstanceableParameter(Double.TYPE, "maxDurationMs", "10.0"),
			new InstanceableParameter(Double.TYPE, "stepSize", "1"),
			new InstanceableParameter(Boolean.TYPE, "continueSampling", "false"),
			new InstanceableParameter(Boolean.TYPE, "doChip", "true"),
			new InstanceableParameter(Boolean.TYPE, "doStraight", "true"))),

	CROOKED_KICK_SAMPLER(new InstanceableClass<>(CrookedKickSamplerRole.class,
			new InstanceableParameter(IVector2.class, "kickPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "orientation", "0.0"),
			new InstanceableParameter(Double.TYPE, "kickDurationMs", "4.0"),
			new InstanceableParameter(Double.TYPE, "offsetCenter", "20.0"),
			new InstanceableParameter(Integer.TYPE, "numSamples", "5"),
			new InstanceableParameter(Boolean.TYPE, "continue", "true"))),

	MOVE(ic(MoveRole.class)
			.setterParam(IVector2.class, "destination", "1000,0", MoveRole::updateDestination)
			.setterParam(Double.TYPE, "orientation", "0.0", MoveRole::updateTargetAngle)
	),

	MOVE_ALONG_PATH(new InstanceableClass<>(MoveAlongPathRole.class)),

	VISION_BLACKOUT_ROLE(new InstanceableClass<>(VisionBlackoutRole.class,
			new InstanceableParameter(VisionBlackoutRole.EChallengeType.class, "Challenge Type", "STATIC_BALL"))),

	DRIBBLE_CHALLENGE_SIMPLE_ROLE(new InstanceableClass<>(DribbleChallengeSimpleRole.class)),
	DRIBBLE_CHALLENGE_ADVANCED_ROLE(new InstanceableClass<>(DribbleChallengeAdvancedRole.class)),

	CONTESTED_POSSESSION_CHALLENGE_ROLE(new InstanceableClass<>(ContestedPossessionChallengeRole.class)),
	;

	private final InstanceableClass<?> instanceableClass;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
