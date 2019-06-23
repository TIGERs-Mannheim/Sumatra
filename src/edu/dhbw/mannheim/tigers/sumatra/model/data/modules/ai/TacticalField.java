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
import java.util.SortedMap;

import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.Size;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.RoleFinderInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveAction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.data.AdvancedPassTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * This class should be used to store and combine all tactical information
 * calculated by {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis} and its calculators.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 23)
public class TacticalField implements ITacticalField
{
	private List<FoeBotData>											dangerousFoeBots				= new ArrayList<FoeBotData>();
	
	private boolean														needTwoForBallBlock			= true;
	
	@NotNull
	@Size(min = 1, profiles = { "fullAiFrame" })
	private List<ValuePoint>											goalValuePoints;
	private ValuePoint													bestDirectShotTarget			= new ValuePoint(
																															Vector2.ZERO_VECTOR);
	
	@NotNull
	private Map<BotID, BotDistance>									tigersToBallDist;
	
	@NotNull
	private Map<BotID, BotDistance>									enemiesToBallDist;
	
	/** Which bot (opponent and Tigers) has the ball? */
	@NotNull
	private BallPossession												ballPossession;
	
	/** Was there possibly a goal? */
	@NotNull
	private EPossibleGoal												possibleGoal;
	
	/** Bot who was touching ball the last time, not null if once a bot has touched the ball */
	private BotID															botLastTouchedBall;
	/** Bot who is touching ball, can be null if no one is touching ball */
	private BotID															botTouchedBall;
	
	private transient ValuedField										supporterValuedField;
	
	private Map<BotID, BotAiInformation>							botAiInformation;
	
	private EGameState													gameState						= EGameState.UNKNOWN;
	/** Flag for a goal scored (tigers or foes), used for forcing all bots on our side before prepare kickoff signal */
	private boolean														goalScored						= false;
	
	private IVector2														ballLeftFieldPos				= null;
	
	private Map<BotID, OffensiveMovePosition>						offenseMovePositionsScored;
	
	private Map<BotID, OffensiveAction>								offensiveActions;
	
	/** Statistics object */
	private Statistics													statistics;
	
	private OffensiveStrategy											offensiveStrategy;
	
	private Map<EPlay, RoleFinderInfo>								roleFinderInfos;
	
	private EGameBehavior												gameBehavior					= EGameBehavior.OFFENSIVE;
	
	private Map<EDrawableShapesLayer, List<IDrawableShape>>	drawableShapes;
	private List<AdvancedPassTarget>									advancedPassTargetsRanked;
	
	private Map<ELetter, List<IVector2>>							letters;
	
	private Map<BotID, SortedMap<Long, IVector2>>				botPosBuffer;
	private List<TrackedBall>											ballBuffer;
	
	private Map<ECalculator, Integer>								metisCalcTimes;
	
	private List<IVector2>												topGpuGridPositions;
	private Map<BotID, IVector2>										supportPositionsV2;
	
