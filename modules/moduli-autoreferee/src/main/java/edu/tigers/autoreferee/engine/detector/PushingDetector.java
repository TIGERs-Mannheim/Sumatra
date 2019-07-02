package edu.tigers.autoreferee.engine.detector;

import java.awt.Color;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotPushedBot;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Detect if a robot pushes an opponent robot by more than 0.2m or into the opponent penalty area
 */
public class PushingDetector extends AGameEventDetector
{
	@Configurable(defValue = "10.0", comment = "Extra margin [mm] to allow between two bots such that they are detected as touching")
	private static double botExtraMargin = 10;
	
	@Configurable(defValue = "200.0", comment = "Maximum allowed distance [mm] that a bot is allowed to push an opponent bot")
	private static double maxAllowedPushDistance = 200;
	
	@Configurable(defValue = "0.7", comment = "Maximum absolute angle [rad] between pushed direction and attacker to opponent direction for counting violation")
	private static double maxPushAngle = 0.7;
	
	@Configurable(defValue = "2.0", comment = "Cool down time [s] until the same robot pair will not be redetected")
	private static double detectionCoolDownTime = 2.0;
	
	private Set<RobotPair> firstRobotPairs = new HashSet<>();
	private Set<RobotPair> recentlyDetectedPushingPairs = new HashSet<>();
	
	
	public PushingDetector()
	{
		super(EGameEventDetectorType.PUSHING, EnumSet.of(EGameState.RUNNING));
	}
	
	
	@Override
	public void doReset()
	{
		firstRobotPairs.clear();
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		removeOldRecentlyDetectedPushingPairs();
		
		final Set<RobotPair> latestRobotPairs = latestRobotPairs();
		latestRobotPairs.addAll(touchingBallRobotPairs());
		latestRobotPairs.removeAll(recentlyDetectedPushingPairs);
		
		firstRobotPairs = merge(firstRobotPairs, latestRobotPairs);
		firstRobotPairs.forEach(this::drawBotPair);
		
		final List<PushedDistance> pushedDistances = pushedDistances(latestRobotPairs);
		pushedDistances.forEach(this::drawPushedDistance);
		
		return ruleViolation(pushedDistances).map(this::createEvent);
	}
	
	
	private IGameEvent createEvent(final PushedDistance pd)
	{
		firstRobotPairs.remove(pd.firstPair);
		recentlyDetectedPushingPairs.add(pd.latestPair);
		return new BotPushedBot(
				pd.firstPair.bot.getBotId(),
				pd.firstPair.opponentBot.getBotId(),
				pd.start(),
				pd.distance());
	}
	
	
	private Optional<PushedDistance> ruleViolation(final List<PushedDistance> pushedDistances)
	{
		return pushedDistances.stream()
				.filter(this::violatesRules)
				.findFirst();
	}
	
	
	private List<PushedDistance> pushedDistances(final Set<RobotPair> latestRobotPairs)
	{
		return latestRobotPairs.stream()
				.map(this::pushedDistance)
				.collect(Collectors.toList());
	}
	
	
	private Set<RobotPair> latestRobotPairs()
	{
		return frame.getWorldFrame().getBots().values().stream()
				.map(this::touchingOpponents)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}
	
	
	private Set<RobotPair> touchingBallRobotPairs()
	{
		final List<ITrackedBot> botsTouchingBall = frame.getBotsTouchingBall().stream()
				.map(BotPosition::getBotID)
				.map(id -> frame.getWorldFrame().getBot(id))
				.collect(Collectors.toList());
		return botsTouchingBall.stream()
				.map(bot -> touching(bot, botsTouchingBall))
				.flatMap(Collection::stream).collect(Collectors.toSet());
	}
	
	
	private void removeOldRecentlyDetectedPushingPairs()
	{
		recentlyDetectedPushingPairs.removeIf(this::detectionCooledDown);
	}
	
	
	private boolean detectionCooledDown(RobotPair robotPair)
	{
		return (frame.getTimestamp() - robotPair.getBot().getTimestamp()) / 1e9 > detectionCoolDownTime;
	}
	
	
	private List<RobotPair> touching(ITrackedBot bot, List<ITrackedBot> ballTouchingBots)
	{
		return ballTouchingBots.stream()
				.filter(b -> b.getBotId() != bot.getBotId())
				.map(b -> new RobotPair(b, bot))
				.collect(Collectors.toList());
	}
	
	
	private boolean violatesRules(PushedDistance pushedDistance)
	{
		final Optional<Double> pushAngle = pushedDistance.pushDirection().angleToAbs(pushedDistance.moveDirection());
		if (pushAngle.isPresent() && pushAngle.get() > maxPushAngle)
		{
			return false;
		}
		
		final IPenaltyArea opponentPenArea = NGeometry
				.getPenaltyArea(pushedDistance.firstPair.opponentBot.getTeamColor());
		
		return pushedDistance.distance() > maxAllowedPushDistance
				|| opponentPenArea.withMargin(Geometry.getBotRadius()).isPointInShape(pushedDistance.end());
	}
	
	
	private void drawPushedDistance(final PushedDistance pushedDistance)
	{
		final DrawableArrow arrow = new DrawableArrow(
				pushedDistance.start(), pushedDistance.pushDirection(),
				Color.red);
		frame.getShapes().get(EAutoRefShapesLayer.PUSHING)
				.add(arrow);
	}
	
	
	private PushedDistance pushedDistance(final RobotPair latestPair)
	{
		for (RobotPair firstPair : firstRobotPairs)
		{
			if (firstPair.equals(latestPair))
			{
				return new PushedDistance(firstPair, latestPair);
			}
		}
		throw new IllegalStateException("Expected a match: " + latestPair);
	}
	
	
	private Set<RobotPair> merge(Set<RobotPair> oldPairs, Set<RobotPair> newPairs)
	{
		// take all old
		final Set<RobotPair> mergedPairs = new HashSet<>(oldPairs);
		// but remove the vanished ones
		mergedPairs.removeIf(b -> !newPairs.contains(b));
		// and add new bots
		newPairs.stream().filter(p -> !oldPairs.contains(p)).forEach(mergedPairs::add);
		
		return mergedPairs;
	}
	
	
	private void drawBotPair(RobotPair robotPair)
	{
		double tubeRadius = Geometry.getBotRadius() + 30 + (robotPair.bot.getTeamColor() == ETeamColor.YELLOW ? 0 : 10);
		Tube tube = Tube.create(robotPair.bot.getPos(), robotPair.opponentBot.getPos(), tubeRadius);
		Color tubeColor = robotPair.bot.getTeamColor().getColor();
		frame.getShapes().get(EAutoRefShapesLayer.PUSHING).add(new DrawableTube(tube, tubeColor));
	}
	
	
	private List<RobotPair> touchingOpponents(ITrackedBot bot)
	{
		return frame.getWorldFrame().getBots().values().stream()
				.filter(b -> bot.getTeamColor() != b.getTeamColor())
				.filter(b -> touching(bot, b))
				.map(b -> new RobotPair(bot, b))
				.collect(Collectors.toList());
	}
	
	
	private boolean touching(ITrackedBot bot1, ITrackedBot bot2)
	{
		double minDist = Geometry.getBotRadius() * 2 + botExtraMargin;
		minDist *= minDist;
		return bot1.getPos().distanceToSqr(bot2.getPos()) < minDist;
	}
	
