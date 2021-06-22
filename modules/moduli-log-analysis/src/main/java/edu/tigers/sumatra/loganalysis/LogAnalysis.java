/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.loganalysis;

import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.gamelog.proto.LogLabels;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.eventtypes.EEventType;
import edu.tigers.sumatra.loganalysis.eventtypes.IEventType;
import edu.tigers.sumatra.loganalysis.eventtypes.IEventTypeDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.eventtypes.ballpossession.BallPossessionEventType;
import edu.tigers.sumatra.loganalysis.eventtypes.dribbling.Dribbling;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.GoalShot;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.Passing;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.loganalysis.GameMemory.GameLogObject.BALL;
import static edu.tigers.sumatra.loganalysis.GameMemory.GameLogObject.CLOSEST_BOT;


public class LogAnalysis extends AModule implements IWorldFrameObserver
{
	private static final Logger log = LogManager.getLogger(LogAnalysis.class.getName());

	private static final String LOG_ANALYSIS_SHAPE = "LOG_ANALYSIS";
	private static final double TOLERANCE_NEXT_FRAME_TIME_JUMP = 0.5;

	private EnumMap<EEventType, IEventTypeDetection> eventTypeDetections = new EnumMap<>(EEventType.class);
	private EnumMap<EEventType, List<IEventType>> eventTypes = new EnumMap<>(EEventType.class);

	private GameMemory memory = new GameMemory(11);
	private long lastTimestamp = -1;

	private WorldFrameWrapper wFrameWrapper;


	public LogAnalysis()
	{
		for (EEventType type : EEventType.values())
		{
			try
			{
				IEventTypeDetection eventType = (IEventTypeDetection) type.getInstanceableClass().newDefaultInstance();
				eventTypeDetections.put(type, eventType);
			} catch (InstanceableClass.NotCreateableException err)
			{
				log.error("Could not instantiate event type: " + err.getMessage(), err);
			}
		}

	}


