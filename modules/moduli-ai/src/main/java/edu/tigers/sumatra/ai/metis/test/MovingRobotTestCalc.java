package edu.tigers.sumatra.ai.metis.test;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.movingrobot.MovingRobotFactory;
import edu.tigers.sumatra.movingrobot.MovingRobotParams;

import java.util.List;


public class MovingRobotTestCalc extends ACalculator
{
	@Configurable(defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean enabled = false;

	@Configurable(defValue = "STOPPING", unit = EConfigUnit.NO_UNIT)
	private static Kind kind = Kind.STOPPING;

	@Configurable(defValue = "0", unit = EConfigUnit.BOT_ID)
	private static int botNumber = 0;

	@Configurable(defValue = "3", unit = EConfigUnit.TIME_S)
	private static double maxHorizon = 3;

	@Configurable(defValue = "90", unit = EConfigUnit.DISTANCE_MM)
	private static double radius = 90;

	@Configurable(defValue = "0.1", unit = EConfigUnit.TIME_S)
	private static double reactionTime = 0.1;

	@Configurable(defValue = "2", unit = EConfigUnit.VELOCITY_M)
	private static double vLimitAtHorizon = 0.1;

	@Configurable(defValue = "0.1", unit = EConfigUnit.TIME_S)
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
					MovingRobotParams.builder()
							.position(getAiFrame().getWorldFrame().getBot(botId).getPos())
							.velocity(getAiFrame().getWorldFrame().getBot(botId).getVel())
							.vLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getVelMax())
							.aLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getAccMax())
							.brkLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getAccMax())
							.radius(radius)
							.reactionTime(reactionTime)
							.build()
			);
			case STOPPING -> MovingRobotFactory.stoppingRobot(
					MovingRobotParams.builder()
							.position(getAiFrame().getWorldFrame().getBot(botId).getPos())
							.velocity(getAiFrame().getWorldFrame().getBot(botId).getVel())
							.vLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getVelMax())
							.aLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getAccMax())
							.brkLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getBrkMax())
							.radius(radius)
							.reactionTime(reactionTime)
							.build()
			);
			case SLOWING_DOWN -> MovingRobotFactory.slowingDownRobot(
					MovingRobotParams.builder()
							.position(getAiFrame().getWorldFrame().getBot(botId).getPos())
							.velocity(getAiFrame().getWorldFrame().getBot(botId).getVel())
							.vLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getVelMax())
							.aLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getAccMax())
							.brkLimit(getAiFrame().getWorldFrame().getBot(botId).getMoveConstraints().getBrkMax())
							.radius(radius)
							.reactionTime(reactionTime)
							.build(),
					vLimitAtHorizon
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
		SLOWING_DOWN
	}
}
