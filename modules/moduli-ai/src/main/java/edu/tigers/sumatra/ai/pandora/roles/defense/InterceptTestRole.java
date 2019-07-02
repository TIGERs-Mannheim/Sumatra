/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.states.InterceptState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 *         Foe testing the InterceptState of the Defense
 */
public class InterceptTestRole extends ADefenseRole
{
	private DynamicPosition toIntercept;
	
	
	/**
	 * @param toIntercept
	 * @param toProtect
	 */
	public InterceptTestRole(DynamicPosition toIntercept, DynamicPosition toProtect)
	{
		
		super(ERole.INTERCEPT_TEST);
		this.toIntercept = toIntercept;
		
		IState interceptState = new MyState(this, toIntercept, toProtect);
		setInitialState(interceptState);
	}
	
	private class MyState extends InterceptState
	{
		
		/**
		 * State used by defender Roles
		 * possible settings:
		 * 2. intersept line between ball and foeBot
		 * 3. intersept line between ball and goal center
		 * 4. intersept line between foeBot and goal center
		 * 5. intersept line between two foeBots
		 * 6. intercept line between two points
		 *
		 * @param parent role that executes this state
		 * @param toIntercept start point of interception line: ball or bot (or any dynamic position)
		 * @param toProtect end point of interception line: bot or goal (or any dynamic position)
		 */
		public MyState(final ADefenseRole parent, final DynamicPosition toIntercept, final DynamicPosition toProtect)
		{
			super(parent, toIntercept, toProtect);
			setLookahead(0.2);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (toIntercept.getTrackedId().isBot())
			{
				getCurrentSkill().getMoveCon()
						.setTheirBotsObstacle(toIntercept.getPos().distanceTo(getPos()) < 4 * Geometry.getBotRadius());
			}
			super.doUpdate();
		}
	}
}
