/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.snapshot;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Lukas Schmierer <lukas.schmierer@lschmierer.de>
 */
public class Snapshot
{
	private Map<BotID, SnapObject>	bots;
	private SnapObject					ball;
	
	
	/**
	 * @param bots
	 * @param ball
	 */
	public Snapshot(final Map<BotID, SnapObject> bots, final SnapObject ball)
	{
		this.bots = bots;
		this.ball = ball;
	}
	
	
	/**
	 * @return the bots
	 */
	public Map<BotID, SnapObject> getBots()
	{
		return bots;
	}
	
	
	/**
	 * @param bots the bots to set
	 */
	public void setBots(final Map<BotID, SnapObject> bots)
	{
		this.bots = bots;
	}
	
	
	/**
	 * @return the ball
	 */
	public SnapObject getBall()
	{
		return ball;
	}
	
	
	/**
	 * @param ball the ball to set
	 */
	public void setBall(final SnapObject ball)
	{
		this.ball = ball;
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (!(o instanceof Snapshot))
			return false;
		
		final Snapshot snapshot = (Snapshot) o;
		
		return new EqualsBuilder()
				.append(getBots(), snapshot.getBots())
				.append(getBall(), snapshot.getBall())
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getBots())
				.append(getBall())
				.toHashCode();
	}
	
	
	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject obj = new JSONObject();
		obj.put("bots", botsToJSON(bots));
		obj.put("ball", ball.toJSON());
		return obj;
	}
	
	
	/**
	 * @param obj
	 * @return
	 */
	public static Snapshot fromJSON(final JSONObject obj)
	{
		Map<BotID, SnapObject> bots = botsFromJSON((JSONArray) obj.get("bots"));
		SnapObject ball = SnapObject.fromJSON((JSONObject) obj.get("ball"));
		return new Snapshot(bots, ball);
	}
	
	
	/**
	 * @param json
	 * @return
	 * @throws ParseException
	 */
	public static Snapshot fromJSONString(final String json) throws ParseException
	{
		JSONParser parser = new JSONParser();
		return Snapshot.fromJSON((JSONObject) parser.parse(json));
	}
	
	
	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static JSONArray botsToJSON(final Map<BotID, SnapObject> bots)
	{
		JSONArray array = new JSONArray();
		for (Entry<BotID, SnapObject> entry : bots.entrySet())
		{
			JSONObject botObj = new JSONObject();
			botObj.put("id", botIdToJSON(entry.getKey()));
			botObj.put("obj", entry.getValue().toJSON());
			array.add(botObj);
		}
		return array;
	}
	
	
	/**
	 * @param array
	 * @return
	 */
	private static Map<BotID, SnapObject> botsFromJSON(final JSONArray array)
	{
		Map<BotID, SnapObject> bots = new LinkedHashMap<>();
		for (Object obj : array)
		{
			JSONObject jsonObj = (JSONObject) obj;
			bots.put(botIdFromJSON((JSONObject) jsonObj.get("id")), SnapObject.fromJSON((JSONObject) jsonObj.get("obj")));
		}
		return bots;
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static JSONObject botIdToJSON(final BotID botId)
	{
		JSONObject obj = new JSONObject();
		obj.put("number", botId.getNumber());
		obj.put("color", botId.getTeamColor().name());
		return obj;
	}
	
	
	/**
	 * @param jsonBotId
	 * @return
	 */
	private static BotID botIdFromJSON(final JSONObject jsonBotId)
	{
		return BotID.createBotId((int) (long) jsonBotId.get("number"),
				ETeamColor.valueOf((String) jsonBotId.get("color")));
	}
	
	
	/**
	 * @param path to the snapshot file
	 * @return the loaded snapshot
	 * @throws IOException on any error
	 */
	public static Snapshot loadFromFile(final String path) throws IOException
	{
		return loadFromFile(Paths.get(path));
	}
	
	
	/**
	 * @param path to the snapshot file
	 * @return the loaded snapshot
	 * @throws IOException on any error
	 */
	public static Snapshot loadFromFile(final Path path) throws IOException
	{
		byte[] encoded = Files.readAllBytes(path);
		String json = new String(encoded, StandardCharsets.UTF_8);
		
		try
		{
			return Snapshot.fromJSONString(json);
		} catch (ParseException err)
		{
			throw new IOException("Could not parse snapshot.", err);
		}
	}
	
	
	/**
	 * Load a snapshot from a resource file
	 * 
	 * @param filename name/path to the file in classpath
	 * @return the snapshot
	 * @throws IOException on any error
	 */
	public static Snapshot loadFromResources(final String filename)
			throws IOException
	{
		Path path;
		try
		{
			URL url = Snapshot.class.getClassLoader().getResource(filename);
			if (url == null)
			{
				throw new IOException("Resource not found: " + filename);
			}
			path = Paths.get(url.toURI());
		} catch (URISyntaxException e)
		{
			throw new IOException("Could not get path to resource file.", e);
		}
		return loadFromFile(path);
	}
	
	
	/**
	 * @param target
	 * @throws IOException
	 */
	public void save(final String target) throws IOException
	{
		File file = Paths.get("", target).toFile();
		// noinspection ResultOfMethodCallIgnored
		file.getParentFile().mkdirs();
		boolean fileCreated = file.createNewFile();
		if (!fileCreated)
		{
			throw new IOException("Could not create snapshot file: " + file.getAbsolutePath());
		}
		
		Files.write(file.toPath(), toJSON().toJSONString().getBytes(StandardCharsets.UTF_8));
	}
	
	
	@Override
	public String toString()
	{
		return "Snapshot{" +
				"bots=" + bots +
				", ball=" + ball +
				'}';
	}
}
