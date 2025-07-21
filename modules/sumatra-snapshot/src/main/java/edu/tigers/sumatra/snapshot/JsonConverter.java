/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.snapshot;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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


	static JsonArray encode(final IVector3 vec)
	{
		JsonArray array = new JsonArray();
		array.add(vec.x());
		array.add(vec.y());
		array.add(vec.z());
		return array;
	}


	static IVector3 decodeVector3(final JsonArray jsonArray)
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


	static JsonArray encode(final IVector2 vec)
	{
		JsonArray array = new JsonArray();
		array.add(vec.x());
		array.add(vec.y());
		return array;
	}


	static IVector2 decodeVector2(final JsonArray jsonArray)
	{
		return decodeVector2(jsonArray, Vector2.zero());
	}


	static IVector2 decodeVector2(final JsonArray jsonArray, IVector2 defaultValue)
	{
		Vector2 vector = Vector2.zero();
		if (jsonArray == null)
		{
			return defaultValue;
		}
		for (int i = 0; i < jsonArray.size(); i++)
		{
			vector.set(i, ((Number) jsonArray.get(i)).doubleValue());
		}
		return vector;
	}


	static JsonArray encode(final Map<BotID, SnapObject> bots)
	{
		JsonArray array = new JsonArray();
		for (Map.Entry<BotID, SnapObject> entry : bots.entrySet())
		{
			JsonObject botObj = new JsonObject();
			botObj.put("id", encode(entry.getKey()));
			botObj.put("obj", entry.getValue().toJSON());
			array.add(botObj);
		}
		return array;
	}


	static JsonArray encodeVector3Map(final Map<BotID, IVector3> vectors)
	{
		JsonArray array = new JsonArray();
		for (Map.Entry<BotID, IVector3> entry : vectors.entrySet())
		{
			JsonObject botObj = new JsonObject();
			botObj.put("id", encode(entry.getKey()));
			botObj.put("dest", JsonConverter.encode(entry.getValue()));
			array.add(botObj);
		}
		return array;
	}


	/**
	 * @param array
	 * @return
	 */
	static Map<BotID, SnapObject> decodeBots(final JsonArray array)
	{
		if (array == null)
		{
			return Map.of();
		}
		Map<BotID, SnapObject> bots = new LinkedHashMap<>();
		for (Object obj : array)
		{
			JsonObject jsonObj = (JsonObject) obj;
			bots.put(decodeBotId((JsonObject) jsonObj.get("id")), SnapObject.fromJSON((JsonObject) jsonObj.get("obj")));
		}
		return bots;
	}


	/**
	 * @param array
	 * @return
	 */
	static Map<BotID, IVector3> decodeVector3Map(final JsonArray array)
	{
		if (array == null)
		{
			return Map.of();
		}
		Map<BotID, IVector3> bots = new LinkedHashMap<>();
		for (Object obj : array)
		{
			JsonObject jsonObj = (JsonObject) obj;
			bots.put(decodeBotId((JsonObject) jsonObj.get("id")),
					JsonConverter.decodeVector3((JsonArray) jsonObj.get("dest")));
		}
		return bots;
	}


	private static JsonObject encode(final BotID botId)
	{
		JsonObject obj = new JsonObject();
		obj.put("number", botId.getNumber());
		obj.put("color", encode(botId.getTeamColor()));
		return obj;
	}


	private static BotID decodeBotId(final JsonObject jsonBotId)
	{
		return BotID.createBotId(((BigDecimal) jsonBotId.get("number")).intValue(),
				ETeamColor.valueOf((String) jsonBotId.get("color")));
	}
}
