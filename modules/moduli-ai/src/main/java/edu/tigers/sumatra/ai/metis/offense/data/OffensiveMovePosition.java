/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * 
 */
@Persistent
public class OffensiveMovePosition extends Vector2
{
	
	// private static final Logger log = Logger.getLogger(OffensiveMovePosition.class.getName());
	// private static final Map<BotID, TrajPathFinder> pathFinders = new HashMap<BotID, TrajPathFinder>();
	
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
		UNREACHABLE
	}
	
	private final ITrackedBot	bot;
	private double					scoring	= 0;
	private EOffensiveMoveType	type		= EOffensiveMoveType.NORMAL;
	
	
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
	public OffensiveMovePosition(final IVector2 position, final ITrackedBot bot, final EOffensiveMoveType type)
	{
		super(position);
		this.bot = bot;
		this.type = type;
	}
	
	
	/**
	 * @return the scoring
	 */
	public double getScoring()
	{
		return scoring;
	}
	
	
	/**
	 * @param scoring
	 */
	public void setScoring(final double scoring)
	{
		this.scoring = scoring;
	}
	
	
	/**
	 * @return the bot
	 */
	public ITrackedBot getBot()
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
		// List<IObstacle> obstacles = (new ObstacleGenerator()).generateObstacles(wFrame, bot.getBotId());
		//
		// TrajPathFinder finder;
		// if (!pathFinders.containsKey(bot.getBotId()))
		// {
		// finder = new TrajPathFinder(bot.getRobotInfo());
		// pathFinders.put(bot.getBotId(), finder);
		// } else
		// {
		// finder = pathFinders.get(bot.getBotId());
		// }
		
		// TrajPathFinderInput input = new TrajPathFinderInput();
		// input.setForcePathAfter(0);
		// input.setNumNodes2TryPerIteration(3);
		// input.setNumPoints2TryOnTraj(10);
		// input.setMaxSubPoints(1);
		// input.setTrajOffset(0.1f);
		// input.setTrackedBot(bot);
		// input.setDest(this);
		// input.setObstacles(obstacles);
		// TrajPath path = finder.calcPath(input);
		double time = 1; // placeholder until there is a real time path calculator
		double distanceToDestination = VectorMath.distancePP(this, bot.getPos());
		double distanceToBall = VectorMath.distancePP(wFrame.getBall().getPos(), this);
		scoring = (time * 8) + ((distanceToDestination / 2.0) + (distanceToBall / 2.0));
		
	}
}
