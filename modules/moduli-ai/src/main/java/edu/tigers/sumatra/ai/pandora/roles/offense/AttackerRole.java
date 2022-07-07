/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.situation.EOffensiveExecutionStatus;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.offense.dribble.EDribblingCondition;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ApproachBallLineSkill;
import edu.tigers.sumatra.skillsystem.skills.DragBallSkill;
import edu.tigers.sumatra.skillsystem.skills.DribbleKickSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.ProtectAndMoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RotateWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.Optional;


/**
 * Kick the still or moving ball.
 */
@Log4j2
public class AttackerRole extends ARole
{
	@Configurable(defValue = "2000.0")
	private static double switchToKickDist = 2000;

	@Configurable(defValue = "0.85")
	private static double maxBallVel = 0.85;

	@Configurable(defValue = "250.0")
	private static double blockingCircleRadius = 250.0;

	private Pass pass;
	private Kick kick;
	private IVector2 ballContactPos;
	@Setter
	private DribbleToPos dribbleToPos;
	@Setter
	private boolean useSingleTouch;
	@Setter
	private boolean waitForKick;
	@Setter
	private boolean allowPenAreas;

	private String previousState = "";


	public AttackerRole()
	{
		super(ERole.ATTACKER);

		var protectState = new ProtectState();
		var dribbleState = new DribbleState();
		var approachAndStopBallState = new ApproachAndStopBallState();
		var approachBallLineState = new ApproachBallLineState();
		var kickState = new KickState();
		var freeKickState = new FreeKickState();
		var receiveState = new ReceiveState();
		var redirectState = new RedirectState();
		var dribbleToState = new DribbleToState();
		var rotateWithBallState = new RotateWithBallState();
		var dribblingKickState = new DribblingKickState();

		setInitialState(protectState);
		dribbleState.addTransition(this::switchToDribbleTo, dribbleToState);
		rotateWithBallState.addTransition(this::switchToDribbleTo, dribbleToState);
		rotateWithBallState.addTransition(ESkillState.FAILURE, protectState);
		dribbleState.addTransition(this::switchToRotate, rotateWithBallState);
		dribbleToState.addTransition(ESkillState.FAILURE, dribbleState);
		dribbleToState.addTransition(ESkillState.SUCCESS, dribbleState);
		protectState.addTransition(this::ballMoves, approachBallLineState);
		protectState.addTransition(this::switchToKick, kickState);
		dribbleState.addTransition(this::switchToKick, kickState);
		rotateWithBallState.addTransition(this::switchToKick, kickState);
		protectState.addTransition(ESkillState.SUCCESS, dribbleState);
		dribbleState.addTransition(ESkillState.FAILURE, protectState);
		approachBallLineState.addTransition(ESkillState.SUCCESS, receiveState);
		approachBallLineState.addTransition(ESkillState.FAILURE, approachAndStopBallState);
		approachBallLineState.addTransition(this::closeToBall, approachAndStopBallState);
		approachAndStopBallState.addTransition(ESkillState.SUCCESS, protectState);
		approachAndStopBallState.addTransition(ESkillState.FAILURE, protectState);
		kickState.addTransition(ESkillState.SUCCESS, approachBallLineState);
		kickState.addTransition(ESkillState.FAILURE, protectState);
		kickState.addTransition(this::kickStateIsInvalid, protectState);
		kickState.addTransition(() -> waitForKick || useSingleTouch, freeKickState);
		freeKickState.addTransition(ESkillState.SUCCESS, approachBallLineState);
		freeKickState.addTransition(ESkillState.FAILURE, protectState);
		receiveState.addTransition(ESkillState.SUCCESS, protectState);
		receiveState.addTransition(ESkillState.FAILURE, protectState);
		receiveState.addTransition(this::switchToRedirect, redirectState);
		redirectState.addTransition(ESkillState.SUCCESS, approachBallLineState);
		redirectState.addTransition(ESkillState.FAILURE, protectState);
		redirectState.addTransition(this::switchToReceive, receiveState);
		dribblingKickState.addTransition(this::dribblingKickIsBlocked, protectState);
		dribblingKickState.addTransition(ESkillState.FAILURE, protectState);
		dribbleState.addTransition(this::switchToDribbleKick, dribblingKickState);
		rotateWithBallState.addTransition(this::switchToDribbleKick, dribblingKickState);
	}


	private boolean kickStateIsInvalid()
	{
		return kick == null || (dribbleToPos != null
				&& dribbleToPos.getDribblingCondition() == EDribblingCondition.DRIBBLING_KICK);
	}


