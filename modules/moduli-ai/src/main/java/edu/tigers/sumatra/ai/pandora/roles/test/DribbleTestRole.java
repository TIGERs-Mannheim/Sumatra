/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DragBallSkill;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.NotImplementedException;

import java.awt.Color;


@Log4j2
public class DribbleTestRole extends ARole
{
	@Setter
	private IVector2 practiceLocation;

	@Setter
	private EDribbleTestMovement testMovement = EDribbleTestMovement.NONE;

	@Setter
	boolean fullAuto = false;

	private EDribbleTestMovement currentMovement = EDribbleTestMovement.NONE;

	private IVector2 contactPos;
	private IVector2 contactBallPos;
	private final TimestampTimer waitTimer = new TimestampTimer(0.4);
	private final TimestampTimer turnWaitTimer = new TimestampTimer(2.5);
	private int repetitionCounter = 0;

	private enum EDribbleTestState
	{
		MOVE_TO_CENTER,
		WAIT,
		READY,
		FINISH
	}

	private EDribbleTestState currentState = EDribbleTestState.MOVE_TO_CENTER;


	public DribbleTestRole()
	{
		super(ERole.DRIBBLE_TEST);

		var getBallState = new GetBallLikeProtectState();
		var dribbleKickState = new DribbleState();

		getBallState.addTransition(ESkillState.SUCCESS, dribbleKickState);
		dribbleKickState.addTransition(ESkillState.FAILURE, getBallState);
		dribbleKickState.addTransition(this::isDribblingFinished, getBallState);

		setInitialState(getBallState);
	}


	@Override
	protected void beforeUpdate()
	{
		super.beforeUpdate();

		if (fullAuto)
		{
			var da = new DrawableAnnotation(
					getPos().subtractNew(Vector2.fromY(Geometry.getBotRadius() * 1.5)),
					currentMovement.toString(), Color.RED.darker()
			);
			getShapes(EAiShapesLayer.TEST_DRIBBLE).add(da);
		}
	}


	private boolean isDribblingFinished()
	{
		return currentState == EDribbleTestState.FINISH;
	}


	private class GetBallLikeProtectState extends RoleState<ProtectiveGetBallSkill>
	{
		public GetBallLikeProtectState()
		{
			super(ProtectiveGetBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			super.onInit();

			contactPos = null;
			contactBallPos = null;
			currentState = EDribbleTestState.MOVE_TO_CENTER;
			waitTimer.reset();

			currentMovement = EDribbleTestMovement.values()[(currentMovement.ordinal() + 1) % (
					EDribbleTestMovement.values().length - 1)];
			repetitionCounter = currentMovement.getRepetitions();
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			skill.getMoveCon().setGameStateObstacle(false);
			skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
			skill.setStrongDribblerContactNeeded(true);

			IVector2 practiceLocationToBall = getBall().getPos().subtractNew(practiceLocation);
			skill.setTarget(getBall().getPos().addNew(
					practiceLocationToBall.scaleToNew(Geometry.getBotRadius())));

			if (contactBallPos == null && getBot().getBallContact().hasContact())
			{
				contactBallPos = getBall().getPos();
			}
		}
	}

