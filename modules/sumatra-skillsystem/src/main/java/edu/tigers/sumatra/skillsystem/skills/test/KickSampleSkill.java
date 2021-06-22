/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.botmanager.botskills.data.ELedColor;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.botskills.data.MultimediaControl;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataCollectorObserver;
import edu.tigers.sumatra.data.collector.TimeSeriesDataCollector;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter2D;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.circle.ICircular;
import edu.tigers.sumatra.math.vector.AVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.util.TimeSeriesDataCollectorFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * Sample kick data with fixed duration and make a recording.
 * The bot will wait at the specified position until the user places a ball in front of it.
 */
public class KickSampleSkill extends AMoveSkill implements ITimeSeriesDataCollectorObserver
{
	private final IVector2 kickPos;
	private final double targetAngle;
	private final EKickerDevice device;
	private final double kickDuration;
	private final double rightOffset;

	private AWorldPredictor wp;
	private AVisionFilter visionFilter;
	private TimeSeriesDataCollector dataCollector;
	private String folderName = "ballKick";
	private int numSamples = 0;
	private Map<String, Object> additionalJsonData;

	private IVector2 smoothedBallPos;
	private double kickVoltage;


	public KickSampleSkill(
			final IVector2 kickPos,
			final double targetAngle,
			final EKickerDevice device,
			final double kickDurationMs,
			final double rightOffset
	)
	{
		this.kickPos = kickPos;
		this.targetAngle = targetAngle;
		this.device = device;
		this.kickDuration = kickDurationMs;
		this.rightOffset = rightOffset;

		IState moveToStart = new MoveToStartState();
		IState waitForBall = new WaitForBallState();
		IState sampleBallPos = new SampleBallPosState();
		IState moveToKickPos = new MoveToKickPosState();
		IState runUpKick = new RunUpKickState();

		setInitialState(moveToStart);
		addTransition(moveToStart, EEvent.AT_TARGET, waitForBall);
		addTransition(waitForBall, EEvent.BALL_READY, sampleBallPos);
		addTransition(sampleBallPos, EEvent.BALL_MOVED, waitForBall);
		addTransition(sampleBallPos, EEvent.TIME_PASSED, moveToKickPos);
		addTransition(moveToKickPos, EEvent.AT_TARGET, runUpKick);
		addTransition(runUpKick, EEvent.TIME_PASSED, moveToStart);
	}


	private enum EEvent implements IEvent
	{
		AT_TARGET,
		BALL_READY,
		BALL_MOVED,
		TIME_PASSED,
	}


	public void setFolderName(final String name)
	{
		folderName = name;
	}


	public void setAdditionalJsonData(final Map<String, Object> jsonMapping)
	{
		additionalJsonData = jsonMapping;
	}


	/**
	 * @return the numSamples
	 */
	public int getNumSamples()
	{
		return numSamples;
	}


	@Override
	public void onAddMetadata(final Map<String, Object> jsonMapping)
	{
		jsonMapping.put("duration", kickDuration);
		jsonMapping.put("isChip", device.getValue());
		jsonMapping.put("voltage", kickVoltage);
		if (getWorldFrame().isInverted())
		{
			jsonMapping.put("kickPos", smoothedBallPos.multiplyNew(-1.0).toJSONArray());
		} else
		{
			jsonMapping.put("kickPos", smoothedBallPos.toJSONArray());
		}

		jsonMapping.put("orientation", targetAngle);

		jsonMapping.put("offset", rightOffset);

		if (additionalJsonData != null)
		{
			jsonMapping.put("opt", additionalJsonData);
		}
	}


	@Override
	protected void doEntryActions()
	{
		super.doEntryActions();
		wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
	}


	@Override
	protected void doUpdate()
	{
		super.doUpdate();
		if (smoothedBallPos != null)
		{
			getShapes().get(ESkillShapesLayer.CALIBRATION)
					.add(new DrawableCircle(Circle.createCircle(smoothedBallPos, 50)));
		}
	}


	private class MoveToStartState extends AState
	{
		private IVector2 waitPos;


