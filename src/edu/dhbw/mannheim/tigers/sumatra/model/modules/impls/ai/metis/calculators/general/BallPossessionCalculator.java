/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 03.09.2010
 * Authors:
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * This calculator determines whether {@link EBallPossession#WE}, {@link EBallPossession#THEY},
 * {@link EBallPossession#BOTH} or {@link EBallPossession#NO_ONE} has the ball.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Gero
 */
public class BallPossessionCalculator extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	private static final BotID	NO_BOT				= new BotID();
	private static final float	SPEED_LIMIT			= 500;
	
	/**
	 * minmum distance a bot must have for being in ball possession.
	 * radius Ball + distance(10cm) + radius Bot
	 */
	private final float			ballRadius			= AIConfig.getGeometry().getBallRadius();
	private final float			minDistToBot		= AIConfig.getMetisCalculators().getMinDistToBot();
	private final float			botRadius			= AIConfig.getGeometry().getBotRadius();
	private final float			possesionTreshold	= ballRadius + minDistToBot + botRadius;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public BallPossessionCalculator()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * This function calculate the ball possession, the id of the bot with the ball and return the result
	 * @param curFrame
	 * @param preFrame
	 */
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		final TacticalField tacticalField = curFrame.tacticalInfo;
		
		// Which robots are near the ball? (Only closest of each team is considered)
		final BotDistance closestTiger = tacticalField.getTigerClosestToBall();
		final BotDistance closestEnemy = tacticalField.getEnemyClosestToBall();
		
		// At this point one could check if both BotDistances are equal to BotDistance.NULL_BOT_DISTANCE.
		// As NULL_BOT_DISTANCE.dist == Float.MAX_VALUE, the current decision is that no one has the ball!
		
		final BotID tigersHave = closestTiger.getDist() < possesionTreshold ? closestTiger.getBot().getId() : NO_BOT;
		final BotID opponentsHave = closestEnemy.getDist() < possesionTreshold ? closestEnemy.getBot().getId() : NO_BOT;
		final IVector2 ballVel = worldFrame.ball.getVel();
		
		
		// Fill BallPossession
		BallPossession currentPossession = new BallPossession();
		
		if (ballVel.getLength2() <= SPEED_LIMIT)
		{
			if (!tigersHave.equals(NO_BOT))
			{
				if (!opponentsHave.equals(NO_BOT))
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
				if (!opponentsHave.equals(NO_BOT))
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
			currentPossession = preFrame.tacticalInfo.getBallPossession();
		}
		curFrame.tacticalInfo.setBallPossesion(currentPossession);
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		curFrame.tacticalInfo.setBallPossesion(new BallPossession());
	}
}
