/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.05.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.vis.data;

import java.util.Map;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData;
import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData.Poi;
import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData.PoiList;
import edu.dhbw.mannheim.tigers.sim.engine.plugins.visualization.SimVisMessages.SimVisData.Vec3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;


/**
 * This class "simply" converts the given CS-internal-data to VisualizationData. It is stateful, as it has to
 * know which side the TIGERS are playing on. This is important, because the input (
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SSLVisionCam} changes the
 * coordinates so we are always playing from right to left. Now we got to change it here back again if necessary!
 * *sight* :-S
 */
public class Translator
{
	private SimVisData.Builder	builder;
	private static int			counter				= 0;
	/** This field must be updated by every external call!!! */
	private TeamProps				currentTeamProps	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public Translator()
	{
		reset();
	}
	
	
	/**
	 * @return Whether we have to turn everything around
	 */
	private boolean haveToTurn()
	{
		return currentTeamProps.getPlayLeftToRight();
	}
	
	
	/**
	 * Not only creates a full {@link edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3}, but also cares
	 * about the orientation which may be inverted
	 */
	private Vec3 v3(float x, float y, float z)
	{
		final Vec3.Builder b = Vec3.newBuilder();
		
		if (haveToTurn())
		{
			b.setX(-x).setY(-y).setZ(z);
		} else
		{
			b.setX(x).setY(y).setZ(z);
		}
		
		return b.build();
	}
	
	
	/**
	 * Not only creates a full {@link edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3}, but also cares
	 * about the orientation which may be inverted
	 */
	private Vec3 v3(IVector2 vec, float z)
	{
		return v3(vec.x(), vec.y(), z);
	}
	
	
	/**
	 * Not only creates a full {@link edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3}, but also cares
	 * about the orientation which may be inverted
	 */
	private Vec3 v3(IVector3 vec)
	{
		return v3(vec.x(), vec.y(), vec.z());
	}
	
	
	// --------------------------------------------------------------------------
	// --- Situations -----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param frame
	 */
	public void translateSituations(AIInfoFrame frame)
	{
		currentTeamProps = frame.worldFrame.teamProps;
		
		// Debug-points
		int i = 0;
		for (final IDrawableShape debugPoint : frame.tacticalInfo.getDebugShapes())
		{
			if (!(debugPoint instanceof DrawablePoint))
			{
				continue;
			}
			final PoiList.Builder b = PoiList.newBuilder();
			b.setId(i);
			b.addPois(convertDebugPoint((DrawablePoint) debugPoint));
			
			builder.addSituations(b);
			i++;
		}
	}
	
	
	private Poi.Builder convertDebugPoint(IVector2 debugVec)
	{
		final Poi.Builder b = Poi.newBuilder();
		b.setPos(v3(debugVec, 0));
		return b;
	}
	
	
	// --------------------------------------------------------------------------
	// --- Bots and balls -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param aiFrame
	 */
	public void translateBots(AIInfoFrame aiFrame)
	{
		currentTeamProps = aiFrame.worldFrame.teamProps;
		
		final float val = 1;
		// Ball
		if (aiFrame.worldFrame.ball != null)
		{
			builder.addBalls(poiList(aiFrame.worldFrame.ball.getId(),
					convertBallData(aiFrame.worldFrame.ball, val, aiFrame.worldFrame.time)));
		}
		
		// Tigers
		for (final Entry<BotID, TrackedTigerBot> entry : aiFrame.worldFrame.tigerBotsVisible.entrySet())
		{
			final TrackedTigerBot bot = entry.getValue();
			builder.addPredictions(poiList(bot.getId(),
					convertBotData(bot, val, aiFrame.worldFrame.time, aiFrame.getAssigendRoles())));
		}
		
		// Enemies
		for (final Entry<BotID, TrackedBot> entry : aiFrame.worldFrame.foeBots.entrySet())
		{
			final TrackedBot bot = entry.getValue();
			builder.addPredictions(poiList(bot.getId(), convertBotData(bot, -val, aiFrame.worldFrame.time, null)));
		}
	}
	
	
	private PoiList poiList(AObjectID id, Poi poi)
	{
		final PoiList.Builder b = PoiList.newBuilder();
		b.setId(id.getNumber());
		b.addPois(poi);
		return b.build();
	}
	
	
	private Poi convertBotData(TrackedBot bot, float value, long time, IBotIDMap<ARole> roleMap)
	{
		final Vec3 pos = v3(bot.getPos(), 0f);
		final Vec3 vel = v3(bot.getVel(), 0f);
		final Vec3 acc = v3(bot.getAcc(), 0f);
		
		final Poi.Builder b = Poi.newBuilder();
		b.setPos(pos).setVel(vel).setAcc(acc).setValue(value).setTime(time).setDir(bot.getAngle())
				.setTurnVel(bot.getaVel()).setTurnAcc(bot.getaAcc());
		
		if (roleMap != null)
		{
			final ARole role = roleMap.getWithNull(bot.getId());
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
		final Vec3 pos = v3(ball.getPos3());
		final Vec3 vel = v3(ball.getVel3());
		final Vec3 acc = v3(ball.getAcc3());
		
		final Poi.Builder b = Poi.newBuilder();
		b.setPos(pos).setVel(vel).setAcc(acc).setValue(value).setTime(time);
		
		return b.build();
	}
	
	
	// --------------------------------------------------------------------------
	// --- Bots and balls -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param paths
	 * @param wf
	 */
	public void translatePaths(final Map<BotID, Path> paths, final WorldFrame wf)
	{
		currentTeamProps = wf.teamProps;
		
		for (final Path path : paths.values())
		{
			int length = 0;
			
			// Look for start-position, as this is not in the Path-object
			final TrackedTigerBot bot = wf.tigerBotsVisible.getWithNull(path.getBotID());
			if (bot == null)
			{
				// No bot with this paths id, so no path! =)
				continue;
			}
			
			// Valid bot ID...
			final PoiList.Builder b = PoiList.newBuilder();
			b.setId(path.getBotID().getNumber());
			
			// ...add current position as first point of path!
			b.addPois(convertNavPoint(bot.getPos(), length++));
			
			
			for (final IVector2 p : path.getPath())
			{
				b.addPois(convertNavPoint(p, length++));
			}
			
			builder.addPaths(b);
		}
	}
	
	
	private Poi convertNavPoint(IVector2 vec, int length)
	{
		final Poi.Builder b = Poi.newBuilder();
		b.setPos(v3(vec, 0f)).setValue(length);
		return b.build();
	}
	
	
	/**
	 * Resets the internal SimVisData#Builder ({@link #builder}).
	 */
	public final void reset()
	{
		builder = SimVisData.newBuilder();
		currentTeamProps = null;
	}
	
	
	/**
	 * @return The result of SimVisData.Builder#build()
	 */
	public SimVisData build()
	{
		builder.setId(counter);
		incCounter();
		return builder.build();
	}
	
	
	private static void incCounter()
	{
		counter++;
	}
}