		@Override
		public void doEntryActions()
		{
			waitPos = kickPos.addNew(Vector2.fromAngle(targetAngle)
					.multiply(-50.0 - getBot().getCenter2DribblerDist() - Geometry.getBallRadius()));
			setTargetPose(waitPos, targetAngle, defaultMoveConstraints().setAccMax(1.0));

			MultimediaControl mmc = new MultimediaControl()
					.setSong(ESong.NONE)
					.setLedColor(ELedColor.GREEN);
			setMultimediaControl(mmc);
		}


		@Override
		public void doUpdate()
		{
			// check for destination reached
			if ((getPos().distanceTo(waitPos) < 10) && (Math.abs(AngleMath.difference(getAngle(), targetAngle)) < 0.1))
			{
				triggerEvent(EEvent.AT_TARGET);
			}
		}


		@Override
		public void doExitActions()
		{
			getMatchCtrl().setSkill(new BotSkillWheelVelocity());
		}
	}

	private class WaitForBallState extends AState implements IWorldFrameObserver
	{
		private static final double CONSECUTIVE_SAMPLES_REQUIRED = 20;

		private Map<Integer, List<IVector2>> ballCamMap = new ConcurrentHashMap<>();
		private List<IVector2> ballPositions = new ArrayList<>();
		private int validBallDetections = 0;

		private double freeBallRadius = 1000.0;
		private double ballAtBotRadius = 190.0;


		@Override
		public void doEntryActions()
		{
			ballCamMap.clear();
			ballPositions.clear();
			validBallDetections = 0;
			wp.addObserver(this);
		}


		private double getBallSpread(final List<IVector2> balls)
		{
			Optional<ICircle> circle = Circle.hullCircle(balls);
			return circle.map(ICircular::radius).orElse(0.0);
		}


		@Override
		public void doUpdate()
		{
			// check if only a single ball is within range
			IVector2 botPos = getPos();

			List<IVector2> allBalls = ballCamMap.values().stream().flatMap(Collection::stream)
					.collect(Collectors.toList());

			long numBallsInKeepout = allBalls.stream().filter(
					b -> (botPos.distanceTo(b) < freeBallRadius)
							&& (botPos.distanceTo(b) > ballAtBotRadius))
					.count();

			List<IVector2> closeBalls = allBalls.stream()
					.filter(b -> getPos().distanceTo(b) < ballAtBotRadius)
					.collect(Collectors.toList());

			if ((!closeBalls.isEmpty()) && (numBallsInKeepout == 0) && (getBallSpread(closeBalls) < 50.0))
			{
				ballPositions.addAll(closeBalls);
				validBallDetections++;
			} else
			{
				validBallDetections = 0;
			}

			if (validBallDetections > CONSECUTIVE_SAMPLES_REQUIRED)
			{
				// 20 consecutive runs with a single ball, we can go on
				// give the WP a ball hint to make the ball in front of the robot the tracked ball
				IVector2 theBall = AVector.meanVector(ballPositions).getXYVector();
				if (getWorldFrame().isInverted())
				{
					visionFilter.resetBall(Vector3.from2d(theBall.multiplyNew(-1.0), 0), Vector3f.ZERO_VECTOR);
				} else
				{
					visionFilter.resetBall(Vector3.from2d(theBall, 0), Vector3f.ZERO_VECTOR);
				}

				triggerEvent(EEvent.BALL_READY);
			}
		}


		@Override
		public void doExitActions()
		{
			wp.removeObserver(this);
		}


		@Override
		public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
		{
			List<IVector2> balls = new ArrayList<>();

			for (CamBall b : frame.getBalls())
			{
				IVector2 pos;
				if (getWorldFrame().isInverted())
				{
					pos = b.getPos().getXYVector().multiplyNew(-1.0);
				} else
				{
					pos = b.getPos().getXYVector();
				}

				balls.add(pos);
			}

			ballCamMap.put(frame.getCameraId(), balls);
		}
	}

	private class SampleBallPosState extends AState
	{
		private static final long SAMPLE_TIME_MS = 1000; // [ms]
		private long startTime;
		private ExponentialMovingAverageFilter2D ballPosFilter = new ExponentialMovingAverageFilter2D(0.98);


