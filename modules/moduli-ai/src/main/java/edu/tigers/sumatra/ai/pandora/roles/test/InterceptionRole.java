/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * The InterceptionRole tries the block enemy Offensive Bots while
 * they prepare indirect Shoots.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class InterceptionRole extends ARole
{
	/**
	 */
	public InterceptionRole()
	{
		super(ERole.INTERCEPTION);
		
		IState blocker = new BlockerState();
		setInitialState(blocker);
	}
	
	
	private class BlockerState implements IState {
		InterceptionSkill	skill	= new InterceptionSkill();
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			BotDistance nearestEnemyBot = getAiFrame().getTacticalField().getEnemyClosestToBall();
			if (nearestEnemyBot != null)
			{
				if (nearestEnemyBot.getBot() != null)
				{
					skill.setNearestEnemyBotPos(nearestEnemyBot.getBot().getPos());
				} else
				{
					skill.setNearestEnemyBotPos(null);
				}
			}
			else
			{
				skill.setNearestEnemyBotPos(null);
			}
		}


    }
}
