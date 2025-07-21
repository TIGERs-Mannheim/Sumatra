/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.observer.EventDistributor;
import edu.tigers.sumatra.observer.EventSubscriber;
import edu.tigers.sumatra.observer.FrameDistributor;
import edu.tigers.sumatra.observer.FrameSubscriber;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.Viewport;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Module for processing raw vision data.
 */
@Log4j2
public abstract class AVisionFilter extends AModule implements ICamFrameObserver
{
	private final FrameDistributor<FilteredVisionFrame> filteredVisionFrame = new FrameDistributor<>();
	private final FrameDistributor<Viewport> viewportFrame = new FrameDistributor<>();
	private final EventDistributor<IBallModelIdentResult> ballModelIdentResult = new EventDistributor<>();

	@Getter
	@Setter
	private Map<BotID, RobotInfo> robotInfoMap = new HashMap<>();

	@Setter
	private IBallPlacer ballPlacer;


	/**
	 * Update filter with a new camera detection frame from one camera
	 *
	 * @param camDetectionFrame detections from a single camera frame
	 */
	protected void updateCamDetectionFrame(CamDetectionFrame camDetectionFrame)
	{
	}


	/**
	 * Send a complete and filtered vision frame to external modules
	 *
	 * @param filteredVisionFrame the filtered and complete vision frame
	 */
	public final void publishFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		this.filteredVisionFrame.newFrame(filteredVisionFrame);
	}


	/**
	 * Send an updated viewport to external modules.
	 *
	 * @param viewport
	 */
	protected final void publishUpdatedViewport(Viewport viewport)
	{
		viewportFrame.newFrame(viewport);
	}


	/**
	 * Send an identified ball model.
	 *
	 * @param ident
	 */
	protected final void publishBallModelIdentification(final IBallModelIdentResult ident)
	{
		ballModelIdentResult.newEvent(ident);
	}


	/**
	 * Reset the ball to a new position. Can be used to
	 * select another ball, if multiple balls are detected (real filter)
	 *
	 * @param pos where the ball should be reset to
	 * @param vel
	 */
	public void resetBall(final IVector3 pos, final IVector3 vel)
	{
	}


	/**
	 * Place the ball to a new position. This should only be implemented by the simulator
	 *
	 * @param pos [mm]
	 * @param vel [mm/s]
	 */
	public final void placeBall(final IVector3 pos, final IVector3 vel)
	{
		if (ballPlacer != null)
		{
			ballPlacer.placeBall(pos, vel);
		}
	}


	@Override
	public void stopModule()
	{
		onClearCamFrame();
	}


	@Override
	public void deinitModule()
	{
		filteredVisionFrame.clear();
		viewportFrame.clear();
		ballModelIdentResult.clear();
	}


	@Override
	public final void onNewCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		try
		{
			updateCamDetectionFrame(camDetectionFrame);
		} catch (Throwable e)
		{
			log.error("Error during cam detection processing", e);
		}
	}


	@Override
	public void onClearCamFrame()
	{
		robotInfoMap = new HashMap<>();
		filteredVisionFrame.clearFrame();
		viewportFrame.clearFrame();
	}


	public FrameSubscriber<FilteredVisionFrame> getFilteredVisionFrame()
	{
		return filteredVisionFrame;
	}


	public FrameSubscriber<Viewport> getViewport()
	{
		return viewportFrame;
	}


	public EventSubscriber<IBallModelIdentResult> getBallModelIdentResult()
	{
		return ballModelIdentResult;
	}
}