	private class DribbleState extends RoleState<DragBallSkill>
	{
		public DribbleState()
		{
			super(DragBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			skill.getMoveCon().setGameStateObstacle(false);
			if (currentState == EDribbleTestState.MOVE_TO_CENTER)
			{
				moveToCenter();
			} else if (currentState == EDribbleTestState.WAIT)
			{
				waitUntilNextState();
			} else if (currentState == EDribbleTestState.READY)
			{
				Pose moveToPose = getMoveToPose();

				boolean destinationReached = getPos().distanceTo(moveToPose.getPos()) < Geometry.getBotRadius();
				boolean orientationReached =
						AngleMath.diffAbs(getBot().getOrientation(), moveToPose.getOrientation()) < AngleMath.deg2rad(10);
				handleRepetitions(destinationReached, orientationReached);

				skill.setDestination(moveToPose.getPos());
				skill.setTargetOrientation(moveToPose.getOrientation());
			}
		}


		private Pose getMoveToPose()
		{
			Pose moveToPose;
			switch (fullAuto ? currentMovement : testMovement)
			{
				case NONE ->
				{
					var moveToPos = contactPos;
					var targetOrientation = contactBallPos.subtractNew(contactPos).getAngle();
					moveToPose = Pose.from(moveToPos, targetOrientation);
				}
				case BACKWARDS_TURN ->
				{
					var moveToPos = contactPos.addNew(
							contactPos.subtractNew(contactBallPos).scaleToNew(400));
					var targetOrientation = contactBallPos.subtractNew(contactPos).getAngle() + Math.PI;
					moveToPose = Pose.from(moveToPos, targetOrientation);
				}
				case FORWARD_TURN ->
				{
					var moveToPos = contactPos.addNew(
							contactPos.subtractNew(contactBallPos).scaleToNew(-400));
					var targetOrientation = contactBallPos.subtractNew(contactPos).getAngle() - Math.PI;
					moveToPose = Pose.from(moveToPos, targetOrientation);
				}
				case TURN_180 ->
				{
					var moveToPos = contactPos;
					var targetOrientation = contactBallPos.subtractNew(contactPos).getAngle() +
							((repetitionCounter % 2 == 0) ? Math.PI : -Math.PI);
					moveToPose = Pose.from(moveToPos, targetOrientation);
				}
				case TURN_LEFT ->
				{
					var moveToPos = contactPos;
					var targetOrientation = getBot().getOrientation() + Math.PI / 2.0;
					turnWaitTimer.update(getWFrame().getTimestamp());
					if (fullAuto && turnWaitTimer.isTimeUp(getWFrame().getTimestamp()))
					{
						turnWaitTimer.reset();
						currentState = EDribbleTestState.FINISH;
					}
					moveToPose = Pose.from(moveToPos, targetOrientation);
				}
				case TURN_RIGHT ->
				{
					var moveToPos = contactPos;
					var targetOrientation = getBot().getOrientation() - Math.PI / 2.0;
					turnWaitTimer.update(getWFrame().getTimestamp());
					if (fullAuto && turnWaitTimer.isTimeUp(getWFrame().getTimestamp()))
					{
						turnWaitTimer.reset();
						currentState = EDribbleTestState.FINISH;
					}
					moveToPose = Pose.from(moveToPos, targetOrientation);
				}
				case SIDEWAYS -> moveToPose = getPoseForSideways();
				case SIDEWAYS_TURN -> moveToPose = getPoseForSidewaysTurn();
				default -> throw new NotImplementedException("not implemented");
			}
			return moveToPose;
		}


		private Pose getPoseForSideways()
		{
			Pose moveToPose;
			var targetOrientation = contactBallPos.subtractNew(contactPos).getAngle();
			var moveToPos = contactPos.addNew(
					contactBallPos.subtractNew(contactPos).getNormalVector()
							.scaleToNew(repetitionCounter % 2 == 0 ? 600 : -600));
			moveToPose = Pose.from(moveToPos, targetOrientation);
			return moveToPose;
		}


		private Pose getPoseForSidewaysTurn()
		{
			Pose moveToPose;
			var targetOrientation =
					contactBallPos.subtractNew(contactPos).getAngle() + ((repetitionCounter % 2) == 0 ?
							Math.PI :
							0);
			var moveToPos = contactPos.addNew(
					contactBallPos.subtractNew(contactPos).getNormalVector()
							.scaleToNew(repetitionCounter % 2 == 0 ? 600 : -600));
			moveToPose = Pose.from(moveToPos, targetOrientation);
			return moveToPose;
		}


		private void handleRepetitions(boolean destinationReached, boolean orientationReached)
		{
			if (fullAuto && destinationReached && getBot().getBallContact().hasContact() && orientationReached)
			{
				if (repetitionCounter == 0)
				{
					currentState = EDribbleTestState.FINISH;
				} else
				{
					repetitionCounter--;
				}
			}
		}


		private void waitUntilNextState()
		{
			waitTimer.update(getWFrame().getTimestamp());
			if (waitTimer.isTimeUp(getWFrame().getTimestamp()))
			{
				currentState = EDribbleTestState.READY;
			}
		}


		private void moveToCenter()
		{
			skill.setDestination(practiceLocation);
			double practiceOrientation = practiceLocation.subtractNew(contactBallPos).multiplyNew(-1).getAngle();
			skill.setTargetOrientation(practiceOrientation);

			boolean practiceLocationReached = getPos().distanceTo(practiceLocation) < Geometry.getBotRadius();
			boolean practiceOrientationReached =
					AngleMath.diffAbs(getBot().getOrientation(), practiceOrientation) < AngleMath.deg2rad(10);
			if (practiceLocationReached && getBot().getBallContact().hasContact() && practiceOrientationReached)
			{
				contactPos = practiceLocation;
				contactBallPos = getBall().getPos();
				currentState = EDribbleTestState.WAIT;
			}
		}
	}
}
