/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * Base for penalty plays
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class APenaltyPlay extends APlay
{
	@Configurable(comment = "Distance to a-axis on line", defValue = "1000.0")
	private static double distanceToX = 1000;
	
	
	/**
	 * 
	 */
	protected APenaltyPlay(final EPlay ePlay)
	{
		super(ePlay);
	}
	
	
	@Override
	protected void onGameStateChanged(final GameState gameState)
	{
	}
	
	
	protected void updateMoveRoles(final AthenaAiFrame frame, final int xSign,
			final int xOffset)
	{
		int offsetStep = 200;
		double xLine = xOffset + (xSign * (Geometry.getPenaltyMarkTheir().x()
				- RuleConstraints.getDistancePenaltyMarkToPenaltyLine() - 100));
		int sign = 1;
		double offset = 0;
		
		
		List<IVector2> destinations = new ArrayList<>();
		for (int i = 0; i < getRoles().size(); i++)
		{
			IVector2 destination = Vector2.fromXY(xLine, sign * (distanceToX - offset));
			offset -= offsetStep;
			destinations.add(destination);
			
			sign *= -1;
		}
		
		int i = 0;
		for (ARole role : getRoles())
		{
			if (!role.getClass().equals(MoveRole.class))
			{
				continue;
			}
			MoveRole moveRole = (MoveRole) role;
			IVector2 destination = destinations.get(i);
			for (int j = 0; j < 10; j++)
			{
				if (AiMath.isShapeFreeOfBots(Circle.createCircle(destination, (Geometry.getBotRadius() * 2) + 50),
						frame.getWorldFrame().getFoeBots(), role.getBot()))
				{
					break;
				}
				destination = destination.addNew(Vector2.fromX(xSign * -100.0));
				
			}
			moveRole.getMoveCon().updateDestination(destination);
			i++;
		}
	}
}
