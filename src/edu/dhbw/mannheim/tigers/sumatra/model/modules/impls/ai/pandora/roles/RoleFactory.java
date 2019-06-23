/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.02.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderK1DRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperK1DRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperK2DRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.AttackerSetPiece;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PositioningRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PullBackRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.Shooter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.AimingTest;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.GetAndShootRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.PathPlanningRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.SkillTester;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.TestLookAtRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.rasterrole.TestRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester.BallMoveTester;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester.DirectMoveTester;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester.KickTester;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester.MoveOnPathTester;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester.SineTester;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester.WPTester;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester.WPTester_init;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standards.penalty.KeeperPenaltyThemRole;


/**
 * Simple factory-class for converting {@link ERole}-types to {@link ARole} instances
 * 
 * @see #createRole(ERole)
 * @author Gero
 */
public class RoleFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static RoleFactory	instance	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public static synchronized RoleFactory getInstance()
	{
		if (instance == null)
		{
			instance = new RoleFactory();
		}
		return instance;
	}
	
	
	/**
	 * @see RoleFactory
	 */
	private RoleFactory()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Factory method for {@link ARole}s.
	 * 
	 * @param type {@link ERole} of a role
	 * @return The generated {@link ARole}
	 * @throws IllegalArgumentException If the
	 *            given {@link ERole} cannot be handled!
	 */
	public ARole createRole(ERole type)
	{
		switch (type)
		{
			case DEFENDER_K1D:
				return new DefenderK1DRole();
			case KEEPER_K1D:
				return new KeeperK1DRole();
				
			case KEEPER_K2D:
				return new KeeperK2DRole();
				
			case BALL_GETTER:
				return new BallGetterRole(EGameSituation.GAME);
				
			case ATTACKER_SET_PIECE:
				return new AttackerSetPiece();
				
			case TESTER:
				return new TestRole();
				
			case SHOOTER:
				return new Shooter(EGameSituation.GAME);
				
			case MOVEROLE:
				return new MoveRole();
				
			case PP_ROLE:
				return new PathPlanningRole();
				
			case PASS_RECEIVER:
				return new PassReceiver();
				
			case PASS_SENDER:
				return new PassSender(EGameSituation.GAME);
				
			case KEEPER_SOLO:
				return new KeeperSoloRole();
				
			case AIM_TEST:
				return new AimingTest();
				
			case TEST_LOOK_AT:
				return new TestLookAtRole();
				
			case KEEPER_PENALTY_THEM:
				return new KeeperPenaltyThemRole();
				
			case PASSIVE_DEFENDER_THEM:
				return new PassiveDefenderRole();
				
			case POSITIONINGROLE:
				return new PositioningRole(false);
				
			case PULL_BACK_ROLE:
				return new PullBackRole();
				
			case KICKER_SET_PIECE:
				// return new KickerSetPiece();
				
				// skill tester
				
			case SINE_TESTER:
				return new SineTester();
				
			case KICK_TESTER:
				return new KickTester();
			case SKILL_TESTER:
				return new SkillTester();
				
			case DIRECTMOVE_TESTER:
				return new DirectMoveTester();
			case MOVE_ON_PATH_TESTER:
				return new MoveOnPathTester();
			case WP_TESTER:
				return new WPTester();
			case WP_TESTER_INIT:
				return new WPTester_init();
			case BALL_MOVE_TESTER:
				return new BallMoveTester();
			case GETBALLANDSHOOT:
				return new GetAndShootRole();
			default:
				throw new IllegalArgumentException("Role of type [" + type + "] can not be generated!");
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
