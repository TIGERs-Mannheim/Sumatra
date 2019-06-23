/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.tigers.sumatra.drawable.ShapeMap;


@Entity
public class BerkeleyShapeMapFrame
{
	@PrimaryKey
	private final long timestamp;
	
	private final Map<String, ShapeMap> shapeMaps = new HashMap<>();
	
	
	@SuppressWarnings("unused")
	private BerkeleyShapeMapFrame()
	{
		timestamp = 0;
	}
	
	
	public BerkeleyShapeMapFrame(final long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	public void putShapeMap(String source, ShapeMap shapeMap)
	{
		shapeMaps.put(source, shapeMap);
	}
	
	
	public ShapeMap getShapeMap(String source)
	{
		return shapeMaps.get(source);
	}
	
	
	public Map<String, ShapeMap> getShapeMaps()
	{
		return Collections.unmodifiableMap(shapeMaps);
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this)
				.append("timestamp", timestamp)
				.append("shapeMaps", shapeMaps)
				.toString();
	}
}
