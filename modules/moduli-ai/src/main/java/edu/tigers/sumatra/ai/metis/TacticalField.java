/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.ai.metis.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallControl;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePassDisruptionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePenAreaPositionAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperActionType;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionType;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribblingInformation;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.EOffensiveZone;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.PassStats;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.skillsystem.skills.BallHandlingAdvise;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
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

	@Singular
	List<BotDistance> opponentsToBallDistances;
	@NonNull
	BotDistance opponentClosestToBall;


	@Singular("desiredBot")
	Map<EPlay, Set<BotID>> desiredBotMap;
	@NonNull
	PathFinderPrioMap pathFinderPrioMap;


	@Singular("roleStatemachineGraph")
	Map<BotID, Map<IEvent, Map<IState, IState>>> roleStatemachineGraphBotMap;
	@Singular("skillStatemachineGraph")
	Map<BotID, Map<IEvent, Map<IState, IState>>> skillStatemachineGraphBotMap;

	//
	// Attack
	//

	@NonNull
	OffensiveStrategy offensiveStrategy;
	@Singular
	Map<BotID, RatedOffensiveAction> offensiveActions;
	@Singular
	Map<BotID, IVector2> supportiveAttackers;
	@NonNull
	IVector2 skirmishRipFreeTarget;
	@Singular
	Map<BotID, RatedBallInterception> ballInterceptions;
	@NonNull
	RedirectorDetectionInformation redirectorDetectionInformation;
	@Singular("ballInterceptionInformation")
	Map<BotID, BallInterceptionInformation> ballInterceptionInformationMap;
	@Singular("filteredAndRatedPasses")
	Map<KickOrigin, List<RatedPass>> filteredAndRatedPassesMap;
	@Singular
	Map<KickOrigin, RatedPass> selectedPasses;
	@NonNull
	Map<EOffensiveActionType, List<IObstacle>> primaryPassObstacles;
	@NonNull
	Map<EOffensiveActionType, List<IObstacle>> allPassObstacles;
	IObstacle ongoingPassObstacle;
	@NonNull
	BallHandlingAdvise ballHandlingAdvise;
	@NonNull
	BallPossession ballPossession;
	@NonNull
	DribblingInformation dribblingInformation;
	@NonNull
	PassStats passStats;

	//
	// Support
	//
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
	IVector2 opponentReceivePosGuess;
	@Singular
	List<DefenseThreatAssignment> defenseOuterThreatAssignments;
	@Singular
	List<DefensePenAreaPositionAssignment> defensePenAreaPositionAssignments;
	DefensePassDisruptionAssignment defensePassDisruptionAssignment;
	IShapeBoundary defensePenAreaBoundaryForPenAreaGroup;
	IVector2 defenseSupportingBallReceptionPos;

	//
	// Keeper
	//
	Pass keeperPass;
	@NonNull
	EKeeperActionType keeperBehavior;
	@NonNull
	IVector2 keeperInterceptPos;


	boolean keepPlayingIfWeHaveAdvantage;


	public static TacticalField empty()
	{
		return TacticalField.builder()
				.matchStats(new MatchStats())
				.opponentClosestToBall(BotDistance.NULL_BOT_DISTANCE)
				.pathFinderPrioMap(new PathFinderPrioMap())
				.offensiveStrategy(new OffensiveStrategy())
				.skirmishRipFreeTarget(Vector2.zero())
				.redirectorDetectionInformation(new RedirectorDetectionInformation())
				.primaryPassObstacles(Map.of())
				.allPassObstacles(Map.of())
				.keeperBehavior(EKeeperActionType.DEFEND)
				.ballHandlingAdvise(new BallHandlingAdvise())
				.keeperInterceptPos(Vector2.zero())
				.ballPossession(new BallPossession(EBallPossession.NO_ONE, EBallControl.NONE, BotID.noBot(), BotID.noBot()))
				.dribblingInformation(new DribblingInformation(Vector2.zero(), false, BotID.noBot(), null, null, false))
				.passStats(new PassStats(new EnumMap<>(EOffensiveZone.class), Collections.emptyList(), 0, 0, 0))
				.build();
	}
}


