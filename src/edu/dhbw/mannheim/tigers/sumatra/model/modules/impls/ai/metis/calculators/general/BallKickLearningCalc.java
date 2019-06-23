/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 10, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * Collecting kick data all the time during normal play and save it to file
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BallKickLearningCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Map<BotID, SingleBotCalc>	calcs	= new HashMap<BotID, SingleBotCalc>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		for (Map.Entry<BotID, TrackedTigerBot> entry : curFrame.worldFrame.tigerBotsAvailable.entrySet())
		{
			if (!calcs.containsKey(entry.getKey()))
			{
				calcs.put(entry.getKey(), new SingleBotCalc(entry.getKey()));
			}
		}
		for (SingleBotCalc calc : calcs.values())
		{
			calc.doCalc(curFrame, preFrame);
		}
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private static class SingleBotCalc extends ACalculator
	{
		private static final float	BALL_DAMPED_THRES		= 0.2f;
		private final BotID			botID;
		private float					lastKickerCharge		= 0;
		private boolean				kicking					= false;
		private float					initialBallDirection;
		private boolean				ballDirectionKnown	= false;
		private List<List<Float>>	shootData				= new LinkedList<List<Float>>();
		
		
		protected SingleBotCalc(BotID botID)
		{
			this.botID = botID;
		}
		
		
		@Override
		public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
		{
			TrackedTigerBot bot = curFrame.worldFrame.tigerBotsVisible.getWithNull(botID);
			if ((bot == null) || (bot.getBot() == null))
			{
				return;
			}
			float diff = lastKickerCharge - bot.getBot().getKickerLevel();
			if (diff > AIConfig.getSkills(bot.getBotType()).getKickerDischargeTreshold())
			{
				kicking = true;
				shootData.clear();
				ballDirectionKnown = false;
			}
			
			if (kicking)
			{
				List<Float> data = new ArrayList<Float>(8);
				IVector3 p = curFrame.worldFrame.ball.getPos3();
				IVector3 v = curFrame.worldFrame.ball.getVel3();
				data.add(p.x());
				data.add(p.y());
				data.add(p.z());
				data.add(v.x());
				data.add(v.y());
				data.add(v.z());
				data.add(v.getLength3());
				data.add(v.getLength2());
				shootData.add(data);
				
				if (!ballDirectionKnown && !curFrame.worldFrame.ball.getVel().equals(Vector2.ZERO_VECTOR, 0.001f))
				{
					initialBallDirection = curFrame.worldFrame.ball.getVel().getAngle();
					ballDirectionKnown = true;
				}
				
				if (ballDirectionKnown)
				{
					if (curFrame.worldFrame.ball.getVel().equals(Vector2f.ZERO_VECTOR, 0.001f))
					{
						kickDone();
					} else if ((AngleMath.getShortestRotation(curFrame.worldFrame.ball.getVel().getAngle(),
							initialBallDirection) > BALL_DAMPED_THRES))
					{
						kickDone();
					}
				}
			}
			
			lastKickerCharge = bot.getBot().getKickerLevel();
		}
		
		
		private void kickDone()
		{
			new Thread(new ExporterThread(new ArrayList<List<Float>>(shootData))).start();
			shootData.clear();
			kicking = false;
		}
		
		
		@Override
		public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
		{
		}
		
		private class ExporterThread implements Runnable
		{
			private static final String	ID		= "kickCalibration";
			private List<List<Float>>		data	= new LinkedList<List<Float>>();
			
			
			protected ExporterThread(List<List<Float>> shootData)
			{
				data = shootData;
			}
			
			
			@Override
			public void run()
			{
				Thread.currentThread().setName("CSVExporter");
				CSVExporter exporter = new CSVExporter(ID, "kickCalibration", true);
				exporter.setHeader("px", "py", "pz", "vx", "vy", "vz", "vel3", "vel2");
				exporter.setAdditionalInfo("BotID: " + botID.getNumber());
				for (List<Float> d : data)
				{
					exporter.addValues(d);
				}
				exporter.close();
			}
		}
	}
}
