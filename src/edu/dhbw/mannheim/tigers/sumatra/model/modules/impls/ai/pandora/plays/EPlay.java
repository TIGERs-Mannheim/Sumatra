/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.DefensePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.learning.LearningPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.AroundTheBallPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.CheeringPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.DebugShapesTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.FormationMovingPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.FormationStaticPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.GuiTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.InitPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.MaintenancePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.MoveTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.PathPlanningTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.TecShootChalTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.redirect.PassingPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.redirect.RedirectCirclePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.redirect.RedirectDualPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.redirect.RedirectRectanglePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.redirect.RedirectTrianglePlay;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EPlay implements IInstanceableEnum
{
	/**  */
	OFFENSIVE(new InstanceableClass(OffensivePlay.class), 20, 20),
	/**  */
	KICKOFF(new InstanceableClass(KickoffPlay.class), 10, 10),
	/** */
	DEFENSIVE(new InstanceableClass(DefensePlay.class), 30, 30),
	/**  */
	SUPPORT(new InstanceableClass(SupportPlay.class), 40, 40),
	/**  */
	KEEPER(new InstanceableClass(KeeperPlay.class), 1, 1),
	/**  */
	LEARNING_PLAY(new InstanceableClass(LearningPlay.class)),
	/**  */
	PENALTY_THEM(new InstanceableClass(PenaltyThemPlay.class), 5, 5),
	/**  */
	PENALTY_WE(new InstanceableClass(PenaltyWePlay.class), 5, 5),
	/**  */
	AROUND_THE_BALL(new InstanceableClass(AroundTheBallPlay.class)),
	/**  */
	INIT(new InstanceableClass(InitPlay.class)),
	/**  */
	MAINTENANCE(new InstanceableClass(MaintenancePlay.class)),
	/**  */
	CHEERING(new InstanceableClass(CheeringPlay.class)),
	/**  */
	STUPID_DEFENDERS(new InstanceableClass(TecShootChalTestPlay.class)),
	/**  */
	MOVE_TEST(new InstanceableClass(MoveTestPlay.class)),
	/**  */
	DEBUG_SHAPES(new InstanceableClass(DebugShapesTestPlay.class)),
	/**  */
	GUI_TEST(new InstanceableClass(GuiTestPlay.class)),
	/**  */
	REDIRECT_CIRCLE(new InstanceableClass(RedirectCirclePlay.class), 0, 0),
	/**  */
	REDIRECT_ANGLE(new InstanceableClass(RedirectRectanglePlay.class), 0, 0),
	/**  */
	REDIRECT_DUAL(new InstanceableClass(RedirectDualPlay.class), 0, 0),
	/**  */
	REDIRECT_TRIANGLE(new InstanceableClass(RedirectTrianglePlay.class), 0, 0),
	/**  */
	PASSING(new InstanceableClass(PassingPlay.class), 0, 0),
	
	/**  */
	FORMATION_STATIC(new InstanceableClass(FormationStaticPlay.class)),
	/**  */
	FORMATION_MOVING(new InstanceableClass(FormationMovingPlay.class)),
	
	/**  */
	PATH_PLANNING_TEST(new InstanceableClass(PathPlanningTestPlay.class)),
	
	/** Berkeley support */
	@Deprecated
	LETTER(new InstanceableClass(Object.class)),
	/**  */
	@Deprecated
	NAVIGATION_CHALLENCE(new InstanceableClass(Object.class)),
	/**  */
	@Deprecated
	REDIRECT_TEST(new InstanceableClass(Object.class)),
	/**  */
	@Deprecated
	CHEERING_SPECIAL(new InstanceableClass(Object.class)),
	/**  */
	@Deprecated
	DEFENSIVEV2(new InstanceableClass(Object.class));
	
	private final InstanceableClass				clazz;
	private final Map<EGameBehavior, Integer>	prio	= new HashMap<EGameBehavior, Integer>();
	
	
	/**
	 */
	private EPlay(final InstanceableClass clazz)
	{
		this(clazz, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	
	private EPlay(final InstanceableClass clazz, final int prioOffensive, final int prioDefensive)
	{
		this.clazz = clazz;
		prio.put(EGameBehavior.OFFENSIVE, prioOffensive);
		prio.put(EGameBehavior.DEFENSIVE, prioDefensive);
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
	
	
	/**
	 * @param gameBehavior
	 * @return the prioOffensive
	 */
	public int getPrio(final EGameBehavior gameBehavior)
	{
		return prio.get(gameBehavior);
	}
}
