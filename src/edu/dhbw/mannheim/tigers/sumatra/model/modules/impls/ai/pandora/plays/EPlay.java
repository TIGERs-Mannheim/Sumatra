/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.AroundTheBallPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.CheeringPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.GuiTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.InitPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.MaintenancePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.MoveTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges.NavigationChallengePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test.DebugShapesTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test.TecShootChalTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EPlay implements IInstanceableEnum
{
	/**  */
	OFFENSIVE(new InstanceableClass(OffensivePlay.class)),
	/**  */
	DEFENSIVE(new InstanceableClass(DefensePlay.class)),
	/**  */
	SUPPORT(new InstanceableClass(SupportPlay.class)),
	/**  */
	KEEPER(new InstanceableClass(KeeperPlay.class)),
	
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
	NAVIGATION_CHALLENCE(new InstanceableClass(NavigationChallengePlay.class)),
	/**  */
	DEBUG_SHAPES(new InstanceableClass(DebugShapesTestPlay.class)),
	/**  */
	GUI_TEST(new InstanceableClass(GuiTestPlay.class));
	
	private final InstanceableClass	clazz;
	
	
	/**
	 */
	private EPlay(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
