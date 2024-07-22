/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.KickedBall;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Pass detection for the passing hardware challenge 4 of RoboCup 2021.
 */
@Log4j2
public class PassDetectionAutoRefCalc implements IAutoRefereeCalc
{
	@Configurable(defValue = "false", comment = "Enable pass detection for passing challenge")
	private static boolean enabled = false;

	@Configurable(defValue = "0.174533", comment = "Min direction change between passes to count a valid pass")
	private static double minDirectionChange = 0.174533;

	@Configurable(defValue = "1500", comment = "Min pass distance to count a valid pass")
	private static double minDistance = 1500;

	@Configurable(defValue = "150", comment = "[mm] Min pass hight to count as chip pass")
	private static double minHeight = 150;

	static
	{
		ConfigRegistration.registerClass("autoreferee", PassDetectionAutoRefCalc.class);
	}

	private final List<Pass> lastPasses = new ArrayList<>();
	private KickedBall lastKickedBall;
	private List<IVector3> ballVel = new ArrayList<>(5);
	private int passId;
	private Pass lastPass;
	private KickedBall lastConsumedKickedBall;
	private int numValidPasses;
	private int numValidChippedPasses;
	private double height;


	@Override
	public void process(AutoRefFrame frame)
	{
		if (!enabled || !frame.getGameState().isRunning())
		{
			if (numValidPasses > 0)
			{
				log.info("Detected {} valid passes including {} valid chipped passes.", numValidPasses,
						numValidChippedPasses);
			}

			passId = 0;
			height = 0;
			lastKickedBall = null;
			ballVel.clear();
			lastPass = null;
			lastPasses.clear();
			numValidPasses = 0;
			numValidChippedPasses = 0;
			return;
		}
		if (passFinished(frame))
		{
			findPass(frame).ifPresent(pass -> {
				lastPass = pass;
				lastConsumedKickedBall = lastKickedBall;
				if (lastPasses.size() >= 3)
				{
					lastPasses.remove(0);
				}
				lastPasses.add(pass);
				log.info("Detected pass: {}", lastPass);
				if (pass.isValid())
				{
					numValidPasses++;
					if (pass.getHeight() > minHeight)
					{
						numValidChippedPasses++;
					}
				}
			});
			height = 0;
		}
		updateMaxHeight(frame.getWorldFrame().getBall().getHeight());
		rememberState(frame);
		drawShapes(frame);
	}


	private void updateMaxHeight(double currentHeight)
	{
		if (currentHeight > height)
		{
			height = currentHeight;
		}
	}


	private void rememberState(AutoRefFrame frame)
	{
		lastKickedBall = frame.getWorldFrame().getKickedBall().orElse(lastKickedBall);
		var lastBallSpeed = frame.getWorldFrame().getBall().getVel3();
		if (ballVel.size() == 5)
		{
			ballVel.remove(0);
		}
		ballVel.add(lastBallSpeed);
	}


	private Optional<Pass> findPass(AutoRefFrame frame)
	{
		var target = frame.getWorldFrame().getBall().getPos();
		var source = lastKickedBall.getPosition();
		var distance = source.distanceTo(target);
		if (distance < 300)
		{
			return Optional.empty();
		}

		var receiver = frame.getWorldFrame().getBots().values().stream()
				.filter(b -> b.getBotKickerPos(Geometry.getBallRadius()).distanceTo(target) < 200)
				.min(Comparator.comparing(b -> b.getPos().distanceTo(target)))
				.map(ITrackedBot::getBotId)
				.orElse(BotID.noBot());
		if (!receiver.isBot())
		{
			return Optional.empty();
		}

		var direction = target.subtractNew(source).getAngle();
		var directionChange = getDirectionChange(direction);
		var initialBallSpeed = lastKickedBall.getAbsoluteKickSpeed();
		var valid = isValid(distance, receiver, directionChange, initialBallSpeed);
		var chipped = height > minHeight;
		return Optional.of(Pass.builder()
				.id(++passId)
				.timestamp(lastKickedBall.getTimestamp())
				.distance(distance)
				.height(height)
				.direction(direction)
				.directionChange(directionChange)
				.valid(valid)
				.chipped(chipped)
				.source(source)
				.target(target)
				.shooter(lastKickedBall.getKickingBot())
				.receiver(receiver)
				.initialBallSpeed(initialBallSpeed)
				.receivingBallSpeed(ballVel.get(0).getLength())
				.build());
	}


	private boolean isValid(double distance, BotID receiver, Double directionChange, double initialBallSpeed)
	{
		if (directionChange != null && directionChange < minDirectionChange)
		{
			return false;
		}
		return distance >= minDistance
				&& initialBallSpeed <= RuleConstraints.getMaxBallSpeed()
				&& getLastShooter() != receiver;
	}


	private Double getDirectionChange(double direction)
	{
		if (lastPass != null)
		{
			return AngleMath.diffAbs(direction, lastPass.getDirection() + AngleMath.DEG_180_IN_RAD);
		}
		return null;
	}


	private BotID getLastShooter()
	{
		if (lastPass != null)
		{
			return lastPass.getShooter();
		}
		return BotID.noBot();
	}


	private boolean passFinished(AutoRefFrame frame)
	{
		if (lastKickedBall == null ||
				(lastConsumedKickedBall != null && lastKickedBall.getTimestamp() == lastConsumedKickedBall.getTimestamp()))
		{
			return false;
		}

		double kickEventAge = (frame.getTimestamp() - lastKickedBall.getTimestamp()) / 1e9;
		if (kickEventAge < 0.1)
		{
			return false;
		}

		var newKickEvent = frame.getWorldFrame().getKickedBall().orElse(null);
		if (newKickEvent == null)
		{
			// kick event reset - ball might be received and stopped by receiver
			return true;
		}
		if (newKickEvent.getTimestamp() != lastKickedBall.getTimestamp())
		{
			// kick event changed
			return true;
		}
		return ballDirectionChanged(frame);
	}


	private boolean ballDirectionChanged(AutoRefFrame frame)
	{
		IVector3 vel3 = frame.getWorldFrame().getBall().getVel3();
		if (vel3.getLength() < 0.1)
		{
			return true;
		}
		return AngleMath.diffAbs(vel3.getXYVector().getAngle(), ballVel.get(0).getXYVector().getAngle()) >
				AngleMath.deg2rad(45);
	}


	private void drawShapes(AutoRefFrame frame)
	{
		for (var pass : lastPasses)
		{
			List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.PASS_DETECTION);
			shapes.add(new DrawableArrow(
					pass.getSource(),
					pass.getTarget().subtractNew(pass.getSource()),
					pass.isValid() ? Color.green : Color.red
			));
			var center = Lines.segmentFromPoints(pass.getSource(), pass.getTarget()).getPathCenter();
			shapes.add(new DrawableAnnotation(
					center,
					String.format(
							"%d: %.1f m/s -> %.1f m/s | α = %.0f°",
							pass.getId(), pass.getInitialBallSpeed(), pass.getReceivingBallSpeed(),
							pass.getDirectionChange() == null ? null : AngleMath.rad2deg(pass.getDirectionChange())
					)
			));
		}
	}
}
