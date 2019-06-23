/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays;

import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Base for penalty plays
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class APenaltyPlay extends APlay
{
	@Configurable(comment = "Distance to a-axis on line")
	private static double distanceToX = 1000;
	
	
	/**
	 * 
	 */
	protected APenaltyPlay(final EPlay ePlay)
	{
		super(ePlay);
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
	}
	
	
	protected void updateMoveRoles(final AthenaAiFrame frame, final List<ARole> moveRoles, final int xSign,
			final int xOffset)
	{
		int offsetStep = (200);
		double xLine = xOffset + (xSign
				* ((Geometry.getFieldLength() / 2.0) - Geometry.getDistanceToPenaltyMark()
						- Geometry.getDistancePenaltyMarkToPenaltyLine() - 100));
		int sign = 1;
		double yStart = distanceToX;
		double offset = 0;
		for (ARole role : moveRoles)
		{
			MoveRole moveRole = (MoveRole) role;
			IVector2 destination;
			boolean blocked = false;
			do
			{
				blocked = false;
				destination = new Vector2(xLine, sign * (yStart - offset));
				offset -= offsetStep;
				if ((yStart - offset) < 0)
				{
					xLine -= offsetStep;
					offset = 0;
				}
				for (ITrackedBot bot : frame.getWorldFrame().getBots().values())
				{
					if (!bot.getBotId().equals(role.getBotID()))
					{
						if (GeoMath
								.distancePP(destination, bot.getPos()) < ((Geometry.getBotRadius() * 2) + 50))
						{
							blocked = true;
							break;
						}
					}
				}
			} while (blocked);
			moveRole.getMoveCon().updateDestination(destination);
			
			sign *= -1;
		}
	}
}
