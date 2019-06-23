/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Maren K�nemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.ETimable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer.SumatraTimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.AWorldPredictorImplementationBluePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BallProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BotProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.KalmanWorldFramePacker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.TrackingManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor.PredictorKey;


/**
 * This class is the core of Sumatras prediction-system. First it dispatches the incoming data, then it initiates their
 * processing. Furthermore, it handles the lifecycle of the whole module
 * 
 * @author Gero
 */
public class OracleExtKalman extends AWorldPredictorImplementationBluePrint
{
	private static final Logger				log	= Logger.getLogger(OracleExtKalman.class.getName());
	
	private SumatraTimer							timer	= null;
	private final TrackingManager				trackingManager;
	private final KalmanWorldFramePacker	packer;
	
	private final BallProcessor				ballProcessor;
	private final BotProcessor					botProcessor;
	
	private final PredictionContext			context;
	
	
	/**
	 * @param predictor
	 */
	public OracleExtKalman(final AWorldPredictor predictor)
	{
		super(PredictorKey.Kalman, predictor);
		context = new PredictionContext();
		
		packer = new KalmanWorldFramePacker(context);
		
		trackingManager = new TrackingManager(context);
		
		ballProcessor = new BallProcessor(context);
		botProcessor = new BotProcessor(context);
	}
	
	
	@Override
	public void onFacadeInitModule()
	{
	}
	
	
	@Override
	public void onFacadeStartModule()
	{
		try
		{
			timer = (SumatraTimer) SumatraModel.getInstance().getModule(ATimer.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.warn("No timer found");
		}
		startThisThread();
	}
	
	
	@Override
	public void onFacadeDeinitModule()
	{
	}
	
	
	@Override
	public void onFacadeStopModule()
	{
		stopThisThread();
	}
	
	
	@Override
	public void predict()
	{
		// get or wait for the next frame
		MergedCamDetectionFrame mergedFrame = pollLatestMergedCamFrame();
		SimpleWorldFrame wFrame = predictSimpleWorldFrame(mergedFrame);
		// Push!
		setReturnFrame(wFrame);
		pushPredictedFrameToWorldPredictor();
	}
	
	
	/**
	 * Predict a new {@link SimpleWorldFrame}
	 * 
	 * @param frame
	 * @return
	 */
	public SimpleWorldFrame predictSimpleWorldFrame(final MergedCamDetectionFrame frame)
	{
		startTime(frame.getFrameNumber());
		
		botProcessor.process(frame.getRobotsYellow(), frame.getRobotsBlue());
		ballProcessor.process(frame);
		botProcessor.performCollisionAwareLookahead();
		ballProcessor.performCollisionAwareLookahead();
		
		trackingManager.checkItems();
		SimpleWorldFrame wFrame = packer.pack(frame);
		
		stopTime(frame.getFrameNumber());
		return wFrame;
	}
	
	
	private void startTime(final long frameId)
	{
		if (timer != null)
		{
			timer.start(ETimable.WP_Kalman, frameId);
		}
	}
	
	
	private void stopTime(final long frameId)
	{
		if (timer != null)
		{
			timer.stop(ETimable.WP_Kalman, frameId);
		}
	}
}
