package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;


@Persistent
public class UnsportingBehaviorMajor extends AGameEvent
{
	private final ETeamColor team;
	private final String reason;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected UnsportingBehaviorMajor()
	{
		reason = null;
		team = null;
	}
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public UnsportingBehaviorMajor(SslGameEvent.GameEvent event)
	{
		super(event);
		team = toTeamColor(event.getUnsportingBehaviorMajor().getByTeam());
		reason = event.getUnsportingBehaviorMajor().getReason();
	}
	
	
	public UnsportingBehaviorMajor(final ETeamColor team, final String reason)
	{
		super(EGameEvent.UNSPORTING_BEHAVIOR_MAJOR);
		this.team = team;
		this.reason = reason;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.UNSPORTING_BEHAVIOR_MAJOR);
		builder.getUnsportingBehaviorMajorBuilder()
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
		return "Major unsporting behavior by " + team + ": " + reason;
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final UnsportingBehaviorMajor that = (UnsportingBehaviorMajor) o;
		
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
