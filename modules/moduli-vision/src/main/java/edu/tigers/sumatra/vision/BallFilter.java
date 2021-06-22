/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.vision.BallFilterPreprocessor.BallFilterPreprocessorOutput;
import edu.tigers.sumatra.vision.data.EBallState;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionKick;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.kick.estimators.KickFitResult;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;
import lombok.Value;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * @author AndreR
 */
public class BallFilter
{
	private EBallState ballState = EBallState.ROLLING;
	private IVector2 ballPosHint;
	private IVector3 lastKnownPosition = Vector3f.ZERO_VECTOR;
	private CircularFifoQueue<MergedBall> mergedBallHistory = new CircularFifoQueue<>(50);
	private KickEvent lastKickEvent;

	@Configurable(defValue = "false", comment = "Always use merged ball velocity instead of kick model velocity.")
	private static boolean alwaysUseMergedBallVelocity = false;

	static
	{
		ConfigRegistration.registerClass("vision", BallFilter.class);
	}


	/**
	 * Insert ball position hint.
	 *
	 * @param pos
	 */
	public void resetBall(final IVector2 pos)
	{
		ballPosHint = pos;
	}


	/**
	 * Update the ball filter with output from the preprocessor.
	 *
	 * @param preInput
	 * @param lastFilteredBall
	 * @param timestamp
	 * @return
	 */
	public BallFilterOutput update(final BallFilterPreprocessorOutput preInput,
			final FilteredVisionBall lastFilteredBall, final long timestamp)
	{
		// process ball position hint if there is one
		if (ballPosHint != null)
		{
			FilteredVisionBall ball = FilteredVisionBall.builder()
					.withTimestamp(timestamp)
					.withBallState(BallState.builder()
							.withPos(ballPosHint.getXYZVector())
							.withVel(Vector3f.ZERO_VECTOR)
							.withAcc(Vector3f.ZERO_VECTOR)
							.withSpin(Vector2f.ZERO_VECTOR)
							.build())
					.withLastVisibleTimestamp(timestamp)
					.build();

			ballPosHint = null;
			lastKickEvent = null;

			return new BallFilterOutput(ball, null, ball.getPos(), preInput);
		}

		// returned last output if there is no valid ball at all
		if (preInput.getMergedBall().isEmpty())
		{
			FilteredVisionBall ball = FilteredVisionBall.builder()
					.withTimestamp(timestamp)
					.withBallState(BallState.builder()
							.withPos(lastFilteredBall.getPos())
							.withVel(Vector3f.ZERO_VECTOR)
							.withAcc(Vector3f.ZERO_VECTOR)
							.withSpin(Vector2f.ZERO_VECTOR)
							.build())
					.withLastVisibleTimestamp(lastFilteredBall.getLastVisibleTimestamp())
					.build();

			lastKickEvent = null;

			return new BallFilterOutput(ball, null, lastFilteredBall.getPos(), preInput);
		}

		MergedBall mergedBall = preInput.getMergedBall().get();
		long lastVisibleTimestamp = lastFilteredBall.getLastVisibleTimestamp();

		if (mergedBall.getLatestCamBall().isPresent())
		{
			lastKnownPosition = mergedBall.getLatestCamBall().get().getPos();
			lastVisibleTimestamp = mergedBall.getLatestCamBall().get().gettCapture();
			mergedBallHistory.add(mergedBall);
		}

		Optional<KickFitResult> optBestKickFitResult = preInput.getBestKickFitResult();
		lastKickEvent = preInput.getKickEvent().orElse(lastKickEvent);

		IVector3 pos;
		IVector3 vel;
		IVector3 acc;
		IVector2 spin;

		if (optBestKickFitResult.isPresent())
		{
			KickFitResult kickFitResult = optBestKickFitResult.get();
			BallState stateNow = kickFitResult.getState(timestamp);
			vel = stateNow.getVel();
			acc = stateNow.getAcc();
			spin = stateNow.getSpin();

			if (stateNow.isChipped())
			{
				ballState = EBallState.AIRBORNE;
				pos = stateNow.getPos();
				lastKnownPosition = pos;
			} else
			{
				ballState = EBallState.KICKED;
				pos = Vector3.from2d(mergedBall.getCamPos(), 0);
			}

			if (alwaysUseMergedBallVelocity)
			{
				vel = Vector3.from2d(vel.getXYVector().scaleToNew(mergedBall.getFiltVel().getLength2()), vel.z());
			}
		} else
		{
			ballState = EBallState.ROLLING;
			lastKickEvent = null;
			pos = Vector3.from2d(mergedBall.getCamPos(), 0);
			vel = Vector3.from2d(mergedBall.getFiltVel(), 0);
			acc = Vector3.from2d(mergedBall.getFiltVel().scaleToNew(-Geometry.getBallParameters().getAccRoll()), 0);
			spin = mergedBall.getFiltVel().multiplyNew(1.0 / Geometry.getBallRadius());
		}

		BallState combinedBallState = BallState.builder()
				.withPos(pos)
				.withVel(vel)
				.withAcc(acc)
				.withSpin(spin)
				.build();

		FilteredVisionKick filteredKick = null;

		if (optBestKickFitResult.isPresent() && lastKickEvent != null)
		{
			KickFitResult kickFitResult = optBestKickFitResult.get();

			filteredKick = FilteredVisionKick.builder()
					.withKickTimestamp(lastKickEvent.getTimestamp())
					.withTrajectoryStartTime(kickFitResult.getKickTimestamp())
					.withKickingBot(lastKickEvent.getKickingBot())
					.withKickingBotPosition(lastKickEvent.getKickingBotPosition())
					.withKickingBotOrientation(lastKickEvent.getBotDirection())
					.withNumBallDetectionsSinceKick(kickFitResult.getGroundProjection().size())
					.withBallTrajectory(kickFitResult.getTrajectory())
					.build();
		}

		FilteredVisionBall filteredBall = FilteredVisionBall.builder()
				.withTimestamp(timestamp)
				.withBallState(combinedBallState)
				.withLastVisibleTimestamp(lastVisibleTimestamp)
				.build();

		return new BallFilterOutput(filteredBall, filteredKick, lastKnownPosition, preInput);
	}


	public List<IDrawableShape> getShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		for (MergedBall b : mergedBallHistory)
		{
			DrawableCircle ballPos = new DrawableCircle(b.getCamPos(), 10, Color.GREEN);
			ballPos.setStrokeWidth(3);
			shapes.add(ballPos);
		}

		DrawableAnnotation state = new DrawableAnnotation(lastKnownPosition.getXYVector(), ballState.toString());
		state.withOffset(Vector2.fromXY(0, -100));
		state.withCenterHorizontally(true);
		state.setStrokeWidth(30);
		shapes.add(state);

		return shapes;
	}


	/**
	 * Output of final ball filter.
	 */
	@Value
	public static class BallFilterOutput
	{
		FilteredVisionBall filteredBall;
		FilteredVisionKick filteredKick;
		IVector3 lastKnownPosition;
		BallFilterPreprocessorOutput preprocessorOutput;
	}
}
