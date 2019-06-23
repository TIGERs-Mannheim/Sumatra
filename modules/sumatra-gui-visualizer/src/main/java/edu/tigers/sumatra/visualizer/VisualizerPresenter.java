/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): Bernhard
 * *********************************************************
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

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.visualizer.view.EVisualizerOptions;
import edu.tigers.sumatra.visualizer.view.IFieldPanelObserver;
import edu.tigers.sumatra.visualizer.view.IRobotsPanelObserver;
import edu.tigers.sumatra.visualizer.view.VisualizerPanel;
import edu.tigers.sumatra.visualizer.view.field.EShapeLayerSource;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.ShapeMap.IShapeLayer;
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
	private static final Logger									log							= Logger
			.getLogger(
					VisualizerPresenter.class
							.getName());
	
	private static final int										VISUALIZATION_FPS			= 30;
	private ScheduledExecutorService								execService;
	private final VisualizerPanel									panel							= new VisualizerPanel();
	private final OptionsPanelPresenter							optionsPanelPresenter;
	
	
	private WorldFrameWrapper										lastWorldFrameWrapper	= null;
	private final Map<Integer, ExtendedCamDetectionFrame>	camFrames					= new ConcurrentHashMap<>();
	
	
	private BotID														selectedRobotId			= BotID.get();
	
	
	/**  */
	public static final String										BLOCKED_BY_SUMATRA		= "Blocked by Sumatra";
	
	
	/**
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
			selectedRobotId = BotID.get();
			panel.getRobotsPanel().deselectRobots();
		} else
		{
			selectedRobotId = botId;
			panel.getRobotsPanel().selectRobot(botId);
		}
		panel.repaint();
	}
	
	
	@Override
	public void onFieldClick(final IVector2 posIn, final MouseEvent e)
	{
		WorldFrameWrapper lastFrame = lastWorldFrameWrapper;
		if ((lastFrame == null) || (lastFrame.getSimpleWorldFrame() == null))
		{
			return;
		}
		
		boolean ctrl = e.isControlDown();
		boolean shift = e.isShiftDown();
		boolean rightClick = SwingUtilities.isRightMouseButton(e);
		boolean middleClick = SwingUtilities.isMiddleMouseButton(e);
		if (rightClick)
		{
			AReferee referee;
			try
			{
				referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
				IVector2 ballPos = lastFrame.getSimpleWorldFrame().getBall().getPos();
				final IVector2 pos, vel;
				if (ctrl && shift)
				{
					double dist = GeoMath.distancePP(ballPos, posIn);
					double passEndVel = 2;
					double speed = Geometry.getBallModel().getVelForDist(dist, passEndVel);
					vel = posIn.subtractNew(ballPos).scaleTo(speed);
					pos = ballPos;
				} else if (ctrl)
				{
					vel = posIn.subtractNew(ballPos).scaleTo(8);
					pos = ballPos;
				} else if (shift)
				{
					double dist = GeoMath.distancePP(ballPos, posIn);
					double passEndVel = 0;
					double speed = Geometry.getBallModel().getVelForDist(dist, passEndVel);
					vel = posIn.subtractNew(ballPos).scaleTo(speed);
					pos = ballPos;
				} else
				{
					vel = AVector2.ZERO_VECTOR;
					pos = posIn;
				}
				referee.replaceBall(new Vector3(pos, 0), new Vector3(vel, 0));
			} catch (ModuleNotFoundException err)
			{
				log.error("Referee module not found.", err);
			}
			return;
		}
		
		if (middleClick)
		{
			AWorldPredictor wp;
			try
			{
				wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
				wp.setLatestBallPosHint(posIn);
			} catch (ModuleNotFoundException err)
			{
				log.error("Could not find WP module!");
			}
			return;
		}
		
		onRobotSelected(posIn, e);
	}
	
	
	/**
	 * @param posIn
	 * @param e
	 */
	public void onRobotSelected(final IVector2 posIn, final MouseEvent e)
	{
	}
	
	
	/**
	 * Start view
	 */
	public void start()
	{
		log.trace("Start");
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
		// execService.scheduleAtFixedRate(new Updater(), 0, 1000 / VISUALIZATION_FPS, TimeUnit.MILLISECONDS);
		execService.execute(new UpdaterSelfSchedule());
		log.trace("Started");
	}
	
	
	/**
	 * 
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
				execService.awaitTermination(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException err)
			{
				log.error("Timed out waiting for update thread shutdown...");
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
				try
				{
					AWorldPredictor predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(
							AWorldPredictor.MODULE_ID);
					predictor.addWorldFrameConsumer(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get WP module");
				}
				
				GlobalShortcuts.register(EShortcut.RESET_FIELD,
						(() -> panel.getFieldPanel().onOptionChanged(EVisualizerOptions.RESET_FIELD, true)));
				
				start();
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				try
				{
					AWorldPredictor predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(
							AWorldPredictor.MODULE_ID);
					predictor.removeWorldFrameConsumer(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get WP module");
				}
				
				GlobalShortcuts.unregisterAll(EShortcut.RESET_FIELD);
				
				stop();
				break;
			default:
				break;
			
		}
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
				updateCamFrameShapes();
				updateVisFrameShapes();
				panel.getFieldPanel().paintOffline();
				
				updateRobotsPanel();
				panel.getRobotsPanel().repaint();
			} catch (Throwable e)
			{
				log.error("Exception in visualizer updater", e);
			}
		}
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
	
	
	protected void updateVisFrameShapes()
	{
		WorldFrameWrapper worldFrame = lastWorldFrameWrapper;
		if (worldFrame == null)
		{
			panel.getFieldPanel().clearField(EShapeLayerSource.WP);
		} else
		{
			for (IShapeLayer sl : worldFrame.getShapeMap().getAllShapeLayers())
			{
				panel.getOptionsMenu().addMenuEntry(sl);
			}
			panel.getFieldPanel().setShapeMap(EShapeLayerSource.WP, worldFrame.getShapeMap(), false);
		}
	}
	
	
	private void updateCamFrameShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		List<ExtendedCamDetectionFrame> mergedFrames = new ArrayList<>(camFrames.values());
		mergedFrames.sort((a, b) -> Long.compare(b.getBall().gettCapture(), a.getBall().gettCapture()));
		for (ExtendedCamDetectionFrame mergedCamFrame : mergedFrames)
		{
			assert mergedCamFrame != null;
			for (CamRobot bot : mergedCamFrame.getRobotsBlue())
			{
				DrawableBotShape botShape = new DrawableBotShape(bot.getPos(), bot.getOrientation(),
						Geometry.getBotRadius(), Geometry.getCenter2DribblerDistDefault());
				botShape.setFill(false);
				botShape.setColor(Color.BLUE);
				botShape.setId(String.valueOf(bot.getRobotID()));
				shapes.add(botShape);
			}
			
			for (CamRobot bot : mergedCamFrame.getRobotsYellow())
			{
				DrawableBotShape botShape = new DrawableBotShape(bot.getPos(), bot.getOrientation(),
						Geometry.getBotRadius(), Geometry.getCenter2DribblerDistDefault());
				botShape.setFill(false);
				botShape.setColor(Color.YELLOW);
				botShape.setId(String.valueOf(bot.getRobotID()));
				shapes.add(botShape);
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
		panel.getFieldPanel().setShapeMap(EShapeLayerSource.CAM, shapeMap, false);
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
				status.setVisible(tBot.isVisible());
			}
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
	
	
	private static class VisionLayer implements IShapeLayer
	{
		
		@Override
		public String getCategory()
		{
			return "VISION";
		}
		
		
		@Override
		public String getLayerName()
		{
			return "vision";
		}
		
		
		@Override
		public int hashCode()
		{
			return getId().hashCode();
		}
		
		
		@Override
		public String getId()
		{
			return VisionLayer.class.getCanonicalName() + getLayerName();
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (obj == null)
			{
				return false;
			}
			if (obj.getClass().equals(obj.getClass()))
			{
				return ((VisionLayer) obj).getId().equals(getId());
			}
			return false;
		}
	}
	
	
	/**
	 * @return the selectedRobotId
	 */
	public final BotID getSelectedRobotId()
	{
		return selectedRobotId;
	}
	
	
	/**
	 * @return the lastWorldFrameWrapper
	 */
	public final WorldFrameWrapper getLastWorldFrameWrapper()
	{
		return lastWorldFrameWrapper;
	}
	
	
	/**
	 * @return the panel
	 */
	public final VisualizerPanel getPanel()
	{
		return panel;
	}
}
