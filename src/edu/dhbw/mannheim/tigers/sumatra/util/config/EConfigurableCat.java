/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 25, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.ProbabilityMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SplineMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefenseCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.RoleFinderCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensiveConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.DestinationCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.DestinationFreeCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.ViewAngleCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.TargetVisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.VisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.DoubleDefPointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.DriveOnLinePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.ShortestPathDefensePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers.ReceiverBlockPointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers.ZoneDefensePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support.ASupportPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support.GPUGridSupportPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support.RandomSupportPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderThread;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.AroundBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.HermiteSplinePathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.KickBallChillTrajDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.KickBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.LongPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.MixedPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.PathPointDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.PositionDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.PushBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.SplinePathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.TurnWithBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.TuneableParameter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.FieldInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathNode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.CatchBallObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.SplineGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.BotNotOnSplineDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.CollisionDetectionDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.DestinationChangedDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.NewPathShorterDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.SplineEndGoalNotReachedDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.BotManagerV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.GrSimBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Performance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.CommandInterpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.PollingService;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario.ESimulationScenario;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.LinearPolicy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillExecutor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.SyncedCamFrameBufferV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BallCorrector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.KalmanWorldFramePacker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.NeuralWP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralBallState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralNetworkImpl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralRobotState;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;


/**
 * Categories for configurables
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EConfigurableCat
{
	/**  */
	USER(null, UserConfig.class),
	/**  */
	TEAM(null, TeamConfig.class),
	/**  */
	GEOM(null, Geometry.class),
	/**  */
	BOTMGR(null, ABot.class, TigerBot.class, GrSimBot.class, TigerBotV3.class, BaseStation.class,
			BotManagerV2.class, Performance.class),
	/**  */
	METIS(ECalculator.class),
	/**  */
	OFFENSIVE(null, OffensiveConstants.class),
	/** */
	DEFENSIVE(null, DriveOnLinePointCalc.class, DoubleDefPointCalc.class, ZoneDefensePointCalc.class,
			ReceiverBlockPointCalc.class, RoleFinderCalc.class, DefenseCalc.class),
	/**  */
	PLAYS(EPlay.class, ShortestPathDefensePointCalc.class),
	/**  */
	ROLES(ERole.class, ASupportPosition.class, RandomSupportPosition.class, GPUGridSupportPosition.class),
	/**  */
	SKILLS(ESkillName.class, TigerDevices.class, SkillExecutor.class, LinearPolicy.class),
	/**  */
	SISYPHUS(null, Sisyphus.class, TuneableParameter.class, DestinationChangedDecisionMaker.class,
			BotNotOnSplineDecisionMaker.class, NewPathShorterDecisionMaker.class,
			SplineEndGoalNotReachedDecisionMaker.class, CollisionDetectionDecisionMaker.class, FieldInformation.class,
			SplineGenerator.class, MixedPathDriver.class, PathPointDriver.class, PositionDriver.class,
			LongPathDriver.class, SplinePathDriver.class, HermiteSplinePathDriver.class, PathFinderThread.class,
			AroundBallDriver.class,
			PushBallDriver.class, TrajPathFinder.class, TrajPathNode.class,
			ObstacleGenerator.class, CatchBallObstacle.class, KickBallChillTrajDriver.class, TrajPathFinderInput.class,
			KickBallDriver.class, TurnWithBallDriver.class),
	
	/**  */
	RCM(null, CommandInterpreter.class, PollingService.class),
	/**  */
	WP(null, NeuralNetworkImpl.class, WPFacade.class, SyncedCamFrameBufferV2.class, TrackedBall.class,
			KalmanWorldFramePacker.class, NeuralWP.class, NeuralBallState.class, NeuralRobotState.class,
			PredictionContext.class, SyncedCamFrameBufferV2.class, BallCorrector.class),
	/**  */
	CONDITIONS(null, DestinationCondition.class, DestinationFreeCondition.class, MovementCon.class,
			ViewAngleCondition.class, TargetVisibleCon.class, VisibleCon.class),
	/**  */
	MATH(null, AiMath.class, GeoMath.class, SumatraMath.class, SplineMath.class, ProbabilityMath.class),
	/**  */
	SIMULATION(ESimulationScenario.class), ;
	
	private final Class<? extends Enum<? extends IInstanceableEnum>>	enumClazz;
	private final Set<Class<?>>													classes;
	
	
	private EConfigurableCat()
	{
		this(null);
	}
	
	
	private EConfigurableCat(final Class<? extends Enum<? extends IInstanceableEnum>> enumClazz,
			final Class<?>... classes)
	{
		this.enumClazz = enumClazz;
		this.classes = new LinkedHashSet<Class<?>>(Arrays.asList(classes));
	}
	
	
	/**
	 * @return the enumClazz
	 */
	public final Class<? extends Enum<? extends IInstanceableEnum>> getEnumClazz()
	{
		return enumClazz;
	}
	
	
	/**
	 * @return the classes
	 */
	public final Set<Class<?>> getClasses()
	{
		return Collections.unmodifiableSet(classes);
	}
}
