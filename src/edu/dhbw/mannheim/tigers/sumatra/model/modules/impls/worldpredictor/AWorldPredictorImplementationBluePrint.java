/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor.PredictorKey;


/**
 * Abstract class for a WorldPredictor Implementation. This base-class is used in the WP-Facade.
 * 
 * @author KaiE
 */
public abstract class AWorldPredictorImplementationBluePrint
{
	private static final Logger					log			= Logger
																					.getLogger(AWorldPredictorImplementationBluePrint.class
																							.getName());
	
	private BlockingDeque<SimpleWorldFrame>	returnFrame	= new LinkedBlockingDeque<SimpleWorldFrame>(1);
	
	private static AtomicLong						nextFrameId	= new AtomicLong(0);
	
	private AWorldPredictor							worldPredictorInstance;
	private final PredictorKey						predictor;
	
	private boolean									running		= false;
	
	private Thread										processingThread;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param thisImpl
	 * @param worldPredictor
	 */
	public AWorldPredictorImplementationBluePrint(final PredictorKey thisImpl, final AWorldPredictor worldPredictor)
	{
		predictor = thisImpl;
		worldPredictorInstance = worldPredictor;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This method is to set the predicted Frame pushed by push pushPredictedFrameToWorldPredictor
	 * 
	 * @frame
	 */
	protected final void setReturnFrame(final SimpleWorldFrame frame)
	{
		try
		{
			returnFrame.clear();
			returnFrame.put(frame);
		} catch (InterruptedException err)
		{
			log.warn("interrupted while putting predicted Frame");
		}
	}
	
	
	/**
	 * This Method can be used by the Implementations to get the next frame-number
	 */
	protected long getNextFrameNumber()
	{
		return nextFrameId.get();
	}
	
	
	/**
	 * This Method returns the calculated SimpleWorldFrame for the CamDetectionFrame. If
	 * it is null the previous Frame is returned.
	 * 
	 * @return SimpleWorldFrame
	 */
	public final SimpleWorldFrame getPredictedWorldFrame()
	{
		return returnFrame.peekLast();
	}
	
	
	/**
	 * This Method should be called when a SimpleWorldFrame is predicted.
	 * It pushes the prediction to the AWorldPredictor implementation, if it is set.
	 * The static FrameCounter is set to the next value of the pushed Frame
	 */
	protected final void pushPredictedFrameToWorldPredictor()
	{
		final SimpleWorldFrame tmp = getPredictedWorldFrame();
		nextFrameId.set(tmp.getId() + 1);
		worldPredictorInstance.onPutPredictedWorldFrame(tmp, predictor);
	}
	
	
	/**
	 * returns the last merged {@link CamDetectionFrame}. If no new frame is available null is returned.
	 * 
	 * @return
	 *         CamDetectionFrame
	 */
	protected final MergedCamDetectionFrame pollLatestMergedCamFrame()
	{
		return worldPredictorInstance.pollNewCamFrame(predictor);
	}
	
	
	/**
	 * Method to start the thread of this implementation
	 */
	protected final void startThisThread()
	{
		running = true;
		processingThread = new Thread((() -> mainloop()), "WP_impl_" + predictor.toString());
		processingThread.start();
	}
	
	
	/**
	 * Method to stop the thread of this implementation.
	 * Note that the current calculation have to be completed before the
	 * thread ends
	 */
	protected final void stopThisThread()
	{
		running = false;
	}
	
	
	private final void mainloop()
	{
		while (running)
		{
			try
			{
				predict();
			} catch (Exception e)
			{
				log.error("caught unhandled error in Worldpredictor Thread: " + predictor.toString(), e);
				
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods to @Override--------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Predict the next SimpleWorldFrame for this CamDetnFrame and store it in
	 * the returnFrame.
	 */
	protected abstract void predict();
	
	
	/**
	 * This method is called from the WPFacede Object when it is initialised.
	 */
	public abstract void onFacadeInitModule();
	
	
	/**
	 * This method is called from the WPFacade Object when it is de-initialised.
	 */
	public abstract void onFacadeDeinitModule();
	
	
	/**
	 * This method is called from the WPFacade Object when it is started.
	 */
	public abstract void onFacadeStartModule();
	
	
	/**
	 * This method is called from the WPFacade Object when it is stopped.
	 */
	public abstract void onFacadeStopModule();
}
