/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamObjectFilterParams;
import edu.tigers.sumatra.data.TimestampBasedBuffer;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.util.Safe;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.FilteredVisionKick;
import edu.tigers.sumatra.wp.data.BallContact;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.KickedBall;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.util.BallContactCalculator;
import edu.tigers.sumatra.wp.util.CurrentBallDetector;
import edu.tigers.sumatra.wp.util.DefaultRobotInfoProvider;
import edu.tigers.sumatra.wp.util.GameStateCalculator;
import edu.tigers.sumatra.wp.util.IRobotInfoProvider;
import edu.tigers.sumatra.wp.util.MalFunctioningBotCalculator;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * This module collects some AI-independent world information, like filtered camera data and vision data
 */
@Log4j2
public class WorldInfoCollector extends AWorldPredictor
		implements IRefereeObserver, ICamFrameObserver
{
	private static final ShapeMapSource WP_SHAPE_MAP_SOURCE = ShapeMapSource.of("World Frame");
	private static final ShapeMapSource VISION_SHAPE_MAP_SOURCE = ShapeMapSource.of("Vision");
	private static final ShapeMapSource VISION_FILTER_SHAPE_MAP_SOURCE = ShapeMapSource.of("Vision Filter");

	@Configurable(
			comment = "Add a faked ball. Set pos,vel,acc in code.",
			defValue = "false"
	)
	private static boolean fakeBall = false;

	@Configurable(
			comment = "Use robot feedback for position and velocity.",
			defValue = "true"
	)
	private static boolean preferRobotFeedback = true;

	@Configurable(
			comment = "Use mal functioning check to filter available bots",
			defValue = "true"
	)
	private static boolean checkMalFunction = true;

	static
	{
		ConfigRegistration.registerClass("wp", WorldInfoCollector.class);
		String env = SumatraModel.getInstance().getEnvironment();
		ConfigRegistration.applySpezi("wp", env);
		ConfigRegistration.registerConfigurableCallback(
				"wp", new IConfigObserver()
				{
					@Override
					public void afterApply(IConfigClient configClient)
					{
						String env = SumatraModel.getInstance().getEnvironment();
						ConfigRegistration.applySpezi("wp", env);
					}
				}
		);
	}

	private final AutoPauseHook autoPauseHook = new AutoPauseHook();
	private final TimestampBasedBuffer<ITrackedBall> ballBuffer = new TimestampBasedBuffer<>(0.3);
	private final GameStateCalculator gameStateCalculator = new GameStateCalculator();
	private final WorldFrameVisualization worldFrameVisualization = new WorldFrameVisualization();
	private final MalFunctioningBotCalculator malFunctioningBotCalculator = new MalFunctioningBotCalculator();
	private final BallContactCalculator ballContactCalculator = new BallContactCalculator();
	private final CurrentBallDetector currentBallDetector = new CurrentBallDetector();
	private final CamFrameShapeMapProducer camFrameShapeMapProducer = new CamFrameShapeMapProducer();
	private AVisionFilter visionFilter;
	private IRobotInfoProvider robotInfoProvider = new DefaultRobotInfoProvider();
	private Referee referee;
	private ACam cam;
	private CiGameControllerConnector ciGameControllerConnector;
	private long lastWFTimestamp;
	private RefereeMsg latestRefereeMsg;


	private Map<BotID, BotState> getFilteredBotStates(final Collection<FilteredVisionBot> visionBots)
	{
		return visionBots.stream()
				.collect(Collectors.toMap(
						FilteredVisionBot::getBotID,
						FilteredVisionBot::toBotState
				));
	}


	private Map<BotID, FilteredVisionBot> getFilteredBots(final Collection<FilteredVisionBot> visionBots)
	{
		return visionBots.stream()
				.collect(Collectors.toMap(
						FilteredVisionBot::getBotID,
						Function.identity()
				));
	}


	private Map<BotID, BotState> getInternalBotStates(final Collection<RobotInfo> robotInfo)
	{
		return robotInfo.stream()
				.map(RobotInfo::getInternalState)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toMap(
						BotState::getBotId,
						Function.identity()
				));
	}


	private BotState selectRobotState(BotState filterState, BotState internalState)
	{
		if ((internalState != null && preferRobotFeedback) || filterState == null)
		{
			return internalState;
		}
		return filterState;
	}


	/**
	 * Get last ball contact.
	 *
	 * @param robotInfo
	 * @param pose      the pose of the robot, ideally that of vision (not predicted)
	 * @return
	 */
	private BallContact getLastBallContact(final RobotInfo robotInfo, final Pose pose)
	{
		return ballContactCalculator.ballContact(robotInfo, pose, robotInfo.getCenter2DribblerDist());
	}


	private TrackedBot createTrackedBot(
			RobotInfo robotInfo,
			BotState filterState,
			BotState internalState,
			FilteredVisionBot filteredVisionBot,
			Map<BotID, BotState> botStates
	)
	{
		if (filterState == null && internalState == null)
		{
			return null;
		}
		var currentBotState = selectRobotState(filterState, internalState);
		var visionBotState = Optional.ofNullable(filterState).orElse(internalState);

		boolean malFunctioning = checkMalFunction &&
				malFunctioningBotCalculator.isMalFunctioning(robotInfo, visionBotState.getPose(), botStates);
		return TrackedBot.newBuilder()
				.withBotId(robotInfo.getBotId())
				.withTimestamp(lastWFTimestamp)
				.withState(currentBotState)
				.withFilteredState(filterState)
				.withBotInfo(robotInfo)
				.withLastBallContact(getLastBallContact(robotInfo, visionBotState.getPose()))
				.withQuality(filteredVisionBot != null ? filteredVisionBot.getQuality() : 0)
				.withMalFunctioning(malFunctioning)
				.build();
	}


	private Map<BotID, ITrackedBot> collectTrackedBots(
			final List<FilteredVisionBot> filteredVisionBots,
			final Collection<RobotInfo> robotInfo
	)
	{
		Map<BotID, BotState> filteredBotStates = getFilteredBotStates(filteredVisionBots);
		Map<BotID, BotState> internalBotStates = getInternalBotStates(robotInfo);
		Map<BotID, FilteredVisionBot> filteredVisionBotMap = getFilteredBots(filteredVisionBots);

		Map<BotID, ITrackedBot> trackedBots = robotInfo.stream()
				.map(r -> createTrackedBot(
						r, filteredBotStates.get(r.getBotId()),
						internalBotStates.get(r.getBotId()),
						filteredVisionBotMap.get(r.getBotId()),
						filteredBotStates
				))
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return new HashMap<>(trackedBots);
	}


	private Map<BotID, RobotInfo> collectRobotInfo(final List<FilteredVisionBot> filteredVisionBots)
	{
		Set<BotID> allRelevantBots = new HashSet<>(robotInfoProvider.getConnectedBotIds());
		filteredVisionBots.stream().map(FilteredVisionBot::getBotID).forEach(allRelevantBots::add);

		return allRelevantBots.stream()
				.map(robotInfoProvider::getRobotInfo)
				.collect(Collectors.toMap(
						RobotInfo::getBotId,
						Function.identity()
				));
	}


	private void visualize(final WorldFrameWrapper wfw)
	{
		ShapeMap wfShapeMap = new ShapeMap();
		worldFrameVisualization.process(wfw, wfShapeMap);
		notifyNewShapeMap(lastWFTimestamp, wfShapeMap, WP_SHAPE_MAP_SOURCE);

		ShapeMap visionShapeMap = camFrameShapeMapProducer.createShapeMap();
		addCamObjectFilterShapes(visionShapeMap);
		notifyNewShapeMap(lastWFTimestamp, visionShapeMap, VISION_SHAPE_MAP_SOURCE);
	}


	private void addCamObjectFilterShapes(ShapeMap shapeMap)
	{
		if (cam == null)
		{
			return;
		}
		cam.getParams()
				.ifPresent(p -> shapeMap.get(EWpShapesLayer.CAM_OBJECT_FILTER).addAll(createCamObjectFilterShapes(p)));
	}


	private List<IDrawableShape> createCamObjectFilterShapes(CamObjectFilterParams p)
	{
		var rectangle = new DrawableRectangle(p.exclusionRectangle(), Color.red);
		if (p.excludedCamIds().isEmpty())
		{
			return List.of(rectangle);
		}
		var camIdStrings = p.excludedCamIds().stream().map(Object::toString).sorted().toList();
		var cams = new DrawableBorderText(Vector2.fromXY(1, 8), "Excluded cams: " + String.join(", ", camIdStrings))
				.setFontSize(EFontSize.LARGE).setColor(Color.RED);
		return List.of(rectangle, cams);
	}


	private KickedBall getKickedBall(FilteredVisionKick kick)
	{
		return KickedBall.builder()
				.kickTimestamp(kick.getKickTimestamp())
				.trajectoryStartTime(kick.getTrajectoryStartTime())
				.kickingBot(kick.getKickingBot())
				.kickingBotPose(Pose.from(kick.getKickingBotPosition(), kick.getKickingBotOrientation()))
				.ballTrajectory(kick.getBallTrajectory())
				.build();
	}


	private void processFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		lastWFTimestamp = filteredVisionFrame.getTimestamp();
		robotInfoProvider.setLastWFTimestamp(lastWFTimestamp);

		ballContactCalculator.setBallPos(filteredVisionFrame.getBall().getPos().getXYVector());

		Map<BotID, RobotInfo> robotInfo = collectRobotInfo(filteredVisionFrame.getBots());
		visionFilter.setRobotInfoMap(robotInfo);

		Map<BotID, ITrackedBot> bots = collectTrackedBots(filteredVisionFrame.getBots(), robotInfo.values());

		ITrackedBall ball = getTrackedBall(filteredVisionFrame);
		ballBuffer.add(ball);

		KickedBall kickedBall = filteredVisionFrame.getKick()
				.map(this::getKickedBall).orElse(null);

		long frameNumber = filteredVisionFrame.getId();
		SimpleWorldFrame swf = new SimpleWorldFrame(frameNumber, lastWFTimestamp, bots, ball, kickedBall);

		if (ciGameControllerConnector != null)
		{
			ciGameControllerConnector.process(swf, referee.flushChanges()).forEach(referee::onNewRefereeMessage);
		}

		GameState gameState = gameStateCalculator.getNextGameState(latestRefereeMsg, ball.getPos(), lastWFTimestamp);

		WorldFrameWrapper wfw = new WorldFrameWrapper(swf, latestRefereeMsg, gameState);
		Safe.forEach(consumers, c -> c.onNewWorldFrame(wfw));
		Safe.forEach(observers, c -> c.onNewWorldFrame(wfw));

		visualize(wfw);
		ShapeMap.setPersistDebugShapes(!SumatraModel.getInstance().isTournamentMode());
	}


	private ITrackedBall getTrackedBall(final FilteredVisionFrame filteredVisionFrame)
	{
		FilteredVisionBall filteredVisionBall = fakeBall ? fakeBall() : filteredVisionFrame.getBall();
		return TrackedBall.fromFilteredVisionBall(lastWFTimestamp, filteredVisionBall);
	}


	private FilteredVisionBall fakeBall()
	{
		return FilteredVisionBall.builder()
				.withTimestamp(lastWFTimestamp)
				.withBallState(BallState.builder()
						.withPos(Vector3.fromXYZ(1500, 500, 0))
						.withVel(Vector2.fromXY(-1500, -900).scaleToNew(2000).getXYZVector())
						.withAcc(Vector3f.ZERO_VECTOR)
						.withSpin(Vector2f.ZERO_VECTOR)
						.build())
				.withLastVisibleTimestamp(lastWFTimestamp)
				.build();
	}


	@Override
	public final void initModule()
	{
		Geometry.refresh();
		clearObservers();

		reset();

		registerToVisionFilterModule();
		registerToRefereeModule();
		registerToCamModule();
		registerToRecordManagerModule();
	}


	@Override
	public final void deinitModule()
	{
		// nothing to do
	}


	@Override
	public void startModule()
	{
		super.startModule();

		if (referee.getActiveSource().getType() == ERefereeMessageSource.CI)
		{
			int port = getSubnodeConfiguration().getInt("ci-port", 11009);
			ciGameControllerConnector = new CiGameControllerConnector(port);
			ciGameControllerConnector.start();
		}
	}


	@Override
	public final void stopModule()
	{
		unregisterFromVisionFilterModule();
		unregisterFromRefereeModule();
		unregisterFromCamModule();
		unregisterToRecordManagerModule();

		if (ciGameControllerConnector != null)
		{
			ciGameControllerConnector.stop();
			ciGameControllerConnector = null;
		}
	}


	private void registerToRecordManagerModule()
	{
		if (SumatraModel.getInstance().isModuleLoaded(RecordManager.class))
		{
			RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
			recordManager.addHook(autoPauseHook);
		}
	}


	private void unregisterToRecordManagerModule()
	{
		if (SumatraModel.getInstance().isModuleLoaded(RecordManager.class))
		{
			RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
			recordManager.removeHook(autoPauseHook);
		}
	}


	private void registerToVisionFilterModule()
	{
		visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
		visionFilter.getFilteredVisionFrame().subscribe(getClass().getCanonicalName(), this::onNewFilteredVisionFrame);
		visionFilter.getFilteredVisionFrame().subscribeClear(getClass().getCanonicalName(), this::reset);
	}


	private void unregisterFromVisionFilterModule()
	{
		if (visionFilter != null)
		{
			visionFilter.getFilteredVisionFrame().unsubscribe(getClass().getCanonicalName());
			visionFilter.getFilteredVisionFrame().unsubscribeClear(getClass().getCanonicalName());
		}
	}


	private void registerToRefereeModule()
	{
		referee = SumatraModel.getInstance().getModule(Referee.class);
		referee.addObserver(this);
	}


	private void unregisterFromRefereeModule()
	{
		referee.removeObserver(this);
	}


	private void registerToCamModule()
	{
		SumatraModel.getInstance().getModuleOpt(ACam.class).ifPresent(c -> {
			this.cam = c;
			c.addObserver(this);
		});
	}


	private void unregisterFromCamModule()
	{
		SumatraModel.getInstance().getModuleOpt(ACam.class).ifPresent(c -> c.removeObserver(this));
		cam = null;
	}


	@Override
	public void reset()
	{
		log.debug("Resetting world info collector");
		observers.forEach(IWorldFrameObserver::onClearCamDetectionFrame);

		gameStateCalculator.reset();
		worldFrameVisualization.reset();
		currentBallDetector.reset();
		camFrameShapeMapProducer.reset();
		ballContactCalculator.reset();
		malFunctioningBotCalculator.reset();
		lastWFTimestamp = 0;
		latestRefereeMsg = new RefereeMsg();
	}


	@Override
	public void onClearCamFrame()
	{
		reset();
	}


	private void clearObservers()
	{
		if (!observers.isEmpty())
		{
			log.warn("There were observers left: {}", observers);
			observers.clear();
		}
		if (!consumers.isEmpty())
		{
			log.warn("There were consumers left: {}", consumers);
			consumers.clear();
		}
	}


	private void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		processFilteredVisionFrame(filteredVisionFrame);
		notifyNewShapeMap(lastWFTimestamp, filteredVisionFrame.getShapeMap(), VISION_FILTER_SHAPE_MAP_SOURCE);
	}


	@Override
	public void onNewRefereeMsg(final SslGcRefereeMessage.Referee refMsg)
	{
		long ts = lastWFTimestamp;
		if (refMsg.getCommandCounter() == latestRefereeMsg.getCmdCounter())
		{
			ts = latestRefereeMsg.getFrameTimestamp();
		}
		latestRefereeMsg = new RefereeMsg(ts, refMsg);
		updateTeamOnPositiveHalf(latestRefereeMsg);
	}


	private void updateTeamOnPositiveHalf(final RefereeMsg refMsg)
	{
		if (refMsg.getNegativeHalfTeam().isNonNeutral())
		{
			Geometry.setNegativeHalfTeam(refMsg.getNegativeHalfTeam());
		}
	}


	@Override
	public void onNewCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		CamBall ball = currentBallDetector.findCurrentBall(camDetectionFrame.getBalls());
		ExtendedCamDetectionFrame eFrame = new ExtendedCamDetectionFrame(camDetectionFrame, ball);
		observers.forEach(o -> o.onNewCamDetectionFrame(eFrame));
		camFrameShapeMapProducer.updateCamFrameShapes(eFrame);
	}


	@Override
	public void setRobotInfoProvider(final IRobotInfoProvider robotInfoProvider)
	{
		this.robotInfoProvider = robotInfoProvider;
	}
}
