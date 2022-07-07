/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.IMovementCon;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Generator for all the obstacles that are required during a regular match.
 */
public class ObstacleGenerator
{
	@Configurable(defValue = "200.0")
	private static double defSecDistBall = 200;

	@Configurable(defValue = "1.0")
	private static double tHorizon = 1.0;

	@Configurable(defValue = "0.5")
	private static double opponentBotTimeHorizon = 0.5;

	@Configurable(defValue = "2.0", comment = "Assumed placement pass kick speed for obstacle")
	private static double placementKickSpeed = 2.0;

	@Configurable(defValue = "10.0", comment = "Extra margin on the opponent obstacle (note: there is also a dynamic margin applied by path planning)")
	private static double opponentExtraMargin = 10;

	@Configurable(defValue = "30.0", comment = "Extra margin on the opponent penArea during free kick")
	private static double opponentPenAreaStandardExtraMargin = 30;

	@Configurable(defValue = "0.7", comment = "Ignore opponent robots based on crash velocity (0.0: disabled, 1.5: sharp to the rules)")
	private static double aggressiveObstacleFilterVelocity = 0.7;

	@Configurable(defValue = "0.8", comment = "Ignore opponents based if their velocity is this amount higher")
	private static double aggressiveObstacleFilterVelocityDiff = 0.8;

	@Configurable(defValue = "false", comment = "Ignore opponent robots in some cases")
	private static boolean enableIgnoreOpponentBots = false;


