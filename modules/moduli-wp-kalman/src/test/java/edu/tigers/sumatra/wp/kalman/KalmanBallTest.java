/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;

import org.junit.Ignore;
import org.junit.Test;

import Jama.Matrix;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBall;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.kalman.data.PredictionContext;
import edu.tigers.sumatra.wp.kalman.data.WPCamBot;
import edu.tigers.sumatra.wp.kalman.filter.ExtKalmanFilter;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;
import edu.tigers.sumatra.wp.kalman.motionModels.BallMotionModel;
import edu.tigers.sumatra.wp.kalman.motionModels.TigersMotionModel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KalmanBallTest
{
	private static final String FOLDER = "data/ball/moduli_sim/ballManual/2015-05-23_21-08-13/";
	
	
	/**
	 */
	@Test
	@Ignore
	public void testCorrectBall()
	{
		BallCorrector.runOnData(FOLDER);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testBallDynamics()
	{
		String baseFolder = "data/ball/ballDynamicsTest/";
		PredictionContext context = new PredictionContext();
		BallMotionModel model = new BallMotionModel();
		
		TigersMotionModel botModel = new TigersMotionModel();
		IFilter botFilter = new ExtKalmanFilter();
		IVector3 botPos = new Vector3(1025, 2000, -AngleMath.PI_HALF);
		CamRobot rawBot = new CamRobot(0, 0, 0, 0, 0, 0, 0, botPos.x(), botPos.y(), botPos.z(), 0, BotID.createBotId(
				0, ETeamColor.YELLOW));
		botFilter.init(botModel, context, 0, new WPCamBot(rawBot));
		context.getBlueBots().put(0, botFilter);
		
		
		CSVExporter exporterBot = new CSVExporter(baseFolder + "rawBots", false);
		exporterBot.addValues(rawBot.getNumberList());
		exporterBot.close();
		
		
		Matrix state = new Matrix(10, 1);
		state.set(0, 0, -3000);
		state.set(1, 0, -1000);
		state.set(2, 0, 0);
		state.set(3, 0, -1500);
		state.set(4, 0, 900);
		state.set(5, 0, 0);
		state.set(6, 0, 0);
		state.set(7, 0, 0);
		state.set(8, 0, 0);
		
		CSVExporter exporter = new CSVExporter(baseFolder + "simBall", false);
		long dt = (long) (16e6);
		long time = 0;
		for (int i = 0; i < 1000; i++)
		{
			IVector3 pos = new Vector3(state.get(0, 0), state.get(1, 0), state.get(2, 0));
			IVector3 vel = new Vector3(state.get(3, 0), state.get(4, 0), state.get(5, 0));
			IVector3 acc = new Vector3(state.get(6, 0), state.get(7, 0), state.get(8, 0));
			WpBall ball = new WpBall(pos, vel, acc, i, time, state.get(9, 0));
			exporter.addValues(ball.getNumberList());
			
			Matrix newState = model.dynamics(state, null, dt * 1e-9, new MotionContext());
			if ((Math.abs(state.get(3, 0) - newState.get(3, 0)) < 0.01)
					&& (Math.abs(state.get(4, 0) - newState.get(4, 0)) < 0.01))
			{
				System.out.println("Converged after " + i + " iterations.");
				break;
			}
			state = newState;
			time += dt;
		}
		exporter.close();
	}
}
