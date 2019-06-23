/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import java.lang.reflect.Constructor;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderK2DRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderKNDWDPRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloV2Role;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveBallToRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveWithDistanceToPointRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.TurnAroundBallRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallBreakerRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallConquerRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ChipKickRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassReceiverStraightRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV2Role;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV3Role;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard.penalty.KeeperPenaltyThemRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.SkillTestRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.TurnAroundTestRole;


/**
 * Enumeration that represents the different {@link ARole} When added a new role do not forget to adjust role factory in
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis}.
 * 
 * @author Gero, ChristianK
 * 
 */
public enum ERole
{
	// movement
	/** Moves with a distance to the ball */
	MOVE_DISTANCE_BALL(MoveWithDistanceToPointRole.class),
	/** */
	MOVE(MoveRole.class),
	/**  */
	MOVE_BALL_TO(MoveBallToRole.class),
	/** */
	TURN_AROUND_BALL(TurnAroundBallRole.class),
	
	// Defense
	/** */
	DEFENDER_K2D(DefenderK2DRole.class),
	/** */
	DEFENDER_KNDWDP(DefenderKNDWDPRole.class),
	/** */
	KEEPER_SOLO(KeeperSoloRole.class),
	/** */
	KEEPER_SOLO_V2(KeeperSoloV2Role.class),
	/** */
	MAN_TO_MAN_MARKER(ManToManMarkerRole.class),
	/** */
	PASSIVE_DEFENDER(PassiveDefenderRole.class),
	
	// Offense
	/** */
	BALL_CONQUERER(BallConquerRole.class),
	/** */
	BALL_GETTER(BallGetterRole.class),
	/** */
	CHIP_KICK(ChipKickRole.class),
	/** */
	PASS_RECEIVER_STRAIGHT(PassReceiverStraightRole.class),
	/** */
	PASS_SENDER(PassSenderRole.class),
	/**  */
	REDIRECTER(RedirectRole.class),
	/** */
	SHOOTERV2(ShooterV2Role.class),
	/**  */
	BALL_BREAKER(BallBreakerRole.class),
	
	// Standards
	/** */
	PENALTY_THEM_KEEPER(KeeperPenaltyThemRole.class),
	/**  */
	SKILL_TEST(SkillTestRole.class),
	/**  */
	TURN_ROUND_TEST(TurnAroundTestRole.class),
	/**  */
	SHOOTERV3(ShooterV3Role.class), ;
	
	private final Class<?>	impl;
	
	
	/**
	 */
	private ERole(Class<?> impl)
	{
		this.impl = impl;
	}
	
	
	/**
	 * Returns the first public constructor of the Play.
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public Constructor<?> getConstructor() throws NoSuchMethodException
	{
		return impl.getConstructor();
	}
}
