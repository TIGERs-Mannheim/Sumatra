/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.EAiType;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.trajectory.TrajectoryWrapper;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.IVisionFilterObserver;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.util.BallContactCalculator;
import edu.tigers.sumatra.wp.util.CurrentBallDetector;
import edu.tigers.sumatra.wp.util.GameStateCalculator;


/**
 * This module collects some AI-independent world information, like filtered camera data and vision data
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class WorldInfoCollector extends AWorldPredictor
		implements IRefereeObserver, IVisionFilterObserver, ICamFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(WorldInfoCollector.class.getName());
	
	private final GameStateCalculator gameStateCalculator = new GameStateCalculator();
	private final WorldFrameVisualization worldFrameVisualization = new WorldFrameVisualization();
	private final BallContactCalculator ballContactCalculator = new BallContactCalculator();
	private final CurrentBallDetector currentBallDetector = new CurrentBallDetector();
	
	private AVisionFilter visionFilter;
	private ABotManager botManager;
	private BotParamsManager botParamsManager;
	
	private long lastWFTimestamp = 0;
	private WorldFrameWrapper lastWorldFrameWrapper = null;
	private RefereeMsg latestRefereeMsg = new RefereeMsg();
	private IKickEvent lastKickEvent = null;
	private Map<BotID, EAiType> botToAiMap = new IdentityHashMap<>();
	
	
	@Configurable(comment = "Add a faked ball. Set pos,vel,acc in code.", defValue = "false")
	private static boolean fakeBall = false;
	
	@Configurable(comment = "Use robot feedback for position and velocity.", defValue = "true")
	private static boolean useRobotFeedback = true;
	
	static
	{
		ConfigRegistration.registerClass("wp", WorldInfoCollector.class);
	}
	
	
	/**
	 * @param config
	 */
	public WorldInfoCollector(final SubnodeConfiguration config)
	{
	}
	
	
	private RobotInfo getRobotInfo(final BotID botID)
	{
		if ((botManager == null) || (botParamsManager == null))
		{
			return RobotInfo.stub(botID, lastWFTimestamp);
		}
		ABot bot = botManager.getBotTable().get(botID);
		if (bot == null)
		{
			IBotParams opponentParams = botParamsManager.getBotParams(EBotParamLabel.OPPONENT);
			
			return RobotInfo.stubBuilder(botID, lastWFTimestamp)
					.withBotParams(opponentParams)
					.build();
		}
		
		Optional<TrajectoryWithTime<IVector3>> trajectoryWithTime = bot.getCurrentTrajectory();
		ITrajectory<IVector3> trajectory = null;
		if (trajectoryWithTime.isPresent())
		{
			double age = (lastWFTimestamp - trajectoryWithTime.get().gettStart()) / 1e9;
			ITrajectory<IVector3> traj = trajectoryWithTime.get().getTrajectory();
			trajectory = new TrajectoryWrapper<>(traj, age, traj.getTotalTime());
		}
		
		return RobotInfo.newBuilder()
				.withBotId(bot.getBotId())
				.withTimestamp(lastWFTimestamp)
				.withAiType(botToAiMap.getOrDefault(botID, EAiType.NONE))
				.withBallContact(bot.isBarrierInterrupted())
				.withBattery(bot.getBatteryRelative())
				.withBotFeatures(bot.getBotFeatures())
				.withBotParams(bot.getBotParams())
				.withChip(bot.getMatchCtrl().getSkill().getDevice().equals(EKickerDevice.CHIP))
				.withDribbleRpm(bot.getMatchCtrl().getSkill().getDribbleSpeed())
				.withHardwareId(bot.getHardwareId())
				.withInternalPose(bot.getSensoryPos().orElse(null))
				.withInternalVel(bot.getSensoryVel().orElse(null))
				.withKickerVoltage(bot.getKickerLevel())
				.withKickSpeed(bot.getMatchCtrl().getSkill().getKickSpeed())
				.withTrajectory(trajectory)
				.withType(bot.getType())
				.withRobotMode(bot.getRobotMode())
				.build();
	}
	
	
	private IBotIDMap<ITrackedBot> convertToBotMap(final List<FilteredVisionBot> bots, final ITrackedBall ball)
	{
		IBotIDMap<ITrackedBot> map = new BotIDMap<>();
		for (FilteredVisionBot visionBot : bots)
		{
			BotID botID = visionBot.getBotID();
			RobotInfo robotInfo = getRobotInfo(botID);
			boolean ballContact = hasBallContact(robotInfo, visionBot.getPos(), visionBot.getOrientation(),
					robotInfo.getCenter2DribblerDist(), ball);
			RobotInfo wpRobotInfo = RobotInfo.copyBuilder(robotInfo)
					.withBallContact(ballContact)
					.build();
			
			final ITrackedBot tBot;
			if (useRobotFeedback)
			{
				tBot = TrackedBot.newBuilder()
						.withBotId(botID)
						.withTimestamp(lastWFTimestamp)
						.withPos(robotInfo.getInternalPose().map(IVector3::getXYVector).orElse(visionBot.getPos()))
						.withVel(robotInfo.getInternalVel().map(IVector3::getXYVector).orElse(visionBot.getVel()))
						.withOrientation(robotInfo.getInternalPose().map(IVector3::z).orElse(visionBot.getOrientation()))
						.withAngularVel(robotInfo.getInternalVel().map(IVector3::z).orElse(visionBot.getAngularVel()))
						.withVisible(true)
						.withBotInfo(wpRobotInfo)
						.build();
			} else
			{
				tBot = TrackedBot.newBuilder()
						.withBotId(botID)
						.withTimestamp(lastWFTimestamp)
						.withPos(visionBot.getPos())
						.withVel(visionBot.getVel())
						.withOrientation(visionBot.getOrientation())
						.withAngularVel(visionBot.getAngularVel())
						.withVisible(true)
						.withBotInfo(wpRobotInfo)
						.build();
			}
			
			map.put(botID, tBot);
		}
		return map;
	}
	
	
	private void addBotsFromBotmanager(final IBotIDMap<ITrackedBot> bots, final ITrackedBall ball)
	{
		for (RobotInfo robotInfo : visionFilter.getRobotInfoFrames())
		{
			if (bots.containsKey(robotInfo.getBotId()))
			{
				break;
			}
			Optional<IVector3> optPose = robotInfo.getInternalPose();
			Optional<IVector3> optVel = robotInfo.getInternalVel();
			if (optPose.isPresent() && optVel.isPresent())
			{
				IVector3 pose = optPose.get();
				IVector3 vel = optVel.get();
				boolean ballContact = hasBallContact(robotInfo, pose.getXYVector(), pose.z(),
						robotInfo.getCenter2DribblerDist(), ball);
				RobotInfo wpRobotInfo = RobotInfo.copyBuilder(robotInfo)
						.withBallContact(ballContact)
						.build();
				
				ITrackedBot tBot = TrackedBot.newBuilder()
						.withBotId(robotInfo.getBotId())
						.withTimestamp(lastWFTimestamp)
						.withPos(pose.getXYVector())
						.withVel(vel.getXYVector())
						.withOrientation(pose.z())
						.withAngularVel(vel.z())
						.withVisible(false)
						.withBotInfo(wpRobotInfo)
						.build();
				
				bots.put(robotInfo.getBotId(), tBot);
			}
		}
	}
	
	
	private boolean hasBallContact(final RobotInfo robotInfo, final IVector2 pos, final double orientation,
			final double center2Dribbler,
			final ITrackedBall ball)
	{
		if (robotInfo.getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING)
		{
			return robotInfo.isBallContact();
		}
		return ballContactCalculator.ballContact(robotInfo.getBotId(), pos, orientation, center2Dribbler, ball);
	}
	
	
	private void updateVisionFilterWithRobotInfo(final IBotIDMap<ITrackedBot> bots)
	{
		for (ITrackedBot bot : bots.values())
		{
			visionFilter.updateRobotInfo(bot.getRobotInfo());
		}
	}
	
	
	private IKickEvent getKickEvent(final FilteredVisionFrame filteredVisionFrame)
	{
		if (filteredVisionFrame.getKickEvent().isPresent())
		{
			lastKickEvent = filteredVisionFrame.getKickEvent().get();
		} else if (filteredVisionFrame.getBall().getVel().getLength2() < 0.1)
		{
			lastKickEvent = null;
		}
		return lastKickEvent;
	}
	
	
	private void processFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		lastWFTimestamp = filteredVisionFrame.getTimestamp();
		long frameNumber = filteredVisionFrame.getId();
		ITrackedBall ball = getTrackedBall(filteredVisionFrame);
		IBotIDMap<ITrackedBot> bots = convertToBotMap(filteredVisionFrame.getBots(), ball);
		IKickEvent kickEvent = getKickEvent(filteredVisionFrame);
		addBotsFromBotmanager(bots, ball);
		updateVisionFilterWithRobotInfo(bots);
		
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, kickEvent, frameNumber, lastWFTimestamp);
		
		GameState gameState = gameStateCalculator.getNextGameState(latestRefereeMsg, ball.getPos());
		
		WorldFrameWrapper wfw = new WorldFrameWrapper(swf, latestRefereeMsg, gameState);
		wfw.getShapeMap().merge(filteredVisionFrame.getShapeMap());
		
		for (IWorldFrameObserver c : consumers)
		{
			c.onNewWorldFrame(wfw);
		}
		
		worldFrameVisualization.process(wfw);
		
		for (IWorldFrameObserver c : observers)
		{
			c.onNewWorldFrame(wfw);
		}
		
		lastWFTimestamp = filteredVisionFrame.getTimestamp();
		lastWorldFrameWrapper = wfw;
	}
	
	
	private ITrackedBall getTrackedBall(final FilteredVisionFrame filteredVisionFrame)
	{
		FilteredVisionBall filteredVisionBall;
		if (fakeBall)
		{
			filteredVisionBall = fakeBall();
		} else
		{
			filteredVisionBall = filteredVisionFrame.getBall();
		}
		return TrackedBall.fromFilteredVisionBall(lastWFTimestamp, filteredVisionBall);
	}
	
	
	private FilteredVisionBall fakeBall()
	{
		return FilteredVisionBall.Builder.create()
				.withPos(Vector3.fromXYZ(1500, 500, 0))
				.withVel(Vector3.from2d(Vector2.fromXY(-1500, -900).scaleToNew(2000), 0))
				.withAcc(Vector3.zero())
				.withIsChipped(false)
				.withvSwitch(0)
				.withLastVisibleTimestamp(lastWFTimestamp)
				.build();
	}
	
	
	@Override
	public final void initModule() throws InitModuleException
	{
		// nothing to do
	}
	
	
	@Override
	public final void deinitModule()
	{
		// nothing to do
	}
	
	
	@Override
	public final void startModule() throws StartModuleException
	{
		Geometry.refresh();
		clearObservers();
		initBotToAiAssignment();
		try
		{
			visionFilter = (AVisionFilter) SumatraModel.getInstance().getModule(AVisionFilter.MODULE_ID);
			visionFilter.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find visionFilter module", err);
		}
		try
		{
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			referee.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find referee module", err);
		}
		try
		{
			botParamsManager = (BotParamsManager) SumatraModel.getInstance().getModule(BotParamsManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.debug("Could not find botParamsManager module", err);
		}
		
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.debug("Could not find botManager module", err);
		}
		try
		{
			if (!"SUMATRA".equals(SumatraModel.getInstance().getEnvironment()))
			{
				ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
				cam.addObserver(this);
			}
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not find ACam module.", err);
		}
		BallFactory.updateConfigs();
		ShapeMap.setPersistDebugShapes(!SumatraModel.getInstance().isProductive());
	}
	
	
	@Override
	public final void stopModule()
	{
		if (visionFilter != null)
		{
			visionFilter.removeObserver(this);
			visionFilter = null;
		}
		try
		{
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			referee.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find referee module", err);
		}
		try
		{
			if (!"SUMATRA".equals(SumatraModel.getInstance().getEnvironment()))
			{
				ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
				cam.removeObserver(this);
			}
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not find ACam module", err);
		}
		saveBotToAiAssignment();
	}
	
	
	private void clearObservers()
	{
		if (!observers.isEmpty())
		{
			log.warn("There were observers left: " + observers);
			observers.clear();
		}
		if (!consumers.isEmpty())
		{
			log.warn("There were consumers left: " + consumers);
			consumers.clear();
		}
	}
	
	
	@Override
	public void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		processFilteredVisionFrame(filteredVisionFrame);
	}
	
	
	@Override
	public void onNewRefereeMsg(final Referee.SSL_Referee refMsg)
	{
		long ts = lastWFTimestamp;
		if (refMsg.getCommandCounter() == latestRefereeMsg.getCommandCounter())
		{
			ts = latestRefereeMsg.getFrameTimestamp();
		}
		latestRefereeMsg = new RefereeMsg(ts, refMsg);
	}
	
	
	@Override
	public void setLatestBallPosHint(final IVector2 pos)
	{
		visionFilter.resetBall(Vector3.from2d(pos, 0), AVector3.ZERO_VECTOR);
	}
	
	
	@Override
	public void onNewCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		CamBall ball = currentBallDetector.findCurrentBall(camDetectionFrame.getBalls());
		ExtendedCamDetectionFrame eFrame = new ExtendedCamDetectionFrame(camDetectionFrame, ball);
		for (IWorldFrameObserver o : observers)
		{
			o.onNewCamDetectionFrame(eFrame);
		}
	}
	
	
	@Override
	public void onClearCamFrame()
	{
		for (IWorldFrameObserver o : observers)
		{
			o.onClearCamDetectionFrame();
		}
		lastWFTimestamp = 0;
		currentBallDetector.reset();
		gameStateCalculator.reset();
		worldFrameVisualization.reset();
		latestRefereeMsg = new RefereeMsg();
	}
	
	
	public WorldFrameWrapper getLastWorldFrameWrapper()
	{
		return lastWorldFrameWrapper;
	}
	
	
	private void initBotToAiAssignment()
	{
		for (BotID botID : BotID.getAll())
		{
			String key = WorldInfoCollector.class.getCanonicalName() + "." + botID.getNumberWithColorOffset();
			EAiType defAiType = EAiType.PRIMARY;
			String aiTypeStr = SumatraModel.getInstance().getUserProperty(key, defAiType.name());
			if (!aiTypeStr.isEmpty())
			{
				try
				{
					EAiType aiType = EAiType.valueOf(aiTypeStr);
					botToAiMap.put(botID, aiType);
				} catch (IllegalArgumentException err)
				{
					log.warn("Could not parse aiType: " + aiTypeStr, err);
				}
			}
		}
	}
	
	
	private void saveBotToAiAssignment()
	{
		for (Map.Entry<BotID, EAiType> entry : botToAiMap.entrySet())
		{
			BotID botID = entry.getKey();
			EAiType aiType = entry.getValue();
			String key = WorldInfoCollector.class.getCanonicalName() + "." + botID.getNumberWithColorOffset();
			if (aiType == null)
			{
				SumatraModel.getInstance().setUserProperty(key, "");
			} else
			{
				SumatraModel.getInstance().setUserProperty(key, aiType.name());
			}
		}
	}
	
	
	@Override
	public void updateBot2AiAssignment(final BotID botID, final EAiType aiTeam)
	{
		botToAiMap.put(botID, aiTeam);
	}
	
	
	@Override
	public Map<BotID, EAiType> getBotToAiMap()
	{
		return botToAiMap;
	}
}
