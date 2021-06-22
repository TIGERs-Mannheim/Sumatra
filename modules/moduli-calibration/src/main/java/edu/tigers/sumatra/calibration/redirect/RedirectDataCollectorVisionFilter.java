/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration.redirect;

import edu.tigers.sumatra.calibration.CalibrationDataSample;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Log4j2
public class RedirectDataCollectorVisionFilter implements IRedirectDataCollector
{
	private static final int MIN_SAMPLES_PER_BUFFER = 30;
	private static final double MIN_DISTANCE_BETWEEN_KICK_POS = 100;
	private static final double MIN_PASS_DISTANCE = 1000;

	private final KickDetector kickDetector = new KickDetector();
	private List<CalibrationDataSample> bufferPre = new ArrayList<>();
	private List<CalibrationDataSample> bufferPost = new ArrayList<>();

	@Getter
	private long numPotentialSamples = 0;
	@Getter
	private long numValidSamples = 0;


	@Override
	public void stop()
	{
		bufferPre.clear();
		bufferPost.clear();
	}


	@Override
	public Optional<RedirectSample> process(CalibrationDataSample sample)
	{
		RedirectSample redirectSample = null;
		var kickState = kickDetector.update(sample.getTimestamp(), sample.getKickPos());

		if (kickState == EKickState.KICK_ENDED || kickState == EKickState.KICK_CHANGED)
		{
			if (!bufferPre.isEmpty())
			{
				redirectSample = createRedirectSample();
				if (redirectSample != null)
				{
					log.debug("Redirect {}/{} detected: {}", numValidSamples, numPotentialSamples, redirectSample);
					numValidSamples++;
				}
				numPotentialSamples++;
			}
			bufferPre = bufferPost;
			bufferPost = new ArrayList<>();
		} else if (kickState == EKickState.KICK_STARTED)
		{
			bufferPre = new ArrayList<>();
			bufferPost = new ArrayList<>();
		}

		bufferPost.add(sample);

		return Optional.ofNullable(redirectSample);
	}


	private RedirectSample createRedirectSample()
	{
		if (bufferPre.size() < MIN_SAMPLES_PER_BUFFER || bufferPost.size() < MIN_SAMPLES_PER_BUFFER)
		{
			log.debug("Invalid buffer sizes: pre={}, post={}", bufferPre.size(), bufferPost.size());
			return null;
		}

		var preFirstSample = bufferPre.get(0);
		var preLastSample = bufferPre.get(bufferPre.size() - 1);
		var postFirstSample = bufferPost.get(0);
		var postLastSample = bufferPost.get(bufferPost.size() - 1);

		var preLine = Lines.halfLineFromPoints(preFirstSample.getBallPos(), preLastSample.getBallPos());
		var postLine = Lines.halfLineFromPoints(postFirstSample.getBallPos(), postLastSample.getBallPos());

		var firstPos = preFirstSample.getBallPos();
		var lastPos = postLastSample.getBallPos();
		var impactPos = preLine.intersectHalfLine(postLine).orElse(preLastSample.getBallPos());
		var kickSpeed = findKickSpeed();

		var passDistPre = firstPos.distanceTo(impactPos);
		var passDistPost = lastPos.distanceTo(impactPos);
		if (passDistPre < MIN_PASS_DISTANCE || passDistPost < MIN_PASS_DISTANCE)
		{
			log.debug("Invalid pass distances: pre={}, post={}", passDistPre, passDistPost);
			return null;
		}

		var linePre = Lines.segmentFromPoints(firstPos, impactPos);
		var linePost = Lines.segmentFromPoints(impactPos, lastPos);

		var dirIn = linePre.directionVector().getAngle();
		var dirOut = linePost.directionVector().getAngle();
		var angle = AngleMath.diffAbs(dirIn + AngleMath.DEG_180_IN_RAD, dirOut);

		var orientation = preLastSample.getBotOrientation();
		var angleIn = AngleMath.diffAbs(dirIn + AngleMath.DEG_180_IN_RAD, orientation);
		var angleOut = AngleMath.diffAbs(dirOut, orientation);
		var angleDiff = AngleMath.difference(angleOut, angleIn);
		var angleDiffFactor = angleOut / angleIn;

		var speedIn = findBallSpeed();
		var speedOut = findSpeedOut();

		double[] diffsPre = bufferPre.stream()
				.map(CalibrationDataSample::getBallPos)
				.mapToDouble(linePre::distanceTo)
				.toArray();

		var meanDiffPre = new Mean().evaluate(diffsPre);
		var stdDevDiffPre = new StandardDeviation().evaluate(diffsPre);

		double[] diffsPost = bufferPost.stream()
				.map(CalibrationDataSample::getBallPos)
				.mapToDouble(linePost::distanceTo)
				.toArray();

		var meanDiffPost = new Mean().evaluate(diffsPost);
		var stdDevDiffPost = new StandardDeviation().evaluate(diffsPost);

		return RedirectSample.builder()
				.samplesPre(new ArrayList<>(bufferPre))
				.samplesPost(new ArrayList<>(bufferPost))
				.dirIn(dirIn)
				.dirOut(dirOut)
				.angle(angle)
				.angleIn(angleIn)
				.angleOut(angleOut)
				.angleDiff(angleDiff)
				.angleDiffFactor(angleDiffFactor)
				.kickSpeed(kickSpeed)
				.speedIn(speedIn)
				.speedOut(speedOut)
				.meanDiffPre(meanDiffPre)
				.meanDiffPost(meanDiffPost)
				.stdDevDiffPre(stdDevDiffPre)
				.stdDevDiffPost(stdDevDiffPost)
				.build();
	}


