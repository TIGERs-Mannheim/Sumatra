/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 03.09.2010
 * Authors:
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This calculator determines whether {@link EBallPossession#WE}, {@link EBallPossession#THEY},
 * {@link EBallPossession#BOTH} or {@link EBallPossession#NO_ONE} has the ball.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Gero
 */
public class BallPossessionCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	private static final BotID	NO_BOT					= BotID.get();
	
	@Configurable(comment = "if ball is faster than this, ball possession will not be changed.")
	private static double		ballSpeedTolerance	= 3.0;
	
	@Configurable(comment = "minmum distance a bot must have for being in ball possession.")
	private static double		minDistToBot			= 150;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public BallPossessionCalc()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		
		// Which robots are near the ball? (Only closest of each team is considered)
		final BotDistance closestTiger = newTacticalField.getTigerClosestToBall();
		final BotDistance closestEnemy = newTacticalField.getEnemyClosestToBall();
		
		// At this point one could check if both BotDistances are equal to BotDistance.NULL_BOT_DISTANCE.
		// As NULL_BOT_DISTANCE.dist == Double.MAX_VALUE, the current decision is that no one has the ball!
		
		final double tolerance = Geometry.getBallRadius() + minDistToBot
				+ Geometry.getBotRadius();
		final BotID tigersHave = closestTiger.getDist() < tolerance ? closestTiger.getBot().getBotId() : NO_BOT;
		final BotID opponentsHave = closestEnemy.getDist() < tolerance ? closestEnemy.getBot().getBotId() : NO_BOT;
		final IVector2 ballVel = wFrame.getBall().getVel();
		
		
		// Fill BallPossession
		BallPossession currentPossession = new BallPossession();
		
		if (ballVel.getLength2() <= ballSpeedTolerance)
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
				ITrackedBot bot = baseAiFrame.getWorldFrame().getBot(tigersHave);
				newTacticalField
						.getDrawableShapes()
						.get(EShapesLayer.BALL_POSSESSION)
						.add(new DrawableCircle(new Circle(bot.getPos(), Geometry.getBotRadius() + 20),
								Color.BLACK));
			} else
			{
				if (!opponentsHave.equals(NO_BOT))
				{
					currentPossession.setEBallPossession(EBallPossession.THEY);
					currentPossession.setOpponentsId(opponentsHave);
					ITrackedBot bot = baseAiFrame.getWorldFrame().getBot(opponentsHave);
					newTacticalField
							.getDrawableShapes()
							.get(EShapesLayer.BALL_POSSESSION)
							.add(new DrawableCircle(new Circle(bot.getPos(), Geometry.getBotRadius() + 20),
									Color.BLACK));
				} else
				{
					currentPossession.setEBallPossession(EBallPossession.NO_ONE);
				}
			}
		} else
		{
			currentPossession = baseAiFrame.getPrevFrame().getTacticalField().getBallPossession();
		}
		
		newTacticalField.setBallPossession(currentPossession);
	}
}
