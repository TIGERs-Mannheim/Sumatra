/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
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
import edu.tigers.sumatra.ai.pandora.roles.offense.EpicPenaltyShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KickoffShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OneOnOneShooter;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ai.pandora.roles.test.ChipInterceptRole;
import edu.tigers.sumatra.ai.pandora.roles.test.DestChangedTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.InterceptionRole;
import edu.tigers.sumatra.ai.pandora.roles.test.KickChillTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.KickSamplerRole;
import edu.tigers.sumatra.ai.pandora.roles.test.MoveStressTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.MoveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.ReceiveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.SimpleShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.test.SubmitBallTestRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.PrimaryPlacementRole;
import edu.tigers.sumatra.ai.pandora.roles.throwin.SecondaryPlacementRole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Enumeration that represents the different {@link ARole} When added a new role do not forget to adjust role factory in
 * {@link edu.tigers.sumatra.ai.lachesis.Lachesis}.
 *
 * @author Gero, ChristianK
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum ERole implements IInstanceableEnum
{
	// main
	/**  */
	OFFENSIVE(new InstanceableClass(OffensiveRole.class,
			new InstanceableParameter(EOffensiveStrategy.class, "StateID", EOffensiveStrategy.KICK.name()),
			new InstanceableParameter(Boolean.TYPE, "allowStateSwitch", "true"))),
	/**  */
	KICKOFF_SHOOTER(new InstanceableClass(KickoffShooterRole.class)),
	/**  */
	SUPPORT(new InstanceableClass(SupportRole.class)),
	/** */
	INTERCEPTION(new InstanceableClass(InterceptionRole.class)),
	/** */
	KEEPER(new InstanceableClass(KeeperRole.class)),
	/**  */
	EPIC_PENALTY_SHOOTER(new InstanceableClass(EpicPenaltyShooterRole.class)),
	/** */
	MAN_TO_MAN_MARKER(new InstanceableClass(ManToManMarkerRole.class,
			new InstanceableParameter(DynamicPosition.class, "foeBot", "0 B"))),
	/**  */
	CENTER_BACK(new InstanceableClass(CenterBackRole.class,
			new InstanceableParameter(DynamicPosition.class, "Threat", "0 B"),
			new InstanceableParameter(CenterBackRole.CoverMode.class, "coverMode", "CENTER"))),
	/** */
	DEFENDER_PLACEHOLDER(new InstanceableClass(DefenderPlaceholderRole.class)),
	/** */
	INTERCEPT_TEST(new InstanceableClass(InterceptTestRole.class,
			new InstanceableParameter(DynamicPosition.class, "toIntercept", "-4500, 0"),
			new InstanceableParameter(DynamicPosition.class, "toProtect", "0 B"))),
	
	/** */
	DEFENDER_PEN_AREA(new InstanceableClass(DefenderPenAreaRole.class,
			new InstanceableParameter(Boolean.class, "ballAsReference", "true"))),
	
	// movement
	/** */
	MOVE(new InstanceableClass(MoveRole.class)),
	
	
	// Standards
	/** */
	PENALTY_KEEPER(new InstanceableClass(PenaltyKeeperRole.class)),
	
	/**  */
	PENALTY_ATTACKER(new InstanceableClass(OneOnOneShooter.class)),
	
	/***/
	ONE_ON_ONE_KEEPER(new InstanceableClass(KeeperOneOnOneRole.class)),
	
	/** */
	PRIMARY_AUTOMATED_THROW_IN(new InstanceableClass(PrimaryPlacementRole.class)),
	/** */
	SECONDARY_AUTOMATED_THROW_IN(new InstanceableClass(SecondaryPlacementRole.class)),
	
	/**  */
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
			new InstanceableParameter(String.class, "logFileName", ""))),
	/** */
	MOVE_TO_TEST(new InstanceableClass(MoveRole.class,
			new InstanceableParameter(IVector2.class, "destination", "1000,0"),
			new InstanceableParameter(Double.TYPE, "orientation", "0.0"))),
	/**  */
	MOVE_STRESS_TEST(new InstanceableClass(MoveStressTestRole.class)),
	/**  */
	DEST_CHANGED(new InstanceableClass(DestChangedTestRole.class, new InstanceableParameter(IVector2.class, "diffDest",
			"0,1000"), new InstanceableParameter(IVector2.class, "diffAngle", "1000,0"), new InstanceableParameter(
					Integer.TYPE, "freq", "30"))),
	/**  */
	SIMPLE_SHOOTER(new InstanceableClass(SimpleShooterRole.class,
			new InstanceableParameter(DynamicPosition.class, "passTarget", "4500,0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"))),
	
	/** */
	KICK_SAMPLER(new InstanceableClass(KickSamplerRole.class,
			new InstanceableParameter(Boolean.TYPE, "onlyOurHalf", "false"),
			new InstanceableParameter(Boolean.TYPE, "chipFromSide", "false"),
			new InstanceableParameter(Double.TYPE, "minDurationMs", "1.0"),
			new InstanceableParameter(Double.TYPE, "maxDurationMs", "12.0"),
			new InstanceableParameter(Integer.TYPE, "numSamples", "12"),
			new InstanceableParameter(Boolean.TYPE, "continue", "true"))),
	
	/**  */
	REDIRECT(
			new InstanceableClass(RedirectTestRole.class,
					new InstanceableParameter(DynamicPosition.class, "target", "0,0"))),
	/**  */
	RECEIVE_TEST(new InstanceableClass(ReceiveTestRole.class,
			new InstanceableParameter(Double.TYPE, "passEndVel", "1.0"),
			new InstanceableParameter(Double.TYPE, "passDist", "1000"))),
	
	/**  */
	KICK_CHILL_TEST(new InstanceableClass(KickChillTestRole.class,
			new InstanceableParameter(Double.TYPE, "rotation [deg]", "90"))),
	
	/** */
	SUBMIT_BALL_TEST(new InstanceableClass(SubmitBallTestRole.class,
			new InstanceableParameter(Double.TYPE, "submitDistance", "1000"))),
	
	/**  */
	MOVE_ALONG_PATH(new InstanceableClass(MoveAlongPathRole.class)),
	
	CHIP_INTERCEPT(new InstanceableClass(ChipInterceptRole.class)),
	
	/** */
	@Deprecated
	MOVE_BALL_TO(new InstanceableClass(Object.class)),
	
	/** */
	@Deprecated
	REDIRECT_TEST(new InstanceableClass(Object.class));
	
	private final InstanceableClass clazz;
	
	
	/**
	 */
	ERole(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
