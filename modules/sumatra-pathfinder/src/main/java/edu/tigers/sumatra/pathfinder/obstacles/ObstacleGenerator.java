/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ObstacleGenerator
{
	private boolean usePenAreaOur = true;
	private boolean usePenAreaTheir = false;
	private boolean useGoalPostsOur = false;
	private boolean useGoalPostsTheir = false;
	private boolean useTheirBots = true;
	private boolean useOurBots = true;
	private boolean useBall = true;
	private boolean useFieldBorders = true;
	private Set<BotID> ignoredBots = new HashSet<>();
	
	@Configurable(defValue = "200.0")
	private static double defSecDistBall = 200;
	
	@Configurable(defValue = "1.0")
	private static double tHorz = 1;
	
	@Configurable(defValue = "0.5")
	private static double opponentBotTimeHorz = 0.5;
	
	
	private double secDistBall = defSecDistBall;
	
	private final transient List<IObstacle> obsGoalPostOur = new ArrayList<>();
	private final transient List<IObstacle> obsGoalPostTheir = new ArrayList<>();
	
	private static List<Color> colorMap = new ArrayList<>(12);
	
	static
	{
		ConfigRegistration.registerClass("sisyphus", ObstacleGenerator.class);
		
		colorMap.add(Color.LIGHT_GRAY);
		colorMap.add(Color.GRAY);
		colorMap.add(Color.DARK_GRAY);
		colorMap.add(Color.BLACK);
		colorMap.add(Color.YELLOW);
		colorMap.add(Color.MAGENTA);
		colorMap.add(Color.CYAN);
		colorMap.add(Color.ORANGE);
		colorMap.add(Color.GREEN);
		colorMap.add(Color.PINK);
		colorMap.add(Color.BLUE);
		colorMap.add(Color.RED);
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
		obsGoalPostOur.add(new RectangleObstacle(Rectangle
				.aroundLine(gpl, gplb, Geometry.getBotRadius())));
		obsGoalPostOur.add(new RectangleObstacle(Rectangle
				.aroundLine(gpr, gprb, Geometry.getBotRadius())));
		obsGoalPostOur.add(new RectangleObstacle(Rectangle.aroundLine(gplb, gprb, Geometry
				.getBotRadius())));
	}
	
	
	private void createGoalPostTheir()
	{
		IVector2 gpl = Geometry.getGoalTheir().getLeftPost();
		IVector2 gplb = gpl.addNew(Vector2.fromXY(Geometry.getGoalOur().getDepth(), 0));
		IVector2 gpr = Geometry.getGoalTheir().getRightPost();
		IVector2 gprb = gpr.addNew(Vector2.fromXY(Geometry.getGoalOur().getDepth(), 0));
		obsGoalPostTheir.add(new RectangleObstacle(Rectangle.aroundLine(gpl, gplb, Geometry
				.getBotRadius())));
		obsGoalPostTheir.add(new RectangleObstacle(Rectangle.aroundLine(gpr, gprb, Geometry
				.getBotRadius())));
		obsGoalPostTheir.add(new RectangleObstacle(Rectangle.aroundLine(gplb, gprb, Geometry
				.getBotRadius())));
	}
	
	
	private List<IObstacle> genOurBots(final WorldFrame wFrame, final BotID botId, final PathFinderPrioMap prioMap,
			final IObstacle obs)
	{
		List<IObstacle> obstacles = new ArrayList<>();
		for (ITrackedBot bot : wFrame.getTigerBotsVisible().values())
		{
			if (bot.getBotId().equals(botId) ||
					ignoredBots.contains(bot.getBotId()) ||
					!obs.isPointCollidingWithObstacle(bot.getPos(), tHorz))
			{
				continue;
			}
			
			double radius = 2 * Geometry.getBotRadius();
			
			final IObstacle botObs;
			
			if (!bot.getRobotInfo().getTrajectory().isPresent()
					|| prioMap.isEqual(botId, bot.getBotId()))
			{
				botObs = new SimpleTimeAwareRobotObstacle(bot, radius);
			} else if (!prioMap.isPreferred(botId, bot.getBotId()))
			{
				TrajectoryWithTime<IVector3> trajectoryWithTime = new TrajectoryWithTime<>(
						bot.getRobotInfo().getTrajectory().get(),
						bot.getTimestamp());
				botObs = new TrajAwareRobotObstacle(trajectoryWithTime,
						wFrame.getTimestamp(), radius);
			} else
			{
				ITrackedBot self = wFrame.getTiger(botId);
				double dist = self.getPos().distanceTo(bot.getPos());
				if (dist > 250)
				{
					radius = Geometry.getBotRadius();
				}
				botObs = new CircleObstacle(Circle.createCircle(bot.getPos(), radius));
			}
			obstacles.add(botObs);
		}
		return obstacles;
	}
	
	
	/**
	 * @param wFrame
	 * @param botId
	 * @param prioMap
	 * @param gameState
	 * @return
	 */
	public List<IObstacle> generateObstacles(final WorldFrame wFrame,
			final BotID botId,
			final PathFinderPrioMap prioMap,
			final GameState gameState)
	{
		ITrackedBot tBot = wFrame.getBot(botId);
		IObstacle obs = getSelfInvertedObstacle(tBot);
		
		List<IObstacle> obstacles = generateStaticObstacles(obs, gameState);
		
		
		if (useOurBots)
		{
			obstacles.addAll(genOurBots(wFrame, botId, prioMap, obs));
		}
		
		
		if (useTheirBots)
		{
			for (ITrackedBot bot : wFrame.getFoeBots().values())
			{
				if (ignoredBots.contains(bot.getBotId()) ||
						!obs.isPointCollidingWithObstacle(bot.getPos(), tHorz))
				{
					continue;
				}
				
				double radius = 2 * Geometry.getBotRadius();
				Tube tube = Tube.create(bot.getPos(), bot.getPosByTime(opponentBotTimeHorz), radius);
				obstacles.add(new TubeObstacle(tube));
			}
		}
		
		if (useBall && obs.isPointCollidingWithObstacle(wFrame.getBall().getPos(), tHorz))
		{
			obstacles.add(new SimpleTimeAwareBallObstacle(wFrame.getBall(), secDistBall));
		}
		
		Color color = getColorForBotId(botId);
		for (IObstacle o : obstacles)
		{
			o.setColor(color);
		}
		
		return obstacles;
	}
	
	
	private double getEffectiveBotToBallDistanceOnStop()
	{
		return RuleConstraints.getStopRadius() + Geometry.getBotRadius();
	}
	
	
	/**
	 * @param wFrame
	 * @param gameState
	 * @return
	 */
	public List<IObstacle> generateGameStateObstacles(final WorldFrame wFrame,
			final GameState gameState)
	{
		List<IObstacle> obs = new ArrayList<>();
		
		if (gameState.getState() == EGameState.BALL_PLACEMENT
				&& placementBotNearBall(wFrame))
		{
			IVector2 placementPos = gameState.getBallPlacementPositionForUs();
			obs.add(new LineObstacle(Line.fromPoints(wFrame.getBall().getPos(), placementPos),
					getEffectiveBotToBallDistanceOnStop()));
		} else if (gameState.isDistanceToBallRequired())
		{
			obs.add(new CircleObstacle(Circle.createCircle(wFrame.getBall().getPos(),
					getEffectiveBotToBallDistanceOnStop())));
		}
		return obs;
	}
	
	
	private boolean placementBotNearBall(final WorldFrame wFrame)
	{
		return wFrame.getBots().values().stream()
				.map(ITrackedBot::getPos)
				.map(p -> p.distanceToSqr(wFrame.getBall().getPos()))
				.sorted()
				.findFirst()
				.map(Math::sqrt)
				.map(d -> d < 200)
				.orElse(false);
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	private static Color getColorForBotId(final BotID botId)
	{
		return colorMap.get(botId.getNumber());
	}
	
	
	/**
	 * @param tBot
	 * @return
	 */
	private IObstacle getSelfInvertedObstacle(final ITrackedBot tBot)
	{
		return new MovingRobotObstacle(tBot, tHorz, Geometry.getBotRadius());
	}
	
	
	/**
	 * @param obs
	 * @return
	 */
	private List<IObstacle> generateStaticObstacles(final IObstacle obs, final GameState gameState)
	{
		List<IObstacle> obstacles = new ArrayList<>();
		
		if (useFieldBorders)
		{
			double margin = Geometry.getBotRadius();
			IRectangle rect = Geometry.getFieldWBorders().withMargin(-margin);
			obstacles.add(new FieldBorderObstacle(rect));
		}
		if (useGoalPostsOur &&
				obs.isPointCollidingWithObstacle(Geometry.getGoalOur().getCenter(), tHorz))
		{
			obstacles.addAll(obsGoalPostOur);
		}
		if (useGoalPostsTheir &&
				obs.isPointCollidingWithObstacle(Geometry.getGoalTheir().getCenter(), tHorz))
		{
			obstacles.addAll(obsGoalPostTheir);
		}
		if (usePenAreaOur)
		{
			double margin = Geometry.getPenaltyAreaMargin();
			obstacles.add(new PenaltyAreaObstacle(Geometry.getPenaltyAreaOur().withMargin(margin)));
		}
		if (usePenAreaTheir)
		{
			double margin;
			if (gameState.isStandardSituation())
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
	 * @param vel
	 * @return
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
	public final void setUseGoalPostsOur(final boolean useGoalPosts)
	{
		useGoalPostsOur = useGoalPosts;
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
	 * @param useBots the useBots to set
	 */
	public final void setUseBots(final boolean useBots)
	{
		useTheirBots = useBots;
		useOurBots = useBots;
	}
	
	
	/**
	 * @param useBall the useBall to set
	 */
	public final void setUseBall(final boolean useBall)
	{
		this.useBall = useBall;
	}
	
	
	/**
	 * @param useGoalPostsTheir the useGoalPostsTheir to set
	 */
	public final void setUseGoalPostsTheir(final boolean useGoalPostsTheir)
	{
		this.useGoalPostsTheir = useGoalPostsTheir;
	}
	
	
	/**
	 * @param useFieldBorders the useFieldBorders to set
	 */
	public final void setUseFieldBorders(final boolean useFieldBorders)
	{
		this.useFieldBorders = useFieldBorders;
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
}
