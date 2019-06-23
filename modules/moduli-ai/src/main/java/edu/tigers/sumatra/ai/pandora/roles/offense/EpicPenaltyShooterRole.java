/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import java.util.Random;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * EpicPenaltyShooterRole
 * 
 * @author David Scholz <David.Scholz@dlr.de>
 */
public class EpicPenaltyShooterRole extends ARole
{
	@Configurable(comment = "distance to penArea mark on preparation", defValue = "200.0")
	private static double preDistance = 400;
	@Configurable(comment = "use shooter with trajectory", defValue = "true")
	private static boolean useTrajectorySkill = true;
	@Configurable(comment = "disables the shooter skill completely and uses KickSkill", defValue = "false")
	private static boolean ignoreKeeperMovement = false;
	@Configurable(comment = "the time until the shoot is done in ms", defValue = "50")
	private static int timeToShoot = 50;
	@Configurable(defValue = "true")
	private static boolean epic = true;
	
	private final Random random = new Random(0);
	private IVector2 keeperInitialPos;
	private boolean keeperHasMoved = false;
	
	
	/**
	 * Default
	 */
	public EpicPenaltyShooterRole()
	{
		super(ERole.EPIC_PENALTY_SHOOTER);
		IState state1 = new PrepareState();
		IState state2 = new TestRightState();
		IState state3 = new WaitRightState();
		IState state4 = new TestLeftState();
		IState state5 = new WaitLeftState();
		IState state6 = new ShootState();
		IState state7 = new NoKeeperState();
		
		setInitialState(state1);
		addTransition(state1, EEvent.PREPARED, state2);
		addTransition(state2, EEvent.TESTEDRIGHT, state3);
		addTransition(state3, EEvent.WAITEDRIGHT, state4);
		addTransition(state4, EEvent.TESTEDLEFT, state5);
		addTransition(state5, EEvent.WAITEDLEFT, state6);
		addTransition(state6, EEvent.SHOTCENTER, state7);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EEvent implements IEvent
	{
		PREPARED,
		TESTEDRIGHT,
		WAITEDRIGHT,
		TESTEDLEFT,
		WAITEDLEFT,
		SHOT,
		SHOTCENTER
	}
	
	private class PrepareState implements IState
	{
		private IVector2 firstDestination;
		private AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			firstDestination = Geometry.getPenaltyMarkTheir().addNew(Vector2.fromXY(-preDistance, 0));
			
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateLookAtTarget(Geometry.getGoalTheir().getCenter());
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
		public void doUpdate()
		{
			if ((getAiFrame().getRefereeMsg() != null)
					&& (getAiFrame().getRefereeMsg().getCommand() == Command.NORMAL_START)
					&& (VectorMath.distancePP(firstDestination, getPos()) < 140))
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
		
		
	}
	
	private class TestRightState implements IState
	{
		IVector2 destination;
		
		
		@Override
		public void doEntryActions()
		{
			AMoveToSkill turn = AMoveToSkill.createMoveToSkill();
			IVector2 ballPos = Vector2.copy(getAiFrame().getWorldFrame().getBall().getPos());
			IVector2 leftGoalPost = Vector2.copy(Geometry.getGoalTheir().getLeftPost());
			destination = ballPos.addNew(ballPos.subtractNew(leftGoalPost).normalizeNew().multiplyNew(preDistance));
			turn.getMoveCon().updateLookAtTarget(ballPos);
			turn.getMoveCon().setPenaltyAreaAllowedTheir(true);
			turn.getMoveCon().setBallObstacle(false);
			turn.getMoveCon().updateDestination(destination);
			setNewSkill(turn);
		}
		
		
		@Override
		public void doUpdate()
		{
			// test whether the rotation is completed
			
			if (getPos().isCloseTo(destination, 50))
			{
				IVector2 keeperPos = getAiFrame().getWorldFrame().getFoeBot(getAiFrame().getKeeperFoeId()).getPos();
				if (!keeperPos.isCloseTo(keeperInitialPos, 20))
				{
					keeperHasMoved = true;
				}
				triggerEvent(EEvent.TESTEDRIGHT);
			}
		}
		
		
	}
	
	private class WaitRightState implements IState
	{
		
		private long time;
		
		
		@Override
		public void doEntryActions()
		{
			time = getWFrame().getTimestamp() + ((500 + random.nextInt(1000)) * 1_000_000L);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getWFrame().getTimestamp() > time)
			{
				triggerEvent(EEvent.WAITEDRIGHT);
			}
		}
		
		
	}
	
	private class TestLeftState implements IState
	{
		IVector2 destination;
		
		
		@Override
		public void doEntryActions()
		{
			AMoveToSkill turn = AMoveToSkill.createMoveToSkill();
			IVector2 ballPos = Vector2.copy(getAiFrame().getWorldFrame().getBall().getPos());
			IVector2 rightGoalPost = Vector2.copy(Geometry.getGoalTheir().getRightPost());
			destination = ballPos.addNew(ballPos.subtractNew(rightGoalPost).normalizeNew().multiplyNew(preDistance));
			turn.getMoveCon().updateLookAtTarget(ballPos);
			turn.getMoveCon().setPenaltyAreaAllowedTheir(true);
			turn.getMoveCon().setBallObstacle(false);
			turn.getMoveCon().updateDestination(destination);
			setNewSkill(turn);
		}
		
		
		@Override
		public void doUpdate()
		{
			// test whether the rotation is completed
			
			if (getPos().isCloseTo(destination, 50))
			{
				IVector2 keeperPos = getAiFrame().getWorldFrame().getFoeBot(getAiFrame().getKeeperFoeId()).getPos();
				if (!keeperPos.isCloseTo(keeperInitialPos, 50))
				{
					keeperHasMoved = true;
				}
				triggerEvent(EEvent.TESTEDLEFT);
			}
		}
		
		
	}
	
	private class WaitLeftState implements IState
	{
		
		private long time;
		
		
		@Override
		public void doEntryActions()
		{
			time = getWFrame().getTimestamp() + ((500 + random.nextInt(1000)) * 1_000_000L);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getWFrame().getTimestamp() > time)
			{
				triggerEvent(EEvent.WAITEDLEFT);
			}
		}
	}
	
