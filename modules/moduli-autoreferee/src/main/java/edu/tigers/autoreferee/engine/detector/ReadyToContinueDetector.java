package edu.tigers.autoreferee.engine.detector;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.Prepared;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Signal a PREPARED event to the GC, when no bots are in the forbidden area around the ball during STOP
 */
public class ReadyToContinueDetector extends AGameEventDetector
{
	private long tStart;
	
	@Configurable(comment = "Tolerance [mm] between ball and bot", defValue = "30.0")
	private static double tolerance = 30.0;
	
	@Configurable(defValue = "2.0", comment = "Minimum time [s] to wait before issuing the prepared event")
	private static double minPrepareTime = 2.0;
	
	
	public ReadyToContinueDetector()
	{
		super(EGameEventDetectorType.READY_TO_CONTINUE, EGameState.STOP);
		setDeactivateOnFirstGameEvent(true);
	}
	
	
	@Override
	protected void doPrepare()
	{
		tStart = frame.getTimestamp();
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		double radius = RuleConstraints.getStopRadius() + Geometry.getBotRadius() + Geometry.getBallRadius() - tolerance;
		final List<ITrackedBot> violatingBots = frame.getWorldFrame().getBots().values().stream()
				.filter(bot -> getBall().getPos().distanceTo(bot.getPos()) < radius)
				.collect(Collectors.toList());
		
		
		List<IDrawableShape> shapes = frame.getShapes().get(EAutoRefShapesLayer.ENGINE);
		violatingBots.forEach(bot -> shapes.add(violatorMarker(bot)));
		
		double timeTaken = (frame.getTimestamp() - tStart) / 1e9;
		if (violatingBots.isEmpty() && timeTaken > minPrepareTime)
		{
			return Optional.of(new Prepared(timeTaken));
		}
		return Optional.empty();
	}
	
	
	private DrawableCircle violatorMarker(final ITrackedBot bot)
	{
		return new DrawableCircle(bot.getPos(), Geometry.getBotRadius() + 30, Color.RED);
	}
}
