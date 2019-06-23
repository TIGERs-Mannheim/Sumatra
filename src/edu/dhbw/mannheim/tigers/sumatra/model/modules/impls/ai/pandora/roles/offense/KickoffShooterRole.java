/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 31, 2014
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.util.List;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EMoveMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Simple shooter role, basically to test skill
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class KickoffShooterRole extends ARole
{
	
	private boolean		normalStartCalled	= false;
	private EMoveState	state					= EMoveState.LEFT;
	private int				numberOfTurns		= 0;
	
	
	/**
	  */
	public KickoffShooterRole()
	{
		super(ERole.KICKOFF_SHOOTER);
		Random rn = new Random();
		int i = (rn.nextInt() % 5);
		numberOfTurns = Math.abs(i) + 2;
		
		IRoleState state1 = new PrepareState();
		setInitialState(state1);
		addTransition(EStateId.PREPARE, EEvent.READY, new MoveState());
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		MOVE;
	}
	
	private enum EEvent
	{
		READY;
	}
	
	private enum EMoveState
	{
		LEFT,
		RIGHT,
		MIDDLE;
	}
	
	private class PrepareState implements IRoleState
	{
		
		IMoveToSkill	skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(false);
			skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 dest = getWFrame().getBall().getPos().subtractNew(new Vector2(300, 0));
			if ((getAiFrame().getLatestRefereeMsg() != null)
					&& (getAiFrame().getLatestRefereeMsg().getCommand() == Command.NORMAL_START))
			{
				normalStartCalled = true;
			}
			skill.getMoveCon().updateDestination(dest);
			
			if (normalStartCalled && (GeoMath.distancePP(getPos(), dest) < 100) && (getBot().getVel().getLength2() < 0.2f))
			{
				triggerEvent(EEvent.READY);
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
			setCompleted();
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PREPARE;
		}
	}
	
	private class MoveState implements IRoleState
	{
		
		private IMoveToSkill	skill				= null;
		private int				counter			= 0;
		private boolean		kickSkillSet	= false;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(true);
			skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 dest = null;
			
			switch (state)
			{
				case LEFT:
					dest = getWFrame().getBall().getPos().subtractNew(new Vector2(300, -150));
					break;
				case MIDDLE:
					dest = getWFrame().getBall().getPos().subtractNew(new Vector2(300, 0));
					break;
				case RIGHT:
					dest = getWFrame().getBall().getPos().subtractNew(new Vector2(300, 150));
					break;
			}
			if (GeoMath.distancePP(getPos(), dest) < 50)
			{
				if (state == EMoveState.RIGHT)
				{
					state = EMoveState.LEFT;
				} else
				{
					state = EMoveState.RIGHT;
				}
				counter++;
			}
			if ((counter > numberOfTurns) && !kickSkillSet)
			{
				kickSkillSet = true;
				IVector2 target = new Vector2(3000, 500);
				KickSkill kickSkill = new KickSkill(new DynamicPosition(target), EKickMode.FIXED_DURATION, EMoveMode.CHILL);
				kickSkill.setDuration(1000);
				kickSkill.setDevice(EKickerDevice.CHIP);
				setNewSkill(kickSkill);
			}
			skill.getMoveCon().updateDestination(dest);
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
			setCompleted();
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.MOVE);
		features.add(EFeature.BARRIER);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
