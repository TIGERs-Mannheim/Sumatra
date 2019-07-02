/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.microtypes;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.loganalysis.ELogAnalysisShapesLayer;
import edu.tigers.sumatra.loganalysis.GameMemory;
import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static edu.tigers.sumatra.loganalysis.GameMemory.GameLogObject.BALL_LINE_DETECTION;


public class LineDetection implements IMicroTypeDetection
{
	private LineDetectionState detectionState = LineDetectionState.INIT;
	private int countDetectionFrames = 0;
	private IVector2 lineStart;

	private static double equalBallTolerance = 5d;
	private double minPassDirDetectionLength = 50d;
	private double minPassLengthTotalMismatchDetection = 120d;
	
	private double maxDiffPointToPassLine = 30d;
	private double maxAnglePassLineAbsolute = 8 * Math.PI / 180;
	private IVector2 firstPassDir = null;

	private List<IDrawableShape> drawings = new ArrayList<>();
	
	
	@Override
	public void nextFrameForDetection(final TypeDetectionFrame frame)
	{
		GameMemory gameMemory = frame.getMemory();
		ITrackedBall ball = frame.getWorldFrameWrapper().getSimpleWorldFrame().getBall();

		if (!gameMemory.get(BALL_LINE_DETECTION).isEmpty() &&
				ball.getPos().distanceTo(gameMemory.get(BALL_LINE_DETECTION, 0).getPos()) < equalBallTolerance)
		{
			// skip frame

			frame.getShapeMap().get(ELogAnalysisShapesLayer.PASSING).addAll(drawings);

			return;
		}

		gameMemory.update(BALL_LINE_DETECTION, frame.getWorldFrameWrapper().getSimpleWorldFrame().getBall());

		if (detectionState != LineDetectionState.LINE && detectionState != LineDetectionState.MISMATCH_ON_LINE)
			return;

		if (countDetectionFrames < 3 || lineStart.distanceTo(ball.getPos()) < minPassDirDetectionLength)
		{
			if(countDetectionFrames == 0)
			{
				lineStart = ball.getPos();
			}
		}
		else if (firstPassDir == null)
		{
				firstPassDir = Vector2.fromPoints(lineStart, ball.getPos());
		}
		else
		{
			IVector2 passDirFromStart = Vector2.fromPoints(lineStart, gameMemory.get(BALL_LINE_DETECTION, 1).getPos()); // new pass line
			IVector2 passDirFromLastPt = Vector2.fromPoints(gameMemory.get(BALL_LINE_DETECTION, 1).getPos(), ball.getPos());

			drawings.clear();

			drawings.add(new DrawableArrow(lineStart, firstPassDir, Color.RED));	//firstPassDir

			drawings.add(new DrawableArrow(lineStart, passDirFromStart, Color.CYAN)); //passDirFromStart
			drawings.add(new DrawableArrow(gameMemory.get(BALL_LINE_DETECTION, 1).getPos(),
					passDirFromLastPt, Color.BLUE)); //passDirFromLastPt

			Optional<Double> optTotalAngleDiff = passDirFromStart.angleToAbs(firstPassDir);
			Optional<Double> optRelativeAngleDiff;

			if (detectionState != LineDetectionState.MISMATCH_ON_LINE)
			{
				optRelativeAngleDiff = passDirFromStart.angleToAbs(passDirFromLastPt);
			}
			else
			{
				// if last frame was a mismatch the angle to penultimate passDir is used
				IVector2 passDirFromPenultimatePt = Vector2.fromPoints(gameMemory.get(BALL_LINE_DETECTION, 2).getPos(), gameMemory.get(BALL_LINE_DETECTION, 1).getPos());
				optRelativeAngleDiff = passDirFromStart.angleToAbs(passDirFromPenultimatePt);

				drawings.add(new DrawableArrow(gameMemory.get(BALL_LINE_DETECTION, 2).getPos(),
						passDirFromPenultimatePt, Color.BLACK)); //passDirFromLastPt
			}


			checkLineMismatchOnAngleDiff(passDirFromLastPt,
					optTotalAngleDiff.orElse(null), optRelativeAngleDiff.orElse(null),
					ball.getPos());

			checkBallInField(ball);

		}


		frame.getShapeMap().get(ELogAnalysisShapesLayer.PASSING).addAll(drawings);
		countDetectionFrames++;
		
	}
	
	
	private void checkLineMismatchOnAngleDiff(final IVector2 passDirFromLastPt,
											  final Double totalAngleDiff, final Double relativeAngleDiff,
											  final IVector2 ballPos)
	{
		if ( totalAngleDiff != null && lineStart.distanceTo(ballPos) > minPassLengthTotalMismatchDetection &&
				totalAngleDiff > maxAnglePassLineAbsolute)
		    // absolute difference
		{
			// total pass direction not in pass line
			detectionState = LineDetectionState.LINE_TOTAL_MISMATCH;
		}

		double relativeAngleDiffFromLine = Math.atan(maxDiffPointToPassLine / passDirFromLastPt.getLength());
		if (relativeAngleDiff != null &&
				relativeAngleDiff > relativeAngleDiffFromLine)
			// relative difference
		{
			onRelativeDifference(passDirFromLastPt, relativeAngleDiff);
		}
		else
		{
			// if there is only one Mismatch where the ball is still on the line
			// it is classified as vision problem and is ignored
			if (detectionState == LineDetectionState.MISMATCH_ON_LINE)
				detectionState = LineDetectionState.LINE;
		}

        if (relativeAngleDiff != null && totalAngleDiff != null)
        {
			drawings.add(new DrawableAnnotation(ballPos, "abs: " + String.format("%1$,.2f", totalAngleDiff / maxAnglePassLineAbsolute)
					+ " rel: " + String.format("%1$,.2f", relativeAngleDiff / relativeAngleDiffFromLine) + "\n"
					+ " count frames: " + countDetectionFrames, Color.BLACK));

        }

	}
	
	
	private void onRelativeDifference(final IVector2 passDirFromLastPt, final Double relativeAngleDiff)
	{
		if (Math.abs(relativeAngleDiff - Math.PI) < Math.atan(maxDiffPointToPassLine / passDirFromLastPt.getLength()))
		{
			// MISMATCH but ball is on line (= angle around 180°) -> can be a vision problem
			// but also a relative mismatch
			
			// two Mismatches on line one after another is classified as mismatch
			if (detectionState == LineDetectionState.MISMATCH_ON_LINE)
			{
				detectionState = LineDetectionState.LINE_RELATIVE_MISMATCH;
			} else
			{
				detectionState = LineDetectionState.MISMATCH_ON_LINE;
			}
		} else
		{
			// relative pass direction not in pass line
			detectionState = LineDetectionState.LINE_RELATIVE_MISMATCH;
		}
	}
	
	
	private void checkBallInField(ITrackedBall ball)
	{
		if (!Geometry.getField().isPointInShape(ball.getPos()))
		{
			// ball out of field
			detectionState = LineDetectionState.BALL_OUT_OF_FIELD;
		}
	}
	
	
	public void resetAndStart()
	{
		detectionState = LineDetectionState.LINE;
		countDetectionFrames = 0;
		firstPassDir = null;
	}
	
	
	public void ignoreMismatchAndContinueTracking()
	{
		detectionState = LineDetectionState.LINE;
	}
	
	
	public LineDetectionState getDetectionState()
	{
		return detectionState;
	}
	
	public enum LineDetectionState
	{
		LINE,
		LINE_TOTAL_MISMATCH,
		LINE_RELATIVE_MISMATCH,
		MISMATCH_ON_LINE,
		BALL_OUT_OF_FIELD,
		INIT
	}
}
