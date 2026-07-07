package edu.tigers.sumatra.pathfinder.obstacles;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.movingrobot.MovingRobotFactory;
import edu.tigers.sumatra.movingrobot.MovingRobotParams;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Generator for all the obstacles that are required during a regular match.
 */
public class ObstacleGenerator
{
	@Configurable(defValue = "50.0", comment = "Default security distance to ball (in addition to ball radius)", unit = EConfigUnit.DISTANCE_MM)
	private static double defaultDistanceToBall = 50;

	@Configurable(defValue = "1.0", comment = "Maximum time to which opponent robots are considered", unit = EConfigUnit.TIME_S)
	private static double opponentBotTimeHorizon = 1.0;

	@Configurable(defValue = "0.5", comment = "Maximum time to which opponent robots are predicted", unit = EConfigUnit.TIME_S)
	private static double opponentBotMaxMovingTimeHorizon = 0.5;

	@Configurable(defValue = "0.1", comment = "Reaction time of opponent for MovingRobots. Predicts constant velocity in this time.", unit = EConfigUnit.TIME_S)
	private static double opponentBotReactionTime = 0.1;

	@Configurable(defValue = "0.4", comment = "Maximum time to which opponent robots are predicted (const velocity)", unit = EConfigUnit.TIME_S)
	private static double opponentBotTimeHorizonForConstVel = 0.4;

	@Configurable(defValue = "0.0", comment = "Maximum time to which own robots are considered that have lower path priority", unit = EConfigUnit.TIME_S)
	private static double ownBotTimeHorizonOnPrio = 0.0;

	@Configurable(defValue = "0.7", comment = "Min velocity of our bot where it considers opponents as obstacle", unit = EConfigUnit.VELOCITY_M)
	private static double minVelocity = 0.7;

	@Configurable(defValue = "2.0", comment = "Assumed placement pass receive speed for obstacle", unit = EConfigUnit.VELOCITY_M)
	private static double placementPassReceiveSpeed = 2.0;

	@Configurable(defValue = "30.0", comment = "Extra margin on the opponent penArea during free kick", unit = EConfigUnit.DISTANCE_MM)
	private static double opponentPenAreaStandardExtraMargin = 30;

	@Configurable(defValue = "true", comment = "Use tube for movingRobot obstacle", unit = EConfigUnit.BOOLEAN)
	private static boolean useTubeForMovingRobot = true;

	@Configurable(defValue = "STOPPING_OPPONENTS", comment = "Type of opponent obstacle for normal behavior", unit = EConfigUnit.NO_UNIT)
	private static EOpponentObstacleType opponentObstacleTypeNormal = EOpponentObstacleType.ACCELERATING_OPPONENTS;

	@Configurable(defValue = "BRAKING_OPPONENTS", comment = "Type of opponent obstacle for aggressive behavior", unit = EConfigUnit.NO_UNIT)
	private static EOpponentObstacleType opponentObstacleTypeAggressive = EOpponentObstacleType.BRAKING_OPPONENTS;

	private double additionalRobotRadius = 0;

	private final List<IObstacle> obsGoalPostOur = new ArrayList<>();
	private final List<IObstacle> obsGoalPostTheir = new ArrayList<>();

	static
	{
		ConfigRegistration.registerClass("sisyphus", ObstacleGenerator.class);
	}


	public ObstacleGenerator()
	{
		createGoalOur();
		createGoalTheir();
	}


