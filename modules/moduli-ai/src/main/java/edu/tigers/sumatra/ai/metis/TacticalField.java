/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaPositionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.situation.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.skillsystem.skills.util.PenAreaBoundary;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.trees.EOffensiveSituation;
import edu.tigers.sumatra.trees.OffensiveActionTreeMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * <p>
 * This class should be used to store and combine all tactical information that are required by Athena (plays, roles)
 * or for further processing (visualization).
 * Metis-only (internal) information should not be included here.
 */
@Value
@Builder
public class TacticalField
{
	//
	// General
	//

	@NonNull
	MatchStats matchStats;

	@NonNull
	BallPossession ballPossession;
	@Singular
	List<BotDistance> tigersToBallDistances;
	@Singular
	List<BotDistance> opponentsToBallDistances;
	@NonNull
	BotDistance tigerClosestToBall;
	@NonNull
	BotDistance opponentClosestToBall;
	boolean ballLeavingFieldGood;

	@NonNull
	EBallResponsibility ballResponsibility;

	@Singular("desiredBot")
	Map<EPlay, Set<BotID>> desiredBotMap;
	@NonNull
	PathFinderPrioMap pathFinderPrioMap;


	@Singular("roleStatemachineGraph")
	Map<BotID, Map<IEvent, Map<IState, IState>>> roleStatemachineGraphBotMap;
	@Singular("skillStatemachineGraph")
	Map<BotID, Map<IEvent, Map<IState, IState>>> skillStatemachineGraphBotMap;

	//
	// Statistics
	//

	OffensiveStatisticsFrame offensiveStatistics;
	OffensiveAnalysedFrame analyzedOffensiveStatisticsFrame;


	//
	// Attack
	//
	GoalKick bestGoalKick;

	@NonNull
	OffensiveStrategy offensiveStrategy;
	@Singular
	Map<BotID, OffensiveAction> offensiveActions;
	@NonNull
	SkirmishInformation skirmishInformation;
	@Singular
	Map<BotID, RatedBallInterception> ballInterceptions;
	@NonNull
	IVector2 supportiveAttackerMovePos;
	@NonNull
	OffensiveActionTreeMap actionTrees;
	@NonNull
	OffensiveActionTreePath currentPath;
	@NonNull
	EOffensiveSituation currentSituation;
	@Singular
	List<IVector2> supportiveGoalPositions;
	@NonNull
	RedirectorDetectionInformation redirectorDetectionInformation;
	@Singular("ballInterceptionInformation")
	Map<BotID, BallInterceptionInformation> ballInterceptionInformationMap;
	@Singular("filteredAndRatedPasses")
	Map<KickOrigin, List<RatedPass>> filteredAndRatedPassesMap;
	@Singular
	Map<KickOrigin, RatedPass> selectedPasses;

	//
	// Support
	//
	@Singular
	List<ICircle> freeSpots;
	@Singular
	List<IArc> offensiveShadows;
	@Singular
	List<IVector2> supporterMidfieldPositions;
	@Singular
	List<IVector2> supporterKickoffPositions;
	@Singular
	Map<BotID, EnumMap<ESupportBehavior, SupportBehaviorPosition>> supportViabilities;
	@Singular("supportBehaviorAssignment")
	Map<BotID, ESupportBehavior> supportBehaviorAssignment;
	@Singular("activeSupportBehaviors")
	Map<ESupportBehavior, Boolean> activeSupportBehaviors;


	//
	// Defense
	//
	ITrackedBot opponentPassReceiver;
	@Singular
	List<DefenseThreatAssignment> defenseOuterThreatAssignments;
	@Singular
	List<DefensePenAreaPositionAssignment> defensePenAreaPositionAssignments;
	PenAreaBoundary defensePenAreaBoundaryForPenAreaGroup;

	//
	// Keeper
	//
	Pass keeperPass;
	double keeperRamboDistance;


	public static TacticalField empty()
	{
		return TacticalField.builder()
				.matchStats(new MatchStats())
				.ballPossession(new BallPossession())
				.tigerClosestToBall(BotDistance.NULL_BOT_DISTANCE)
				.opponentClosestToBall(BotDistance.NULL_BOT_DISTANCE)
				.ballResponsibility(EBallResponsibility.OFFENSE)
				.pathFinderPrioMap(new PathFinderPrioMap())
				.offensiveStrategy(new OffensiveStrategy())
				.skirmishInformation(new SkirmishInformation())
				.supportiveAttackerMovePos(Vector2.zero())
				.actionTrees(new OffensiveActionTreeMap())
				.currentPath(new OffensiveActionTreePath())
				.currentSituation(EOffensiveSituation.DEFAULT_SITUATION)
				.supportiveGoalPosition(Vector2.zero())
				.redirectorDetectionInformation(new RedirectorDetectionInformation())
				.build();
	}


}


