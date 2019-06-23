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
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillPenaltyShoot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillPenaltyShoot.EPenaltyShootFlags;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BotSkillWrapperSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootSkill.ERotateDirection;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootTrajSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootTrajSkill.ERotateTrajDirection;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * EpicPenaltyShooterRole
 * 
 * @author David Scholz <David.Scholz@dlr.de>
 */
public class EpicPenaltyShooterRole extends ARole
{
	@Configurable(comment = "distance to penArea mark on preparation")
	private static float						preDistance				= 400;
	
	@Configurable(comment = "use the botskill")
	private static boolean					useBotskill				= false;
	
	@Configurable(comment = "use shooter with trajectory")
	private static boolean					useTrajectorySkill	= true;
	@Configurable(comment = "disables the shooter skill completely and uses KickSkill")
	private static boolean					ignoreKeeperMovement	= false;
	@Configurable(comment = "the time until the shoot is done in ms")
	private static int						timeToShoot				= 50;
	@Configurable(comment = "choose the flag that should be used")
	private static EPenaltyShootFlags	movementSpeed			= EPenaltyShootFlags.FORWARD_MOVEMENT_LEVEL_1;
	
	private IVector2							keeperInitialPos;
	private boolean							keeperHasMoved			= false;
	
	private Random								random					= new Random();
	
	
	/**
	  * 
	  */
	public EpicPenaltyShooterRole()
	{
		super(ERole.EPIC_PENALTY_SHOOTER);
		IRoleState state1 = new PrepareState();
		IRoleState state2 = new TestRightState();
		IRoleState state3 = new WaitRightState();
		IRoleState state4 = new TestLeftState();
		IRoleState state5 = new WaitLeftState();
		IRoleState state6 = new ShootState();
		IRoleState state7 = new NoKeeperState();
		
		setInitialState(state1);
		addTransition(EStateId.PREPARE, EEvent.PREPARED, state2);
		addTransition(EStateId.TESTRIGHT, EEvent.TESTEDRIGHT, state3);
		addTransition(EStateId.WAITRIGHT, EEvent.WAITEDRIGHT, state4);
		addTransition(EStateId.TESTLEFT, EEvent.TESTEDLEFT, state5);
		addTransition(EStateId.WAITLEFT, EEvent.WAITEDLEFT, state6);
		addTransition(EStateId.PREPARE, EEvent.SHOTCENTER, state7);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		TESTRIGHT,
		WAITRIGHT,
		TESTLEFT,
		WAITLEFT,
		SHOOT,
		SHOOTCENTER
	}
	
	private enum EEvent
	{
		PREPARED,
		TESTEDRIGHT,
		WAITEDRIGHT,
		TESTEDLEFT,
		WAITEDLEFT,
		SHOT,
		SHOTCENTER
	}
	
	private class PrepareState implements IRoleState
	{
		private IVector2		firstDestination;
		private IMoveToSkill	skill;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			triggerEvent(EEvent.PREPARED);
		}
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill();
			firstDestination = AIConfig.getGeometry().getPenaltyMarkTheir().addNew(new Vector2(-preDistance, 0));
			
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateLookAtTarget(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
			skill.getMoveCon().setBallObstacle(true);
			skill.getMoveCon().setArmKicker(false);
			skill.getMoveCon().updateDestination(firstDestination);
			setNewSkill(skill);
			TrackedBot bot = getAiFrame().getWorldFrame().getFoeBot(getAiFrame().getKeeperFoeId());
			if (bot != null)
			{
				keeperInitialPos = bot.getPos();
			}
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
				IVector2 ballPos = getAiFrame().getWorldFrame().getBall().getPos();
				IVector2 posToBall = ballPos.subtractNew(getPos());
				
				getAiFrame().addDebugShape(new DrawableLine(new Line(ballPos, keeperPos.subtractNew(ballPos))));
				getAiFrame().addDebugShape(new DrawableLine(new Line(ballPos, posToBall)));
			}
			
