/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.movingrobot.AcceleratingRobotFactory;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.pathfinder.IMovementCon;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Generator for all the obstacles that are required during a regular match.
 */
public class ObstacleGenerator
{
	@Configurable(defValue = "50.0", comment = "Default security distance [mm] to ball (in addition to ball radius)")
	private static double defaultDistanceToBall = 50;

	@Configurable(defValue = "1.0", comment = "Maximum time [s] to which opponent robots are considered")
	private static double opponentBotTimeHorizon = 1.0;

	@Configurable(defValue = "0.5", comment = "Maximum time [s] to which opponent robots are predicted")
	private static double opponentBotMaxMovingTimeHorizon = 0.3;

	@Configurable(defValue = "0.1", comment = "Reaction time of opponent [s] for MovingRobots. Predicts constant velocity in this time.")
	private static double opponentBotReactionTime = 0.1;

	@Configurable(defValue = "0.4", comment = "Maximum time [s] to which opponent robots are predicted (const velocity)")
	private static double opponentBotTimeHorizonForConstVel = 0.4;

	@Configurable(defValue = "0.2", comment = "Maximum time [s] to which own robots are predicted (when not using trajectory)")
	private static double ownBotTimeHorizon = 0.2;

	@Configurable(defValue = "0.5", comment = "Maximum time [s] to which own robots are considered that have higher path priority")
	private static double ownBotTimeHorizonOnPrio = 0.5;

	@Configurable(defValue = "0.7", comment = "Min velocity [m/s] of our bot where it considers opponents as obstacle")
	private static double minVelocity = 0.7;

	@Configurable(defValue = "2.0", comment = "Assumed placement pass receive speed [m/s] for obstacle")
	private static double placementPassReceiveSpeed = 2.0;

	@Configurable(defValue = "30.0", comment = "Extra margin [mm] on the opponent penArea during free kick")
	private static double opponentPenAreaStandardExtraMargin = 30;

	@Configurable(defValue = "true", comment = "Use tube for movingRobot obstacle")
	private static boolean useTubeForMovingRobot = true;


	private IMovementCon moveCon;

	private final List<IObstacle> obsGoalPostOur = new ArrayList<>();
	private final List<IObstacle> obsGoalPostTheir = new ArrayList<>();

	static
	{
		ConfigRegistration.registerClass("sisyphus", ObstacleGenerator.class);
	}


	public ObstacleGenerator(final IMovementCon moveCon)
	{
		this.moveCon = moveCon;
		createGoalPostOur();
		createGoalPostTheir();
	}


