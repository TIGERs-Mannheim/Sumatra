/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.ai.pandora.plays.defense.DefensePlay;
import edu.tigers.sumatra.ai.pandora.plays.learning.LearningPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.AroundTheBallPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.ExchangePositioningPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.FormationMovingPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.FormationStaticPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.GuiTestPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.InitPlay;
import edu.tigers.sumatra.ai.pandora.plays.others.MaintenancePlay;
import edu.tigers.sumatra.ai.pandora.plays.positioning.PositioningPlay;
import edu.tigers.sumatra.ai.pandora.plays.redirect.PassingPlay;
import edu.tigers.sumatra.ai.pandora.plays.redirect.RedirectCirclePlay;
import edu.tigers.sumatra.ai.pandora.plays.redirect.RedirectDualPlay;
import edu.tigers.sumatra.ai.pandora.plays.redirect.RedirectDynamicTrianglePlay;
import edu.tigers.sumatra.ai.pandora.plays.redirect.RedirectRectanglePlay;
import edu.tigers.sumatra.ai.pandora.plays.redirect.RedirectTrianglePlay;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EPlay implements IInstanceableEnum
{
	/**  */
	OFFENSIVE(new InstanceableClass(OffensivePlay.class), 20),
	/**  */
	AUTOMATED_THROW_IN(new InstanceableClass(AutomatedThrowInPlay.class), 10),
	/**  */
	KICKOFF(new InstanceableClass(KickoffPlay.class), 10),
	/** */
	DEFENSIVE(new InstanceableClass(DefensePlay.class), 30),
	/**  */
	SUPPORT(new InstanceableClass(SupportPlay.class), 40),
	/**  */
	KEEPER(new InstanceableClass(KeeperPlay.class), 1),
	/**  */
	LEARNING_PLAY(new InstanceableClass(LearningPlay.class)),
	/**  */
	ATTACKER_SHOOTOUT(new InstanceableClass(OneOnOneShootoutPlay.class), 1),
    /** */
    KEEPER_SHOOTOUT(new InstanceableClass(KeeperShootoutPlay.class), 1),
    /**  */
	PENALTY_THEM(new InstanceableClass(PenaltyThemPlay.class), 5),
	/**  */
	PENALTY_WE(new InstanceableClass(PenaltyWePlay.class), 5),
	/**  */
	AROUND_THE_BALL(new InstanceableClass(AroundTheBallPlay.class)),
	/**  */
	INIT(new InstanceableClass(InitPlay.class)),
	/**  */
	MAINTENANCE(new InstanceableClass(MaintenancePlay.class)),
	/**  */
	EXCHANGE_POSITIONING(new InstanceableClass(ExchangePositioningPlay.class)),
	/**  */
	CHEERING(new InstanceableClass(CheeringPlay.class)),
	/**  */
	GUI_TEST(new InstanceableClass(GuiTestPlay.class)),
	/**  */
	REDIRECT_CIRCLE(new InstanceableClass(RedirectCirclePlay.class), 0),
	/**  */
	REDIRECT_ANGLE(new InstanceableClass(RedirectRectanglePlay.class), 0),
	/**  */
	REDIRECT_DUAL(new InstanceableClass(RedirectDualPlay.class), 0),
	/**  */
	REDIRECT_TRIANGLE(new InstanceableClass(RedirectTrianglePlay.class), 0),
	/**  */
	REDIRECT_DYNAMIC_TRIANGLE(new InstanceableClass(RedirectDynamicTrianglePlay.class), 0),
	/**  */
	PASSING(new InstanceableClass(PassingPlay.class), 0),
	
	/**  */
	FORMATION_STATIC(new InstanceableClass(FormationStaticPlay.class)),
	/**  */
	FORMATION_MOVING(new InstanceableClass(FormationMovingPlay.class)),

	/**  */
	POSITIONING_PLAY(new InstanceableClass(PositioningPlay.class)),
	
	/**  */
	@Deprecated
	REDIRECT_TEST(new InstanceableClass(Object.class));
	
	private final InstanceableClass	clazz;
	private int								priority;
	
	
	/**
	 */
	EPlay(final InstanceableClass clazz)
	{
		this(clazz, Integer.MAX_VALUE);
	}
	
	
	EPlay(final InstanceableClass clazz, final int priority)
	{
		this.clazz = clazz;
		this.priority = priority;
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
	 * @return the prioOffensive
	 */
	public int getPrio()
	{
		return priority;
	}
}
