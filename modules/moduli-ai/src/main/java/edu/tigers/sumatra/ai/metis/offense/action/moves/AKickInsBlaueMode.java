/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.general.ChipKickReasonableDecider;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


public abstract class AKickInsBlaueMode
{
	private BotID closestTigerBot = BotID.noBot();
	private final Predicate<RatedPosition> stillCandidatePredicate = e -> e
			.getStatus() == ERatedPositionStatus.CANDIDATE;
	private final Comparator<RatedPosition> scoreComparator = Comparator.comparingDouble(RatedPosition::getScore)
			.reversed();
	
	protected KickInsBlaueFilterParameters filterParameters;
	protected final List<IDrawableShape> shapes = new ArrayList<>();
	
	protected double minKickDistanceSqr = 0.;
	protected double maxKickDistanceSqr = 0.;
	protected double maxDistanceToGoalCenterSqr = 0.;
	
	
	public Optional<KickTarget> create(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final KickInsBlaueFilterParameters filterParameters)
	{
		this.filterParameters = filterParameters;
		shapes.clear();
		
		calculateDistancesSqr();
		drawFilterParameter();
		
		List<IVector2> gridPositions = createFullPositionGrid();
		List<RatedPosition> grid = preFilterPositionGrid(gridPositions);
		Optional<RatedPosition> finalPosition = selectFinalPosition(id, baseAiFrame, grid);
		
		Optional<KickTarget> kickTarget = finalPosition.map(this::createKickTarget);
		
		drawGridPoints(grid);
		
		if (baseAiFrame.getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot().orElse(BotID.noBot())
				.equals(id))
		{
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE).addAll(shapes);
		}
		
		closestTigerBot = finalPosition.map(RatedPosition::getSmallestTimeTigerBot).orElse(BotID.noBot());
		
