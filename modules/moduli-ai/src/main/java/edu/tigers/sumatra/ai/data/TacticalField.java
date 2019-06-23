/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.data.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.data.event.GameEvents;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.ECalculator;
import edu.tigers.sumatra.ai.metis.defense.KeeperStateCalc;
import edu.tigers.sumatra.ai.metis.defense.data.AngleDefenseData;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.data.AdvancedPassTarget;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;


/**
 * This class should be used to store and combine all tactical information
 * calculated by {@link edu.tigers.sumatra.ai.metis.Metis} and its calculators.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TacticalField implements ITacticalField
{
	private final List<FoeBotData>					dangerousFoeBots							= new ArrayList<FoeBotData>();
	
	private AngleDefenseData							angleDefenseData							= new AngleDefenseData();
	
	private DefensePoint									directShotSingleDefenderDefPoint		= null;
	private DefensePoint									directShotDoubleDefenderDefPointA	= null;
	private DefensePoint									directShotDoubleDefenderDefPointB	= null;
	
	private Map<ITrackedBot, DefensePoint>			directShotDefenderDistr					= new HashMap<ITrackedBot, DefensePoint>();
	
	private List<BotID>									crucialDefenders							= new ArrayList<BotID>();
	
	private final List<ValuePoint>					goalValuePoints							= new ArrayList<>();
	private ValuePoint									bestDirectShotTarget						= new ValuePoint(
			
			Geometry.getGoalTheir()
					.getGoalCenter());
	
	private Map<BotID, ValuePoint>					bestDirectShotTargetsForTigerBots	= new HashMap<BotID, ValuePoint>();
	
	private final Map<BotID, BotDistance>			tigersToBallDist							= new LinkedHashMap<>();
	
	private final Map<BotID, BotDistance>			enemiesToBallDist							= new LinkedHashMap<>();
	
	/** Which bot (opponent and Tigers) has the ball? */
	private BallPossession								ballPossession;
	
	/** Was there possibly a goal? */
	private EPossibleGoal								possibleGoal;
	
	/** Bot who was touching ball the last time, not null if once a bot has touched the ball */
	private BotID											botLastTouchedBall;
	/** Bot who is touching ball, can be null if no one is touching ball */
	private BotID											botTouchedBall;
	
	private final Map<BotID, BotAiInformation>	botAiInformation							= new HashMap<>();
	
	private EGameStateTeam								gameState									= EGameStateTeam.UNKNOWN;
	/** Flag for a goal scored (tigers or foes), used for forcing all bots on our side before prepare kickoff signal */
	private boolean										goalScored									= false;
	
	private IVector2										ballLeftFieldPos							= null;
	
	private final Map<BotID, OffensiveAction>		offensiveActions							= new HashMap<>();
	
	/** Statistics object */
	private final MatchStatistics						statistics;
	
	private final GameEvents							gameEvents;
	
	private OffensiveStrategy							offensiveStrategy;
	
	private final Map<EPlay, RoleFinderInfo>		roleFinderInfos							= new LinkedHashMap<>();
	
	private EGameBehavior								gameBehavior								= EGameBehavior.OFFENSIVE;
	
	private final ShapeMap								drawableShapes								= new ShapeMap();
	private final List<AdvancedPassTarget>			advancedPassTargetsRanked				= new ArrayList<>();
	
	private final Map<ECalculator, Integer>		metisCalcTimes								= new EnumMap<>(
			ECalculator.class);
	
	private final List<IVector2>						topGpuGridPositions						= new ArrayList<>();
	private ValuedField									supporterValuedField;
	
	private IBotIDMap<ITrackedBot>					supporterBots								= new BotIDMap<>();
	private List<ValuePoint>							scoreChancePoints							= new LinkedList<>();
	private List<ValuePoint>							ballDistancePoints						= new LinkedList<>();
	
	
	private List<ValuePoint>							distanceToFOEGrid							= new LinkedList<>();
	
	private boolean										mixedTeamBothTouchedBall				= false;
	
	private AutomatedThrowInInfo						throwInInfo									= new AutomatedThrowInInfo();
	
	private KeeperStateCalc.EStateId					keeperState									= KeeperStateCalc.EStateId.MOVE_TO_PENALTYAREA;
	
	private Map<BotID, Double>							kickSkillTimes								= new HashMap<>();
	
	private Map<BotID, LedControl>					ledData										= new HashMap<>();
	
	
	/**
	  */
	public TacticalField()
	{
		ballPossession = new BallPossession();
		possibleGoal = EPossibleGoal.NO_ONE;
		botLastTouchedBall = BotID.get();
		statistics = new MatchStatistics();
		gameEvents = new GameEvents();
	}
	
	
	@Override
	public final void cleanup()
	{
		supporterValuedField = null;
	}
	
	
	/**
	 * @param directShotSingleDefenderDefPoint
	 */
	@Override
	public void setDirectShotSingleDefenderDefPoint(final DefensePoint directShotSingleDefenderDefPoint)
	{
		
		this.directShotSingleDefenderDefPoint = directShotSingleDefenderDefPoint;
	}
	
	
	/**
	 * @return the angleDefensePreData
	 */
	@Override
	public AngleDefenseData getAngleDefenseData()
	{
		return angleDefenseData;
	}
	
	
	/**
	 * @param angleDefenseData the angleDefensePreData to set
	 */
	@Override
	public void setAngleDefenseData(final AngleDefenseData angleDefenseData)
	{
		this.angleDefenseData = angleDefenseData;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public DefensePoint getDirectShotSingleDefenderDefPoint()
	{
		
		return directShotSingleDefenderDefPoint;
	}
	
	
	/**
	 * @param directShotDoubleDefenderDefPointA
	 */
	@Override
	public void setDirectShotDoubleDefenderDefPointA(final DefensePoint directShotDoubleDefenderDefPointA)
	{
		
		this.directShotDoubleDefenderDefPointA = directShotDoubleDefenderDefPointA;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public DefensePoint getDirectShotDoubleDefenderDefPointA()
	{
		
		return directShotDoubleDefenderDefPointA;
	}
	
	
	/**
	 * @param directShotDoubleDefenderDefPointB
	 */
	@Override
	public void setDirectShotDoubleDefenderDefPointB(final DefensePoint directShotDoubleDefenderDefPointB)
	{
		
		this.directShotDoubleDefenderDefPointB = directShotDoubleDefenderDefPointB;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public DefensePoint getDirectShotDoubleDefenderDefPointB()
	{
		
		return directShotDoubleDefenderDefPointB;
	}
	
	
	@Override
	public void setDirectShotDefenderDistr(final Map<ITrackedBot, DefensePoint> directShotDefenderDistr)
	{
		
		this.directShotDefenderDistr = directShotDefenderDistr;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public Map<ITrackedBot, DefensePoint> getDirectShotDefenderDistr()
	{
		return directShotDefenderDistr;
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
			tigersToBallDist.put(bDist.getBot().getBotId(), bDist);
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
			enemiesToBallDist.put(bDist.getBot().getBotId(), bDist);
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
	 * Bot who was touching ball the last time, not null if once a bot has touched the ball
	 * 
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
	 * Bot who is touching ball, can be null if no one is touching ball
	 * 
	 * @return the botTouchedBall
	 */
	@Override
	public BotID getBotTouchedBall()
	{
		return botTouchedBall;
	}
	
	
	/**
	 * @param botTouchedBall the botTouchedBall to set
	 */
	public void setBotTouchedBall(final BotID botTouchedBall)
	{
		this.botTouchedBall = botTouchedBall;
	}
	
	
	/**
	 * @return the goalValuePoints
	 */
	@Override
	public final List<ValuePoint> getGoalValuePoints()
	{
		return goalValuePoints;
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
	 * @return the bestDirectShotTargetsForTigerBots
	 */
	@Override
	public Map<BotID, ValuePoint> getBestDirectShotTargetsForTigerBots()
	{
		return bestDirectShotTargetsForTigerBots;
	}
	
	
	/**
	 * @param bestDirectShotTargetsForTigerBots the bestDirectShotTargetsForTigerBots to set
	 */
	public void setBestDirectShotTargetsForTigerBots(final Map<BotID, ValuePoint> bestDirectShotTargetsForTigerBots)
	{
		this.bestDirectShotTargetsForTigerBots = bestDirectShotTargetsForTigerBots;
	}
	
	
	/**
	 * @return Move Positions OffenseRole
	 */
	@Override
	public final Map<BotID, OffensiveAction> getOffensiveActions()
	{
		return offensiveActions;
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
	 * @return the gameState
	 */
	@Override
	public final EGameStateTeam getGameState()
	{
		return gameState;
	}
	
	
	/**
	 * @param gameState the gameState to set
	 */
	public final void setGameState(final EGameStateTeam gameState)
	{
		this.gameState = gameState;
	}
	
	
	/**
	 * Flag for a goal scored (tigers or foes), used for forcing all bots on our side before prepare kickoff signal
	 * true when a goal was scored, the game state is stopped until it is started again.
	 * 
	 * @return the goalScored
	 */
	@Override
	public boolean isGoalScored()
	{
		return goalScored;
	}
	
	
	/**
	 * @param goalScored the goalScored to set
	 */
	public void setGoalScored(final boolean goalScored)
	{
		this.goalScored = goalScored;
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
	 * @return the supportValues
	 */
	@Override
	public final ValuedField getSupporterValuedField()
	{
		return supporterValuedField;
	}
	
	
	/**
	 * @param supporterValuedField
	 */
	public final void setSupporterValuedField(final ValuedField supporterValuedField)
	{
		this.supporterValuedField = supporterValuedField;
	}
	
	
	/**
	 * @return the Statistics
	 */
	@Override
	public MatchStatistics getStatistics()
	{
		return statistics;
	}
	
	
	/**
	 * @return the gameEvents
	 */
	@Override
	public GameEvents getGameEvents()
	{
		return gameEvents;
	}
	
	
	/**
	 * @return offensiveStrategy
	 */
	@Override
	public OffensiveStrategy getOffensiveStrategy()
	{
		return offensiveStrategy;
	}
	
	
	/**
	 * @param strategy
	 */
	public void setOffensiveStrategy(final OffensiveStrategy strategy)
	{
		offensiveStrategy = strategy;
	}
	
	
	@Override
	public Map<EPlay, RoleFinderInfo> getRoleFinderInfos()
	{
		return roleFinderInfos;
	}
	
	
	/**
	 * @return the gameBehavior
	 */
	@Override
	public EGameBehavior getGameBehavior()
	{
		return gameBehavior;
	}
	
	
	/**
	 * @param gameBehavior the gameBehavior to set
	 */
	public void setGameBehavior(final EGameBehavior gameBehavior)
	{
		this.gameBehavior = gameBehavior;
	}
	
	
	/**
	 * @param crucialDefenderID
	 */
	@Override
	public void addCrucialDefender(final BotID crucialDefenderID)
	{
		for (BotID curBotId : crucialDefenders)
		{
			if (curBotId.getNumber() == crucialDefenderID.getNumber())
			{
				
				return;
			}
		}
		
		crucialDefenders.add(crucialDefenderID);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public List<BotID> getCrucialDefenders()
	{
		
		return crucialDefenders;
	}
	
	
	/**
	 * @return the drawableShapes
	 */
	@Override
	public ShapeMap getDrawableShapes()
	{
		return drawableShapes;
	}
	
	
	/**
	 * @return the advancedPassTargetsRanked
	 */
	@Override
	public List<AdvancedPassTarget> getAdvancedPassTargetsRanked()
	{
		return advancedPassTargetsRanked;
	}
	
	
	/**
	 * @return the metisCalcTimes
	 */
	@Override
	public Map<ECalculator, Integer> getMetisCalcTimes()
	{
		return metisCalcTimes;
	}
	
	
	/**
	 * @return the topGpuGridPositions
	 */
	@Override
	public final List<IVector2> getTopGpuGridPositions()
	{
		return topGpuGridPositions;
	}
	
	
	/**
	 * @return the mixedTeamBothTouchedBall
	 */
	@Override
	public boolean isMixedTeamBothTouchedBall()
	{
		return mixedTeamBothTouchedBall;
	}
	
	
	/**
	 * @param mixedTeamBothTouchedBall the mixedTeamBothTouchedBall to set
	 */
	public void setMixedTeamBothTouchedBall(final boolean mixedTeamBothTouchedBall)
	{
		this.mixedTeamBothTouchedBall = mixedTeamBothTouchedBall;
	}
	
	
	/**
	 * @return the throwInInfo
	 */
	@Override
	public AutomatedThrowInInfo getThrowInInfo()
	{
		return throwInInfo;
	}
	
	
	/**
	 * @param throwInInfo the throwInInfo to set
	 */
	public void setThrowInInfo(final AutomatedThrowInInfo throwInInfo)
	{
		this.throwInInfo = throwInInfo;
	}
	
	
	/**
	 * @return the keeperState
	 */
	@Override
	public KeeperStateCalc.EStateId getKeeperState()
	{
		return keeperState;
	}
	
	
	/**
	 * @param keeperState the keeperState to set
	 */
	public void setKeeperState(final KeeperStateCalc.EStateId keeperState)
	{
		this.keeperState = keeperState;
	}
	
	
	@Override
	public List<ValuePoint> getScoreChancePoints()
	{
		return scoreChancePoints;
	}
	
	
	/**
	 * @return kickSkillTimes
	 */
	public Map<BotID, Double> getKickSkillTimes()
	{
		return kickSkillTimes;
		
	}
	
	
	/**
	 * @return the distanceToFOEGrid
	 */
	public List<ValuePoint> getDistanceToFOEGrid()
	{
		return distanceToFOEGrid;
	}
	
	
	/**
	 * @param distancePoints the distanceToFOEGrid to set
	 */
	public void setDistanceToFOEGrid(final List<ValuePoint> distancePoints)
	{
		distanceToFOEGrid = distancePoints;
	}
	
	
	/**
	 * @return the supporterBots
	 */
	public IBotIDMap<ITrackedBot> getSupporterBots()
	{
		return supporterBots;
	}
	
	
	/**
	 * @param supporterBots the supporterBots to set
	 */
	public void setSupporterBots(final IBotIDMap<ITrackedBot> supporterBots)
	{
		this.supporterBots = supporterBots;
	}
	
	
	/**
	 * @param kickSkillTimes the kickSkillTimes to set
	 */
	public void setKickSkillTimes(final Map<BotID, Double> kickSkillTimes)
	{
		this.kickSkillTimes = kickSkillTimes;
	}
	
	
	/**
	 * @return the dangerousFoeBots
	 */
	@Override
	public List<FoeBotData> getDangerousFoeBots()
	{
		return dangerousFoeBots;
	}
	
	
	/**
	 * @return the ledData
	 */
	@Override
	public Map<BotID, LedControl> getLedData()
	{
		return ledData;
	}
	
	
	/**
	 * @param ledData the ledData to set
	 */
	public void setLedData(final Map<BotID, LedControl> ledData)
	{
		this.ledData = ledData;
	}
	
	
	/**
	 * @return the ballDistancePoints
	 */
	@Override
	public List<ValuePoint> getBallDistancePoints()
	{
		return ballDistancePoints;
	}
}
