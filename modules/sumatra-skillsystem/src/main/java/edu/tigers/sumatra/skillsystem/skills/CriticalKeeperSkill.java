/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.DefendingKeeper.ECriticalKeeperStates;
import edu.tigers.sumatra.skillsystem.skills.keeper.GoOutDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.IKeeperDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.InterceptBallCurveDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.InterceptBallLineDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.NormalBlockDestinationCalculator;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;


/**
 * Skill Implementation for Time critical keeper skills. Used by CriticalKeeperState
 */
public class CriticalKeeperSkill extends AMoveToSkill
{
	@Configurable(comment = "Use MoveConstraints PrimaryDirection while going out or catching redirects", defValue = "true")
	private static boolean usePrimaryDirectionOuterBlockingStates = true;

	@Configurable(comment = "Use ball curve for intercepts", defValue = "false")
	private static boolean useBallCurveForIntercept = false;

	@Configurable(comment = "[s] Necessary spare time to delay the catch redirect of the keeper", defValue = "0.3")
	private static double necessarySpareTimeToDelayRedirect = 0.3;

	private ECriticalKeeperStates currentState = ECriticalKeeperStates.NORMAL;
	private DefendingKeeper defendingKeeper = new DefendingKeeper(this);


	public CriticalKeeperSkill()
	{
		var blockState = new NormalBlock();
		setInitialState(blockState);

		addTransition(ECriticalKeeperStates.INTERCEPT_BALL, new InterceptBall());
		addTransition(ECriticalKeeperStates.DEFEND_REDIRECT, new CatchRedirect());
		addTransition(ECriticalKeeperStates.GO_OUT, new GoOut());
		addTransition(ECriticalKeeperStates.NORMAL, blockState);
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		defendingKeeper.update(getWorldFrame());

		ECriticalKeeperStates nextState = defendingKeeper.calcNextKeeperState();
		if (nextState != currentState)
		{
			currentState = nextState;
			triggerEvent(nextState);
		}
	}


	private void prepareMoveCon()
	{
		getMoveCon().setPenaltyAreaOurObstacle(false);
		getMoveCon().setBotsObstacle(false);
		getMoveCon().setBallObstacle(false);
		getMoveCon().setGoalPostsObstacle(false);
	}


	private void prepareMovementConstraints()
	{
		getMoveConstraints().setPrimaryDirection(Vector2f.ZERO_VECTOR);
	}


	private IVector2 getBallPosWithLookahead()
	{
		return getWorldFrame().getBall().getTrajectory().getPosByTime(0.1).getXYVector();
	}


