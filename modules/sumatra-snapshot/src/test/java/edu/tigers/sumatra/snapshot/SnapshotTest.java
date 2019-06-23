/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.05.2016
 * Author(s): Lukas Schmierer <lukas.schmierer@lschmierer.de>
 * *********************************************************
 */
package edu.tigers.sumatra.snapshot;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Lukas Schmierer <lukas.schmierer@lschmierer.de>
 */
public class SnapshotTest
{
	
	/**
	 * 
	 */
	@Test
	public void testToJSON()
	{
		Snapshot snapshot = new Snapshot(new HashMap<>(), new SnapObject(Vector3.zero(), Vector3.zero()));
		assertEquals("{\"ball\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"bots\":[]}",
				snapshot.toJSON().toJSONString());
		
		Map<BotID, SnapObject> bots = new HashMap<>();
		bots.put(BotID.createBotId(0, ETeamColor.BLUE), new SnapObject(Vector3.zero(), Vector3.zero()));
		snapshot = new Snapshot(bots, new SnapObject(Vector3.zero(), Vector3.zero()));
		assertEquals(
				"{\"ball\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"bots\":[{\"obj\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"id\":{\"number\":0,\"color\":\"BLUE\"}}]}",
				snapshot.toJSON().toJSONString());
	}
	
	
	/**
	 * @throws ParseException
	 */
	@Test
	public void testFromJSON() throws ParseException
	{
		JSONParser parser = new JSONParser();
		Snapshot snapshot = new Snapshot(new HashMap<>(), new SnapObject(Vector3.zero(), Vector3.zero()));
		assertEquals(snapshot,
				Snapshot.fromJSON(
						(JSONObject) parser.parse("{\"ball\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"bots\":[]}")));
		
		Map<BotID, SnapObject> bots = new HashMap<>();
		bots.put(BotID.createBotId(0, ETeamColor.BLUE), new SnapObject(Vector3.zero(), Vector3.zero()));
		snapshot = new Snapshot(bots, new SnapObject(Vector3.zero(), Vector3.zero()));
		assertEquals(snapshot,
				Snapshot.fromJSON(
						(JSONObject) parser.parse(
								"{\"ball\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"bots\":[{\"obj\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"id\":{\"number\":0,\"color\":\"BLUE\"}}]}")));
	}
	
}
