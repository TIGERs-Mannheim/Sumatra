/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.03.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.support;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * Checks if a bot could move to the given point without breaking any rules.
 * 
 * @author JulianT
 */
public class LegalPointChecker
{
	
	@Configurable()
	private static double distToPenaltyArea = 1200;
	
	static
	{
		ConfigRegistration.registerClass("Support", LegalPointChecker.class);
	}
	
	
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
		
		EGameStateTeam gameState = tacticalField.getGameState();
		if (point == null)
		{
			// This is a stability fix, so I wont be the one throwing NullPointers
			return false;
		}
		
		// Check game-state independent conditions
		
		// Check outside field
		if (!Geometry.getField().isPointInShape(point))
		{
			return false;
		}
		
		// Avoid penalty areas
		if (Geometry.getPenaltyAreaOur()
				.isPointInShape(point, distToPenaltyArea)
				|| Geometry.getPenaltyAreaTheir()
						.isPointInShape(point, Geometry.getBotRadius() * 2))
		{
			return false;
		}
		
		// Avoid
		if (Geometry.getPenaltyAreaTheir()
				.isPointInShape(point, 200)
				&& ((gameState != EGameStateTeam.THROW_IN_WE) || (gameState != EGameStateTeam.THROW_IN_THEY)))
		{
			return false;
		}
		
		// Check game state/referee dependent conditions
		if (aiFrame != null)
		{
			if (gameState != EGameStateTeam.RUNNING)
			{
				// Check minimum distance to ball
				if (GeoMath.distancePP(aiFrame.getWorldFrame().getBall().getPos(), point) <= (Geometry
						.getBotToBallDistanceStop() + Geometry.getBallRadius() + (2 * Geometry
								.getBotRadius())))
				{
					return false;
				}
				
				// Bots must be in our half and not in center circle during kickoff (ours or theirs)
				Stage stage = aiFrame.getRefereeMsg().getStage();
				boolean isPreStage = ((stage == Stage.EXTRA_FIRST_HALF_PRE) || (stage == Stage.EXTRA_SECOND_HALF_PRE)
						|| (stage == Stage.NORMAL_FIRST_HALF_PRE) || (stage == Stage.NORMAL_SECOND_HALF_PRE));
				boolean isKickoffState = (gameState == EGameStateTeam.PREPARE_KICKOFF_THEY)
						|| (gameState == EGameStateTeam.PREPARE_KICKOFF_WE) || tacticalField.isGoalScored();
				boolean isInOurHalf = Geometry.getHalfOur()
						.isPointInShape(point, -Geometry.getBotRadius() * 2);
				boolean isInCenterCircle = Geometry.getCenterCircle()
						.isPointInShape(point, Geometry.getBotRadius() * 2);
				
				if ((isPreStage || isKickoffState) && (!isInOurHalf || isInCenterCircle))
				{
					return false;
				}
				
				// Keep away from ball-placement pos
				if ((gameState == EGameStateTeam.BALL_PLACEMENT_THEY) || (gameState == EGameStateTeam.BALL_PLACEMENT_WE))
				{
					Circle placementPos = new Circle(aiFrame.getRefereeMsg().getBallPlacementPos(), 500);
					if (placementPos.isPointInShape(point, 2 * Geometry.getBotRadius()))
					{
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
