/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.Size;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValueBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.AIRectangleVector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.EnhancedFieldAnalyser;


/**
 * This class should be used to store and combine all tactical information
 * calculated by {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis} and its calculators.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 9)
public class TacticalField implements ITacticalField
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// goal points
	@NotNull
	@Size(min = 1, profiles = { "fullAiFrame" })
	private List<DefensePoint>						defGoalPoints;
	@NotNull
	@Size(min = 1, profiles = { "fullAiFrame" })
	private List<ValuePoint>						goalValuePoints;
	private ValuePoint								bestDirectShotTarget				= new ValuePoint(Vector2.ZERO_VECTOR);
	private Map<BotID, ValuePoint>				bestDirectShotTargetBots		= new HashMap<BotID, ValuePoint>();
	private Map<BotID, List<ValueBot>>			shooterReceiverStraightLines	= new HashMap<BotID, List<ValueBot>>();
	private Map<BotID, ValueBot>					ballReceiverStraightLines		= new HashMap<BotID, ValueBot>();
	
	// offense
	@NotNull
	private Map<BotID, BotDistance>				tigersToBallDist;
	
	@NotNull
	private Map<BotID, BotDistance>				enemiesToBallDist;
	
	/** Which bot (opponent and Tigers) has the ball? */
	@NotNull
	private BallPossession							ballPossession;
	
	/** Was there possibly a goal? */
	@NotNull
	private EPossibleGoal							possibleGoal;
	
	private transient EnhancedFieldAnalyser	dynamicFieldAnalyser;
	
	private BotID										botLastTouchedBall;
	
	private Map<BotID, IVector2>					supportPositions;
	private Map<BotID, IVector2>					supportTargets;
	private List<IVector2>							supportIntersections;
	private Map<BotID, IVector2>					supportRedirectPositions;
	private transient Map<BotID, ValuedField>	supportValues;
	
	private Map<BotID, BotAiInformation>		botAiInformation;
	
	private EGameState								gameState							= EGameState.UNKNOWN;
	
	private IVector2									ballLeftFieldPos					= null;
	
	private Map<BotID, ValuePoint>				offenseMovePositions;
	
	private ValueBot									bestPassTarget;
	
	/** Statistics object */
	private Statistics								statistics;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	private TacticalField()
	{
		defGoalPoints = new ArrayList<DefensePoint>();
		goalValuePoints = new ArrayList<ValuePoint>();
		
		ballPossession = new BallPossession();
		possibleGoal = EPossibleGoal.NO_ONE;
		
		tigersToBallDist = new LinkedHashMap<BotID, BotDistance>();
		enemiesToBallDist = new LinkedHashMap<BotID, BotDistance>();
		
		botLastTouchedBall = BotID.createBotId();
		
		supportPositions = new HashMap<BotID, IVector2>();
		supportTargets = new HashMap<BotID, IVector2>();
		supportIntersections = new ArrayList<IVector2>();
		supportRedirectPositions = new HashMap<BotID, IVector2>();
		supportValues = new HashMap<BotID, ValuedField>();
		offenseMovePositions = new HashMap<BotID, ValuePoint>();
		botAiInformation = new HashMap<BotID, BotAiInformation>();
		
		bestPassTarget = null;
		
		statistics = null;
	}
	
	
	/**
	 * @param worldFrame
	 */
	public TacticalField(final WorldFrame worldFrame)
	{
		this();
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values
	 * 
	 * @param original
	 */
	public TacticalField(final TacticalField original)
	{
		defGoalPoints = new ArrayList<DefensePoint>(original.defGoalPoints);
		goalValuePoints = new ArrayList<ValuePoint>(original.goalValuePoints);
		bestDirectShotTarget = original.bestDirectShotTarget;
		
		ballPossession = original.ballPossession;
		possibleGoal = original.possibleGoal;
		
		tigersToBallDist = new LinkedHashMap<BotID, BotDistance>(original.tigersToBallDist);
		enemiesToBallDist = new LinkedHashMap<BotID, BotDistance>(original.enemiesToBallDist);
		
		dynamicFieldAnalyser = original.dynamicFieldAnalyser;
		
		botLastTouchedBall = original.botLastTouchedBall;
		
		supportPositions = new HashMap<BotID, IVector2>(original.supportPositions);
		supportTargets = new HashMap<BotID, IVector2>(original.supportTargets);
		supportIntersections = new ArrayList<IVector2>(original.supportIntersections);
		supportRedirectPositions = new HashMap<BotID, IVector2>(original.supportRedirectPositions);
		supportValues = new HashMap<BotID, ValuedField>(original.supportValues);
		offenseMovePositions = new HashMap<BotID, ValuePoint>(original.offenseMovePositions);
		botAiInformation = new HashMap<BotID, BotAiInformation>(original.botAiInformation);
		
		gameState = original.gameState;
		ballLeftFieldPos = original.ballLeftFieldPos;
		
		bestPassTarget = original.getBestPassTarget();
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	@Override
	public List<DefensePoint> getDefGoalPoints()
	{
		return Collections.unmodifiableList(defGoalPoints);
	}
	
	
	/**
	 * @param newDefGoalPoints
	 */
	public void setDefGoalPoints(final List<DefensePoint> newDefGoalPoints)
	{
		defGoalPoints = newDefGoalPoints;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public BallPossession getBallPossession()
	{
		return ballPossession;
	}
	
	
	/**
	 * @param newBallPossession
	 */
	public void setBallPossession(final BallPossession newBallPossession)
	{
		if (newBallPossession == null)
		{
			ballPossession = new BallPossession();
		} else
		{
			ballPossession = newBallPossession;
		}
	}
	
	
	/**
	 * @param newTigersToBallDist
	 */
	public void setTigersToBallDist(final List<BotDistance> newTigersToBallDist)
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
	@Override
	public Map<BotID, BotDistance> getTigersToBallDist()
	{
		return Collections.unmodifiableMap(tigersToBallDist);
	}
	
	
	/**
	 * @return The {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are no
	 *         Tiger bots!!!)
	 */
	@Override
	public BotDistance getTigerClosestToBall()
	{
		if (!getTigersToBallDist().values().isEmpty())
		{
			return getTigersToBallDist().values().iterator().next();
		}
		return BotDistance.NULL_BOT_DISTANCE;
	}
	
	
	/**
	 * @param newEnemiesToBallDist
	 */
	public void setEnemiesToBallDist(final List<BotDistance> newEnemiesToBallDist)
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
	@Override
	public Map<BotID, BotDistance> getEnemiesToBallDist()
	{
		return Collections.unmodifiableMap(enemiesToBallDist);
	}
	
	
	/**
	 * @return The enemy {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are
	 *         no enemy bots!!!)
	 */
	@Override
	public BotDistance getEnemyClosestToBall()
	{
		if (enemiesToBallDist.values().size() > 0)
		{
			return enemiesToBallDist.values().iterator().next();
		}
		return BotDistance.NULL_BOT_DISTANCE;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public EPossibleGoal getPossibleGoal()
	{
		return possibleGoal;
	}
	
	
	/**
	 * @param possibleGoal
	 */
	public void setPossibleGoal(final EPossibleGoal possibleGoal)
	{
		this.possibleGoal = possibleGoal;
	}
	
	
	/**
	 * @return the botLastTouchedBall
	 */
	@Override
	public BotID getBotLastTouchedBall()
	{
		return botLastTouchedBall;
	}
	
	
	/**
	 * @param botLastTouchedBall the botLastTouchedBall to set
	 */
	public void setBotLastTouchedBall(final BotID botLastTouchedBall)
	{
		this.botLastTouchedBall = botLastTouchedBall;
	}
	
	
	/**
	 * @param dynamicFieldAnalyser - a analyser witch allowed to find some specific points with the help of
	 *           {@link AIRectangleVector}
	 */
	public void setEnhancedFieldAnalyser(final EnhancedFieldAnalyser dynamicFieldAnalyser)
	{
		this.dynamicFieldAnalyser = dynamicFieldAnalyser;
		
	}
	
	
	/**
	 * @return dynamicFieldAnalyser - a analyser witch allowed to find some specific points with the help of
	 *         {@link AIRectangleVector}
	 */
	@Override
	public EnhancedFieldAnalyser getEnhancedFieldAnalyser()
	{
		return dynamicFieldAnalyser;
	}
	
	
	/**
	 * @return the goalValuePoints
	 */
	@Override
	public final List<ValuePoint> getGoalValuePoints()
	{
		return Collections.unmodifiableList(goalValuePoints);
	}
	
	
	/**
	 * @param goalValuePoints
	 */
	public final void setGoalValuePoints(final List<ValuePoint> goalValuePoints)
	{
		this.goalValuePoints = goalValuePoints;
	}
	
	
	/**
	 * @return the bestDirectShootTarget
	 */
	@Override
	public final ValuePoint getBestDirectShootTarget()
	{
		return bestDirectShotTarget;
	}
	
	
	/**
	 * @param bestDirectShootTarget the bestDirectShootTarget to set
	 */
	public final void setBestDirectShotTarget(final ValuePoint bestDirectShootTarget)
	{
		bestDirectShotTarget = bestDirectShootTarget;
	}
	
	
	/**
	 * @return the bestDirectShotTargetBots
	 */
	@Override
	public final Map<BotID, ValuePoint> getBestDirectShotTargetBots()
	{
		return Collections.unmodifiableMap(bestDirectShotTargetBots);
	}
	
	
	/**
	 * @param bestDirectShotTargetBots the bestDirectShotTargetBots to set
	 */
	public final void setBestDirectShotTargetBots(final Map<BotID, ValuePoint> bestDirectShotTargetBots)
	{
		this.bestDirectShotTargetBots = bestDirectShotTargetBots;
	}
	
	
	/**
	 * @return the shooterReceiverStraightLines
	 */
	@Override
	public final Map<BotID, List<ValueBot>> getShooterReceiverStraightLines()
	{
		if (shooterReceiverStraightLines == null)
		{
			shooterReceiverStraightLines = new HashMap<BotID, List<ValueBot>>();
		}
		return Collections.unmodifiableMap(shooterReceiverStraightLines);
	}
	
	
	/**
	 * @param shooterReceiverStraightLines the shooterReceiverStraightLines to set
	 */
	public final void setShooterReceiverStraightLines(final Map<BotID, List<ValueBot>> shooterReceiverStraightLines)
	{
		this.shooterReceiverStraightLines = shooterReceiverStraightLines;
	}
	
	
	/**
	 * @return the ballReceiverStraightLines
	 */
	@Override
	public final Map<BotID, ValueBot> getBallReceiverStraightLines()
	{
		if (ballReceiverStraightLines == null)
		{
			ballReceiverStraightLines = new HashMap<BotID, ValueBot>();
		}
		return Collections.unmodifiableMap(ballReceiverStraightLines);
	}
	
	
	/**
	 * @param ballReceiverStraightLines the ballReceiverStraightLines to set
	 */
	public final void setBallReceiverStraightLines(final Map<BotID, ValueBot> ballReceiverStraightLines)
	{
		this.ballReceiverStraightLines = ballReceiverStraightLines;
	}
	
	
	/**
	 * @return Move Positions OffenseRole
	 */
	@Override
	public final Map<BotID, ValuePoint> getOffenseMovePositions()
	{
		return offenseMovePositions;
	}
	
	
	/**
	 * @param offenseMovePositions Positions for the offenserole to set
	 */
	public final void setOffenseMovePositions(final Map<BotID, ValuePoint> offenseMovePositions)
	{
		this.offenseMovePositions = offenseMovePositions;
	}
	
	
	/**
	 * @return Positions for the SupportRole
	 */
	@Override
	public final Map<BotID, IVector2> getSupportPositions()
	{
		return supportPositions;
	}
	
	
	/**
	 * @param supportPositions Positions for the SupportRole to set
	 */
	public final void setSupportPositions(final Map<BotID, IVector2> supportPositions)
	{
		this.supportPositions = supportPositions;
	}
	
	
	/**
	 * @return Targets for the SupportRole
	 */
	@Override
	public final Map<BotID, IVector2> getSupportTargets()
	{
		return supportTargets;
	}
	
	
	/**
	 * @param supportTargets Targets for the SupportRole to set
	 */
	public final void setSupportTargets(final Map<BotID, IVector2> supportTargets)
	{
		this.supportTargets = supportTargets;
	}
	
	
	/**
	 * @return the botAiInformation
	 */
	@Override
	public final Map<BotID, BotAiInformation> getBotAiInformation()
	{
		return botAiInformation;
	}
	
	
	/**
	 * @param botAiInformation the botAiInformation to set
	 */
	public final void setBotAiInformation(final Map<BotID, BotAiInformation> botAiInformation)
	{
		this.botAiInformation = botAiInformation;
	}
	
	
	/**
	 * @return the gameState
	 */
	@Override
	public final EGameState getGameState()
	{
		return gameState;
	}
	
	
	/**
	 * @param gameState the gameState to set
	 */
	public final void setGameState(final EGameState gameState)
	{
		this.gameState = gameState;
	}
	
	
	/**
	 * @return the ballLeftFieldPos
	 */
	@Override
	public final IVector2 getBallLeftFieldPos()
	{
		return ballLeftFieldPos;
	}
	
	
	/**
	 * @param ballLeftFieldPos the ballLeftFieldPos to set
	 */
	public final void setBallLeftFieldPos(final IVector2 ballLeftFieldPos)
	{
		this.ballLeftFieldPos = ballLeftFieldPos;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public ValueBot getBestPassTarget()
	{
		return bestPassTarget;
	}
	
	
	/**
	 * @param newBestPassTarget
	 */
	public void setBestPassTarget(final ValueBot newBestPassTarget)
	{
		bestPassTarget = newBestPassTarget;
	}
	
	
	/**
	 * @return the supportIntersections
	 */
	@Override
	public final List<IVector2> getSupportIntersections()
	{
		return supportIntersections;
	}
	
	
	/**
	 * @param supportIntersections the supportIntersections to set
	 */
	public final void setSupportIntersections(final List<IVector2> supportIntersections)
	{
		this.supportIntersections = supportIntersections;
	}
	
	
	/**
	 * @return the supportRedirectPositions
	 */
	@Override
	public final Map<BotID, IVector2> getSupportRedirectPositions()
	{
		return supportRedirectPositions;
	}
	
	
	/**
	 * @param supportRedirectPositions the supportRedirectPositions to set
	 */
	public final void setSupportRedirectPositions(final Map<BotID, IVector2> supportRedirectPositions)
	{
		this.supportRedirectPositions = supportRedirectPositions;
	}
	
	
	/**
	 * @return the supportValues
	 */
	@Override
	public final Map<BotID, ValuedField> getSupportValues()
	{
		return supportValues;
	}
	
	
	/**
	 * @param stats the stats to set
	 */
	public void setStatistics(final Statistics stats)
	{
		statistics = stats;
	}
	
	
	/**
	 * @return the Statistics
	 */
	@Override
	public Statistics getStatistics()
	{
		return statistics;
	}
}
