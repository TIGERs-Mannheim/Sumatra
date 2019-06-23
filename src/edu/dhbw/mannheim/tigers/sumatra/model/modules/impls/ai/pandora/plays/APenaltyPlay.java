/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Base for penalty plays
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class APenaltyPlay extends APlay
{
	@Configurable(comment = "Distance to a-axis on line")
	private static float		distanceToX	= 1000;
	@Configurable(comment = "Direction from startingPos with length")
	private static IVector2	direction	= new Vector2(0, 200);
	
	
	/**
	 * 
	 */
	protected APenaltyPlay(final EPlay ePlay)
	{
		super(ePlay);
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
	}
	
	
	protected void updateMoveRoles(final AthenaAiFrame frame, final List<ARole> moveRoles, final int xSign,
			final int xOffset)
	{
		int offsetStep = (200);
		float xLine = xOffset + (xSign
				* ((AIConfig.getGeometry().getFieldLength() / 2) - AIConfig.getGeometry().getDistanceToPenaltyMark()
				- AIConfig.getGeometry().getDistancePenaltyMarkToPenaltyLine() - 100));
		int sign = 1;
		float yStart = distanceToX;
		float offset = 0;
		for (ARole role : moveRoles)
		{
			MoveRole moveRole = (MoveRole) role;
			IVector2 destination;
			do
			{
				destination = new Vector2(xLine, sign * (yStart - offset));
				offset -= offsetStep;
				if ((yStart - offset) < 0)
				{
					xLine -= offsetStep;
					offset = 0;
				}
			} while (moveRole.getMoveCon().checkCondition(frame.getWorldFrame(), moveRole.getBotID()) == EConditionState.BLOCKED);
			moveRole.getMoveCon().updateDestination(destination);
			
			sign *= -1;
		}
	}
}
