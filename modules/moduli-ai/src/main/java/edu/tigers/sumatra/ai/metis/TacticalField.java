/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import edu.tigers.sumatra.SslGameControllerTeam.TeamToController.AdvantageResponse;
import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallToBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.goal.EPossibleGoal;
import edu.tigers.sumatra.ai.metis.interchange.BotInterchange;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.situation.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterception;
import edu.tigers.sumatra.ai.metis.offense.kickoff.KickoffStrategy;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.offense.strategy.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.ai.metis.shootout.PenaltyPlacementTargetGroup;
import edu.tigers.sumatra.ai.metis.statistics.MatchStats;
import edu.tigers.sumatra.ai.metis.support.SupportPosition;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.RatedPassTargetNoScore;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.botmanager.botskills.data.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.trees.EOffensiveSituation;
import edu.tigers.sumatra.trees.OffensiveActionTreeMap;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * <p>
 * This class should be used to store and combine all tactical information
 * calculated by {@link edu.tigers.sumatra.ai.metis.Metis} and its calculators.
 * </p>
 * <p>
 * This class itself is mutable. The interface {@link ITacticalField} ideally provides a immutable view on the data.
 * This way, after Metis, no one can modify the data, which would cause confusion about when data is written and read.
 * </p>
 * <p>
 * Here are some hints for adding new fields:
 * <ul>
 * <li>Initialize the field with a meaningful default value</li>
 * <li>The field should be immutable. Use Collections util for list/maps/sets etc.</li>
 * <li>Add a getter in {@link ITacticalField}</li>
 * <li>Add a setter here</li>
 * <li>Do not write custom code here. Only simple getters/setters</li>
 * </ul>
 * </p>
 *
 * @author many
 */
public class TacticalField implements ITacticalField
{
	private final MatchStats matchStatistics = new MatchStats();
	private final ShapeMap drawableShapes = new ShapeMap();
	private List<BotDistance> tigersToBallDist = Collections.emptyList();
	private List<BotDistance> enemiesToBallDist = Collections.emptyList();
	private Map<BotID, OffensiveAction> offensiveActions = Collections.emptyMap();
	private Map<EPlay, RoleMapping> roleMapping = Collections.emptyMap();

	private List<IRatedPassTarget> ratedPassTargetsRanked = Collections.emptyList();
	private List<IPassTarget> allPassTargets = Collections.emptyList();
	private List<RatedPassTargetNoScore> allRatedPassTargetsNoScore = Collections.emptyList();

