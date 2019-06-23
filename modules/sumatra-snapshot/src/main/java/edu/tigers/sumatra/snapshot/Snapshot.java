/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.05.2016
 * Author(s): lukas
 * *********************************************************
 */
package edu.tigers.sumatra.snapshot;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	private Map<BotID, SnapObject> bots;
	
	
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
	
	
	private SnapObject ball;
	
	
	/**
	 * @param bots
	 * @param ball
	 */
	public Snapshot(final Map<BotID, SnapObject> bots, final SnapObject ball)
	{
		this.bots = bots;
		this.ball = ball;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((ball == null) ? 0 : ball.hashCode());
		result = (prime * result) + ((bots == null) ? 0 : bots.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Snapshot other = (Snapshot) obj;
		if (ball == null)
		{
			if (other.ball != null)
			{
				return false;
			}
		} else if (!ball.equals(other.ball))
		{
			return false;
		}
		if (bots == null)
		{
			if (other.bots != null)
			{
				return false;
			}
		} else if (!bots.equals(other.bots))
		{
			return false;
		}
		return true;
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
		Map<BotID, SnapObject> bots = new HashMap<>();
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
	 * @param path
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static Snapshot load(final String path) throws ParseException, IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		String json = new String(encoded, StandardCharsets.UTF_8);
		
		return Snapshot.fromJSONString(json);
	}
	
	
	/**
	 * @param target
	 * @throws IOException
	 */
	public void save(final String target) throws IOException
	{
		File file = Paths.get("", target).toFile();
		file.getParentFile().mkdirs();
		file.createNewFile();
		
		Files.write(file.toPath(), toJSON().toJSONString().getBytes(StandardCharsets.UTF_8));
	}
	
}
