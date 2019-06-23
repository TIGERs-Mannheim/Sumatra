/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.05.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BallCorrector;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This class is to replace the SyncedCamFrameBuffer. It now merged the frames itself and does not just store
 * them.
 * The merged frame is pushed to a consumer defined in the Constructor.
 * 
 * @author KaiE
 */
public class SyncedCamFrameBufferV2 implements Runnable
{
	@SuppressWarnings("unused")
	private static final Logger							log								= Logger
																												.getLogger(SyncedCamFrameBufferV2.class
																														.getName());
	
	@Configurable(comment = "possible keys: Q1,Q2,Q3,Q4,Q12,Q13,Q14,Q23,Q24,Q34,Q1234", spezis = {
			"LAB", "SIM" })
	private static MathematicalQuadrants[]				camToQuadrantAssociation	= { MathematicalQuadrants.Q1234 };
	@Configurable(spezis = { "LAB", "SIM" })
	private static double									intersectionEpsilon			= 0.0;
	
	private final BlockingDeque<CamDetectionFrame>	buffer							= new LinkedBlockingDeque<>(10);
	private final IMergedCamFrameConsumer				consumer;
	private final BallCorrector							ballCorrector					= new BallCorrector();
	
	private static long										nextFrameNumber				= 0;
	private static int										numCams							= 0;
	
	private Thread												thread;
	
