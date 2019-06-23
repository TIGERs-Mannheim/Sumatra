/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.EnhancedFieldAnalyser;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle.AIRectangleVector;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern.Pattern;


/**
 * This class should be used to store and combine all tactical information
 * calculated by {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis} and its calculators.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
@Entity
public class TacticalField implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long						serialVersionUID					= -2739518925641905536L;
	
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<ECalculator>						activeCalculators;
	
	// goal points
	private List<DefensePoint>						defGoalPoints;
	private List<ValuePoint>						goalValuePoints;
	private IVector2									bestDirectShootTarget			= null;
	private Float										valueOfBestDirectShootTarget	= null;
	
	// DEBUG
	private List<IDrawableShape>					debugShapes;
	
	// offense
	private List<ValuePoint>						offCarrierPoints;
	private List<ValuePoint>						offLeftReceiverPoints;
	private List<ValuePoint>						offRightReceiverPoints;
	
	private Map<BotID, BotDistance>				tigersToBallDist;
	private BotDistance								tigerClosestToBall				= BotDistance.NULL_BOT_DISTANCE;
	private Map<BotID, BotDistance>				enemiesToBallDist;
	private BotDistance								enemyClosestToBall				= BotDistance.NULL_BOT_DISTANCE;
	
	/** Which bot (opponent and Tigers) has the ball? */
	private BallPossession							ballPossession;
	
	/** Was ther possibly a goal? */
	private EPossibleGoal							possibleGoal;
	
	/** we can shoot on the goal immediately */
	private boolean									tigersScoringChance;
	/** our opponents can shoot on the goal immediately */
	private boolean									opponentScoringChance;
	/** we can shoot on the goal after getting and/or aiming the ball */
	private boolean									tigersApproximateScoringChance;
	/** our opponents can shoot on the goal after getting and/or aiming the ball */
	private boolean									opponentApproximateScoringChance;
	
	private boolean									ballInOurPenArea;
	
	private ETeam										teamClosestToBall;
	private List<TrackedBot>						dangerousOpponents;
	private TrackedBot								opponentBallGetter;
	private TrackedBot								opponentPassReceiver;
	private TrackedBot								opponentKeeper;
	
	private transient EnhancedFieldAnalyser	dynamicFieldAnalyser;
	// descending patterns
	private List<Pattern>							playPattern;
	
	/**
	 * Id of bot that kicked the ball and is not allowed to touch it another time until another bot touched it. May
	 * be null!
	 */
	private BotID										botNotAllowedToTouchBall;
	private BotID										botLastTouchedBall;
	
	private boolean									otherMixedTeamTouchedBall;
	
	private boolean									forceStartAfterKickoffEnemies;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param worldFrame
	 */
	public TacticalField(WorldFrame worldFrame)
	{
		activeCalculators = new ArrayList<ECalculator>(ECalculator.values().length);
		
		defGoalPoints = new ArrayList<DefensePoint>();
		goalValuePoints = new ArrayList<ValuePoint>();
		debugShapes = new ArrayList<IDrawableShape>();
		
		offCarrierPoints = new ArrayList<ValuePoint>();
		offLeftReceiverPoints = new ArrayList<ValuePoint>();
		offRightReceiverPoints = new ArrayList<ValuePoint>();
		
		ballPossession = new BallPossession();
		possibleGoal = EPossibleGoal.NO_ONE;
		
		tigersToBallDist = new LinkedHashMap<BotID, BotDistance>();
		enemiesToBallDist = new LinkedHashMap<BotID, BotDistance>();
		
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
		
		playPattern = new ArrayList<Pattern>();
		
		botNotAllowedToTouchBall = new BotID();
		botLastTouchedBall = new BotID();
		
		otherMixedTeamTouchedBall = false;
		
		forceStartAfterKickoffEnemies = false;
		
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values
	 * @param original
	 */
	public TacticalField(TacticalField original)
	{
		activeCalculators = original.activeCalculators;
		
		defGoalPoints = new ArrayList<DefensePoint>(original.defGoalPoints);
		goalValuePoints = new ArrayList<ValuePoint>(original.goalValuePoints);
		bestDirectShootTarget = original.bestDirectShootTarget;
		debugShapes = new ArrayList<IDrawableShape>(original.getDebugShapes());
		
		offCarrierPoints = new ArrayList<ValuePoint>(original.offCarrierPoints);
		offLeftReceiverPoints = new ArrayList<ValuePoint>(original.offLeftReceiverPoints);
		offRightReceiverPoints = new ArrayList<ValuePoint>(original.offRightReceiverPoints);
		
		ballPossession = original.ballPossession;
		possibleGoal = original.possibleGoal;
		
		tigersToBallDist = new LinkedHashMap<BotID, BotDistance>(original.tigersToBallDist);
		tigerClosestToBall = original.tigerClosestToBall;
		enemiesToBallDist = new LinkedHashMap<BotID, BotDistance>(original.enemiesToBallDist);
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
		
		dynamicFieldAnalyser = original.dynamicFieldAnalyser;
		
		playPattern = new ArrayList<Pattern>(original.playPattern);
		
		botNotAllowedToTouchBall = original.botNotAllowedToTouchBall;
		botLastTouchedBall = original.botLastTouchedBall;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * Calculates the number of enemies on our subfield.
	 * @param worldFrame
	 * 
	 * @return
	 */
	public int getEnemiesOnOwnSubfield(WorldFrame worldFrame)
	{
		int counter = 0;
		TrackedBot bot;
		final Iterator<Entry<BotID, TrackedBot>> foeIterator = worldFrame.getFoeBotMapIterator();
		
		while (foeIterator.hasNext())
		{
			bot = foeIterator.next().getValue();
			if (bot.getPos().x() <= 0)
			{
				counter++;
			}
		}
		return counter;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public List<ECalculator> getActiveCalculators()
	{
		return activeCalculators;
	}
	
	
	/**
	 * @param newCalculators
	 */
	public void setActiveCalculators(List<ECalculator> newCalculators)
	{
		activeCalculators.clear();
		activeCalculators.addAll(newCalculators);
	}
	
	
	/**
	 * @return
	 */
	public List<DefensePoint> getDefGoalPoints()
	{
		return defGoalPoints;
	}
	
	
	/**
	 * @param newDefGoalPoints
	 */
	public void setDefGoalPoints(List<DefensePoint> newDefGoalPoints)
	{
		defGoalPoints.clear();
		defGoalPoints.addAll(newDefGoalPoints);
	}
	
	
	/**
	 * @return
	 */
	public List<ValuePoint> getOffLeftReceiverPoints()
	{
		return offLeftReceiverPoints;
	}
	
	
	/**
	 * @param newOffLeftReceiverPoints
	 */
	public void setOffLeftReceiverPoints(List<ValuePoint> newOffLeftReceiverPoints)
	{
		offLeftReceiverPoints.clear();
		offLeftReceiverPoints.addAll(newOffLeftReceiverPoints);
	}
	
	
	/**
	 * @return
	 */
	public List<ValuePoint> getOffRightReceiverPoints()
	{
		return offRightReceiverPoints;
	}
	
	
	/**
	 * @param newOffRightReceiverPoints
	 */
	public void setOffRightReceiverPoints(List<ValuePoint> newOffRightReceiverPoints)
	{
		offRightReceiverPoints.clear();
		offRightReceiverPoints.addAll(newOffRightReceiverPoints);
	}
	
	
	/**
	 * @return
	 */
	public List<ValuePoint> getOffCarrierPoints()
	{
		return offCarrierPoints;
	}
	
	
	/**
	 * @param newOffCarrierPoints
	 */
	public void setOffCarrierPoints(List<ValuePoint> newOffCarrierPoints)
	{
		offCarrierPoints.clear();
		offCarrierPoints.addAll(newOffCarrierPoints);
	}
	
	
	/**
	 * @return
	 */
	public BallPossession getBallPossession()
	{
		return ballPossession;
	}
	
	
	/**
	 * @param newBallPossesion
	 */
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
	
	
	/**
	 * @return
	 */
	public boolean getTigersScoringChance()
	{
		return tigersScoringChance;
	}
	
	
	/**
	 * @param tigersScoringChance
	 */
	public void setTigersScoringChance(boolean tigersScoringChance)
	{
		this.tigersScoringChance = tigersScoringChance;
	}
	
	
	/**
	 * @return
	 */
	public boolean getOpponentScoringChance()
	{
		return opponentScoringChance;
	}
	
	
	/**
	 * @param opponentScoringChance
	 */
	public void setOpponentScoringChance(boolean opponentScoringChance)
	{
		this.opponentScoringChance = opponentScoringChance;
	}
	
	
	/**
	 * @return
	 */
	public boolean getTigersApproximateScoringChance()
	{
		return tigersApproximateScoringChance;
	}
	
	
	/**
	 * @param tigersApproximateScoringChance
	 */
	public void setTigersApproximateScoringChance(boolean tigersApproximateScoringChance)
	{
		this.tigersApproximateScoringChance = tigersApproximateScoringChance;
	}
	
	
	/**
	 * @return
	 */
	public boolean getOpponentApproximateScoringChance()
	{
		return opponentApproximateScoringChance;
	}
	
	
	/**
	 * @param opponentApproximateScoringChance
	 */
	public void setOpponentApproximateScoringChance(boolean opponentApproximateScoringChance)
	{
		this.opponentApproximateScoringChance = opponentApproximateScoringChance;
	}
	
	
	/**
	 * @param newTigersToBallDist
	 */
	public void setTigersToBallDist(List<BotDistance> newTigersToBallDist)
	{
		tigersToBallDist.clear();
		for (final BotDistance bDist : newTigersToBallDist)
		{
			tigersToBallDist.put(bDist.getBot().getId(), bDist);
		}
	}
	
	
	/**
	 * @return A {@link LinkedHashMap} with all Tiger bots sorted by their distance to the ball (
	 *         {@link BotDistance#ASCENDING}).
	 */
	public Map<BotID, BotDistance> getTigersToBallDist()
	{
		return tigersToBallDist;
	}
	
	
	/**
	 * Implemented lazy. If {@link #tigersToBallDist} is empty (tests!?) a {@link java.util.NoSuchElementException} would
	 * be
	 * thrown.
	 * 
	 * @return The {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are no
	 *         Tiger bots!!!)
	 */
	public BotDistance getTigerClosestToBall()
	{
		if ((tigerClosestToBall == BotDistance.NULL_BOT_DISTANCE) && (tigersToBallDist.values().size() > 0))
		{
			tigerClosestToBall = tigersToBallDist.values().iterator().next();
		}
		return tigerClosestToBall;
	}
	
	
	/**
	 * @param newEnemiesToBallDist
	 */
	public void setEnemiesToBallDist(List<BotDistance> newEnemiesToBallDist)
	{
		enemiesToBallDist.clear();
		for (final BotDistance bDist : newEnemiesToBallDist)
		{
			enemiesToBallDist.put(bDist.getBot().getId(), bDist);
		}
	}
	
	
	/**
	 * @return A {@link LinkedHashMap} with all enemy bots sorted by their distance to the ball (
	 *         {@link BotDistance#ASCENDING}).
	 */
	public Map<BotID, BotDistance> getEnemiesToBallDist()
	{
		return enemiesToBallDist;
	}
	
	
	/**
	 * Implemented lazy. If {@link #enemiesToBallDist} is empty (tests!?) a {@link java.util.NoSuchElementException}
	 * would be
	 * thrown.
	 * 
	 * @return The enemy {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are
	 *         no
	 *         enemy bots!!!)
	 */
	public BotDistance getEnemyClosestToBall()
	{
		if ((enemyClosestToBall == BotDistance.NULL_BOT_DISTANCE) && (enemiesToBallDist.values().size() > 0))
		{
			enemyClosestToBall = enemiesToBallDist.values().iterator().next();
		}
		return enemyClosestToBall;
	}
	
	
	/**
	 * @return
	 */
	public ETeam getTeamClosestToBall()
	{
		return teamClosestToBall;
	}
	
	
	/**
	 * @param newTeamClosestToBall
	 */
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
	
	
	/**
	 * @return
	 */
	public boolean isBallInOurPenArea()
	{
		return ballInOurPenArea;
	}
	
	
	/**
	 * @param ballInOurPenArea
	 */
	public void setBallInOurPenArea(boolean ballInOurPenArea)
	{
		this.ballInOurPenArea = ballInOurPenArea;
	}
	
	
	/**
	 * @return
	 */
	public List<TrackedBot> getDangerousOpponents()
	{
		return dangerousOpponents;
	}
	
	
	/**
	 * @param newDangerousOpponents
	 */
	public void setDangerousOpponents(List<TrackedBot> newDangerousOpponents)
	{
		dangerousOpponents.clear();
		dangerousOpponents.addAll(newDangerousOpponents);
	}
	
	
	/**
	 * @return
	 */
	public TrackedBot getOpponentBallGetter()
	{
		return opponentBallGetter;
	}
	
	
	/**
	 * @param opponentBallGetter
	 */
	public void setOpponentBallGetter(TrackedBot opponentBallGetter)
	{
		this.opponentBallGetter = opponentBallGetter;
	}
	
	
	/**
	 * @return
	 */
	public TrackedBot getOpponentPassReceiver()
	{
		return opponentPassReceiver;
	}
	
	
	/**
	 * @param opponentPassReceiver
	 */
	public void setOpponentPassReceiver(TrackedBot opponentPassReceiver)
	{
		this.opponentPassReceiver = opponentPassReceiver;
	}
	
	
	/**
	 * @return
	 */
	public TrackedBot getOpponentKeeper()
	{
		return opponentKeeper;
	}
	
	
	/**
	 * @param opponentKeeper
	 */
	public void setOpponentKeeper(TrackedBot opponentKeeper)
	{
		this.opponentKeeper = opponentKeeper;
	}
	
	
	/**
	 * @return
	 */
	public List<Pattern> getPlayPattern()
	{
		return playPattern;
	}
	
	
	/**
	 * @param pattern
	 */
	public void setPlayPattern(List<Pattern> pattern)
	{
		playPattern.clear();
		playPattern.addAll(pattern);
	}
	
	
	/**
	 * @return the botNotAllowedToTouchBall
	 */
	public BotID getBotNotAllowedToTouchBall()
	{
		return botNotAllowedToTouchBall;
	}
	
	
	/**
	 * @param botNotAllowedToTouchBall the {@link TacticalField#botNotAllowedToTouchBall} to set
	 */
	public void setBotNotAllowedToTouchBall(BotID botNotAllowedToTouchBall)
	{
		this.botNotAllowedToTouchBall = botNotAllowedToTouchBall;
	}
	
	
	/**
	 * @return
	 */
	public EPossibleGoal getPossibleGoal()
	{
		return possibleGoal;
	}
	
	
	/**
	 * @param possibleGoal
	 */
	public void setPossibleGoal(EPossibleGoal possibleGoal)
	{
		this.possibleGoal = possibleGoal;
	}
	
	
	/**
	 * @return the botLastTouchedBall
	 */
	public BotID getBotLastTouchedBall()
	{
		return botLastTouchedBall;
	}
	
	
	/**
	 * @param botLastTouchedBall the botLastTouchedBall to set
	 */
	public void setBotLastTouchedBall(BotID botLastTouchedBall)
	{
		this.botLastTouchedBall = botLastTouchedBall;
	}
	
	
	/**
	 * @return the debugShapes
	 */
	public List<IDrawableShape> getDebugShapes()
	{
		return debugShapes;
	}
	
	
	/**
	 * @param dynamicFieldAnalyser - a analyser witch allowed to find some specific points with the help of
	 *           {@link AIRectangleVector}
	 */
	public void setEnhancedFieldAnalyser(EnhancedFieldAnalyser dynamicFieldAnalyser)
	{
		this.dynamicFieldAnalyser = dynamicFieldAnalyser;
		
	}
	
	
	/**
	 * @return dynamicFieldAnalyser - a analyser witch allowed to find some specific points with the help of
	 *         {@link AIRectangleVector}
	 */
	public EnhancedFieldAnalyser getEnhancedFieldAnalyser()
	{
		return dynamicFieldAnalyser;
	}
	
	
	/**
	 * @return the otherMixedTeamTouchedBall
	 */
	public final boolean isOtherMixedTeamTouchedBall()
	{
		return otherMixedTeamTouchedBall;
	}
	
	
	/**
	 * @param otherMixedTeamTouchedBall the otherMixedTeamTouchedBall to set
	 */
	public final void setOtherMixedTeamTouchedBall(boolean otherMixedTeamTouchedBall)
	{
		this.otherMixedTeamTouchedBall = otherMixedTeamTouchedBall;
	}
	
	
	/**
	 * @return the goalValuePoints
	 */
	public final List<ValuePoint> getGoalValuePoints()
	{
		return goalValuePoints;
	}
	
	
	/**
	 * @return the bestDirectShootTarget
	 */
	public final IVector2 getBestDirectShootTarget()
	{
		return bestDirectShootTarget;
	}
	
	
	/**
	 * 
	 * @return Float scala for a good (0) or bad (1) shoot target [0..1]
	 */
	public final Float getValueOfBestDirectShootTarget()
	{
		return valueOfBestDirectShootTarget;
	}
	
	
	/**
	 * @param bestDirectShootTarget the bestDirectShootTarget to set
	 */
	public final void setBestDirectShootTarget(IVector2 bestDirectShootTarget)
	{
		this.bestDirectShootTarget = bestDirectShootTarget;
	}
	
	
	/**
	 * 
	 * @param value Value of the best point for a direct shoot
	 */
	public final void setValueOfBestDirectShootTarget(Float value)
	{
		valueOfBestDirectShootTarget = value;
	}
	
	
	/**
	 * true if a force start occurs after kickoff
	 * @return
	 */
	public boolean isForceStartAfterKickoffEnemies()
	{
		return forceStartAfterKickoffEnemies;
	}
	
	
	/**
	 * @param forceStartAfterKickoffEnemies the forceStartAfterKickoffEnemies to set
	 */
	public void setForceStartAfterKickoffEnemies(boolean forceStartAfterKickoffEnemies)
	{
		this.forceStartAfterKickoffEnemies = forceStartAfterKickoffEnemies;
	}
}
