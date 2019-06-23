/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.05.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.vis.data;

import java.util.HashMap;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData;
import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData.Poi;
import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData.PoiList;
import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData.Vec3;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SSLVisionCam;


/**
 * This class "simply" converts the given CS-internal-data to {@link VisualizationData}. It is stateful, as it has to
 * know which side the TIGERS are playing on. This is important, because the input ({@link SSLVisionCam} changes the
 * coordinates so we are always playing from right to left. Now we got to change it here back again if necessary!
 * *sight* :-S
 */
public class Translator
{
	/** Whether the coordinates has to be switched */
	private final boolean		turn;
	
	private SimVisData.Builder	builder;
	private static int			counter	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public Translator()
	{
		turn = SumatraModel.getInstance().getGlobalConfiguration().getString("ourGameDirection")
				.equalsIgnoreCase("leftToRight");
		
		reset();
	}
	

	/** Not only creates a full {@link Vector3}, but also cares about the orientation which may be inverted */
	private Vec3 v3(float x, float y, float z)
	{
		Vec3.Builder b = Vec3.newBuilder();
		
		if (turn)
		{
			b.setX(-x).setY(-y).setZ(z);
		} else
		{
			b.setX(x).setY(y).setZ(z);
		}
		
		return b.build();
	}
	

	/** Not only creates a full {@link Vector3}, but also cares about the orientation which may be inverted */
	private Vec3 v3(IVector2 vec, float z)
	{
		return v3(vec.x(), vec.y(), z);
	}
	

	/** Not only creates a full {@link Vector3}, but also cares about the orientation which may be inverted */
	private Vec3 v3(IVector3 vec)
	{
		return v3(vec.x(), vec.y(), vec.z());
	}
	

	// --------------------------------------------------------------------------
	// --- Situations -----------------------------------------------------------
	// --------------------------------------------------------------------------
	public void translateSituations(AIInfoFrame frame)
	{
		// Debug-points
		int i = 0;
		for (IVector2 debugPoint : frame.getDebugPoints())
		{
			PoiList.Builder b = PoiList.newBuilder();
			b.setId(i);
			b.addPois(convertDebugPoint(debugPoint));
			
			builder.addSituations(b);
			i++;
		}
	}
	

	private Poi.Builder convertDebugPoint(IVector2 debugVec)
	{
		Poi.Builder b = Poi.newBuilder();
		b.setPos(v3(debugVec, 0));
		return b;
	}
	

	// --------------------------------------------------------------------------
	// --- Bots and balls -------------------------------------------------------
	// --------------------------------------------------------------------------
	public void translateBots(AIInfoFrame aiFrame)
	{
		
		// for (int i = 0; i < wfs.length; i++)
		// {
		// aiFrame.playStrategy.getActivePlays().get(0).getRoles().get(0).
		
		// WorldFrame wf = wfs[i];
		// float val = 1 - (i / (float) wfs.length);
		float val = 1;
		// Ball
		if (aiFrame.worldFrame.ball != null)
		{
			builder.addBalls(poiList(aiFrame.worldFrame.ball.id,
					convertBallData(aiFrame.worldFrame.ball, val, aiFrame.worldFrame.time)));
		}
		
		// Tigers
		for (Entry<Integer, TrackedTigerBot> entry : aiFrame.worldFrame.tigerBots.entrySet())
		{
			TrackedTigerBot bot = entry.getValue();
			builder.addPredictions(poiList(bot.id,
					convertBotData(bot, val, aiFrame.worldFrame.time, aiFrame.assignedRoles)));
		}
		
		// Enemies
		for (Entry<Integer, TrackedBot> entry : aiFrame.worldFrame.foeBots.entrySet())
		{
			TrackedBot bot = entry.getValue();
			builder.addPredictions(poiList(bot.id, convertBotData(bot, -val, aiFrame.worldFrame.time, null)));
		}
		// }
	}
	

	private PoiList poiList(int id, Poi poi)
	{
		PoiList.Builder b = PoiList.newBuilder();
		b.setId(id);
		b.addPois(poi);
		return b.build();
	}
	

	/**
	 * TODO extend for TrackedTigerBot...?
	 */
	private Poi convertBotData(TrackedBot bot, float value, long time, HashMap<Integer, ARole> roleMap)
	{
		Vec3 pos = v3(bot.pos, 0f);
		Vec3 vel = v3(bot.vel, 0f);
		Vec3 acc = v3(bot.acc, 0f);
		
		Poi.Builder b = Poi.newBuilder();
		b.setPos(pos).setVel(vel).setAcc(acc).setValue(value).setTime(time).setDir(bot.angle).setTurnVel(bot.aVel)
				.setTurnAcc(bot.aAcc);
		
		if (roleMap != null)
		{
			ARole role = roleMap.get(bot.id);
			if (role != null)
			{
				b.addText(role.toString());
			} else
			{
				b.addText("no role");
			}
		}
		
		return b.build();
	}
	

	private Poi convertBallData(TrackedBall ball, float value, long time)
	{
		Vec3 pos = v3(ball.pos3());
		Vec3 vel = v3(ball.vel3());
		Vec3 acc = v3(ball.acc3());
		
		Poi.Builder b = Poi.newBuilder();
		b.setPos(pos).setVel(vel).setAcc(acc).setValue(value).setTime(time);
		
		return b.build();
	}
	

	// --------------------------------------------------------------------------
	// --- Bots and balls -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param paths
	 * @param to
	 */
	public void translatePaths(HashMap<Integer, Path> paths, WorldFrame wf)
	{
		
		for (Path path : paths.values())
		{
			int length = 0;
			
			// Look for start-position, as this is not in the Path-object
			TrackedTigerBot bot = wf.tigerBots.get(path.botID);
			if (bot == null)
			{
				continue; // No bot with this paths id, so no path! =)
			}
			
			// Valid bot ID...
			PoiList.Builder b = PoiList.newBuilder();
			b.setId(path.botID);
			
			// ...add current position as first point of path!
			b.addPois(convertNavPoint(bot.pos, length++));
			

			for (IVector2 p : path.path)
			{
				b.addPois(convertNavPoint(p, length++));
			}
			
			builder.addPaths(b);
		}
	}
	

	private Poi convertNavPoint(IVector2 vec, int length)
	{
		Poi.Builder b = Poi.newBuilder();
		b.setPos(v3(vec, 0f)).setValue(length);
		return b.build();
	}
	

	/**
	 * Resets the internal {@link SimVisData.Builder} ({@link #builder}).
	 */
	public void reset()
	{
		this.builder = SimVisData.newBuilder();
	}
	

	/**
	 * @return The result of {@link SimVisData.Builder#build()}
	 */
	public SimVisData build()
	{
		builder.setId(counter++);
		return builder.build();
	}
}