	private void createGoalOur()
	{
		IVector2 gpl = Geometry.getGoalOur().getLeftPost();
		IVector2 gplb = gpl.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth(), 0));
		IVector2 gpr = Geometry.getGoalOur().getRightPost();
		IVector2 gprb = gpr.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth(), 0));
		IVector2 gplbw = gpl.addNew(Vector2.fromXY(-Geometry.getBoundaryWidthGoalLine(), 0));
		IVector2 gprbw = gpr.addNew(Vector2.fromXY(-Geometry.getBoundaryWidthGoalLine(), 0));
		obsGoalPostOur.add(new TubeObstacle("goalPostOurLeft", Tube.create(gpl, gplbw, Geometry.getBotRadius())));
		obsGoalPostOur.add(new TubeObstacle("goalPostOurRight", Tube.create(gpr, gprbw, Geometry.getBotRadius())));
		obsGoalPostOur.add(new TubeObstacle("goalPostOurBack", Tube.create(gplb, gprb, Geometry.getBotRadius())));
	}


	private void createGoalTheir()
	{
		IVector2 gpl = Geometry.getGoalTheir().getLeftPost();
		IVector2 gplb = gpl.addNew(Vector2.fromXY(Geometry.getGoalTheir().getDepth(), 0));
		IVector2 gpr = Geometry.getGoalTheir().getRightPost();
		IVector2 gprb = gpr.addNew(Vector2.fromXY(Geometry.getGoalTheir().getDepth(), 0));
		IVector2 gplbw = gpl.addNew(Vector2.fromXY(Geometry.getBoundaryWidthGoalLine(), 0));
		IVector2 gprbw = gpr.addNew(Vector2.fromXY(Geometry.getBoundaryWidthGoalLine(), 0));
		obsGoalPostTheir.add(new TubeObstacle("goalPostTheirLeft", Tube.create(gpl, gplbw, Geometry.getBotRadius())));
		obsGoalPostTheir.add(new TubeObstacle("goalPostTheirRight", Tube.create(gpr, gprbw, Geometry.getBotRadius())));
		obsGoalPostTheir.add(new TubeObstacle("goalPostTheirBack", Tube.create(gplb, gprb, Geometry.getBotRadius())));
	}


	private List<IObstacle> genOurBots(
			final WorldFrame wFrame,
			final BotID botId,
			final MovementCon moveCon,
			final PathFinderPrioMap prioMap
	)
	{
		return wFrame.getTigerBotsVisible().values().stream()
				.filter(tBot -> !tBot.getBotId().equals(botId))
				.filter(tBot -> !moveCon.getIgnoredBots().contains(tBot.getBotId()))
				.map(tBot -> selectObstacleForOurBot(wFrame, botId, tBot, moveCon, prioMap))
				.toList();
	}


	private IObstacle selectObstacleForOurBot(
			WorldFrame wFrame,
			BotID botId,
			ITrackedBot bot,
			MovementCon moveCon,
			PathFinderPrioMap prioMap
	)
	{
		double radius = 2 * Geometry.getBotRadius() + additionalRobotRadius;
		var trajectory = bot.getRobotInfo().getTrajectory();
		if (trajectory.isEmpty())
		{
			return brakingRobot(bot)
					.setOrderId(IObstacle.BOT_OUR_ORDER_ID);
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

		if (prioMap.isPreferred(botId, bot.getBotId()))
		{
			return new LimitedTimeObstacle(obs, ownBotTimeHorizonOnPrio);
		}
		return obs.setHasPriority(true);
	}


	private List<IObstacle> genTheirBots(WorldFrame wFrame, MovementCon moveCon)
	{
		return wFrame.getOpponentBotsVisible().values().stream()
				.filter(tBot -> !moveCon.getIgnoredBots().contains(tBot.getBotId()))
				.map(tBot -> opponentObstacle(tBot, moveCon))
				.map(o -> (IObstacle) new LimitedTimeObstacle(o, opponentBotTimeHorizon))
				.toList();
	}


	private IObstacle opponentObstacle(ITrackedBot tBot, MovementCon moveCon)
	{
		Map<EObstacleAvoidanceMode, EOpponentObstacleType> map = new EnumMap<>(EObstacleAvoidanceMode.class);
		map.put(EObstacleAvoidanceMode.NORMAL, opponentObstacleTypeNormal);
		map.put(EObstacleAvoidanceMode.SAFE, opponentObstacleTypeNormal);
		map.put(EObstacleAvoidanceMode.AGGRESSIVE, opponentObstacleTypeAggressive);

		EOpponentObstacleType type = map.get(moveCon.getObstacleAvoidanceMode());
		if (moveCon.getObstacleAvoidanceMode() == EObstacleAvoidanceMode.SAFE)
		{
			additionalRobotRadius = Geometry.getBotRadius();
		} else
		{
			additionalRobotRadius = 0;
		}

		AObstacle obstacle = switch (type)
		{
			case ACCELERATING_OPPONENTS -> movingRobotObstacle(tBot, acceleratingRobot(tBot));
			case STOPPING_OPPONENTS -> movingRobotObstacle(tBot, stoppingRobot(tBot));
			case BRAKING_OPPONENTS -> brakingRobot(tBot);
			case CONST_VEL_OPPONENTS -> constVelocityObstacle(tBot);
		};
		return obstacle
				.setOrderId(IObstacle.BOT_THEIR_ORDER_ID);
	}


	private AObstacle movingRobotObstacle(ITrackedBot tBot, IMovingRobot movingRobot)
	{
		if (useTubeForMovingRobot)
		{
			return new MovingRobotTubeObstacle(tBot.getBotId(), movingRobot, minVelocity, opponentBotMaxMovingTimeHorizon)
					.setMaxSpeed(tBot.getMoveConstraints().getVelMax());
		}
		return new MovingRobotObstacle(tBot.getBotId(), movingRobot, minVelocity, opponentBotMaxMovingTimeHorizon)
				.setMaxSpeed(tBot.getMoveConstraints().getVelMax());
	}


	private AObstacle constVelocityObstacle(ITrackedBot tBot)
	{
		return new ConstVelocityObstacle(
				tBot.getBotId(),
				tBot.getPos(),
				tBot.getVel(),
				2 * Geometry.getBotRadius(),
				opponentBotTimeHorizonForConstVel,
				minVelocity
		)
				.setMaxSpeed(tBot.getVel().getLength2());
	}


	private AObstacle brakingRobot(ITrackedBot tBot)
	{
		return new BrakingRobotObstacle(
				tBot.getBotId(),
				tBot.getPos(),
				tBot.getVel(),
				2 * Geometry.getBotRadius() + additionalRobotRadius,
				tBot.getMoveConstraints().getBrkMax(),
				minVelocity
		)
				.setMaxSpeed(tBot.getVel().getLength2());
	}


	private IMovingRobot acceleratingRobot(ITrackedBot tBot)
	{
		double speed = tBot.getVel().getLength2();
		return MovingRobotFactory.acceleratingRobot(
				MovingRobotParams.builder()
						.position(tBot.getPos())
						.velocity(speed < 0.5 ? Vector2.zero() : tBot.getVel())
						.vLimit(Math.max(tBot.getMoveConstraints().getVelMax(), speed))
						.aLimit(tBot.getMoveConstraints().getAccMax())
						.brkLimit(tBot.getMoveConstraints().getAccMax())
						.radius(2 * Geometry.getBotRadius() + additionalRobotRadius)
						.reactionTime(opponentBotReactionTime)
						.build()
		);
	}


	private IMovingRobot stoppingRobot(ITrackedBot tBot)
	{
		double speed = tBot.getVel().getLength2();
		return MovingRobotFactory.stoppingRobot(
				MovingRobotParams.builder()
						.position(tBot.getPos())
						.velocity(tBot.getVel())
						.vLimit(Math.max(tBot.getMoveConstraints().getVelMax(), speed))
						.aLimit(tBot.getMoveConstraints().getAccMax())
						.brkLimit(tBot.getMoveConstraints().getBrkMax())
						.radius(2 * Geometry.getBotRadius() + additionalRobotRadius)
						.reactionTime(opponentBotReactionTime)
						.build()
		);
	}


	/**
	 * Generate all required obstacles for the given world state, game state and movement constraints.
	 *
	 * @param wFrame    the current world frame
	 * @param forBotId  the id of the bot that the obstacle are for
	 * @param gameState the current game state
	 * @param moveCon   the movement constraints driving obstacle selection
	 * @param prioMap   the path finder priority map
	 * @return a list of the generated obstacles
	 */
	public List<IObstacle> generateObstacles(
			final WorldFrame wFrame,
			final BotID forBotId,
			final GameState gameState,
			final MovementCon moveCon,
			final PathFinderPrioMap prioMap
	)
	{
		List<IObstacle> obstacles = generateStaticObstacles(gameState, moveCon);

		if (moveCon.isOurBotsObstacle())
		{
			obstacles.addAll(genOurBots(wFrame, forBotId, moveCon, prioMap));
		}

		if (moveCon.isTheirBotsObstacle())
		{
			obstacles.addAll(genTheirBots(wFrame, moveCon));
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

		if (gameState.isBallPlacement())
		{
			var ballPos = wFrame.getBall().getPos();
			var placementPos = Optional.ofNullable(gameState.getBallPlacementPositionForUs()).orElse(ballPos);
			if (gameState.isBallPlacementForUs())
			{
				obs.add(createBallPlacementPassObstacle(ballPos, placementPos));
			} else
			{
				obs.add(
						new TubeObstacle(
								"placement",
								Tube.create(ballPos, placementPos, getEffectiveBotToBallDistanceOnStop())
						).setOrderId(IObstacle.BALL_ORDER_ID)
				);
			}
		} else if (gameState.isDistanceToBallRequired())
		{
			obs.add(
					new GenericCircleObstacle(
							"distanceToBall",
							Circle.createCircle(wFrame.getBall().getPos(), getEffectiveBotToBallDistanceOnStop())
					).setOrderId(IObstacle.BALL_ORDER_ID)
			);
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


	private List<IObstacle> generateStaticObstacles(GameState gameState, MovementCon moveCon)
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
		// Allow getting a bit closer than bot radius
		return Geometry.getBotRadius() / 2;
	}
}
