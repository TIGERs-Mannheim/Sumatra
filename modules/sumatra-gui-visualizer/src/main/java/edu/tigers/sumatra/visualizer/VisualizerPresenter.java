/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory.FixedLossPlusRollingParameters;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingConsultant;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Presenter for the visualizer.
 * <p>
 * NOTE: The fact that the view stores the actual state is not MVP-conform, but that would need greater refactoring and
 * I don't have time for this now
 * </p>
 *
 * @author Bernhard, (Gero)
 */
public class VisualizerPresenter extends ASumatraViewPresenter implements IRobotsPanelObserver, IFieldPanelObserver,
		IWorldFrameObserver
{
	private static final Logger log = Logger.getLogger(VisualizerPresenter.class.getName());

	private static final int VISUALIZATION_FPS = 24;

	private final VisualizerPanel panel = new VisualizerPanel();
	private final OptionsPanelPresenter optionsPanelPresenter;
	private final Map<Integer, ExtendedCamDetectionFrame> camFrames = new ConcurrentHashMap<>();
	private ScheduledExecutorService execService;
	private WorldFrameWrapper lastWorldFrameWrapper = null;
	private BotID selectedRobotId = BotID.noBot();
	private final Map<String, ShapeMap> shapeMaps = new ConcurrentHashMap<>();


	/**
	 * Default
	 */
	public VisualizerPresenter()
	{
		optionsPanelPresenter = new OptionsPanelPresenter(panel.getFieldPanel());
		panel.getOptionsMenu().addMenuEntry(new VisionLayer());
	}


	@Override
	public void onRobotClick(final BotID botId)
	{
		// --- select/deselect item ---
		if (selectedRobotId.equals(botId))
		{
			selectedRobotId = BotID.noBot();
			panel.getRobotsPanel().deselectRobots();
		} else
		{
			selectedRobotId = botId;
			panel.getRobotsPanel().selectRobot(botId);
		}
	}


	private IVector3 doChipKick(final IVector2 posIn, final double dist, final int numTouchdown)
	{
		double kickSpeed = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getChipConsultant()
				.getInitVelForDistAtTouchdown(dist, numTouchdown);
		IVector2 kickVector = new FixedLossPlusRollingConsultant(new FixedLossPlusRollingParameters())
				.absoluteKickVelToVector(kickSpeed);
		IVector2 ballPos = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
		return Vector3.from2d(posIn.subtractNew(ballPos).scaleTo(kickVector.x()), kickVector.y());
	}


	private IVector3 doStraightKick(final IVector2 posIn, final double dist, final double passEndVel)
	{
		double speed = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getStraightConsultant().getInitVelForDist(
				dist,
				passEndVel);
		IVector2 ballPos = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
		return Vector3.from2d(posIn.subtractNew(ballPos).scaleTo(speed), 0);
	}


	private void handleBall(final IVector2 posIn, final MouseEvent e, final AVisionFilter referee)
	{
		IVector2 ballPos = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
		double dist = VectorMath.distancePP(ballPos, posIn);
		IVector2 pos = ballPos;
		final IVector3 vel;

		boolean ctrl = e.isControlDown();
		boolean shift = e.isShiftDown();
		boolean alt = e.isAltDown();

		if (ctrl && shift)
		{
			if (alt)
			{
				vel = doChipKick(posIn, dist, 2);
			} else
			{
				vel = doStraightKick(posIn, dist, 2);
			}
		} else if (ctrl)
		{
			vel = Vector3.from2d(posIn.subtractNew(ballPos).scaleTo(RuleConstraints.getMaxBallSpeed() - 0.001), 0);
		} else if (shift)
		{
			if (alt)
			{
				vel = doChipKick(posIn, dist, 0);
			} else
			{
				vel = doStraightKick(posIn, dist, 0);
			}
		} else
		{
			vel = Vector3f.ZERO_VECTOR;
			pos = posIn;
		}
		referee.placeBall(Vector3.from2d(pos, 0), vel.multiplyNew(1e3));
	}


	@Override
	public void onFieldClick(final IVector2 posIn, final MouseEvent e)
	{
		AVisionFilter visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);

		boolean rightClick = SwingUtilities.isRightMouseButton(e);
		boolean middleClick = SwingUtilities.isMiddleMouseButton(e);

		if (rightClick)
		{
			handleBall(posIn, e, visionFilter);
		} else if (middleClick)
		{
			visionFilter.resetBall(Vector3.from2d(posIn, 0), Vector3f.ZERO_VECTOR);
		} else
		{
			onRobotSelected(posIn, e);
		}
	}


	/**
	 * @param posIn
	 * @param e
	 */
	public void onRobotSelected(final IVector2 posIn, final MouseEvent e)
	{
		// overwritten by sub-class
	}


	/**
	 * Start view
	 */
	public void start()
	{
		panel.getFieldPanel().start();

		// --- register on robotspanel as observer ---
		panel.getRobotsPanel().addObserver(this);

		// --- register on fieldpanel as observer ---
		panel.getFieldPanel().addObserver(this);

		// --- register on optionspanel as observer ---
		panel.getOptionsMenu().addObserver(optionsPanelPresenter);

		panel.getRobotsPanel().clearView();
		panel.getOptionsMenu().setInitialButtonState();
		panel.getOptionsMenu().setButtonsEnabled(true);
		panel.getFieldPanel().setPanelVisible(true);

		execService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("VisualizerUpdater"));
		execService.execute(new UpdaterSelfSchedule());
	}


	/**
	 * Remove observers, stop updater clear panels
	 */
	public void stop()
	{
		panel.getFieldPanel().stop();

		// --- register on robotspanel as observer ---
		panel.getRobotsPanel().removeObserver(this);

		// --- register on fieldpanel as observer ---
		panel.getFieldPanel().removeObserver(this);

		// --- register on optionspanel as observer ---
		panel.getOptionsMenu().removeObserver(optionsPanelPresenter);

		if (execService != null)
		{
			execService.shutdownNow();
			try
			{
				Validate.isTrue(execService.awaitTermination(100, TimeUnit.MILLISECONDS));
			} catch (InterruptedException err)
			{
				log.error("Interrupted while awaiting termination", err);
				Thread.currentThread().interrupt();
			}
		}

		panel.getRobotsPanel().clearView();
		panel.getFieldPanel().setPanelVisible(false);
		panel.getFieldPanel().clearField();
	}


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				init();
				start();
				break;
			case RESOLVED:
				deinit();
				stop();
				break;
			case NOT_LOADED:
			default:
				break;
		}
	}


	private void deinit()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.removeObserver(this);

		GlobalShortcuts.unregisterAll(EShortcut.RESET_FIELD);
	}


	private void init()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(this);

		GlobalShortcuts.register(EShortcut.RESET_FIELD,
				() -> panel.getFieldPanel().onOptionChanged(EVisualizerOptions.RESET_FIELD, true));
	}


	@Override
	public Component getComponent()
	{
		return panel;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return panel;
	}


	@Override
	public void onNewShapeMap(final long timestamp, final ShapeMap shapeMap, final String source)
	{
		shapeMaps.put(source, shapeMap);
	}


	@Override
	public void onClearShapeMap(final String source)
	{
		shapeMaps.put(source, new ShapeMap());
	}


	public OptionsPanelPresenter getOptionsPanelPresenter()
	{
		return optionsPanelPresenter;
	}


	protected void updateRobotsPanel()
	{
		WorldFrameWrapper lastFrame = lastWorldFrameWrapper;
		if (lastFrame != null)
		{
			IBotIDMap<ITrackedBot> tBotsMap = lastFrame.getSimpleWorldFrame().getBots();
			for (ITrackedBot tBot : tBotsMap.values())
			{
				BotStatus status = panel.getRobotsPanel().getBotStatus(tBot.getBotId());
				status.setVisible(tBot.getFilteredState().isPresent());
			}
			Map<BotID, BotStatus> botStati = panel.getRobotsPanel().getBotStati();
			// set all bots invisible that are not tracked any longer
			for (Map.Entry<BotID, BotStatus> status : botStati.entrySet())
			{
				if (!tBotsMap.containsKey(status.getKey()))
				{
					status.getValue().setVisible(false);
				}
			}
			panel.getRobotsPanel().updateBotStati();
		}
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		lastWorldFrameWrapper = wfWrapper;
	}


	@Override
	public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		camFrames.put(frame.getCameraId(), frame);
	}


	@Override
	public void onClearWorldFrame()
	{
		lastWorldFrameWrapper = null;
		panel.getRobotsPanel().clearView();
	}


	@Override
	public void onClearCamDetectionFrame()
	{
		camFrames.clear();
	}


	/**
	 * @return the selectedRobotId
	 */
	protected final BotID getSelectedRobotId()
	{
		return selectedRobotId;
	}


	/**
	 * @return the panel
	 */
	public final VisualizerPanel getPanel()
	{
		return panel;
	}

	private class UpdaterSelfSchedule extends Updater
	{
		@Override
		public void run()
		{
			while (!Thread.interrupted())
			{
				long t0 = System.nanoTime();
				super.run();
				long t1 = System.nanoTime();
				long sleep = (1_000_000_000L / VISUALIZATION_FPS) - (t1 - t0);
				if (sleep > 0)
				{
					ThreadUtil.parkNanosSafe(sleep);
				}
			}
		}
	}

	/**
	 * update new worldframes in a fixed interval.
	 */
	private class Updater implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				update();
			} catch (Throwable e)
			{
				log.error("Exception in visualizer updater", e);
			}
		}


		private void update()
		{
			shapeMaps.forEach(this::newShapeMap);
			updateCamFrameShapes();
			panel.getFieldPanel().paintOffline();
			updateRobotsPanel();
		}


		private void newShapeMap(final String source, final ShapeMap shapeMap)
		{
			panel.getOptionsMenu().addSourceMenuIfNotPresent(source);
			for (IShapeLayer sl : shapeMap.getAllShapeLayersIdentifiers())
			{
				panel.getOptionsMenu().addMenuEntry(sl);
			}
			panel.getFieldPanel().setShapeMap(source, shapeMap);
		}


		private void updateCamFrameShapes()
		{
			List<IDrawableShape> shapes = new ArrayList<>();
			List<ExtendedCamDetectionFrame> mergedFrames = new ArrayList<>(camFrames.values());
			mergedFrames.sort((a, b) -> Long.compare(b.getBall().gettCapture(), a.getBall().gettCapture()));
			for (ExtendedCamDetectionFrame mergedCamFrame : mergedFrames)
			{
				for (CamRobot bot : mergedCamFrame.getRobotsBlue())
				{
					shapes.add(createDrawableShape(bot, Color.BLUE));
				}

				for (CamRobot bot : mergedCamFrame.getRobotsYellow())
				{
					shapes.add(createDrawableShape(bot, Color.YELLOW));
				}

				Color ballsColor = new Color(50, 100, 20);
				for (CamBall ball : mergedCamFrame.getBalls())
				{
					DrawableCircle ballCircle = new DrawableCircle(ball.getPos().getXYVector(), Geometry
							.getBallRadius(), ballsColor);
					ballCircle.setFill(true);
					shapes.add(ballCircle);
				}
			}

			if (!mergedFrames.isEmpty())
			{
				Color ballColor = new Color(50, 100, 200);
				double age = (mergedFrames.get(0).gettCapture() - mergedFrames.get(0).getBall().gettCapture()) / 1e9;
				double size = ((Geometry.getBallRadius() - 5) * (1 - Math.min(1, Math.max(0, age / 0.2)))) + 5;
				DrawableCircle ballCircle = new DrawableCircle(mergedFrames.get(0).getBall().getPos().getXYVector(),
						size, ballColor);
				ballCircle.setFill(true);
				shapes.add(ballCircle);
			}

			ShapeMap shapeMap = new ShapeMap();
			IShapeLayer visionLayer = new VisionLayer();
			shapeMap.get(visionLayer).addAll(shapes);
			long timestamp = mergedFrames.stream().map(ExtendedCamDetectionFrame::gettCapture).findFirst().orElse(0L);
			onNewShapeMap(timestamp, shapeMap, "CAM");
		}


		private DrawableBotShape createDrawableShape(final CamRobot bot, final Color color)
		{
			DrawableBotShape botShape = new DrawableBotShape(bot.getPos(), bot.getOrientation(),
					Geometry.getBotRadius(), 75);
			botShape.setFillColor(null);
			botShape.setBorderColor(color);
			botShape.setFontColor(Color.white);
			botShape.setId(String.valueOf(bot.getRobotID()));
			return botShape;
		}
	}
}
