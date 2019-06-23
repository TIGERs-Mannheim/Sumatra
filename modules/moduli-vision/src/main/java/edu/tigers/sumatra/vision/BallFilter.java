/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.vision.BallFilterPreprocessor.BallFilterPreprocessorOutput;
import edu.tigers.sumatra.vision.data.EBallState;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;


/**
 * @author AndreR
 */
public class BallFilter
{
	private EBallState ballState = EBallState.ROLLING;
	private IVector2 ballPosHint;
	private IVector3 lastKnownPosition = AVector3.ZERO_VECTOR;
	private CircularFifoQueue<MergedBall> mergedBallHistory = new CircularFifoQueue<>(50);
	
	@Configurable(defValue = "true", comment = "Always use merged ball velocity instead of kick model velocity.")
	private static boolean alwaysUseMergedBallVelocity = true;
	
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
			FilteredVisionBall ball = FilteredVisionBall.Builder.create()
					.withPos(Vector3.from2d(ballPosHint, 0))
					.withVel(AVector3.ZERO_VECTOR)
					.withAcc(AVector3.ZERO_VECTOR)
					.withIsChipped(false)
					.withvSwitch(0)
					.withLastVisibleTimestamp(timestamp)
					.build();
			
			ballPosHint = null;
			
			return new BallFilterOutput(ball, ball.getPos(), EBallState.ROLLING, preInput);
		}
		
		// returned last output if there is no valid ball at all
		if (!preInput.getMergedBall().isPresent())
		{
			FilteredVisionBall ball = FilteredVisionBall.Builder.create()
					.withPos(lastFilteredBall.getPos())
					.withVel(AVector3.ZERO_VECTOR)
					.withAcc(AVector3.ZERO_VECTOR)
					.withIsChipped(false)
					.withvSwitch(0)
					.withLastVisibleTimestamp(lastFilteredBall.getLastVisibleTimestamp())
					.build();
			
			return new BallFilterOutput(ball, lastFilteredBall.getPos(), ballState, preInput);
		}
		
		MergedBall mergedBall = preInput.getMergedBall().get();
		long lastVisibleTimestamp = 0;
		
		if (mergedBall.getLatestCamBall().isPresent())
		{
			lastKnownPosition = mergedBall.getLatestCamBall().get().getPos();
			lastVisibleTimestamp = mergedBall.getLatestCamBall().get().gettCapture();
			mergedBallHistory.add(mergedBall);
		}
		
		Optional<FilteredVisionBall> optKickFitState = preInput.getKickFitState();
		
		IVector3 pos;
		IVector3 vel;
		IVector3 acc;
		double vSwitch;
		
		if (optKickFitState.isPresent())
		{
			FilteredVisionBall kickFitState = optKickFitState.get();
			acc = kickFitState.getAcc();
			vel = kickFitState.getVel();
			vSwitch = kickFitState.getVSwitch();
			
			if (kickFitState.isChipped())
			{
				ballState = EBallState.AIRBORNE;
				pos = kickFitState.getPos();
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
			pos = Vector3.from2d(mergedBall.getCamPos(), 0);
			vel = Vector3.from2d(mergedBall.getFiltVel(), 0);
			acc = Vector3.from2d(mergedBall.getFiltVel().scaleToNew(-Geometry.getBallParameters().getAccRoll()), 0);
			vSwitch = vel.getLength2();
		}
		
		FilteredVisionBall filteredBall = FilteredVisionBall.Builder.create()
				.withPos(pos)
				.withVel(vel)
				.withAcc(acc)
				.withIsChipped(ballState == EBallState.AIRBORNE)
				.withvSwitch(vSwitch)
				.withLastVisibleTimestamp(lastVisibleTimestamp)
				.build();
		
		return new BallFilterOutput(filteredBall, lastKnownPosition, ballState, preInput);
	}
	
	
	/**
	 * @return the ballState
	 */
	public EBallState getBallState()
	{
		return ballState;
	}
	
	
	/**
	 * @param ballState the ballState to set
	 */
	public void setBallState(final EBallState ballState)
	{
		this.ballState = ballState;
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
		state.setOffset(Vector2.fromXY(0, -100));
		state.setCenterHorizontally(true);
		state.setStrokeWidth(30);
		shapes.add(state);
		
		return shapes;
	}
	
	/**
	 * Output of final ball filter.
	 * 
	 * @author AndreR <andre@ryll.cc>
	 */
	public static class BallFilterOutput
	{
		private final FilteredVisionBall filteredBall;
		private final IVector3 lastKnownPosition;
		private final EBallState ballState;
		private final BallFilterPreprocessorOutput preprocessorOutput;
		
		
		/**
		 * @param filteredBall
		 * @param lastKnownPosition
		 * @param ballState
		 * @param preprocessorOutput
		 */
		public BallFilterOutput(final FilteredVisionBall filteredBall, final IVector3 lastKnownPosition,
				final EBallState ballState,
				final BallFilterPreprocessorOutput preprocessorOutput)
		{
			this.filteredBall = filteredBall;
			this.lastKnownPosition = lastKnownPosition;
			this.ballState = ballState;
			this.preprocessorOutput = preprocessorOutput;
		}
		
		
		public FilteredVisionBall getFilteredBall()
		{
			return filteredBall;
		}
		
		
		public IVector3 getLastKnownPosition()
		{
			return lastKnownPosition;
		}
		
		
		public EBallState getBallState()
		{
			return ballState;
		}
		
		
		public BallFilterPreprocessorOutput getPreprocessorOutput()
		{
			return preprocessorOutput;
		}
	}
}
