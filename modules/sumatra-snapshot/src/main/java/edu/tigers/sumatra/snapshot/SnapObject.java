/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.05.2016
 * Author(s): lukas
 * *********************************************************
 */
package edu.tigers.sumatra.snapshot;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Lukas Schmierer <lukas.schmierer@lschmierer.de>
 */
public class SnapObject
{
	private final IVector3	pos;
	private final IVector3	vel;
	
	
	/**
	 * @param pos
	 * @param vel
	 */
	public SnapObject(final IVector3 pos, final IVector3 vel)
	{
		this.pos = pos;
		this.vel = vel;
	}
	
	
	/**
	 * @return the pos
	 */
	public final IVector3 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @return the vel
	 */
	public final IVector3 getVel()
	{
		return vel;
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (!(o instanceof SnapObject))
			return false;
		
		final SnapObject that = (SnapObject) o;
		
		return new EqualsBuilder()
				.append(getPos(), that.getPos())
				.append(getVel(), that.getVel())
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getPos())
				.append(getVel())
				.toHashCode();
	}
	
	
	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject obj = new JSONObject();
		obj.put("pos", vec3ToJSON(pos));
		obj.put("vel", vec3ToJSON(vel));
		return obj;
	}
	
	
	/**
	 * @param obj to read from
	 * @return snapObject from json
	 */
	public static SnapObject fromJSON(final JSONObject obj)
	{
		return new SnapObject(vec3FromJSON((JSONArray) obj.get("pos")), vec3FromJSON((JSONArray) obj.get("vel")));
	}
	
	
	@SuppressWarnings("unchecked")
	private static JSONArray vec3ToJSON(final IVector3 vec)
	{
		JSONArray array = new JSONArray();
		array.add(vec.x());
		array.add(vec.y());
		array.add(vec.z());
		return array;
	}
	
	
	private static IVector3 vec3FromJSON(final JSONArray jsonArray)
	{
		return Vector3.fromXYZ((double) jsonArray.get(0), (double) jsonArray.get(1), (double) jsonArray.get(2));
	}
	
	
	@Override
	public String toString()
	{
		return "SnapObject{" +
				"pos=" + pos +
				", vel=" + vel +
				'}';
	}
}
