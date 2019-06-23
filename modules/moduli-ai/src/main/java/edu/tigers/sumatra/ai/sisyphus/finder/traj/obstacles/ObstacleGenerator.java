/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.sisyphus.finder.traj.PathFinderPrioMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ObstacleGenerator
{
	private boolean									usePenAreaOur		= true;
	private boolean									usePenAreaTheir	= false;
	private boolean									useGoalPostsOur	= false;
	private boolean									useGoalPostsTheir	= false;
	private boolean									useTheirBots		= true;
	private boolean									useOurBots			= true;
	private boolean									useBall				= true;
	private boolean									useFieldBorders	= true;
	
	@Configurable()
	private static double							defSecDistBall		= 180;
	@Configurable()
	private static double							defSecDistBots		= 220;
	@Configurable()
	private static double							minSecDistBots		= 30;
	
	@Configurable
	private static double							tHorz					= 1;
	
	@Configurable
	private static double							tHorzBots			= 0.1;
	
	private double										secDistBall			= defSecDistBall;
	
	private transient final List<IObstacle>	obsPenAreaOur		= new ArrayList<>();
	private transient final List<IObstacle>	obsPenAreaTheir	= new ArrayList<>();
	private transient final List<IObstacle>	obsGoalPostOur		= new ArrayList<>();
	private transient final List<IObstacle>	obsGoalPostTheir	= new ArrayList<>();
	
	private static List<Color>						colorMap				= new ArrayList<>(12);
	
	
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
	 * 
	 */
	public ObstacleGenerator()
	{
		PenaltyArea penAreaOur = Geometry.getPenaltyAreaOur();
		PenaltyArea penAreaTheir = Geometry.getPenaltyAreaTheir();
		double radius = penAreaOur.getRadiusOfPenaltyArea() + Geometry.getPenaltyAreaMargin();
		
		
		{
			double maxX = penAreaOur.getGoalCenter().x() + 50;
			double maxY = penAreaOur.getLengthOfPenaltyAreaFrontLineHalf() + penAreaOur.getRadiusOfPenaltyArea()
					+ Geometry.getPenaltyAreaMargin();
			obsPenAreaOur.add(new RectangleObstacle(
					new Rectangle(new Vector2(maxX, maxY), new Vector2(maxX - 1000, -maxY))));
			
			obsPenAreaOur.add(getPenAreaArc(penAreaOur.getPenaltyCircleNegCentre(), radius));
			obsPenAreaOur.add(getPenAreaArc(penAreaOur.getPenaltyCirclePosCentre(), radius));
			obsPenAreaOur.add(new RectangleObstacle(new Rectangle(penAreaOur.getPenaltyCirclePosCentre(),
					penAreaOur.getPenaltyCircleNegCentre().addNew(new Vector2(radius, 0)))));
		}
		{
			double maxX = penAreaTheir.getGoalCenter().x() - 50;
			double maxY = -penAreaTheir.getLengthOfPenaltyAreaFrontLineHalf() - penAreaTheir.getRadiusOfPenaltyArea()
					- Geometry.getPenaltyAreaMargin();
			obsPenAreaTheir.add(new RectangleObstacle(
					new Rectangle(new Vector2(maxX, maxY), new Vector2(maxX + 1000, -maxY))));
			
			obsPenAreaTheir.add(getPenAreaArc(penAreaTheir.getPenaltyCircleNegCentre(), radius));
			obsPenAreaTheir.add(getPenAreaArc(penAreaTheir.getPenaltyCirclePosCentre(), radius));
			obsPenAreaTheir.add(new RectangleObstacle(new Rectangle(penAreaTheir.getPenaltyCirclePosCentre(),
					penAreaTheir.getPenaltyCircleNegCentre().addNew(new Vector2(-radius, 0)))));
		}
		{
			IVector2 gpl = Geometry.getGoalOur().getGoalPostLeft();
			IVector2 gplb = gpl.addNew(new Vector2(-Geometry.getGoalDepth(), 0));
			IVector2 gpr = Geometry.getGoalOur().getGoalPostRight();
			IVector2 gprb = gpr.addNew(new Vector2(-Geometry.getGoalDepth(), 0));
			obsGoalPostOur.add(new RectangleObstacle(Rectangle
					.aroundLine(gpl, gplb, Geometry.getBotRadius())));
			obsGoalPostOur.add(new RectangleObstacle(Rectangle
					.aroundLine(gpr, gprb, Geometry.getBotRadius())));
			obsGoalPostOur.add(new RectangleObstacle(Rectangle.aroundLine(gplb, gprb, Geometry
					.getBotRadius())));
		}
		{
			IVector2 gpl = Geometry.getGoalTheir().getGoalPostLeft();
			IVector2 gplb = gpl.addNew(new Vector2(Geometry.getGoalDepth(), 0));
			IVector2 gpr = Geometry.getGoalTheir().getGoalPostRight();
			IVector2 gprb = gpr.addNew(new Vector2(Geometry.getGoalDepth(), 0));
			obsGoalPostTheir.add(new RectangleObstacle(Rectangle.aroundLine(gpl, gplb, Geometry
					.getBotRadius())));
			obsGoalPostTheir.add(new RectangleObstacle(Rectangle.aroundLine(gpr, gprb, Geometry
					.getBotRadius())));
			obsGoalPostTheir.add(new RectangleObstacle(Rectangle.aroundLine(gplb, gprb, Geometry
					.getBotRadius())));
		}
	}
	
	
	private CircularObstacle getPenAreaArc(final IVector2 center, final double radius)
	{
		double startAngle = AVector2.X_AXIS.multiplyNew(-center.x()).getAngle();
		double stopAngle = AVector2.Y_AXIS.multiplyNew(center.y()).getAngle();
		double rotation = AngleMath.getShortestRotation(startAngle, stopAngle);
		return CircularObstacle.arcWithMargin(center, radius, startAngle, rotation, 100);
	}
	
	
	/**
	 * @param wFrame
	 * @param botId
	 * @param prioMap
	 * @return
	 */
	public List<IObstacle> generateObstacles(final WorldFrame wFrame, final BotID botId, final PathFinderPrioMap prioMap)
	{
		ITrackedBot tBot = wFrame.getBot(botId);
		IObstacle obs = getSelfInvertedObstacle(tBot);
		
		List<IObstacle> obstacles = generateStaticObstacles(obs);
		
		
		if (useOurBots)
		{
			for (ITrackedBot bot : wFrame.getTigerBotsVisible().values())
			{
				if (bot.getBotId().equals(botId))
				{
					continue;
				}
				if (!obs.isPointCollidingWithObstacle(bot.getPos(), tHorz))
				{
					continue;
				}
				
				double radius = (2 * Geometry.getBotRadius());
				
				double maxTrajTime = 1;
				if (prioMap.isPreferred(botId, bot.getBotId()))
				{
					// no future prediction - this bot ignores the path of the other bot, because the other bot will do the
					// avoiding
					maxTrajTime = -1;
				} else if (prioMap.isEqual(botId, bot.getBotId()))
				{
					maxTrajTime = 0.0;
				}
				
				IObstacle botObs;
				synchronized (bot.getBot())
				{
					if ((bot.getTeamColor() == tBot.getTeamColor()) &&
							bot.getBot().getCurrentTrajectory().isPresent())
					{
						botObs = new TrajAwareRobotObstacle(bot.getBot().getCurrentTrajectory().get(),
								wFrame.getTimestamp(), radius, maxTrajTime);
					} else
					{
						botObs = new SimpleTimeAwareRobotObstacle(bot, radius, 100, tHorzBots);
					}
				}
				obstacles.add(botObs);
			}
		}
		
		
		if (useTheirBots)
		{
			for (ITrackedBot bot : wFrame.getFoeBots().values())
			{
				if (!obs.isPointCollidingWithObstacle(bot.getPos(), tHorz))
				{
					continue;
				}
				
				double radius = (2 * Geometry.getBotRadius());
				
				double maxSimpleTime = tHorzBots;
				if (prioMap.isPreferred(botId, bot.getBotId()) || prioMap.isEqual(botId, bot.getBotId()))
				{
					// no future prediction - this bot ignores the path of the other bot, because the other bot will do the
					// avoiding
					maxSimpleTime = 0.0;
				}
				IObstacle botObs = new SimpleTimeAwareRobotObstacle(bot, radius, 100, maxSimpleTime);
				obstacles.add(botObs);
			}
		}
		
		if (useBall && (GeoMath.distancePP(wFrame.getBall().getPos(), tBot.getPos()) < 2000))
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
	
	
	/**
	 * @param wFrame
	 * @param botId
	 * @param refereeMsg
	 * @param gameState
	 * @return
	 */
	public List<IObstacle> generateObstacles(final WorldFrame wFrame, final BotID botId, final RefereeMsg refereeMsg,
			final EGameStateTeam gameState)
	{
		ITrackedBot tBot = wFrame.getBot(botId);
		List<IObstacle> obs = new ArrayList<>();
		switch (gameState)
		{
			case BALL_PLACEMENT_THEY:
			case CORNER_KICK_THEY:
			case DIRECT_KICK_THEY:
			case GOAL_KICK_THEY:
			case PREPARE_KICKOFF_THEY:
			case THROW_IN_THEY:
			case PREPARE_PENALTY_THEY:
			case STOPPED:
			case PREPARE_KICKOFF_WE:
				if (GeoMath.distancePP(wFrame.getBall().getPos(), tBot.getPos()) < 2000)
				{
					obs.add(CircularObstacle.circleWithMargin(wFrame.getBall().getPos(),
							Geometry.getBotToBallDistanceStop() + Geometry.getBotRadius() + Geometry.getBallRadius() + 10,
							50));
				}
				break;
			
			case BALL_PLACEMENT_WE:
			case BREAK:
			case CORNER_KICK_WE:
			case DIRECT_KICK_WE:
			case GOAL_KICK_WE:
			case HALTED:
			case POST_GAME:
			case PREPARE_PENALTY_WE:
			case RUNNING:
			case THROW_IN_WE:
			case TIMEOUT_THEY:
			case TIMEOUT_WE:
			case UNKNOWN:
				break;
			default:
				break;
		}
		return obs;
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	public static Color getColorForBotId(final BotID botId)
	{
		return colorMap.get(botId.getNumber());
	}
	
	
	/**
	 * @param tBot
	 * @return
	 */
	public IObstacle getSelfInvertedObstacle(final ITrackedBot tBot)
	{
		return new MovingRobotObstacle(tBot, tHorz, Geometry.getBotRadius());
	}
	
	
	/**
	 * @param obs
	 * @return
	 */
	public List<IObstacle> generateStaticObstacles(final IObstacle obs)
	{
		List<IObstacle> obstacles = new ArrayList<>();
		
		if (useFieldBorders)
		{
			double margin = Geometry.getBotRadius();
			Rectangle rect = new Rectangle(
					new Vector2((-Geometry.getFieldWBorders().getxExtend() / 2) - margin,
							(-Geometry.getFieldWBorders().getyExtend() / 2) - margin),
					new Vector2((Geometry.getFieldWBorders().getxExtend() / 2) + margin,
							(Geometry.getFieldWBorders().getyExtend() / 2) + margin));
			obstacles.add(new FieldBorderObstacle(rect));
		}
		if (useGoalPostsOur &&
				obs.isPointCollidingWithObstacle(Geometry.getGoalOur().getGoalCenter(), tHorz))
		{
			obstacles.addAll(obsGoalPostOur);
		}
		if (useGoalPostsTheir &&
				obs.isPointCollidingWithObstacle(Geometry.getGoalTheir().getGoalCenter(), tHorz))
		{
			obstacles.addAll(obsGoalPostTheir);
		}
		if (usePenAreaOur
		// && obs.isPointCollidingWithObstacle(Geometry.getPenaltyMarkOur(), tHorz)
		)
		{
			obstacles.addAll(obsPenAreaOur);
		}
		if (usePenAreaTheir
		// && obs.isPointCollidingWithObstacle(Geometry.getPenaltyMarkTheir(), tHorz)
		)
		{
			obstacles.addAll(obsPenAreaTheir);
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
		double extraMargin = relMargin * relMargin * 200;
		return extraMargin;
	}
	
	
	/**
	 * @return the usePenAreaOur
	 */
	public final boolean isUsePenAreaOur()
	{
		return usePenAreaOur;
	}
	
	
	/**
	 * @param usePenAreaOur the usePenAreaOur to set
	 */
	public final void setUsePenAreaOur(final boolean usePenAreaOur)
	{
		this.usePenAreaOur = usePenAreaOur;
	}
	
	
	/**
	 * @return the usePenAreaTheir
	 */
	public final boolean isUsePenAreaTheir()
	{
		return usePenAreaTheir;
	}
	
	
	/**
	 * @param usePenAreaTheir the usePenAreaTheir to set
	 */
	public final void setUsePenAreaTheir(final boolean usePenAreaTheir)
	{
		this.usePenAreaTheir = usePenAreaTheir;
	}
	
	
	/**
	 * @return the useGoalPosts
	 */
	public final boolean isUseGoalPostsOur()
	{
		return useGoalPostsOur;
	}
	
	
	/**
	 * @param useGoalPosts the useGoalPosts to set
	 */
	public final void setUseGoalPostsOur(final boolean useGoalPosts)
	{
		useGoalPostsOur = useGoalPosts;
	}
	
	
	/**
	 * @return the useBots
	 */
	public final boolean isUseTheirBots()
	{
		return useTheirBots;
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
	 * @return the useBall
	 */
	public final boolean isUseBall()
	{
		return useBall;
	}
	
	
	/**
	 * @param useBall the useBall to set
	 */
	public final void setUseBall(final boolean useBall)
	{
		this.useBall = useBall;
	}
	
	
	/**
	 * @return the useGoalPostsTheir
	 */
	public final boolean isUseGoalPostsTheir()
	{
		return useGoalPostsTheir;
	}
	
	
	/**
	 * @param useGoalPostsTheir the useGoalPostsTheir to set
	 */
	public final void setUseGoalPostsTheir(final boolean useGoalPostsTheir)
	{
		this.useGoalPostsTheir = useGoalPostsTheir;
	}
	
	
	/**
	 * @return the useFieldBorders
	 */
	public final boolean isUseFieldBorders()
	{
		return useFieldBorders;
	}
	
	
	/**
	 * @param useFieldBorders the useFieldBorders to set
	 */
	public final void setUseFieldBorders(final boolean useFieldBorders)
	{
		this.useFieldBorders = useFieldBorders;
	}
	
	
	/**
	 * @return the secDistBall
	 */
	public final double getSecDistBall()
	{
		return secDistBall;
	}
	
	
	/**
	 * @param secDistBall the secDistBall to set
	 */
	public final void setSecDistBall(final double secDistBall)
	{
		this.secDistBall = secDistBall;
	}
}
