/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.states.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.AVector3;
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
	private static final Logger log = Logger.getLogger(StopState.class);
	
	private static final Color PLACEMENT_CIRCLE_COLOR = Color.BLUE;
	
	@Configurable(comment = "[ms] Time to wait before performing an action after reaching the stop state")
	private static long stopWaitTimeMs = 2_000; // ms
	
	@Configurable(comment = "[ms] The time to wait after all bots have come to a stop and the ball has been placed correctly")
	private static long readyWaitTimeMs = 3_000;
	
	@Configurable()
	private static boolean moveBallSlowlyToTarget = true;
	
	static
	{
		AbstractAutoRefState.registerClass(StopState.class);
	}
	
	private Long readyTime;
	private boolean simulationPlacementAttempted = false;
	
	
	/**
	 * Creates a new StopState
	 */
	public StopState()
	{
		// Nothing to do
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		readyTime = null;
		simulationPlacementAttempted = false;
	}
	
	
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	@Override
	public void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		if (ctx.getFollowUpAction() == null)
		{
			setCanProceed(false);
			return;
		}
		
		FollowUpAction action = ctx.getFollowUpAction();
		
		ITrackedBall ball = frame.getWorldFrame().getBall();
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ENGINE);
		
		IVector2 kickPos = determineKickPos(action);
		double penAreaMargin = Geometry.getBotToPenaltyAreaMarginStandard() + Geometry.getBotToBallDistanceStop();
		kickPos = NGeometry.getPenaltyArea(ETeamColor.YELLOW).withMargin(penAreaMargin).nearestPointOutside(kickPos);
		kickPos = NGeometry.getPenaltyArea(ETeamColor.BLUE).withMargin(penAreaMargin).nearestPointOutside(kickPos);
		kickPos = NGeometry.getField().withMargin(-AutoRefMath.THROW_IN_DISTANCE).nearestPointInside(kickPos);
		
		visualizeKickPos(shapes, kickPos);
		
		/*
		 * Wait a minimum amount of time before doing anything
		 */
		if (!timeElapsedSinceEntry(stopWaitTimeMs))
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
		
		if (!placementWasAttempted(frame) && (!AutoRefConfig.getBallPlacementTeams().isEmpty()) && ball.isOnCam())
		{
			if (ballStationary && !ballPlaced)
			{
				// Try to place the ball
				sendCommandIfReady(ctx, getPlacementCommand(kickPos, action.getTeamInFavor()));
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
			RefboxRemoteCommand cmd = new RefboxRemoteCommand(action.getCommand());
			if (ctx.doProceed())
			{
				sendCommand(ctx, cmd);
			} else
			{
				sendCommandIfReady(ctx, cmd);
			}
			
		}
	}
	
	
	private void visualizeKickPos(final List<IDrawableShape> shapes, final IVector2 kickPos)
	{
		double radius = AutoRefConfig.getBallPlacementAccuracy();
		
		shapes.add(new DrawableCircle(kickPos, radius, PLACEMENT_CIRCLE_COLOR));
		shapes.add(new DrawableCircle(kickPos, Geometry.getBotToBallDistanceStop(), PLACEMENT_CIRCLE_COLOR));
		shapes.add(new DrawablePoint(kickPos, Color.BLACK));
		
		IVector2 textPos = kickPos;
		DrawableAnnotation placementText = new DrawableAnnotation(textPos, "New Ball Pos", Color.BLACK);
		placementText.setFontHeight(100);
		placementText.setCenterHorizontally(true);
		placementText.setOffset(Vector2.fromY(-radius - 100));
		shapes.add(placementText);
	}
	
	
	/**
	 * @return
	 */
	private RefboxRemoteCommand getPlacementCommand(final IVector2 kickPos, final ETeamColor kickExecutingTeam)
	{
		List<ETeamColor> capableTeams = AutoRefConfig.getBallPlacementTeams();
		if (capableTeams.isEmpty())
		{
			return null;
		}
		
		/*
		 * At this point we know that the size of the list of capable teams is greater than zero. We pick the first entry
		 * in the list by default and perform further checks if the size is greater than 1 which means both teams are
		 * capable of placing the ball.
		 */
		ETeamColor placingTeam = capableTeams.get(0);
		
		ETeamColor preference = AutoRefConfig.getBallPlacementPreference();
		if (capableTeams.size() > 1)
		{
			/*
			 * At this point both teams are capable of placing the ball which means that we need to pick one of them. The
			 * preference setting takes precedence but is only considered if it is initialized. If it is not initialized we
			 * try to let the team which will later on execute the kick place the ball. If this fails too, we simply fall
			 * back to the first team in the list which has already been set before.
			 */
			if (preference.isNonNeutral())
			{
				placingTeam = preference;
			} else
			{
				if (kickExecutingTeam.isNonNeutral())
				{
					placingTeam = kickExecutingTeam;
				}
			}
		}
		
		Command cmd = placingTeam == ETeamColor.BLUE ? Command.BALL_PLACEMENT_BLUE : Command.BALL_PLACEMENT_YELLOW;
		return new RefboxRemoteCommand(cmd, kickPos);
	}
	
	
	private IVector2 determineKickPos(final FollowUpAction action)
	{
		switch (action.getActionType())
		{
			case DIRECT_FREE:
			case INDIRECT_FREE:
			case FORCE_START:
				return action.getNewBallPosition()
						.orElseThrow(() -> new IllegalArgumentException("Ball position not present"));
			case KICK_OFF:
				return NGeometry.getCenter();
			case PENALTY:
				return NGeometry.getPenaltyMark(action.getTeamInFavor().opposite());
			default:
				throw new IllegalArgumentException("Update the StopState to handle the new ActionType: "
						+ action.getActionType());
		}
	}
	
	
	private List<ETeamColor> determineAttemptedPlacements(final IAutoRefFrame frame)
	{
		List<ETeamColor> teams = new ArrayList<>();
		
		// Only search for attempts which were performed directly before this stop state
		List<GameState> stateHist = frame.getStateHistory();
		for (int i = 1; i < stateHist.size(); i++)
		{
			GameState state = stateHist.get(i);
			if (state.getState() == EGameState.BALL_PLACEMENT)
			{
				teams.add(state.getForTeam());
			} else
			{
				break;
			}
		}
		
		return teams;
	}
	
	
	private boolean placementWasAttempted(final IAutoRefFrame frame)
	{
		return !determineAttemptedPlacements(frame).isEmpty();
	}
	
	
	private void tryPlaceBallInSimulation(final IVector2 pos)
	{
		try
		{
			AVisionFilter vf = (AVisionFilter) SumatraModel.getInstance().getModule(AVisionFilter.MODULE_ID);
			vf.resetBall(Vector3f.from2d(pos, 0), AVector3.ZERO_VECTOR);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find vision filter module.", e);
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
