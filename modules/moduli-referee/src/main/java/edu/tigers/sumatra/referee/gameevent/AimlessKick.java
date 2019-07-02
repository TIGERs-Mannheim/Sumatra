/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class AimlessKick extends AGameEvent
{
	private final ETeamColor team;
	private final Integer bot;
	private final IVector2 location;
	private final IVector2 kickLocation;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected AimlessKick()
	{
		team = null;
		bot = 0;
		location = null;
		kickLocation = null;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 * 
	 * @param event a protobuf event
	 */
	public AimlessKick(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getAimlessKick().getByTeam());
		this.bot = event.getAimlessKick().getByBot();
		this.location = toVector(event.getAimlessKick().getLocation());
		this.kickLocation = toVector(event.getAimlessKick().getKickLocation());
	}
	
	
	public AimlessKick(BotID bot, IVector2 location, IVector2 kickLocation)
	{
		super(EGameEvent.AIMLESS_KICK);
		this.team = bot == null ? null : bot.getTeamColor();
		this.bot = bot == null ? null : bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.AIMLESS_KICK);
		builder.getAimlessKickBuilder()
				.setLocation(getLocationFromVector(location));
		
		if (bot != null)
		{
			builder.getAimlessKickBuilder().setByBot(bot);
		}
		
		if (team != null)
		{
			builder.getAimlessKickBuilder().setByTeam(getTeam(team));
		}
		
		if (kickLocation != null)
		{
			builder.getAimlessKickBuilder().setKickLocation(getLocationFromVector(kickLocation));
		}
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Aimless kick by Bot %d %s @ %s", bot, team, formatVector(location));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final AimlessKick that = (AimlessKick) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(team, that.team)
				.append(location, that.location)
				.append(kickLocation, that.kickLocation)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(bot)
				.append(location)
				.append(kickLocation)
				.toHashCode();
	}
}
