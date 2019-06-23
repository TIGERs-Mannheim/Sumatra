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

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamCalibration;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Altigraph;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.FlyingBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.FramePacker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.LinFunc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ICamGeomObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.moduli.exceptions.ModuleNotFoundException;

/**
 * This Class is to correct the ssl-ballPosition from bottom to fly
 * 
 * @author Birgit
 * 
 */
public class BallCorrector implements ICamGeomObserver
{
	public Altigraph 		m_a 			;
	
	private CamRobot     m_bot		;
	private double       m_angleDistance = Def.DUMMY;
	private FramePacker packer = null;

	public BallCorrector()
	{
		m_a = new Altigraph();
		
		ACam cam;
		try
		{
			cam = (ACam) SumatraModel.getInstance().getModule("cam");
			cam.addCamGeometryObserver(this);
		} catch (ModuleNotFoundException err)
		{
			err.printStackTrace();
		}	
		
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/*
	 * correct the ball frame, if ball is flying
	 */
	public CamDetectionFrame correctFrame(CamDetectionFrame frame)
	{
		if(Def.debugCam)
		{
			if(frame.balls.size() > 0)
			{
				System.out.println("Ball: "+frame.cameraId+"..."+frame.balls.get(0).pos.x+":"+frame.balls.get(0).pos.y);
			}
		//return frame;
		}
		
		packer = new FramePacker(frame);
		
		if(Def.debug)
		System.out.print("# Frame "+frame.frameNumber);
		
		//if ball not inside the frame, return the origin frame
		if(!Def.isParametersSet)
		{
			if(Def.debug)
			System.out.println("Parameter noch nicht gesetzt oder kein Ball im Frame");
			return packer.getOldFrame();
		}		
		
		if(frame.balls.size() < 1)
		{
			if(Def.debug)
			System.out.println("Kein Ball im Frame");
			return packer.getOldFrame();
		}	
		
		// get Data of Ball and Bots
		CamBall bottomBall = packer.getBall();
		List<CamRobot> bots = packer.getBotList();
			
		
		//if kick possible, add new fly
		if(kickPossibleDetected(bottomBall, bots))
		{
			if(Def.debug)
			System.out.println("Corr: Flug ergänzt");
			//append new fly
			m_a.addKickerZoneIdentified(m_bot.pos.x, m_bot.pos.y, m_bot.orientation);
		}
		
		
		//append the ball to old and new flys
		m_a.addCamFrame(bottomBall.pos.x, bottomBall.pos.y, frame.cameraId);
		//System.out.println("Corr: Ball ergänzt");
		
		//if ball is flying
		if(m_a.isBallFlying())
		{
			if(Def.debug)
			System.err.println("Corr: Ball fliegt: "+m_a.getCorrectedFrame().z());
			//correct the ball-data and give new frame back
			
			
			packer.setNewCamBall(
					(float) m_a.getCorrectedFrame().x(), 
					(float) m_a.getCorrectedFrame().y(), 
					(float) m_a.getCorrectedFrame().z());
			//System.out.println("");
		
		}
		//give old frame back
		else
		{
			if(Def.debug)
			System.out.println("Boden");
		}

		printData();
		return packer.getMaybeCorrectedFrame();
	}
	
	/*
	 * print data for gnuplot
	 */
	private void printData()
	{
		if(packer != null)
		{
			double printHeight = 0;
			double printDistance = 0;
		
			if(m_a.m_flys.size() > 0 && m_a.m_flys.get(0).m_balls.size() > 0)
			{
				FlyingBall lastBall = m_a.m_flys.get(0).m_balls.get(m_a.m_flys.get(0).m_balls.size()-1);
				printHeight = lastBall.getFlyingHeight();
				printDistance = lastBall.getDistance();
				
			}	
			if(Def.debugFlyHeight)
			{
			System.out.println("Intern: "+printHeight);	
			}
			packer.print(printHeight, printDistance, m_a.isBallFlying());
		}
		else
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
		//reset viewAngle
		m_angleDistance = Def.DUMMY;
		
		 // area around the ball
		//
		//		|--u--|
		// y	l     r
		// | 	|--d--|
		// |
		// |----x
		//
		double radius = Def.KICK_RADIUS_AROUND_BOT;
		double l = ball.pos.x - radius;
		double r = ball.pos.x + radius;
		double d = ball.pos.y - radius;
		double u = ball.pos.y + radius;
		
		//for all bots
		for (CamRobot bot : bots)
		{
			double kickerx = Math.cos(bot.orientation)*Def.BOT_RADIUS;
			double kickery = Math.sin(bot.orientation)*Def.BOT_RADIUS;
			
			double startX = bot.pos.x+kickerx;
			double startY = bot.pos.y+kickery;
			
			//if ball is inside botRadius
			if((l < startX) &&
				(r > startX) &&
				(d < startY) &&
				(u > startY))
			{
				if(Def.debug)
				System.out.println("Kicker ist in diesem Radius");
				//if viewAngle is good				
				LinFunc viewFunc = new LinFunc(new Coord(startX, startY), bot.orientation);
				
				double ballDeltaX = ball.pos.x - startX;
				double ballDeltaY = ball.pos.y - startY;
				
				double Bot2BallAngle = viewFunc.getAngleToVector(new Coord(ballDeltaX, ballDeltaY));
				

				//is diff small enough
				if( Bot2BallAngle < Def.START_BOT2BALL_MAX_ANGLE)
				{
					if(Def.debug)
					System.out.println("Bot gefunden, im Bereich mit ausreichend kleinem Winkel");
					//is there anyone, who is better?
					if(botFound)
					{
						//System.out.print("Er hat den besten Winkel: ");
						if( Bot2BallAngle < m_angleDistance)
						{
							//set this as new
							m_angleDistance =  Bot2BallAngle;
							m_bot = bot;
							//System.out.println("Ja");
						}
						//else: change nothing
						else
						{
							//System.out.println("Nein");
						}
					}
					//else set this one
					else
					{
						//System.out.println("Das ist der erste Bot dieses Flugs");
						m_angleDistance =  Bot2BallAngle;
						m_bot = bot;
			    		//set flag
						botFound = true;
					}
				}
				else
				{
					if(Def.debug)
					System.out.println("Bot gefunden, im Bereich mit NICHT ausreichend kleinem Winkel");
				}
			}
			else
			{
				//System.out.println("Kicher ist nicht in diesem Radius");
			}
		}
		return botFound;
	}

	@Override
	public void update(ICamGeomObservable observable, CamGeometryFrame event)
	{
		/*
		System.out.println("###########CamGeometryFrame");
		for(int i = 0; i < event.cameraCalibration.size(); i++)
		{
			System.out.println("###########Cam "+i);
			System.out.println(event.cameraCalibration.get(i).toString());
		}
		*/

		
		if(!Def.isParametersSet)
		{
			if(event.cameraCalibration.size() < 2)
			{
				throw new IllegalArgumentException("BallCorrector: At least one Cam is missing!");
			}
			CamCalibration camNull = event.cameraCalibration.get(0);
			CamCalibration camOne = event.cameraCalibration.get(1);
			double height = (camNull.derived_camera_world_tz + camOne.derived_camera_world_tz)/2;

			if(Def.debugCam)
			{
			System.out.println("Param: Kamera "+camNull.camera_id+"..."+camNull.derived_camera_world_tx+":"+camNull.derived_camera_world_ty);
			System.out.println("Param: Kamera "+camOne.camera_id+"..."+camOne.derived_camera_world_tx+":"+camOne.derived_camera_world_ty);
			}
			
						
				Def.setParameter(
						height, 
						camNull.derived_camera_world_tx, 
						camNull.derived_camera_world_ty, 
						camNull.camera_id,
						camOne.derived_camera_world_tx, 
						camOne.derived_camera_world_ty,
						camOne.camera_id);

		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
