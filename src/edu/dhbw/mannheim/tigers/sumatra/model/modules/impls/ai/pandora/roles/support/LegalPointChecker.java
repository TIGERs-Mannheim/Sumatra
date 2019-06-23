/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.03.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ITacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;


/**
 * Checks if a bot could move to the given point without breaking any rules.
 * 
 * @author JulianT
 */
public class LegalPointChecker
{
	/**
	 * Checks if a bot would be allowed to move to the given point
	 * 
	 * @param point The position to check
	 * @param aiFrame The current AthenaAiFrame
	 * @param tacticalField
	 * @return false if the point is forbidden
	 */
	public static boolean checkPoint(final IVector2 point, final BaseAiFrame aiFrame, final ITacticalField tacticalField)
	{
		if (point == null)
		{
			// This is a stability fix, so I wont be the one throwing NullPointers
			return true;
		}
		
		// Check game-state independent conditions
		
		// Check outside field
		if (!AIConfig.getGeometry().getField().isPointInShape(point))
		{
			return false;
		}
		
		// Avoid penalty areas
		if (AIConfig.getGeometry().getPenaltyAreaOur()
				.isPointInShape(point, Geometry.getPenaltyAreaMargin() + (AIConfig.getGeometry().getBotRadius() * 2f))
				|| AIConfig.getGeometry().getPenaltyAreaTheir()
						.isPointInShape(point, AIConfig.getGeometry().getBotRadius() * 2f))
		{
			return false;
		}
		
		// Check game state/referee dependent conditions
		if (aiFrame != null)
		{
			EGameState gameState = tacticalField.getGameState();
			if (gameState != EGameState.RUNNING)
			{
				// Check minimum distance to ball
				if (GeoMath.distancePP(aiFrame.getWorldFrame().getBall(), point) <= (AIConfig.getGeometry()
						.getBotToBallDistanceStop() + AIConfig.getGeometry().getBallRadius() + (2 * AIConfig.getGeometry()
						.getBotRadius())))
				{
					return false;
				}
				
				// Bots must be in our half and not in center circle during kickoff (ours or theirs)
				boolean isKickoffState = (gameState == EGameState.PREPARE_KICKOFF_THEY)
						|| (gameState == EGameState.PREPARE_KICKOFF_WE) || tacticalField.isGoalScored();
				boolean isInOurHalf = AIConfig.getGeometry().getOurHalf()
						.isPointInShape(point, -AIConfig.getGeometry().getBotRadius() * 2);
				boolean isInCenterCircle = AIConfig.getGeometry().getCenterCircle()
						.isPointInShape(point, AIConfig.getGeometry().getBotRadius() * 2);
				
				if (isKickoffState && (!isInOurHalf || isInCenterCircle))
				{
					return false;
				}
			}
		}
		
		return true;
	}
}
