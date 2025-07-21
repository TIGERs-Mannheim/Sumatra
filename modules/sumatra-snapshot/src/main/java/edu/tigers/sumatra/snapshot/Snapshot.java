/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.snapshot;


import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;


/**
 * A snapshot of a game situation.
 */
@Value
@Builder(toBuilder = true)
public class Snapshot
{
	@Singular
	Map<BotID, SnapObject> bots;
	SnapObject ball;
	SslGcRefereeMessage.Referee.Command command;
	SslGcRefereeMessage.Referee.Stage stage;
	@Builder.Default
	boolean autoContinue = true;
	IVector2 placementPos;
	@Singular
	Map<BotID, IVector3> moveDestinations;


	/**
	 * @return
	 */
	public JsonObject toJSON()
	{
		JsonObject obj = new JsonObject();
		obj.put("bots", JsonConverter.encode(bots));
		obj.put("ball", ball.toJSON());
		if (command != null)
		{
			obj.put("command", JsonConverter.encode(command));
		}
		if (stage != null)
		{
			obj.put("stage", JsonConverter.encode(stage));
		}
		if (placementPos != null)
		{
			obj.put("placementPos", JsonConverter.encode(placementPos));
		}
		if (moveDestinations != null && !moveDestinations.isEmpty())
		{
			obj.put("moveDestinations", JsonConverter.encodeVector3Map(moveDestinations));
		}
		return obj;
	}


	/**
	 * @param obj
	 * @return
	 */
	public static Snapshot fromJSON(final JsonObject obj)
	{
		Map<BotID, SnapObject> bots = JsonConverter.decodeBots((JsonArray) obj.get("bots"));
		SnapObject ball = SnapObject.fromJSON((JsonObject) obj.get("ball"));
		SslGcRefereeMessage.Referee.Command command = Optional.ofNullable((String) obj.get("command"))
				.map(SslGcRefereeMessage.Referee.Command::valueOf).orElse(null);
		SslGcRefereeMessage.Referee.Stage stage = Optional.ofNullable((String) obj.get("stage"))
				.map(SslGcRefereeMessage.Referee.Stage::valueOf).orElse(null);
		IVector2 placementPos = Optional.ofNullable((JsonArray) obj.get("placementPos"))
				.map(JsonConverter::decodeVector2)
				.orElse(null);
		Map<BotID, IVector3> moveDestinations = JsonConverter.decodeVector3Map((JsonArray) obj.get("moveDestinations"));
		return Snapshot.builder()
				.bots(bots)
				.ball(ball)
				.command(command)
				.stage(stage)
				.placementPos(placementPos)
				.moveDestinations(moveDestinations)
				.build();
	}


	public static Snapshot fromJSONString(final String json) throws JsonException
	{
		return Snapshot.fromJSON((JsonObject) Jsoner.deserialize(json));
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
		} catch (JsonException err)
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
	public void save(final Path target) throws IOException
	{
		Files.createDirectories(target.toAbsolutePath().getParent());

		Files.writeString(
				target,
				toJSON().toJson(),
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE
		);
	}
}
