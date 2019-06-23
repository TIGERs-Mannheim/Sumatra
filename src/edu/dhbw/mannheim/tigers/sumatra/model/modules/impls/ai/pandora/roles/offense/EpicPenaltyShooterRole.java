/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.05.2014
 * Author(s): David Scholz <David.Scholz@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootSkill.ERotateDirection;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * EpicPenaltyShooterRole
 * 
 * @author David Scholz <David.Scholz@dlr.de>
 */
public class EpicPenaltyShooterRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// private static final Logger log = Logger.getLogger(PenaltyShooterRole.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public EpicPenaltyShooterRole()
	{
		super(ERole.EPIC_PENALTY_SHOOTER);
		IRoleState state1 = new PrepareState();
		IRoleState state2 = new ShootState();
		IRoleState state3 = new NoKeeperState();
		
		setInitialState(state1);
		addTransition(EStateId.PREPARE, EEvent.PREPARED, state2);
		addTransition(EStateId.SHOOT, EEvent.SHOTCENTER, state3);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		SHOOT,
		SHOOTCENTER
	}
	
	private enum EEvent
	{
		PREPARED,
		SHOT,
		SHOTCENTER
	}
	
	private class PrepareState implements IRoleState
	{
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
			
			
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			nextState(EEvent.PREPARED);
		}
		
		
		/*
		 * Startposition of EpicPenaltyShooter
		 */
		
		@Override
		public void doEntryActions()
		{
			
			MoveToSkill startPos = new MoveAndStaySkill();
			IVector2 firstDestination = AIConfig.getGeometry().getPenaltyMarkTheir().addNew(new Vector2(-400, 0));
			startPos.getMoveCon().updateLookAtTarget(getAiFrame().getWorldFrame().getBall().getPos());
			startPos.getMoveCon().updateDestination(firstDestination);
			startPos.getMoveCon().setBallObstacle(false);
			startPos.getMoveCon().setArmKicker(false);
			startPos.getMoveCon().setBallObstacle(true);
			setNewSkill(startPos);
			
		}
		
		
		@Override
		public void doExitActions()
		{
			
			
		}
		
		
		@Override
		public void doUpdate()
		{
			TrackedTigerBot keeperFoe = getAiFrame().getWorldFrame().getBot(getAiFrame().getKeeperFoeId());
			if (keeperFoe != null)
			{
				IVector2 keeperPos = keeperFoe.getPos();
				IVector2 ballPos = getAiFrame().getWorldFrame().ball.getPos();
				IVector2 posToBall = ballPos.subtractNew(getPos());
				
				getAiFrame().addDebugShape(new DrawableLine(new Line(ballPos, keeperPos.subtractNew(ballPos))));
				getAiFrame().addDebugShape(new DrawableLine(new Line(ballPos, posToBall)));
			}
			
			IVector2 firstDestination = AIConfig.getGeometry().getPenaltyMarkTheir().addNew(new Vector2(-400, 0));
			
			
			if (GeoMath.distancePP(firstDestination, getPos()) < 40)
			{
				nextState(EEvent.PREPARED);
			}
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			
			return EStateId.PREPARE;
		}
		
	}
	
	private class ShootState implements IRoleState
	{
		private PenaltyShootSkill	shooter	= new PenaltyShootSkill(ERotateDirection.LEFT);
		
		
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
			setNewSkill(shooter);
		}
		
		
		@Override
		public void doExitActions()
		{
			
			
		}
		
		
		@Override
		public void doUpdate()
		{
			TrackedTigerBot keeperFoe = getAiFrame().getWorldFrame().getBot(getAiFrame().getKeeperFoeId());
			if (keeperFoe != null)
			{
				float keeperPosYCoordinate = keeperFoe.getPos().y();
				
				if (keeperPosYCoordinate < 0)
				{
					shooter.setShootDirection(ERotateDirection.RIGHT);
				} else if (keeperPosYCoordinate > 0)
				{
					shooter.setShootDirection(ERotateDirection.LEFT);
				}
				else
				{
					shooter.setShootDirection(ERotateDirection.RIGHT);
				}
				
				if (getAiFrame().getNewRefereeMsg() != null)
				{
					if (getAiFrame().getNewRefereeMsg().getCommand().equals(Command.NORMAL_START))
					{
						shooter.normalStartCalled();
					}
				}
				
				if (GeoMath.distancePP(getPos(), getWFrame().getBall().getPos()) > 600)
				{
					setCompleted();
				}
			} else
			{
				if (getAiFrame().getNewRefereeMsg() != null)
				{
					if (getAiFrame().getNewRefereeMsg().getCommand().equals(Command.NORMAL_START))
					{
						nextState(EEvent.SHOTCENTER);
					}
				}
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			
			return EStateId.SHOOT;
		}
		
	}
	
	private class NoKeeperState implements IRoleState
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
			KickSkill kick = new KickSkill(new DynamicPosition(AIConfig.getGeometry().getGoalTheir().getGoalCenter()),
					EKickMode.MAX);
			setNewSkill(kick);
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
			
			return EStateId.SHOOTCENTER;
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		
		
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