		@Override
		public void doEntryActions()
		{
			startTime = getWorldFrame().getTimestamp();
			ballPosFilter.setState(getBall().getPos());
		}


		@Override
		public void doUpdate()
		{
			MultimediaControl mmc = new MultimediaControl()
					.setSong(ESong.CHEERING)
					.setLedColor(ELedColor.RED);
			setMultimediaControl(mmc);

			ballPosFilter.update(getBall().getPos());

			if (getBall().getVel().getLength2() > 0.05)
			{
				triggerEvent(EEvent.BALL_MOVED);
			}

			smoothedBallPos = ballPosFilter.getState().getXYVector();
			if (smoothedBallPos.distanceTo(getBall().getPos()) > 50)
			{
				startTime = getWorldFrame().getTimestamp();
			}

			if (((getWorldFrame().getTimestamp() - startTime) * 1e-6) > SAMPLE_TIME_MS)
			{
				triggerEvent(EEvent.TIME_PASSED);
			}
		}
	}

	private class MoveToKickPosState extends AState
	{
		private IVector2 runUpPos;
		private static final long WAITE_TIME_MS = 2000; // [ms]
		private long startWaitTime = 0;


		@Override
		public void doEntryActions()
		{
			startWaitTime = 0;

			IVector2 y = Vector2.fromAngleLength(targetAngle,
					-20.0 - getBot().getCenter2DribblerDist() - Geometry.getBallRadius());
			IVector2 x = Vector2.fromAngleLength(targetAngle + AngleMath.PI_HALF, rightOffset);
			runUpPos = smoothedBallPos.addNew(y).add(x);
			setTargetPose(runUpPos, targetAngle, defaultMoveConstraints());
			setCurrentTrajectory(null);

			MultimediaControl mmc = new MultimediaControl()
					.setSong(ESong.CHEERING)
					.setLedColor(ELedColor.RED);
			setMultimediaControl(mmc);
		}


		@Override
		public void doUpdate()
		{
			// check for destination reached
			if ((getPos().distanceTo(runUpPos) < 10) && (Math.abs(AngleMath.difference(getAngle(), targetAngle)) < 0.1)
					&& (startWaitTime == 0))
			{
				startWaitTime = getWorldFrame().getTimestamp();
			}

			if ((startWaitTime > 0) && (((getWorldFrame().getTimestamp() - startWaitTime) * 1e-6) > WAITE_TIME_MS))
			{
				triggerEvent(EEvent.AT_TARGET);
			}
		}


		@Override
		public void doExitActions()
		{
			getMatchCtrl().setSkill(new BotSkillWheelVelocity());
		}
	}

	private class RunUpKickState extends AState
	{
		private static final long SAMPLE_TIME_MS = 2000; // [ms]
		private long startTime;


		@Override
		public void doEntryActions()
		{
			startTime = getWorldFrame().getTimestamp();

			kickVoltage = getBot().getKickerLevel();

			// start watcher
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String fileName = folderName + "/" + sdf.format(new Date());
			dataCollector = TimeSeriesDataCollectorFactory.createFullCollector(fileName);
			dataCollector.setStopAutomatically(false);
			dataCollector.addObserver(KickSampleSkill.this);
			dataCollector.start();

			// do a kick
			setKickParams(null);
			BotSkillLocalVelocity loc = new BotSkillLocalVelocity(Vector2.fromXY(0, 0.1), 0,
					defaultMoveConstraints());
			KickerDribblerCommands kd = new KickerDribblerCommands();
			kd.setKick(kickDuration, device, EKickerMode.ARM_TIME);
			loc.setKickerDribbler(kd);
			getMatchCtrl().setSkill(loc);
		}


		@Override
		public void doUpdate()
		{
			if (((getWorldFrame().getTimestamp() - startTime) * 1e-6) > 1000.0)
			{
				getMatchCtrl().setSkill(new BotSkillMotorsOff());
			}

			if (((getWorldFrame().getTimestamp() - startTime) * 1e-6) > SAMPLE_TIME_MS)
			{
				triggerEvent(EEvent.TIME_PASSED);
			}
		}


		@Override
		public void doExitActions()
		{
			dataCollector.stopExport();
			numSamples++;
		}
	}
}
