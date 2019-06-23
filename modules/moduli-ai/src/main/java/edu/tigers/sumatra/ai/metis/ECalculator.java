/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.ai.metis.defense.DefenderAssignerCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBallThreatCalc;
import edu.tigers.sumatra.ai.metis.defense.DefenseBotThreatCalc;
import edu.tigers.sumatra.ai.metis.defense.DesiredDefendersCalc;
import edu.tigers.sumatra.ai.metis.defense.NDefenderCalc;
import edu.tigers.sumatra.ai.metis.defense.PassReceiverCalc;
import edu.tigers.sumatra.ai.metis.general.BallLeftFieldCalc;
import edu.tigers.sumatra.ai.metis.general.BallPossessionCalc;
import edu.tigers.sumatra.ai.metis.general.BallResponsibilityCalc;
import edu.tigers.sumatra.ai.metis.general.BotBallContactCalc;
import edu.tigers.sumatra.ai.metis.general.BotToBallDistanceCalc;
import edu.tigers.sumatra.ai.metis.general.DesiredGameStateBotsCalc;
import edu.tigers.sumatra.ai.metis.general.DirectShotCalc;
import edu.tigers.sumatra.ai.metis.general.DoubleTouchEvaderCalc;
import edu.tigers.sumatra.ai.metis.general.IcingDetectorCalc;
import edu.tigers.sumatra.ai.metis.general.KickingCalc;
import edu.tigers.sumatra.ai.metis.general.MixedTeamBothTouchedBothCalc;
import edu.tigers.sumatra.ai.metis.general.MultimediaCalc;
import edu.tigers.sumatra.ai.metis.general.PlayNumberCalc;
import edu.tigers.sumatra.ai.metis.general.PossibleGoalCalc;
import edu.tigers.sumatra.ai.metis.general.PrepareThrowInCalc;
import edu.tigers.sumatra.ai.metis.general.RoleFinderCalc;
import edu.tigers.sumatra.ai.metis.general.RoleShapeCalc;
import edu.tigers.sumatra.ai.metis.general.SkirmishDetectorCalc;
import edu.tigers.sumatra.ai.metis.general.TeamGameStateCalc;
import edu.tigers.sumatra.ai.metis.keeper.ChipKickTargetCalc;
import edu.tigers.sumatra.ai.metis.keeper.DesiredKeeperCalc;
import edu.tigers.sumatra.ai.metis.keeper.KeeperStateCalc;
import edu.tigers.sumatra.ai.metis.offense.CrucialOffenderCalc;
import edu.tigers.sumatra.ai.metis.offense.DesiredOffendersCalc;
import edu.tigers.sumatra.ai.metis.offense.DesiredShootoutAttackerCalc;
import edu.tigers.sumatra.ai.metis.offense.KickoffActionsCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveStatisticsCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveStatisticsPostAnalysisCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveStrategyCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveTimeEstimationCalc;
import edu.tigers.sumatra.ai.metis.offense.OngoingPassCalc;
import edu.tigers.sumatra.ai.metis.offense.PenaltyPlacementTargetCalc;
import edu.tigers.sumatra.ai.metis.offense.PenaltyPlacementTargetFilter;
import edu.tigers.sumatra.ai.metis.offense.ShootScoreCalc;
import edu.tigers.sumatra.ai.metis.offense.ShooterCalc;
import edu.tigers.sumatra.ai.metis.offense.SupportiveAttackerPosCalc;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionsCalc;
import edu.tigers.sumatra.ai.metis.statistics.StatisticsCalc;
import edu.tigers.sumatra.ai.metis.support.DesiredSupportersCalc;
import edu.tigers.sumatra.ai.metis.support.PassTargetGenerationCalc;
import edu.tigers.sumatra.ai.metis.support.PassTargetRatingCalc;
import edu.tigers.sumatra.ai.metis.support.PassTargetSelectionCalc;
import edu.tigers.sumatra.ai.metis.support.SupportPositionGenerationCalc;
import edu.tigers.sumatra.ai.metis.support.SupportPositionRatingCalc;
import edu.tigers.sumatra.ai.metis.support.SupportPositionSelectionCalc;
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
	/**  */
	GAME_STATE(new InstanceableClass(TeamGameStateCalc.class)),
	/** */
	BOT_TO_BALL_DISTANCE_TIGERS(new InstanceableClass(BotToBallDistanceCalc.class,
			new InstanceableParameter(ETeam.class, "team", ETeam.TIGERS.name()))),
	/** */
	BOT_TO_BALL_DISTANCE_OPPONENTS(new InstanceableClass(BotToBallDistanceCalc.class,
			new InstanceableParameter(ETeam.class, "team", ETeam.OPPONENTS.name()))),
	/** */
	PASS_RECEIVER(new InstanceableClass(PassReceiverCalc.class)),
	/** */
	BALL_POSSESSION(new InstanceableClass(BallPossessionCalc.class),
			BOT_TO_BALL_DISTANCE_TIGERS, BOT_TO_BALL_DISTANCE_OPPONENTS, PASS_RECEIVER),
	/** */
	KEEPER_CALC(new InstanceableClass(KeeperStateCalc.class),
			GAME_STATE, BOT_TO_BALL_DISTANCE_OPPONENTS),
	/** */
	DEFENSE_BOT_THREATS(new InstanceableClass(DefenseBotThreatCalc.class)),
	
	/** */
	DEFENSE_BALL_THREAT(new InstanceableClass(DefenseBallThreatCalc.class),
			PASS_RECEIVER),
	/** */
	BALL_RESPONSIBILITY(new InstanceableClass(BallResponsibilityCalc.class)),
	
	/** */
	N_DEFENDER(new InstanceableClass(NDefenderCalc.class),
			BALL_POSSESSION, BALL_RESPONSIBILITY, KEEPER_CALC),
	
	/** */
	DEFENDER_ASSIGNER(new InstanceableClass(DefenderAssignerCalc.class),
			DEFENSE_BOT_THREATS, DEFENSE_BALL_THREAT, N_DEFENDER, BOT_TO_BALL_DISTANCE_OPPONENTS),
	/** */
	CRUCIAL_OFFENDER(new InstanceableClass(CrucialOffenderCalc.class)),
	
	/** */
	CRUCIAL_DEFENDER(new InstanceableClass(DesiredDefendersCalc.class, new InstanceableParameter(Boolean.class, "crucial", Boolean.TRUE.toString())),
			DEFENDER_ASSIGNER),
	/**  */
	SUPPORTIVE_ATTACKER_POS(new InstanceableClass(SupportiveAttackerPosCalc.class)),
	/** */
	POSSIBLE_GOAL(new InstanceableClass(PossibleGoalCalc.class)),
	/** */
	BOT_LAST_TOUCHED_BALL(new InstanceableClass(BotBallContactCalc.class)),
	/** */
	KICKING(new InstanceableClass(KickingCalc.class)),
	/**  */
	DIRECT_SHOT(new InstanceableClass(DirectShotCalc.class)),
	/** */
	CALCULATE_SHOT_TARGETS(new InstanceableClass(ShooterCalc.class)),
	/**  */
	SHOOT_SCORE(new InstanceableClass(ShootScoreCalc.class), false),
	/**  */
	OFFENSIVE_TIME_ESTIMATOR(new InstanceableClass(OffensiveTimeEstimationCalc.class)),
	/**  */
	BALL_LEFT_FIELD(new InstanceableClass(BallLeftFieldCalc.class)),
	/** */
	PASS_TARGET_GENERATION(new InstanceableClass(PassTargetGenerationCalc.class)),
	/** */
	PASS_TARGET_RATING(new InstanceableClass(PassTargetRatingCalc.class), PASS_TARGET_GENERATION),
	/** */
	PASS_TARGET_SELECTION(new InstanceableClass(PassTargetSelectionCalc.class), PASS_TARGET_RATING),
	/** */
	ICING_DETECTOR(new InstanceableClass(IcingDetectorCalc.class), BOT_LAST_TOUCHED_BALL),
	/** */
	DOUBLE_TOUCH_EVICTION(new InstanceableClass(DoubleTouchEvaderCalc.class), GAME_STATE, BOT_LAST_TOUCHED_BALL),
	/**  */
	OFFENSIVE_STATS(new InstanceableClass(OffensiveStatisticsCalc.class)),
	/**  */
	SKIRMISH_DETECTOR(new InstanceableClass(SkirmishDetectorCalc.class)),
	/** */
	ONGOING_PASS(new InstanceableClass(OngoingPassCalc.class)),
	/**  */
	OFFENSIVE_ACTION(new InstanceableClass(OffensiveActionsCalc.class)),
	/**  */
	OFFENSIVE_STRATEGY(new InstanceableClass(OffensiveStrategyCalc.class),
			GAME_STATE, CALCULATE_SHOT_TARGETS, CRUCIAL_DEFENDER, ICING_DETECTOR, DOUBLE_TOUCH_EVICTION),
	/**  */
	PREPARE_THROW_IN(new InstanceableClass(PrepareThrowInCalc.class), GAME_STATE),
	/**  */
	PLAY_NUMBER(new InstanceableClass(PlayNumberCalc.class), N_DEFENDER, OFFENSIVE_STRATEGY),
	/** */
	DESIRED_KEEPER(new InstanceableClass(DesiredKeeperCalc.class), PLAY_NUMBER),
	/** */
	DESIRED_SHOOTOUT_ATTACKER(new InstanceableClass(DesiredShootoutAttackerCalc.class), PLAY_NUMBER),
	/** */
	DESIRED_OFFENDERS(new InstanceableClass(DesiredOffendersCalc.class), OFFENSIVE_STRATEGY, PLAY_NUMBER),
	/** */
	DESIRED_DEFENDERS(new InstanceableClass(DesiredDefendersCalc.class, new InstanceableParameter(Boolean.class, "crucial", Boolean.FALSE.toString())),
			DEFENDER_ASSIGNER),
	/**  */
	DESIRED_GAME_STATE_BOTS(new InstanceableClass(DesiredGameStateBotsCalc.class), DESIRED_DEFENDERS,
			DESIRED_KEEPER, DESIRED_OFFENDERS),
	/**  */
	DESIRED_SUPPORTERS(new InstanceableClass(DesiredSupportersCalc.class),
			DESIRED_DEFENDERS, DESIRED_OFFENDERS, DESIRED_KEEPER, DESIRED_GAME_STATE_BOTS),
	
	/**  */
	ROLE_FINDER(new InstanceableClass(RoleFinderCalc.class), PLAY_NUMBER,
			DESIRED_DEFENDERS, DESIRED_OFFENDERS, DESIRED_SUPPORTERS, DESIRED_KEEPER, DESIRED_GAME_STATE_BOTS),
	/** */
	SUPPORT_POSITION_GENERATION(new InstanceableClass(SupportPositionGenerationCalc.class), ROLE_FINDER,
			BOT_TO_BALL_DISTANCE_TIGERS),
	/** */
	SUPPORT_POSITION_RATING(new InstanceableClass(SupportPositionRatingCalc.class), SUPPORT_POSITION_GENERATION),
	/** */
	SUPPORT_POSITION_SELECTION(new InstanceableClass(SupportPositionSelectionCalc.class), SUPPORT_POSITION_RATING),
	/** */
	KICK_OFF_ACTION(new InstanceableClass(KickoffActionsCalc.class),
			SUPPORT_POSITION_SELECTION, OFFENSIVE_ACTION, PASS_TARGET_SELECTION),
	/** */
	LED(new InstanceableClass(MultimediaCalc.class)),
	/** Checks if both subteams have touched the ball */
	MIXED_TEAM_BOTH_TOUCHED(new InstanceableClass(MixedTeamBothTouchedBothCalc.class),
			GAME_STATE, BOT_LAST_TOUCHED_BALL),
	
	/** */
	PENALTY_PLACEMENT_TARGET(new InstanceableClass(PenaltyPlacementTargetCalc.class), GAME_STATE),
	/** */
	PENALTY_PLACEMENT_FILTER(new InstanceableClass(PenaltyPlacementTargetFilter.class), PENALTY_PLACEMENT_TARGET),
	
	/** */
	STATISTICS(new InstanceableClass(StatisticsCalc.class),
			GAME_STATE, BALL_POSSESSION, POSSIBLE_GOAL, BOT_LAST_TOUCHED_BALL, ROLE_FINDER),
	
	/** */
	OFFENSIVE_STATS_POST(new InstanceableClass(OffensiveStatisticsPostAnalysisCalc.class)),
	
	/** */
	ROLE_COLOR(new InstanceableClass(RoleShapeCalc.class), ROLE_FINDER),
	
	/** */
	CHIP_KICK_TARGET(new InstanceableClass(ChipKickTargetCalc.class));
	
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
