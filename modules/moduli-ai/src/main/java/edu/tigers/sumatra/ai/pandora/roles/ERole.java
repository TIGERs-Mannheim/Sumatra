/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.ai.metis.offense.finisher.EFinisherMove;
import edu.tigers.sumatra.ai.pandora.roles.defense.CenterBackRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPenAreaRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderPlaceholderRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.InterceptTestRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.ManToManMarkerRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.PenaltyKeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperOneOnOneRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.path.MoveAlongPathRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DelayedAttackRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.EpicPenaltyShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.FreeSkirmishRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KeepDistToBallRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KickoffShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OneOnOneShooter;
import edu.tigers.sumatra.ai.pandora.roles.offense.OpponentInterceptionRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.PassReceiverRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.SupportiveAttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ai.pandora.roles.test.ChipInterceptRole;
import edu.tigers.sumatra.ai.pandora.roles.test.CrookedKickSamplerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.FinisherMoveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.IdentifyBotModelRole;
import edu.tigers.sumatra.ai.pandora.roles.test.KickTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.MoveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.StraightChipKickSamplerRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryBallPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.SecondaryBallPlacementRole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Enumeration that represents the different {@link ARole}s
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum ERole implements IInstanceableEnum
{
	// offensive roles
	
	ATTACKER(new InstanceableClass(AttackerRole.class)),
	SUPPORTIVE_ATTACKER(new InstanceableClass(SupportiveAttackerRole.class)),
	KEEP_DIST_TO_BALL(new InstanceableClass(KeepDistToBallRole.class)),
	OPPONENT_INTERCEPTION(new InstanceableClass(OpponentInterceptionRole.class)),
	FREE_SKIRMISH(new InstanceableClass(FreeSkirmishRole.class)),
	DELAYED_ATTACK(new InstanceableClass(DelayedAttackRole.class)),
	PASS_RECEIVER(new InstanceableClass(PassReceiverRole.class)),
	
	
	KICKOFF_SHOOTER(new InstanceableClass(KickoffShooterRole.class)),
	
	SUPPORT(new InstanceableClass(SupportRole.class)),
	
	// defense
	
	KEEPER(new InstanceableClass(KeeperRole.class)),
	
	MAN_TO_MAN_MARKER(new InstanceableClass(ManToManMarkerRole.class,
			new InstanceableParameter(DynamicPosition.class, "foeBot", "0 B"))),
	
	CENTER_BACK(new InstanceableClass(CenterBackRole.class,
			new InstanceableParameter(DynamicPosition.class, "Threat", "0 B"),
			new InstanceableParameter(CenterBackRole.CoverMode.class, "coverMode", "CENTER"))),
	
	DEFENDER_PLACEHOLDER(new InstanceableClass(DefenderPlaceholderRole.class)),
	
	DEFENDER_PEN_AREA(new InstanceableClass(DefenderPenAreaRole.class,
			new InstanceableParameter(Boolean.class, "ballAsReference", "true"))),
	
	// Standards
	PENALTY_KEEPER(new InstanceableClass(PenaltyKeeperRole.class)),
	PENALTY_ATTACKER(new InstanceableClass(OneOnOneShooter.class)),
	ONE_ON_ONE_KEEPER(new InstanceableClass(KeeperOneOnOneRole.class)),
	EPIC_PENALTY_SHOOTER(new InstanceableClass(EpicPenaltyShooterRole.class)),
	
	
	SECONDARY_BALL_PLACEMENT(new InstanceableClass(SecondaryBallPlacementRole.class,
			new InstanceableParameter(IVector2.class, "placementPos", "0,0"))),
	PRIMARY_BALL_PLACEMENT(new InstanceableClass(PrimaryBallPlacementRole.class,
			new InstanceableParameter(IVector2.class, "placementPos", "0,0"))),
	
	MOVE_TEST(new InstanceableClass(MoveTestRole.class,
			new InstanceableParameter(MoveTestRole.EMoveMode.class, "mode", "TRAJ_VEL"),
			new InstanceableParameter(IVector2.class, "initPos", "1500,1000"),
			new InstanceableParameter(Double.TYPE, "orientation", "0"),
			new InstanceableParameter(Double.TYPE, "scale", "1000"),
			new InstanceableParameter(Double.TYPE, "startAngleDeg", "0"),
			new InstanceableParameter(Double.TYPE, "stopAngleDeg", "180"),
			new InstanceableParameter(Double.TYPE, "stepAngleDeg", "10"),
			new InstanceableParameter(Double.TYPE, "angleTurn", "0"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"),
			new InstanceableParameter(String.class, "logFileName", ""),
			new InstanceableParameter(Boolean.TYPE, "rollOut", "false"))),
	
	IDENTIFY_BOT_MODEL(new InstanceableClass(IdentifyBotModelRole.class,
			new InstanceableParameter(IVector2.class, "startPos", "0,0"),
			new InstanceableParameter(IVector2.class, "endPos", "1000,0"),
			new InstanceableParameter(Double.TYPE, "accMaxXY", "2"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "12"),
			new InstanceableParameter(Double[].class, "velXY", "0.5; 1.0; 1.25; 1.5"),
			new InstanceableParameter(Double[].class, "velW", "6; 12; 18; 24; 30"),
			new InstanceableParameter(Integer.TYPE, "iterations", "1"))),
	
	KICK_TEST(new InstanceableClass(KickTestRole.class,
			new InstanceableParameter(DynamicPosition.class, "passTarget", "4500,0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "6.5"))),
	
	REDIRECT_TEST(new InstanceableClass(RedirectTestRole.class,
			new InstanceableParameter(DynamicPosition.class, "target", "0,0"))),
	
	INTERCEPT_TEST(new InstanceableClass(InterceptTestRole.class,
			new InstanceableParameter(DynamicPosition.class, "toIntercept", "-4500, 0"),
			new InstanceableParameter(DynamicPosition.class, "toProtect", "0 B"))),
	
	STRAIGHT_CHIP_KICK_SAMPLER(new InstanceableClass(StraightChipKickSamplerRole.class,
			new InstanceableParameter(Boolean.TYPE, "onlyOurHalf", "false"),
			new InstanceableParameter(Boolean.TYPE, "chipFromSide", "false"),
			new InstanceableParameter(Double.TYPE, "minDurationMs", "1.0"),
			new InstanceableParameter(Double.TYPE, "maxDurationMs", "12.0"),
			new InstanceableParameter(Integer.TYPE, "numSamples", "12"),
			new InstanceableParameter(Boolean.TYPE, "continue", "true"),
			new InstanceableParameter(Boolean.TYPE, "doChip", "true"),
			new InstanceableParameter(Boolean.TYPE, "doStraight", "true"))),
	
	CROOKED_KICK_SAMPLER(new InstanceableClass(CrookedKickSamplerRole.class,
			new InstanceableParameter(IVector2.class, "kickPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "orientation", "0.0"),
			new InstanceableParameter(Double.TYPE, "kickDurationMs", "4.0"),
			new InstanceableParameter(Double.TYPE, "offsetCenter", "20.0"),
			new InstanceableParameter(Integer.TYPE, "numSamples", "5"),
			new InstanceableParameter(Boolean.TYPE, "continue", "true"))),
	
	MOVE(new InstanceableClass(MoveRole.class,
			new InstanceableParameter(IVector2.class, "destination", "1000,0"),
			new InstanceableParameter(Double.TYPE, "orientation", "0.0"))),
	
	MOVE_ALONG_PATH(new InstanceableClass(MoveAlongPathRole.class)),
	
	CHIP_INTERCEPT(new InstanceableClass(ChipInterceptRole.class)),
	
	FINISHER_MOVE_TEST(new InstanceableClass(FinisherMoveTestRole.class,
			new InstanceableParameter(EFinisherMove.class, "move", "EXAMPLE"))),
	
	;
	
	private final InstanceableClass clazz;
	
	
	ERole(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
