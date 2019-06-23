/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 15, 2011
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamCalibration;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Altigraph;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.FlyingBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.FramePacker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.LinFunc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;


/**
 * This Class is to correct the ssl-ballPosition from bottom to fly
 * 
 * @author Birgit
 * 
 */
public class BallCorrector implements ICamGeomObserver
{
	// Logger
	private static final Logger	log				= Logger.getLogger(BallCorrector.class.getName());
	
	/** */
	private Altigraph					m_a;
	
	private CamRobot					mBot;
	private double						mAngleDistance	= Def.DUMMY;
	private FramePacker				packer			= null;
	
	private CamBall					lastSeenBall;
	
	private long						lastSeenBallTimestamp;
	
	
	/**
	 */
	public BallCorrector()
	{
		m_a = new Altigraph();
		
		ACam cam;
		try
		{
			cam = (ACam) SumatraModel.getInstance().getModule("cam");
			cam.addCamGeometryObserver(this);
		} catch (final ModuleNotFoundException err)
		{
			log.error("ModuleNotFoundException", err);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * correct the ball frame, if ball is flying
	 * @param frame
	 * @return
	 */
	public CamDetectionFrame correctFrame(CamDetectionFrame frame)
	{
		if (Def.DEBUG_CAM)
		{
			if (frame.balls.size() > 0)
			{
				log.info("Ball: " + frame.cameraId + "..." + frame.balls.get(0).pos.x() + ":" + frame.balls.get(0).pos.y());
			}
			// return frame;
		}
		
		packer = new FramePacker(frame);
		
		if (Def.DEBUG)
		{
			log.info("# Frame " + frame.frameNumber);
		}
		
		if (frame.balls.size() > 0)
		{
			CamBall ballToUse = frame.balls.get(0);
			if (lastSeenBall != null)
			{
				float shortestDifference = difference(ballToUse, lastSeenBall);
				for (CamBall ball : frame.balls)
				{
					if (difference(ball, lastSeenBall) < shortestDifference)
					{
						ballToUse = ball;
						shortestDifference = difference(ball, lastSeenBall);
					}
				}
				frame.balls.clear();
				if (difference(lastSeenBall, ballToUse) > (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()
						- lastSeenBallTimestamp) * 10))
				{
					frame.balls.add(lastSeenBall);
				} else
				{
					frame.balls.add(ballToUse);
					lastSeenBall = new CamBall(frame.balls.get(0));
					lastSeenBallTimestamp = System.nanoTime();
				}
			} else
			{
				lastSeenBall = new CamBall(frame.balls.get(0));
				lastSeenBallTimestamp = System.nanoTime();
			}
		}
		
		
		// if ball not inside the frame, return the origin frame
		if (!Def.isParametersSet)
		{
			if (Def.DEBUG)
			{
				log.info("Parameter noch nicht gesetzt oder kein Ball im Frame");
			}
			return packer.getOldFrame();
		}
		
		if (frame.balls.size() < 1)
		{
			if (Def.DEBUG)
			{
				log.info("Kein Ball im Frame");
			}
			return packer.getOldFrame();
		}
		
		// get Data of Ball and Bots
		final CamBall bottomBall = packer.getBall();
		final List<CamRobot> bots = packer.getBotList();
		
		
		// if kick possible, add new fly
		if (kickPossibleDetected(bottomBall, bots))
		{
			if (Def.DEBUG)
			{
				log.info("Corr: Flug erg�nzt");
			}
			// append new fly
			m_a.addKickerZoneIdentified(mBot.pos.x(), mBot.pos.y(), mBot.orientation);
		}
		
		
		// append the ball to old and new flys
		m_a.addCamFrame(bottomBall.pos.x(), bottomBall.pos.y(), frame.cameraId);
		// log.info("Corr: Ball erg�nzt");
		
		// if ball is flying
		if (m_a.isBallFlying())
		{
			if (Def.DEBUG)
			{
				log.info("Corr: Ball fliegt: " + m_a.getCorrectedFrame().z());
				// correct the ball-data and give new frame back
			}
			
			
			packer.setNewCamBall((float) m_a.getCorrectedFrame().x(), (float) m_a.getCorrectedFrame().y(), (float) m_a
					.getCorrectedFrame().z());
			
		}
		// give old frame back
		else
		{
			if (Def.DEBUG)
			{
				log.info("Boden");
			}
		}
		
		printData();
		return packer.getMaybeCorrectedFrame();
	}
	
	
	private float difference(CamBall ball1, CamBall ball2)
	{
		return ball1.pos.subtractNew(ball2.pos).getLength3();
	}
	
	
	/*
	 * print data for gnuplot
	 */
	private void printData()
	{
		if (packer != null)
		{
			double printHeight = 0;
			double printDistance = 0;
			
			if ((m_a.getFlys().size() > 0) && (m_a.getFlys().get(0).getBalls().size() > 0))
			{
				final FlyingBall lastBall = m_a.getFlys().get(0).getBalls().get(m_a.getFlys().get(0).getBalls().size() - 1);
				printHeight = lastBall.getFlyingHeight();
				printDistance = lastBall.getDistance();
			}
			if (Def.DEBUG_FLY_HEIGHT)
			{
				log.info("Intern: " + printHeight);
			}
			packer.print(printHeight, printDistance, m_a.isBallFlying());
		} else
		{
			throw new IllegalArgumentException("WP:FlyBall:BallCorr Packer is not initialized");
		}
	}
	
	
	/*
	 * find out the potential kicker-bot
	 */
	private boolean kickPossibleDetected(CamBall ball, List<CamRobot> bots)
	{
		boolean botFound = false;
		// reset viewAngle
		mAngleDistance = Def.DUMMY;
		
		// area around the ball
		//
		// |--u--|
		// y l r
		// | |--d--|
		// |
		// |----x
		//
		final double radius = Def.KICK_RADIUS_AROUND_BOT;
		final double l = ball.pos.x() - radius;
		final double r = ball.pos.x() + radius;
		final double d = ball.pos.y() - radius;
		final double u = ball.pos.y() + radius;
		
		// for all bots
		for (final CamRobot bot : bots)
		{
			final double kickerx = Math.cos(bot.orientation) * Def.BOT_RADIUS;
			final double kickery = Math.sin(bot.orientation) * Def.BOT_RADIUS;
			
			final double startX = bot.pos.x() + kickerx;
			final double startY = bot.pos.y() + kickery;
			
			// if ball is inside botRadius
			if ((l < startX) && (r > startX) && (d < startY) && (u > startY))
			{
				if (Def.DEBUG)
				{
					log.info("Kicker ist in diesem Radius");
				}
				// if viewAngle is good
				final LinFunc viewFunc = new LinFunc(new Coord(startX, startY), bot.orientation);
				
				final double ballDeltaX = ball.pos.x() - startX;
				final double ballDeltaY = ball.pos.y() - startY;
				
				final double Bot2BallAngle = viewFunc.getAngleToVector(new Coord(ballDeltaX, ballDeltaY));
				
				
				// is diff small enough
				if (Bot2BallAngle < Def.START_BOT2BALL_MAX_ANGLE)
				{
					if (Def.DEBUG)
					{
						log.info("Bot gefunden, im Bereich mit ausreichend kleinem Winkel");
					}
					// is there anyone, who is better?
					if (botFound)
					{
						// log.info("Er hat den besten Winkel: ");
						if (Bot2BallAngle < mAngleDistance)
						{
							// set this as new
							mAngleDistance = Bot2BallAngle;
							mBot = bot;
							// log.info("Ja");
						}
						// else: change nothing
						// else
						// {
						// // log.info("Nein");
						// }
					}
					// else set this one
					else
					{
						// log.info("Das ist der erste Bot dieses Flugs");
						mAngleDistance = Bot2BallAngle;
						mBot = bot;
						// set flag
						botFound = true;
					}
				} else
				{
					if (Def.DEBUG)
					{
						log.info("Bot gefunden, im Bereich mit NICHT ausreichend kleinem Winkel");
					}
				}
			}
			// else
			// {
			// // log.info("Kicker ist nicht in diesem Radius");
			// }
		}
		return botFound;
	}
	
	
	@Override
	public void update(ICamGeomObservable observable, CamGeometryFrame event)
	{
		if (!Def.isParametersSet)
		{
			if (event.cameraCalibration.size() < 2)
			{
				throw new IllegalArgumentException("BallCorrector: At least one Cam is missing!");
			}
			final CamCalibration camZero = event.cameraCalibration.get(0);
			final CamCalibration camOne = event.cameraCalibration.get(1);
			final double height = (camZero.derivedCameraWorldTz + camOne.derivedCameraWorldTz) / 2;
			
			if (Def.DEBUG_CAM)
			{
				log.info("Param: Kamera " + camZero.cameraId + "..." + camZero.derivedCameraWorldTx + ":"
						+ camZero.derivedCameraWorldTy);
				log.info("Param: Kamera " + camOne.cameraId + "..." + camOne.derivedCameraWorldTx + ":"
						+ camOne.derivedCameraWorldTy);
			}
			
			Def.setParameter(height, event.cameraCalibration);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
