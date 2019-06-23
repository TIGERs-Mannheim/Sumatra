/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.10.2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Simple KickRole implementation
 * 
 * @author MarkG
 */
public class KickTestRole extends ARole
{
	private enum EState
	{
		SHOT;
	}
	
	
	/**
	 * 
	 */
	public KickTestRole()
	{
		super(ERole.SIMPLE_KICK);
		setInitialState(new ShootState());
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.BARRIER);
		features.add(EFeature.STRAIGHT_KICKER);
	}
	
	private class ShootState implements IRoleState
	{
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new KickSkill(new DynamicPosition(AVector2.ZERO_VECTOR), EKickMode.MAX));
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EState.SHOT;
		}
		
	}
	
}
