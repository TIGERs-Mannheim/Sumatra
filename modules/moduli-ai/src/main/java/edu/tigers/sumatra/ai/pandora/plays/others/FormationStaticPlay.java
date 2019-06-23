/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.others;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;


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
	
	@Configurable(comment = "Formation of Bots: LINE, U, CIRCLE", defValue = "LINE")
	private static EFormations	formation	= EFormations.LINE;
	
	private static double		orientation	= Math.PI / 2.0;
	
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
		MoveRole role = new MoveRole();
		role.getMoveCon().updateTargetAngle(orientation);
		role.getMoveCon().setPenaltyAreaAllowedOur(true);
		
		
		return role;
	}
	
	
	@Override
	protected void onGameStateChanged(final GameState gameState)
	{
		if (lastRole != null)
		{
			switch (gameState.getState())
			{
				case RUNNING:
					lastRole.getMoveCon().updateDestination(formation.getMovingBotDestinations()[1]);
					break;
				case STOP:
					lastRole.getMoveCon().updateDestination(formation.getMovingBotDestinations()[0]);
					break;
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
		List<ARole> roles = new ArrayList<>(getRoles());
		Collections.sort(roles, Comparator.comparing(ARole::getBotID));
		
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
		LINE(new IVector2[] { Vector2.fromXY(0, 0), Vector2.fromXY(-300, 0),
				Vector2.fromXY(300, 0), Vector2.fromXY(-600, 0), Vector2.fromXY(600, 0) },
				new IVector2[] { Vector2.fromXY(0, -500), Vector2.fromXY(0, 2000),
						Vector2.fromXY(0, 250) }),
		U(new IVector2[] { Vector2.fromXY(0, 0), Vector2.fromXY(-300, 250),
				Vector2.fromXY(300, 250), Vector2.fromXY(-300, 500), Vector2.fromXY(300, 500) },
				new IVector2[] { Vector2.fromXY(0, -500), Vector2.fromXY(0, 2000),
						Vector2.fromXY(0, 200) }),
		CIRCLE(new IVector2[] { Vector2.fromXY(-1000, 0), Vector2.fromXY(1000, 0),
				Vector2.fromXY(0, -1000), Vector2.fromXY(0, 1000), Vector2.fromXY(-2000, -2000) },
				new IVector2[] { Vector2.fromXY(-1500, 0), Vector2.fromXY(1500, 0), Vector2.fromXY(0, 0) });
		
		private IVector2[]	staticBotPositions;
		private IVector2[]	movingBotDestinations;
		
		
		EFormations(final IVector2[] staticBotPositions, final IVector2[] movingBotDestinations)
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