	private void createGoalPostOur()
	{
		IVector2 gpl = Geometry.getGoalOur().getLeftPost();
		IVector2 gplb = gpl.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth(), 0));
		IVector2 gpr = Geometry.getGoalOur().getRightPost();
		IVector2 gprb = gpr.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth(), 0));
		obsGoalPostOur.add(new TubeObstacle("goalPostOurLeft", Tube.create(gpl, gplb, Geometry.getBotRadius())));
		obsGoalPostOur.add(new TubeObstacle("goalPostOurRight", Tube.create(gpr, gprb, Geometry.getBotRadius())));
		obsGoalPostOur.add(new TubeObstacle("goalPostOurBack", Tube.create(gplb, gprb, Geometry.getBotRadius())));
	}


	private void createGoalPostTheir()
	{
		IVector2 gpl = Geometry.getGoalTheir().getLeftPost();
		IVector2 gplb = gpl.addNew(Vector2.fromXY(Geometry.getGoalTheir().getDepth(), 0));
		IVector2 gpr = Geometry.getGoalTheir().getRightPost();
		IVector2 gprb = gpr.addNew(Vector2.fromXY(Geometry.getGoalTheir().getDepth(), 0));
		obsGoalPostTheir.add(new TubeObstacle("goalPostTheirLeft", Tube.create(gpl, gplb, Geometry.getBotRadius())));
		obsGoalPostTheir.add(new TubeObstacle("goalPostTheirRight", Tube.create(gpr, gprb, Geometry.getBotRadius())));
		obsGoalPostTheir.add(new TubeObstacle("goalPostTheirBack", Tube.create(gplb, gprb, Geometry.getBotRadius())));
	}


	private List<IObstacle> genOurBots(
			final WorldFrame wFrame,
			final BotID botId
	)
	{
		return wFrame.getTigerBotsVisible().values().stream()
				.filter(tBot -> !tBot.getBotId().equals(botId))
				.filter(tBot -> !moveCon.getIgnoredBots().contains(tBot.getBotId()))
				.map(tBot -> selectObstacleForOurBot(wFrame, botId, tBot))
				.map(IObstacle.class::cast)
				.toList();
	}


	private IObstacle selectObstacleForOurBot(WorldFrame wFrame, BotID botId, ITrackedBot bot)
	{
		double radius = 2 * Geometry.getBotRadius();
		var trajectory = bot.getRobotInfo().getTrajectory();
		if (trajectory.isEmpty())
		{
			var tube = Tube.create(bot.getPos(), bot.getPosByTime(ownBotTimeHorizon), radius);
			// assume a static (motionless) obstacle, as we may have no control over the robot without a trajectory
			return new TubeObstacle(bot.getBotId().getSaveableString(), tube);
		}

		var trajectoryAge = (wFrame.getTimestamp() - bot.getTimestamp()) / 1e9;
		var traj = trajectory.get();
		var tStart = Math.min(trajectoryAge, traj.getTotalTime());
		var extraMarginBase = 0;
		var maxSpeed = traj.getMaxSpeed();
		var obs = new TrajAwareRobotObstacle(bot.getBotId(), traj, radius, tStart, extraMarginBase)
				.setMaxSpeed(maxSpeed);

		obs.setOrderId(IObstacle.BOT_OUR_ORDER_ID);

		if (moveCon.getObstacleAvoidanceMode() == EObstacleAvoidanceMode.AGGRESSIVE)
		{
			obs.setUseDynamicMargin(false);
		}

		if (moveCon.getPrioMap().isPreferred(botId, bot.getBotId()))
		{
			return new LimitedTimeObstacle(obs, ownBotTimeHorizonOnPrio);
		}
		return obs.setHasPriority(true);
	}


	private List<IObstacle> genTheirBots(WorldFrame wFrame)
	{
		return wFrame.getOpponentBots().values().stream()
				.filter(tBot -> !moveCon.getIgnoredBots().contains(tBot.getBotId()))
				.map(this::opponentObstacle)
				.map(o -> (IObstacle) new LimitedTimeObstacle(o, opponentBotTimeHorizon))
				.toList();
	}


	private IObstacle opponentObstacle(ITrackedBot tBot)
	{
		return switch (moveCon.getObstacleAvoidanceMode())
		{
			case NORMAL -> movingRobotObstacle(tBot);
			case AGGRESSIVE -> constVelocityObstacle(tBot);
		};
	}


	private IObstacle movingRobotObstacle(ITrackedBot tBot)
	{
		IMovingRobot movingRobot = movingRobot(tBot);
		if (useTubeForMovingRobot)
		{
			return new MovingRobotTubeObstacle(tBot.getBotId(), movingRobot, minVelocity, opponentBotMaxMovingTimeHorizon)
					.setMaxSpeed(tBot.getMoveConstraints().getVelMax())
					.setOrderId(IObstacle.BOT_THEIR_ORDER_ID);
		}
		return new MovingRobotObstacle(tBot.getBotId(), movingRobot, minVelocity, opponentBotMaxMovingTimeHorizon)
				.setMaxSpeed(tBot.getMoveConstraints().getVelMax())
				.setOrderId(IObstacle.BOT_THEIR_ORDER_ID);
	}


	private IObstacle constVelocityObstacle(ITrackedBot tBot)
	{
		return new ConstVelocityObstacle(
				tBot.getPos(),
				tBot.getVel(),
				2 * Geometry.getBotRadius(),
				opponentBotTimeHorizonForConstVel,
				minVelocity
		)
				.setMaxSpeed(tBot.getVel().getLength2())
				.setOrderId(IObstacle.BOT_THEIR_ORDER_ID);
	}


	private IMovingRobot movingRobot(ITrackedBot tBot)
	{
		double speed = tBot.getVel().getLength2();
		return AcceleratingRobotFactory.create(
				tBot.getPos(),
				speed < 0.5 ? Vector2.zero() : tBot.getVel(),
				Math.max(tBot.getMoveConstraints().getVelMax(), speed),
				tBot.getMoveConstraints().getAccMax(),
				2 * Geometry.getBotRadius(),
				opponentBotReactionTime
		);
	}


	/**
	 * Generate all required obstacles based on the current generator state and given input.
	 *
	 * @param wFrame    the current world frame
	 * @param forBotId  the id of the bot that the obstacle are for
	 * @param gameState the current game state
	 * @return a list of the generated obstacles
	 */
	public List<IObstacle> generateObstacles(
			final WorldFrame wFrame,
			final BotID forBotId,
			final GameState gameState)
	{
		List<IObstacle> obstacles = generateStaticObstacles(gameState);

		if (moveCon.isOurBotsObstacle())
		{
			obstacles.addAll(genOurBots(wFrame, forBotId));
		}

		if (moveCon.isTheirBotsObstacle())
		{
			obstacles.addAll(genTheirBots(wFrame));
		}

		if (moveCon.isBallObstacle())
		{
			double distanceToBall = Optional.ofNullable(moveCon.getDistanceToBall()).orElse(defaultDistanceToBall);
			obstacles.add(new SimpleTimeAwareBallObstacle(
					wFrame.getBall().getTrajectory(),
					Geometry.getBallRadius() + Geometry.getBotRadius() + distanceToBall
			).setOrderId(IObstacle.BALL_ORDER_ID));
		}

		if (moveCon.isGameStateObstacle())
		{
			obstacles.addAll(generateGameStateObstacles(wFrame, gameState));
		}

		obstacles.addAll(moveCon.getCustomObstacles());

		return obstacles;
	}


	private double getEffectiveBotToBallDistanceOnStop()
	{
		return RuleConstraints.getStopRadius() + Geometry.getBotRadius();
	}


	private List<IObstacle> generateGameStateObstacles(WorldFrame wFrame, GameState gameState)
	{
		List<IObstacle> obs = new ArrayList<>();

		if (gameState.isBallPlacement() || gameState.isDistanceToBallRequired())
		{
			var ballPos = wFrame.getBall().getPos();
			var placementPos = Optional.ofNullable(gameState.getBallPlacementPositionForUs()).orElse(ballPos);
			if (gameState.isBallPlacementForUs())
			{
				obs.add(createBallPlacementPassObstacle(ballPos, placementPos));
			} else
			{
				obs.add(new TubeObstacle("placement",
						Tube.create(ballPos, placementPos, getEffectiveBotToBallDistanceOnStop()))
						.setOrderId(IObstacle.BALL_ORDER_ID));
			}
		}

		return obs;
	}


	private IObstacle createBallPlacementPassObstacle(
			IVector2 ballPos,
			IVector2 placementPos
	)
	{
		var distance = ballPos.distanceTo(placementPos);
		var ballFactory = Geometry.getBallFactory();
		var kickSpeed = ballFactory.createFlatConsultant().getInitVelForDist(distance, placementPassReceiveSpeed);
		var kickVel = placementPos.subtractNew(ballPos).scaleTo(kickSpeed * 1000).getXYZVector();
		var trajectory = ballFactory.createTrajectoryFromKickedBallWithoutSpin(ballPos, kickVel);
		var tEnd = trajectory.getTimeByPos(placementPos);
		return new SimpleTimeAwareBallObstacle(
				trajectory,
				getEffectiveBotToBallDistanceOnStop(),
				tEnd
		).setOrderId(IObstacle.BALL_ORDER_ID);
	}


	private List<IObstacle> generateStaticObstacles(GameState gameState)
	{
		List<IObstacle> obstacles = new ArrayList<>();

		if (moveCon.isFieldBorderObstacle())
		{
			IRectangle rect = Geometry.getFieldWBorders().withMargin(-Geometry.getBotRadius());
			obstacles.add(new FieldBorderObstacle(rect));
		}

		if (moveCon.isGoalPostsObstacle())
		{
			obstacles.addAll(obsGoalPostOur);
			obstacles.addAll(obsGoalPostTheir);
		}
		if (moveCon.isPenaltyAreaOurObstacle())
		{
			obstacles.add(new PenaltyAreaObstacle(Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius())));
		}
		if (moveCon.isPenaltyAreaTheirObstacle())
		{
			obstacles.add(new PenaltyAreaObstacle(
					Geometry.getPenaltyAreaTheir().withMargin(getMarginToPenArea(gameState))
			));
		}
		return obstacles;
	}


	private double getMarginToPenArea(GameState gameState)
	{
		if (gameState.isStandardSituation() || gameState.isStoppedGame() || gameState.isBallPlacement())
		{
			return RuleConstraints.getPenAreaMarginStandard() + Geometry.getBotRadius()
					+ opponentPenAreaStandardExtraMargin;
		}
		return 0.0;
	}
}
