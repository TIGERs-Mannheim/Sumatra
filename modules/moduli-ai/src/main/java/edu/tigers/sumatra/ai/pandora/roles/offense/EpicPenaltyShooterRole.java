/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Optional;
import java.util.Random;


/**
 * EpicPenaltyShooterRole
 *
 * @author David Scholz <David.Scholz@dlr.de>
 */
public class EpicPenaltyShooterRole extends ARole
{
	@Configurable(comment = "distance to penArea mark on preparation", defValue = "700.0")
	private static double prepareDistance = 700;

	@Configurable(comment = "distance to penArea mark on preparation", defValue = "200.0")
	private static double testDistance = 200;

	@Configurable(comment = "If true: disable the analytic of the keepers movement", defValue = "false")
	private static boolean ignoreKeeperMovement = false;

	@Configurable(defValue = "true")
	private static boolean epic = true;

	private Random rnd = null;
	private IVector2 keeperInitialPos;
	private boolean keeperHasMoved = false;


	/**
	 * Default
	 */
	public EpicPenaltyShooterRole()
	{
		super(ERole.EPIC_PENALTY_SHOOTER);
		IState prepareState = new PrepareState();
		IState testRightState = new TestRightState();
		IState waitRightState = new WaitRightState();
		IState testLeftState = new TestLeftState();
		IState waitLeftState = new WaitLeftState();
		IState shootState = new ShootState();
		IState noKeeperState = new NoKeeperState();

		setInitialState(prepareState);
		addTransition(prepareState, EEvent.PREPARED, testRightState);
		addTransition(prepareState, EEvent.SHOTCENTER, noKeeperState);
		addTransition(testRightState, EEvent.TESTEDRIGHT, waitRightState);
		addTransition(waitRightState, EEvent.WAITEDRIGHT, testLeftState);
		addTransition(testLeftState, EEvent.TESTEDLEFT, waitLeftState);
		addTransition(waitLeftState, EEvent.WAITEDLEFT, shootState);
		addTransition(shootState, EEvent.SHOTCENTER, noKeeperState);
	}


