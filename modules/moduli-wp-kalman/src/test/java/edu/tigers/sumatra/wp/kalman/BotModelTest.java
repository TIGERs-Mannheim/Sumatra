/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 29, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import Jama.Matrix;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1DOrient;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.kalman.data.PredictionContext;
import edu.tigers.sumatra.wp.kalman.data.RobotMotionResult_V2;
import edu.tigers.sumatra.wp.kalman.data.WPCamBot;
import edu.tigers.sumatra.wp.kalman.filter.ExtKalmanFilter;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;
import edu.tigers.sumatra.wp.kalman.motionModels.IMotionModel;
import edu.tigers.sumatra.wp.kalman.motionModels.TigersMotionModel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotModelTest
{
	
	
	/**
	 * 
	 */
	@Test
	@Ignore
	public void testControl()
	{
		PredictionContext context = new PredictionContext();
		BotProcessor bp = new BotProcessor(context);
		
		
		IVector2 initPos = new Vector2(0, 0);
		IVector2 finalPos = new Vector2(4, 0);
		IVector2 initialVel = new Vector2(0, 0);
		double initOrient = 0;
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(initPos, finalPos, initialVel, 3, 3, 3);
		BangBangTrajectory1DOrient trajW = new BangBangTrajectory1DOrient(initOrient, 0, 0, 30, 30, 10);
		BotID botID = BotID.createBotId(0, ETeamColor.YELLOW);
		
		double frameDt = 0.016;
		double dt = 0.002;
		
		CSVExporter exp = new CSVExporter("/tmp/testControl", false);
		
		double t = 0;
		int frameId = 1;
		double nextT = 0;
		long ts = (long) (t * 1e9);
		
		CamRobot cBot = new CamRobot(1.0, 0.0, 0.0, ts, ts, 0, frameId, initPos.x(), initPos.y(), initOrient, 150,
				botID);
		final IFilter bot = new ExtKalmanFilter();
		final IMotionModel motionModel = new TigersMotionModel();
		bot.init(motionModel, context, ts, new WPCamBot(cBot));
		context.getYellowBots().put(botID.getNumber() + WPConfig.YELLOW_ID_OFFSET, bot);
		
		IVector2 pos = trajXy.getPositionMM(t);
		double orientation = trajW.getPosition(t);
		IVector2 vel = trajXy.getVelocity(t);
		double aVel = trajW.getVelocity(t);
		
		while (t < trajXy.getTotalTime())
		{
			List<CamRobot> camYellowBots = new ArrayList<>();
			List<CamRobot> camBlueBots = new ArrayList<>();
			
			nextT += frameDt;
			while (t < nextT)
			{
				final ExtKalmanFilter existingBot = (ExtKalmanFilter) context.getYellowBots()
						.get(botID.getNumber() + WPConfig.YELLOW_ID_OFFSET);
				final RobotMotionResult_V2 s = (RobotMotionResult_V2) existingBot
						.getPrediction(ts);
				Matrix contr = existingBot.getContr();
				if (contr == null)
				{
					contr = new Matrix(6, 1);
				}
				ITrackedBot tBot = s.motionToTrackedBot(ts, botID);
				exp.addValues(t,
						tBot.getPos().x(), tBot.getPos().y(), tBot.getAngle(),
						tBot.getVel().x(), tBot.getVel().y(), tBot.getaVel(),
						pos.x(), pos.y(), orientation,
						vel.x(), vel.y(), aVel,
						contr.get(0, 0), contr.get(1, 0), contr.get(2, 0));
				t += dt;
				ts = (long) (t * 1e9);
			}
			
			pos = trajXy.getPositionMM(t);
			orientation = trajW.getPosition(t);
			vel = trajXy.getVelocity(t);
			aVel = trajW.getVelocity(t);
			cBot = new CamRobot(1.0, 0.0, 0.0, ts, ts, 0, frameId, pos.x(), pos.y(), orientation, 150,
					botID);
			camYellowBots.add(cBot);
			bp.process(camYellowBots, camBlueBots);
			frameId++;
		}
		
		exp.close();
	}
	
	
	/**
	 * 
	 */
	@Test
	@Ignore
	public void test()
	{
		for (double a = -10; a < 10; a += 0.2)
		{
			double r = determineContinuousAngle(a, -1.2);
			System.out.printf("%.3f %.3f %.3f\n", a, -1.2, r);
		}
		
	}
	
	
	protected double determineContinuousAngle(final double oldAngle, final double newAngle)
	{
		// project new orientation next to old one
		// thus values greater than PI are possible but the handling gets easier
		// standard: just a small rotation
		double dAngle = newAngle - AngleMath.normalizeAngle(oldAngle);
		// rotation clockwise over Pi-border
		if (dAngle > Math.PI)
		{
			dAngle = dAngle - (2 * Math.PI);
		}
		// rotation counter-clockwise over Pi-border
		else if (dAngle < -Math.PI)
		{
			dAngle = dAngle + (2 * Math.PI);
		}
		return oldAngle + dAngle;
	}
}
