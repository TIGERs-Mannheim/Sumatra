/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.persistence.PersistenceTable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class PersistenceShapeMapFrame implements PersistenceTable.IEntry<PersistenceShapeMapFrame>
{
	private final long timestamp;

	private final Map<ShapeMapSource, ShapeMap> shapeMapsBySource = new HashMap<>();


	public PersistenceShapeMapFrame(final long timestamp)
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


	@Override
	public long getKey()
	{
		return getTimestamp();
	}


	@Override
	public void merge(PersistenceShapeMapFrame other)
	{
		shapeMapsBySource.putAll(other.shapeMapsBySource);
	}
}
