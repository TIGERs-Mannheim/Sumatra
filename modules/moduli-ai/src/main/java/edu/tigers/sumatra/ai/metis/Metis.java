/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField.TacticalFieldBuilder;
import edu.tigers.sumatra.ai.metis.ballplacement.BallPlacementBotsCalc;
import edu.tigers.sumatra.ai.metis.ballplacement.DesiredBallPlacementBotsCalc;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossessionCalc;
import edu.tigers.sumatra.ai.metis.ballresponsibility.BallResponsibilityCalc;
import edu.tigers.sumatra.ai.metis.botdistance.BotToBallDistanceCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBallThreatCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBotThreatCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBotThreatDefStrategyCenterBackCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBotThreatDefStrategyDataAccumulatorCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBotThreatDefStrategyMan2ManCalc;
import edu.tigers.sumatra.ai.metis.defense.DefensePassDisruptionCalc;
import edu.tigers.sumatra.ai.metis.defense.DefensePenAreaBoundaryCalc;
import edu.tigers.sumatra.ai.metis.defense.DefensePenAreaTargetGroupAssignerCalc;
import edu.tigers.sumatra.ai.metis.defense.DefensePenAreaTargetGroupFinderCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseThreatAssignmentSeparationCalc;
import edu.tigers.sumatra.ai.metis.defense.DesiredDefendersCalc;
import edu.tigers.sumatra.ai.metis.defense.KeeperPassThreatCalc;
import edu.tigers.sumatra.ai.metis.defense.NumDefenderCalc;
import edu.tigers.sumatra.ai.metis.defense.NumDefendersForBallCalc;
import edu.tigers.sumatra.ai.metis.defense.OpponentPassReceiverCalc;
import edu.tigers.sumatra.ai.metis.defense.SupporterMan2ManAssignmentCalc;
import edu.tigers.sumatra.ai.metis.defenseoffensecoordination.BallDefenseReadyCalc;
import edu.tigers.sumatra.ai.metis.defenseoffensecoordination.BestBallDefenderCandidatesCalc;
import edu.tigers.sumatra.ai.metis.defenseoffensecoordination.CrucialDefenderCalc;
import edu.tigers.sumatra.ai.metis.defenseoffensecoordination.CrucialOffenderCalc;
import edu.tigers.sumatra.ai.metis.defenseoffensecoordination.DefenseSupportingBallReceptionCalc;
import edu.tigers.sumatra.ai.metis.general.BallLeavingFieldCalc;
import edu.tigers.sumatra.ai.metis.general.BallLeftFieldCalc;
import edu.tigers.sumatra.ai.metis.general.BotBallContactCalc;
import edu.tigers.sumatra.ai.metis.general.DesiredBotsCalc;
import edu.tigers.sumatra.ai.metis.general.DesiredRemainingBotsCalc;
import edu.tigers.sumatra.ai.metis.general.DirectShotDetectionCalc;
import edu.tigers.sumatra.ai.metis.general.MultimediaCalc;
import edu.tigers.sumatra.ai.metis.general.OpponentRoleClassifierCalc;
import edu.tigers.sumatra.ai.metis.general.PathFinderPrioMapCalc;
import edu.tigers.sumatra.ai.metis.general.PlayNumberCalc;
import edu.tigers.sumatra.ai.metis.general.RoleStatemachinePublisherCalc;
import edu.tigers.sumatra.ai.metis.goal.PossibleGoalCalc;
import edu.tigers.sumatra.ai.metis.interchange.BotInterchangeCalc;
import edu.tigers.sumatra.ai.metis.interchange.DesiredInterchangeBotsCalc;
import edu.tigers.sumatra.ai.metis.interchange.WeakBotsCalc;
import edu.tigers.sumatra.ai.metis.keeper.DesiredKeeperCalc;
import edu.tigers.sumatra.ai.metis.keeper.KeeperBallInterceptionCalc;
import edu.tigers.sumatra.ai.metis.keeper.KeeperBehaviorCalc;
import edu.tigers.sumatra.ai.metis.keeper.KeeperPassTargetCalc;
import edu.tigers.sumatra.ai.metis.keeper.PenaltyGoOutDistanceCalc;
import edu.tigers.sumatra.ai.metis.offense.BallHandlingBotCalc;
import edu.tigers.sumatra.ai.metis.offense.BallHandlingSkillMovementCalc;
import edu.tigers.sumatra.ai.metis.offense.DelayFreeKickCalc;
import edu.tigers.sumatra.ai.metis.offense.DesiredOffendersCalc;
import edu.tigers.sumatra.ai.metis.offense.DesiredOffenseBotsCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveBallAccessibilityCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveBallInterceptionCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveZonesCalc;
import edu.tigers.sumatra.ai.metis.offense.PassReceiverCalc;
import edu.tigers.sumatra.ai.metis.offense.PotentialOffensiveBotsCalc;
import edu.tigers.sumatra.ai.metis.offense.SupportiveAttackerCalc;
import edu.tigers.sumatra.ai.metis.offense.SupportiveAttackerPosCalc;
import edu.tigers.sumatra.ai.metis.offense.SupportiveFinisherBlockCalc;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionsCalc;
import edu.tigers.sumatra.ai.metis.offense.dribble.BallDribbleToPosCalc;
import edu.tigers.sumatra.ai.metis.offense.dribble.BallDribblingDetectorCalc;
import edu.tigers.sumatra.ai.metis.offense.strategy.BallHandlingRobotsStrategyCalc;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategyCalc;
import edu.tigers.sumatra.ai.metis.offense.strategy.SkirmishFreeBallCalc;
import edu.tigers.sumatra.ai.metis.pass.KickOriginCalc;
import edu.tigers.sumatra.ai.metis.pass.OngoingPassCalc;
import edu.tigers.sumatra.ai.metis.pass.PassFilteringCalc;
import edu.tigers.sumatra.ai.metis.pass.PassGenerationCalc;
import edu.tigers.sumatra.ai.metis.pass.PassObstacleCalc;
import edu.tigers.sumatra.ai.metis.pass.PassRatingCalc;
import edu.tigers.sumatra.ai.metis.pass.PassSelectionCalc;
import edu.tigers.sumatra.ai.metis.pass.PassStatisticsCalc;
import edu.tigers.sumatra.ai.metis.pass.PassStatisticsExporterCalc;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionCalc;
import edu.tigers.sumatra.ai.metis.skirmish.SkirmishCategorizationCalc;
import edu.tigers.sumatra.ai.metis.skirmish.SkirmishStrategyCalc;
import edu.tigers.sumatra.ai.metis.statistics.MatchStatisticsCalc;
import edu.tigers.sumatra.ai.metis.statistics.TimeSeriesStatsCalc;
import edu.tigers.sumatra.ai.metis.support.SupportBehaviorAssignmentCalc;
import edu.tigers.sumatra.ai.metis.support.SupportBehaviorCalc;
import edu.tigers.sumatra.ai.metis.support.SupportBridgePositionCalc;
import edu.tigers.sumatra.ai.metis.support.SupportKickoffPositionsCalc;
import edu.tigers.sumatra.ai.metis.targetrater.BestGoalKickRaterCalc;
import edu.tigers.sumatra.ai.metis.test.AngleRangeTestCalc;
import edu.tigers.sumatra.ai.metis.test.DebugGridTestCalc;
import edu.tigers.sumatra.ai.metis.test.MovingRobotTestCalc;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;