	@Override
	public void startModule()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
		SumatraModel.getInstance().getModule(AWorldPredictor.class).notifyRemoveSourceFromShapeMap(LOG_ANALYSIS_SHAPE);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		this.wFrameWrapper = wFrameWrapper;
	}


	public void process(final long frameId)
	{

		if (wFrameWrapper == null)
		{
			return;
		}
		ShapeMap shapes = new ShapeMap();

		IVector2 ballPos = wFrameWrapper.getSimpleWorldFrame().getBall().getPos();

		List<ITrackedBot> closestBotsToBall = wFrameWrapper.getSimpleWorldFrame().getBots().values().stream()
				.sorted(Comparator.comparingDouble(b -> b.getBotKickerPos().distanceToSqr(ballPos)))
				.limit(2)
				.collect(Collectors.toList());

		long currentTimestamp = wFrameWrapper.getSimpleWorldFrame().getTimestamp();
		boolean timeJump = false;

		if (lastTimestamp != -1)
		{
			timeJump = (currentTimestamp - lastTimestamp) > (TOLERANCE_NEXT_FRAME_TIME_JUMP * 1e9)
					|| lastTimestamp > currentTimestamp;
		}

		if (closestBotsToBall.isEmpty() || timeJump)
		{
			memory.clear();

			String cause = closestBotsToBall.isEmpty() ? "no bots on field" : "jump in timeline";
			log.debug("Because of " + cause + ", game memory was cleared and detections reset (timestamp: " +
					currentTimestamp + ")");

			eventTypeDetections.values().forEach(IEventTypeDetection::resetDetection);
			storeDetectedEventTypes();

			lastTimestamp = wFrameWrapper.getSimpleWorldFrame().getTimestamp();
			return;
		}

		memory.updateFrame(wFrameWrapper.getSimpleWorldFrame());
		memory.update(BALL, wFrameWrapper.getSimpleWorldFrame().getBall());
		memory.update(CLOSEST_BOT, closestBotsToBall.get(0));

		Optional<ITrackedBot> optionalSecondClosestBotToBall = Optional.empty();
		if (closestBotsToBall.size() > 1)
		{
			optionalSecondClosestBotToBall = Optional.of(closestBotsToBall.get(1));
		}

		TypeDetectionFrame typeDetectionFrame = new TypeDetectionFrame(wFrameWrapper, memory,
				closestBotsToBall.get(0), optionalSecondClosestBotToBall, shapes, frameId);
		// update all eventType with the new frame
		eventTypeDetections.values().forEach(event -> event.nextFrameForDetection(typeDetectionFrame));

		paintInstantaneousTypes(eventTypeDetections, shapes);

		storeDetectedEventTypes();

		paintOverviewDetections(shapes);

		SumatraModel.getInstance().getModule(AWorldPredictor.class)
				.notifyNewShapeMap(wFrameWrapper.getTimestamp(), shapes, ShapeMapSource.of(LOG_ANALYSIS_SHAPE));

		lastTimestamp = wFrameWrapper.getSimpleWorldFrame().getTimestamp();
	}


	private void storeDetectedEventTypes()
	{
		for (Map.Entry<EEventType, IEventTypeDetection> eventTypeEntry : eventTypeDetections.entrySet())
		{
			IEventTypeDetection typeDetection = eventTypeEntry.getValue();
			IEventType event = typeDetection.getDetectedEventType();

			if (!eventTypes.containsKey(eventTypeEntry.getKey()))
			{
				eventTypes.put(eventTypeEntry.getKey(), new ArrayList<>());
			}

			if (event != null && !eventTypes.get(eventTypeEntry.getKey()).contains(event))
			{
				eventTypes.get(eventTypeEntry.getKey()).add(event);
			}
		}
	}


	public LogLabels.Labels getProtobufMsgLogLabels()
	{
		LogLabels.Labels.Builder labelsBuilder = LogLabels.Labels.newBuilder();

		for (Map.Entry<EEventType, List<IEventType>> eventTypeList : eventTypes.entrySet())
		{
			int frameId = 0;
			for (IEventType eventType : eventTypeList.getValue())
			{
				eventType.addEventTypeTo(labelsBuilder, frameId++);
			}
		}

		return labelsBuilder.build();
	}


	private void paintInstantaneousTypes(Map<EEventType, IEventTypeDetection> typeDetections, final ShapeMap shapes)
	{

		if (typeDetections.containsKey(EEventType.DRIBBLING))
		{
			Dribbling dribbling = (Dribbling) typeDetections.get(EEventType.DRIBBLING).getDetectedEventType();

			if (dribbling.isDribbling() && dribbling.getRobot().isPresent())
			{
				shapes.get(ELogAnalysisShapesLayer.LOG_ANALYSIS)
						.add(new DrawableArrow(dribbling.getRobot().get().getBotKickerPos(),
								Vector2.fromAngle(dribbling.getRobot().get().getOrientation())
										.scaleTo(Geometry.getBallRadius()),
								dribbling.getRobotTeam().getColor()));
			}
		}


		if (typeDetections.containsKey(EEventType.BALL_POSSESSION))
		{
			BallPossessionEventType ballPossession = (BallPossessionEventType) typeDetections
					.get(EEventType.BALL_POSSESSION).getDetectedEventType();

			boolean blueOrYellow = ballPossession.getPossessionState() == ETeamColor.BLUE ||
					ballPossession.getPossessionState() == ETeamColor.YELLOW;

			if (ballPossession.getRobot().isPresent() && blueOrYellow)
			{
				ITrackedBot bot = ballPossession.getRobot().get();
				IVector2 ballCenter = bot.getBotKickerPos()
						.addNew(Vector2.fromAngle(bot.getOrientation()).scaleTo(Geometry.getBallRadius()));
				shapes.get(ELogAnalysisShapesLayer.LOG_ANALYSIS)
						.add(new DrawableCircle(ballCenter, Geometry.getBallRadius(), bot.getTeamColor().getColor()));
			}
		}

	}


	private void paintOverviewDetections(final ShapeMap shapes)
	{
		Iterator keyIterator = eventTypeDetections.keySet().iterator();

		int i;
		for (i = 0; keyIterator.hasNext(); i++)
		{
			EEventType eventType = (EEventType) keyIterator.next();

			String info = eventType.toString() + " (size): " + eventTypes.get(eventType).size();
			DrawableBorderText labelHistoryDetection = new DrawableBorderText(Vector2.fromXY(10d, 150d + i * 10d), info,
					Color.BLACK);
			shapes.get(ELogAnalysisShapesLayer.LOG_ANALYSIS).add(labelHistoryDetection);
		}

		String infoPassing = " PASSING (size): " + eventTypes.get(EEventType.SHOT).stream()
				.filter(shot -> shot instanceof Passing).count();
		DrawableBorderText passing = new DrawableBorderText(Vector2.fromXY(10d, 150d + i * 10d), infoPassing,
				Color.BLACK);
		i++;

		String infoGoalShot = " GOAL_SHOT (size): " + eventTypes.get(EEventType.SHOT).stream()
				.filter(shot -> shot instanceof GoalShot).count();
		DrawableBorderText goalShot = new DrawableBorderText(Vector2.fromXY(10d, 150d + i * 10d), infoGoalShot,
				Color.BLACK);

		shapes.get(ELogAnalysisShapesLayer.LOG_ANALYSIS).add(passing);
		shapes.get(ELogAnalysisShapesLayer.LOG_ANALYSIS).add(goalShot);
	}
}