	@Configurable(comment = "P1 of rectangle defining a range where objects are ignored")
	private static IVector2									exclusionRectP1				= Vector2.ZERO_VECTOR;
	@Configurable(comment = "P2 of rectangle defining a range where objects are ignored")
	private static IVector2									exclusionRectP2				= Vector2.ZERO_VECTOR;
	
	
	/**
	 * Quadrants for the association with the camera-ID. All required permutations
	 * 
	 * <pre>
	 * Axis:y^\n
	 *  _____|_____
	 * |__2__|__1__|__> Axis:x
	 * |__3__|__4__|
	 * </pre>
	 * 
	 * @author KaiE
	 */
	public enum MathematicalQuadrants
	{
		/**  */
		Q1,
		/**  */
		Q2,
		/**  */
		Q3,
		/**  */
		Q4,
		/**  */
		Q12,
		/**  */
		Q14,
		/**  */
		Q23,
		/**  */
		Q34,
		/**  */
		Q1234
	}
	
	
	/**
	 * Constructor to specify the Cams by Id and to set the consumer of the produced camframe.
	 * 
	 * @param cnsmr
	 */
	public SyncedCamFrameBufferV2(final IMergedCamFrameConsumer cnsmr)
	{
		consumer = cnsmr;
		thread = new Thread(this, "WP_Merge");
		thread.start();
		
	}
	
	
	private boolean isWithinQuadrant(final float x, final float y, final MathematicalQuadrants quad)
	{
		switch (quad)
		{
			case Q1: // x positive; y positive
				if ((x >= -intersectionEpsilon) && (y >= -intersectionEpsilon))
				{
					return true;
				}
				break;
			case Q2: // x negative; y positive
				if ((x <= intersectionEpsilon) && (y >= -intersectionEpsilon))
				{
					return true;
				}
				break;
			case Q3: // x negative; y negative
				if ((x <= intersectionEpsilon) && (y <= intersectionEpsilon))
				{
					return true;
				}
				break;
			case Q4: // x positive; y negative
				if ((x >= -intersectionEpsilon) && (y <= intersectionEpsilon))
				{
					return true;
				}
				break;
			case Q12:// y positive
				if (y >= -intersectionEpsilon)
				{
					return true;
				}
				break;
			case Q14:// x positive
				if ((x >= -intersectionEpsilon))
				{
					return true;
				}
				break;
			case Q23:// x negative
				if ((x <= intersectionEpsilon))
				{
					return true;
				}
				break;
			case Q34:// y negative
				if (y <= intersectionEpsilon)
				{
					return true;
				}
				break;
			case Q1234:// do always when one cam
				return true;
		}
		return false;
	}
	
	
	private boolean isWithinExclusionRectangle(final IVector2 p)
	{
		Rectangle rect = new Rectangle(exclusionRectP1, exclusionRectP2);
		return rect.isPointInShape(p, -0.00001f);
	}
	
	
	private MathematicalQuadrants getQuadrant(final int camId)
	{
		if (camId >= camToQuadrantAssociation.length)
		{
			return MathematicalQuadrants.Q1234;
		}
		return camToQuadrantAssociation[camId];
	}
	
	
	private List<CamRobot> filterOldBots(final List<CamRobot> oldBots, final List<CamRobot> incomingBots, final int
			camId)
	{
		List<CamRobot> newBots = new ArrayList<>(oldBots);
		for (CamRobot newBot : incomingBots)
		{
			if (!isWithinQuadrant(newBot.getPos().x(), newBot.getPos().y(), getQuadrant(camId)))
			{
				continue;
			}
			if (isWithinExclusionRectangle(newBot.getPos()))
			{
				continue;
			}
			for (CamRobot oldBot : oldBots)
			{
				if ((newBot.getRobotID() == oldBot.getRobotID())
						&& (oldBot.getTimestamp() < newBot.getTimestamp()))
				{
					newBots.remove(oldBot);
				}
			}
			newBots.add(newBot);
		}
		return newBots;
	}
	
	
	private List<CamBall> filterOldBalls(final List<CamBall> old, final List<CamBall> incoming, final int camId)
	{
		List<CamBall> newBalls = new ArrayList<>(old);
		for (CamBall newBall : incoming)
		{
			if (isWithinExclusionRectangle(newBall.getPos().getXYVector()))
			{
				continue;
			}
			for (CamBall oldBall : old)
			{
				if (oldBall.getTimestamp() < newBall.getTimestamp())
				{
					newBalls.remove(oldBall);
				}
			}
			newBalls.add(newBall);
		}
		return newBalls;
	}
	
	
	/**
	 * This method is called by the ssl-vision Thread indirectly through the onNewCamDetectionFrame. If the frame is
	 * relevant then the merge method is called else the frame is discarded.
	 * The ssl-vision thread is relieved by using just the buffer and run the merge asynchronously.
	 * 
	 * @param frame
	 */
	public final void newCamDetectionFrame(final CamDetectionFrame frame)
	{
		numCams = Math.max(numCams, frame.getCameraId() + 1);
		if (buffer.remainingCapacity() == 0)
		{
			buffer.pollFirst();
		}
		buffer.offer(frame);
	}
	
	
	/**
	 * Use to stop the service. Avoids multiple Threads
	 */
	public final void stopService()
	{
		thread.interrupt();
		buffer.clear();
	}
	
	
	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			final CamDetectionFrame frame;
			try
			{
				frame = buffer.pollFirst(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException err)
			{
				log.info("Disconnected...");
				return;
			}
			if (frame == null)
			{
				// silently continue...
				continue;
			}
			
			// pass list instead of empty list to have a merged frame
			List<CamRobot> yellowBots = filterOldBots(Collections.emptyList(), frame.getRobotsYellow(),
					frame.getCameraId());
			List<CamRobot> blueBots = filterOldBots(Collections.emptyList(), frame.getRobotsBlue(), frame.getCameraId());
			List<CamBall> balls = filterOldBalls(Collections.emptyList(), frame.getBalls(), frame.getCameraId());
			CamBall ball = ballCorrector.correctBall(balls, yellowBots, blueBots);
			MergedCamDetectionFrame mergedFrame = new MergedCamDetectionFrame(getNextFrameNumber(), balls,
					yellowBots,
					blueBots,
					ball);
			consumer.notifyNewSyncedCamFrame(mergedFrame);
		}
	}
	
	
	private long getNextFrameNumber()
	{
		return nextFrameNumber++;
	}
	
	
	/**
	 * returns configurable
	 * 
	 * @return
	 */
	public static double getIntersectionEpsilon()
	{
		return intersectionEpsilon;
	}
	
	
	/**
	 * returns configurable
	 * 
	 * @return
	 */
	public static MathematicalQuadrants[] getCamToQuadrantAssociation()
	{
		return camToQuadrantAssociation;
	}
	
	
	/**
	 * @return
	 */
	public static int getNumCams()
	{
		return numCams;
	}
	
	
	/**
	 * @param pos
	 */
	public void setLatestBallPosHint(final IVector2 pos)
	{
		ballCorrector.setLatestBallPos(pos);
	}
}
