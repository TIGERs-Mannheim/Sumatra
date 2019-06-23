/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.06.2016
 * Author(s): julian
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.event.EGameEvent;
import edu.tigers.sumatra.ai.data.event.GameEventFrame;
import edu.tigers.sumatra.ai.data.event.IGameEventDetector;
import edu.tigers.sumatra.ai.data.event.IGameEventStorage;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Detector to track man-to-man or man-to-goal marked bots
 * 
 * @author julian
 */
public class MarkingDetector implements IGameEventDetector
{
	@Configurable(comment = "Maximum radius in which an opponent bot is considered marking [mm]")
	private static int				markingRadius		= 600;
	
	@Configurable(comment = "Maximum angle of marker to relevant vector [deg]")
	private static int				markingAngleDeg	= 40;
	
	@Configurable(comment = "Number of frames after which a bot is considered marked [1]")
	private static int				markingDelay		= 10;
	
	@Configurable(comment = "Number of frames after which a bot is considered unmarked [1]")
	private static int				notMarkingDelay	= 10;
	
	
	private double						markingAngle		= 0.0;
	private Map<BotID, Integer>	markedFrames		= null;
	private Map<BotID, Integer>	notMarkedFrames	= null;
	
	static
	{
		ConfigRegistration.registerClass("data", MarkingDetector.class);
	}
	
	
	/**
	 * Standard constructor
	 */
	public MarkingDetector()
	{
		markingAngle = AngleMath.deg2rad(markingAngleDeg);
		markedFrames = new HashMap<BotID, Integer>();
		notMarkedFrames = new HashMap<BotID, Integer>();
	}
	
	
	@Override
	public GameEventFrame getActiveParticipant(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		GameEventFrame frame = new GameEventFrame();
		IGameEventStorage markingEvents = baseAiFrame.getPrevFrame().getTacticalField().getGameEvents().storedEvents
				.get(EGameEvent.MARKING);
		
		for (ITrackedBot bot : baseAiFrame.getWorldFrame().getTigerBotsAvailable().values())
		{
			IBotIDMap<ITrackedBot> markingBots = getMarkingBots(bot, baseAiFrame);
			boolean wasMarked = true;
			
			if (markedFrames.get(bot.getBotId()) == null)
			{
				markedFrames.put(bot.getBotId(), 0);
			}
			
			if (notMarkedFrames.get(bot.getBotId()) == null)
			{
				notMarkedFrames.put(bot.getBotId(), 0);
			}
			
			try
			{
				markingEvents.getEventsForSingleBot(bot.getBotId()).get(0);
			} catch (NullPointerException npe)
			{
				wasMarked = false;
			}
			
			if (!markingBots.isEmpty())
			{
				if (wasMarked || (markedFrames.get(bot.getBotId()) > markingDelay))
				{
					frame.putInvolvedBots(bot.getBotId(), new ArrayList<BotID>(markingBots.keySet()));
					markedFrames.put(bot.getBotId(), 0);
				} else
				{
					Integer update = markedFrames.get(bot.getBotId()) + 1;
					markedFrames.put(bot.getBotId(), update);
				}
			} else
			{
				if (!wasMarked || (notMarkedFrames.get(bot.getBotId()) > notMarkingDelay))
				{
					notMarkedFrames.put(bot.getBotId(), 0);
				} else
				{
					frame.putInvolvedBots(bot.getBotId(), new ArrayList<BotID>(markingBots.keySet()));
					Integer update = notMarkedFrames.get(bot.getBotId()) + 1;
					notMarkedFrames.put(bot.getBotId(), update);
				}
			}
		}
		
		drawMarkingBots(frame, newTacticalField, baseAiFrame);
		return frame;
	}
	
	
	private IBotIDMap<ITrackedBot> getMarkingBots(final ITrackedBot bot, final BaseAiFrame baseAiFrame)
	{
		IBotIDMap<ITrackedBot> markingBots = new BotIDMap<ITrackedBot>();
		Circle environment = new Circle(bot.getPos(), markingRadius);
		IBotIDMap<ITrackedBot> botsInShape = AiMath.getBotsInShape(environment, baseAiFrame.getWorldFrame().getFoeBots());
		
		for (ITrackedBot botInShape : botsInShape.values())
		{
			IVector2 botVector = botInShape.getPos().subtractNew(bot.getPos());
			
			IVector2 ballVector = baseAiFrame.getWorldFrame().getBall().getPos().subtractNew(bot.getPos());
			IVector2 leftGoalVector = Geometry.getGoalLineTheir().supportVector().subtractNew(bot.getPos());
			IVector2 rightGoalVector = Geometry.getGoalLineTheir().supportVector()
					.addNew(Geometry.getGoalLineTheir().directionVector()).subtractNew(bot.getPos());
			
			double ballAngle = GeoMath.angleBetweenVectorAndVectorWithNegative(botVector, ballVector);
			double leftGoalAngle = GeoMath.angleBetweenVectorAndVectorWithNegative(botVector, leftGoalVector);
			double rightGoalAngle = GeoMath.angleBetweenVectorAndVectorWithNegative(botVector, rightGoalVector);
			
			if ((Math.abs(ballAngle) <= markingAngle) || (Math.abs(leftGoalAngle) <= markingAngle)
					|| (Math.abs(rightGoalAngle) <= markingAngle))
			{
				markingBots.put(botInShape.getBotId(), botInShape);
			}
		}
		
		
		return markingBots;
	}
	
	
	private void drawMarkingBots(final GameEventFrame frame, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.MARKERS);
		Color blue = new Color(0, 0, 255, 128);
		
		for (Entry<BotID, List<BotID>> entry : frame.getEntrySet())
		{
			IVector2 botPos = baseAiFrame.getWorldFrame().getBot(entry.getKey()).getPos();
			
			for (BotID involvedBot : entry.getValue())
			{
				IVector2 involvedBotPos = baseAiFrame.getWorldFrame().getBot(involvedBot).getPos();
				
				DrawableLine markerLine = new DrawableLine(Line.newLine(botPos, involvedBotPos), blue);
				shapes.add(markerLine);
			}
		}
	}
}