	private Map<ECalculator, Integer> metisCalcTimes = Collections.emptyMap();
	private Map<ECalculator, Boolean> metisExecutionStatus = new EnumMap<>(ECalculator.class);
	private Map<BotID, BallInterception> ballInterceptions = Collections.emptyMap();
	private List<DefenseThreatAssignment> defenseThreatAssignments = Collections.emptyList();
	private Set<BotID> crucialDefender = Collections.emptySet();
	private Set<BotID> crucialOffender = Collections.emptySet();
	private Map<BotID, Optional<IRatedTarget>> bestGoalKickTargetForBot = Collections.emptyMap();
	private IRatedTarget bestGoalKickTarget = null;
	private BallPossession ballPossession = new BallPossession();
	private EPossibleGoal possibleGoal = EPossibleGoal.NO_ONE;
	private Set<BotID> botsLastTouchedBall = Collections.emptySet();
	private Set<BotID> botsTouchingBall = Collections.emptySet();
	private BotID botNotAllowedToTouchBall = BotID.noBot();
	private Map<ETeam, IKickEvent> detectedGoalKick = new EnumMap<>(ETeam.class);
	private boolean ballLeavingFieldGood = false;
	private GameState gameState = GameState.HALT;
	private boolean goalScored = false;
	private int numDefenderForBall = 0;
	private int numDefender = 0;
	private List<DefenseBotThreat> defenseBotThreats = Collections.emptyList();
	private List<DefenseBallToBotThreat> defenseBallToBotThreats = Collections.emptyList();
	private DefenseBallThreat defenseBallThreat = null;
	private Map<EPlay, Integer> playNumbers = new EnumMap<>(EPlay.class);
	private BallLeftFieldPosition ballLeftFieldPos = null;
	private OffensiveStrategy offensiveStrategy = new OffensiveStrategy();
	private Set<BotID> potentialOffensiveBots = new HashSet<>();
	private EKeeperState keeperState = EKeeperState.MOVE_TO_PENALTY_AREA;
	private boolean isBotInterferingKeeperChip = false;
	private Map<BotID, MultimediaControl> multimediaControl = Collections.emptyMap();
	private SkirmishInformation skirmishInformation = new SkirmishInformation();
	private OffensiveStatisticsFrame offensiveStatistics = null;
	private OffensiveAnalysedFrame analyzedOffensiveStatisticsFrame = null;
	private KickoffStrategy kickoffStrategy = new KickoffStrategy();
	private IVector2 supportiveAttackerMovePos = null;
	private IVector2 chipKickTarget = null;
	private ITrackedBot chipKickTargetBot = null;
	private ITrackedBot opponentPassReceiver = null;
	private List<SupportPosition> globalSupportPositions = Collections.emptyList();
	private List<SupportPosition> selectedSupportPositions = Collections.emptyList();
	private EBallResponsibility ballResponsibility = EBallResponsibility.UNKNOWN;
	private Map<EPlay, Set<BotID>> desiredBotMap = new EnumMap<>(EPlay.class);
	private List<PenaltyPlacementTargetGroup> penaltyPlacementTargetGroups = Collections.emptyList();
	private PenaltyPlacementTargetGroup filteredPenaltyPlacementTargetGroup = null;
	private OffensiveActionTreeMap actionTrees = null;
	private OffensiveActionTreePath currentPath = new OffensiveActionTreePath();
	private EOffensiveSituation currentSituation = EOffensiveSituation.DEFAULT_SITUATION;
	private List<ICircle> freeSpots = Collections.emptyList();
	private IAiInfoForNextFrame aiInfoForNextFrame = new AICom();
	private IAiInfoFromPrevFrame aiInfoFromPrevFrame = new AICom();
	private BotInterchange botInterchange = new BotInterchange();
	private boolean ballInPushRadius = false;
	private double keeperRamboDistance = 2000;
	private int availableAttackers = 0;
	private List<IVector2> supportiveGoalPositions = Collections.emptyList();
	private RedirectorDetectionInformation redirectorDetectionInformation = null;
	private List<IArc> offensiveShadows = Collections.emptyList();
	private AdvantageResponse advantageChoice = AdvantageResponse.UNDECIDED;
	private Map<BotID, List<AngleRange>> unaccessibleBallAngles = Collections.emptyMap();
	private boolean insaneKeeper = false;


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
		ballPossession = newBallPossession;
	}


	@Override
	public List<BotDistance> getTigersToBallDist()
	{
		return tigersToBallDist;
	}


	/**
	 * @param tigersToBallDist
	 */
	public void setTigersToBallDist(final List<BotDistance> tigersToBallDist)
	{
		this.tigersToBallDist = Collections.unmodifiableList(tigersToBallDist);
	}


	@Override
	public BotDistance getTigerClosestToBall()
	{
		if (!tigersToBallDist.isEmpty())
		{
			return tigersToBallDist.get(0);
		}
		return BotDistance.NULL_BOT_DISTANCE;
	}


	@Override
	public List<BotDistance> getEnemiesToBallDist()
	{
		return enemiesToBallDist;
	}


	/**
	 * @param enemiesToBallDist
	 */
	public void setEnemiesToBallDist(final List<BotDistance> enemiesToBallDist)
	{
		this.enemiesToBallDist = Collections.unmodifiableList(enemiesToBallDist);
	}


	@Override
	public BotDistance getEnemyClosestToBall()
	{
		if (!enemiesToBallDist.isEmpty())
		{
			return enemiesToBallDist.get(0);
		}
		return BotDistance.NULL_BOT_DISTANCE;
	}


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


	@Override
	public Set<BotID> getBotsLastTouchedBall()
	{
		return botsLastTouchedBall;
	}


	/**
	 * @param botsLastTouchedBall the botsLastTouchedBall to set
	 */
	public void setBotsLastTouchedBall(final Set<BotID> botsLastTouchedBall)
	{
		this.botsLastTouchedBall = Collections.unmodifiableSet(botsLastTouchedBall);
	}


	@Override
	public Set<BotID> getBotsTouchingBall()
	{
		return botsTouchingBall;
	}


	/**
	 * @param botsTouchingBall the botsTouchingBall to set
	 */
	public void setBotsTouchingBall(final Set<BotID> botsTouchingBall)
	{
		this.botsTouchingBall = Collections.unmodifiableSet(botsTouchingBall);
	}


	@Override
	public BotID getBotNotAllowedToTouchBall()
	{
		return botNotAllowedToTouchBall;
	}


	/**
	 * @param botNotAllowedToTouchBall set bot that just kicked ball to get game running > double touch avoidance
	 */
	public void setBotNotAllowedToTouchBall(final BotID botNotAllowedToTouchBall)
	{
		this.botNotAllowedToTouchBall = botNotAllowedToTouchBall;
	}


	@Override
	public Optional<IKickEvent> getDetectedGoalKick(ETeam team)
	{
		return Optional.ofNullable(detectedGoalKick.get(team));
	}


	/**
	 * @param detectedGoalKick sets KickEvent for direct Shots
	 * @param team the attacking team
	 */
	public void setDetectedGoalKick(final IKickEvent detectedGoalKick, ETeam team)
	{
		this.detectedGoalKick.put(team, detectedGoalKick);
	}


	@Override
	public boolean isBallLeavingFieldGood()
	{
		return ballLeavingFieldGood;
	}


	/**
	 * @param opponentWillDoIcing true if icing rule will surely be violated by opponent team
	 */
	public void setBallLeavingFieldGood(final boolean opponentWillDoIcing)
	{
		this.ballLeavingFieldGood = opponentWillDoIcing;
	}


	@Override
	public final Optional<IRatedTarget> getBestGoalKickTarget()
	{
		return Optional.ofNullable(bestGoalKickTarget);
	}


	/**
	 * @param bestDirectShootTarget the bestDirectShootTarget to set
	 */
	public final void setBestGoalKickTarget(final IRatedTarget bestDirectShootTarget)
	{
		bestGoalKickTarget = bestDirectShootTarget;
	}


	@Override
	public Map<BotID, Optional<IRatedTarget>> getBestGoalKickTargetForBot()
	{
		return bestGoalKickTargetForBot;
	}


	public void setBestGoalKickTargetForBot(
			final Map<BotID, Optional<IRatedTarget>> bestGoalKickTargetForBot)
	{
		this.bestGoalKickTargetForBot = Collections.unmodifiableMap(bestGoalKickTargetForBot);
	}


	@Override
	public final Map<BotID, OffensiveAction> getOffensiveActions()
	{
		return offensiveActions;
	}


	public void setOffensiveActions(final Map<BotID, OffensiveAction> offensiveActions)
	{
		this.offensiveActions = Collections.unmodifiableMap(offensiveActions);
	}


	@Override
	public final GameState getGameState()
	{
		return gameState;
	}


	/**
	 * @param gameState the gameState to set
	 */
	public final void setGameState(final GameState gameState)
	{
		this.gameState = gameState;
	}


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


	@Override
	public final BallLeftFieldPosition getBallLeftFieldPos()
	{
		return ballLeftFieldPos;
	}


	/**
	 * @param ballLeftFieldPos the ballLeftFieldPos to set
	 */
	public final void setBallLeftFieldPos(final BallLeftFieldPosition ballLeftFieldPos)
	{
		this.ballLeftFieldPos = ballLeftFieldPos;
	}


	@Override
	public MatchStats getMatchStatistics()
	{
		return matchStatistics;
	}


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
		Objects.requireNonNull(strategy);
		offensiveStrategy = strategy;
	}


	@Override
	public Set<BotID> getPotentialOffensiveBots()
	{
		return potentialOffensiveBots;
	}


	public void setPotentialOffensiveBots(final Set<BotID> potentialOffensiveBots)
	{
		this.potentialOffensiveBots = Collections.unmodifiableSet(potentialOffensiveBots);
	}


	@Override
	public Map<EPlay, RoleMapping> getRoleMapping()
	{
		return roleMapping;
	}


	public void setRoleMapping(final Map<EPlay, RoleMapping> roleMapping)
	{
		this.roleMapping = Collections.unmodifiableMap(roleMapping);
	}


	@Override
	public Set<BotID> getCrucialDefender()
	{
		return crucialDefender;
	}


	public void setCrucialDefender(final Set<BotID> crucialDefender)
	{
		this.crucialDefender = Collections.unmodifiableSet(crucialDefender);
	}


	@Override
	public Set<BotID> getCrucialOffender()
	{
		return crucialOffender;
	}


	public void setCrucialOffender(final Set<BotID> crucialOffender)
	{
		this.crucialOffender = Collections.unmodifiableSet(crucialOffender);
	}


	@Override
	public ShapeMap getDrawableShapes()
	{
		return drawableShapes;
	}


	@Override
	public List<IRatedPassTarget> getRatedPassTargetsRanked()
	{
		return ratedPassTargetsRanked;
	}


	public void setRatedPassTargetsRanked(final List<IRatedPassTarget> ratedPassTargetsRanked)
	{
		this.ratedPassTargetsRanked = Collections.unmodifiableList(ratedPassTargetsRanked);
	}


	@Override
	public Map<ECalculator, Integer> getMetisCalcTimes()
	{
		return Collections.unmodifiableMap(metisCalcTimes);
	}


	public void setMetisExecutionStatus(Map<ECalculator, Boolean> statusMap)
	{
		this.metisExecutionStatus = statusMap;
	}


	@Override
	public Map<ECalculator, Boolean> getMetisExecutionStatus()
	{
		return Collections.unmodifiableMap(metisExecutionStatus);
	}


	public void setMetisCalcTimes(final Map<ECalculator, Integer> metisCalcTimes)
	{
		this.metisCalcTimes = Collections.unmodifiableMap(metisCalcTimes);
	}


	@Override
	public EKeeperState getKeeperState()
	{
		return keeperState;
	}


	/**
	 * @param keeperState the keeperState to set
	 */
	public void setKeeperState(final EKeeperState keeperState)
	{
		this.keeperState = keeperState;
	}


	@Override
	public Map<BotID, MultimediaControl> getMultimediaControl()
	{
		return multimediaControl;
	}


	public void setMultimediaControl(final Map<BotID, MultimediaControl> multimediaControl)
	{
		this.multimediaControl = Collections.unmodifiableMap(multimediaControl);
	}


	@Override
	public SkirmishInformation getSkirmishInformation()
	{
		return skirmishInformation;
	}


	/**
	 * @param skirmishInformation the skirmishInformation to set
	 */
	public void setSkirmishInformation(final SkirmishInformation skirmishInformation)
	{
		this.skirmishInformation = skirmishInformation;
	}


	/**
	 * Note: the frames are only stored if the offensiveStatistics are activated in the config
	 *
	 * @return
	 */
	@Override
	public OffensiveStatisticsFrame getOffensiveStatistics()
	{
		return offensiveStatistics;
	}


	/**
	 * @param offensiveStatistics
	 */
	public void setOffensiveStatistics(final OffensiveStatisticsFrame offensiveStatistics)
	{
		this.offensiveStatistics = offensiveStatistics;
	}


	/**
	 * @return
	 */
	@Override
	public OffensiveAnalysedFrame getAnalyzedOffensiveStatisticsFrame()
	{
		return analyzedOffensiveStatisticsFrame;
	}


	/**
	 * @param analyzedOffensiveStatisticsFrame
	 */
	public void setAnalyzedOffensiveStatisticsFrame(final OffensiveAnalysedFrame analyzedOffensiveStatisticsFrame)
	{
		this.analyzedOffensiveStatisticsFrame = analyzedOffensiveStatisticsFrame;
	}


	@Override
	public Map<BotID, BallInterception> getBallInterceptions()
	{
		return ballInterceptions;
	}


	public void setBallInterceptions(final Map<BotID, BallInterception> ballInterceptions)
	{
		this.ballInterceptions = Collections.unmodifiableMap(ballInterceptions);
	}


	@Override
	public boolean isBotInterferingKeeperChip()
	{
		return isBotInterferingKeeperChip;
	}


	public void setBotInterferingKeeperChip(final boolean botInterferingKeeperChip)
	{
		isBotInterferingKeeperChip = botInterferingKeeperChip;
	}


	@Override
	public KickoffStrategy getKickoffStrategy()
	{
		return kickoffStrategy;
	}


	public void setKickoffStrategy(final KickoffStrategy kickoffStrategy)
	{
		this.kickoffStrategy = kickoffStrategy;
	}


	@Override
	public IVector2 getSupportiveAttackerMovePos()
	{
		return supportiveAttackerMovePos;
	}


	public void setSupportiveAttackerMovePos(final IVector2 supportiveAttackerMovePos)
	{
		this.supportiveAttackerMovePos = supportiveAttackerMovePos;
	}


	public List<SupportPosition> getGlobalSupportPositions()
	{
		return globalSupportPositions;
	}


	public void setGlobalSupportPositions(final List<SupportPosition> pos)
	{
		globalSupportPositions = Collections.unmodifiableList(pos);
	}


	public List<IPassTarget> getAllPassTargets()
	{
		return allPassTargets;
	}


	public void setAllPassTargets(List<IPassTarget> allPassTargets)
	{
		this.allPassTargets = allPassTargets;
	}


	public List<RatedPassTargetNoScore> getAllRatedPassTargetsNoScore()
	{
		return allRatedPassTargetsNoScore;
	}


	public void setAllRatedPassTargetsNoScore(final List<RatedPassTargetNoScore> allRatedPassTargetsNoScore)
	{
		this.allRatedPassTargetsNoScore = allRatedPassTargetsNoScore;
	}


	/**
	 * Returns the number of defenders calculated by the NumDefenderCalc
	 *
	 * @return nDefender
	 */
	public int getNumDefender()
	{
		return numDefender;
	}


	/**
	 * Sets the number of defenders assigned by NumDefenderCalc.
	 *
	 * @param nDefender
	 */
	public void setNumDefender(final int nDefender)
	{
		numDefender = nDefender;
	}


	/**
	 * The number of defenders that should be assigned to the ball.
	 * This number may be higher than the total number of defenders.
	 * It is an input for determining the total number of defenders.
	 *
	 * @return number of defenders that should be assigned to the ball
	 */
	public int getNumDefenderForBall()
	{
		return numDefenderForBall;
	}


	public void setNumDefenderForBall(final int numDefenderForBall)
	{
		this.numDefenderForBall = numDefenderForBall;
	}


	@Override
	public IVector2 getChipKickTarget()
	{
		return chipKickTarget;
	}


	public void setChipKickTarget(final IVector2 target)
	{
		chipKickTarget = target;
	}


	@Override
	public ITrackedBot getChipKickTargetBot()
	{
		return chipKickTargetBot;
	}


	public void setChipKickTargetBot(final ITrackedBot bot)
	{
		chipKickTargetBot = bot;
	}


	@Override
	public Optional<ITrackedBot> getOpponentPassReceiver()
	{
		return Optional.ofNullable(opponentPassReceiver);
	}


	public void setOpponentPassReceiver(final ITrackedBot bot)
	{
		opponentPassReceiver = bot;
	}


	@Override
	public List<DefenseThreatAssignment> getDefenseThreatAssignments()
	{
		return defenseThreatAssignments;
	}


	public void setDefenseThreatAssignments(final List<DefenseThreatAssignment> defenseThreatAssignments)
	{
		this.defenseThreatAssignments = Collections.unmodifiableList(defenseThreatAssignments);
	}


	@Override
	public List<DefenseBotThreat> getDefenseBotThreats()
	{
		return defenseBotThreats;
	}


	/**
	 * @param defenseBotThreats the defense threads
	 */
	public void setDefenseBotThreats(final List<DefenseBotThreat> defenseBotThreats)
	{
		this.defenseBotThreats = Collections.unmodifiableList(defenseBotThreats);
	}


	@Override
	public List<DefenseBallToBotThreat> getDefenseBallToBotThreats()
	{
		return defenseBallToBotThreats;
	}


	public void setDefenseBallToBotThreats(final List<DefenseBallToBotThreat> defenseBallToBotThreats)
	{
		this.defenseBallToBotThreats = Collections.unmodifiableList(defenseBallToBotThreats);
	}


	@Override
	public DefenseBallThreat getDefenseBallThreat()
	{
		return defenseBallThreat;
	}


	public void setDefenseBallThreat(final DefenseBallThreat defenseBallThreat)
	{
		this.defenseBallThreat = defenseBallThreat;
	}


	@Override
	public final Map<EPlay, Set<BotID>> getDesiredBotMap()
	{
		return Collections.unmodifiableMap(desiredBotMap);
	}


	/**
	 * Adds the bots to the desiredBotMap
	 *
	 * @param play
	 * @param bots
	 */
	public void addDesiredBots(final EPlay play, final Set<BotID> bots)
	{
		desiredBotMap.put(play, bots);
	}


	@Override
	public EBallResponsibility getBallResponsibility()
	{
		return ballResponsibility;
	}


	/**
	 * @param ballResponsibility the ball responsibility
	 */
	public void setBallResponsibility(final EBallResponsibility ballResponsibility)
	{
		this.ballResponsibility = ballResponsibility;
	}


	@Override
	public Map<EPlay, Integer> getPlayNumbers()
	{
		return Collections.unmodifiableMap(playNumbers);
	}


	public void putPlayNumbers(EPlay play, int number)
	{
		playNumbers.put(play, number);
	}


	@Override
	public List<SupportPosition> getSelectedSupportPositions()
	{
		return selectedSupportPositions;
	}


	public void setSelectedSupportPositions(final List<SupportPosition> selectedSupportPositions)
	{
		this.selectedSupportPositions = Collections.unmodifiableList(selectedSupportPositions);
	}


	@Override
	public List<PenaltyPlacementTargetGroup> getPenaltyPlacementTargetGroups()
	{
		return penaltyPlacementTargetGroups;
	}


	public void setPenaltyPlacementTargetGroups(final List<PenaltyPlacementTargetGroup> penaltyPlacementTargetGroups)
	{
		this.penaltyPlacementTargetGroups = penaltyPlacementTargetGroups;
	}


	@Override
	public PenaltyPlacementTargetGroup getFilteredPenaltyPlacementTargetGroup()
	{
		return filteredPenaltyPlacementTargetGroup;
	}


	public void setFilteredPenaltyPlacementTargetGroup(final PenaltyPlacementTargetGroup group)
	{
		filteredPenaltyPlacementTargetGroup = group;
	}


	@Override
	public OffensiveActionTreePath getCurrentPath()
	{
		return currentPath;
	}


	public void setCurrentPath(final OffensiveActionTreePath currentPath)
	{
		this.currentPath = currentPath;
	}


	@Override
	public EOffensiveSituation getCurrentSituation()
	{
		return currentSituation;
	}


	public void setCurrentSituation(final EOffensiveSituation currentSituation)
	{
		this.currentSituation = currentSituation;
	}


	@Override
	public double getKeeperRamboDistance()
	{
		return keeperRamboDistance;
	}


	public void setKeeperRamboDistance(final double keeperRamboDistance)
	{
		this.keeperRamboDistance = keeperRamboDistance;
	}


	@Override
	public List<ICircle> getFreeSpots()
	{
		return freeSpots;
	}


	public void setFreeSpots(List<ICircle> freeSpots)
	{
		this.freeSpots = freeSpots;
	}


	@Override
	public IAiInfoForNextFrame getAiInfoForNextFrame()
	{
		return aiInfoForNextFrame;
	}


	@Override
	public IAiInfoFromPrevFrame getAiInfoFromPrevFrame()
	{
		return aiInfoFromPrevFrame;
	}


	public void setAiInfoFromPrevFrame(final IAiInfoFromPrevFrame aiInfoFromPrevFrame)
	{
		this.aiInfoFromPrevFrame = aiInfoFromPrevFrame;
	}


	@Override
	public BotInterchange getBotInterchange()
	{
		return botInterchange;
	}


	@Override
	public boolean isBallInPushRadius()
	{
		return ballInPushRadius;
	}


	/**
	 * @param ballInsidePushRadius
	 */
	public void setBallInPushRadius(boolean ballInsidePushRadius)
	{
		ballInPushRadius = ballInsidePushRadius;
	}


	public void setAvailableAttackers(final int nAvailableAttackers)
	{
		this.availableAttackers = nAvailableAttackers;
	}


	@Override
	public int getAvailableAttackers()
	{
		return availableAttackers;
	}


	@Override
	public List<IVector2> getSupportiveGoalPositions()
	{
		return supportiveGoalPositions;
	}


	public void setSupportiveGoalPositions(List<IVector2> positions)
	{
		supportiveGoalPositions = positions;
	}


	@Override
	public RedirectorDetectionInformation getRedirectorDetectionInformation()
	{
		return redirectorDetectionInformation;
	}


	public void setRedirectorDetectionInformation(final RedirectorDetectionInformation redirectorDetectionInformation)
	{
		this.redirectorDetectionInformation = redirectorDetectionInformation;
	}


	@Override
	public List<IArc> getOffensiveShadows()
	{
		return offensiveShadows;
	}


	public void setOffensiveShadows(List<IArc> shadows)
	{
		this.offensiveShadows = shadows;
	}


	@Override
	public OffensiveActionTreeMap getActionTrees()
	{
		return actionTrees;
	}


	public void setActionTrees(final OffensiveActionTreeMap actionTrees)
	{
		this.actionTrees = actionTrees;
	}


	@Override
	public AdvantageResponse getAdvantageChoice()
	{
		return advantageChoice;
	}


	public void setAdvantageChoice(final AdvantageResponse advantageResponse)
	{
		this.advantageChoice = advantageResponse;
	}


	@Override
	public Map<BotID, List<AngleRange>> getUnaccessibleBallAngles()
	{
		return Collections.unmodifiableMap(unaccessibleBallAngles);
	}


	public void setUnaccessibleBallAngles(final Map<BotID, List<AngleRange>> unaccessibleBallAngles)
	{
		this.unaccessibleBallAngles = unaccessibleBallAngles;
	}


	@Override
	public boolean isInsaneKeeper()
	{
		return insaneKeeper;
	}


	public void setInsaneKeeper(final boolean insaneKeeper)
	{
		this.insaneKeeper = insaneKeeper;
	}
}
