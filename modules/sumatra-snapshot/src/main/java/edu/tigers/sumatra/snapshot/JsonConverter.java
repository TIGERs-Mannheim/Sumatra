/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.snapshot;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonConverter
{
	static String encode(final Enum<?> e)
	{
		if (e == null)
		{
			return null;
		}
		return e.name();
	}


	@SuppressWarnings("unchecked")
	static JSONArray encode(final IVector3 vec)
	{
		JSONArray array = new JSONArray();
		array.add(vec.x());
		array.add(vec.y());
		array.add(vec.z());
		return array;
	}


	static IVector3 decodeVector3(final JSONArray jsonArray)
	{
		Vector3 vector = Vector3.zero();
		if (jsonArray == null)
		{
			return vector;
		}
		for (int i = 0; i < jsonArray.size(); i++)
		{
			vector.set(i, ((Number) jsonArray.get(i)).doubleValue());
		}
		return vector;
	}


	@SuppressWarnings("unchecked")
	static JSONArray encode(final IVector2 vec)
	{
		JSONArray array = new JSONArray();
		array.add(vec.x());
		array.add(vec.y());
		return array;
	}


	static IVector2 decodeVector2(final JSONArray jsonArray)
	{
		Vector2 vector = Vector2.zero();
		if (jsonArray == null)
		{
			return vector;
		}
		for (int i = 0; i < jsonArray.size(); i++)
		{
			vector.set(i, ((Number) jsonArray.get(i)).doubleValue());
		}
		return vector;
	}


	@SuppressWarnings("unchecked")
	static JSONArray encode(final Map<BotID, SnapObject> bots)
	{
		JSONArray array = new JSONArray();
		for (Map.Entry<BotID, SnapObject> entry : bots.entrySet())
		{
			JSONObject botObj = new JSONObject();
			botObj.put("id", encode(entry.getKey()));
			botObj.put("obj", entry.getValue().toJSON());
			array.add(botObj);
		}
		return array;
	}


	/**
	 * @param array
	 * @return
	 */
	static Map<BotID, SnapObject> decodeBots(final JSONArray array)
	{
		Map<BotID, SnapObject> bots = new LinkedHashMap<>();
		for (Object obj : array)
		{
			JSONObject jsonObj = (JSONObject) obj;
			bots.put(decodeBotId((JSONObject) jsonObj.get("id")), SnapObject.fromJSON((JSONObject) jsonObj.get("obj")));
		}
		return bots;
	}


	@SuppressWarnings("unchecked")
	private static JSONObject encode(final BotID botId)
	{
		JSONObject obj = new JSONObject();
		obj.put("number", botId.getNumber());
		obj.put("color", encode(botId.getTeamColor()));
		return obj;
	}


	private static BotID decodeBotId(final JSONObject jsonBotId)
	{
		return BotID.createBotId((int) (long) jsonBotId.get("number"),
				ETeamColor.valueOf((String) jsonBotId.get("color")));
	}
}
