package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;


@Persistent
public class MultiplePlacementFailures extends AGameEvent
{
	private final ETeamColor team;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected MultiplePlacementFailures()
	{
		team = null;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public MultiplePlacementFailures(SslGameEvent.GameEvent event)
	{
		super(event);
		team = toTeamColor(event.getMultiplePlacementFailures().getByTeam());
	}
	
	
	public MultiplePlacementFailures(final ETeamColor team)
	{
		super(EGameEvent.MULTIPLE_PLACEMENT_FAILURES);
		this.team = team;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.MULTIPLE_PLACEMENT_FAILURES);
		builder.getMultiplePlacementFailuresBuilder()
				.setByTeam(getTeam(team));
		
		return builder.build();
	}
	
	
	public ETeamColor getTeam()
	{
		return team;
	}
	
	
	@Override
	public String toString()
	{
		return "Multiple placement failures collected by " + team;
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final MultiplePlacementFailures that = (MultiplePlacementFailures) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(team, that.team)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.toHashCode();
	}
}
