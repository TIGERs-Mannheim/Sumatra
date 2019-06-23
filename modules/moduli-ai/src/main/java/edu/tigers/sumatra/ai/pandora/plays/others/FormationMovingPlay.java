/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.01.2015
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * This play moves up to 5 bots to the specified moving formation. Another bot can then be used to drive to 3 positions. <br>
 * This play is intended for testing. The three positions can be switched by clicking in Referee menu: Normal Start,
 * Stop and halt.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class FormationMovingPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable(comment = "Formation of Bots: LINE, CIRCLE")
	private static EFormations	formation	= EFormations.LINE;
	
	private static double		orientation	= 0;
	
	private MoveRole				lastRole		= null;
	
	
	private boolean				init			= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public FormationMovingPlay()
	{
		super(EPlay.FORMATION_MOVING);
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
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
		if (lastRole != null)
		{
			switch (gameState)
			{
				case RUNNING:
					init = false;
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
		int i = 0;
		if (init)
		{
			for (ARole aRole : roles)
			{
				MoveRole moveRole = (MoveRole) aRole;
				moveRole.getMoveCon().updateDestination(formation.getStartBotPositions()[i]);
				i++;
			}
		} else
		{
			int[] switchDir = formation.getSwitchDir();
			for (ARole aRole : roles)
			{
				MoveRole moveRole = (MoveRole) aRole;
				IVector2 dest = new Vector2(0, switchDir[i] * 500);
				IVector2 newDest = formation.getStartBotPositions()[i].addNew(dest);
				if (aRole.getBot().getPos().equals(newDest, Geometry.getBotRadius() * 1.5))
				// if (moveRole.getMoveCon().checkCondition(frame.getWorldFrame(), aRole.getBotID()) ==
				// EConditionState.FULFILLED)
				{
					switchDir[i] = switchDir[i] * -1;
				}
				moveRole.getMoveCon().updateDestination(newDest);
				
				i++;
			}
			formation.setSwitchDir(switchDir);
		}
		
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private enum EFormations
	{
		LINE(new IVector2[] { new Vector2(-1000, 0), new Vector2(-200, 0),
				new Vector2(600, 0), new Vector2(1400, 0), new Vector2(2200, 0) },
				new IVector2[] { new Vector2(-1500, 0), new Vector2(2600, 0), new Vector2(2000, -2000) },
				new int[] { 1, -1, 1, -1, 1 }),
		CIRCLE(new IVector2[] { new Vector2(-1000, 0), new Vector2(1000, 0),
				new Vector2(0, -1000), new Vector2(0, 1000), new Vector2(-2000, -2000) },
				new IVector2[] { new Vector2(-1500, 0), new Vector2(1500, 0), new Vector2(0, 0) },
				new int[] { 1, -1, 1, -1, 1 });
		
		private IVector2[]	startBotPositions;
		private IVector2[]	movingBotDestinations;
		private int[]			switchDir;
		
		
		private EFormations(final IVector2[] startBotPositions, final IVector2[] movingBotDestinations,
				final int[] switchDir)
		{
			this.startBotPositions = startBotPositions;
			this.movingBotDestinations = movingBotDestinations;
			this.switchDir = switchDir;
		}
		
		
		/**
		 * @return the startBotPositions
		 */
		public IVector2[] getStartBotPositions()
		{
			return startBotPositions;
		}
		
		
		/**
		 * @return the movingBotDestinations
		 */
		public IVector2[] getMovingBotDestinations()
		{
			return movingBotDestinations;
		}
		
		
		/**
		 * @return the switchDir
		 */
		public int[] getSwitchDir()
		{
			return switchDir;
		}
		
		
		/**
		 * @param switchDir
		 */
		public void setSwitchDir(final int[] switchDir)
		{
			this.switchDir = switchDir;
		}
	}
	
}
