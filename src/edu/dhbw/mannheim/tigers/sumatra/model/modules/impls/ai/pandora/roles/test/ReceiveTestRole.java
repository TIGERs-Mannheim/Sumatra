/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ReceiverSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReceiveTestRole extends ARole
{
	private final IVector2			dest;
	private final DynamicPosition	target;
	
	private enum EStateId
	{
		RECEIVE,
		KICK,
		INIT
	}
	
	private enum EEvent
	{
		RECEIVED,
		KICKED,
		INIT;
	}
	
	
	/**
	 * @param dest
	 * @param target
	 */
	public ReceiveTestRole(final IVector2 dest, final DynamicPosition target)
	{
		super(ERole.RECEIVE_TEST);
		this.dest = dest;
		this.target = target;
		
		IRoleState receiveState = new ReceiveState();
		IRoleState kickState = new KickState();
		IRoleState initState = new InitState();
		setInitialState(initState);
		addTransition(EEvent.RECEIVED, kickState);
		addTransition(EEvent.KICKED, initState);
		addTransition(EEvent.INIT, receiveState);
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	
	private class InitState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			IMoveToSkill move = AMoveSkill.createMoveToSkill();
			move.getMoveCon().updateDestination(dest);
			setNewSkill(move);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (GeoMath.distancePP(dest, getPos()) < 50)
			{
				triggerEvent(EEvent.INIT);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.INIT;
		}
	}
	
	private class ReceiveState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new ReceiverSkill());
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			triggerEvent(EEvent.RECEIVED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.RECEIVE;
		}
	}
	
	private class KickState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new KickSkill(target, EKickMode.MAX));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (GeoMath.distancePP(getPos(), getAiFrame().getWorldFrame().getBall().getPos()) > 200)
			{
				triggerEvent(EEvent.KICKED);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.KICK;
		}
	}
}