	private boolean dribblingKickIsBlocked()
	{
		return dribbleToPos == null || dribbleToPos.getDribblingCondition() != EDribblingCondition.DRIBBLING_KICK;
	}


	private boolean switchToDribbleKick()
	{
		return dribbleToPos != null && (dribbleToPos.getDribblingCondition() == EDribblingCondition.DRIBBLING_KICK);
	}


	public void setPass(Pass pass)
	{
		this.pass = pass;
		this.kick = pass.getKick();
		this.ballContactPos = kick.getSource();
	}


	public void setKick(Kick kick)
	{
		this.pass = null;
		this.kick = kick;
		this.ballContactPos = kick.getSource();
	}


	public void setBallContactPos(IVector2 ballContactPos)
	{
		this.pass = null;
		this.kick = null;
		this.ballContactPos = ballContactPos;
	}


	public EOffensiveExecutionStatus getExecutionStatus()
	{
		boolean switchedState = !previousState.equals(getCurrentState().getIdentifier());
		previousState = getCurrentState().getIdentifier();
		if (getPos().distanceTo(getBall().getPos()) > 200 || switchedState)
		{
			return EOffensiveExecutionStatus.GETTING_READY;
		}
		return EOffensiveExecutionStatus.IMMINENT;
	}


	private boolean switchToRedirect()
	{
		return ballMoves() && kick != null;
	}


	private boolean switchToDribbleTo()
	{
		return dribbleToPos != null && dribbleToPos.getDribblingCondition() == EDribblingCondition.REPOSITION
				&& dribbleToPos.getDribbleToDestination() != null;
	}


	private boolean switchToRotate()
	{
		if (dribbleToPos == null)
		{
			return false;
		}
		return isOpponentBlockingBall();
	}


	private boolean isOpponentBlockingBall()
	{
		var closestEnemyBotID = getAiFrame().getTacticalField().getOpponentClosestToBall().getBotId();
		if (getWFrame().getOpponentBots().containsKey(closestEnemyBotID))
		{
			var blockingCircle = Circle.createCircle(getWFrame().getOpponentBot(closestEnemyBotID).getPos(),
					blockingCircleRadius);
			getShapes(EAiShapesLayer.OFFENSIVE_DRIBBLE).add(new DrawableCircle(blockingCircle).setColor(Color.white));

			var timeToBall = getBot().getCurrentTrajectory().map(ITrajectory::getTotalTime).orElse(0.1);
			var lookaheadLine = Lines.segmentFromPoints(getWFrame().getOpponentBot(closestEnemyBotID).getPos(),
					getWFrame().getOpponentBot(closestEnemyBotID).getPosByTime(timeToBall));

			var ballCircle = Circle.createCircle(getBall().getPos(),
					Geometry.getBotRadius() * 2.0 + Geometry.getBallRadius());

			if (ballCircle.isIntersectingWithLine(lookaheadLine))
			{
				getShapes(EAiShapesLayer.OFFENSIVE_DRIBBLE).add(new DrawableCircle(ballCircle).setColor(Color.RED));
				return true;
			} else
			{
				getShapes(EAiShapesLayer.OFFENSIVE_DRIBBLE).add(new DrawableCircle(ballCircle).setColor(Color.WHITE));
			}

			getShapes(EAiShapesLayer.OFFENSIVE_DRIBBLE).add(new DrawableLine(lookaheadLine).setColor(Color.WHITE));

			if (blockingCircle.isPointInShape(getWFrame().getBall().getPos()))
			{
				return true;
			}
		}
		return dribbleToPos.getDribblingCondition() == EDribblingCondition.REPOSITION
				&& dribbleToPos.getDribbleToDestination() == null;
	}


	private boolean switchToReceive()
	{
		return kick == null;
	}


	private boolean switchToKick()
	{
		boolean ballContactOrNoOpponentNearBall = getBot().getBallContact().hasContactFromVisionOrBarrier()
				|| getAiFrame().getTacticalField().getOpponentClosestToBall().getDist() > Geometry.getBotRadius() * 1.5;
		boolean validKickAndIsClose =
				kick != null && getBall().getPos().distanceTo(getPos()) < switchToKickDist && dribbleToPos == null;
		return validKickAndIsClose && ballContactOrNoOpponentNearBall;
	}


	private boolean ballMoves()
	{
		return getBall().getVel().getLength2() > maxBallVel;
	}


