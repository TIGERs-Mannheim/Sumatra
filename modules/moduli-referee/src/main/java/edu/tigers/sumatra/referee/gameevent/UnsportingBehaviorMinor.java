package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;


@Persistent
public class UnsportingBehaviorMinor extends AGameEvent
{
	private final ETeamColor team;
	private final String reason;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected UnsportingBehaviorMinor()
	{
		team = null;
		reason = null;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public UnsportingBehaviorMinor(SslGameEvent.GameEvent event)
	{
		super(event);
		team = toTeamColor(event.getUnsportingBehaviorMinor().getByTeam());
		reason = event.getUnsportingBehaviorMinor().getReason();
	}
	
	
	public UnsportingBehaviorMinor(final ETeamColor team, final String reason)
	{
		super(EGameEvent.UNSPORTING_BEHAVIOR_MINOR);
		this.team = team;
		this.reason = reason;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.UNSPORTING_BEHAVIOR_MINOR);
		builder.getUnsportingBehaviorMinorBuilder()
				.setByTeam(getTeam(team))
				.setReason(reason);
		
		return builder.build();
	}
	
	
	public ETeamColor getTeam()
	{
		return team;
	}
	
	
	public String getReason()
	{
		return reason;
	}
	
	
	@Override
	public String toString()
	{
		return "Minor unsporting behavior by " + team + ": " + reason;
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final UnsportingBehaviorMinor that = (UnsportingBehaviorMinor) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(team, that.team)
				.append(reason, that.reason)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(reason)
				.toHashCode();
	}
}
