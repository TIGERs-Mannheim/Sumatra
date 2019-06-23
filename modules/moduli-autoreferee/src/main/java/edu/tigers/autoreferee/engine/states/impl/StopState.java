/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.states.impl;

import static edu.tigers.autoreferee.engine.AutoRefMath.DEFENSE_AREA_GOAL_LINE_DISTANCE;
import static edu.tigers.autoreferee.engine.AutoRefMath.THROW_IN_DISTANCE;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefGlobalState;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * The stop state rule initiates new actions depending on the queue follow up action. If the ball is not in the correct
 * position for the action a ball placement command is issued. If a ball placement has been attempted but the ball is
 * still not in the correct position the game will restart anyway after a maximum wait time
 * {@literal AutoRefConfig#getMaxUnplacedWaitTime()} or the ball is closely placed and the closely placed wait time has
 * elapsed.
 * 
 * <pre>
 *                                         v
 *              +-----------+         +-----------+
 * +----+   No  | Has       |  Yes    | Minimum   | No  +----+
 * |Exit| <-----+ follow-up +-------> | wait time +---> |Exit|
 * +----+       | action?   |         | over?     |     +----+
 *              +-----------+         |           |
 *                                    +----+------+
 *                                         |
 *                                         v Yes
 * 
 *       +-------------------+    Yes +---------+  No  +------------+
 *       |  Bots stationary? | <------+ Is ball +----> | Placement  |
 *       |  Stop distance    |        | placed? |      | attempted? |
 *       |  correct?         |        +---------+      ++-------+---+
 *       +--+----------+-----+                          |       |
 *          |          |                         Yes +--+       +--+ No
 *   No  +--+          |Yes                          v             v
 *       v             v
 *                                                +------+   +------------+
 *    +------+    +-------------+                 | Exit |   | Place ball |
 *    | Exit |    | Send action |                 +------+   +------------+
 *    +------+    | command     |
 *                +-------------+
 * </pre>
 * 
 * @author "Lukas Magel"
 */
public class StopState extends AbstractAutoRefState
{
	private static final Color PLACEMENT_CIRCLE_COLOR = Color.BLUE;
	
	@Configurable(comment = "[ms] Time to wait before performing an action after reaching the stop state", defValue = "2000")
	private static long stopWaitTimeMs = 2_000;
	
	@Configurable(comment = "[ms] The time to wait after all bots have come to a stop and the ball has been placed correctly", defValue = "3000")
	private static long readyWaitTimeMs = 3_000;
	
	@Configurable(comment = "Simulation only: Move the ball slowly towards target instead of just placing it", defValue = "true")
	private static boolean moveBallSlowlyToTarget = true;
	
	static
	{
		AbstractAutoRefState.registerClass(StopState.class);
	}
	
	private Long readyTime;
	private boolean simulationPlacementAttempted = false;
	private Referee.SSL_Referee.Stage lastStage = null;
	
	private final Random rnd = new Random(System.currentTimeMillis());
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		readyTime = null;
		simulationPlacementAttempted = false;
		
		if (ctx.getAutoRefGlobalState().getBallPlacementStage() == AutoRefGlobalState.EBallPlacementStage.IN_PROGRESS)
		{
			ctx.getAutoRefGlobalState().setBallPlacementStage(AutoRefGlobalState.EBallPlacementStage.CANCELED);
		} else
		{
			ctx.getAutoRefGlobalState().setBallPlacementStage(AutoRefGlobalState.EBallPlacementStage.UNKNOWN);
		}
	}
	
	
	private void updatePlacementCounter(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		Referee.SSL_Referee.Stage stage = frame.getRefereeMsg().getStage();
		if (lastStage == null || lastStage != stage)
		{
			lastStage = stage;
			ctx.getAutoRefGlobalState().getFailedBallPlacements().clear();
		}
	}
	
	
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	@Override
	public void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		updatePlacementCounter(frame, ctx);
		if (ctx.getFollowUpAction() == null)
		{
			setCanProceed(false);
			return;
		}
		
		FollowUpAction action = ctx.getFollowUpAction();
		
		ITrackedBall ball = frame.getWorldFrame().getBall();
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ENGINE);
		
		IVector2 kickPos = determineKickPos(action);
		
		visualizeKickPos(shapes, kickPos);
		
		/*
		 * Wait a minimum amount of time before doing anything
		 */
		if (stillInTime(stopWaitTimeMs))
		{
			return;
		}
		
		setCanProceed(true);
		
		boolean ballPlaced = checkBallPlaced(ball, kickPos, shapes);
		boolean ballStationary = AutoRefMath.ballIsStationary(ball);
		
		boolean botsCorrectDistance = checkBotStopDistance(frame, shapes);
		boolean readyWaitTimeOver = false;
		
		if (ballPlaced && botsCorrectDistance)
		{
			if (readyTime == null)
			{
				readyTime = frame.getTimestamp();
			}
			long waitTimeNS = frame.getTimestamp() - readyTime;
			readyWaitTimeOver = waitTimeNS > TimeUnit.MILLISECONDS.toNanos(readyWaitTimeMs);
			drawReadyCircle((int) ((TimeUnit.NANOSECONDS.toMillis(waitTimeNS) * 100L) / readyWaitTimeMs),
					ball.getPos(), shapes);
		} else
		{
			readyTime = null;
		}
		
		
		final ETeamColor placementTeam = getPlacementTeam(action, ctx);
		if (placeBallByTeams(frame, ctx, placementTeam))
		{
			if (ballStationary && !ballPlaced)
			{
				// Try to place the ball
				final RefboxRemoteCommand placementCommand = new RefboxRemoteCommand(
						placementCommand(placementTeam),
						kickPos, null);
				sendCommandIfReady(ctx, placementCommand);
				return;
			}
		} else if (moveBallSlowlyToTarget)
		{
			moveBallSlowlyToTargetInSimulation(kickPos, frame.getWorldFrame().getBall());
		} else if (!simulationPlacementAttempted)
		{
			tryPlaceBallInSimulation(kickPos);
			simulationPlacementAttempted = true;
		}
		
		if (readyWaitTimeOver || ctx.doProceed())
		{
			RefboxRemoteCommand cmd = new RefboxRemoteCommand(action.getCommand(), null);
			if (ctx.doProceed())
			{
				sendCommand(ctx, cmd);
			} else
			{
				sendCommandIfReady(ctx, cmd);
			}
			
		}
	}
	
	
	private boolean placeBallByTeams(final IAutoRefFrame frame, final IAutoRefStateContext ctx,
			final ETeamColor placementTeam)
	{
		return placementTeam != ETeamColor.NEUTRAL
				&& autoBallPlacementAllowed(frame, ctx)
				&& !placementWasAttemptedBy(frame, placementTeam);
	}
	
	
	private boolean autoBallPlacementAllowed(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		return ctx.getFollowUpAction().getActionType() != FollowUpAction.EActionType.PENALTY
				&& ctx.getAutoRefGlobalState().getBallPlacementStage() != AutoRefGlobalState.EBallPlacementStage.CANCELED
				&& frame.getWorldFrame().getBall().isOnCam();
	}
	
	
	private void visualizeKickPos(final List<IDrawableShape> shapes, final IVector2 kickPos)
	{
		double radius = AutoRefConfig.getBallPlacementAccuracy();
		
		shapes.add(new DrawableCircle(kickPos, radius, PLACEMENT_CIRCLE_COLOR));
		shapes.add(new DrawableCircle(kickPos, RuleConstraints.getStopRadius(), PLACEMENT_CIRCLE_COLOR));
		shapes.add(new DrawableCircle(kickPos,
				RuleConstraints.getStopRadius() + RuleConstraints.getBotToPenaltyAreaMarginStandard(),
				PLACEMENT_CIRCLE_COLOR));
		shapes.add(new DrawablePoint(kickPos, Color.BLACK));
		
		IVector2 textPos = kickPos;
		DrawableAnnotation placementText = new DrawableAnnotation(textPos, "New Ball Pos", Color.BLACK);
		placementText.withFontHeight(100);
		placementText.withCenterHorizontally(true);
		placementText.withOffset(Vector2.fromY(-radius - 100));
		shapes.add(placementText);
	}
	
	
	private ETeamColor getPlacementTeam(final FollowUpAction followUpAction, final IAutoRefStateContext ctx)
	{
		ETeamColor kickExecutingTeam = followUpAction.getTeamInFavor();
		List<ETeamColor> capableTeams = AutoRefConfig.getBallPlacementTeams();
		capableTeams.removeIf(t -> ctx.getAutoRefGlobalState().getFailedBallPlacements().getOrDefault(t, 0) >= 5);
		if (capableTeams.isEmpty())
		{
			return ETeamColor.NEUTRAL;
		}
		
		if (kickExecutingTeam == ETeamColor.NEUTRAL)
		{
			if (capableTeams.size() == 2)
			{
				return rnd.nextInt(2) == 0 ? ETeamColor.BLUE : ETeamColor.YELLOW;
			}
			return capableTeams.get(0);
		}
		return ballPlacingTeam(followUpAction);
	}
	
	
	private Command placementCommand(ETeamColor teamColor)
	{
		return teamColor == ETeamColor.BLUE
				? Command.BALL_PLACEMENT_BLUE
				: Command.BALL_PLACEMENT_YELLOW;
	}
	
	
	private ETeamColor ballPlacingTeam(final FollowUpAction followUpAction)
	{
		ETeamColor teamInFavor = followUpAction.getTeamInFavor();
		List<ETeamColor> capableTeams = AutoRefConfig.getBallPlacementTeams();
		if (!capableTeams.contains(teamInFavor))
		{
			if (capableTeams.contains(teamInFavor.opposite()))
			{
				teamInFavor = teamInFavor.opposite();
			} else
			{
				teamInFavor = ETeamColor.NEUTRAL;
			}
		}
		return teamInFavor;
	}
	
	
	private IVector2 determineKickPos(final FollowUpAction action)
	{
		switch (action.getActionType())
		{
			case DIRECT_FREE:
			case INDIRECT_FREE:
			case FORCE_START:
				return validKickPos(action);
			case KICK_OFF:
				return NGeometry.getCenter();
			case PENALTY:
				return NGeometry.getPenaltyMark(action.getTeamInFavor().opposite());
			default:
				throw new IllegalArgumentException("Update the StopState to handle the new ActionType: "
						+ action.getActionType());
		}
	}
	
	
	private IVector2 validKickPos(final FollowUpAction action)
	{
		IVector2 kickPos = action.getNewBallPosition()
				.orElseThrow(() -> new IllegalArgumentException("Ball position not present"));
		if (action.getActionType() == FollowUpAction.EActionType.PENALTY)
		{
			return kickPos;
		}
		
		final double margin = RuleConstraints.getBotToPenaltyAreaMarginStandard()
				+ RuleConstraints.getStopRadius()
				+ Geometry.getBallRadius();
		
		if (action.getActionType() == FollowUpAction.EActionType.INDIRECT_FREE
				|| action.getActionType() == FollowUpAction.EActionType.DIRECT_FREE)
		{
			IPenaltyArea attackingTeamsPenArea = NGeometry.getPenaltyArea(action.getTeamInFavor());
			if (attackingTeamsPenArea.withMargin(200).isPointInShapeOrBehind(kickPos))
			{
				// from rules: If the free kick is awarded to a team inside or within 200 mm of its own defence area, the
				// free kick
				// is taken from a point 600 mm from the goal line and 100 mm from the touch line closest to where
				// the infringement occurred.
				double xSign = Math.signum(attackingTeamsPenArea.getGoalCenter().x());
				double ySign = kickPos.y() > 0 ? 1 : -1;
				return Vector2.fromXY(xSign * (Geometry.getFieldLength() / 2 - DEFENSE_AREA_GOAL_LINE_DISTANCE),
						ySign * (Geometry.getFieldWidth() / 2 - THROW_IN_DISTANCE));
			}
			
			IPenaltyArea opposingTeamsPenArea = NGeometry.getPenaltyArea(action.getTeamInFavor().opposite());
			// from rules: If the free kick is awarded to the attacking team within 700 mm of the opposing defence area,
			// the
			// ball is moved to the closest point 700 mm from the defence area.
			kickPos = opposingTeamsPenArea.withMargin(margin).nearestPointOutside(kickPos);
		} else if (action.getActionType() == FollowUpAction.EActionType.FORCE_START)
		{
			kickPos = NGeometry.getPenaltyArea(ETeamColor.YELLOW).withMargin(margin).nearestPointOutside(kickPos);
			kickPos = NGeometry.getPenaltyArea(ETeamColor.BLUE).withMargin(margin).nearestPointOutside(kickPos);
		}
		
		return NGeometry.getField().withMargin(-THROW_IN_DISTANCE).nearestPointInside(kickPos);
	}
	
	
	private List<ETeamColor> determineAttemptedPlacements(final IAutoRefFrame frame)
	{
		List<ETeamColor> teams = new ArrayList<>();
		
		List<GameState> stateHist = frame.getStateHistory();
		for (int i = 1; i < stateHist.size(); i++)
		{
			GameState state = stateHist.get(i);
			if (state.getState() == EGameState.BALL_PLACEMENT)
			{
				teams.add(state.getForTeam());
			} else if (state.getState() != EGameState.STOP)
			{
				break;
			}
		}
		
		return teams;
	}
	
	
	private boolean placementWasAttemptedBy(final IAutoRefFrame frame, final ETeamColor teamColor)
	{
		return determineAttemptedPlacements(frame).contains(teamColor);
	}
	
	
	private void tryPlaceBallInSimulation(final IVector2 pos)
	{
		if (SumatraModel.getInstance().isModuleLoaded(AVisionFilter.class))
		{
			AVisionFilter vf = SumatraModel.getInstance().getModule(AVisionFilter.class);
			vf.placeBall(Vector3f.from2d(pos, 0), Vector3f.ZERO_VECTOR);
		}
	}
	
	
	private void moveBallSlowlyToTargetInSimulation(IVector2 target, ITrackedBall ball)
	{
		BangBangTrajectory2D traj = new BangBangTrajectory2D(ball.getPos().multiplyNew(1e-3), target.multiplyNew(1e-3),
				ball.getVel(), 2.0, 3.0);
		IVector2 nextPos = traj.getPositionMM(0.1);
		tryPlaceBallInSimulation(nextPos);
	}
}
