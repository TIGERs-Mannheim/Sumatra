/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.tigers.sumatra.ai.data.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.ECalculator;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.keeper.KeeperStateCalc;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OngoingPassInfo;
import edu.tigers.sumatra.ai.metis.offense.data.PenaltyPlacementTargetGroup;
import edu.tigers.sumatra.ai.metis.offense.data.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.PassTarget;
import edu.tigers.sumatra.ai.metis.support.SupportPosition;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.botmanager.commands.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.vision.data.IKickEvent;
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
	private Map<EPlay, RoleFinderInfo> roleFinderInfos = Collections.emptyMap();
	private List<IPassTarget> passTargetsRanked = Collections.emptyList();
	private List<PassTarget> allPassTargets = Collections.emptyList();
	private Map<ECalculator, Integer> metisCalcTimes = Collections.emptyMap();
	private Map<ECalculator, Boolean> metisExecutionStatus = new EnumMap<>(ECalculator.class);
	private Map<BotID, OffensiveTimeEstimation> offensiveTimeEstimations = Collections.emptyMap();
	private OngoingPassInfo ongoingPassInfo = null;
	private List<DefenseThreatAssignment> defenseThreatAssignments = Collections.emptyList();
	private Set<BotID> crucialDefender = Collections.emptySet();
	private Set<BotID> crucialOffender = Collections.emptySet();
	private Map<BotID, ValuePoint> bestDirectShotTargetsForTigerBots = Collections.emptyMap();
	private ValuePoint bestDirectShotTarget = new ValuePoint(
			Geometry.getGoalTheir().getCenter());
	private BallPossession ballPossession = new BallPossession();
	private EPossibleGoal possibleGoal = EPossibleGoal.NO_ONE;
	private BotID botLastTouchedBall = BotID.noBot();
	private BotID lastBotCloseToBall = BotID.noBot();
	private BotID botTouchedBall;
	private BotID botNotAllowedToTouchBall = BotID.noBot();
	private Optional<IKickEvent> directShot = Optional.empty();
	private Optional<IKickEvent> kicking = Optional.empty();
	private boolean opponentWillDoIcing = false;
	private GameState gameState = GameState.HALT;
	private boolean goalScored = false;
	private int numDefender = 0;
	private List<DefenseBotThreat> defenseBotThreats = Collections.emptyList();
	private DefenseBallThreat defenseBallThreat = null;
	private Map<EPlay, Integer> playNumbers = new EnumMap<>(EPlay.class);
	private IVector2 ballLeftFieldPos = null;
	private OffensiveStrategy offensiveStrategy = new OffensiveStrategy();
	private boolean mixedTeamBothTouchedBall = false;
	private AutomatedThrowInInfo throwInInfo = new AutomatedThrowInInfo();
	private KeeperStateCalc.EKeeperState keeperState = KeeperStateCalc.EKeeperState.MOVE_TO_PENALTY_AREA;
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
	private MultiTeamPlan multiTeamPlan = new MultiTeamPlan();
	private List<PenaltyPlacementTargetGroup> penaltyPlacementTargetGroups = Collections.emptyList();
	private PenaltyPlacementTargetGroup filteredPenaltyPlacementTargetGroup = null;
	
	
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
	
	
	@Override
	public BotID getLastBotCloseToBall()
	{
		return lastBotCloseToBall;
	}
	
	
	/**
	 * @param lastBotCloseToBall the bot that was last closest to ball (but may not have touched it
	 */
	public void setLastBotCloseToBall(final BotID lastBotCloseToBall)
	{
		this.lastBotCloseToBall = lastBotCloseToBall;
	}
	
	
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
	public Optional<IKickEvent> getDirectShot()
	{
		return directShot;
	}
	
	
	/**
	 * @param directShot sets KickEvent for direct Shots
	 */
	public void setDirectShot(final Optional<IKickEvent> directShot)
	{
		this.directShot = directShot;
	}
	
	
	/**
	 * @param kicking sets KickEvent for detected kicks
	 */
	public void setKicking(final Optional<IKickEvent> kicking)
	{
		this.kicking = kicking;
	}
	
	
	@Override
	public Optional<IKickEvent> getKicking()
	{
		return kicking;
	}
	
	
	@Override
	public boolean isOpponentWillDoIcing()
	{
		return opponentWillDoIcing;
	}
	
	
	/**
	 * @param opponentWillDoIcing true if icing rule will surely be violated by opponent team
	 */
	public void setOpponentWillDoIcing(final boolean opponentWillDoIcing)
	{
		this.opponentWillDoIcing = opponentWillDoIcing;
	}
	
	
	@Override
	public final ValuePoint getBestDirectShotTarget()
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
	
	
	@Override
	public Map<BotID, ValuePoint> getBestDirectShotTargetsForTigerBots()
	{
		return bestDirectShotTargetsForTigerBots;
	}
	
	
	public void setBestDirectShotTargetsForTigerBots(final Map<BotID, ValuePoint> bestDirectShotTargetsForTigerBots)
	{
		this.bestDirectShotTargetsForTigerBots = Collections.unmodifiableMap(bestDirectShotTargetsForTigerBots);
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
		offensiveStrategy = strategy;
	}
	
	
	@Override
	public Map<EPlay, RoleFinderInfo> getRoleFinderInfos()
	{
		return roleFinderInfos;
	}
	
	
	public void setRoleFinderInfos(final Map<EPlay, RoleFinderInfo> roleFinderInfos)
	{
		// do not make this unmodifiable as it is modified by TestModeAdapter
		this.roleFinderInfos = roleFinderInfos;
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
	public List<IPassTarget> getPassTargetsRanked()
	{
		return passTargetsRanked;
	}
	
	
	public void setPassTargetsRanked(final List<IPassTarget> passTargetsRanked)
	{
		this.passTargetsRanked = Collections.unmodifiableList(passTargetsRanked);
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
	
	
	@Override
	public KeeperStateCalc.EKeeperState getKeeperState()
	{
		return keeperState;
	}
	
	
	/**
	 * @param keeperState the keeperState to set
	 */
	public void setKeeperState(final KeeperStateCalc.EKeeperState keeperState)
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
	
	
	/**
	 * @return the time estimations
	 */
	@Override
	public Map<BotID, OffensiveTimeEstimation> getOffensiveTimeEstimations()
	{
		return offensiveTimeEstimations;
	}
	
	
	public void setOffensiveTimeEstimations(final Map<BotID, OffensiveTimeEstimation> offensiveTimeEstimations)
	{
		this.offensiveTimeEstimations = Collections.unmodifiableMap(offensiveTimeEstimations);
	}
	
	
	@Override
	public Optional<OngoingPassInfo> getOngoingPassInfo()
	{
		return Optional.ofNullable(ongoingPassInfo);
	}
	
	
	public void setOngoingPassInfo(OngoingPassInfo info)
	{
		ongoingPassInfo = info;
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
	
	
	public List<PassTarget> getAllPassTargets()
	{
		return allPassTargets;
	}
	
	
	public void setAllPassTargets(List<PassTarget> allPassTargets)
	{
		this.allPassTargets = allPassTargets;
	}
	
	
	@Override
	public int getNumDefender()
	{
		return numDefender;
	}
	
	
	/**
	 * Sets the number of defenders assigned by NDefenderCalc.
	 *
	 * @param nDefender
	 */
	public void setNumDefender(final int nDefender)
	{
		numDefender = nDefender;
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
		return playNumbers;
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
	public MultiTeamPlan getMultiTeamPlan()
	{
		return multiTeamPlan;
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
	
}
