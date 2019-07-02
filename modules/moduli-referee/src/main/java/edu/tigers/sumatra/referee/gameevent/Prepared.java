/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;


@Persistent
public class Prepared extends AGameEvent
{
	private final double timeTaken;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected Prepared()
	{
		timeTaken = 0;
	}
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public Prepared(SslGameEvent.GameEvent event)
	{
		super(event);
		this.timeTaken = event.getPrepared().getTimeTaken();
	}
	
	
	/**
	 * @param timeTaken [s]
	 */
	public Prepared(double timeTaken)
	{
		super(EGameEvent.PREPARED);
		this.timeTaken = timeTaken;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.PREPARED);
		builder.getPreparedBuilder().setTimeTaken((float) timeTaken);
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Both teams are prepared after %.2f s", timeTaken);
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final Prepared prepared = (Prepared) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(timeTaken, prepared.timeTaken)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(timeTaken)
				.toHashCode();
	}
}
