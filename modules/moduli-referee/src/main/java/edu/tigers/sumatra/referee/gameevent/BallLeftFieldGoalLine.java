package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class BallLeftFieldGoalLine extends AGameEvent
{
	private final ETeamColor team;
	private final Integer bot;
	private final IVector2 location;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected BallLeftFieldGoalLine()
	{
		team = null;
		bot = 0;
		location = null;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BallLeftFieldGoalLine(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBallLeftFieldGoalLine().getByTeam());
		this.bot = event.getBallLeftFieldGoalLine().getByBot();
		this.location = toVector(event.getBallLeftFieldGoalLine().getLocation());
	}
	
	
	public BallLeftFieldGoalLine(BotID bot, IVector2 location)
	{
		super(EGameEvent.BALL_LEFT_FIELD_GOAL_LINE);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
	}
	
	
	public BallLeftFieldGoalLine(ETeamColor team, IVector2 location)
	{
		super(EGameEvent.BALL_LEFT_FIELD_GOAL_LINE);
		this.team = team;
		this.bot = null;
		this.location = location;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		
		builder.setType(SslGameEvent.GameEventType.BALL_LEFT_FIELD_GOAL_LINE);
		builder.getBallLeftFieldGoalLineBuilder()
				.setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location));
		
		if (bot != null)
		{
			builder.getBallLeftFieldGoalLineBuilder().setByBot(bot);
		}
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Ball left field @ %s by bot %d %s via goal line", formatVector(location), bot, team);
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final BallLeftFieldGoalLine that = (BallLeftFieldGoalLine) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(team, that.team)
				.append(location, that.location)
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
				.toHashCode();
	}
}