	private double findKickSpeed()
	{
		for (int i = 0; i < 10; i++)
		{
			var redirectDataCollectorSample = bufferPre.get(bufferPre.size() - 1 - i);
			if (redirectDataCollectorSample.getKickSpeed() > 0)
			{
				return redirectDataCollectorSample.getKickSpeed();
			}
		}
		return 0.0;
	}


	private double findBallSpeed()
	{
		for (int i = 0; i < 50; i++)
		{
			var redirectDataCollectorSample = bufferPre.get(bufferPre.size() - 1 - i);
			if (redirectDataCollectorSample.getKickPos() != null)
			{
				// assuming that while there is still a kick pos, the ball also still has its original ball velocity
				return redirectDataCollectorSample.getBallSpeed();
			}
		}
		return 0.0;
	}


	private double findSpeedOut()
	{
		for (int i = 0; i < 50; i++)
		{
			var redirectDataCollectorSample = bufferPost.get(bufferPost.size() - 1 - i);
			if (redirectDataCollectorSample.getKickVel() != null)
			{
				return redirectDataCollectorSample.getKickVel().getLength();
			}
		}
		return 0.0;
	}


	private static class KickDetector
	{
		long lastKickPosTimestamp;
		IVector2 currentKickPos;


		EKickState update(long timestamp, IVector2 kickPos)
		{
			var lastKickPos = currentKickPos;
			if (kickPos == null)
			{
				if (lastKickPos == null)
				{
					return EKickState.NO_KICK;
				}
				var dt = (timestamp - lastKickPosTimestamp) / 1e9;
				if (dt > 0.2)
				{
					currentKickPos = null;
					lastKickPosTimestamp = 0;
					log.trace("Kick ended at {}", lastKickPos);
					return EKickState.KICK_ENDED;
				}
				return EKickState.IN_PROGRESS;
			}
			currentKickPos = kickPos;
			lastKickPosTimestamp = timestamp;
			if (lastKickPos == null)
			{
				log.trace("Kick started at {}", kickPos);
				return EKickState.KICK_STARTED;
			}
			if (kickPos.distanceTo(lastKickPos) > MIN_DISTANCE_BETWEEN_KICK_POS)
			{
				log.trace("Kick changed from {} to {}", lastKickPos, kickPos);
				return EKickState.KICK_CHANGED;
			}

			return EKickState.IN_PROGRESS;
		}
	}

	private enum EKickState
	{
		NO_KICK,
		IN_PROGRESS,
		KICK_STARTED,
		KICK_ENDED,
		KICK_CHANGED,
	}
}
