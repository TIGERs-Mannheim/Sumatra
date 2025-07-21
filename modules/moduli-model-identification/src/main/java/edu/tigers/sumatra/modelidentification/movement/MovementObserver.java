/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.modelidentification.movement;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.ids.ETeamColor.BLUE;
import static edu.tigers.sumatra.ids.ETeamColor.YELLOW;


/**
 * Observes bot velocities and accelerations and updates the live limits accordingly.
 */
@RequiredArgsConstructor
public class MovementObserver implements IWorldFrameObserver, ICamFrameObserver
{
	private static final IShapeLayerIdentifier SHAPE_LAYER_ID = ShapeLayerIdentifier.builder().id("Bot observer")
			.layerName("Bot observer").category("Movement Observer").visibleByDefault(false).orderId(0).build();

	@Configurable(defValue = "500", comment = "Update the observed values every x world frames")
	private static int updateEvery = 500;
	@Configurable(defValue = "5", comment = "Distance between camera frames used for vel and acc determination")
	private static int frameDistance = 5;

	static
	{
		ConfigRegistration.registerClass("wp", MovementObserver.class);
	}

	private final BotParamsManager botParamsManager;

	private int waitFrames = updateEvery;

	private GameState state = GameState.HALT;
	private final Map<ETeamColor, TeamMovementObserver> teamObservers = Arrays
			.stream(ETeamColor.yellowBlueValues())
			.collect(Collectors.toMap(t -> t, t -> new TeamMovementObserver()));
	private final Map<Integer, List<CamDetectionFrame>> camFrames = new HashMap<>();


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		state = wFrameWrapper.getGameState();
		if (state.isIdleGame() && state.getState() != EGameState.HALT)
		{
			// Human adjustments possible
			teamObservers.values().forEach(TeamMovementObserver::clear);
		}

		if (--waitFrames == 0)
		{
			publish();
			waitFrames = updateEvery;
		}
	}


	@Override
	public void onNewCamDetectionFrame(CamDetectionFrame frame2)
	{
		int framesNeeded = 2 * frameDistance + 1;
		List<CamDetectionFrame> frames = camFrames.computeIfAbsent(frame2.getCameraId(),
				camId -> new ArrayList<>(framesNeeded));
		while (frames.size() >= framesNeeded)
			frames.removeFirst();

		frames.addLast(frame2);
		if (state.isIdleGame() || frames.size() < framesNeeded)
			return;

		CamDetectionFrame frame0 = frames.getFirst();
		CamDetectionFrame frame1 = frames.get(frameDistance);

		double dt1 = (frame1.getTimestamp() - frame0.getTimestamp()) * 1e-9;
		double dt2 = (frame2.getTimestamp() - frame1.getTimestamp()) * 1e-9;
		teamObservers.get(YELLOW).addSamples(state.isRunning(), dt1, dt2,
				frame0.getRobotsYellow(), frame1.getRobotsYellow(), frame2.getRobotsYellow());
		teamObservers.get(BLUE).addSamples(state.isRunning(), dt1, dt2,
				frame0.getRobotsBlue(), frame1.getRobotsBlue(), frame2.getRobotsBlue());
	}


	public void fillShapeMap(ShapeMap shapeMap)
	{
		int y = 12;
		List<IDrawableShape> shapes = shapeMap.get(SHAPE_LAYER_ID);

		for (final ETeamColor color : ETeamColor.yellowBlueValues())
		{
			EBotParamLabel label = teamToLabel(color);
			BotParams params = botParamsManager.getDatabase().getSelectedParams(label);
			BotMovementLimits limits = (BotMovementLimits) params.getMovementLimits();
			shapes.add(new DrawableBorderText(
					Vector2.fromXY(1, y++),
					String.format("MaxVel: %.1f", limits.getVelMax())
			).setColor(color.getColor()));
			shapes.add(new DrawableBorderText(
					Vector2.fromXY(1, y++),
					String.format("MaxAcc: %.1f", limits.getAccMax())
			).setColor(color.getColor()));
			shapes.add(new DrawableBorderText(
					Vector2.fromXY(1, y++),
					String.format("MaxBrk: %.1f", limits.getBrkMax())
			).setColor(color.getColor()));
		}
	}


	private void publish()
	{
		for (final ETeamColor color : ETeamColor.yellowBlueValues())
		{
			EBotParamLabel label = teamToLabel(color);
			BotParams params = botParamsManager.getDatabase().getSelectedParams(label);
			BotMovementLimits limits = (BotMovementLimits) params.getMovementLimits();
			teamObservers.get(color).publish(limits);
			botParamsManager.onEntryUpdated(botParamsManager.getDatabase().getTeamStringForLabel(label), params);
		}
	}


	private EBotParamLabel teamToLabel(ETeamColor color)
	{
		if (color == YELLOW)
		{
			return EBotParamLabel.YELLOW_LIVE;
		} else
		{
			return EBotParamLabel.BLUE_LIVE;
		}
	}
}
