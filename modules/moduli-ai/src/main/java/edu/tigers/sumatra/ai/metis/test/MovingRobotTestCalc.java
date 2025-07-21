/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.test;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.movingrobot.MovingRobotFactory;

import java.util.List;


public class MovingRobotTestCalc extends ACalculator
{
	@Configurable(defValue = "false")
	private static boolean enabled = false;

	@Configurable(defValue = "STOPPING")
	private static Kind kind = Kind.STOPPING;

	@Configurable(defValue = "0")
	private static int botNumber = 0;

	@Configurable(defValue = "3")
	private static double maxHorizon = 3;

	@Configurable(defValue = "90")
	private static double radius = 90;

	@Configurable(defValue = "0.1")
	private static double reactionTime = 0.1;

	@Configurable(defValue = "0.1")
	private static double stepSize = 0.1;


	@Override
	protected boolean isCalculationNecessary()
	{
		return enabled;
	}


	@Override
	protected void doCalc()
	{
		var botId = BotID.createBotId(botNumber, getAiFrame().getTeamColor());
		var movingRobot = switch (kind)
		{
			case ACCELERATING -> MovingRobotFactory.acceleratingRobot(
					getAiFrame().getWorldFrame().getBot(botId).getPos(),
					getAiFrame().getWorldFrame().getBot(botId).getVel(),
					getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getVelMax(),
					getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getAccMax(),
					radius,
					reactionTime
			);
			case STOPPING -> MovingRobotFactory.stoppingRobot(
					getAiFrame().getWorldFrame().getBot(botId).getPos(),
					getAiFrame().getWorldFrame().getBot(botId).getVel(),
					getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getVelMax(),
					getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getAccMax(),
					getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getBrkMax(),
					radius,
					reactionTime
			);
		};

		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.TEST_GRID_ADDITIONAL);

		for (double t = 0; t < maxHorizon; t += stepSize)
		{
			var color = ColorPickerFactory.greenRedGradient().getColor(t / maxHorizon);
			shapes.add(new DrawableCircle(movingRobot.getMovingHorizon(t)).setColor(color));
		}
	}


	private enum Kind
	{
		ACCELERATING,
		STOPPING,
	}
}
