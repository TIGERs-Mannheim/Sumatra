/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 31, 2014
 * Authors: Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.EMoveToMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * PenaltyShooter
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class PenaltyShooterRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// private static final Logger log = Logger.getLogger(PenaltyShooterRole.class.getName());
	
	@Configurable(comment = "Distance from ball to prepositionate")
	private static float	distanceToBall	= 15f;
	
	@Configurable(comment = "Defines shooting-angle between bot and keeper")
	private static int	shootingAngle	= 20;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  */
	public PenaltyShooterRole()
	{
		super(ERole.PENALTY_SHOOTER);
		IRoleState state1 = new PrepareState();
		
		setInitialState(state1);
		addTransition(EStateId.PREPARE, EEvent.PREPARED, new TrickState());
		addTransition(EStateId.TRICK, EEvent.TRICKED, state1);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		TRICK
	}
	
	private enum EEvent
	{
		PREPARED,
		TRICKED
	}
	
	private class PrepareState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			MoveToSkill skill = new MoveAndStaySkill();
			IVector2 firstDestination = AIConfig.getGeometry().getPenaltyMarkTheir().addNew(new Vector2(-400, 0));
			skill.getMoveCon().updateLookAtTarget(getAiFrame().getWorldFrame().getBall().getPos());
			skill.getMoveCon().updateDestination(firstDestination);
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setArmKicker(false);
			skill.getMoveCon().setBallObstacle(true);
			setNewSkill(skill);
			
			IVector2 secDestination = AIConfig.getGeometry().getPenaltyMarkTheir().addNew(new Vector2(-200, 0));
			skill.getMoveCon().updateLookAtTarget(getAiFrame().getWorldFrame().getBall().getPos());
			skill.getMoveCon().updateDestination(secDestination);
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
			
			try
			{
				if (getAiFrame().getNewRefereeMsg().getCommand().equals(Command.NORMAL_START))
				{
					// System.out.println("prepared!");
					nextState(EEvent.PREPARED);
				}
			} catch (NullPointerException e)
			{
				
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
			return EStateId.PREPARE;
		}
	}
	
	private class TrickState implements IRoleState
	{
		
		private class BoolRef
		{
			private boolean	shoot	= false;
		}
		
		private BoolRef	shoot	= new BoolRef();
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
			
			
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			// IVector2 ballPos = getAiFrame().getWorldFrame().ball.getPos();
			// MoveToSkill moveSkill = new MoveToSkill();
			
			if (shoot.shoot)
			{
				
				
				KickSkill kick = new KickSkill(
						new DynamicPosition(AIConfig.getGeometry().getGoalTheir().getGoalPostLeft()
								.addNew(new Vector2(0, -55))), EKickMode.MAX);
				
				setNewSkill(kick);
				
			} else
			{
				
				KickSkill kick = new KickSkill(new DynamicPosition(AIConfig.getGeometry().getGoalTheir()
						.getGoalPostRight()
						.addNew(new Vector2(0, -55))), EKickMode.MAX);
				setNewSkill(kick);
				/*
				 * IVector2 target = ballPos.addNew(ballPos.subtractNew(getPos()).normalizeNew().multiplyNew(100));
				 * moveSkill.getMoveCon().setBallObstacle(false);
				 * moveSkill.getMoveCon().setArmKicker(true);
				 * moveSkill.getMoveCon().updateDestination(target);
				 * setNewSkill(moveSkill);
				 */
				
			}
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			IVector2 ballPos = getAiFrame().getWorldFrame().ball.getPos();
			IVector2 target = AIConfig.getGeometry().getGoalTheir().getGoalPostRight().addNew(new Vector2(0, 30));
			IVector2 targetToBall = ballPos.subtractNew(target).normalizeNew();
			IVector2 moveTarget = ballPos.addNew(targetToBall.multiplyNew(distanceToBall));
			
			IMoveToSkill moveSkill = AMoveSkill.createMoveToSkill(EMoveToMode.DO_COMPLETE);
			moveSkill.getMoveCon().updateDestination(moveTarget);
			moveSkill.getMoveCon().updateLookAtTarget(ballPos);
			setNewSkill(moveSkill);
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
				IVector2 posToEnemy = keeperPos.subtractNew(ballPos);
				
				float pos = 0f;
				pos = GeoMath.angleBetweenVectorAndVector(posToBall, posToEnemy);
				shoot.shoot = false;
				
				if (Math.abs(AngleMath.rad2deg(pos)) < shootingAngle)
				{
					shoot.shoot = true;
				}
			} else
			{
				shoot.shoot = true;
			}
			
			float distance = GeoMath.distancePP(getPos(), getWFrame().getBall().getPos());
			
			if (distance > 600)
			{
				setCompleted();
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.TRICK;
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
