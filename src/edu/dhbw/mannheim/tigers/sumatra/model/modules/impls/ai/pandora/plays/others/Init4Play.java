/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.06.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;


/**
 * Simple play, that handles 4 {@link PassiveDefenderRole}, which don't do anything.
 * 
 * @author Malte
 * 
 */
public class Init4Play extends APlay
{
	
	/**  */
	private static final long		serialVersionUID	= 990208595141445207L;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private PassiveDefenderRole	role1;
	private PassiveDefenderRole	role2;
	private PassiveDefenderRole	role3;
	private PassiveDefenderRole	role4;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Init4Play(AIInfoFrame aiFrame)
	{
		super(EPlay.INIT4, aiFrame);
		float l = AIConfig.getGeometry().getFieldLength();
		float w = AIConfig.getGeometry().getFieldWidth();
		// Intitial positions of the bots
		Vector2 d1 = new Vector2(-l / 2 + l / 24, w / 14);
		Vector2 d2 = new Vector2(-l / 2 + l / 7, -w / 20);
		Vector2 d3 = new Vector2(-l / 6, 0);
		Vector2 d4 = new Vector2(-l / 20, -w / 5);
		Vector2 t = new Vector2(AIConfig.getGeometry().getCenter());
		
		role1 = new PassiveDefenderRole(d1, t);
		role1.setKeeper(true); // First role is a keeper
		
		role2 = new PassiveDefenderRole(d2, t);
		role3 = new PassiveDefenderRole(d3, t);
		role4 = new PassiveDefenderRole(d4, t);
		
		addDefensiveRole(role1, d1);
		addDefensiveRole(role2, d2);
		addDefensiveRole(role3, d3);
		addDefensiveRole(role4, d4);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
