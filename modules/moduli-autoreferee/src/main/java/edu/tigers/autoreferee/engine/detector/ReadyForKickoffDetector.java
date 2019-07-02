package edu.tigers.autoreferee.engine.detector;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.Prepared;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Check if game can be continued during a kickoff
 */
public class ReadyForKickoffDetector extends AGameEventDetector
{
	private long tStart;
	private boolean eventRaised;
	
	
	public ReadyForKickoffDetector()
	{
		super(EGameEventDetectorType.READY_FOR_KICKOFF,
				EnumSet.of(EGameState.PREPARE_PENALTY, EGameState.PREPARE_KICKOFF));
	}
	
	
	@Override
	protected void doPrepare()
	{
		tStart = frame.getTimestamp();
		eventRaised = false;
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		if (eventRaised)
		{
			return Optional.empty();
		}
		
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ENGINE);
		
		if (!Geometry.getCenterCircle().isPointInShape(getBall().getPos()))
		{
			shapes.add(new DrawableCircle(getBall().getPos(), Geometry.getBallRadius() * 2, Color.RED));
			return Optional.empty();
		}
		
		final List<ITrackedBot> botsOnWrongSide = findBotsOnWrongSide();
		if (!botsOnWrongSide.isEmpty())
		{
			botsOnWrongSide.forEach(bot -> shapes.add(violatorMarker(bot)));
			return Optional.empty();
		}
		
		final List<ITrackedBot> botsTooCloseToBall = findBotsTooCloseToBall();
		if (!botsTooCloseToBall.isEmpty() &&
				(botsTooCloseToBall.size() > 1 ||
						botsTooCloseToBall.get(0).getBotId().getTeamColor() != frame.getGameState().getForTeam()))
		{
			botsTooCloseToBall.forEach(bot -> shapes.add(violatorMarker(bot)));
			return Optional.empty();
		}
		
		double timeTaken = (frame.getTimestamp() - tStart) / 1e9;
		eventRaised = true;
		return Optional.of(new Prepared(timeTaken));
	}
	
	
	private DrawableCircle violatorMarker(final ITrackedBot bot)
	{
		return new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.RED);
	}
	
	
	private List<ITrackedBot> findBotsOnWrongSide()
	{
		return frame.getWorldFrame().getBots().values().stream()
				.filter(this::insideField)
				.filter(this::inWrongFieldHalf)
				.filter(this::outsideCenterCircle)
				.collect(Collectors.toList());
	}
	
	
	private boolean outsideCenterCircle(final ITrackedBot bot)
	{
		return Geometry.getCenter().distanceTo(bot.getPos()) > Geometry.getCenterCircle().radius()
				+ Geometry.getBotRadius();
	}
	
	
	private boolean inWrongFieldHalf(final ITrackedBot bot)
	{
		return !NGeometry.getFieldSide(bot.getBotId().getTeamColor())
				.withMargin(-Geometry.getBotRadius())
				.isPointInShape(bot.getPos());
	}
	
	
	private boolean insideField(final ITrackedBot bot)
	{
		return NGeometry.getField().withMargin(-Geometry.getBotRadius()).isPointInShape(bot.getPos());
	}
	
	
	private List<ITrackedBot> findBotsTooCloseToBall()
	{
		return frame.getWorldFrame().getBots().values().stream()
				.filter(bot -> getBall().getPos().distanceTo(bot.getPos()) < RuleConstraints.getStopRadius())
				.collect(Collectors.toList());
	}
}
