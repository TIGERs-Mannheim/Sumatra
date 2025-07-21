/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.detectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.KickEvent;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Early kick detector based on velocity jumps on individual cameras.
 */
public class EarlyKickDetector implements IKickDetector
{
	private static final Logger log = LogManager.getLogger(EarlyKickDetector.class.getName());

	private Map<Integer, LinkedList<MergedBall>> camBallMap = new HashedMap<>();

	@Configurable(comment = "Ball velocity threshold [mm/s]", defValue = "1000.0")
	private static double velocityThreshold = 1000.0;

	@Configurable(comment = "A bot must be within this radius [mm]", defValue = "150.0")
	private static double nearBotLimit = 150.0;

	@Configurable(comment = "Fast ball direction change threshold [deg]", defValue = "20.0")
	private static double directionThreshold = 20.0;

	@Configurable(comment = "Ball velocity threshold for direction change [mm/s]", defValue = "2000.0")
	private static double velocityThresholdDirection = 2000.0;

	static
	{
		ConfigRegistration.registerClass("vision", EarlyKickDetector.class);
	}


	@Override
	public KickEvent addRecord(final MergedBall ball, final List<FilteredVisionBot> mergedRobots)
	{
		Optional<CamBall> camBall = ball.getLatestCamBall();
		if (!camBall.isPresent())
		{
			return null;
		}

		boolean isVirtualBall = camBall.get().getConfidence() <= 0.0;
		if (isVirtualBall)
		{
			return null;
		}

		int camId = camBall.get().getCameraId();
		camBallMap.putIfAbsent(camId, new LinkedList<>());
		LinkedList<MergedBall> camBallList = camBallMap.get(camId);

		if (!camBallList.isEmpty())
		{
			// clear camera buffer if ball was not visible for some time
			double timeSinceLastFrame = (ball.getTimestamp() - camBallList.getLast().getTimestamp()) * 1e-9;
			if (timeSinceLastFrame > 0.1)
			{
				camBallList.clear();
			}
		}

		camBallList.add(ball);
		if (camBallList.size() > 3)
		{
			camBallList.remove();
		}

		if (camBallList.size() < 3)
		{
			return null;
		}

		if (!detectKick(camBallList, mergedRobots))
		{
			return null;
		}

		// we have a kick event, let's gather some data
		MergedBall kickBall = camBallList.get(1);
		// firstly, who kicked?
		FilteredVisionBot kickingBot = mergedRobots.stream()
				.min(Comparator.comparingDouble(b -> b.getPos().distanceTo(kickBall.getCamPos())))
				.orElseThrow(IllegalStateException::new);

		// secondly, gather all merged balls we have since kick timestamp
		List<MergedBall> allBalls = camBallMap.values().stream()
				.flatMap(List::stream)
				.filter(b -> b.getTimestamp() >= kickBall.getTimestamp())
				.toList();

		log.debug("Early kick detected, bot: {}", kickingBot.getBotID());

		return KickEvent.builder()
				.kickingBot(kickingBot.getBotID())
				.position(kickBall.getCamPos())
				.botDirection(kickingBot.getOrientation())
				.kickingBotPosition(kickingBot.getPos())
				.timestamp(kickBall.getTimestamp())
				.isEarlyDetection(true)
				.recordsSinceKick(allBalls)
				.build();
	}


	private boolean detectKick(final List<MergedBall> balls, final List<FilteredVisionBot> mergedRobots)
	{
		CamBall ball0 = balls.get(0).getLatestCamBall().get();
		CamBall ball1 = balls.get(1).getLatestCamBall().get();
		CamBall ball2 = balls.get(2).getLatestCamBall().get();

		long numNearRobots = mergedRobots.stream()
				.filter(b -> b.getPos().distanceTo(ball0.getPos().getXYVector()) < nearBotLimit)
				.count();

		if (numNearRobots == 0)
		{
			// no kicking robot nearby :(
			return false;
		}

		double t0 = (ball0.getTimestamp() * 1e-9);
		double t1 = (ball1.getTimestamp() * 1e-9);
		double t2 = (ball2.getTimestamp() * 1e-9);

		double dt1 = t1 - t0;
		double dt2 = t2 - t1;

		IVector2 vel1 = ball1.getFlatPos()
				.subtractNew(ball0.getFlatPos()).multiply(1.0 / dt1);
		IVector2 vel2 = ball2.getFlatPos()
				.subtractNew(ball1.getFlatPos()).multiply(1.0 / dt2);

		if ((vel1.getLength2() > 10000) || (vel2.getLength2() > 10000))
		{
			return false;
		}

		// trigger on velocity jump
		if ((vel2.getLength2() - vel1.getLength2()) > velocityThreshold)
		{
			return true;
		}

		// trigger on large vel AND direction jump
		return (vel1.getLength2() > velocityThresholdDirection) && (vel2.getLength2() > velocityThresholdDirection)
				&& (vel1.angleToAbs(vel2).orElse(0.0) > AngleMath.deg2rad(directionThreshold));

	}


	@Override
	public void reset()
	{
		camBallMap.clear();
	}


	@Override
	public List<IDrawableShape> getDrawableShapes()
	{
		return Collections.emptyList();
	}
}