	private static class RobotPair
	{
		ITrackedBot bot;
		ITrackedBot opponentBot;
		
		
		public RobotPair(final ITrackedBot bot, final ITrackedBot opponentBot)
		{
			this.bot = bot;
			this.opponentBot = opponentBot;
		}
		
		
		public ITrackedBot getBot()
		{
			return bot;
		}
		
		
		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			
			if (o == null || getClass() != o.getClass())
				return false;
			
			final RobotPair robotPair = (RobotPair) o;
			
			return new EqualsBuilder()
					.append(bot.getBotId(), robotPair.bot.getBotId())
					.append(opponentBot.getBotId(), robotPair.opponentBot.getBotId())
					.isEquals();
		}
		
		
		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37)
					.append(bot.getBotId())
					.append(opponentBot.getBotId())
					.toHashCode();
		}
		
		
		@Override
		public String toString()
		{
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("bot", bot)
					.append("opponentBot", opponentBot)
					.toString();
		}
	}
	
	private static class PushedDistance
	{
		RobotPair firstPair;
		RobotPair latestPair;
		
		
		public PushedDistance(final RobotPair firstPair, final RobotPair latestPair)
		{
			this.firstPair = firstPair;
			this.latestPair = latestPair;
		}
		
		
		IVector2 start()
		{
			return firstPair.opponentBot.getPos();
		}
		
		
		IVector2 end()
		{
			return latestPair.opponentBot.getPos();
		}
		
		
		IVector2 pushDirection()
		{
			return end().subtractNew(start());
		}
		
		
		IVector2 moveDirection()
		{
			return firstPair.opponentBot.getPos().subtractNew(firstPair.bot.getPos());
		}
		
		
		double distance()
		{
			return pushDirection().getLength2();
		}
	}
}
