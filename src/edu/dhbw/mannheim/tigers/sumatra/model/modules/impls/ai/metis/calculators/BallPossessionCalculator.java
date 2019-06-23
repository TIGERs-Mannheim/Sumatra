/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 03.09.2010
 * Authors:
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.TacticalField;


/**
 * This calculator determines wether {@link EBallPossession#WE}, {@link EBallPossession#THEY},
 * {@link EBallPossession#BOTH} or {@link EBallPossession#NONE} has the ball.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Gero
 */
public class BallPossessionCalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	private static final int	NO_BOT					= -1;
	
	/**
	 * minmum distance a bot must have for being in ball possession.
	 * radius Ball + distance(10cm) + radius Bot
	 */
	private final float			BALL_RADIUS				= AIConfig.getGeometry().getBallRadius();
	private final float			MIN_DIST_TO_BOT		= AIConfig.getCalculators().getMinDistToBot();
	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final float			POSSESSION_THRESHOLD	= BALL_RADIUS + MIN_DIST_TO_BOT + BOT_RADIUS;
	private final float			SPEED_LIMIT				= 500;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public BallPossessionCalculator()
	{
	}
	

	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * This function calculate the ball possession, the id of the bot with the ball and return the result
	 * @return BallPossessionObject
	 */
	public BallPossession calculate(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		final TacticalField tacticalField = curFrame.tacticalInfo;
		
		// Which robots are near the ball? (Only closest of each team is considered)
		final BotDistance closestTiger = tacticalField.getTigerClosestToBall();
		final BotDistance closestEnemy = tacticalField.getEnemyClosestToBall();
		
		// At this point one could check if both BotDistances are equal to BotDistance.NULL_BOT_DISTANCE.
		// As NULL_BOT_DISTANCE.dist == Float.MAX_VALUE, the current decision is that no one has the ball!
		
		final int tigersHave = closestTiger.dist < POSSESSION_THRESHOLD ? closestTiger.bot.id : NO_BOT;
		final int opponentsHave = closestEnemy.dist < POSSESSION_THRESHOLD ? closestEnemy.bot.id : NO_BOT;
		Vector2f ballVel = worldFrame.ball.vel;
		

		// Fill BallPossession
		BallPossession currentPossession = new BallPossession();
		
		if (ballVel.getLength2() <= SPEED_LIMIT)
		{
			if (tigersHave != NO_BOT)
			{
				if (opponentsHave != NO_BOT)
				{
					currentPossession.setEBallPossession(EBallPossession.BOTH);
					currentPossession.setOpponentsId(opponentsHave);
					currentPossession.setTigersId(tigersHave);
				} else
				{
					currentPossession.setEBallPossession(EBallPossession.WE);
					currentPossession.setTigersId(tigersHave);
				}
			} else
			{
				if (opponentsHave != NO_BOT)
				{
					currentPossession.setEBallPossession(EBallPossession.THEY);
					currentPossession.setOpponentsId(opponentsHave);
				} else
				{
					currentPossession.setEBallPossession(EBallPossession.NO_ONE);
				}
			}
		} else
		{
			currentPossession = preFrame.tacticalInfo.getBallPossesion();
		}
		
		return currentPossession;
	}
}
