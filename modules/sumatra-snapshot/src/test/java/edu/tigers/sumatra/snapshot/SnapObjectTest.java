/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.snapshot;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class SnapObjectTest
{
	@Test
	void testToJSON()
	{
		SnapObject obj = new SnapObject(Vector3.zero(), Vector3.zero());
		assertEquals("{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]}", obj.toJSON().toJson());
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.zero());
		assertEquals("{\"pos\":[0.0,1.0,0.0],\"vel\":[0.0,0.0,0.0]}", obj.toJSON().toJson());
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.fromXYZ(3, 4, 0));
		assertEquals("{\"pos\":[0.0,1.0,0.0],\"vel\":[3.0,4.0,0.0]}", obj.toJSON().toJson());

		obj = new SnapObject(Vector3.zero(), Vector3.zero(), Vector2.zero());
		assertEquals("{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0],\"movement\":[0.0,0.0]}", obj.toJSON().toJson());
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.fromXYZ(3, 4, 0), Vector2.fromXY(1, 2));
		assertEquals("{\"pos\":[0.0,1.0,0.0],\"vel\":[3.0,4.0,0.0],\"movement\":[1.0,2.0]}", obj.toJSON().toJson());
	}


	@Test
	void testFromJSON() throws JsonException
	{
		SnapObject obj = new SnapObject(Vector3.zero(), Vector3.zero());
		assertEquals(obj,
				SnapObject.fromJSON((JsonObject) Jsoner.deserialize("{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]}")));
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.zero());
		assertEquals(obj,
				SnapObject.fromJSON((JsonObject) Jsoner.deserialize("{\"pos\":[0.0,1.0,0.0],\"vel\":[0.0,0.0,0.0]}")));
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.fromXYZ(3, 4, 0));
		assertEquals(obj,
				SnapObject.fromJSON((JsonObject) Jsoner.deserialize("{\"pos\":[0.0,1.0,0.0],\"vel\":[3.0,4.0,0.0]}")));

		obj = new SnapObject(Vector3.zero(), Vector3.zero(), Vector2.zero());
		assertEquals(
				obj,
				SnapObject.fromJSON((JsonObject) Jsoner.deserialize(
						"{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0],\"movement\":[0.0,0.0]}"))
		);
		obj = new SnapObject(Vector3.fromXYZ(0, 1, 0), Vector3.fromXYZ(3, 4, 0), Vector2.fromXY(1, 2));
		assertEquals(
				obj,
				SnapObject.fromJSON((JsonObject) Jsoner.deserialize(
						"{\"pos\":[0.0,1.0,0.0],\"vel\":[3.0,4.0,0.0],\"movement\":[1.0,2.0]}"))
		);
	}
}
