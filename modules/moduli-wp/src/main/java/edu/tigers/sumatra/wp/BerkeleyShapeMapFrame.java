/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;


@Entity(version = 1)
public class BerkeleyShapeMapFrame
{
	@PrimaryKey
	private final long timestamp;

	/** Old map for compatibility to older DBs */
	private Map<String, ShapeMap> shapeMaps = null;
	private final Map<ShapeMapSource, ShapeMap> shapeMapsBySource = new HashMap<>();


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


	public void putShapeMap(ShapeMapSource source, ShapeMap shapeMap)
	{
		shapeMapsBySource.put(source, shapeMap);
	}


	public Map<ShapeMapSource, ShapeMap> getShapeMaps()
	{
		if (shapeMaps != null)
		{
			// conversion
			shapeMaps.forEach((key, value) -> shapeMapsBySource.put(toSource(key), value));
			shapeMaps = null;
		}
		return Collections.unmodifiableMap(shapeMapsBySource);
	}


	private ShapeMapSource toSource(String name)
	{
		if (name.startsWith("Skill"))
		{
			return ShapeMapSource.of(name, "Skills");
		}

		return ShapeMapSource.of(name);
	}


	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("timestamp", timestamp)
				.append("shapeMaps", shapeMaps)
				.append("shapeMapsBySource", shapeMapsBySource)
				.toString();
	}
}
