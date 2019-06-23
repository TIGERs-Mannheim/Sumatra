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

import java.awt.Color;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


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
	private static final BotID	NO_BOT					= BotID.createBotId();
	
	@Configurable(comment = "if ball is faster than this, ball possession will not be changed.")
	private static float			ballSpeedTolerance	= 3.0f;
	
	@Configurable(comment = "minmum distance a bot must have for being in ball possession.")
	private static float			minDistToBot			= 150;
	
	
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
		// As NULL_BOT_DISTANCE.dist == Float.MAX_VALUE, the current decision is that no one has the ball!
		
		final float tolerance = AIConfig.getGeometry().getBallRadius() + minDistToBot
				+ AIConfig.getGeometry().getBotRadius();
		final BotID tigersHave = closestTiger.getDist() < tolerance ? closestTiger.getBot().getId() : NO_BOT;
		final BotID opponentsHave = closestEnemy.getDist() < tolerance ? closestEnemy.getBot().getId() : NO_BOT;
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
				TrackedTigerBot bot = baseAiFrame.getWorldFrame().getBot(tigersHave);
				newTacticalField
						.getDrawableShapes()
						.get(EDrawableShapesLayer.BALL_POSSESSION)
						.add(new DrawableCircle(new Circle(bot.getPos(), AIConfig.getGeometry().getBotRadius() + 20),
								Color.BLACK));
			} else
			{
				if (!opponentsHave.equals(NO_BOT))
				{
					currentPossession.setEBallPossession(EBallPossession.THEY);
					currentPossession.setOpponentsId(opponentsHave);
					TrackedTigerBot bot = baseAiFrame.getWorldFrame().getBot(opponentsHave);
					newTacticalField
							.getDrawableShapes()
							.get(EDrawableShapesLayer.BALL_POSSESSION)
							.add(new DrawableCircle(new Circle(bot.getPos(), AIConfig.getGeometry().getBotRadius() + 20),
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
