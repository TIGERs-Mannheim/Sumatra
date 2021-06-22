/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.snapshot;

import edu.tigers.sumatra.math.vector.Vector3;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SnapObjectTest
{
	@Test
	public void testToJSON()
	{
		SnapObject obj = new SnapObject(Vector3.zero(), Vector3.zero());
		assertEquals("{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]}", obj.toJSON().toJSONString());
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.zero());
		assertEquals("{\"pos\":[0.0,1.0,0.0],\"vel\":[0.0,0.0,0.0]}", obj.toJSON().toJSONString());
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.fromXYZ(3, 4, 0));
		assertEquals("{\"pos\":[0.0,1.0,0.0],\"vel\":[3.0,4.0,0.0]}", obj.toJSON().toJSONString());
	}


	@Test
	public void testFromJSON() throws ParseException
	{
		JSONParser parser = new JSONParser();
		SnapObject obj = new SnapObject(Vector3.zero(), Vector3.zero());
		assertEquals(obj,
				SnapObject.fromJSON((JSONObject) parser.parse("{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]}")));
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.zero());
		assertEquals(obj,
				SnapObject.fromJSON((JSONObject) parser.parse("{\"pos\":[0.0,1.0,0.0],\"vel\":[0.0,0.0,0.0]}")));
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.fromXYZ(3, 4, 0));
		assertEquals(obj,
				SnapObject.fromJSON((JSONObject) parser.parse("{\"pos\":[0.0,1.0,0.0],\"vel\":[3.0,4.0,0.0]}")));
	}
}