/**
 * This class does situation/field analysis. Metis coordinates all calculators to analyze the
 * {@link WorldFrame}.
 * She will eventually put all the gathered conclusions in the {@link AIInfoFrame}.
 */
@Log4j2
public class Metis implements IConfigObserver
{
	private static final String CONFIG_METIS = "metis";
	private final TacticalFieldFiller tacticalFieldFiller = new TacticalFieldFiller();
	private final List<ACalculator> calculators = new ArrayList<>();
	private ETeamColor teamColor = ETeamColor.NEUTRAL;


	/**
	 * init new metis instance
	 */
	public Metis()
	{
		//**************************************************************************************
		// General Calculators
		var ongoingPassCalc = register(new OngoingPassCalc());
		var passStatisticsCalc = register(new PassStatisticsCalc(ongoingPassCalc::getOngoingPass));
		register(new PassStatisticsExporterCalc(ongoingPassCalc::getOngoingPass));
		var ballLeftFieldCalc = register(new BallLeftFieldCalc());
		var offensiveZonesCalc = register(new OffensiveZonesCalc());
		var possibleGoalCalc = register(new PossibleGoalCalc(
				ballLeftFieldCalc::getBallLeftFieldPosition
		));
		var botBallContactCalc = register(new BotBallContactCalc());
		var ballDribblingDetectorCalc = register(new BallDribblingDetectorCalc(
				botBallContactCalc::getCurrentlyTouchingBots));
		var botToBallDistanceCalc = register(new BotToBallDistanceCalc());
		var ballResponsibilityCalc = register(new BallResponsibilityCalc(
				botToBallDistanceCalc::getOpponentClosestToBall
		));
		var opponentPassReceiverCalc = register(new OpponentPassReceiverCalc());
		var ballPossessionCalc = register(new BallPossessionCalc(
				botToBallDistanceCalc::getTigerClosestToBall,
				botToBallDistanceCalc::getOpponentClosestToBall,
				opponentPassReceiverCalc::getOpponentPassReceiver,
				ongoingPassCalc::getOngoingPass
		));
		var keeperBallInterceptionCalc = register(new KeeperBallInterceptionCalc());
		var ballLeavingFieldCalc = register(new BallLeavingFieldCalc(
				botBallContactCalc::getBotsLastTouchedBall
		));
		var roleStatemachinePublisherCalc = register(new RoleStatemachinePublisherCalc());
		var weakBotsCalc = register(new WeakBotsCalc());
		var offensiveBallAccessibilityCalc = register(new OffensiveBallAccessibilityCalc());
		var directShotDetectionCalc = register(new DirectShotDetectionCalc());
		var numInterchangeBotsCalc = register(new BotInterchangeCalc(
				weakBotsCalc::getWeakBots
		));
		var penaltyGoOutDistanceCalc = register(new PenaltyGoOutDistanceCalc());
		var keeperBehaviorCalc = register(new KeeperBehaviorCalc(
				penaltyGoOutDistanceCalc::getKeeperRamboDistance,
				botToBallDistanceCalc::getOpponentClosestToBall,
				keeperBallInterceptionCalc::getKeeperBallInterception
		));

		//**************************************************************************************
		// Defense Calculators
		var defenseBallThreatCalc = register(new DefenseBallThreatCalc(
				directShotDetectionCalc::getDetectedGoalKickOpponents,
				opponentPassReceiverCalc::getOpponentPassReceiver,
				botToBallDistanceCalc::getOpponentClosestToBall
		));
		var numDefendersForBallCalc = register(new NumDefendersForBallCalc(
				ballResponsibilityCalc::getBallResponsibility,
				defenseBallThreatCalc::getDefenseBallThreat
		));
		var defenseBotThreatDefStrategyCenterBackCalc = register(new DefenseBotThreatDefStrategyCenterBackCalc(
				defenseBallThreatCalc::getDefenseBallThreat
		));
		var defenseBotThreatDefStrategyMan2ManCalc = register(new DefenseBotThreatDefStrategyMan2ManCalc(
				defenseBallThreatCalc::getDefenseBallThreat,
				defenseBotThreatDefStrategyCenterBackCalc::getCenterBackDefData,
				botToBallDistanceCalc::getOpponentClosestToBall
		));
		var defenseBotThreatDefStrategyDataAccumulatorCalc = register(new DefenseBotThreatDefStrategyDataAccumulatorCalc(
				defenseBotThreatDefStrategyCenterBackCalc::getCenterBackDefData,
				defenseBotThreatDefStrategyMan2ManCalc::getMan2ManDefData,
				defenseBallThreatCalc::getDefenseBallThreat
		));
		var defenseBotThreatCalc = register(new DefenseBotThreatCalc(
				defenseBotThreatDefStrategyDataAccumulatorCalc::getInDangerZoneBotThreatDefData,
				defenseBotThreatDefStrategyDataAccumulatorCalc::getAllDefenseBotThreatDefData,
				defenseBallThreatCalc::getDefenseBallThreat,
				numDefendersForBallCalc::getNumDefenderForBall
		));

		//**************************************************************************************
		// Defense-Offense Coordination Calculators
		var bestBallDefenderCandidatesCalc = register(new BestBallDefenderCandidatesCalc(
				numDefendersForBallCalc::getNumDefenderForBall,
				defenseBallThreatCalc::getDefenseBallThreat
		));
		register(new BallDefenseReadyCalc(
				bestBallDefenderCandidatesCalc::getBestBallDefenderCandidates,
				defenseBallThreatCalc::getDefenseBallThreat
		));
		var crucialOffenderCalc = register(new CrucialOffenderCalc(
				ongoingPassCalc::getOngoingPass,
				botToBallDistanceCalc::getOpponentClosestToBall,
				opponentPassReceiverCalc::getOpponentPassReceiver
		));

		//**************************************************************************************
		// Defense Intermezzo
		var defensePassDisruptionCalc = register(new DefensePassDisruptionCalc(
				crucialOffenderCalc::getCrucialOffender,
				defenseBallThreatCalc::getDefenseBallThreat,
				ongoingPassCalc::getOngoingPass
		));
		var numDefenderCalc = register(new NumDefenderCalc(
				ballResponsibilityCalc::getBallResponsibility,
				numDefendersForBallCalc::getNumDefenderForBall,
				numInterchangeBotsCalc::getBotsToInterchange,
				defenseBotThreatDefStrategyDataAccumulatorCalc::getInDangerZoneBotThreatDefData,
				defensePassDisruptionCalc::getCurrentAssignment
		));

		//**************************************************************************************
		// Defense-Offense Coordination Calculators
		var crucialDefenderCalc = register(new CrucialDefenderCalc(
				defenseBallThreatCalc::getDefenseBallThreat,
				numDefendersForBallCalc::getNumDefenderForBall,
				numDefenderCalc::getNumDefender,
				numInterchangeBotsCalc::getBotsToInterchange,
				crucialOffenderCalc::getCrucialOffender,
				botToBallDistanceCalc::getTigerClosestToBall,
				botToBallDistanceCalc::getOpponentClosestToBall,
				defensePassDisruptionCalc::getCurrentAssignment
		));

		//**************************************************************************************
		// Offense Calculators
		var potentialOffensiveBotsCalc = register(new PotentialOffensiveBotsCalc(
				ballLeavingFieldCalc::isBallLeavingFieldGood,
				crucialDefenderCalc::getCrucialDefenders,
				numInterchangeBotsCalc::getBotsToInterchange
		));

		var opponentClassifierCalc = register(new OpponentRoleClassifierCalc(
				potentialOffensiveBotsCalc::getPotentialOffensiveBots
		));

		var ballInterceptionCalc = register(new OffensiveBallInterceptionCalc(
				potentialOffensiveBotsCalc::getPotentialOffensiveBots,
				ballPossessionCalc::getBallPossession,
				ongoingPassCalc::getOngoingPass
		));
		var ballHandlingBotCalc = register(new BallHandlingBotCalc(
				ballResponsibilityCalc::getBallResponsibility,
				potentialOffensiveBotsCalc::getPotentialOffensiveBots,
				ballInterceptionCalc::getBallInterceptions,
				botToBallDistanceCalc::getTigersToBallDist
		));
		var skirmishCategorizationCalc = register(new SkirmishCategorizationCalc(
				botToBallDistanceCalc::getTigerClosestToBall,
				botToBallDistanceCalc::getOpponentClosestToBall
		));
		var supportiveFinsisherBlocker = register(new SupportiveFinisherBlockCalc(
				ballDribblingDetectorCalc::getDribblingInformation,
				ballHandlingBotCalc::getBallHandlingBots
		));
		var skirmishStrategyCalc = register(new SkirmishStrategyCalc(
				skirmishCategorizationCalc::getSkirmishCategory,
				botToBallDistanceCalc::getTigerClosestToBall,
				botToBallDistanceCalc::getOpponentClosestToBall,
				ballDribblingDetectorCalc::getDribblingInformation,
				supportiveFinsisherBlocker::getBlockerPos
		));
		var kickOriginCalc = register(new KickOriginCalc(
				ballInterceptionCalc::getBallInterceptions,
				ballInterceptionCalc::isTigerDribblingBall,
				ballHandlingBotCalc::getBallHandlingBots
		));
		var redirectorDetectionCalc = register(new RedirectorDetectionCalc(
				kickOriginCalc::getKickOrigins
		));
		var supportiveAttackerPosCalc = register(new SupportiveAttackerPosCalc(
				redirectorDetectionCalc::getRedirectorDetectionInformation,
				botToBallDistanceCalc::getOpponentClosestToBall
		));
		var supportiveAttackerCalc = register(new SupportiveAttackerCalc(
				skirmishStrategyCalc::getSkirmishStrategy,
				redirectorDetectionCalc::getRedirectorDetectionInformation,
				potentialOffensiveBotsCalc::getPotentialOffensiveBots,
				ballHandlingBotCalc::getBallHandlingBots,
				bestBallDefenderCandidatesCalc::getBestBallDefenderCandidates,
				kickOriginCalc::getKickOrigins,
				supportiveAttackerPosCalc::getSupportiveReceiveEarlyPos,
				supportiveAttackerPosCalc::getSupportiveDefaultPos
		));
		// Note: SkirmishFreeBallCalc writes into skirmishDetectorCalc::getSkirmishInformation :/
		var skirmishFreeBallCalc = register(new SkirmishFreeBallCalc(
				skirmishStrategyCalc::getSkirmishStrategy,
				botToBallDistanceCalc::getOpponentClosestToBall,
				ballHandlingBotCalc::getBallHandlingBots,
				supportiveAttackerCalc::getSupportiveAttackers
		));
		var bestGoalShotRaterCalc = register(new BestGoalKickRaterCalc(
				kickOriginCalc::getKickOrigins
		));
		var delayFreeKickCalc = register(new DelayFreeKickCalc(
				botToBallDistanceCalc::getTigerClosestToBall,
				bestGoalShotRaterCalc::getBestGoalKickPerBot
		));
		var passTargetGenerationCalc = register(new PassGenerationCalc(
				kickOriginCalc::getKickOrigins,
				offensiveBallAccessibilityCalc::getInaccessibleBallAngles,
				ballHandlingBotCalc::getBallHandlingBots,
				crucialDefenderCalc::getCrucialDefenders
		));
		var passTargetRatingCalc = register(new PassRatingCalc(
				passTargetGenerationCalc::getGeneratedPasses,
				passStatisticsCalc::getPassStats,
				offensiveZonesCalc::getOffensiveZones,
				opponentClassifierCalc::getOpponentMan2ManMarkers

		));
		var passTargetFilteringCalc = register(new PassFilteringCalc(
				passTargetRatingCalc::getPassesRated
		));
		var passTargetSelectionCalc = register(new PassSelectionCalc(
				passTargetFilteringCalc::getFilteredAndRatedPasses
		));
		var ballHandlingRobotsStrategyCalc = register(new BallHandlingRobotsStrategyCalc(
				skirmishFreeBallCalc::isStartMoving,
				delayFreeKickCalc::getFreeKickDelay
		));

		var ballDribbleToPosCalc = register(new BallDribbleToPosCalc(
				ballHandlingBotCalc::getBallHandlingBots,
				ballDribblingDetectorCalc::getDribblingInformation,
				ballPossessionCalc::getBallPossession,
				botToBallDistanceCalc::getOpponentClosestToBall
		));
		var offensiveActionsCalc = register(new OffensiveActionsCalc(
				ballHandlingRobotsStrategyCalc::getBallHandlingRobotStrategy,
				ballHandlingBotCalc::getBallHandlingBots,
				passTargetSelectionCalc::getSelectedPasses,
				kickOriginCalc::getKickOrigins,
				botToBallDistanceCalc::getOpponentClosestToBall,
				bestGoalShotRaterCalc::getBestGoalKickPerBot,
				bestGoalShotRaterCalc::getBestRedirectableGoalKickPerBot,
				ballDribblingDetectorCalc::getDribblingInformation,
				ballDribbleToPosCalc::getDribbleToPos,
				ballInterceptionCalc::isTigerDribblingBall,
				ballPossessionCalc::getBallPossession,
				ballDribbleToPosCalc::getProtectPass
		));
		var ballHandlingSkillMovementCalc = register(new BallHandlingSkillMovementCalc(
				ballDribbleToPosCalc::getDribbleToPos,
				offensiveActionsCalc::getOffensiveActions,
				ballHandlingBotCalc::getBallHandlingBots
		));

		var keeperPassTargetCalc = register(new KeeperPassTargetCalc(
				ballResponsibilityCalc::getBallResponsibility,
				botToBallDistanceCalc::getOpponentClosestToBall
		));
		var passReceiverCalc = register(new PassReceiverCalc(
				offensiveActionsCalc::getOffensiveActions,
				keeperPassTargetCalc::getKeeperPass
		));
		var desiredOffenseBotsCalc = register(new DesiredOffenseBotsCalc(
				ballHandlingBotCalc::getBallHandlingBots,
				supportiveAttackerCalc::getSupportiveAttackers,
				passReceiverCalc::getPassReceivers
		));
		var offensiveStrategyCalc = register(new OffensiveStrategyCalc(
				ballHandlingRobotsStrategyCalc::getBallHandlingRobotStrategy,
				ballHandlingBotCalc::getBallHandlingBots,
				passReceiverCalc::getPassReceivers,
				supportiveAttackerCalc::getSupportiveAttackers
		));

		var ballPlacementBotsCalc = register(new BallPlacementBotsCalc(
				numInterchangeBotsCalc::getBotsToInterchange
		));
		var playNumberCalc = register(new PlayNumberCalc(
				ballPlacementBotsCalc::getNumBallPlacementBots,
				numDefenderCalc::getNumDefender,
				numInterchangeBotsCalc::getNumInterchangeBots,
				desiredOffenseBotsCalc::getNumOffenseBots
		));

		//**************************************************************************************
		// Small Defense Intermezzo
		var keeperPassThreatCalc = register(new KeeperPassThreatCalc(
				keeperPassTargetCalc::getKeeperPass
		));

		//**************************************************************************************
		// Desired Bots Calculators
		var desiredBotsCalc = register(new DesiredBotsCalc());
		register(new DesiredInterchangeBotsCalc(
				desiredBotsCalc::getDesiredBots,
				playNumberCalc::getPlayNumbers,
				numInterchangeBotsCalc::getBotsToInterchange
		));
		register(new DesiredKeeperCalc(
				desiredBotsCalc::getDesiredBots,
				playNumberCalc::getPlayNumbers
		));
		register(new DesiredBallPlacementBotsCalc(
				desiredBotsCalc::getDesiredBots,
				playNumberCalc::getPlayNumbers,
				ballPlacementBotsCalc::getPreferredBallPlacementBots
		));
		register(new DesiredOffendersCalc(
				desiredBotsCalc::getDesiredBots,
				playNumberCalc::getPlayNumbers,
				desiredOffenseBotsCalc::getDesiredOffenseBots
		));
		var desiredDefendersCalc = register(new DesiredDefendersCalc(
				desiredBotsCalc::getDesiredBots,
				playNumberCalc::getPlayNumbers,
				defenseBallThreatCalc::getDefenseBallThreat,
				keeperPassThreatCalc::getKeeperPassThreat,
				defenseBotThreatCalc::getDefenseBotThreats,
				crucialDefenderCalc::getStrategy,
				numDefendersForBallCalc::getNumDefenderForBall,
				defensePassDisruptionCalc::getCurrentAssignment
		));
		register(new DesiredRemainingBotsCalc(
				desiredBotsCalc::getDesiredBots,
				playNumberCalc::getPlayNumbers
		));

		//**************************************************************************************
		// Offense Calculators Again
		var passObstacleCalc = register(new PassObstacleCalc(
				offensiveActionsCalc::getOffensiveActions,
				desiredBotsCalc::getDesiredBots,
				ongoingPassCalc::getOngoingPass
		));

		//**************************************************************************************
		// Defense Calculators Again
		var defenseThreatAssignmentSeparationCalc = register(new DefenseThreatAssignmentSeparationCalc(
				desiredBotsCalc::getDesiredBots,
				desiredDefendersCalc::getDefenseRawThreatAssignments,
				desiredDefendersCalc::getDesiredPassDisruptionAssignment
		));
		var defensePenAreaBoundaryCalc = register(new DefensePenAreaBoundaryCalc());
		var defensePenAreaTargetGroupFinderCalc = register(new DefensePenAreaTargetGroupFinderCalc(
				defenseThreatAssignmentSeparationCalc::getDefensePenAreaThreatAssignments,
				defenseThreatAssignmentSeparationCalc::getPenAreaDefenders,
				defensePenAreaBoundaryCalc::getPenAreaBoundary
		));
		var defensePenAreaTargetGroupAssignerCalc = register(new DefensePenAreaTargetGroupAssignerCalc(
				defenseThreatAssignmentSeparationCalc::getPenAreaDefenders,
				defensePenAreaTargetGroupFinderCalc::getDefensePenAreaTargetGroups,
				defensePenAreaBoundaryCalc::getPenAreaBoundary
		));
		//**************************************************************************************
		// Defense-Offense Coordination Calculators Again
		var defenseSupportingBallReceptionCalc = register(new DefenseSupportingBallReceptionCalc(
				defenseThreatAssignmentSeparationCalc::getDefenseOuterThreatAssignments,
				crucialDefenderCalc::getCrucialDefenders,
				opponentPassReceiverCalc::getOpponentPassReceiver,
				offensiveStrategyCalc::getStrategy,
				offensiveActionsCalc::getOffensiveActions,
				ballInterceptionCalc::getBallInterceptions
		));

		//**************************************************************************************
		// Supportive Calculators
		var bridgePositionCalc = register(new SupportBridgePositionCalc(
				kickOriginCalc::getKickOrigins
		));
		var kickoffCalc = register(new SupportKickoffPositionsCalc());
		var aggressiveManToManCalc = register(new SupporterMan2ManAssignmentCalc(
				desiredBotsCalc::getDesiredBots,
				desiredDefendersCalc::getDefenseRawThreatAssignments,
				defenseBotThreatCalc::getAllMan2manBotThreats,
				defenseBallThreatCalc::getDefenseBallThreat
		));
		var supportBehaviorCalc = register(new SupportBehaviorCalc(
				desiredBotsCalc::getDesiredBots,
				ballPossessionCalc::getBallPossession,
				offensiveStrategyCalc::getStrategy,
				bridgePositionCalc::getSupportiveGoalPositions,
				kickoffCalc::getKickoffPositions,
				offensiveActionsCalc::getOffensiveActions,
				kickOriginCalc::getKickOrigins,
				bestGoalShotRaterCalc::getBestGoalKickPerBot,
				bridgePositionCalc::getOffensiveShadows,
				ongoingPassCalc::getOngoingPass,
				aggressiveManToManCalc::getSupporterToBotThreatMapping,
				ballInterceptionCalc::canOngoingPassBeTrusted,
				defenseBallThreatCalc::getDefenseBallThreat,
				passReceiverCalc::getSupportPassReceivers
		));
		var supportBehaviorAssignmentCalc = register(new SupportBehaviorAssignmentCalc(
				desiredBotsCalc::getDesiredBots,
				supportBehaviorCalc::getSupportViabilities
		));
		var pathFinderPrioMapCalc = register(new PathFinderPrioMapCalc(
				desiredBotsCalc::getDesiredBots,
				crucialDefenderCalc::getCrucialDefenders,
				crucialOffenderCalc::getCrucialOffender
		));
		//**************************************************************************************
		// Game State Calculators
		//Calculate if we want to keep playing after a Card or if we want a STOP
		var advantageRuleCalc = register(new AdvantageRuleCalc(
				ballPossessionCalc::getBallPossession
		));

		//**************************************************************************************
		// Statistics Calculators
		var matchStatisticsCalc = register(new MatchStatisticsCalc(
				desiredBotsCalc::getDesiredBots,
				ballPossessionCalc::getBallPossession,
				botBallContactCalc::getBotsLastTouchedBall,
				possibleGoalCalc::getPossibleGoal,
				directShotDetectionCalc::getDetectedGoalKickTigers,
				botToBallDistanceCalc::getOpponentClosestToBall,
				opponentPassReceiverCalc::getOpponentPassReceiver,
				defenseBotThreatDefStrategyDataAccumulatorCalc::getInDangerZoneBotThreatDefData,
				defenseBotThreatCalc::getDefenseThreatRatingForNumDefender,
				desiredDefendersCalc::getDefenseRawThreatAssignments,
				botBallContactCalc::getCurrentlyTouchingBots,
				passStatisticsCalc::getPassStats
		));
		// write time series data to a file
		register(new TimeSeriesStatsCalc(
				desiredBotsCalc::getDesiredBots,
				botToBallDistanceCalc::getOpponentsToBallDist,
				botToBallDistanceCalc::getTigersToBallDist,
				ballPossessionCalc::getBallPossession,
				matchStatisticsCalc::getMatchStatistics
		));

		register(new MultimediaCalc(
				crucialDefenderCalc::getCrucialDefenders,
				offensiveStrategyCalc::getStrategy,
				desiredBotsCalc::getDesiredBots
		));

		// test calculators that only produce drawable shapes
		register(new AngleRangeTestCalc());
		register(new MovingRobotTestCalc());
		register(new DebugGridTestCalc(
				passStatisticsCalc::getPassStats,
				offensiveZonesCalc::getOffensiveZones,
				opponentClassifierCalc::getOpponentMan2ManMarkers
		));

		connect(
				TacticalFieldBuilder::roleStatemachineGraphBotMap,
				roleStatemachinePublisherCalc::getRoleStatemachineGraph
		);
		connect(
				TacticalFieldBuilder::skillStatemachineGraphBotMap,
				roleStatemachinePublisherCalc::getSkillStatemachineGraph
		);
		connect(
				TacticalFieldBuilder::ballInterceptionInformationMap,
				ballInterceptionCalc::getBallInterceptionInformationMap
		);
		connect(TacticalFieldBuilder::matchStats, matchStatisticsCalc::getMatchStatistics);
		connect(TacticalFieldBuilder::opponentsToBallDistances, botToBallDistanceCalc::getOpponentsToBallDist);
		connect(TacticalFieldBuilder::opponentClosestToBall, botToBallDistanceCalc::getOpponentClosestToBall);
		connect(TacticalFieldBuilder::desiredBotMap, desiredBotsCalc::getDesiredBots);
		connect(TacticalFieldBuilder::pathFinderPrioMap, pathFinderPrioMapCalc::getPathFinderPrioMap);
		connect(TacticalFieldBuilder::offensiveStrategy, offensiveStrategyCalc::getStrategy);
		connect(TacticalFieldBuilder::offensiveActions, offensiveActionsCalc::getOffensiveActions);
		connect(TacticalFieldBuilder::skirmishRipFreeTarget, skirmishFreeBallCalc::getTargetPosition);
		connect(TacticalFieldBuilder::ballInterceptions, ballInterceptionCalc::getBallInterceptions);
		connect(TacticalFieldBuilder::supportiveAttackers, supportiveAttackerCalc::getSupportiveAttackers);
		connect(
				TacticalFieldBuilder::redirectorDetectionInformation,
				redirectorDetectionCalc::getRedirectorDetectionInformation
		);
		connect(TacticalFieldBuilder::filteredAndRatedPassesMap, passTargetFilteringCalc::getFilteredAndRatedPasses);
		connect(TacticalFieldBuilder::selectedPasses, passTargetSelectionCalc::getSelectedPasses);
		connect(TacticalFieldBuilder::opponentPassReceiver, opponentPassReceiverCalc::getOpponentPassReceiver);
		connect(TacticalFieldBuilder::opponentReceivePosGuess, defenseBallThreatCalc::getOpponentReceivePosGuess);
		connect(
				TacticalFieldBuilder::defenseOuterThreatAssignments,
				defenseThreatAssignmentSeparationCalc::getDefenseOuterThreatAssignments
		);
		connect(
				TacticalFieldBuilder::defensePenAreaPositionAssignments,
				defensePenAreaTargetGroupAssignerCalc::getPenAreaPositionAssignments
		);
		connect(
				TacticalFieldBuilder::defensePenAreaBoundaryForPenAreaGroup,
				defensePenAreaBoundaryCalc::getPenAreaBoundary
		);
		connect(
				TacticalFieldBuilder::defensePassDisruptionAssignment,
				desiredDefendersCalc::getDesiredPassDisruptionAssignment
		);
		connect(TacticalFieldBuilder::keeperPass, keeperPassTargetCalc::getKeeperPass);
		connect(TacticalFieldBuilder::keeperBehavior, keeperBehaviorCalc::getSelectedBehavior);
		connect(TacticalFieldBuilder::keeperInterceptPos, keeperBehaviorCalc::getInterceptPos);
		connect(TacticalFieldBuilder::supportViabilities, supportBehaviorCalc::getSupportViabilities);
		connect(TacticalFieldBuilder::activeSupportBehaviors, supportBehaviorCalc::getActiveBehaviors);
		connect(TacticalFieldBuilder::supportBehaviorAssignment, supportBehaviorAssignmentCalc::getBehaviorAssignment);
		connect(TacticalFieldBuilder::keepPlayingIfWeHaveAdvantage, advantageRuleCalc::isKeepPlaying);
		connect(TacticalFieldBuilder::primaryPassObstacles, passObstacleCalc::getPrimaryPlannedObstacles);
		connect(TacticalFieldBuilder::allPassObstacles, passObstacleCalc::getAllPlannedPassObstacles);
		connect(TacticalFieldBuilder::ongoingPassObstacle, passObstacleCalc::getOngoingPassObstacle);
		connect(TacticalFieldBuilder::ballHandlingAdvise, ballHandlingSkillMovementCalc::getBallHandlingAdvise);
		connect(TacticalFieldBuilder::ballPossession, ballPossessionCalc::getBallPossession);
		connect(TacticalFieldBuilder::dribblingInformation, ballDribblingDetectorCalc::getDribblingInformation);
		connect(TacticalFieldBuilder::passStats, passStatisticsCalc::getPassStats);
		connect(
				TacticalFieldBuilder::defenseSupportingBallReceptionPos,
				defenseSupportingBallReceptionCalc::getSupportiveDefensePosition
		);

		calculators.forEach(c -> ConfigRegistration.registerClass(CONFIG_METIS, c.getClass()));
	}