	private transient MultiTeamMessage								multiTeamMessage;
	private boolean														mixedTeamBothTouchedBall	= false;
	
	
	private TacticalField()
	{
		goalValuePoints = new ArrayList<ValuePoint>();
		
		ballPossession = new BallPossession();
		possibleGoal = EPossibleGoal.NO_ONE;
		
		tigersToBallDist = new LinkedHashMap<BotID, BotDistance>();
		enemiesToBallDist = new LinkedHashMap<BotID, BotDistance>();
		
		botLastTouchedBall = BotID.createBotId();
		
		offenseMovePositionsScored = new HashMap<BotID, OffensiveMovePosition>();
		botAiInformation = new HashMap<BotID, BotAiInformation>();
		
		statistics = new Statistics();
		
		roleFinderInfos = new LinkedHashMap<EPlay, RoleFinderInfo>();
		drawableShapes = new HashMap<>();
		for (EDrawableShapesLayer l : EDrawableShapesLayer.values())
		{
			drawableShapes.put(l, new ArrayList<IDrawableShape>(0));
		}
		
		advancedPassTargetsRanked = new ArrayList<>();
		
		letters = new HashMap<>();
		botPosBuffer = new HashMap<>(0);
		ballBuffer = new ArrayList<>(0);
		metisCalcTimes = new HashMap<ECalculator, Integer>(ECalculator.values().length);
		topGpuGridPositions = new ArrayList<>();
		supportPositionsV2 = new HashMap<BotID, IVector2>();
	}
	
	
	/**
	 * @param worldFrame
	 */
	public TacticalField(final WorldFrame worldFrame)
	{
		this();
	}
	
	
	@Override
	public final void cleanup()
	{
		supporterValuedField = null;
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
	 * @return Move Positions OffenseRole
	 */
	@Override
	public final Map<BotID, OffensiveMovePosition> getOffenseMovePositions()
	{
		return offenseMovePositionsScored;
	}
	
	
	/**
	 * @param offenseMovePositions Positions for the offenserole to set
	 */
	public final void setOffenseMovePositions(final Map<BotID, OffensiveMovePosition> offenseMovePositions)
	{
		offenseMovePositionsScored = offenseMovePositions;
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
	 * @param offensiveActions Actions for the offenserole to set
	 */
	public final void setOffensiveActions(final Map<BotID, OffensiveAction> offensiveActions)
	{
		this.offensiveActions = offensiveActions;
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
	public Statistics getStatistics()
	{
		return statistics;
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
	 * @return the dangerousFoeBots
	 */
	@Override
	public List<FoeBotData> getDangerousFoeBots()
	{
		return dangerousFoeBots;
	}
	
	
	/**
	 * @param dangerousFoeBots the dangerousFoeBots to set
	 */
	public void setDangerousFoeBots(final List<FoeBotData> dangerousFoeBots)
	{
		this.dangerousFoeBots = dangerousFoeBots;
	}
	
	
	/**
	 * @return the drawableShapes
	 */
	@Override
	public Map<EDrawableShapesLayer, List<IDrawableShape>> getDrawableShapes()
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
	 * @return the letters
	 */
	@Override
	public Map<ELetter, List<IVector2>> getLetters()
	{
		return letters;
	}
	
	
	/**
	 * @return the botBuffer
	 */
	@Override
	public final Map<BotID, SortedMap<Long, IVector2>> getBotPosBuffer()
	{
		return botPosBuffer;
	}
	
	
	/**
	 * @return the ballBuffer
	 */
	@Override
	public final List<TrackedBall> getBallBuffer()
	{
		return ballBuffer;
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
	 * @return the needTwoForBallBlock
	 */
	@Override
	public boolean needTwoForBallBlock()
	{
		return needTwoForBallBlock;
	}
	
	
	/**
	 * @param needTwoForBallBlock the needTwoForBallBlock to set
	 */
	@Override
	public void setNeedTwoForBallBlock(final boolean needTwoForBallBlock)
	{
		this.needTwoForBallBlock = needTwoForBallBlock;
	}
	
	
	/**
	 * @return the topGpuGridPositions
	 */
	@Override
	public final List<IVector2> getTopGpuGridPositions()
	{
		return topGpuGridPositions;
	}
	
	
	@Override
	public Map<BotID, IVector2> getSupportPositions()
	{
		return supportPositionsV2;
	}
	
	
	/**
	 * @param supportPositions
	 */
	public void setSupportPositions(final Map<BotID, IVector2> supportPositions)
	{
		supportPositionsV2 = supportPositions;
	}
	
	
	@Override
	public MultiTeamMessage getMultiTeamMessage()
	{
		return multiTeamMessage;
	}
	
	
	@Override
	public void setMultiTeamMessage(final MultiTeamMessage message)
	{
		multiTeamMessage = message;
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
}
