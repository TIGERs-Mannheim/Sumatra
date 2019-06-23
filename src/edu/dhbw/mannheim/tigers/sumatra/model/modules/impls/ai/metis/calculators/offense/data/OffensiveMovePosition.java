/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.12.2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;


/**
 * 
 */
@Persistent
public class OffensiveMovePosition extends Vector2
{
	
	// private static final Logger log = Logger.getLogger(OffensiveMovePosition.class.getName());
	private static final Map<BotID, TrajPathFinder>	pathFinders	= new HashMap<BotID, TrajPathFinder>();
	
	/**
	 * @author MarkG
	 */
	public enum EOffensiveMoveType
	{
		/*** */
		NORMAL,
		/*** */
		IGNORE_BALL,
		/*** */
		UNREACHABLE;
	}
	
	private final TrackedTigerBot	bot;
	private float						scoring	= 0;
	private EOffensiveMoveType		type		= EOffensiveMoveType.NORMAL;
	
	
	/**
	  * 
	  */
	@SuppressWarnings("unused")
	private OffensiveMovePosition()
	{
		bot = null;
	}
	
	
	/**
	 * @param position
	 * @param bot
	 * @param type
	 */
	public OffensiveMovePosition(final IVector2 position, final TrackedTigerBot bot, final EOffensiveMoveType type)
	{
		super(position);
		this.bot = bot;
		this.type = type;
	}
	
	
	/**
	 * @return the scoring
	 */
	public float getScoring()
	{
		return scoring;
	}
	
	
	/**
	 * @param scoring
	 */
	public void setScoring(final float scoring)
	{
		this.scoring = scoring;
	}
	
	
	/**
	 * @return the bot
	 */
	public TrackedTigerBot getBot()
	{
		return bot;
	}
	
	
	/**
	 * @return the type
	 */
	public EOffensiveMoveType getType()
	{
		return type;
	}
	
	
	/**
	 * small scorings are good
	 * 
	 * @param wFrame
	 * @param tacticalField
	 */
	public void generateScoring(final WorldFrame wFrame, final TacticalField tacticalField)
	{
		// TrajectoryGenerator.generatePositionTrajectory(bot, this).getTotalTime();
		List<IObstacle> obstacles = (new ObstacleGenerator()).generateObstacles(wFrame, bot.getId());
		
		TrajPathFinder finder;
		if (!pathFinders.containsKey(bot.getId()))
		{
			finder = new TrajPathFinder(bot.getBot());
			pathFinders.put(bot.getId(), finder);
		} else
		{
			finder = pathFinders.get(bot.getId());
		}
		
		TrajPathFinderInput input = new TrajPathFinderInput();
		input.setForcePathAfter(0);
		input.setNumNodes2TryPerIteration(3);
		input.setNumPoints2TryOnTraj(10);
		input.setMaxSubPoints(1);
		input.setTrajOffset(0.1f);
		input.setTrackedBot(bot);
		input.setDest(this);
		input.setObstacles(obstacles);
		TrajPath path = finder.calcPath(input);
		
		float time = path.getTotalTime();
		float distanceToDestination = GeoMath.distancePP(this, bot.getPos());
		float distanceToBall = GeoMath.distancePP(wFrame.getBall().getPos(), this);
		scoring = (time * 8) + ((distanceToDestination / 2.0f) + (distanceToBall / 2.0f));
		
	}
}
