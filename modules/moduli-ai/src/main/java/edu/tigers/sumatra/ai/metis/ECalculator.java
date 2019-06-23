/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.ai.metis.ballplacement.DesiredBallPlacementBotsCalc;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossessionCalc;
import edu.tigers.sumatra.ai.metis.ballresponsibility.BallResponsibilityCalc;
import edu.tigers.sumatra.ai.metis.botdistance.BotToBallDistanceCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenderAssignerCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBallThreatCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBotThreatCalc;
import edu.tigers.sumatra.ai.metis.defense.DesiredDefendersCalc;
import edu.tigers.sumatra.ai.metis.defense.NumDefenderCalc;
import edu.tigers.sumatra.ai.metis.defense.OpponentPassReceiverCalc;
import edu.tigers.sumatra.ai.metis.general.AiInfoCommunicationCalc;
import edu.tigers.sumatra.ai.metis.general.BallLeftFieldCalc;
import edu.tigers.sumatra.ai.metis.general.BotBallContactCalc;
import edu.tigers.sumatra.ai.metis.general.DesiredGameStateBotsCalc;
import edu.tigers.sumatra.ai.metis.general.DirectShotDetectionCalc;
import edu.tigers.sumatra.ai.metis.general.DoubleTouchEvaderCalc;
import edu.tigers.sumatra.ai.metis.general.IcingDetectorCalc;
import edu.tigers.sumatra.ai.metis.general.MultimediaCalc;
import edu.tigers.sumatra.ai.metis.general.PlayNumberCalc;
import edu.tigers.sumatra.ai.metis.general.RoleMappingCalc;
import edu.tigers.sumatra.ai.metis.general.SkirmishDetectorCalc;
import edu.tigers.sumatra.ai.metis.general.TeamGameStateCalc;
import edu.tigers.sumatra.ai.metis.interchange.DesiredInterchangeBotsCalc;
import edu.tigers.sumatra.ai.metis.interchange.NumInterchangeBotsCalc;
import edu.tigers.sumatra.ai.metis.interchange.WeakBotsCalc;
import edu.tigers.sumatra.ai.metis.keeper.ChipKickTargetCalc;
import edu.tigers.sumatra.ai.metis.keeper.DesiredKeeperCalc;
import edu.tigers.sumatra.ai.metis.keeper.KeeperStateCalc;
import edu.tigers.sumatra.ai.metis.keeper.PenaltyshootoutRambodistanceCalc;
import edu.tigers.sumatra.ai.metis.offense.CrucialOffenderCalc;
import edu.tigers.sumatra.ai.metis.offense.DesiredOffendersCalc;
import edu.tigers.sumatra.ai.metis.offense.SupportiveAttackerPosCalc;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionsCalc;
import edu.tigers.sumatra.ai.metis.offense.action.situation.OffensiveSituationRatingCalc;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterceptionCalc;
import edu.tigers.sumatra.ai.metis.offense.kickoff.KickoffActionsCalc;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsCalc;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsPostAnalysisCalc;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategyCalc;
import edu.tigers.sumatra.ai.metis.shootout.DesiredShootoutAttackerCalc;
import edu.tigers.sumatra.ai.metis.shootout.PenaltyPlacementTargetCalc;
import edu.tigers.sumatra.ai.metis.shootout.PenaltyPlacementTargetFilter;
import edu.tigers.sumatra.ai.metis.statistics.StatisticsCalc;
import edu.tigers.sumatra.ai.metis.statistics.possiblegoal.PossibleGoalCalc;
import edu.tigers.sumatra.ai.metis.support.DesiredSupportersCalc;
import edu.tigers.sumatra.ai.metis.support.PassTargetGenerationCalc;
import edu.tigers.sumatra.ai.metis.support.PassTargetRatingCalc;
import edu.tigers.sumatra.ai.metis.support.PassTargetSelectionCalc;
import edu.tigers.sumatra.ai.metis.support.SupportPositionGenerationCalc;
import edu.tigers.sumatra.ai.metis.support.SupportPositionRatingCalc;
import edu.tigers.sumatra.ai.metis.support.SupportPositionSelectionCalc;
import edu.tigers.sumatra.ai.metis.support.VoronoiCalc;
import edu.tigers.sumatra.ai.metis.targetrater.BestGoalShotRaterCalc;
import edu.tigers.sumatra.ai.metis.test.AngleRangeRaterPassTargetTestCalc;
import edu.tigers.sumatra.ai.metis.test.AngleRangeRaterTestCalc;
import edu.tigers.sumatra.ai.metis.test.AngleRangeTestCalc;
import edu.tigers.sumatra.ai.metis.test.DebugGridTestCalc;
import edu.tigers.sumatra.ids.ETeam;


