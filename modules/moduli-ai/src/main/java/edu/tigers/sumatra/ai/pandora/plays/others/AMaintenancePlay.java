/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.others;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This play moves all bots to the maintenance position.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public abstract class AMaintenancePlay extends APlay
{
	
	private long tDestReached = 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected AMaintenancePlay(EPlay play)
	{
		super(play);
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
		role.getMoveCon().setPenaltyAreaAllowedOur(true);
		role.getMoveCon().setGoalPostObstacle(true);
		return role;
	}
	
	
	protected void calculateBotActions(final AthenaAiFrame frame, IVector2 startingPos, IVector2 direction,
			double orientation)
	{
		IVector2 dest = startingPos.subtractNew(direction);
		
		IBotIDMap<ITrackedBot> otherBots = new BotIDMap<>();
		otherBots.putAll(frame.getWorldFrame().getFoeBots());
		otherBots.putAll(frame.getWorldFrame().getTigerBotsVisible());
		
		List<ARole> roles = new ArrayList<>(getRoles());
		roles.sort(Comparator.comparing(ARole::getBotID));
		
		ICircle shape;
		boolean destsReached = true;
		for (ARole aRole : roles)
		{
			MoveRole moveRole = (MoveRole) aRole;
			do
			{
				dest = dest.addNew(direction);
				shape = Circle.createCircle(dest, Geometry.getBotRadius() * 2);
				
			} while (!AiMath.isShapeFreeOfBots(shape, AiMath.getNonMovingBots(otherBots, 0.2), aRole.getBot()));
			
			moveRole.getMoveCon().updateDestination(dest);
			moveRole.getMoveCon().updateTargetAngle(orientation * Math.PI / 180);
			
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