			if ((getAiFrame().getLatestRefereeMsg() != null)
					&& (getAiFrame().getLatestRefereeMsg().getCommand() == Command.NORMAL_START)
					&& (GeoMath.distancePP(firstDestination, getPos()) < 140))
			{
				if (keeperInitialPos != null)
				{
					triggerEvent(EEvent.PREPARED);
				} else
				{
					triggerEvent(EEvent.SHOTCENTER);
				}
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			
			return EStateId.PREPARE;
		}
		
	}
	
	private class TestRightState implements IRoleState
	{
		IVector2	destination;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			triggerEvent(EEvent.TESTEDRIGHT);
		}
		
		
		@Override
		public void doEntryActions()
		{
			IMoveToSkill turn = AMoveSkill.createMoveToSkill();
			IVector2 ballPos = new Vector2(getAiFrame().getWorldFrame().getBall().getPos());
			IVector2 leftGoalPost = new Vector2((AIConfig.getGeometry().getGoalTheir().getGoalPostLeft()));
			destination = ballPos.addNew(ballPos.subtractNew(leftGoalPost).normalizeNew().multiplyNew(preDistance));
			turn.getMoveCon().updateLookAtTarget(ballPos);
			turn.getMoveCon().setArmKicker(false);
			turn.getMoveCon().setPenaltyAreaAllowedTheir(true);
			turn.getMoveCon().setBallObstacle(false);
			turn.getMoveCon().updateDestination(destination);
			setNewSkill(turn);
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			// test whether the rotation is completed
			
			if (getPos().equals(destination, 50))
			{
				IVector2 keeperPos = getAiFrame().getWorldFrame().getFoeBot(getAiFrame().getKeeperFoeId()).getPos();
				if (!keeperPos.equals(keeperInitialPos, 20))
				{
					keeperHasMoved = true;
				}
				triggerEvent(EEvent.TESTEDRIGHT);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.TESTRIGHT;
		}
		
	}
	
	private class WaitRightState implements IRoleState
	{
		
		private long	time;
		
		
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
			time = System.currentTimeMillis() + 500 + random.nextInt(1000);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (System.currentTimeMillis() > time)
			{
				triggerEvent(EEvent.WAITEDRIGHT);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.WAITRIGHT;
		}
		
	}
	
	private class TestLeftState implements IRoleState
	{
		IVector2	destination;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			triggerEvent(EEvent.TESTEDLEFT);
		}
		
		
		@Override
		public void doEntryActions()
		{
			IMoveToSkill turn = AMoveSkill.createMoveToSkill();
			IVector2 ballPos = new Vector2(getAiFrame().getWorldFrame().getBall().getPos());
			IVector2 rightGoalPost = new Vector2((AIConfig.getGeometry().getGoalTheir().getGoalPostRight()));
			destination = ballPos.addNew(ballPos.subtractNew(rightGoalPost).normalizeNew().multiplyNew(preDistance));
			turn.getMoveCon().updateLookAtTarget(ballPos);
			turn.getMoveCon().setArmKicker(false);
			turn.getMoveCon().setPenaltyAreaAllowedTheir(true);
			turn.getMoveCon().setBallObstacle(false);
			turn.getMoveCon().updateDestination(destination);
			setNewSkill(turn);
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			// test whether the rotation is completed
			
			if (getPos().equals(destination, 10))
			{
				IVector2 keeperPos = getAiFrame().getWorldFrame().getFoeBot(getAiFrame().getKeeperFoeId()).getPos();
				if (!keeperPos.equals(keeperInitialPos, 20))
				{
					keeperHasMoved = true;
				}
				triggerEvent(EEvent.TESTEDLEFT);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.TESTLEFT;
		}
		
	}
	
	private class WaitLeftState implements IRoleState
	{
		
		private long	time;
		
		
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
			time = System.currentTimeMillis() + 500 + random.nextInt(1000);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (System.currentTimeMillis() > time)
			{
				triggerEvent(EEvent.WAITEDLEFT);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.WAITLEFT;
		}
		
	}
	
	private class ShootState implements IRoleState
	{
		private BotSkillWrapperSkill	shooter2;
		private BotSkillPenaltyShoot	botSkill	= null;
		private PenaltyShootTrajSkill	trajectoryShooter;
		private PenaltyShootSkill		backupShooter;
		
		
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
			if (keeperHasMoved || ignoreKeeperMovement)
			{
				if (useBotskill)
				{
					botSkill = new BotSkillPenaltyShoot(getPos(), getWFrame().getBall().getPos(),
							timeToShoot, movementSpeed,
							EPenaltyShootFlags.TURNING_LEFT, EPenaltyShootFlags.NO_OP);
					shooter2 = new BotSkillWrapperSkill(botSkill);
					setNewSkill(shooter2);
				} else if (useTrajectorySkill)
				{
					trajectoryShooter = new PenaltyShootTrajSkill(ERotateTrajDirection.LEFT);
					trajectoryShooter.getMoveCon().setPenaltyAreaAllowedOur(true);
					trajectoryShooter.getMoveCon().setBallObstacle(false);
					setNewSkill(trajectoryShooter);
				}
				else
				{
					backupShooter = new PenaltyShootSkill(ERotateDirection.LEFT);
					backupShooter.getMoveCon().setPenaltyAreaAllowedOur(true);
					backupShooter.getMoveCon().setBallObstacle(false);
					setNewSkill(backupShooter);
				}
				
			} else
			{
				IVector2 directShotTarget = getAiFrame().getTacticalField().getBestDirectShootTarget();
				if (directShotTarget == null)
				{
					directShotTarget = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
				}
				setNewSkill(new KickSkill(new DynamicPosition(directShotTarget),
						EKickMode.MAX));
			}
			
		}
		
		
		@Override
		public void doExitActions()
		{
			
			
		}
		
		
		@Override
		public void doUpdate()
		{
			if (keeperHasMoved || ignoreKeeperMovement)
			{
				TrackedTigerBot keeperFoe = getAiFrame().getWorldFrame().getBot(getAiFrame().getKeeperFoeId());
				if (keeperFoe != null)
				{
					float keeperPosYCoordinate = keeperFoe.getPos().y();
					
					if (keeperPosYCoordinate < 0)
					{
						if (useBotskill)
						{
							botSkill.removeFlag(EPenaltyShootFlags.TURNING_LEFT);
							botSkill.addFlag(EPenaltyShootFlags.TURNING_RIGHT);
						} else if (useTrajectorySkill)
						{
							trajectoryShooter.setShootDirection(ERotateTrajDirection.RIGHT);
						} else
						{
							backupShooter.setShootDirection(ERotateDirection.RIGHT);
						}
					} else if (keeperPosYCoordinate > 0)
					{
						if (useBotskill)
						{
							botSkill.removeFlag(EPenaltyShootFlags.TURNING_RIGHT);
							botSkill.addFlag(EPenaltyShootFlags.TURNING_LEFT);
						} else if (useTrajectorySkill)
						{
							trajectoryShooter.setShootDirection(ERotateTrajDirection.LEFT);
						} else
						{
							backupShooter.setShootDirection(ERotateDirection.LEFT);
						}
					}
					else
					{
						if (useBotskill)
						{
							botSkill.removeFlag(EPenaltyShootFlags.TURNING_LEFT);
							botSkill.addFlag(EPenaltyShootFlags.TURNING_RIGHT);
						} else if (useTrajectorySkill)
						{
							trajectoryShooter.setShootDirection(ERotateTrajDirection.RIGHT);
						}
						else
						{
							backupShooter.setShootDirection(ERotateDirection.RIGHT);
						}
						
					}
					if (useBotskill)
					{
						botSkill.updateBallPos(getWFrame().getBall().getPos());
					} else if (useTrajectorySkill)
					{
						trajectoryShooter.normalStartCalled();
					} else
					{
						backupShooter.normalStartCalled();
					}
					
				} else
				{
					triggerEvent(EEvent.SHOTCENTER);
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
			KickSkill kick = new KickSkill(
					new DynamicPosition(AIConfig.getGeometry().getGoalTheir().getGoalCenter()),
					EKickMode.MAX);
			kick.getMoveCon().setPenaltyAreaAllowedOur(true);
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
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.MOVE);
		features.add(EFeature.BARRIER);
	}
}