	private <T> void connect(BiConsumer<TacticalFieldBuilder, T> consumer, Supplier<T> supplier)
	{
		tacticalFieldFiller.connect(consumer, supplier);
	}


	private <T extends ACalculator> T register(T calc)
	{
		calculators.add(calc);
		return calc;
	}


	/**
	 * Process a frame
	 *
	 * @param baseAiFrame
	 * @return
	 */
	public MetisAiFrame process(final BaseAiFrame baseAiFrame)
	{
		if (teamColor == ETeamColor.NEUTRAL)
		{
			teamColor = baseAiFrame.getTeamColor();
			afterApply(null);
		}
		Map<Class<? extends ACalculator>, CalculatorExecution> calculatorExecutions = new HashMap<>();
		calculators.forEach(c -> c.calculate(baseAiFrame));
		calculators.forEach(c -> calculatorExecutions.put(
				c.getClass(), CalculatorExecution.builder()
						.executed(c.isExecutionStatusLastFrame())
						.processingTime(c.getLastProcessingTimeNs())
						.build()
		));
		var tacticalField = tacticalFieldFiller.build();
		return MetisAiFrame.builder()
				.baseAiFrame(baseAiFrame)
				.tacticalField(tacticalField)
				.calculatorExecutions(calculatorExecutions)
				.build();
	}


