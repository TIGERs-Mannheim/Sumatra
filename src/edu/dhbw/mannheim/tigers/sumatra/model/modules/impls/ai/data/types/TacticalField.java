/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis;


/**
 * This class should be used to store and combine all tactical information
 * calculated by {@link Metis} and its calculators.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class TacticalField implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long									serialVersionUID		= -2739518925641905536L;
	
	
	private final WorldFrame									worldFrame;
	
	// defense
	// TODO Oliver, find better name
	private final List<ValuePoint>							defGoalPoints;
	
	// debug
	private final List<IVector2>								debugPoints;
	
	// offense
	// TODO Oliver, some description for java doc?
	private final List<ValuePoint>							offCarrierPoints;
	private final List<ValuePoint>							offLeftReceiverPoints;
	private final List<ValuePoint>							offRightReceiverPoints;
	
	private final LinkedHashMap<Integer, BotDistance>	tigersToBallDist;
	private BotDistance											tigerClosestToBall	= BotDistance.NULL_BOT_DISTANCE;
	private final LinkedHashMap<Integer, BotDistance>	enemiesToBallDist;
	private BotDistance											enemyClosestToBall	= BotDistance.NULL_BOT_DISTANCE;
	
	/** Which bot (opponent and Tigers) has the ball? */
	private BallPossession										ballPossession;
	
	/** we can shoot on the goal immediately */
	private boolean												tigersScoringChance;
	/** our opponents can shoot on the goal immediately */
	private boolean												opponentScoringChance;
	/** we can shoot on the goal after getting and/or aiming the ball */
	private boolean												tigersApproximateScoringChance;
	/** our opponents can shoot on the goal after getting and/or aiming the ball */
	private boolean												opponentApproximateScoringChance;
	
	private boolean												ballInOurPenArea;
	
	private ETeam													teamClosestToBall;
	private List<TrackedBot>									dangerousOpponents;
	private TrackedBot											opponentBallGetter;
	private TrackedBot											opponentPassReceiver;
	private TrackedBot											opponentKeeper;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public TacticalField(WorldFrame worldFrame)
	{
		this.worldFrame = worldFrame;
		
		defGoalPoints = new ArrayList<ValuePoint>();
		debugPoints = new ArrayList<IVector2>();
		
		offCarrierPoints = new ArrayList<ValuePoint>();
		offLeftReceiverPoints = new ArrayList<ValuePoint>();
		offRightReceiverPoints = new ArrayList<ValuePoint>();
		
		ballPossession = new BallPossession();
		
		tigersToBallDist = new LinkedHashMap<Integer, BotDistance>();
		enemiesToBallDist = new LinkedHashMap<Integer, BotDistance>();
		
		tigersScoringChance = false;
		opponentScoringChance = false;
		tigersApproximateScoringChance = false;
		opponentApproximateScoringChance = false;
		teamClosestToBall = ETeam.UNKNOWN;
		ballInOurPenArea = false;
		dangerousOpponents = new ArrayList<TrackedBot>();
		opponentBallGetter = null;
		opponentPassReceiver = null;
		opponentKeeper = null;
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values
	 * @param original
	 */
	public TacticalField(TacticalField original)
	{
		this.worldFrame = original.worldFrame;
		
		defGoalPoints = new ArrayList<ValuePoint>(original.defGoalPoints);
		debugPoints = new ArrayList<IVector2>(original.getDebugPoints());
		
		offCarrierPoints = new ArrayList<ValuePoint>(original.offCarrierPoints);
		offLeftReceiverPoints = new ArrayList<ValuePoint>(original.offLeftReceiverPoints);
		offRightReceiverPoints = new ArrayList<ValuePoint>(original.offRightReceiverPoints);
		
		ballPossession = original.ballPossession;
		
		tigersToBallDist = new LinkedHashMap<Integer, BotDistance>(original.tigersToBallDist);
		tigerClosestToBall = original.tigerClosestToBall;
		enemiesToBallDist = new LinkedHashMap<Integer, BotDistance>(original.enemiesToBallDist);
		enemyClosestToBall = original.enemyClosestToBall;
		
		tigersScoringChance = original.tigersScoringChance;
		opponentScoringChance = original.opponentScoringChance;
		tigersApproximateScoringChance = original.tigersApproximateScoringChance;
		opponentApproximateScoringChance = original.opponentApproximateScoringChance;
		teamClosestToBall = original.teamClosestToBall;
		ballInOurPenArea = original.ballInOurPenArea;
		dangerousOpponents = new ArrayList<TrackedBot>(original.dangerousOpponents);
		opponentBallGetter = original.opponentBallGetter;
		opponentPassReceiver = original.opponentPassReceiver;
		opponentKeeper = original.opponentKeeper;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * Calculates the number of enemies on our subfield.
	 * 
	 * @return
	 */
	public int getEnemiesOnOwnSubfield()
	{
		int counter = 0;
		TrackedBot bot;
		
		for (Entry<Integer, TrackedBot> entry : worldFrame.foeBots.entrySet())
		{
			bot = entry.getValue();
			
			if (bot.pos.x() <= 0)
			{
				counter++;
			}
		}
		return counter;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	public List<ValuePoint> getDefGoalPoints()
	{
		return defGoalPoints;
	}
	
	
	public List<IVector2> getDebugPoints()
	{
		return debugPoints;
	}
	
	
	public void setDefGoalPoints(List<ValuePoint> newDefGoalPoints)
	{
		defGoalPoints.clear();
		defGoalPoints.addAll(newDefGoalPoints);
	}
	
	
	public List<ValuePoint> getOffLeftReceiverPoints()
	{
		return offLeftReceiverPoints;
	}
	
	
	public void setOffLeftReceiverPoints(List<ValuePoint> newOffLeftReceiverPoints)
	{
		offLeftReceiverPoints.clear();
		offLeftReceiverPoints.addAll(newOffLeftReceiverPoints);
	}
	
	
	public List<ValuePoint> getOffRightReceiverPoints()
	{
		return offRightReceiverPoints;
	}
	
	
	public void setOffRightReceiverPoints(List<ValuePoint> newOffRightReceiverPoints)
	{
		offRightReceiverPoints.clear();
		offRightReceiverPoints.addAll(newOffRightReceiverPoints);
	}
	
	
	public List<ValuePoint> getOffCarrierPoints()
	{
		return offCarrierPoints;
	}
	
	
	public void setOffCarrierPoints(List<ValuePoint> newOffCarrierPoints)
	{
		offCarrierPoints.clear();
		offCarrierPoints.addAll(newOffCarrierPoints);
	}
	
	
	public BallPossession getBallPossesion()
	{
		return ballPossession;
	}
	
	
	public void setBallPossesion(BallPossession newBallPossesion)
	{
		if (newBallPossesion == null)
		{
			ballPossession = new BallPossession();
		} else
		{
			ballPossession = newBallPossesion;
		}
	}
	
	
	public boolean getTigersScoringChance()
	{
		return tigersScoringChance;
	}
	
	
	public void setTigersScoringChance(boolean tigersScoringChance)
	{
		this.tigersScoringChance = tigersScoringChance;
	}
	
	
	public boolean getOpponentScoringChance()
	{
		return opponentScoringChance;
	}
	
	
	public void setOpponentScoringChance(boolean opponentScoringChance)
	{
		this.opponentScoringChance = opponentScoringChance;
	}
	
	
	public boolean getTigersApproximateScoringChance()
	{
		return tigersApproximateScoringChance;
	}
	
	
	public void setTigersApproximateScoringChance(boolean tigersApproximateScoringChance)
	{
		this.tigersApproximateScoringChance = tigersApproximateScoringChance;
	}
	
	
	public boolean getOpponentApproximateScoringChance()
	{
		return opponentApproximateScoringChance;
	}
	
	
	public void setOpponentApproximateScoringChance(boolean opponentApproximateScoringChance)
	{
		this.opponentApproximateScoringChance = opponentApproximateScoringChance;
	}
	
	
	public void setTigersToBallDist(List<BotDistance> newTigersToBallDist)
	{
		this.tigersToBallDist.clear();
		for (BotDistance bDist : newTigersToBallDist)
		{
			this.tigersToBallDist.put(bDist.bot.id, bDist);
		}
	}
	
	
	/**
	 * @return A {@link LinkedHashMap} with all Tiger bots sorted by their distance to the ball (
	 *         {@link BotDistance#ASCENDING}).
	 */
	public LinkedHashMap<Integer, BotDistance> getTigersToBallDist()
	{
		return tigersToBallDist;
	}
	
	
	/**
	 * Implemented lazy. If {@link #tigersToBallDist} is empty (tests!?) a {@link NoSuchElementException} would be
	 * thrown.
	 * 
	 * @return The {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are no
	 *         Tiger bots!!!)
	 */
	public BotDistance getTigerClosestToBall()
	{
		if (tigerClosestToBall == BotDistance.NULL_BOT_DISTANCE && tigersToBallDist.values().size() > 0)
		{
			tigerClosestToBall = tigersToBallDist.values().iterator().next();
		}
		return tigerClosestToBall;
	}
	
	
	public void setEnemiesToBallDist(List<BotDistance> newEnemiesToBallDist)
	{
		this.enemiesToBallDist.clear();
		for (BotDistance bDist : newEnemiesToBallDist)
		{
			this.enemiesToBallDist.put(bDist.bot.id, bDist);
		}
	}
	
	
	/**
	 * @return A {@link LinkedHashMap} with all enemy bots sorted by their distance to the ball (
	 *         {@link BotDistance#ASCENDING}).
	 */
	public LinkedHashMap<Integer, BotDistance> getEnemiesToBallDist()
	{
		return enemiesToBallDist;
	}
	
	
	/**
	 * Implemented lazy. If {@link #enemiesToBallDist} is empty (tests!?) a {@link NoSuchElementException} would be
	 * thrown.
	 * 
	 * @return The enemy {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are
	 *         no
	 *         enemy bots!!!)
	 */
	public BotDistance getEnemyClosestToBall()
	{
		if (enemyClosestToBall == BotDistance.NULL_BOT_DISTANCE && enemiesToBallDist.values().size() > 0)
		{
			enemyClosestToBall = enemiesToBallDist.values().iterator().next();
		}
		return enemyClosestToBall;
	}
	
	
	public ETeam getTeamClosestToBall()
	{
		return teamClosestToBall;
	}
	
	
	public void setTeamClosestToBall(ETeam newTeamClosestToBall)
	{
		if (newTeamClosestToBall == null)
		{
			teamClosestToBall = ETeam.UNKNOWN;
		} else
		{
			teamClosestToBall = newTeamClosestToBall;
		}
	}
	
	
	public boolean isBallInOurPenArea()
	{
		return ballInOurPenArea;
	}
	
	
	public void setBallInOurPenArea(boolean ballInOurPenArea)
	{
		this.ballInOurPenArea = ballInOurPenArea;
	}
	
	
	public List<TrackedBot> getDangerousOpponents()
	{
		return dangerousOpponents;
	}
	
	
	public void setDangerousOpponents(List<TrackedBot> newDangerousOpponents)
	{
		this.dangerousOpponents.clear();
		this.dangerousOpponents.addAll(newDangerousOpponents);
	}
	
	
	public TrackedBot getOpponentBallGetter()
	{
		return opponentBallGetter;
	}
	
	
	public void setOpponentBallGetter(TrackedBot opponentBallGetter)
	{
		this.opponentBallGetter = opponentBallGetter;
	}
	
	
	public TrackedBot getOpponentPassReceiver()
	{
		return opponentPassReceiver;
	}
	
	
	public void setOpponentPassReceiver(TrackedBot opponentPassReceiver)
	{
		this.opponentPassReceiver = opponentPassReceiver;
	}
	
	
	public TrackedBot getOpponentKeeper()
	{
		return opponentKeeper;
	}
	
	
	public void setOpponentKeeper(TrackedBot opponentKeeper)
	{
		this.opponentKeeper = opponentKeeper;
	}
}
