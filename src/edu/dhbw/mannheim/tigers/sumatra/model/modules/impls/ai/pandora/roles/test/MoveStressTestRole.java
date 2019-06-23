/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 11, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.util.List;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveStressTestRole extends ARole
{
	
	/**
	 * 
	 */
	public MoveStressTestRole()
	{
		super(ERole.MOVE_STRESS_TEST);
		setInitialState(new SmallDestChanges());
	}
	
	private enum EState
	{
		SMALL_DEST_CHANGES
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	private class SmallDestChanges implements IRoleState
	{
		private MovementCon	moveCon			= null;
		private IVector2		initBotPos		= null;
		private float			initBotAngle	= 0;
		private float			b					= 0;
		private Random			rnd				= new Random();
		
		
		@Override
		public void doEntryActions()
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill();
			moveCon = skill.getMoveCon();
			moveCon.setPenaltyAreaAllowedOur(true);
			setNewSkill(skill);
			initBotPos = getPos();
			initBotAngle = getBot().getAngle();
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 baseDest = initBotPos.addNew(new Vector2(1000, 0));
			
			b += (1 / 50f) * AngleMath.PI_TWO;
			
			float a = (float) Math.cos(b);
			
			IVector2 dest = baseDest.addNew(new Vector2((a * 10), (a * 10)));
			moveCon.updateDestination(dest);
			
			float baseAngle = AngleMath.normalizeAngle(initBotAngle + AngleMath.PI);
			float angle = baseAngle + ((float) rnd.nextGaussian() * 0.15f);
			moveCon.updateTargetAngle(angle);
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
			return EState.SMALL_DEST_CHANGES;
		}
	}
}
