/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.01.2015
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This play moves up to 5 bots to the specified formation. Another bot can then be used to drive to 3 positions. <br>
 * This play is intended for testing. The three positions can be switched by clicking in Referee menu: Normal Start,
 * Stop and halt.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class FormationStaticPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable(comment = "Formation of Bots: LINE, U, CIRCLE")
	private static EFormations	formation	= EFormations.LINE;
	
	private static float			orientation	= (float) (Math.PI / 2);
	
	private MoveRole				lastRole		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public FormationStaticPlay()
	{
		super(EPlay.FORMATION_STATIC);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
		role.getMoveCon().updateTargetAngle(orientation);
		role.getMoveCon().setPenaltyAreaAllowedOur(true);
		
		
		return role;
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
		if (lastRole != null)
		{
			switch (gameState)
			{
				case RUNNING:
					lastRole.getMoveCon().updateDestination(formation.getMovingBotDestinations()[1]);
					break;
				case STOPPED:
					lastRole.getMoveCon().updateDestination(formation.getMovingBotDestinations()[0]);
					break;
				case HALTED:
				default:
					lastRole.getMoveCon().updateDestination(formation.getMovingBotDestinations()[2]);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		List<ARole> roles = new ArrayList<ARole>(getRoles());
		Collections.sort(roles, Comparator.comparing(e -> e.getBotID()));
		
		if ((roles.size() - 1) >= 0)
		{
			lastRole = (MoveRole) (roles.remove(roles.size() - 1));
		}
		IVector2[] dest = formation.getStaticBotPositions();
		
		int i = 0;
		for (ARole aRole : roles)
		{
			MoveRole moveRole = (MoveRole) aRole;
			moveRole.getMoveCon().updateDestination(dest[i]);
			i++;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EFormations
	{
		LINE(new IVector2[] { new Vector2(0, 0), new Vector2(-300, 0),
				new Vector2(300, 0), new Vector2(-600, 0), new Vector2(600, 0) },
				new IVector2[] { new Vector2(0, -500), new Vector2(0, 2000),
						new Vector2(0, 250) }),
		U(new IVector2[] { new Vector2(0, 0), new Vector2(-300, 250),
				new Vector2(300, 250), new Vector2(-300, 500), new Vector2(300, 500) },
				new IVector2[] { new Vector2(0, -500), new Vector2(0, 2000),
						new Vector2(0, 200) }),
		CIRCLE(new IVector2[] { new Vector2(-1000, 0), new Vector2(1000, 0),
				new Vector2(0, -1000), new Vector2(0, 1000), new Vector2(-2000, -2000) },
				new IVector2[] { new Vector2(-1500, 0), new Vector2(1500, 0), new Vector2(0, 0) });
		
		private IVector2[]	staticBotPositions;
		private IVector2[]	movingBotDestinations;
		
		
		private EFormations(final IVector2[] staticBotPositions, final IVector2[] movingBotDestinations)
		{
			this.staticBotPositions = staticBotPositions;
			this.movingBotDestinations = movingBotDestinations;
		}
		
		
		/**
		 * @return the staticBotPositions
		 */
		public IVector2[] getStaticBotPositions()
		{
			return staticBotPositions;
		}
		
		
		/**
		 * @return the movingBotDestinations
		 */
		public IVector2[] getMovingBotDestinations()
		{
			return movingBotDestinations;
		}
	}
}