	private boolean closeToBall()
	{
		return getBall().getPos().distanceTo(getPos()) < 200;
	}


	@Override
	protected void afterUpdate()
	{
		if (pass != null)
		{
			var color = getAiFrame().getTeamColor().getColor();
			var drawables = pass.createDrawables();
			drawables.forEach(d -> d.setColor(color));
			getShapes(EAiShapesLayer.OFFENSIVE_ATTACKER).addAll(drawables);
		} else if (kick != null)
		{
			var drawables = kick.createDrawables();
			drawables.forEach(d -> d.setColor(Color.red));
			getShapes(EAiShapesLayer.OFFENSIVE_ATTACKER).addAll(drawables);
		}
	}


	/**
	 * Approach the ball line of a moving ball (intercept the ball)
	 */
	private class ApproachBallLineState extends RoleState<ApproachBallLineSkill>
	{
		ApproachBallLineState()
		{
			super(ApproachBallLineSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.setDesiredBallCatchPos(ballContactPos);
			skill.setTarget(Optional.ofNullable(kick).map(Kick::getTarget).orElse(null));
			if (allowPenAreas)
			{
				skill.getMoveCon().setPenaltyAreaOurObstacle(false);
				skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			}
		}
	}

	private class ApproachAndStopBallState extends RoleState<ApproachAndStopBallSkill>
	{
		private ApproachAndStopBallState()
		{
			super(ApproachAndStopBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			if (allowPenAreas)
			{
				skill.getMoveCon().setPenaltyAreaOurObstacle(false);
				skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			}
		}
	}

	private class KickState extends RoleState<TouchKickSkill>
	{
		KickState()
		{
			super(TouchKickSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.setTarget(kick.getTarget());
			skill.setPassRange(kick.getAimingTolerance());
			skill.setDesiredKickParams(kick.getKickParams());
			if (allowPenAreas)
			{
				skill.getMoveCon().setPenaltyAreaOurObstacle(false);
				skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			}
		}
	}

	private class FreeKickState extends RoleState<SingleTouchKickSkill>
	{
		FreeKickState()
		{
			super(SingleTouchKickSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			// kick == null: No pass possible, wait for another possibility or just perform the current kick
			if (kick != null)
			{
				skill.setTarget(kick.getTarget());
				skill.setPassRange(kick.getAimingTolerance());
				skill.setDesiredKickParams(kick.getKickParams());
				skill.setReadyForKick(!waitForKick);
			}
			if (allowPenAreas)
			{
				skill.getMoveCon().setPenaltyAreaOurObstacle(false);
				skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			}

			if (waitForKick)
			{
				getShapes(EAiShapesLayer.OFFENSIVE_ATTACKER)
						.add(new DrawableAnnotation(getPos(), "Waiting")
								.withCenterHorizontally(true)
								.withOffset(Vector2.fromY(100))
								.setColor(Color.red));
			}
		}
	}


	private class ReceiveState extends RoleState<ReceiveBallSkill>
	{
		ReceiveState()
		{
			super(ReceiveBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			// State is only activated when close. Let it hold its position!
			skill.getMoveCon().setTheirBotsObstacle(false);
			skill.setBallReceivingPosition(ballContactPos);
			if (allowPenAreas)
			{
				skill.getMoveCon().setPenaltyAreaOurObstacle(false);
				skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
				skill.setConsideredPenAreas(ETeam.UNKNOWN);
			}
		}
	}


	private class RedirectState extends RoleState<RedirectBallSkill>
	{
		RedirectState()
		{
			super(RedirectBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			// State is only activated when close. Let it hold its position!
			skill.getMoveCon().setTheirBotsObstacle(false);
			skill.setBallReceivingPosition(ballContactPos);
			skill.setTarget(kick.getTarget());
			skill.setDesiredKickParams(kick.getKickParams());
			if (allowPenAreas)
			{
				skill.getMoveCon().setPenaltyAreaOurObstacle(false);
				skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
				skill.setConsideredPenAreas(ETeam.UNKNOWN);
			}
		}
	}

	private class ProtectState extends RoleState<ProtectiveGetBallSkill>
	{

