/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 7, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;

import java.util.Arrays;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefenseCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BallLeftFieldCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BallPossessionCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotInformationCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotLastTouchedBallCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotToBallDistanceCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BufferCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.GameBehaviorCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.GameStateCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.LetterCalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.MixedTeamBothTouchedBothCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.PossibleGoalCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.RoleFinderCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.StatisticsCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensiveActionsCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensiveMovePositionsCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensiveStrategyCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.ShooterCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.PassTargetCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.RedirectPosGPUCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.SupportCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.test.AiMathTestCalc;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableParameter;


/**
 * This Enum contains all calculators by name
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public enum ECalculator implements IInstanceableEnum
{
	/**  */
	LETTER(new InstanceableClass(LetterCalculator.class), false),
	/**  */
	BUFFER(new InstanceableClass(BufferCalc.class)),
	/**  */
	GAME_STATE(new InstanceableClass(GameStateCalc.class)),
	/**  */
	GAME_BEHAVIOR(new InstanceableClass(GameBehaviorCalc.class)),
	/** */
	BOT_TO_BALL_DISTANCE_TIGERS(new InstanceableClass(BotToBallDistanceCalc.class, new InstanceableParameter(
			ETeam.class, "team", ETeam.TIGERS.name()))),
	/** */
	BOT_TO_BALL_DISTANCE_OPPONENTS(new InstanceableClass(BotToBallDistanceCalc.class, new InstanceableParameter(
			ETeam.class, "team", ETeam.OPPONENTS.name()))),
	/** */
	DEFENSE_CALC(new InstanceableClass(DefenseCalc.class), GAME_STATE),
	/** */
	BALL_POSSESSION(new InstanceableClass(BallPossessionCalc.class), BOT_TO_BALL_DISTANCE_TIGERS,
			BOT_TO_BALL_DISTANCE_OPPONENTS),
	/** */
	POSSIBLE_GOAL(new InstanceableClass(PossibleGoalCalc.class)),
	/** */
	BOT_LAST_TOUCHED_BALL(new InstanceableClass(BotLastTouchedBallCalc.class)),
	/**  */
	SHOOTER_MEMORY(new InstanceableClass(ShooterCalc.class)),
	/** */
	OFFENSE_MOVE_POSITIONS(new InstanceableClass(OffensiveMovePositionsCalc.class)),
	/**  */
	BOT_INFORMATION(new InstanceableClass(BotInformationCalc.class)),
	/**  */
	BALL_LEFT_FIELD(new InstanceableClass(BallLeftFieldCalc.class)),
	/**  */
	REDIRECT_POS_GPU(new InstanceableClass(RedirectPosGPUCalc.class), BALL_POSSESSION),
	/** */
	PASS_TARGETS(new InstanceableClass(PassTargetCalc.class), REDIRECT_POS_GPU),
	/** */
	SUPPORT(new InstanceableClass(SupportCalc.class), REDIRECT_POS_GPU, PASS_TARGETS, GAME_STATE),
	/**  */
	OFFENSIVE_STRATEGY(new InstanceableClass(OffensiveStrategyCalc.class), GAME_STATE, OFFENSE_MOVE_POSITIONS,
			SHOOTER_MEMORY),
	/**  */
	OFFENSIVE_ACTION(new InstanceableClass(OffensiveActionsCalc.class)),
	/**  */
	ROLE_FINDER(new InstanceableClass(RoleFinderCalc.class), GAME_BEHAVIOR),
	/** */
	// BIG_DATA(new InstanceableClass(BigDataCalc.class)),
	/**  */
	AI_MATH_TEST(new InstanceableClass(AiMathTestCalc.class), false),
	/** Checks if both subteams have touched the ball */
	MIXED_TEAM_BOTH_TOUCHED(new InstanceableClass(MixedTeamBothTouchedBothCalc.class), GAME_STATE, BOT_LAST_TOUCHED_BALL),
	/** */
	STATISTICS(new InstanceableClass(StatisticsCalc.class), GAME_STATE, BALL_POSSESSION, POSSIBLE_GOAL,
			BOT_LAST_TOUCHED_BALL, ROLE_FINDER),
	
	/** berkeley support */
	/** */
	@Deprecated
	BALL_MOVEMENT_ANALYSER(new InstanceableClass(null), false),
	/** */
	@Deprecated
	DEFENSE_POINTS(new InstanceableClass(null), false),
	/** */
	@Deprecated
	SUPPORT_POSITIONS(new InstanceableClass(null), false),
	/**  */
	@Deprecated
	REDIRECT_POSITIONS(new InstanceableClass(null), false),
	/**  */
	@Deprecated
	SUPPORT_POSITIONS_GPU(new InstanceableClass(null), false),
	/**  */
	@Deprecated
	BEST_PASSRECEIVER(new InstanceableClass(null), false),
	/** */
	@Deprecated
	DEFENSE_CALC2(new InstanceableClass(null), false),
	/**  */
	@Deprecated
	BIG_DATA(new InstanceableClass(null), false);
	
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
	 * @return the impl
	 */
	public final InstanceableClass getImpl()
	{
		return impl;
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
