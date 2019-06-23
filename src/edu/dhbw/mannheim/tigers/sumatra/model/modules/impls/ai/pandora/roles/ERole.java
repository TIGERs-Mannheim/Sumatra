/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;


/**
 * Enumeration that represents the different {@link ARole} When added a new role do not forget to adjust role factory in
 * {@link Lachesis}.
 * 
 * @author Gero, ChristianK
 * 
 */
public enum ERole
{
	// Defense
	KEEPER_SOLO,
	KEEPER_K2D,
	KEEPER_K1D,
	DEFENDER_K2D,
	DEFENDER_K1D,
	MAN_TO_MAN_MARKER,
	
	PASSIVE_DEFENDER_THEM,
	
	DEFENSE_BLOCKER,
	
	// Offense
	SHOOTER,
	KICKER_SET_PIECE,
	ATTACKER_SET_PIECE,
	PASS_SENDER,
	PASS_RECEIVER,
	INDIRECT_SHOOTER,
	PULL_BACK_ROLE,
	
	// Standards
	FREEKICKER(true),
	FREEKICKER_V2,
	FREEBLOCKER,
	PENALTY_US_SHOOTER,
	KEEPER_PENALTY_THEM,
	
	// leftovers
	BALL_GETTER,
	ARMED_BALL_GETTER,
	FREEKICKERV25(true),
	BALL_DRIBBLER,
	TESTER(true),
	MOVEROLE,
	PP_ROLE(true),
	GENERIC_MOVE,
	AIM_TEST(true),
	TEST_LOOK_AT(true),
	POSITIONINGROLE(true),
	AIMING,
	PASS_BLOCKER,
	NULL,
	CHIP_SENDER,
	
	// skill tester
	KICK_TESTER(true),
	SINE_TESTER(true),
	TRIANGLE_TESTER(true),
	SKILL_TESTER(true),
	DIRECTMOVE_TESTER(true),
	BALL_MOVE_TESTER(true),
	MOVE_ON_PATH_TESTER(true),
	WP_TESTER_INIT(true),
	WP_TESTER(true),
	GETBALLANDSHOOT(true),
	RECEIVER;
	

	private final boolean	isTestRole;
	
	
	/**
	 * Calls {@link #ERole(boolean)}
	 */
	private ERole()
	{
		this(false);
	}
	

	/**
	 * @param testRole
	 */
	private ERole(boolean isTestRole)
	{
		this.isTestRole = isTestRole;
	}
	

	/**
	 * @return the isTestRole
	 */
	public boolean isTestRole()
	{
		return isTestRole;
	}
	

	/**
	 * @return All test-roles
	 */
	public static ERole[] testRoles()
	{
		List<ERole> testRoles = new ArrayList<ERole>();
		for (ERole r : ERole.values())
		{
			if (r.isTestRole())
			{
				testRoles.add(r);
			}
		}
		
		return testRoles.toArray(new ERole[testRoles.size()]);
	}
}