		return kickTarget;
	}
	
	
	public List<IDrawableShape> getShapes()
	{
		return shapes;
	}
	
	
	public BotID getClosestTigerBot()
	{
		return closestTigerBot;
	}
	
	
	protected abstract void drawFilterParameter();
	
	
	protected abstract List<IVector2> createFullPositionGrid();
	
	
	protected abstract KickTarget createKickTarget(final RatedPosition finalPosition);
	
	
	protected abstract boolean preFilterByKickDistance(final IVector2 gridPoint);
	
	
	protected abstract boolean preFilterByPosition(final IVector2 gridPoint);
	
	
	protected abstract boolean preFilterByTurnAngle(final double ball2GridPointAngle);
	
	
	protected abstract boolean preFilterByBallLeaveField(final IVector2 gridPoint, final double ball2GridPointAngle);
	
	
	protected abstract void rateByBotDistance(final BaseAiFrame baseAiFrame,
			final List<RatedPosition> grid);
	
	
	protected abstract void rateAndFilterByFreePassWay(final BotID id, final BaseAiFrame baseAiFrame,
			final List<RatedPosition> grid);
	
	
	private void calculateDistancesSqr()
	{
		minKickDistanceSqr = KickInsBlaueActionMove.minKickDistance * KickInsBlaueActionMove.minKickDistance;
		maxKickDistanceSqr = KickInsBlaueActionMove.maxKickDistance * KickInsBlaueActionMove.maxKickDistance;
		maxDistanceToGoalCenterSqr = KickInsBlaueActionMove.maxDistanceToGoalCenter
				* KickInsBlaueActionMove.maxDistanceToGoalCenter;
	}
	
	
	private Optional<RatedPosition> selectFinalPosition(final BotID id, final BaseAiFrame baseAiFrame,
			final List<RatedPosition> grid)
	{
		if (grid.isEmpty())
		{
			return Optional.empty();
		}
		
		rateByBotDistance(baseAiFrame,
				grid.stream().filter(stillCandidatePredicate).collect(Collectors.toList()));
		// Select the 5 best
		grid.sort(scoreComparator);
		grid.stream().filter(stillCandidatePredicate).skip(5)
				.forEach(e -> e.setStatus(ERatedPositionStatus.OUT_BY_SCORE));
		
		if (grid.stream().noneMatch(stillCandidatePredicate))
		{
			return Optional.empty();
		}
		
		rateAndFilterByFreePassWay(id, baseAiFrame,
				grid.stream().filter(stillCandidatePredicate).collect(Collectors.toList()));
		
		if (grid.stream().noneMatch(stillCandidatePredicate))
		{
			return Optional.empty();
		}
		
		return rateAndSelectByBotTravelTime(baseAiFrame,
				grid.stream().filter(stillCandidatePredicate).collect(Collectors.toList()));
	}
	
	
	private Optional<RatedPosition> rateAndSelectByBotTravelTime(final BaseAiFrame baseAiFrame,
			final List<RatedPosition> grid)
	{
		
		for (RatedPosition rp : grid)
		{
			Pair<BotID, Double> tigerTime = getShortestTigerTravelTime(rp, baseAiFrame);
			Pair<BotID, Double> foeTime = getShortestFoeTravelTime(rp, baseAiFrame);
			rp.setSmallestTimeTigerBot(tigerTime.getFirst());
			if (Double.compare(foeTime.getSecond(),
					tigerTime.getSecond() + KickInsBlaueActionMove.minTimeDifferenceAtTarget) == -1)
			{
				rp.setStatus(ERatedPositionStatus.OUT_BY_BOT_TRAVEL_TIME);
			}
		}
		
		// Select the Position
		// Sort by max TravelTime from own Bots -> the work before makes sure the left over candidates are viable
		Optional<RatedPosition> ret = grid.stream().filter(stillCandidatePredicate)
				.max(Comparator.comparingDouble((RatedPosition e) -> mapBotAndPositionToDistance(
						baseAiFrame.getWorldFrame().getBot(e.getSmallestTimeTigerBot()), e.getPosition()).getValue()));
		ret.ifPresent(e -> e.setStatus(ERatedPositionStatus.FINAL_SELECTED));
		return ret;
	}
	
	
	private List<RatedPosition> preFilterPositionGrid(final List<IVector2> grid)
	{
		List<RatedPosition> returnGrid = new ArrayList<>();
		for (IVector2 gridPoint : grid)
		{
			
			RatedPosition ratedPosition = new RatedPosition(gridPoint);
			final double ball2GridPointAngle = Vector2.fromPoints(filterParameters.getBallPosition(), gridPoint)
					.getAngle();
			
			// Filter depending on field position
			boolean filteredOut = preFilterByPosition(gridPoint);
			
			if (!filteredOut)
			{
				// If to close or too far, filter out
				filteredOut = preFilterByKickDistance(gridPoint);
			}
			
			if (!filteredOut)
			{
				// If turn angle too wide, filter out
				filteredOut = preFilterByTurnAngle(ball2GridPointAngle);
			}
			
			if (!filteredOut)
			{
				// If ball will leave field, filter out
				filteredOut = preFilterByBallLeaveField(gridPoint, ball2GridPointAngle);
			}
			
			ratedPosition.setStatus(filteredOut ? ERatedPositionStatus.OUT_BY_PRE_FILTER : ERatedPositionStatus.CANDIDATE);
			returnGrid.add(ratedPosition);
		}
		
		return returnGrid;
	}
	
	
	protected double getSmallestTigerDistance(final RatedPosition ratedPosition, final BaseAiFrame baseAiFrame)
	{
		
		Pair<BotID, Double> smallestDistance = baseAiFrame.getWorldFrame().getTigerBotsAvailable().values().stream()
				.filter(e -> e.getBotId() != baseAiFrame.getKeeperId())
				.map(e -> mapBotAndPositionToDistance(e, ratedPosition.getPosition()))
				.min(Comparator.comparingDouble(Pair::getValue)).orElse(new Pair<>(BotID.noBot(), Double.MAX_VALUE));
		
		ratedPosition.setSmallestDistanceTigerBot(smallestDistance.getKey());
		return smallestDistance.getValue();
	}
	
	
	protected double getSmallestFoeDistance(RatedPosition ratedPosition, final BaseAiFrame baseAiFrame)
	{
		
		Pair<BotID, Double> smallestDistance = baseAiFrame.getWorldFrame().getFoeBots().values().stream()
				.filter(e -> e.getBotId() != baseAiFrame.getKeeperFoeId())
				.map(e -> mapBotAndPositionToDistance(e, ratedPosition.getPosition()))
				.min(Comparator.comparingDouble(Pair::getValue)).orElse(new Pair<>(BotID.noBot(), 0.0));
		
		ratedPosition.setSmallestDistanceFoeBot(smallestDistance.getKey());
		return smallestDistance.getValue();
	}
	
	
	private Pair<BotID, Double> mapBotAndPositionToDistance(final ITrackedBot bot, final IVector2 position)
	{
		return new Pair<>(bot.getBotId(), bot.getPos().distanceTo(position));
	}
	
	
	protected boolean isStraightPassWayFree(final IVector2 target, final WorldFrame worldFrame)
	{
		IVector2 ballPosition = worldFrame.getBall().getPos();
		IVector2 ballToTargetNormal = target.subtractNew(ballPosition).getNormalVector().normalize();
		double distance = ballPosition.distanceTo(target);
		
		IVector2 a = target.addNew(ballToTargetNormal.scaleToNew(0.15 * distance));
		IVector2 b = target.addNew(ballToTargetNormal.scaleToNew(-0.15 * distance));
		Triangle triangle = Triangle.fromCorners(a, b, ballPosition);
		
		return worldFrame.getFoeBots().values().stream().noneMatch(e -> triangle.isPointInShape(e.getPos()));
	}
	
	
	protected boolean isChipPassWayFree(final BotID id, final IVector2 target, final WorldFrame worldFrame,
			int numTouchdown)
	{
		ITrackedBall ball = worldFrame.getBall();
		IBotIDMap<ITrackedBot> obstacles = new BotIDMap<>(worldFrame.getBots());
		obstacles.remove(id);
		
		ChipKickReasonableDecider reasonableDecider = new ChipKickReasonableDecider(ball.getPos(), target,
				obstacles.values(),
				ball.getChipConsultant().getInitVelForDistAtTouchdown(ball.getPos().distanceTo(target), numTouchdown));
		return reasonableDecider.isChipKickReasonable();
	}
	
	
	private Pair<BotID, Double> getShortestTigerTravelTime(final RatedPosition ratedPosition,
			final BaseAiFrame baseAiFrame)
	{
		BotID secondClosest = baseAiFrame.getWorldFrame().getTigerBotsAvailable().values().stream()
				.filter(e -> (e.getBotId() != baseAiFrame.getKeeperId())
						&& (e.getBotId() != ratedPosition.getSmallestDistanceTigerBot()))
				.map(e -> mapBotAndPositionToDistance(e, ratedPosition.getPosition()))
				.min(Comparator.comparingDouble(Pair::getValue)).orElse(new Pair<>(BotID.noBot(), Double.MAX_VALUE))
				.getKey();
		
		return baseAiFrame.getWorldFrame().getTigerBotsAvailable().values().stream()
				.filter(
						e -> (e.getBotId() == secondClosest) || (e.getBotId() == ratedPosition.getSmallestDistanceTigerBot()))
				.map(e -> mapBotAndPositionToTravelTime(e, ratedPosition.getPosition()))
				.min(Comparator.comparingDouble(Pair::getValue)).orElse(new Pair<>(BotID.noBot(), Double.MAX_VALUE));
	}
	
	
	private Pair<BotID, Double> getShortestFoeTravelTime(final RatedPosition ratedPosition,
			final BaseAiFrame baseAiFrame)
	{
		BotID secondClosest = baseAiFrame.getWorldFrame().getFoeBots().values().stream()
				.filter(e -> (e.getBotId() != baseAiFrame.getKeeperFoeId())
						&& (e.getBotId() != ratedPosition.getSmallestDistanceFoeBot()))
				.map(e -> mapBotAndPositionToDistance(e, ratedPosition.getPosition()))
				.min(Comparator.comparingDouble(Pair::getValue)).orElse(new Pair<>(BotID.noBot(), Double.MAX_VALUE))
				.getKey();
		
		return baseAiFrame.getWorldFrame().getFoeBots().values().stream()
				.filter(
						e -> (e.getBotId() == secondClosest) || (e.getBotId() == ratedPosition.getSmallestDistanceFoeBot()))
				.map(e -> mapBotAndPositionToTravelTime(e, ratedPosition.getPosition()))
				.min(Comparator.comparingDouble(Pair::getValue)).orElse(new Pair<>(BotID.noBot(), Double.MAX_VALUE));
	}
	
	
	private Pair<BotID, Double> mapBotAndPositionToTravelTime(final ITrackedBot bot, final IVector2 position)
	{
		return new Pair<>(bot.getBotId(), TrajectoryGenerator.generatePositionTrajectory(bot, position).getTotalTime());
	}
	
	
	protected void drawTurnAngleSector(final double minDistance, final double maxDistance, final double angle,
			final Color color)
	{
		DrawableArc inner = new DrawableArc(Arc.createArc(filterParameters.getBallPosition(), minDistance,
				filterParameters.getBotAngleToBall() - angle, 2 * angle), color);
		inner.setArcType(Arc2D.OPEN);
		shapes.add(inner);
		DrawableArc outer = new DrawableArc(Arc.createArc(filterParameters.getBallPosition(), maxDistance,
				filterParameters.getBotAngleToBall() - angle, 2 * angle), color);
		outer.setArcType(Arc2D.OPEN);
		shapes.add(outer);
		
		IVector2 point00b = Vector2.fromAngle(filterParameters.getBotAngleToBall() - angle).scaleTo(minDistance)
				.add(filterParameters.getBallPosition());
		IVector2 point01b = Vector2.fromAngle(filterParameters.getBotAngleToBall() - angle).scaleTo(maxDistance)
				.add(filterParameters.getBallPosition());
		shapes.add(new DrawableLine(Line.fromPoints(point00b, point01b), color));
		IVector2 point10b = Vector2.fromAngle(filterParameters.getBotAngleToBall() + angle).scaleTo(minDistance)
				.add(filterParameters.getBallPosition());
		IVector2 point11b = Vector2.fromAngle(filterParameters.getBotAngleToBall() + angle).scaleTo(maxDistance)
				.add(filterParameters.getBallPosition());
		shapes.add(new DrawableLine(Line.fromPoints(point10b, point11b), color));
		
	}
	
	
	private void drawGridPoints(final List<RatedPosition> grid)
	{
		for (RatedPosition ratedPosition : grid)
		{
			Color color;
			double radius = 0.25 * Geometry.getBotRadius();
			switch (ratedPosition.getStatus())
			{
				case CANDIDATE:
					color = Color.RED;
					break;
				case OUT_BY_PRE_FILTER:
					color = Color.BLACK;
					break;
				case OUT_BY_SCORE:
					color = Color.LIGHT_GRAY;
					break;
				case OUT_BY_NO_PASS_WAY:
					color = Color.CYAN;
					break;
				case OUT_BY_BOT_TRAVEL_TIME:
					color = Color.BLUE;
					break;
				case FINAL_SELECTED:
					color = Color.RED;
					radius = 1.5 * Geometry.getBotRadius();
					break;
				default:
					throw new IllegalArgumentException(ratedPosition.getStatus() + " is Illegal argument");
			}
			shapes.add(new DrawableCircle(Circle.createCircle(ratedPosition.getPosition(), radius), color));
		}
	}
	
	protected enum ERatedPositionStatus
	{
		CANDIDATE,
		OUT_BY_PRE_FILTER,
		OUT_BY_SCORE,
		OUT_BY_NO_PASS_WAY,
		OUT_BY_BOT_TRAVEL_TIME,
		FINAL_SELECTED
	}
	
	protected class RatedPosition
	{
		
		private final IVector2 position;
		private double score;
		private BotID smallestTimeTigerBot;
		private BotID smallestDistanceTigerBot;
		private BotID smallestDistanceFoeBot;
		private ERatedPositionStatus status;
		
		
		RatedPosition(IVector2 position)
		{
			this.position = position;
			score = 0;
			smallestTimeTigerBot = BotID.noBot();
			status = ERatedPositionStatus.CANDIDATE;
			
		}
		
		
		protected IVector2 getPosition()
		{
			return position;
		}
		
		
		protected double getScore()
		{
			return score;
		}
		
		
		protected void setScore(final double score)
		{
			this.score = score;
		}
		
		
		protected BotID getSmallestTimeTigerBot()
		{
			return smallestTimeTigerBot;
		}
		
		
		protected void setSmallestTimeTigerBot(final BotID smallestTimeTigerBot)
		{
			this.smallestTimeTigerBot = smallestTimeTigerBot;
		}
		
		
		public BotID getSmallestDistanceTigerBot()
		{
			return smallestDistanceTigerBot;
		}
		
		
		public void setSmallestDistanceTigerBot(final BotID smallestDistanceTigerBot)
		{
			this.smallestDistanceTigerBot = smallestDistanceTigerBot;
		}
		
		
		public BotID getSmallestDistanceFoeBot()
		{
			return smallestDistanceFoeBot;
		}
		
		
		public void setSmallestDistanceFoeBot(final BotID smallestDistanceFoeBot)
		{
			this.smallestDistanceFoeBot = smallestDistanceFoeBot;
		}
		
		
		protected ERatedPositionStatus getStatus()
		{
			return status;
		}
		
		
		protected void setStatus(final ERatedPositionStatus status)
		{
			this.status = status;
		}
	}
}
