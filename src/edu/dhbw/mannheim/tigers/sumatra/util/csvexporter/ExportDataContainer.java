/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.csvexporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.DummyBot;


/**
 * Data container for all relevant data for export
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ExportDataContainer implements IJsonString
{
	@SuppressWarnings("unused")
	private static final Logger						log						= Logger.getLogger(ExportDataContainer.class
																									.getName());
	
	private long											timestampRecorded		= 0;
	private final List<RawBall>						balls						= new ArrayList<>();
	private final List<RawBot>							rawBots					= new ArrayList<>();
	private final List<WpBot>							wpBots					= new ArrayList<>();
	private RawBall										curBall					= new RawBall();
	private WpBall											wpBall					= new WpBall();
	private final Map<String, INumberListable>	customNumberListable	= new HashMap<>();
	
	
	@Override
	public JSONObject toJSON()
	{
		Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
		jsonMapping.put("timestampRecorded", timestampRecorded);
		jsonMapping.put("balls", balls.stream().map(e -> e.toJSON()).collect(Collectors.toList()));
		jsonMapping.put("rawBots", rawBots.stream().map(e -> e.toJSON()).collect(Collectors.toList()));
		jsonMapping.put("wpBots", wpBots.stream().map(e -> e.toJSON()).collect(Collectors.toList()));
		jsonMapping.put("curBall", curBall.toJSON());
		jsonMapping.put("wpBall", wpBall.toJSON());
		return new JSONObject(jsonMapping);
	}
	
	
	/**
	 * @param tBot
	 * @param frameId
	 * @param timestamp
	 * @return
	 */
	public static WpBot trackedBot2WpBot(final TrackedTigerBot tBot, final long frameId, final long timestamp)
	{
		IVector3 pos = new Vector3(tBot.getPos(), tBot.getAngle());
		IVector3 vel = new Vector3(tBot.getVel(), tBot.getaVel());
		IVector3 acc = new Vector3(tBot.getAcc(), tBot.getaAcc());
		return new WpBot(pos, vel, acc, tBot.getId().getNumber(), tBot.getId().getTeamColor(), frameId, timestamp);
	}
	
	
	/**
	 * @param m
	 * @return
	 */
	public static List<Number> toNumberList(final Matrix m)
	{
		double[] arr = m.getRowPackedCopy();
		List<Number> list = new ArrayList<>(arr.length);
		for (double d : arr)
		{
			list.add(d);
		}
		return list;
	}
	
	
	/**
	 * @param folder
	 * @param filename
	 * @return
	 */
	public static List<RawBall> readRawBall(final String folder, final String filename)
	{
		try
		{
			return Files
					.lines(Paths.get(folder + "/" + filename + ".csv"))
					.filter(line -> !line.startsWith("#"))
					.map(line -> line.split(","))
					.map(arr -> Arrays.asList(arr).stream()
							.map(s -> Double.valueOf(s))
							.collect(Collectors.toList()))
					.map(l -> RawBall.fromNumberList(l))
					.collect(Collectors.toList());
		} catch (IOException err)
		{
			throw new IllegalStateException();
		}
	}
	
	
	/**
	 * @param folder
	 * @param filename
	 * @param color
	 * @return
	 */
	public static List<RawBot> readRawBots(final String folder, final String filename, final ETeamColor color)
	{
		int colorNumber = color.getNumberList().get(0).intValue();
		try
		{
			return Files
					.lines(Paths.get(folder + "/" + filename + ".csv"))
					.filter(line -> !line.startsWith("#"))
					.map(line -> line.split(","))
					.filter(arr -> Integer.valueOf(arr[3]) == colorNumber)
					.map(arr -> Arrays.asList(arr).stream()
							.map(s -> Double.valueOf(s))
							.collect(Collectors.toList()))
					.map(l -> RawBot.fromNumberList(l))
					.collect(Collectors.toList());
		} catch (IOException err)
		{
			throw new IllegalStateException();
		}
	}
	
	
	/**
	 * @return the timestampRecorded
	 */
	public final long getTimestampRecorded()
	{
		return timestampRecorded;
	}
	
	
	/**
	 * @param timestampRecorded the timestampRecorded to set
	 */
	public final void setTimestampRecorded(final long timestampRecorded)
	{
		this.timestampRecorded = timestampRecorded;
	}
	
	
	/**
	 * @return the balls
	 */
	public final List<RawBall> getBalls()
	{
		return balls;
	}
	
	
	/**
	 * @return the curBall
	 */
	public final RawBall getCurBall()
	{
		return curBall;
	}
	
	
	/**
	 * @param curBall the curBall to set
	 */
	public final void setCurBall(final RawBall curBall)
	{
		this.curBall = curBall;
	}
	
	
	/**
	 * @return the wpBall
	 */
	public final WpBall getWpBall()
	{
		return wpBall;
	}
	
	
	/**
	 * @param wpBall the wpBall to set
	 */
	public final void setWpBall(final WpBall wpBall)
	{
		this.wpBall = wpBall;
	}
	
	
	/**
	 * @return the rawBots
	 */
	public final List<RawBot> getRawBots()
	{
		return rawBots;
	}
	
	
	/**
	 * @return the wpBots
	 */
	public final List<WpBot> getWpBots()
	{
		return wpBots;
	}
	
	/**
	 */
	public static class RawBall implements IJsonString, INumberListable
	{
		private long		timestamp	= 0;
		private int			camId			= -1;
		private IVector3	pos			= Vector3.ZERO_VECTOR;
		private long		frameId		= -1;
		
		
		/**
		 * 
		 */
		public RawBall()
		{
		}
		
		
		/**
		 * @param timestamp
		 * @param camId
		 * @param pos
		 * @param frameId
		 */
		public RawBall(final long timestamp, final int camId, final IVector3 pos, final long frameId)
		{
			super();
			this.timestamp = timestamp;
			this.camId = camId;
			this.pos = pos;
			this.frameId = frameId;
		}
		
		
		/**
		 * @param list
		 * @return
		 */
		public static RawBall fromNumberList(final List<? extends Number> list)
		{
			return new RawBall(list.get(0).longValue(), list.get(1).intValue(),
					Vector3.fromNumberList(list.subList(2, 5)), list.size() > 5 ? list.get(5).longValue() : -1);
		}
		
		
		/**
		 * @return
		 */
		public CamBall toCamBall()
		{
			return new CamBall(0, 0, pos.x(), pos.y(), pos.z(), 0, 0, timestamp, camId);
		}
		
		
		@Override
		public JSONObject toJSON()
		{
			Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
			jsonMapping.put("timestamp", timestamp);
			jsonMapping.put("camId", camId);
			jsonMapping.put("pos", pos.toJSON());
			jsonMapping.put("frameId", frameId);
			return new JSONObject(jsonMapping);
		}
		
		
		@Override
		public List<Number> getNumberList()
		{
			List<Number> numbers = new ArrayList<>();
			numbers.add(timestamp);
			numbers.add(camId);
			numbers.addAll(pos.getNumberList());
			numbers.add(frameId);
			return numbers;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public final long getTimestamp()
		{
			return timestamp;
		}
		
		
		/**
		 * @param timestamp the timestamp to set
		 */
		public final void setTimestamp(final long timestamp)
		{
			this.timestamp = timestamp;
		}
		
		
		/**
		 * @return the camId
		 */
		public final int getCamId()
		{
			return camId;
		}
		
		
		/**
		 * @param camId the camId to set
		 */
		public final void setCamId(final int camId)
		{
			this.camId = camId;
		}
		
		
		/**
		 * @return the pos
		 */
		public final IVector3 getPos()
		{
			return pos;
		}
		
		
		/**
		 * @param pos the pos to set
		 */
		public final void setPos(final IVector3 pos)
		{
			this.pos = pos;
		}
		
		
		/**
		 * @return the frameId
		 */
		public final long getFrameId()
		{
			return frameId;
		}
		
		
		/**
		 * @param frameId the frameId to set
		 */
		public final void setFrameId(final long frameId)
		{
			this.frameId = frameId;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("RawBall [timestamp=");
			builder.append(timestamp);
			builder.append(", camId=");
			builder.append(camId);
			builder.append(", pos=");
			builder.append(pos);
			builder.append(", frameId=");
			builder.append(frameId);
			builder.append("]");
			return builder.toString();
		}
	}
	
	/**
	 */
	public static class WpBall implements IJsonString, INumberListable
	{
		private IVector3	pos			= Vector3.ZERO_VECTOR;
		private IVector3	vel			= Vector3.ZERO_VECTOR;
		private IVector3	acc			= Vector3.ZERO_VECTOR;
		private long		frameId		= -1;
		private long		timestamp	= 0;
		
		
		/**
		 * 
		 */
		public WpBall()
		{
		}
		
		
		/**
		 * @param pos
		 * @param vel
		 * @param acc
		 * @param frameId
		 * @param timestamp
		 */
		public WpBall(final IVector3 pos, final IVector3 vel, final IVector3 acc, final long frameId, final long timestamp)
		{
			super();
			this.pos = pos;
			this.vel = vel;
			this.acc = acc;
			this.frameId = frameId;
			this.timestamp = timestamp;
		}
		
		
		/**
		 * @param list
		 * @return
		 */
		public static WpBall fromNumberList(final List<? extends Number> list)
		{
			return new WpBall(Vector3.fromNumberList(list.subList(0, 3)),
					Vector3.fromNumberList(list.subList(3, 6)),
					Vector3.fromNumberList(list.subList(6, 9)),
					list.size() > 10 ? list.get(10).longValue() : -1,
					list.size() > 11 ? list.get(11).longValue() : -1);
		}
		
		
		/**
		 * @return
		 */
		public TrackedBall toTrackedBall()
		{
			return new TrackedBall(pos, vel, acc, 0, true);
		}
		
		
		@Override
		public JSONObject toJSON()
		{
			Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
			jsonMapping.put("pos", pos.toJSON());
			jsonMapping.put("vel", vel.toJSON());
			jsonMapping.put("frameId", frameId);
			jsonMapping.put("timestamp", timestamp);
			return new JSONObject(jsonMapping);
		}
		
		
		@Override
		public List<Number> getNumberList()
		{
			List<Number> numbers = new ArrayList<>();
			numbers.addAll(pos.getNumberList());
			numbers.addAll(vel.getNumberList());
			numbers.addAll(acc.getNumberList());
			numbers.add(frameId);
			numbers.add(timestamp);
			return numbers;
		}
		
		
		/**
		 * @return the pos
		 */
		public final IVector3 getPos()
		{
			return pos;
		}
		
		
		/**
		 * @param pos the pos to set
		 */
		public final void setPos(final IVector3 pos)
		{
			this.pos = pos;
		}
		
		
		/**
		 * @return the vel
		 */
		public final IVector3 getVel()
		{
			return vel;
		}
		
		
		/**
		 * @param vel the vel to set
		 */
		public final void setVel(final IVector3 vel)
		{
			this.vel = vel;
		}
		
		
		/**
		 * @return the acc
		 */
		public final IVector3 getAcc()
		{
			return acc;
		}
		
		
		/**
		 * @param acc the acc to set
		 */
		public final void setAcc(final IVector3 acc)
		{
			this.acc = acc;
		}
		
		
		/**
		 * @return the frameId
		 */
		public final long getFrameId()
		{
			return frameId;
		}
		
		
		/**
		 * @param frameId the frameId to set
		 */
		public final void setFrameId(final long frameId)
		{
			this.frameId = frameId;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public final long getTimestamp()
		{
			return timestamp;
		}
		
		
		/**
		 * @param timestamp the timestamp to set
		 */
		public final void setTimestamp(final long timestamp)
		{
			this.timestamp = timestamp;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("WpBall [pos=");
			builder.append(pos);
			builder.append(", vel=");
			builder.append(vel);
			builder.append(", acc=");
			builder.append(acc);
			builder.append(", frameId=");
			builder.append(frameId);
			builder.append(", timestamp=");
			builder.append(timestamp);
			builder.append("]");
			return builder.toString();
		}
	}
	
	/**
	 */
	public static class RawBot implements IJsonString, INumberListable
	{
		private long			timestamp	= 0;
		private int				camId			= -1;
		private int				id				= -1;
		private ETeamColor	color			= ETeamColor.UNINITIALIZED;
		private IVector3		pos			= Vector3.ZERO_VECTOR;
		private long			frameId		= -1;
		
		
		/**
		 * 
		 */
		public RawBot()
		{
		}
		
		
		/**
		 * @param timestamp
		 * @param camId
		 * @param id
		 * @param color
		 * @param pos
		 * @param frameId
		 */
		public RawBot(final long timestamp, final int camId, final int id, final ETeamColor color, final IVector3 pos,
				final long frameId)
		{
			super();
			this.timestamp = timestamp;
			this.camId = camId;
			this.pos = pos;
			this.id = id;
			this.color = color;
			this.frameId = frameId;
		}
		
		
		/**
		 * @param list
		 * @return
		 */
		public static RawBot fromNumberList(final List<? extends Number> list)
		{
			return new RawBot(list.get(0).longValue(),
					list.get(1).intValue(),
					list.get(2).intValue(),
					ETeamColor.fromNumberList(list.get(3)),
					Vector3.fromNumberList(list.subList(4, 7)),
					list.size() > 7 ? list.get(7).longValue() : -1);
		}
		
		
		/**
		 * @return
		 */
		public CamRobot toCamRobot()
		{
			return new CamRobot(0, id, pos.x(), pos.y(), pos.z(), 0, 0, 0, timestamp, camId);
		}
		
		
		@Override
		public JSONObject toJSON()
		{
			Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
			jsonMapping.put("timestamp", timestamp);
			jsonMapping.put("camId", camId);
			jsonMapping.put("pos", pos.toJSON());
			jsonMapping.put("frameId", frameId);
			return new JSONObject(jsonMapping);
		}
		
		
		@Override
		public List<Number> getNumberList()
		{
			List<Number> numbers = new ArrayList<>();
			numbers.add(timestamp);
			numbers.add(camId);
			numbers.add(id);
			numbers.addAll(color.getNumberList());
			numbers.addAll(pos.getNumberList());
			numbers.add(frameId);
			return numbers;
		}
		
		
		/**
		 * @return the id
		 */
		public final int getId()
		{
			return id;
		}
		
		
		/**
		 * @param id the id to set
		 */
		public final void setId(final int id)
		{
			this.id = id;
		}
		
		
		/**
		 * @return the color
		 */
		public final ETeamColor getColor()
		{
			return color;
		}
		
		
		/**
		 * @param color the color to set
		 */
		public final void setColor(final ETeamColor color)
		{
			this.color = color;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public final long getTimestamp()
		{
			return timestamp;
		}
		
		
		/**
		 * @param timestamp the timestamp to set
		 */
		public final void setTimestamp(final long timestamp)
		{
			this.timestamp = timestamp;
		}
		
		
		/**
		 * @return the camId
		 */
		public final int getCamId()
		{
			return camId;
		}
		
		
		/**
		 * @param camId the camId to set
		 */
		public final void setCamId(final int camId)
		{
			this.camId = camId;
		}
		
		
		/**
		 * @return the pos
		 */
		public final IVector3 getPos()
		{
			return pos;
		}
		
		
		/**
		 * @param pos the pos to set
		 */
		public final void setPos(final IVector3 pos)
		{
			this.pos = pos;
		}
		
		
		/**
		 * @return the frameId
		 */
		public final long getFrameId()
		{
			return frameId;
		}
		
		
		/**
		 * @param frameId the frameId to set
		 */
		public final void setFrameId(final long frameId)
		{
			this.frameId = frameId;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("RawBot [timestamp=");
			builder.append(timestamp);
			builder.append(", camId=");
			builder.append(camId);
			builder.append(", id=");
			builder.append(id);
			builder.append(", color=");
			builder.append(color);
			builder.append(", pos=");
			builder.append(pos);
			builder.append(", frameId=");
			builder.append(frameId);
			builder.append("]");
			return builder.toString();
		}
	}
	
	/**
	 */
	public static class WpBot implements IJsonString, INumberListable
	{
		private int				id				= -1;
		private ETeamColor	color			= ETeamColor.UNINITIALIZED;
		private IVector3		pos			= Vector3.ZERO_VECTOR;
		private IVector3		vel			= Vector3.ZERO_VECTOR;
		private IVector3		acc			= Vector3.ZERO_VECTOR;
		private long			frameId		= -1;
		private long			timestamp	= 0;
		
		
		/**
		 * 
		 */
		public WpBot()
		{
		}
		
		
		/**
		 * @param pos
		 * @param vel
		 * @param acc
		 * @param id
		 * @param color
		 * @param frameId
		 * @param timestamp
		 */
		public WpBot(final IVector3 pos, final IVector3 vel, final IVector3 acc, final int id, final ETeamColor color,
				final long frameId, final long timestamp)
		{
			super();
			this.pos = pos;
			this.vel = vel;
			this.acc = acc;
			this.id = id;
			this.color = color;
			this.frameId = frameId;
			this.timestamp = timestamp;
		}
		
		
		/**
		 * @param list
		 * @return
		 */
		public static WpBot fromNumberList(final List<? extends Number> list)
		{
			return new WpBot(Vector3.fromNumberList(list.subList(0, 3)),
					Vector3.fromNumberList(list.subList(3, 6)),
					Vector3.fromNumberList(list.subList(6, 9)),
					list.get(9).intValue(),
					ETeamColor.fromNumberList(list.get(10)),
					list.size() > 11 ? list.get(11).longValue() : -1,
					list.size() > 12 ? list.get(12).longValue() : -1);
		}
		
		
		/**
		 * @return
		 */
		public TrackedTigerBot toTrackedBot()
		{
			BotID botId = BotID.createBotId(id, color);
			return new TrackedTigerBot(botId, pos.getXYVector(), vel.getXYVector(), acc.getXYVector(), 0, pos.z(),
					vel.z(), acc.z(), 0, new DummyBot(botId), color);
		}
		
		
		@Override
		public JSONObject toJSON()
		{
			Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
			jsonMapping.put("pos", pos.toJSON());
			jsonMapping.put("vel", vel.toJSON());
			jsonMapping.put("acc", acc.toJSON());
			jsonMapping.put("id", id);
			jsonMapping.put("color", color.name());
			jsonMapping.put("frameId", frameId);
			jsonMapping.put("timestamp", timestamp);
			return new JSONObject(jsonMapping);
		}
		
		
		@Override
		public List<Number> getNumberList()
		{
			List<Number> numbers = new ArrayList<>();
			numbers.add(id);
			numbers.addAll(color.getNumberList());
			numbers.addAll(pos.getNumberList());
			numbers.addAll(vel.getNumberList());
			numbers.addAll(acc.getNumberList());
			numbers.add(frameId);
			numbers.add(timestamp);
			return numbers;
		}
		
		
		/**
		 * @return the id
		 */
		public final int getId()
		{
			return id;
		}
		
		
		/**
		 * @param id the id to set
		 */
		public final void setId(final int id)
		{
			this.id = id;
		}
		
		
		/**
		 * @return the color
		 */
		public final ETeamColor getColor()
		{
			return color;
		}
		
		
		/**
		 * @param color the color to set
		 */
		public final void setColor(final ETeamColor color)
		{
			this.color = color;
		}
		
		
		/**
		 * @return the pos
		 */
		public final IVector3 getPos()
		{
			return pos;
		}
		
		
		/**
		 * @param pos the pos to set
		 */
		public final void setPos(final IVector3 pos)
		{
			this.pos = pos;
		}
		
		
		/**
		 * @return the vel
		 */
		public final IVector3 getVel()
		{
			return vel;
		}
		
		
		/**
		 * @param vel the vel to set
		 */
		public final void setVel(final IVector3 vel)
		{
			this.vel = vel;
		}
		
		
		/**
		 * @return the acc
		 */
		public final IVector3 getAcc()
		{
			return acc;
		}
		
		
		/**
		 * @param acc the acc to set
		 */
		public final void setAcc(final IVector3 acc)
		{
			this.acc = acc;
		}
		
		
		/**
		 * @return the frameId
		 */
		public final long getFrameId()
		{
			return frameId;
		}
		
		
		/**
		 * @param frameId the frameId to set
		 */
		public final void setFrameId(final long frameId)
		{
			this.frameId = frameId;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public final long getTimestamp()
		{
			return timestamp;
		}
		
		
		/**
		 * @param timestamp the timestamp to set
		 */
		public final void setTimestamp(final long timestamp)
		{
			this.timestamp = timestamp;
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("WpBot [id=");
			builder.append(id);
			builder.append(", color=");
			builder.append(color);
			builder.append(", pos=");
			builder.append(pos);
			builder.append(", vel=");
			builder.append(vel);
			builder.append(", acc=");
			builder.append(acc);
			builder.append(", frameId=");
			builder.append(frameId);
			builder.append(", timestamp=");
			builder.append(timestamp);
			builder.append("]");
			return builder.toString();
		}
	}
	
	
	/**
	 * @return the customNumberListable
	 */
	public final Map<String, INumberListable> getCustomNumberListable()
	{
		return customNumberListable;
	}
}
