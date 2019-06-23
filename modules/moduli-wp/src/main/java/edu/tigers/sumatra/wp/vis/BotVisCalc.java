/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.EAiType;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotVisCalc implements IWpCalc
{
	
	private final Map<ETeamColor, Map<EAiType, Color>> colorMap = new EnumMap<>(ETeamColor.class);
	
	
	/**
	 * Default
	 */
	public BotVisCalc()
	{
		Map<EAiType, Color> yellowMap = new EnumMap<>(EAiType.class);
		yellowMap.put(EAiType.PRIMARY, new Color(255, 255, 0));
		yellowMap.put(EAiType.SECONDARY, new Color(255, 200, 0));
		yellowMap.put(EAiType.NONE, new Color(255, 110, 0));
		colorMap.put(ETeamColor.YELLOW, yellowMap);
		Map<EAiType, Color> blueMap = new EnumMap<>(EAiType.class);
		blueMap.put(EAiType.PRIMARY, new Color(0, 0, 255));
		blueMap.put(EAiType.SECONDARY, new Color(150, 0, 255));
		blueMap.put(EAiType.NONE, new Color(255, 0, 255));
		colorMap.put(ETeamColor.BLUE, blueMap);
	}
	
	
	@Override
	public void process(final WorldFrameWrapper wfw)
	{
		List<IDrawableShape> shapes = wfw.getShapeMap().get(EWpShapesLayer.BOTS);
		for (ITrackedBot bot : wfw.getSimpleWorldFrame().getBots().values())
		{
			DrawableBotShape shape = new DrawableBotShape(bot.getPos(), bot.getOrientation(), Geometry.getBotRadius(),
					bot.getCenter2DribblerDist());
			Color color = colorMap.get(bot.getTeamColor()).get(bot.getRobotInfo().getAiType());
			if (!bot.isVisible())
			{
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 150);
			}
			shape.setColor(color);
			shape.setFontColor(bot.getTeamColor() == ETeamColor.YELLOW ? Color.black : Color.white);
			shape.setId(String.valueOf(bot.getBotId().getNumber()));
			shapes.add(shape);
		}
	}
	
}
