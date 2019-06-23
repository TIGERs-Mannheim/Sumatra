/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botmanager.commands.MultimediaControl;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.botmanager.commands.other.ESong;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.util.IBallWatcherObserver;
import edu.tigers.sumatra.wp.util.VisionWatcher;


/**
 * Sample kick data with fixed duration and make a recording.
 * The bot will wait at the specified position until the user places a ball in front of it.
 * 
 * @author AndreR
 * 
 *         <pre>
           +---> MoveToState +-----+
TimePassed |                       | AtTarget
           |                       |
           +                       v
      SampleState            WaitForBallState
           ^                       +
           |                       |
TimePassed |                       | BallReady
           +---+ WarningState <----+
 *         </pre>
 */
public class KickSampleSkill extends AMoveSkill implements IBallWatcherObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(KickSampleSkill.class.getName());
	
	private IVector2 kickPos;
	private double targetAngle;
	private AWorldPredictor wp;
	private VisionWatcher watcher;
	private EKickerDevice device;
	private double kickDuration;
	private String folderName = "ballKick";
	private int numSamples = 0;
	private Map<String, Object> additionalJsonData;
	
	
	/**
	 * @param kickPos
	 * @param targetAngle
	 * @param device
	 * @param kickDurationMs [ms]
	 */
	public KickSampleSkill(final IVector2 kickPos, final double targetAngle, final EKickerDevice device,
			final double kickDurationMs)
	{
		super(ESkill.KICK_SAMPLE);
		
		this.kickPos = kickPos;
		this.targetAngle = targetAngle;
		this.device = device;
		kickDuration = kickDurationMs * 1e-3;
		
		IState moveTo = new MoveToState();
		IState waitForBall = new WaitForBallState();
		IState warning = new WarningState();
		IState sample = new SampleState();
		setInitialState(moveTo);
		addTransition(moveTo, EEvent.AT_TARGET, waitForBall);
		addTransition(waitForBall, EEvent.BALL_READY, warning);
		addTransition(warning, EEvent.TIME_PASSED, sample);
		addTransition(warning, EEvent.BALL_LOST, waitForBall);
		addTransition(sample, EEvent.TIME_PASSED, moveTo);
	}
	
	private enum EEvent implements IEvent
	{
		AT_TARGET,
		BALL_READY,
		BALL_LOST,
		TIME_PASSED
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
	public void beforeExport(final Map<String, Object> jsonMapping)
	{
		jsonMapping.put("duration", kickDuration);
		jsonMapping.put("isChip", device.getValue());
		jsonMapping.put("voltage", getBot().getKickerLevel());
		if (getWorldFrame().isInverted())
		{
			jsonMapping.put("kickPos", kickPos.multiplyNew(-1.0).toJSONArray());
		} else
		{
			jsonMapping.put("kickPos", kickPos.toJSONArray());
		}
		
		if (additionalJsonData != null)
		{
			jsonMapping.put("opt", additionalJsonData);
		}
	}
	
	
	@Override
	protected void onSkillStarted()
	{
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
		
		getMoveCon().setPenaltyAreaAllowedOur(true);
		getMoveCon().setPenaltyAreaAllowedTheir(true);
	}
	
	
	private class MoveToState implements IState
	{
		@Override
		public void doEntryActions()
		{
			setTargetPose(kickPos, targetAngle);
			MultimediaControl mmc = new MultimediaControl(true, false, true, false);
			mmc.setSong(ESong.NONE);
			setMultimediaControl(mmc);
		}
		
		
		@Override
		public void doUpdate()
		{
			// check for destination reached
			if ((getPos().distanceTo(kickPos) < 10) && (Math.abs(AngleMath.difference(getAngle(), targetAngle)) < 0.1))
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
	
	private class WaitForBallState implements IState, IWorldFrameObserver
	{
		private Map<Integer, List<IVector2>> ballCamMap = new ConcurrentHashMap<>();
		private int validBallDetections = 0;
		
		private double freeBallDistance = 1000.0;
		private double consecutiveSamplesRequired = 20;
		
		
		@Override
		public void doEntryActions()
		{
			wp.addObserver(this);
			validBallDetections = 0;
		}
		
		
		@Override
		public void doUpdate()
		{
			// check if only a single ball is within range
			IVector2 botPos = getPos();
			
			Stream<IVector2> ballStream = ballCamMap.values().stream().flatMap(Collection::stream);
			
			long numBallsInKeepout = ballStream.filter(
					b -> (botPos.distanceTo(b) < freeBallDistance)
							&& (botPos.distanceTo(b) > (Geometry.getBotRadius() * 1.5)))
					.count();
			
			ballStream = ballCamMap.values().stream().flatMap(Collection::stream);
			long numBallsAtBot = ballStream.filter(b -> botPos.distanceTo(b) < (Geometry.getBotRadius() * 1.5)).count();
			
			if ((numBallsAtBot > 0) && (numBallsInKeepout == 0) && getBot().isBarrierInterrupted())
			{
				validBallDetections++;
			} else
			{
				validBallDetections = 0;
			}
			
			if (validBallDetections > consecutiveSamplesRequired)
			{
				// 20 consecutive runs with a single ball, we can go on
				// give the WP a ball hint to make the ball in front of the robot the tracked ball
				ballStream = ballCamMap.values().stream().flatMap(Collection::stream);
				Optional<IVector2> theBall = ballStream.filter(b -> botPos.distanceTo(b) < (Geometry.getBotRadius() * 1.5))
						.findFirst();
				if (theBall.isPresent())
				{
					if (getWorldFrame().isInverted())
					{
						wp.setLatestBallPosHint(theBall.get().multiplyNew(-1.0));
					} else
					{
						wp.setLatestBallPosHint(theBall.get());
					}
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
	
	private class WarningState implements IState
	{
		private long warnTimeMs = 2000; // [ms]
		private long startTime;
		
		
		@Override
		public void doEntryActions()
		{
			MultimediaControl mmc = new MultimediaControl(false, true, false, true);
			mmc.setSong(ESong.CHEERING);
			setMultimediaControl(mmc);
			
			startTime = getWorldFrame().getTimestamp();
			
			// start watcher
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String fileName = folderName + "/" + sdf.format(new Date());
			watcher = new VisionWatcher(fileName);
			watcher.setStopAutomatically(false);
			watcher.addObserver(KickSampleSkill.this);
			watcher.start();
		}
		
		
		@Override
		public void doUpdate()
		{
			if (((getWorldFrame().getTimestamp() - startTime) * 1e-6) > warnTimeMs)
			{
				if (getBot().isBarrierInterrupted())
				{
					triggerEvent(EEvent.TIME_PASSED);
				} else
				{
					triggerEvent(EEvent.BALL_LOST);
				}
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			MultimediaControl mmc = new MultimediaControl(false, true, false, true);
			mmc.setSong(ESong.NONE);
			setMultimediaControl(mmc);
		}
	}
	
	private class SampleState implements IState
	{
		private long sampleTimeMs = 5000; // [ms]
		private long startTime;
		
		
		@Override
		public void doEntryActions()
		{
			startTime = getWorldFrame().getTimestamp();
			
			// do a kick
			BotSkillWheelVelocity wh = new BotSkillWheelVelocity();
			KickerDribblerCommands kd = new KickerDribblerCommands();
			kd.setKick(kickDuration, device, EKickerMode.ARM_TIME);
			wh.setKickerDribbler(kd);
			getMatchCtrl().setSkill(wh);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (((getWorldFrame().getTimestamp() - startTime) * 1e-6) > 100.0)
			{
				getMatchCtrl().setSkill(new BotSkillMotorsOff());
			}
			
			if (((getWorldFrame().getTimestamp() - startTime) * 1e-6) > sampleTimeMs)
			{
				triggerEvent(EEvent.TIME_PASSED);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			watcher.stopExport();
			numSamples++;
		}
	}
}
