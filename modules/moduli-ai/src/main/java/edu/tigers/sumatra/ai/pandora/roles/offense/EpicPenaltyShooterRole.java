/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.05.2014
 * Author(s): David Scholz <David.Scholz@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense;

import java.util.Random;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkill.ERotateDirection;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkillNew;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkillNew.ERotateTrajDirection;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * EpicPenaltyShooterRole
 * 
 * @author David Scholz <David.Scholz@dlr.de>
 */
public class EpicPenaltyShooterRole extends ARole
{
	@Configurable(comment = "distance to penArea mark on preparation")
	private static double	preDistance				= 400;
	
	
	@Configurable(comment = "use shooter with trajectory")
	private static boolean	useTrajectorySkill	= true;
	@Configurable(comment = "disables the shooter skill completely and uses KickSkill")
	private static boolean	ignoreKeeperMovement	= false;
	@Configurable(comment = "the time until the shoot is done in ms")
	private static int		timeToShoot				= 50;
	
	@Configurable
	private static boolean	epic						= false;
	
	private IVector2			keeperInitialPos;
	private boolean			keeperHasMoved			= false;
	
	private final Random		random					= new Random();
	
	
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
		private AMoveToSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			firstDestination = Geometry.getPenaltyMarkTheir().addNew(new Vector2(-preDistance, 0));
			
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateLookAtTarget(Geometry.getGoalTheir().getGoalCenter());
			skill.getMoveCon().setBallObstacle(true);
			skill.getMoveCon().updateDestination(firstDestination);
			setNewSkill(skill);
			ITrackedBot bot = getAiFrame().getWorldFrame().getFoeBot(getAiFrame().getKeeperFoeId());
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
			if ((getAiFrame().getRefereeMsg() != null)
					&& (getAiFrame().getRefereeMsg().getCommand() == Command.NORMAL_START)
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
		IVector2 destination;
		
		
		@Override
		public void doEntryActions()
		{
			AMoveToSkill turn = AMoveToSkill.createMoveToSkill();
			IVector2 ballPos = new Vector2(getAiFrame().getWorldFrame().getBall().getPos());
			IVector2 leftGoalPost = new Vector2((Geometry.getGoalTheir().getGoalPostLeft()));
			destination = ballPos.addNew(ballPos.subtractNew(leftGoalPost).normalizeNew().multiplyNew(preDistance));
			turn.getMoveCon().updateLookAtTarget(ballPos);
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
		
		private long time;
		
		
		@Override
		public void doEntryActions()
		{
			time = getWFrame().getTimestamp() + ((500 + random.nextInt(1000)) * 1_000_000L);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getWFrame().getTimestamp() > time)
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
		IVector2 destination;
		
		
		@Override
		public void doEntryActions()
		{
			AMoveToSkill turn = AMoveToSkill.createMoveToSkill();
			IVector2 ballPos = new Vector2(getAiFrame().getWorldFrame().getBall().getPos());
			IVector2 rightGoalPost = new Vector2((Geometry.getGoalTheir().getGoalPostRight()));
			destination = ballPos.addNew(ballPos.subtractNew(rightGoalPost).normalizeNew().multiplyNew(preDistance));
			turn.getMoveCon().updateLookAtTarget(ballPos);
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
				if (!keeperPos.equals(keeperInitialPos, 50))
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
		
		private long time;
		
		
		@Override
		public void doEntryActions()
		{
			time = getWFrame().getTimestamp() + ((500 + random.nextInt(1000)) * 1_000_000L);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getWFrame().getTimestamp() > time)
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
		private PenaltyShootSkillNew	trajectoryShooter;
		private PenaltyShootSkill		backupShooter;
		
		
		@Override
		public void doEntryActions()
		{
			if ((keeperHasMoved || ignoreKeeperMovement) && epic)
			{
				if (useTrajectorySkill)
				{
					trajectoryShooter = new PenaltyShootSkillNew(ERotateTrajDirection.LEFT);
					trajectoryShooter.getMoveCon().setPenaltyAreaAllowedOur(true);
					trajectoryShooter.getMoveCon().setBallObstacle(false);
					setNewSkill(trajectoryShooter);
				} else
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
					directShotTarget = Geometry.getGoalTheir().getGoalCenter();
				}
				ASkill skill = new KickSkill(new DynamicPosition(directShotTarget));
				skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
				skill.getMoveCon().setPenaltyAreaAllowedOur(true);
				setNewSkill(skill);
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
				ITrackedBot keeperFoe = getAiFrame().getWorldFrame().getBot(getAiFrame().getKeeperFoeId());
				if (keeperFoe != null)
				{
					double keeperPosYCoordinate = keeperFoe.getPos().y();
					
					if (keeperPosYCoordinate < 0)
					{
						if (useTrajectorySkill)
						{
							trajectoryShooter.setShootDirection(ERotateTrajDirection.RIGHT);
						} else
						{
							backupShooter.setShootDirection(ERotateDirection.RIGHT);
						}
					} else if (keeperPosYCoordinate > 0)
					{
						if (useTrajectorySkill)
						{
							trajectoryShooter.setShootDirection(ERotateTrajDirection.LEFT);
						} else
						{
							backupShooter.setShootDirection(ERotateDirection.LEFT);
						}
					} else
					{
						if (useTrajectorySkill)
						{
							trajectoryShooter.setShootDirection(ERotateTrajDirection.RIGHT);
						} else
						{
							backupShooter.setShootDirection(ERotateDirection.RIGHT);
						}
						
					}
					if (useTrajectorySkill)
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
		public void doEntryActions()
		{
			KickSkill kick = new KickSkill(
					new DynamicPosition(Geometry.getGoalTheir().getGoalCenter()));
			kick.setMoveMode(EMoveMode.CHILL);
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
}
