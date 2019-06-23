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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePointsCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.FieldAnalyserCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BallLeftFieldCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BallPossessionCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotInformationCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotLastTouchedBallCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotToBallDistanceCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.GameStateCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.PossibleGoalCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.StatisticsCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffenseMovePositionsCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.ShooterCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.PassCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.RedirectPosGPUCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.RedirectPosRefCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.SupportPositionGPUCalc;
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
	GAME_STATE(new InstanceableClass(GameStateCalc.class)),
	/** */
	BOT_TO_BALL_DISTANCE_TIGERS(new InstanceableClass(BotToBallDistanceCalc.class, new InstanceableParameter(
			ETeam.class, "team", ETeam.TIGERS.name()))),
	/** */
	BOT_TO_BALL_DISTANCE_OPPONENTS(new InstanceableClass(BotToBallDistanceCalc.class, new InstanceableParameter(
			ETeam.class, "team", ETeam.OPPONENTS.name()))),
	/** */
	DEFENSE_POINTS(new InstanceableClass(DefensePointsCalc.class)),
	/** */
	BALL_POSSESSION(new InstanceableClass(BallPossessionCalc.class), BOT_TO_BALL_DISTANCE_TIGERS,
			BOT_TO_BALL_DISTANCE_OPPONENTS),
	/** */
	POSSIBLE_GOAL(new InstanceableClass(PossibleGoalCalc.class)),
	/** */
	BOT_LAST_TOUCHED_BALL(new InstanceableClass(BotLastTouchedBallCalc.class)),
	/** */
	FIELD_ANALYSER(new InstanceableClass(FieldAnalyserCalc.class), false),
	/**  */
	SHOOTER_MEMORY(new InstanceableClass(ShooterCalc.class)),
	/** */
	OFFENSE_MOVE_POSITIONS(new InstanceableClass(OffenseMovePositionsCalc.class)),
	/**  */
	BOT_INFORMATION(new InstanceableClass(BotInformationCalc.class)),
	/**  */
	BALL_LEFT_FIELD(new InstanceableClass(BallLeftFieldCalc.class)),
	/**  */
	BEST_PASSRECEIVER(new InstanceableClass(PassCalc.class), SHOOTER_MEMORY),
	/**  */
	REDIRECT_POSITIONS(new InstanceableClass(RedirectPosRefCalc.class)),
	/**  */
	REDIRECT_POS_GPU(new InstanceableClass(RedirectPosGPUCalc.class), BALL_POSSESSION),
	/**  */
	SUPPORT_POSITIONS_GPU(new InstanceableClass(SupportPositionGPUCalc.class), REDIRECT_POSITIONS, REDIRECT_POS_GPU),
	/** */
	STATISTICS(new InstanceableClass(StatisticsCalc.class), GAME_STATE, BALL_POSSESSION, POSSIBLE_GOAL,
			BOT_LAST_TOUCHED_BALL);
	
	
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
