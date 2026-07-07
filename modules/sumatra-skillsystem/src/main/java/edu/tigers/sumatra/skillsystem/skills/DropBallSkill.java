package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.botmanager.data.BotFeedback;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


/**
 * Drop the ball smoothly by reducing the max dribble current.
 */
@Log4j2
public class DropBallSkill extends AMoveToSkill
{

	@Configurable(comment = "Use maxAcceleration for movement close to ball instead of regular bot limits.", defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean maxAccelerationOverride = false;

	@Configurable(comment = "Max acceleration for movement close to ball.", defValue = "3.0", unit = EConfigUnit.ACCELERATION_M)
	private static double maxAcceleration = 3.0;

	@Configurable(comment = "Max velocity for movement close to ball.", defValue = "0.5", unit = EConfigUnit.VELOCITY_M)
	private static double maxVelocity = 0.5;

	@Configurable(comment = "Time to wait per m/s of estimated ball spin.", defValue = "0.3", unit = EConfigUnit.TIME_S)
	private static double ballSpinWaitFactor = 0.3;

	@Configurable(comment = "Dribbler spin subtracted as noise from the m/s of estimated ball spin.", defValue = "0.5", unit = EConfigUnit.VELOCITY_M)
	private static double ballSpinDeadZone = 0.5;

	@Configurable(comment = "Time to wait for ball contact loss.", defValue = "0.5", unit = EConfigUnit.TIME_S)
	private static double waitBallDroppedTime = 0.5;

	@Configurable(comment = "Tolerance for reaching robot positions.", defValue = "10.0", unit = EConfigUnit.DISTANCE_MM)
	private static double positionTolerance = 10.0;

	private IVector2 ballTargetPos;
	private double orientation;
	private final double placementTolerance;


	public DropBallSkill(double placementTolerance)
	{
		this.placementTolerance = placementTolerance;

		var disableDribbler = new DisableDribblerState();
		var backwardMove = new MoveToState(-placementTolerance - positionTolerance);
		var evaluationState = new EvaluationState();

		addTransition(EDropBallEvent.START_DROP, disableDribbler);
		addTransition(disableDribbler, EDropBallEvent.DRIBBLER_OFF, backwardMove);
		addTransition(backwardMove, EDropBallEvent.DESTINATION_REACHED, evaluationState);
		addTransition(backwardMove, EDropBallEvent.LOST_BALL, evaluationState);
		addTransition(evaluationState, EDropBallEvent.EVALUATION_DONE, IDLE_STATE);

		setInitialState(IDLE_STATE);
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();

		ballTargetPos = getBall().getPos();
		orientation = ballTargetPos.subtractNew(getPos()).getAngle();

		// We won't move that much, just ignore all obstacles to make sure we correctly drop the ball
		setMoveCon(getMoveCon().withNoObstacles());

		if (getTBot().getBallContact().hadContact(waitBallDroppedTime))
		{
			setSkillState(ESkillState.IN_PROGRESS);
			triggerEvent(EDropBallEvent.START_DROP);
		} else
		{
			setSkillState(ESkillState.SUCCESS);
		}
	}


	private enum EDropBallEvent implements IEvent
	{
		START_DROP,
		DRIBBLER_OFF,
		DESTINATION_REACHED,
		LOST_BALL,
		EVALUATION_DONE
	}


	@RequiredArgsConstructor
	private class MoveToState extends AState
	{
		private final double offset;


		@Override
		public void doUpdate()
		{
			double dist2Ball = Geometry.getBallRadius() + getTBot().getCenter2DribblerDist();
			IVector2 destination = Vector2.fromAngleLength(orientation, offset - dist2Ball).add(ballTargetPos);

			if (!getTBot().getBallContact().hadContact(waitBallDroppedTime))
			{
				triggerEvent(EDropBallEvent.LOST_BALL);
				return;
			}

			if (getPos().distanceTo(destination) < positionTolerance)
			{
				triggerEvent(EDropBallEvent.DESTINATION_REACHED);
				return;
			}

			var mc = getMoveConstraints().withVelMax(maxVelocity);
			if (maxAccelerationOverride)
			{
				mc = mc.withAccMax(maxAcceleration);
			}

			setComeToAStop(false);
			setMoveConstraints(mc);
			updateDestination(destination);
		}
	}


	private class DisableDribblerState extends AState
	{
		private long tEnd = 0;

		@Override
		public void doUpdate()
		{
			BotFeedback feedback = getBot().getLastReceivedBotFeedback();
			if(feedback.getDribbleSpeed() > ballSpinDeadZone)
			{
				tEnd = Math.max(
						tEnd,
						getTimestamp() + (long) (feedback.getDribbleSpeed() * ballSpinWaitFactor * 1e9)
				);
			}

			setComeToAStop(true);
			setKickParams(KickParams.disarm().withDribbleSpeed(0, 0));

			if (getBot().getLastReceivedBotFeedback().getDribbleTraction() == EDribbleTractionState.OFF &&
					tEnd < getTimestamp())
			{
				triggerEvent(EDropBallEvent.DRIBBLER_OFF);
			}
		}
	}


	private class EvaluationState extends AState
	{
		@Override
		public void doUpdate()
		{
			// Target position is not updated to continue driving away from the ball
			if (getBall().getVel().getLength() > 0.1)
			{
				return;
			}

			double newRollbackDistance
					= getBall().getPos().subtractNew(ballTargetPos).turn(-orientation).x();

			log.debug("Ball rolled {}mm away from the robot", newRollbackDistance);

			boolean droppedBall = !getTBot().getBallContact().hadContact(waitBallDroppedTime);
			boolean ballAtTarget = getBall().getPos().distanceTo(ballTargetPos) < placementTolerance;
			setSkillState(droppedBall && ballAtTarget ? ESkillState.SUCCESS : ESkillState.FAILURE);
			triggerEvent(EDropBallEvent.EVALUATION_DONE);
		}
	}
}
