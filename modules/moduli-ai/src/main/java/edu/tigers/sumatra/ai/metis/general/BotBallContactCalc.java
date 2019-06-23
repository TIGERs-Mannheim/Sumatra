/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.BotLastTouchedBallCalculator;


/**
 * This calculator tries to determine the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotBallContactCalc extends ACalculator
{
	
	@Configurable(comment = "The algorithm to use for ball touch detection", defValue = "REGULAR")
	private static CalcMode mode = CalcMode.REGULAR;
	
	private BotID lastBotCloseToBall = BotID.noBot();
	private BotID lastBotTouchedBall = BotID.noBot();
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		BotLastTouchedBallCalculator calculator = new BotLastTouchedBallCalculator(getWFrame(),
				baseAiFrame.getPrevFrame().getWorldFrame());
		
		BotID chosenBotCloseToBall = calculator.processByVicinity();
		
		BotID chosenBotTouchedBall;
		if (mode == CalcMode.REGULAR)
		{
			chosenBotTouchedBall = calculator.processByBallHeading();
		} else
		{
			chosenBotTouchedBall = chosenBotCloseToBall;
		}
		
		if (chosenBotCloseToBall != null)
		{
			lastBotCloseToBall = chosenBotCloseToBall;
		}
		
		if (chosenBotTouchedBall != null)
		{
			newTacticalField.setBotTouchedBall(chosenBotTouchedBall);
			lastBotTouchedBall = chosenBotTouchedBall;
		}
		
		newTacticalField.setBotLastTouchedBall(lastBotTouchedBall);
		newTacticalField.setLastBotCloseToBall(lastBotCloseToBall);
		
		ITrackedBot bot = getWFrame().getBot(lastBotTouchedBall);
		if (bot != null)
		{
			newTacticalField
					.getDrawableShapes()
					.get(EAiShapesLayer.BALL_POSSESSION)
					.add(new DrawableCircle(Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 10),
							Color.magenta));
		}
	}
	
	
	/**
	 * @author "Lukas Magel"
	 */
	public enum CalcMode
	{
		/**  */
		REGULAR,
		/**  */
		FALLBACK
	}
}