	public void start()
	{
		ConfigRegistration.registerConfigurableCallback(CONFIG_METIS, this);
	}


	/**
	 * Stop all calculators
	 */
	public void stop()
	{
		ConfigRegistration.unregisterConfigurableCallback(CONFIG_METIS, this);
		calculators.forEach(ACalculator::stop);
	}


	@Override
	public void afterApply(final IConfigClient configClient)
	{
		// apply default spezi
		calculators.forEach(calc -> ConfigRegistration.applySpezis(calc, CONFIG_METIS, ""));

		// apply team spezies if not in match mode
		if (!SumatraModel.getInstance().isTournamentMode())
		{
			calculators.forEach(calc -> ConfigRegistration.applySpezis(calc, CONFIG_METIS, teamColor.name()));
		}
	}


	private static class TacticalFieldFiller
	{
		private final List<Runnable> connectors = new ArrayList<>();
		private TacticalFieldBuilder builder;


		TacticalField build()
		{
			builder = TacticalField.builder();
			connectors.forEach(Runnable::run);
			return builder.build();
		}


		private <T> void connect(BiConsumer<TacticalFieldBuilder, T> consumer, Supplier<T> supplier)
		{
			connectors.add(() -> consumer.accept(builder, supplier.get()));
		}
	}
}
