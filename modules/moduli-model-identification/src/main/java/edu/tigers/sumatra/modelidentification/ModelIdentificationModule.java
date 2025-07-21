/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.modelidentification;

import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.modelidentification.ball.BallObserver;
import edu.tigers.sumatra.modelidentification.kickspeed.KickSpeedObserver;
import edu.tigers.sumatra.modelidentification.movement.MovementObserver;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.wp.AWorldPredictor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;


/**
 * Identifies models of for example the ball and the kick speed by collecting samples and storing them in a database.
 * It optionally live-updates the model parameters.
 */
@Log4j2
public class ModelIdentificationModule extends AModule
{
	private static final ShapeMapSource SHAPE_MAP_SOURCE = ShapeMapSource.of("Model Identification");

	private AWorldPredictor wp;
	@Getter
	private MovementObserver movementObserver;
	@Getter
	private KickSpeedObserver kickSpeedObserver = new KickSpeedObserver();
	@Getter
	private BallObserver ballObserver = new BallObserver();


	@Override
	public void startModule()
	{
		kickSpeedObserver.start();
		BotParamsManager botParamsManager = SumatraModel.getInstance().getModule(BotParamsManager.class);
		movementObserver = new MovementObserver(botParamsManager);
		wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(movementObserver);
		wp.addObserver(kickSpeedObserver);
		SumatraModel.getInstance().getModule(ACam.class).addObserver(movementObserver);
		AVisionFilter visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
		visionFilter.getFilteredVisionFrame().subscribe(getClass().getCanonicalName(), this::onNewFilteredVisionFrame);
		visionFilter.getBallModelIdentResult()
					.subscribe(getClass().getCanonicalName(), ballObserver::onBallModelIdentificationResult);
	}


	@Override
	public void stopModule()
	{
		wp.removeObserver(movementObserver);
		wp.removeObserver(kickSpeedObserver);
		SumatraModel.getInstance().getModule(ACam.class).removeObserver(movementObserver);
		AVisionFilter visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
		visionFilter.getFilteredVisionFrame().unsubscribe(getClass().getCanonicalName());
		visionFilter.getBallModelIdentResult().unsubscribe(getClass().getCanonicalName());
		kickSpeedObserver.stop();
	}


	private void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		ShapeMap shapeMap = new ShapeMap();
		ballObserver.fillShapeMap(shapeMap);
		movementObserver.fillShapeMap(shapeMap);
		wp.notifyNewShapeMap(filteredVisionFrame.getTimestamp(), shapeMap, SHAPE_MAP_SOURCE);
	}
}
