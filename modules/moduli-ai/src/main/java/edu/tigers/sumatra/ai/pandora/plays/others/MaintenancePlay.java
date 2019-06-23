/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.01.2011
 * Author(s): osteinbrecher
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
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This play moves all bots to the maintenance position.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class MaintenancePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable(comment = "first maintenance position")
	private static IVector2	startingPos		= new Vector2(-3100, -1800);
	
	@Configurable(comment = "Direction from startingPos with length")
	private static IVector2	direction		= new Vector2(0, 200);
	
	@Configurable(comment = "Orientation of bots")
	private static double	orientation		= 0;
	
	
	private long				tDestReached	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public MaintenancePlay()
	{
		super(EPlay.MAINTENANCE);
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
		role.getMoveCon().setGoalPostObstacle(true);
		return role;
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		IVector2 dest = startingPos.subtractNew(direction);
		
		IBotIDMap<ITrackedBot> otherBots = new BotIDMap<>();
		otherBots.putAll(frame.getWorldFrame().getFoeBots());
		otherBots.putAll(frame.getWorldFrame().getTigerBotsVisible());
		
		List<ARole> roles = new ArrayList<ARole>(getRoles());
		Collections.sort(roles, Comparator.comparing(e -> e.getBotID()));
		
		Circle shape;
		boolean destsReached = true;
		for (ARole aRole : roles)
		{
			MoveRole moveRole = (MoveRole) aRole;
			do
			{
				dest = dest.addNew(direction);
				shape = new Circle(dest, Geometry.getBotRadius() * 2);
				
			} while (!AiMath.isShapeFreeOfBots(shape, AiMath.getNonMovingBots(otherBots, 0.2), aRole.getBot()));
			moveRole.getMoveCon().updateDestination(dest);
			
			destsReached = destsReached && moveRole.isDestinationReached();
		}
		if (destsReached)
		{
			if (tDestReached == 0)
			{
				tDestReached = frame.getWorldFrame().getTimestamp();
			} else if ((frame.getWorldFrame().getTimestamp() - tDestReached) > 1e9)
			{
				for (ARole aRole : roles)
				{
					aRole.setCompleted();
				}
			}
		} else
		{
			tDestReached = 0;
		}
	}
}