/**
 * This Enum contains all calculators by name.
 * <p>
 * Use the dependency management to ensure your desired input was calculated before.
 * The calculators will be processing in the order of this enum. You can not specify an enum constant,
 * before it is not defined. That ensures that if you specify an enum constant, it was defined before the current one.
 * This way, there is no need to check dependencies on runtime.
 * </p>
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public enum ECalculator implements IInstanceableEnum
{
	// ##### General tactics
	AI_INFO_COMM(new InstanceableClass(AiInfoCommunicationCalc.class)),
	/**  */
	GAME_STATE(new InstanceableClass(TeamGameStateCalc.class)),
	/** */
	BALL_RESPONSIBILITY(new InstanceableClass(BallResponsibilityCalc.class)),
	/**  */
	BALL_LEFT_FIELD(new InstanceableClass(BallLeftFieldCalc.class)),
	/** */
	POSSIBLE_GOAL(new InstanceableClass(PossibleGoalCalc.class),
			BALL_LEFT_FIELD),
	/** */
	BOT_BALL_CONTACT(new InstanceableClass(BotBallContactCalc.class)),
	/** */
	BEST_GOAL_SHOT(new InstanceableClass(BestGoalShotRaterCalc.class)),
	/** */
	BOT_TO_BALL_DISTANCE_TIGERS(new InstanceableClass(BotToBallDistanceCalc.class,
			new InstanceableParameter(ETeam.class, "team", ETeam.TIGERS.name()))),
	/** */
	BOT_TO_BALL_DISTANCE_OPPONENTS(new InstanceableClass(BotToBallDistanceCalc.class,
			new InstanceableParameter(ETeam.class, "team", ETeam.OPPONENTS.name()))),
	/** */
	OPPONENT_PASS_RECEIVER(new InstanceableClass(OpponentPassReceiverCalc.class)),
	/** */
	BALL_POSSESSION(new InstanceableClass(BallPossessionCalc.class),
			BOT_TO_BALL_DISTANCE_TIGERS, BOT_TO_BALL_DISTANCE_OPPONENTS, OPPONENT_PASS_RECEIVER),
	/** */
	ICING_DETECTOR(new InstanceableClass(IcingDetectorCalc.class),
			BOT_BALL_CONTACT),
	/** */
	DOUBLE_TOUCH_EVADER(new InstanceableClass(DoubleTouchEvaderCalc.class),
			GAME_STATE, BOT_BALL_CONTACT),
	WEAK_BOTS(new InstanceableClass(WeakBotsCalc.class)),
	
	// ##### Statistics
	/**  */
	DIRECT_SHOT_WE(new InstanceableClass(DirectShotDetectionCalc.class,
			new InstanceableParameter(ETeam.class, "attackingTeam", ETeam.TIGERS.name()))),
	/**  */
	DIRECT_SHOT_THEY(new InstanceableClass(DirectShotDetectionCalc.class,
			new InstanceableParameter(ETeam.class, "attackingTeam", ETeam.OPPONENTS.name()))),
	
	// ##### Keeper, Defense, Offense, Support
	
	NUM_INTERCHANGE_BOTS(new InstanceableClass(NumInterchangeBotsCalc.class),
			WEAK_BOTS),
	
	/**  */
	BALL_PLACEMENT(new InstanceableClass(DesiredBallPlacementBotsCalc.class),
			GAME_STATE),
	
	/** */
	KEEPER_CALC(new InstanceableClass(KeeperStateCalc.class),
			GAME_STATE, BOT_TO_BALL_DISTANCE_OPPONENTS),
	/** */
	KEEPER_RAMBO_DISTANCE_CALC(new InstanceableClass(PenaltyshootoutRambodistanceCalc.class)),
	/** */
	CHIP_KICK_TARGET(new InstanceableClass(ChipKickTargetCalc.class)),
	
	/** */
	DEFENSE_BOT_THREATS(new InstanceableClass(DefenseBotThreatCalc.class)),
	/** */
	DEFENSE_BALL_THREAT(new InstanceableClass(DefenseBallThreatCalc.class),
			OPPONENT_PASS_RECEIVER),
	/** */
	NUM_DEFENDER(new InstanceableClass(NumDefenderCalc.class),
			BALL_POSSESSION, BALL_RESPONSIBILITY, KEEPER_CALC, NUM_INTERCHANGE_BOTS),
	/** */
	DEFENDER_ASSIGNER(new InstanceableClass(DefenderAssignerCalc.class),
			DEFENSE_BOT_THREATS, DEFENSE_BALL_THREAT, NUM_DEFENDER, BOT_TO_BALL_DISTANCE_OPPONENTS),
	
	/** */
	CRUCIAL_OFFENDER(new InstanceableClass(CrucialOffenderCalc.class)),
	/** */
	CRUCIAL_DEFENDER(new InstanceableClass(DesiredDefendersCalc.class,
			new InstanceableParameter(Boolean.class, "crucial", Boolean.TRUE.toString())),
			DEFENDER_ASSIGNER),
	
	/** */
	PASS_TARGET_GENERATION(new InstanceableClass(PassTargetGenerationCalc.class)),
	/** */
	PASS_TARGET_RATING(new InstanceableClass(PassTargetRatingCalc.class), PASS_TARGET_GENERATION),
	/** */
	PASS_TARGET_SELECTION(new InstanceableClass(PassTargetSelectionCalc.class), PASS_TARGET_RATING),
	
	/**  */
	SUPPORTIVE_ATTACKER_POS(new InstanceableClass(SupportiveAttackerPosCalc.class)),
	/**  */
	BALL_INTERCEPTION(new InstanceableClass(BallInterceptionCalc.class)),
	/**  */
	OFFENSIVE_STATS(new InstanceableClass(OffensiveStatisticsCalc.class)),
	/**  */
	SKIRMISH_DETECTOR(new InstanceableClass(SkirmishDetectorCalc.class)),
	/**  */
	OFFENSIVE_ACTION(new InstanceableClass(OffensiveActionsCalc.class)),
	/**  */
	OFFENSIVE_STRATEGY(new InstanceableClass(OffensiveStrategyCalc.class),
			GAME_STATE, BEST_GOAL_SHOT, CRUCIAL_DEFENDER, ICING_DETECTOR, DOUBLE_TOUCH_EVADER),
	/**  */
	OFFENSIVE_SITUATION(new InstanceableClass(OffensiveSituationRatingCalc.class),
			OFFENSIVE_ACTION, OFFENSIVE_STRATEGY),
	
	/**  */
	PLAY_NUMBER(new InstanceableClass(PlayNumberCalc.class),
			NUM_DEFENDER, OFFENSIVE_STRATEGY, NUM_INTERCHANGE_BOTS),
	DESIRED_INTERCHANGE(new InstanceableClass(DesiredInterchangeBotsCalc.class),
			PLAY_NUMBER),
	/** */
	DESIRED_KEEPER(new InstanceableClass(DesiredKeeperCalc.class),
			PLAY_NUMBER),
	/** */
	DESIRED_SHOOTOUT_ATTACKER(new InstanceableClass(DesiredShootoutAttackerCalc.class),
			PLAY_NUMBER),
	/** */
	DESIRED_OFFENDERS(new InstanceableClass(DesiredOffendersCalc.class),
			OFFENSIVE_STRATEGY, PLAY_NUMBER),
	/** */
	DESIRED_DEFENDERS(new InstanceableClass(DesiredDefendersCalc.class,
			new InstanceableParameter(Boolean.class, "crucial", Boolean.FALSE.toString())),
			DEFENDER_ASSIGNER),
	/**  */
	DESIRED_GAME_STATE_BOTS(new InstanceableClass(DesiredGameStateBotsCalc.class),
			DESIRED_DEFENDERS, DESIRED_KEEPER, DESIRED_OFFENDERS),
	/**  */
	DESIRED_SUPPORTERS(new InstanceableClass(DesiredSupportersCalc.class),
			DESIRED_DEFENDERS, DESIRED_OFFENDERS, DESIRED_KEEPER, DESIRED_GAME_STATE_BOTS),
	
	/**  */
	ROLE_MAPPER(new InstanceableClass(RoleMappingCalc.class), PLAY_NUMBER,
			DESIRED_DEFENDERS, DESIRED_OFFENDERS, DESIRED_SUPPORTERS, DESIRED_KEEPER, DESIRED_GAME_STATE_BOTS),
	
	/** */
	SUPPORT_POSITION_GENERATION(new InstanceableClass(SupportPositionGenerationCalc.class),
			ROLE_MAPPER, BOT_TO_BALL_DISTANCE_TIGERS),
	/** */
	SUPPORT_POSITION_RATING(new InstanceableClass(SupportPositionRatingCalc.class),
			SUPPORT_POSITION_GENERATION),
	/** */
	SUPPORT_POSITION_SELECTION(new InstanceableClass(SupportPositionSelectionCalc.class),
			SUPPORT_POSITION_RATING),
	/** */
	SUPPORT_VORONOI(new InstanceableClass(VoronoiCalc.class), OFFENSIVE_STRATEGY),
	/** */
	KICK_OFF_ACTION(new InstanceableClass(KickoffActionsCalc.class),
			SUPPORT_POSITION_SELECTION, OFFENSIVE_ACTION, PASS_TARGET_SELECTION),
	
	/** */
	PENALTY_PLACEMENT_TARGET(new InstanceableClass(PenaltyPlacementTargetCalc.class), GAME_STATE),
	/** */
	PENALTY_PLACEMENT_FILTER(new InstanceableClass(PenaltyPlacementTargetFilter.class), PENALTY_PLACEMENT_TARGET),
	
	/** */
	STATISTICS(new InstanceableClass(StatisticsCalc.class),
			GAME_STATE, BALL_POSSESSION, POSSIBLE_GOAL, BOT_BALL_CONTACT, ROLE_MAPPER),
	
	/** */
	OFFENSIVE_STATS_POST(new InstanceableClass(OffensiveStatisticsPostAnalysisCalc.class)),
	
	/** */
	MULTIMEDIA(new InstanceableClass(MultimediaCalc.class)),
	
	// ##### Test calculators (should be disabled by default
	/** */
	ANGLE_RANGE_TEST(new InstanceableClass(AngleRangeTestCalc.class), false),
	/** */
	ANGLE_RANGE_RATER_TEST(new InstanceableClass(AngleRangeRaterTestCalc.class), false),
	/** */
	ANGLE_RANGE_RATER_PASS_TARGET_TEST(new InstanceableClass(AngleRangeRaterPassTargetTestCalc.class), false),
	
	DEBUG_GRID_TEST(new InstanceableClass(DebugGridTestCalc.class), false),
	
	;
	
	
	private final boolean initiallyActive;
	private final InstanceableClass impl;
	
	
	ECalculator(final InstanceableClass impl, final ECalculator... dependencies)
	{
		this(impl, true, dependencies);
	}
	
	
	@SuppressWarnings("unused") // it's used for compilation only
	ECalculator(final InstanceableClass impl, final boolean initiallyActive,
			final ECalculator... dependencies)
	{
		this.impl = impl;
		this.initiallyActive = initiallyActive;
	}
	
	
	/**
	 * @return the initiallyActive
	 */
	public final boolean isInitiallyActive()
	{
		return initiallyActive;
	}
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return impl;
	}
}
