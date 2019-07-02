/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Generator for all the obstacles that are required during a regular match.
 */
public class ObstacleGenerator
{
	private boolean usePenAreaOur = true;
	private boolean usePenAreaTheir = false;
	private boolean useField = true;
	private boolean useGoalPosts = false;
	private boolean useTheirBots = true;
	private boolean useOurBots = true;
	private boolean useBall = true;
	private Set<BotID> ignoredBots = new HashSet<>();
	private Set<BotID> criticalFoeBots = new HashSet<>();
	
	@Configurable(defValue = "200.0")
	private static double defSecDistBall = 200;
	
	@Configurable(defValue = "1.0")
	private static double tHorz = 1;
	
	@Configurable(defValue = "0.5")
	private static double opponentBotTimeHorz = 0.5;
	
	@Configurable(defValue = "2.0", comment = "Assumed placement pass kick speed for obstacle")
	private static double placementKickSpeed = 2.0;
	
	
	private double secDistBall = defSecDistBall;
	
	private final transient List<IObstacle> obsGoalPostOur = new ArrayList<>();
	private final transient List<IObstacle> obsGoalPostTheir = new ArrayList<>();
	
	static
	{
		ConfigRegistration.registerClass("sisyphus", ObstacleGenerator.class);
	}
	
	
	/**
	 * Default
	 */
	public ObstacleGenerator()
	{
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
			final PathFinderPrioMap prioMap,
			final MovingRobot self)
	{
		List<IObstacle> obstacles = new ArrayList<>();
		for (ITrackedBot bot : wFrame.getTigerBotsVisible().values())
		{
			if (bot.getBotId().equals(botId) ||
					ignoredBots.contains(bot.getBotId()) ||
					!self.isPointInRobot(bot.getPos(), tHorz))
			{
				continue;
			}
			
			double radius = (2 * Geometry.getBotRadius()) - 10;
			
			final IObstacle botObs;
			
			if (!bot.getRobotInfo().getTrajectory().isPresent()
					|| prioMap.isEqual(botId, bot.getBotId()))
			{
				Tube tube = Tube.create(bot.getPos(), bot.getPosByTime(opponentBotTimeHorz), radius);
				botObs = new TubeObstacle(tube);
			} else if (!prioMap.isPreferred(botId, bot.getBotId()))
			{
				TrajectoryWithTime<IVector3> trajectoryWithTime = new TrajectoryWithTime<>(
						bot.getRobotInfo().getTrajectory().get(),
						bot.getTimestamp());
				botObs = new TrajAwareRobotObstacle(trajectoryWithTime,
						wFrame.getTimestamp(), radius);
			} else
			{
				ITrackedBot tBot = wFrame.getTiger(botId);
				double dist = tBot.getPos().distanceTo(bot.getPos());
				if (dist > 250)
				{
					radius = Geometry.getBotRadius();
				}
				botObs = new GenericCircleObstacle(Circle.createCircle(bot.getPos(), radius));
			}
			obstacles.add(botObs);
		}
		return obstacles;
	}
	
	
	/**
	 * Generate all required obstacles based on the current generator state and given input.
	 *
	 * @param wFrame the current world frame
	 * @param botId the id of the bot that the obstacle are for
	 * @param prioMap the prioMap to use
	 * @param gameState the current game state
	 * @return a list of the generated obstacles
	 */
	public List<IObstacle> generateObstacles(
			final WorldFrame wFrame,
			final BotID botId,
			final PathFinderPrioMap prioMap,
			final GameState gameState)
	{
		ITrackedBot tBot = wFrame.getBot(botId);
		MovingRobot self = new MovingRobot(tBot, tHorz, Geometry.getBotRadius());
		
		List<IObstacle> obstacles = generateStaticObstacles(self, gameState);
		
		if (useOurBots)
		{
			obstacles.addAll(genOurBots(wFrame, botId, prioMap, self));
		}
		
		if (useTheirBots)
		{
			for (ITrackedBot bot : wFrame.getFoeBots().values())
			{
				if (ignoredBots.contains(bot.getBotId()) ||
						!self.isPointInRobot(bot.getPos(), tHorz))
				{
					continue;
				}
				
				double radius = 2 * Geometry.getBotRadius();
				Tube tube = Tube.create(bot.getPos(), bot.getPosByTime(opponentBotTimeHorz), radius);
				TubeObstacle obstacle = new TubeObstacle(tube);
				if (criticalFoeBots.contains(bot.getBotId()))
				{
					obstacle.setCritical(true);
				}
				obstacles.add(obstacle);
			}
		}
		
		if (useBall && self.isPointInRobot(wFrame.getBall().getPos(), tHorz))
		{
			obstacles.add(new SimpleTimeAwareBallObstacle(wFrame.getBall(), secDistBall));
		}
		
		return obstacles;
	}
	
	
	private double getEffectiveBotToBallDistanceOnStop()
	{
		return RuleConstraints.getStopRadius() + Geometry.getBotRadius();
	}
	
	
	public List<IObstacle> generateGameStateObstacles(final WorldFrame wFrame,
			final GameState gameState)
	{
		List<IObstacle> obs = new ArrayList<>();
		
		if (gameState.isBallPlacement() || gameState.isDistanceToBallRequired())
		{
			IVector2 ballPos = wFrame.getBall().getPos();
			IVector2 placementPos = Optional.ofNullable(gameState.getBallPlacementPositionForUs()).orElse(ballPos);
			final SimpleTimeAwarePassObstacle obstacle = new SimpleTimeAwarePassObstacle(ballPos, placementKickSpeed,
					placementPos,
					getEffectiveBotToBallDistanceOnStop());
			// make the obstacle critical to avoid DefenderTooCloseToBall violations
			obstacle.setCritical(true);
			obs.add(obstacle);
		}
		return obs;
	}
	
	
	private List<IObstacle> generateStaticObstacles(final MovingRobot self, final GameState gameState)
	{
		List<IObstacle> obstacles = new ArrayList<>();
		
		if (useField)
		{
			IRectangle rect = Geometry.getFieldWBorders().withMargin(-Geometry.getBotRadius());
			obstacles.add(new FieldBorderObstacle(rect));
		}
		
		if (useGoalPosts)
		{
			if (self.isPointInRobot(Geometry.getGoalOur().getCenter(), tHorz))
			{
				obstacles.addAll(obsGoalPostOur);
			} else if (self.isPointInRobot(Geometry.getGoalTheir().getCenter(), tHorz))
			{
				obstacles.addAll(obsGoalPostTheir);
			}
		}
		if (usePenAreaOur)
		{
			double margin = Geometry.getBotRadius() + Geometry.getPenaltyAreaMargin();
			obstacles.add(new PenaltyAreaObstacle(Geometry.getPenaltyAreaOur().withMargin(margin)));
		}
		if (usePenAreaTheir)
		{
			double margin;
			if (gameState.isStandardSituation() || gameState.isStoppedGame())
			{
				margin = RuleConstraints.getBotToPenaltyAreaMarginStandard() + Geometry.getBotRadius();
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
		double minVel = 0;
		double maxVel = 3;
		double vel3 = Math.min(maxVel, Math.max(minVel, vel));
		double relMargin = (vel3 - minVel) / (maxVel - minVel);
		return relMargin * relMargin * 200;
	}
	
	
	/**
	 * @param usePenAreaOur the usePenAreaOur to set
	 */
	public final void setUsePenAreaOur(final boolean usePenAreaOur)
	{
		this.usePenAreaOur = usePenAreaOur;
	}
	
	
	/**
	 * @param usePenAreaTheir the usePenAreaTheir to set
	 */
	public final void setUsePenAreaTheir(final boolean usePenAreaTheir)
	{
		this.usePenAreaTheir = usePenAreaTheir;
	}
	
	
	/**
	 * @param useGoalPosts the useGoalPosts to set
	 */
	public final void setUseGoalPosts(final boolean useGoalPosts)
	{
		this.useGoalPosts = useGoalPosts;
	}
	
	
	/**
	 * @param useBots the useBots to set
	 */
	public final void setUseOurBots(final boolean useBots)
	{
		useOurBots = useBots;
	}
	
	
	/**
	 * @param useBots the useBots to set
	 */
	public final void setUseTheirBots(final boolean useBots)
	{
		useTheirBots = useBots;
	}
	
	
	/**
	 * @param useBall the useBall to set
	 */
	public final void setUseBall(final boolean useBall)
	{
		this.useBall = useBall;
	}
	
	
	/**
	 * @param secDistBall the secDistBall to set
	 */
	public final void setSecDistBall(final double secDistBall)
	{
		this.secDistBall = secDistBall;
	}
	
	
	public void setIgnoredBots(final Set<BotID> ignoredBots)
	{
		this.ignoredBots = ignoredBots;
	}
	
	
	public void setUseField(final boolean useField)
	{
		this.useField = useField;
	}
	
	
	public void setCriticalFoeBots(final Set<BotID> criticalFoeBots)
	{
		this.criticalFoeBots = criticalFoeBots;
	}
}