	private class ShootState implements IState
	{
		private PenaltyShootSkill backupShooter;
		
		
		@Override
		public void doEntryActions()
		{
			if ((keeperHasMoved || ignoreKeeperMovement) && epic)
			{
				backupShooter = new PenaltyShootSkill(PenaltyShootSkill.ERotateDirection.CW);
				backupShooter.getMoveCon().setPenaltyAreaAllowedOur(true);
				backupShooter.getMoveCon().setBallObstacle(false);
				setNewSkill(backupShooter);
			} else
			{
				IVector2 directShotTarget = getAiFrame().getTacticalField().getBestDirectShotTarget();
				if (directShotTarget == null)
				{
					directShotTarget = Geometry.getGoalTheir().getCenter();
				}
				ASkill skill = new KickNormalSkill(new DynamicPosition(directShotTarget));
				skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
				skill.getMoveCon().setPenaltyAreaAllowedOur(true);
				setNewSkill(skill);
			}
			
		}
		
		
		@Override
		public void doUpdate()
		{
			if (backupShooter != null && (keeperHasMoved || ignoreKeeperMovement))
			{
				ITrackedBot keeperFoe = getAiFrame().getWorldFrame().getBot(getAiFrame().getKeeperFoeId());
				if (keeperFoe != null)
				{
					double keeperPosYCoordinate = keeperFoe.getPos().y();
					
					if (keeperPosYCoordinate < 0)
					{
						backupShooter.setShootDirection(PenaltyShootSkill.ERotateDirection.CW);
					} else if (keeperPosYCoordinate > 0)
					{
						backupShooter.setShootDirection(PenaltyShootSkill.ERotateDirection.CCW);
					}
					
				} else
				{
					triggerEvent(EEvent.SHOTCENTER);
				}
			}
		}
		
		
	}
	
	private class NoKeeperState implements IState
	{
		@Override
		public void doEntryActions()
		{
			KickChillSkill kick = new KickChillSkill(
					new DynamicPosition(Geometry.getGoalTheir().getCenter()));
			kick.getMoveCon().setPenaltyAreaAllowedOur(true);
			setNewSkill(kick);
		}
	}
}
