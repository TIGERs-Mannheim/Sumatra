/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 7, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis;

import java.util.Arrays;
import java.util.List;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.ai.metis.defense.AngleDefenseCalc;
import edu.tigers.sumatra.ai.metis.defense.KeeperStateCalc;
import edu.tigers.sumatra.ai.metis.general.BallLeftFieldCalc;
import edu.tigers.sumatra.ai.metis.general.BallPossessionCalc;
import edu.tigers.sumatra.ai.metis.general.BotInformationCalc;
import edu.tigers.sumatra.ai.metis.general.BotLastTouchedBallCalc;
import edu.tigers.sumatra.ai.metis.general.BotToBallDistanceCalc;
import edu.tigers.sumatra.ai.metis.general.GameBehaviorCalc;
import edu.tigers.sumatra.ai.metis.general.GameEventCalc;
import edu.tigers.sumatra.ai.metis.general.LedCalc;
import edu.tigers.sumatra.ai.metis.general.MixedTeamBothTouchedBothCalc;
import edu.tigers.sumatra.ai.metis.general.PossibleGoalCalc;
import edu.tigers.sumatra.ai.metis.general.PrepareThrowinCalc;
import edu.tigers.sumatra.ai.metis.general.RoleFinderCalc;
import edu.tigers.sumatra.ai.metis.general.StatisticsCalc;
import edu.tigers.sumatra.ai.metis.general.TeamGameStateCalc;
import edu.tigers.sumatra.ai.metis.offense.KickOffActionsCalc;
import edu.tigers.sumatra.ai.metis.offense.KickSkillTimingCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveActionsCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMovePositionsCalc;
import edu.tigers.sumatra.ai.metis.offense.OffensiveStrategyCalc;
import edu.tigers.sumatra.ai.metis.offense.ShooterCalc;
import edu.tigers.sumatra.ai.metis.support.CPUPassTargetCalc;
import edu.tigers.sumatra.ai.metis.support.CPUSupporterCalc;
import edu.tigers.sumatra.ai.metis.test.AiMathTestCalc;
import edu.tigers.sumatra.ai.metis.vis.BotAvailableVisCalc;
import edu.tigers.sumatra.ai.metis.vis.CoordinateSystemVisCalc;
import edu.tigers.sumatra.ai.metis.vis.RoleNameVisCalc;
import edu.tigers.sumatra.ids.ETeam;