		ProtectState()
		{
			super(ProtectiveGetBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			if (dribbleToPos != null && dribbleToPos.getDribblingCondition() != EDribblingCondition.DRIBBLING_KICK)
			{
				var protectionPos = dribbleToPos.getProtectFromPos();
				if (isOpponentBlockingBall())
				{
					// mirror protectionPos if opponent is blocking the ball
					var posToBall = getBall().getPos().subtractNew(protectionPos);
					var mirroredPos = protectionPos.addNew(posToBall.multiplyNew(2.0));
					getShapes(EAiShapesLayer.OFFENSIVE_ATTACKER).add(
							new DrawableCircle(Circle.createCircle(mirroredPos, 50), Color.RED).setFill(true));
					getShapes(EAiShapesLayer.OFFENSIVE_ATTACKER).add(new DrawableAnnotation(getPos(), "Opponent blocking!"));
					skill.setProtectionTarget(mirroredPos);
				} else
				{
					getShapes(EAiShapesLayer.OFFENSIVE_ATTACKER).add(new DrawableAnnotation(getPos(), "default!"));
					getShapes(EAiShapesLayer.OFFENSIVE_ATTACKER).add(
							new DrawableCircle(Circle.createCircle(dribbleToPos.getProtectFromPos(), 50), Color.RED));
					skill.setProtectionTarget(dribbleToPos.getProtectFromPos());
				}
			} else
			{
				// fallback
				getShapes(EAiShapesLayer.OFFENSIVE_ATTACKER).add(new DrawableAnnotation(getPos(), "fallback!"));
				skill.setProtectionTarget(getFallBackProtectionTarget());
			}
		}


		private IVector2 getFallBackProtectionTarget()
		{
			var closestEnemyBotID = getAiFrame().getTacticalField().getOpponentClosestToBall().getBotId();
			IVector2 opponentPos;
			if (getWFrame().getOpponentBots().containsKey(closestEnemyBotID))
			{
				opponentPos = getWFrame().getOpponentBot(closestEnemyBotID).getPos();
			} else
			{
				opponentPos = Geometry.getGoalOur().getCenter();
			}
			var opponentToBall = getBall().getPos().subtractNew(opponentPos);
			return getBall().getPos().addNew(opponentToBall.scaleToNew(Geometry.getBotRadius() * 4.0));
		}
	}

	private class DribbleState extends RoleState<ProtectAndMoveWithBallSkill>
	{

		DribbleState()
		{
			super(ProtectAndMoveWithBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			if (dribbleToPos != null)
			{
				skill.setProtectionTarget(dribbleToPos.getProtectFromPos());
			} else
			{
				// fallback
				var protectionTarget = getBall().getPos().addNew(
						Geometry.getGoalOur().getCenter().subtractNew(getBall().getPos())
								.scaleToNew(-Geometry.getBotRadius() * 4));
				skill.setProtectionTarget(protectionTarget);
			}
			if (allowPenAreas)
			{
				skill.getMoveCon().setPenaltyAreaOurObstacle(false);
				skill.getMoveCon().setPenaltyAreaTheirObstacle(false);
			}
		}
	}

	private class DribbleToState extends RoleState<DragBallSkill>
	{
		DribbleToState()
		{
			super(DragBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			if (dribbleToPos != null)
			{
				skill.setDestination(dribbleToPos.getDribbleToDestination());
			}
			skill.setTargetOrientation(getBot().getOrientation());
		}
	}

	private class RotateWithBallState extends RoleState<RotateWithBallSkill>
	{
		RotateWithBallState()
		{
			super(RotateWithBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			if (dribbleToPos != null)
			{
				skill.setProtectionTarget(dribbleToPos.getProtectFromPos());
			}
		}
	}


	private class DribblingKickState extends RoleState<DribbleKickSkill>
	{
		DribbleToPos lastValidDribbleToPos = null;


		DribblingKickState()
		{
			super(DribbleKickSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			var rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
			rater.setObstacles(getWFrame().getOpponentBots().values());
			var bestGoalKick = rater.rate(getBot().getBotKickerPos());

			var canScoreGoalHere = bestGoalKick.isPresent() && bestGoalKick.get().getScore() > 0.5;
			if (dribbleToPos != null && kick != null)
			{
				skill.setTarget(kick.getTarget());
				skill.setDestination(dribbleToPos.getDribbleToDestination());
				skill.setTargetBlocked(
						getPos().distanceTo(dribbleToPos.getDribbleToDestination()) >= Geometry.getBotRadius()
								&& !canScoreGoalHere);
				lastValidDribbleToPos = dribbleToPos;
			} else if (lastValidDribbleToPos != null)
			{
				skill.setTargetBlocked(
						getPos().distanceTo(lastValidDribbleToPos.getDribbleToDestination()) >= Geometry.getBotRadius()
								&& !canScoreGoalHere);
			}
		}
	}
}
