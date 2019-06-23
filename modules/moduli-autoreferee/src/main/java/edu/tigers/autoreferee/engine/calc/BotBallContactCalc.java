/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import java.awt.Color;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.BotLastTouchedBallCalculator;


/**
 * This calculator tries to determine the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotBallContactCalc implements IRefereeCalc
{
	private static final Color EXT_COLOR = Color.WHITE;
	@Configurable(comment = "The algorithm to use for ball touch detection")
	private static CalcMode mode = CalcMode.REGULAR;
	
	static
	{
		ConfigRegistration.registerClass("autoreferee", BotBallContactCalc.class);
	}
	
	private ShapeMap curShapes;
	private BotPosition lastBotCloseToBall = new BotPosition();
	private BotPosition lastBotTouchedBall = new BotPosition();
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		curShapes = frame.getShapes();
		long ts = frame.getTimestamp();
		BotLastTouchedBallCalculator calculator = new BotLastTouchedBallCalculator(frame.getWorldFrame(),
				frame.getPreviousFrame().getWorldFrame());
		
		BotID chosenBotCloseToBall = calculator.processByVicinity();
		
		BotID chosenBotTouchedBall;
		if (mode == CalcMode.REGULAR)
		{
			chosenBotTouchedBall = calculator.processByBallHeading();
			List<ITrackedBot> closeBots = calculator.getCloseBots();
			if (closeBots != null)
			{
				closeBots.forEach(bot -> getExtendedLayer().add(
						new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, EXT_COLOR)));
				getExtendedLayer().add(new DrawableLine(calculator.getReversedBallHeading()));
				getExtendedLayer().add(new DrawableCircle(frame.getWorldFrame().getBall().getPos(),
						calculator.getRadius(), EXT_COLOR));
			}
		} else
		{
			chosenBotTouchedBall = chosenBotCloseToBall;
		}
		
		if (chosenBotCloseToBall != null)
		{
			lastBotCloseToBall = new BotPosition(ts, frame.getWorldFrame().getBot(chosenBotCloseToBall));
		}
		
		if (chosenBotTouchedBall != null)
		{
			frame.setBotTouchedBall(new BotPosition(ts, frame.getWorldFrame().getBot(chosenBotTouchedBall)));
			lastBotTouchedBall = new BotPosition(ts, frame.getWorldFrame().getBot(chosenBotTouchedBall));
		}
		
		if (lastBotCloseToBall.getBotID().equals(lastBotTouchedBall.getBotID()))
		{
			addMark(frame, lastBotTouchedBall.getBotID(), Color.MAGENTA);
		} else
		{
			addMark(frame, lastBotTouchedBall.getBotID(), Color.BLUE);
			addMark(frame, lastBotCloseToBall.getBotID(), Color.RED);
		}
		
		frame.setBotLastTouchedBall(lastBotTouchedBall);
		frame.setLastBotCloseToBall(lastBotCloseToBall);
	}
	
	
	private void addMark(final IAutoRefFrame frame, final BotID id, final Color color)
	{
		ITrackedBot bot = frame.getWorldFrame().getBot(id);
		if (bot != null)
		{
			getRegularLayer().add(new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 1.5d, color));
		}
	}
	
	
	private List<IDrawableShape> getRegularLayer()
	{
		return curShapes.get(EAutoRefShapesLayer.LAST_BALL_CONTACT);
	}
	
	
	private List<IDrawableShape> getExtendedLayer()
	{
		return curShapes.get(EAutoRefShapesLayer.LAST_BALL_CONTACT_EXT);
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
