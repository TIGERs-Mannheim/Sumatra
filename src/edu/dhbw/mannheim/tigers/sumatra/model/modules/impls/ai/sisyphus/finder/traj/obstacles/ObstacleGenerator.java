/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Arc;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictionInformation;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class ObstacleGenerator implements IDrawableShape
{
	private boolean									usePenAreaOur			= true;
	private boolean									usePenAreaTheir		= false;
	private boolean									useGoalPostsOur		= false;
	private boolean									useGoalPostsTheir		= false;
	private boolean									useBots					= true;
	private boolean									useBall					= true;
	private boolean									useSecondaryBall		= true;
	private boolean									useFieldBorders		= true;
	
	@Configurable()
	private static float								defSecDistBall			= 180;
	@Configurable()
	private static float								defSecDistBots			= 220;
	@Configurable()
	private static float								defMinDist2Obstacle	= 3000;
	
	private float										secDistBall				= defSecDistBall;
	private float										secDistBots				= defSecDistBots;
	private float										minDist2Obstacle		= defMinDist2Obstacle;
	
	private transient List<IObstacle>			obstacles				= new ArrayList<>();
	
	private transient final List<IObstacle>	obsPenAreaOur			= new ArrayList<>();
	private transient final List<IObstacle>	obsPenAreaTheir		= new ArrayList<>();
	private transient final List<IObstacle>	obsGoalPostOur			= new ArrayList<>();
	private transient final List<IObstacle>	obsGoalPostTheir		= new ArrayList<>();
	
	
	/**
	 * 
	 */
	public ObstacleGenerator()
	{
		PenaltyArea penAreaOur = AIConfig.getGeometry().getPenaltyAreaOur();
		PenaltyArea penAreaTheir = AIConfig.getGeometry().getPenaltyAreaTheir();
		float radius = penAreaOur.getRadiusOfPenaltyArea() + Geometry.getPenaltyAreaMargin();
		
		{
			float maxX = penAreaOur.getGoalCenter().x() + 50;
			float maxY = penAreaOur.getLengthOfPenaltyAreaFrontLineHalf() + penAreaOur.getRadiusOfPenaltyArea()
					+ Geometry.getPenaltyAreaMargin();
			obsPenAreaOur.add(new Rectangle(new Vector2(maxX, maxY), new Vector2(maxX - 1000, -maxY)));
			
			obsPenAreaOur.add(getPenAreaArc(penAreaOur.getPenaltyCircleNegCentre(), radius));
			obsPenAreaOur.add(getPenAreaArc(penAreaOur.getPenaltyCirclePosCentre(), radius));
			obsPenAreaOur.add(new Rectangle(penAreaOur.getPenaltyCirclePosCentre(),
					penAreaOur.getPenaltyCircleNegCentre().addNew(new Vector2(radius, 0))));
		}
		{
			obsPenAreaTheir.add(getPenAreaArc(penAreaTheir.getPenaltyCircleNegCentre(), radius));
			obsPenAreaTheir.add(getPenAreaArc(penAreaTheir.getPenaltyCirclePosCentre(), radius));
			obsPenAreaTheir.add(new Rectangle(penAreaTheir.getPenaltyCirclePosCentre(),
					penAreaTheir.getPenaltyCircleNegCentre().addNew(new Vector2(-radius, 0))));
		}
		{
			IVector2 gpl = AIConfig.getGeometry().getGoalOur().getGoalPostLeft();
			IVector2 gplb = gpl.addNew(new Vector2(-AIConfig.getGeometry().getGoalDepth(), 0));
			IVector2 gpr = AIConfig.getGeometry().getGoalOur().getGoalPostRight();
			IVector2 gprb = gpr.addNew(new Vector2(-AIConfig.getGeometry().getGoalDepth(), 0));
			obsGoalPostOur.add(Rectangle.aroundLine(gpl, gplb, AIConfig.getGeometry().getBotRadius()));
			obsGoalPostOur.add(Rectangle.aroundLine(gpr, gprb, AIConfig.getGeometry().getBotRadius()));
			obsGoalPostOur.add(Rectangle.aroundLine(gplb, gprb, AIConfig.getGeometry().getBotRadius()));
		}
		{
			IVector2 gpl = AIConfig.getGeometry().getGoalTheir().getGoalPostLeft();
			IVector2 gplb = gpl.addNew(new Vector2(AIConfig.getGeometry().getGoalDepth(), 0));
			IVector2 gpr = AIConfig.getGeometry().getGoalTheir().getGoalPostRight();
			IVector2 gprb = gpr.addNew(new Vector2(AIConfig.getGeometry().getGoalDepth(), 0));
			obsGoalPostTheir.add(Rectangle.aroundLine(gpl, gplb, AIConfig.getGeometry().getBotRadius()));
			obsGoalPostTheir.add(Rectangle.aroundLine(gpr, gprb, AIConfig.getGeometry().getBotRadius()));
			obsGoalPostTheir.add(Rectangle.aroundLine(gplb, gprb, AIConfig.getGeometry().getBotRadius()));
		}
	}
	
	
	private Arc getPenAreaArc(final IVector2 center, final float radius)
	{
		float startAngle = AVector2.X_AXIS.multiplyNew(-center.x()).getAngle();
		float stopAngle = AVector2.Y_AXIS.multiplyNew(center.y()).getAngle();
		float rotation = AngleMath.getShortestRotation(startAngle, stopAngle);
		return CircularObstacle.arcWithMargin(center, radius, startAngle, rotation, 20);
	}
	
	
	/**
	 * @param wFrame
	 * @param botId
	 * @return
	 */
	public List<IObstacle> generateObstacles(final WorldFrame wFrame, final BotID botId)
	{
		List<IObstacle> obstacles = generateStaticObstacles();
		
		if (useBots)
		{
			TrackedTigerBot tBot = wFrame.getBot(botId);
			for (TrackedTigerBot bot : wFrame.getBots().values())
			{
				if (bot.getId().equals(botId))
				{
					continue;
				}
				if (tBot != null)
				{
					float dist2Bot = GeoMath.distancePP(bot.getPos(), tBot.getPos());
					if (dist2Bot > minDist2Obstacle)
					{
						continue;
					}
				}
				
				FieldPredictionInformation predInfo = wFrame.getWorldFramePrediction().getBot(bot.getId());
				// if (bot.getBot().getPathFinder().getCurPath() != null)
				// {
				// obstacles.add(new TrajAwareRobotObstacle(bot.getBot().getPathFinder().getCurPath(), secDistBots));
				// } else
				if (predInfo != null)
				{
					obstacles.add(new SimpleTimeAwareRobotObstacle(predInfo, 200));
				} else
				{
					obstacles.add(CircularObstacle.circleWithMargin(bot.getPos(), secDistBots, 20));
				}
			}
		}
		
		if (useBall)
		{
			obstacles.add(new SimpleTimeAwareBallObstacle(wFrame.getBall(), secDistBall));
		}
		
		if (useSecondaryBall)
		{
			obstacles.add(new SimpleTimeAwareBallObstacle(wFrame.getBall(), AIConfig.getGeometry().getBotRadius() + 20));
		}
		
		this.obstacles = obstacles;
		return obstacles;
	}
	
	
	/**
	 * @return
	 */
	public List<IObstacle> generateStaticObstacles()
	{
		List<IObstacle> obstacles = new ArrayList<>();
		
		if (useFieldBorders)
		{
			obstacles.add(new FieldBorderObstacle(AIConfig.getGeometry().getFieldWBorders()));
		}
		if (useGoalPostsOur)
		{
			obstacles.addAll(obsGoalPostOur);
		}
		if (useGoalPostsTheir)
		{
			obstacles.addAll(obsGoalPostTheir);
		}
		if (usePenAreaOur)
		{
			obstacles.addAll(obsPenAreaOur);
		}
		if (usePenAreaTheir)
		{
			obstacles.addAll(obsPenAreaTheir);
		}
		return obstacles;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		for (IObstacle obs : obstacles)
		{
			obs.paintShape(g, fieldPanel, invert);
		}
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
	public final boolean isUseBots()
	{
		return useBots;
	}
	
	
	/**
	 * @param useBots the useBots to set
	 */
	public final void setUseBots(final boolean useBots)
	{
		this.useBots = useBots;
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
	public final float getSecDistBall()
	{
		return secDistBall;
	}
	
	
	/**
	 * @param secDistBall the secDistBall to set
	 */
	public final void setSecDistBall(final float secDistBall)
	{
		this.secDistBall = secDistBall;
	}
	
	
	/**
	 * @return the secDistBots
	 */
	public final float getSecDistBots()
	{
		return secDistBots;
	}
	
	
	/**
	 * @param secDistBots the secDistBots to set
	 */
	public final void setSecDistBots(final float secDistBots)
	{
		this.secDistBots = secDistBots;
	}
	
	
	/**
	 * @return the minDist2Obstacle
	 */
	public final float getMinDist2Obstacle()
	{
		return minDist2Obstacle;
	}
	
	
	/**
	 * @param minDist2Obstacle the minDist2Obstacle to set
	 */
	public final void setMinDist2Obstacle(final float minDist2Obstacle)
	{
		this.minDist2Obstacle = minDist2Obstacle;
	}
}
