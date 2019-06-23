/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotVisCalc implements IWpCalc
{
	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		List<IDrawableShape> botsShapes = shapeMap.get(EWpShapesLayer.BOTS);
		wfw.getSimpleWorldFrame().getBots().values().stream().map(this::createBotShape).forEach(botsShapes::add);
		
		List<IDrawableShape> feedbackShapes = shapeMap.get(EWpShapesLayer.BOT_FEEDBACK);
		wfw.getSimpleWorldFrame().getBots().values().stream().map(this::createBotFeedbackShape).filter(Objects::nonNull)
				.forEach(feedbackShapes::add);
		
		List<IDrawableShape> filterShapes = shapeMap.get(EWpShapesLayer.BOT_FILTER);
		wfw.getSimpleWorldFrame().getBots().values().stream().map(this::createBotFilterShape).filter(Objects::nonNull)
				.forEach(filterShapes::add);
		
		List<IDrawableShape> bufferedTrajShapes = shapeMap.get(EWpShapesLayer.BOT_BUFFERED_TRAJ);
		wfw.getSimpleWorldFrame().getBots().values().stream().map(this::createBotBufferedTrajShape)
				.filter(Objects::nonNull)
				.forEach(bufferedTrajShapes::add);
	}
	
	
	private DrawableBotShape createBotFeedbackShape(final ITrackedBot bot)
	{
		if (bot.getRobotInfo().getInternalState().isPresent())
		{
			Pose pose = bot.getRobotInfo().getInternalState().get().getPose();
			DrawableBotShape botShape = new DrawableBotShape(pose.getPos(), pose.getOrientation(),
					Geometry.getBotRadius(), bot.getRobotInfo().getCenter2DribblerDist());
			botShape.setFillColor(null);
			botShape.setBorderColor(Color.GRAY);
			botShape.setFontColor(Color.GRAY);
			botShape.setId(String.valueOf(bot.getBotId().getNumber()));
			return botShape;
		}
		return null;
	}
	
	
	private DrawableBotShape createBotFilterShape(final ITrackedBot bot)
	{
		if (bot.getFilteredState().isPresent())
		{
			Pose pose = bot.getFilteredState().get().getPose();
			DrawableBotShape botShape = new DrawableBotShape(pose.getPos(), pose.getOrientation(),
					Geometry.getBotRadius(), bot.getRobotInfo().getCenter2DribblerDist());
			botShape.setFillColor(null);
			botShape.setBorderColor(Color.WHITE);
			botShape.setFontColor(Color.WHITE);
			botShape.setId(String.valueOf(bot.getBotId().getNumber()));
			return botShape;
		}
		return null;
	}
	
	
	private DrawableBotShape createBotBufferedTrajShape(final ITrackedBot bot)
	{
		if (bot.getBufferedTrajState().isPresent())
		{
			Pose pose = bot.getBufferedTrajState().get().getPose();
			DrawableBotShape botShape = new DrawableBotShape(pose.getPos(), pose.getOrientation(),
					Geometry.getBotRadius(), bot.getRobotInfo().getCenter2DribblerDist());
			botShape.setFillColor(null);
			botShape.setBorderColor(Color.LIGHT_GRAY);
			botShape.setFontColor(Color.LIGHT_GRAY);
			botShape.setId(String.valueOf(bot.getBotId().getNumber()));
			return botShape;
		}
		return null;
	}
	
	
	private DrawableBotShape createBotShape(final ITrackedBot bot)
	{
		DrawableBotShape shape = new DrawableBotShape(bot.getPos(), bot.getOrientation(), Geometry.getBotRadius(),
				bot.getCenter2DribblerDist());
		shape.setBorderColor(Color.black);
		shape.setFillColor(fillColor(bot));
		shape.setFontColor(bot.getTeamColor() == ETeamColor.YELLOW ? Color.black : Color.white);
		shape.setId(String.valueOf(bot.getBotId().getNumber()));
		return shape;
	}
	
	
	private Color fillColor(final ITrackedBot bot)
	{
		Color color = bot.getTeamColor().getColor();
		if (!bot.getFilteredState().isPresent())
		{
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 150);
		}
		return color;
	}
	
}
