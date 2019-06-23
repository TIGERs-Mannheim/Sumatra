/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 6, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableWay;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BufferCalc implements IWpCalc
{
	private static final double									MIN_ANGLE_DIFF	= 0.05;
																								
	private static final double									TIME_BALL		= 3;
	private static final double									TIME_BOTS		= 3;
																								
	private final SortedMap<Long, DrawablePoint>				ballShapes		= new TreeMap<>();
																								
	private final Map<BotID, SortedMap<Long, IVector2>>	buffer			= new HashMap<>();
																								
																								
	@Override
	public void process(final WorldFrameWrapper wfw)
	{
		long curTimestamp = wfw.getSimpleWorldFrame().getTimestamp();
		
		// remove old entries
		for (SortedMap<Long, IVector2> path : buffer.values())
		{
			while (!path.isEmpty() && (((curTimestamp - path.firstKey()) / 1e9) > TIME_BOTS))
			{
				path.remove(path.firstKey());
			}
		}
		
		for (ITrackedBot bot : wfw.getSimpleWorldFrame().getBots().values())
		{
			SortedMap<Long, IVector2> pathPoints = buffer.get(bot.getBotId());
			if (pathPoints == null)
			{
				pathPoints = new TreeMap<>();
				buffer.put(bot.getBotId(), pathPoints);
			}
			if (pathPoints.isEmpty())
			{
				pathPoints.put(wfw.getSimpleWorldFrame().getTimestamp(), bot.getPos());
			} else if (pathPoints.size() < 3)
			{
				IVector2 lastPos = pathPoints.get(pathPoints.lastKey());
				double dist = GeoMath.distancePP(lastPos, bot.getPos());
				if (dist > 1)
				{
					pathPoints.put(wfw.getSimpleWorldFrame().getTimestamp(), bot.getPos());
				}
			} else
			{
				List<IVector2> allPos = new ArrayList<>(pathPoints.values());
				IVector2 lastPos = allPos.get(allPos.size() - 1);
				IVector2 preLastPos = allPos.get(allPos.size() - 2);
				IVector2 lastDir = lastPos.subtractNew(preLastPos);
				
				IVector2 nextDir = bot.getPos().subtractNew(lastPos);
				if ((nextDir.getLength2() > 1) && (lastDir.getLength2() > 1))
				{
					double diff = Math.abs(AngleMath.difference(lastDir.getAngle(), nextDir.getAngle()));
					if (diff < MIN_ANGLE_DIFF)
					{
						pathPoints.remove(pathPoints.lastKey());
					}
					pathPoints.put(wfw.getSimpleWorldFrame().getTimestamp(), bot.getPos());
				}
			}
		}
		
		List<IDrawableShape> shapes = wfw.getShapeMap().get(EWpShapesLayer.BOT_BUFFER);
		for (Map.Entry<BotID, SortedMap<Long, IVector2>> entry : buffer.entrySet())
		{
			BotID botId = entry.getKey();
			SortedMap<Long, IVector2> pathPoints = entry.getValue();
			if (pathPoints.size() > 1)
			{
				Color color = botId.getTeamColor() == ETeamColor.YELLOW ? Color.YELLOW : Color.BLUE;
				DrawableWay dw = new DrawableWay(pathPoints.values(), color);
				shapes.add(dw);
			}
		}
		
		while (!ballShapes.isEmpty() && (((curTimestamp - ballShapes.firstKey()) / 1e9) > TIME_BALL))
		{
			ballShapes.remove(ballShapes.firstKey());
		}
		
		IVector2 ballPos = wfw.getSimpleWorldFrame().getBall().getPos();
		if (ballShapes.isEmpty())
		{
			ballShapes.put(wfw.getSimpleWorldFrame().getTimestamp(), new DrawablePoint(ballPos, Color.red));
		} else
		{
			IVector2 lastBallPos = ballShapes.get(ballShapes.lastKey());
			double dist = GeoMath.distancePP(lastBallPos, ballPos);
			if (dist > 1)
			{
				ballShapes.put(wfw.getSimpleWorldFrame().getTimestamp(), new DrawablePoint(ballPos, Color.red));
			}
		}
		wfw.getShapeMap().get(EWpShapesLayer.BALL_BUFFER).addAll(ballShapes.values());
	}
}