	private IMovementCon moveCon;
	private double secDistBall = defSecDistBall;

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
		obsGoalPostOur.add(new TubeObstacle(Tube.create(gpl, gplb, Geometry.getBotRadius())));
		obsGoalPostOur.add(new TubeObstacle(Tube.create(gpr, gprb, Geometry.getBotRadius())));
		obsGoalPostOur.add(new TubeObstacle(Tube.create(gplb, gprb, Geometry.getBotRadius())));
	}


	private void createGoalPostTheir()
	{
		IVector2 gpl = Geometry.getGoalTheir().getLeftPost();
		IVector2 gplb = gpl.addNew(Vector2.fromXY(Geometry.getGoalTheir().getDepth(), 0));
		IVector2 gpr = Geometry.getGoalTheir().getRightPost();
		IVector2 gprb = gpr.addNew(Vector2.fromXY(Geometry.getGoalTheir().getDepth(), 0));
		obsGoalPostTheir.add(new TubeObstacle(Tube.create(gpl, gplb, Geometry.getBotRadius())));
		obsGoalPostTheir.add(new TubeObstacle(Tube.create(gpr, gprb, Geometry.getBotRadius())));
		obsGoalPostTheir.add(new TubeObstacle(Tube.create(gplb, gprb, Geometry.getBotRadius())));
	}


	private List<IObstacle> genOurBots(
			final WorldFrame wFrame,
			final BotID botId,
			final MovingRobot self)
	{
		List<IObstacle> obstacles = new ArrayList<>();
		for (ITrackedBot bot : wFrame.getTigerBotsVisible().values())
		{
			if (bot.getBotId().equals(botId) ||
					moveCon.getIgnoredBots().contains(bot.getBotId()) ||
					!self.isPointInRobot(bot.getPos(), tHorizon))
			{
				continue;
			}

			AObstacle botObs = selectObstacleType(wFrame, botId, bot);
			botObs.setActivelyEvade(true);
			obstacles.add(botObs);
		}
		return obstacles;
	}


	private AObstacle selectObstacleType(WorldFrame wFrame, BotID botId, ITrackedBot bot)
	{
		double radius = (2 * Geometry.getBotRadius()) - 10;
		var trajectory = bot.getRobotInfo().getTrajectory();
		if (trajectory.isEmpty() || moveCon.getPrioMap().isEqual(botId, bot.getBotId()))
		{
			var tube = Tube.create(bot.getPos(), bot.getPosByTime(opponentBotTimeHorizon), radius);
			return new TubeObstacle(tube);
		}

		if (!moveCon.getPrioMap().isPreferred(botId, bot.getBotId()))
		{
			var trajectoryWithTime = new TrajectoryWithTime<>(trajectory.get(), bot.getTimestamp());
			return new TrajAwareRobotObstacle(trajectoryWithTime, wFrame.getTimestamp(), radius);
		}

		ITrackedBot tBot = wFrame.getTiger(botId);
		double dist = tBot.getPos().distanceTo(bot.getPos());
		if (dist < 250)
		{
			// Robots are close. Although the other should drive around this bot, we should try avoiding the other as well
			return new GenericCircleObstacle(Circle.createCircle(bot.getPos(), Geometry.getBotRadius() * 2));
		}
		return new GenericCircleObstacle(Circle.createCircle(bot.getPos(), Geometry.getBotRadius()));
	}


	private List<IObstacle> genTheirBots(
			final WorldFrame wFrame,
			final ITrackedBot tBot,
			final MovingRobot self)
	{
		double radius = 2 * Geometry.getBotRadius() + opponentExtraMargin;
		final List<IObstacle> obstacles = new ArrayList<>();
		for (ITrackedBot opponentBot : wFrame.getOpponentBots().values())
		{
			IVector2 pos = opponentBot.getPos();
			if (moveCon.getIgnoredBots().contains(opponentBot.getBotId()) || // explicitly ignored robot
					(canOpponentBeIgnored(tBot, opponentBot) && enableIgnoreOpponentBots) ||
					!self.isPointInRobot(pos, tHorizon) // Optimization: Opponent bot is not close
			)
			{
				continue;
			}

			double adaptedRadius = Math.min(radius, opponentBot.getPos().distanceTo(self.getPos()) - 10);
			obstacles.add(new ConstVelocityObstacle(pos, opponentBot.getVel(), adaptedRadius, opponentBotTimeHorizon));
		}
		return obstacles;
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
		ITrackedBot tBot = wFrame.getBot(forBotId);
		var self = MovingRobot.fromTrackedBot(tBot, tHorizon, Geometry.getBotRadius());

		List<IObstacle> obstacles = generateStaticObstacles(self, gameState);

		if (moveCon.isOurBotsObstacle())
		{
			obstacles.addAll(genOurBots(wFrame, forBotId, self));
		}

		if (moveCon.isTheirBotsObstacle())
		{
			obstacles.addAll(genTheirBots(wFrame, tBot, self));
		}

		if (moveCon.isBallObstacle() && self.isPointInRobot(wFrame.getBall().getPos(), tHorizon))
		{
			obstacles.add(new SimpleTimeAwareBallObstacle(wFrame.getBall(), secDistBall));
		}

		if (moveCon.isGameStateObstacle())
		{
			obstacles.addAll(generateGameStateObstacles(wFrame, gameState));
		}

		obstacles.addAll(moveCon.getCustomObstacles());

		obstacles.sort(Comparator.comparingInt(IObstacle::getPriority).reversed());
		return obstacles;
	}


	/**
	 * Check if the opponent can be ignored based on the crash velocity.
	 * This is a rough estimation based on the current velocity, not the velocity at the crash.
	 * Idea: We do not want to be driven by the movement of the opponents too much. If they are getting in our
	 * way and we brake for them, they have an advantage and we may loose the ball, just because we were
	 * more careful then they, although they should have been more carefully in this situation.
	 *
	 * @param ownBot
	 * @param opponentBot
	 * @return
	 */
	private boolean canOpponentBeIgnored(final ITrackedBot ownBot, final ITrackedBot opponentBot)
	{
		if (opponentBot.getVel().getLength2() < 0.5)
		{
			return false;
		}
		boolean safeCrashVel = calcCrashVelocity(opponentBot, ownBot) < aggressiveObstacleFilterVelocity;
		double velDiff = opponentBot.getVel().getLength2() - ownBot.getVel().getLength2();
		boolean ownBotSignificantlySlower = velDiff > aggressiveObstacleFilterVelocityDiff;
		return safeCrashVel || ownBotSignificantlySlower;
	}


	private double predictBotVel(final ITrackedBot bot)
	{
		return bot.getVel().getLength2() - bot.getMoveConstraints().getAccMax() * 0.1;
	}


	/**
	 * Calculate the relative crash velocity between two robots as defined in the rules.
	 * If the result is greater than 1.5m/s, than either one or both teams have committed a foul.
	 *
	 * @param bot1
	 * @param bot2
	 * @return the crash velocity [m/s] (>=0)
	 */
	private double calcCrashVelocity(final ITrackedBot bot1, final ITrackedBot bot2)
	{
		IVector2 bot1Vel = bot1.getVel().scaleToNew(predictBotVel(bot1));
		IVector2 bot2Vel = bot2.getVel().scaleToNew(predictBotVel(bot2));
		IVector2 velDiff = bot1Vel.subtractNew(bot2Vel);
		IVector2 center = Lines.segmentFromPoints(bot1.getPos(), bot2.getPos()).getCenter();
		IVector2 crashVelReferencePoint = center.addNew(velDiff);
		ILine collisionReferenceLine = Lines.lineFromPoints(bot1.getPos(), bot2.getPos());
		IVector2 projectedCrashVelReferencePoint = collisionReferenceLine.closestPointOnLine(crashVelReferencePoint);

		return projectedCrashVelReferencePoint.distanceTo(center);
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
				var obstacle = new SimpleTimeAwarePassObstacle(
						ballPos,
						placementKickSpeed,
						placementPos,
						getEffectiveBotToBallDistanceOnStop()
				);
				// make the obstacle critical to avoid DefenderTooCloseToBall violations
				obstacle.setEmergencyBrakeFor(true);
				obstacle.setActivelyEvade(true);
				obs.add(obstacle);
			} else
			{
				obs.add(new TubeObstacle(Tube.create(ballPos, placementPos, getEffectiveBotToBallDistanceOnStop())));
			}
		}

		return obs;
	}


	private List<IObstacle> generateStaticObstacles(final MovingRobot self, final GameState gameState)
	{
		List<IObstacle> obstacles = new ArrayList<>();

		if (moveCon.isFieldBorderObstacle())
		{
			IRectangle rect = Geometry.getFieldWBorders().withMargin(-Geometry.getBotRadius());
			obstacles.add(new FieldBorderObstacle(rect));
		}

		if (moveCon.isGoalPostsObstacle())
		{
			if (self.isPointInRobot(Geometry.getGoalOur().getCenter(), tHorizon))
			{
				obstacles.addAll(obsGoalPostOur);
			} else if (self.isPointInRobot(Geometry.getGoalTheir().getCenter(), tHorizon))
			{
				obstacles.addAll(obsGoalPostTheir);
			}
		}
		if (moveCon.isPenaltyAreaOurObstacle())
		{
			obstacles.add(new PenaltyAreaObstacle(Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius())));
		}
		if (moveCon.isPenaltyAreaTheirObstacle())
		{
			double margin;
			if (gameState.isStandardSituation() || gameState.isStoppedGame())
			{
				margin = RuleConstraints.getPenAreaMarginStandard() + Geometry.getBotRadius() + opponentPenAreaStandardExtraMargin;
			} else
			{
				margin = 0.0;
			}
			obstacles.add(new PenaltyAreaObstacle(Geometry.getPenaltyAreaTheir().withMargin(margin)));
		}
		return obstacles;
	}


	/**
	 * Get the extra margin based on the current velocity
	 *
	 * @param vel current velocity
	 * @return extra margin
	 */
	public static double getExtraMargin(final double vel)
	{
		double maxVel = 3;
		double vel3 = Math.min(maxVel, vel);
		double relMargin = vel3 / maxVel;
		return relMargin * relMargin * 200;
	}
}
