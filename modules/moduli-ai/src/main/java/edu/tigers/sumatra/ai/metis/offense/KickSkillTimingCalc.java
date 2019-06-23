/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 23, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.DrawableTrajectoryPath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.skillsystem.driver.KickSkillCalc;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickSkillTimingCalc extends ACalculator
{
	private final DynamicPosition					receiver		= new DynamicPosition(Geometry.getGoalTheir().getGoalCenter());
	private final Map<BotID, KickSkillCalc>	calcs			= new LinkedHashMap<>();
	private final DecimalFormat					decFormat	= new DecimalFormat("0.00");
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.KICK_SKILL_TIMING);
		receiver.update(new DynamicPosition(newTacticalField.getBestDirectShootTarget()));
		
		Map<BotID, Double> timing = newTacticalField.getKickSkillTimes();
		
		for (Map.Entry<BotID, ITrackedBot> entry : baseAiFrame.getWorldFrame().getTigerBotsAvailable().entrySet())
		{
			BotID botId = entry.getKey();
			ITrackedBot tBot = entry.getValue();
			if (botId.equals(baseAiFrame.getKeeperId()))
			{
				continue;
			}
			KickSkillCalc calc = calcs.get(botId);
			if (calc == null)
			{
				calc = new KickSkillCalc(receiver);
				calcs.put(botId, calc);
			}
			
			Optional<TrajectoryWithTime<IVector2>> path = calc.estimatePath(tBot, baseAiFrame.getWorldFrame());
			double time;
			if (!path.isPresent())
			{
				time = Double.POSITIVE_INFINITY;
			} else
			{
				time = path.get().getRemainingTrajectoryTime(baseAiFrame.getWorldFrame().getTimestamp());
				shapes.add(new DrawableTrajectoryPath(path.get().getTrajectory(), Color.gray));
				shapes.add(new DrawableCircle(new Circle(path.get().getFinalDestination(), 30), Color.cyan));
			}
			timing.put(botId, time);
			
			shapes.add(new DrawableText(tBot.getPos(), decFormat.format(time), Color.gray));
			
			shapes.add(new DrawableCircle(new Circle(calc.getCatchDest().getXYVector(), 20), Color.magenta));
		}
		
		Optional<Map.Entry<BotID, Double>> time = timing.entrySet().stream()
				.sorted((e1, e2) -> Double.compare(e1.getValue(), e2.getValue())).findFirst();
		if (time.isPresent())
		{
			IVector2 pos = baseAiFrame.getWorldFrame().getBot(time.get().getKey()).getPos();
			shapes.add(new DrawableCircle(new Circle(pos, 120), Color.cyan));
		}
	}
	
}