	private IVector2 getPrimaryDirectionOuterBlockingStates(final IVector2 threatPosition,
			final IVector2 defensePosition)
	{
		IVector2 finalPrimaryDirection = Vector2f.ZERO_VECTOR;
		if (usePrimaryDirectionOuterBlockingStates)
		{
			var primaryDirection = Vector2.fromPoints(threatPosition, defensePosition);
			var keeperPrimaryDirectionIntersection = Lines.halfLineFromDirection(getPos(), getVel())
					.intersect(Lines.lineFromDirection(threatPosition, primaryDirection)).asOptional();
			final Color primaryDirectionColor;
			if (keeperPrimaryDirectionIntersection.isEmpty() || Geometry.getField()
					.isPointInShape(keeperPrimaryDirectionIntersection.get()))
			{
				finalPrimaryDirection = primaryDirection;
				primaryDirectionColor = Color.BLACK;
			} else
			{
				primaryDirectionColor = Color.GRAY;
			}
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(Lines.segmentFromOffset(getPos(), primaryDirection.scaleToNew(-1000)),
							primaryDirectionColor));
		}
		return finalPrimaryDirection;
	}


	private class InterceptBall extends AState
	{
		private double targetAngle;
		private IKeeperDestinationCalculator destCalculator;


		@Override
		public void doEntryActions()
		{
			targetAngle = getAngle();
			prepareMoveCon();
			prepareMovementConstraints();
			if (useBallCurveForIntercept)
			{
				destCalculator = new InterceptBallLineDestinationCalculator();
			} else
			{
				destCalculator = new InterceptBallCurveDestinationCalculator();
			}
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			if (getBall().getVel().isZeroVector())
			{
				// skill is not designed for lying balls
				return;
			}
			updateDestination(
					destCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), getMoveConstraints(), null));
			updateTargetAngle(calcTargetAngle());

			super.doUpdate();
		}


		public double calcTargetAngle()
		{
			if (VectorMath.distancePP(getBall().getPos(), getTBot().getPos()) < (Geometry.getBotRadius() / 2))
			{
				return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			}
			return targetAngle;
		}
	}

	private class CatchRedirect extends AState
	{
		private boolean stayInGoOut = false;
		private boolean spareTimeOver = false;
		private IKeeperDestinationCalculator goOutCalculator;
		private IKeeperDestinationCalculator normalBlockCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			getMoveCon().setFieldBorderObstacle(false);
			stayInGoOut = false;
			spareTimeOver = false;

			goOutCalculator = new GoOutDestinationCalculator();
			normalBlockCalculator = new NormalBlockDestinationCalculator();

			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			IVector2 redirectPos = getRedirectBotPosition();
			IVector2 destination = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
					getMoveConstraints(), redirectPos);

			if (VectorMath.distancePP(getPos(), destination) < (Geometry.getBotRadius() / 2))
			{
				updateLookAtTarget(redirectPos);
			}
			getMoveConstraints().setPrimaryDirection(getPrimaryDirectionOuterBlockingStates(redirectPos, destination));
			if ((isKeeperBetweenRedirectAndNormalBlock(redirectPos, destination) || stayInGoOut)
					&& isRedirectGoOutFeasible(redirectPos))
			{
				stayInGoOut = true;
				destination = goOutCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), getMoveConstraints(),
						redirectPos);
				updateLookAtTarget(redirectPos);
			} else
			{
				stayInGoOut = false;
			}
			if (isSpareTimeLeft(redirectPos, destination) && !spareTimeOver)
			{
				destination = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
						getMoveConstraints(), getBallPosWithLookahead());
			} else
			{
				spareTimeOver = true;
			}
			updateDestination(destination);
			super.doUpdate();
		}


		private boolean isRedirectGoOutFeasible(IVector2 redirectPos)
		{
			return defendingKeeper.isPositionCloseToPenaltyArea(redirectPos) || defendingKeeper.isGoOutFeasible();
		}


		private IVector2 getRedirectBotPosition()
		{
			IVector2 redirectBotPosition;
			var redirectBotId = defendingKeeper.getBestRedirector(getWorldFrame().getOpponentBots());
			if (redirectBotId.isBot())
			{
				redirectBotPosition = getWorldFrame().getOpponentBot(redirectBotId).getBotKickerPos();
			} else
			{
				// .getOpponentClosestToBall from tactical field
				redirectBotPosition = getWorldFrame().getOpponentBots().values().stream().map(ITrackedBot::getBotKickerPos)
						.min(new VectorDistanceComparator(getWorldFrame().getBall().getPos())).orElse(Vector2.fromXY(0, 0));
			}
			return redirectBotPosition;
		}


		private boolean isKeeperBetweenRedirectAndNormalBlock(IVector2 redirectPos, IVector2 normalBlockPosition)
		{
			return Lines.segmentFromPoints(redirectPos, normalBlockPosition).distanceTo(getPos())
					< Geometry.getBotRadius();
		}


		private boolean isSpareTimeLeft(IVector2 redirectPos, IVector2 destination)
		{
			var timeLeft = getBall().getTrajectory().getTimeByPos(redirectPos);
			var drivingTime = TrajectoryGenerator.generatePositionTrajectory(getTBot(), destination).getTotalTime();
			var spareTime = timeLeft - drivingTime;

			var shapes = getShapes().get(ESkillShapesLayer.KEEPER);
			shapes.add(new DrawableCircle(destination, Geometry.getBotRadius(), Color.RED));
			shapes.add(new DrawableAnnotation(destination, String.format("%.2f", spareTime), Color.RED));
			return spareTime > necessarySpareTimeToDelayRedirect;
		}
	}

	private class GoOut extends AState
	{
		private IKeeperDestinationCalculator goOutCalculator;
		private IKeeperDestinationCalculator normalBlockCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			goOutCalculator = new GoOutDestinationCalculator();
			normalBlockCalculator = new NormalBlockDestinationCalculator();
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			var ballPos = getBallPosWithLookahead();
			IVector2 normalBlockingPos = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
					getMoveConstraints(), ballPos);
			IVector2 targetPosition;
			if (isBallInsidePenaltyArea())
			{
				targetPosition = normalBlockingPos;
			} else
			{
				targetPosition = goOutCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
						getMoveConstraints(), ballPos);
			}
			getMoveConstraints().setPrimaryDirection(
					getPrimaryDirectionOuterBlockingStates(getBall().getPos(), normalBlockingPos));
			updateDestination(targetPosition);
			updateTargetAngle(getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle());

			super.doUpdate();

			draw(targetPosition);
		}


		private boolean isBallInsidePenaltyArea()
		{
			return Geometry.getPenaltyAreaOur()
					.withMargin(-2 * Geometry.getBotRadius())
					.isPointInShape(getWorldFrame().getBall().getPos());
		}


		private void draw(IVector2 targetPose)
		{
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(getWorldFrame().getBall().getPos(), Geometry.getGoalOur().getLeftPost()));
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(getWorldFrame().getBall().getPos(), Geometry.getGoalOur().getRightPost()));
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableCircle(targetPose, Geometry.getBotRadius(), Color.green));
		}


	}

	private class NormalBlock extends AState
	{
		IKeeperDestinationCalculator destCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			prepareMovementConstraints();
			destCalculator = new NormalBlockDestinationCalculator();
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			updateDestination(destCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), getMoveConstraints(),
					getBallPosWithLookahead()));
			updateLookAtTarget(getBall());

			super.doUpdate();
		}


	}


}