/**
 * This Enum contains all calculators by name
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public enum ECalculator implements IInstanceableEnum
{
	/**  */
	GAME_STATE(new InstanceableClass(TeamGameStateCalc.class)),
	/**  */
	GAME_BEHAVIOR(new InstanceableClass(GameBehaviorCalc.class)),
	/** */
	BOT_TO_BALL_DISTANCE_TIGERS(new InstanceableClass(BotToBallDistanceCalc.class, new InstanceableParameter(
			ETeam.class, "team", ETeam.TIGERS.name()))),
	/** */
	BOT_TO_BALL_DISTANCE_OPPONENTS(new InstanceableClass(BotToBallDistanceCalc.class, new InstanceableParameter(
			ETeam.class, "team", ETeam.OPPONENTS.name()))),
	
	// /** */
	// DIRECT_SHOT_DEFENSE_CALC(new InstanceableClass(DirectShotDefenseCalc.class), GAME_STATE),
	
	/**  */
	ANGLE_DEFENSE_CALC(new InstanceableClass(AngleDefenseCalc.class), GAME_STATE),
	/** */
	KEEPER_CALC(new InstanceableClass(KeeperStateCalc.class), GAME_STATE),
	/** */
	BALL_POSSESSION(new InstanceableClass(BallPossessionCalc.class), BOT_TO_BALL_DISTANCE_TIGERS,
			BOT_TO_BALL_DISTANCE_OPPONENTS),
	/** */
	POSSIBLE_GOAL(new InstanceableClass(PossibleGoalCalc.class)),
	/** */
	BOT_LAST_TOUCHED_BALL(new InstanceableClass(BotLastTouchedBallCalc.class)),
	/**  */
	// BALL_HEADING(new InstanceableClass(BallHeadingCalc.class)),
	/**  */
	SHOOTER_MEMORY(new InstanceableClass(ShooterCalc.class)),
	/**  */
	KICK_SKILL_TIMING(new InstanceableClass(KickSkillTimingCalc.class)),
	/**  */
	BOT_INFORMATION(new InstanceableClass(BotInformationCalc.class)),
	/**  */
	BALL_LEFT_FIELD(new InstanceableClass(BallLeftFieldCalc.class)),
	/**  */
	// REDIRECT_POS_GPU(new InstanceableClass(RedirectPosGPUCalc.class), BALL_POSSESSION),
	/** */
	// PASS_TARGETS(new InstanceableClass(PassTargetCalc.class), REDIRECT_POS_GPU),
	/** */
	CPU_PASS_TARGETS(new InstanceableClass(CPUPassTargetCalc.class)),
	/**  */
	OFFENSIVE_ACTION(new InstanceableClass(OffensiveActionsCalc.class)),
	/** */
	OFFENSE_MOVE_POSITIONS(new InstanceableClass(OffensiveMovePositionsCalc.class), OFFENSIVE_ACTION),
	/**  */
	OFFENSIVE_STRATEGY(new InstanceableClass(OffensiveStrategyCalc.class), GAME_STATE, OFFENSE_MOVE_POSITIONS,
			SHOOTER_MEMORY),
	/**  */
	PREPARE_THROW_IN(new InstanceableClass(PrepareThrowinCalc.class)),
	/**  */
	ROLE_FINDER(new InstanceableClass(RoleFinderCalc.class), GAME_BEHAVIOR),
	/** */
	CPU_SUPPORTER(new InstanceableClass(CPUSupporterCalc.class), ROLE_FINDER), // TEST
	/** */
	KICK_OFF_ACTION(new InstanceableClass(KickOffActionsCalc.class), CPU_SUPPORTER, OFFENSIVE_ACTION),
	/** */
	// SUPPORT(new InstanceableClass(SupportCalc.class), REDIRECT_POS_GPU, CPU_PASS_TARGETS, GAME_STATE, CPU_SUPPORTER),
	/** */
	// BIG_DATA(new InstanceableClass(BigDataCalc.class), false),
	/** */
	LED(new InstanceableClass(LedCalc.class)),
	/**  */
	AI_MATH_TEST(new InstanceableClass(AiMathTestCalc.class), false),
	/** Checks if both subteams have touched the ball */
	MIXED_TEAM_BOTH_TOUCHED(new InstanceableClass(MixedTeamBothTouchedBothCalc.class), GAME_STATE,
			BOT_LAST_TOUCHED_BALL),
	
	/** Tracks events that happen during the game */
	GAME_EVENTS(new InstanceableClass(GameEventCalc.class), GAME_STATE, BALL_POSSESSION, POSSIBLE_GOAL,
			BOT_LAST_TOUCHED_BALL, ROLE_FINDER),
	/** */
	STATISTICS(new InstanceableClass(StatisticsCalc.class), GAME_STATE, BALL_POSSESSION, POSSIBLE_GOAL,
			BOT_LAST_TOUCHED_BALL, ROLE_FINDER, GAME_EVENTS),
	
	/**  */
	ROLE_NAME_VIS(new InstanceableClass(RoleNameVisCalc.class), BOT_INFORMATION),
	/**  */
	BOT_AVAILABLE_VIS(new InstanceableClass(BotAvailableVisCalc.class)),
	/**  */
	COORDINATE_SYSTEM_VIS(new InstanceableClass(CoordinateSystemVisCalc.class));
	
	private final boolean				initiallyActive;
	private final long					timeRateMs;
	private final InstanceableClass	impl;
	private final List<ECalculator>	dependencies;
	
	
	private ECalculator(final InstanceableClass impl, final ECalculator... dependencies)
	{
		this(impl, 0, true, dependencies);
	}
	
	
	private ECalculator(final InstanceableClass impl, final long timeRateMs, final ECalculator... dependencies)
	{
		this(impl, timeRateMs, true, dependencies);
	}
	
	
	private ECalculator(final InstanceableClass impl, final boolean initiallyActive, final ECalculator... dependencies)
	{
		this(impl, 0, initiallyActive, dependencies);
	}
	
	
	private ECalculator(final InstanceableClass impl, final long timeRateMs, final boolean initiallyActive,
			final ECalculator... dependencies)
	{
		this.impl = impl;
		this.timeRateMs = timeRateMs;
		this.initiallyActive = initiallyActive;
		this.dependencies = Arrays.asList(dependencies);
	}
	
	
	/**
	 * @return the initiallyActive
	 */
	public final boolean isInitiallyActive()
	{
		return initiallyActive;
	}
	
	
	/**
	 * @return the timeRateMs
	 */
	public final long getTimeRateMs()
	{
		return timeRateMs;
	}
	
	
	/**
	 * @return the dependencies
	 */
	public final List<ECalculator> getDependencies()
	{
		return dependencies;
	}
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return impl;
	}
}