	@Override
	protected void beforeFirstUpdate()
	{
		super.beforeFirstUpdate();
		rnd = new Random(getWFrame().getTimestamp());
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

	private class PrepareState extends AState
	{
		private IVector2 firstDestination;


		@Override
		public void doEntryActions()
		{
			var skill = MoveToSkill.createMoveToSkill();
			firstDestination = Geometry.getPenaltyMarkTheir().addNew(Vector2.fromXY(-prepareDistance, 0));

			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.updateLookAtTarget(Geometry.getGoalTheir().getCenter());
			skill.getMoveCon().setBallObstacle(true);
			skill.updateDestination(firstDestination);
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			if ((getAiFrame().getRefereeMsg() != null)
					&& (getAiFrame().getRefereeMsg().getCommand() == Command.NORMAL_START)
					&& (VectorMath.distancePP(firstDestination, getPos()) < 140))
			{
				if (isKeeperInGoal())
				{
					triggerEvent(EEvent.PREPARED);
				} else
				{
					triggerEvent(EEvent.SHOTCENTER);
				}
			}
		}


		private boolean isKeeperInGoal()
		{
			ITrackedBot bot = getAiFrame().getWorldFrame().getOpponentBot(getAiFrame().getKeeperOpponentId());
			if (bot != null)
			{
				keeperInitialPos = bot.getPos();
			} else
			{
				return false;
			}

			if (keeperInitialPos == null)
			{
				return false;
			}

			boolean inPenArea = Geometry.getPenaltyAreaTheir().isPointInShape(keeperInitialPos);
			boolean inGoal = Geometry.getGoalTheir().isPointInShape(keeperInitialPos);

			return inGoal || inPenArea;

		}


	}

	private class TestRightState extends AState
	{
		IVector2 destination;


		@Override
		public void doEntryActions()
		{
			MoveToSkill turn = MoveToSkill.createMoveToSkill();
			IVector2 ballPos = Vector2.copy(getAiFrame().getWorldFrame().getBall().getPos());
			IVector2 leftGoalPost = Vector2.copy(Geometry.getGoalTheir().getLeftPost());
			destination = ballPos.addNew(ballPos.subtractNew(leftGoalPost).normalizeNew().multiplyNew(testDistance));
			turn.updateLookAtTarget(ballPos);
			turn.getMoveCon().setPenaltyAreaTheirObstacle(false);
			turn.getMoveCon().setBallObstacle(false);
			turn.updateDestination(destination);
			setNewSkill(turn);
		}


		@Override
		public void doUpdate()
		{
			// test whether the rotation is completed

			if (getPos().isCloseTo(destination, 50))
			{
				IVector2 keeperPos = getAiFrame().getWorldFrame().getOpponentBot(getAiFrame().getKeeperOpponentId())
						.getPos();
				if (!keeperPos.isCloseTo(keeperInitialPos, 20))
				{
					keeperHasMoved = true;
				}
				triggerEvent(EEvent.TESTEDRIGHT);
			}
		}


	}

	private class WaitRightState extends AState
	{

		private long time;


		@Override
		public void doEntryActions()
		{
			time = getWFrame().getTimestamp() + ((500 + rnd.nextInt(1000)) * 1_000_000L);
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

	private class TestLeftState extends AState
	{
		IVector2 destination;


		@Override
		public void doEntryActions()
		{
			MoveToSkill turn = MoveToSkill.createMoveToSkill();
			IVector2 ballPos = Vector2.copy(getAiFrame().getWorldFrame().getBall().getPos());
			IVector2 rightGoalPost = Vector2.copy(Geometry.getGoalTheir().getRightPost());
			destination = ballPos.addNew(ballPos.subtractNew(rightGoalPost).normalizeNew().multiplyNew(testDistance));
			turn.updateLookAtTarget(ballPos);
			turn.getMoveCon().setPenaltyAreaTheirObstacle(false);
			turn.getMoveCon().setBallObstacle(false);
			turn.updateDestination(destination);
			setNewSkill(turn);
		}


		@Override
		public void doUpdate()
		{
			// test whether the rotation is completed

			if (getPos().isCloseTo(destination, 50))
			{
				IVector2 keeperPos = getAiFrame().getWorldFrame().getOpponentBot(getAiFrame().getKeeperOpponentId())
						.getPos();
				if (!keeperPos.isCloseTo(keeperInitialPos, 50))
				{
					keeperHasMoved = true;
				}
				triggerEvent(EEvent.TESTEDLEFT);
			}
		}


	}

	private class WaitLeftState extends AState
	{

		private long time;


		@Override
		public void doEntryActions()
		{
			time = getWFrame().getTimestamp() + ((500 + rnd.nextInt(1000)) * 1_000_000L);
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

	private class ShootState extends AState
	{
		private PenaltyShootSkill backupShooter;


		@Override
		public void doEntryActions()
		{
			if ((keeperHasMoved || ignoreKeeperMovement) && epic)
			{
				backupShooter = new PenaltyShootSkill();
				setNewSkill(backupShooter);
			} else
			{
				var directShotTarget = Optional
						.ofNullable(getAiFrame().getTacticalField().getBestGoalKick())
						.map(GoalKick::getKick)
						.map(Kick::getTarget)
						.orElse(Geometry.getGoalTheir().getCenter());
				var skill = new TouchKickSkill(directShotTarget, KickParams.maxStraight());
				skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
				skill.getMoveCon().setPenaltyAreaOurObstacle(false);
				setNewSkill(skill);
			}
		}


		@Override
		public void doUpdate()
		{
			if ((backupShooter != null) && (keeperHasMoved || ignoreKeeperMovement))
			{
				ITrackedBot keeperOpponent = getAiFrame().getWorldFrame().getBot(getAiFrame().getKeeperOpponentId());
				if (keeperOpponent != null)
				{
					double keeperPosYCoordinate = keeperOpponent.getPos().y();

					if (keeperPosYCoordinate < 0)
					{
						backupShooter.setShootDirection(PenaltyShootSkill.ERotateDirection.CW);
					} else
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

	private class NoKeeperState extends AState
	{
		@Override
		public void doEntryActions()
		{
			var kick = new SingleTouchKickSkill(Geometry.getGoalTheir().getCenter(), KickParams.maxStraight());
			kick.getMoveCon().setPenaltyAreaOurObstacle(false);
			setNewSkill(kick);
		}
	}
}
