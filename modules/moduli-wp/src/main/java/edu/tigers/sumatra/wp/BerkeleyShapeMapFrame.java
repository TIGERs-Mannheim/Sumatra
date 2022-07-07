/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Entity(version = 1)
public class BerkeleyShapeMapFrame
{
	@PrimaryKey
	private final long timestamp;

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
		return Collections.unmodifiableMap(shapeMapsBySource);
	}


	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("timestamp", timestamp)
				.append("shapeMapsBySource", shapeMapsBySource)
				.toString();
	}
}
