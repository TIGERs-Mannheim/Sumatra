/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.common;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


/**
 * A dynamic and extensible point checker
 */
public class PointChecker
{
	private static final double FIELD_MARGIN = 100;
	private final Map<String, Function<IVector2, Boolean>> functions = new LinkedHashMap<>();

	private WorldFrame worldFrame;
	private GameState gameState;
	private RefereeMsg refereeMsg;
	private BotID botID;

	@Setter
	private double theirPenAreaMargin = Geometry.getBotRadius() + RuleConstraints.getPenAreaMarginStandard();
	@Setter
	private double ourPenAreaMargin = 200;


	/**
	 * Points must not be in a forbidden area around the ball and the ball placement position (including velocity)
	 *
	 * @return this
	 */
	public PointChecker checkBallDistances()
	{
		functions.put("ballDistance", this::isPointConformWithBallDistance);
		functions.put("ballPlacement", this::isPointConformWithBallPlacement);
		return this;
	}


	/**
	 * Points must not be in a forbidden area around the ball (ignoring ball velocity)
	 *
	 * @return this
	 */
	public PointChecker checkBallDistanceStatic()
	{
		functions.put("ballDistanceStatic", this::isPointConformWithBallDistanceStatic);
		return this;
	}


	/**
	 * Points must be inside field
	 *
	 * @return this
	 */
	public PointChecker checkInsideField()
	{
		functions.put("inField", this::insideField);
		return this;
	}


	/**
	 * Points must not be inside either penalty area
	 *
	 * @return this
	 */
	public PointChecker checkNotInPenaltyAreas()
	{
		functions.put("outsideOurPenArea", this::outsideOurPenArea);
		functions.put("outsideTheirPenArea", this::outsideTheirPenArea);
		return this;
	}


	/**
	 * During and before kickoff (the game stage is used), positions in the opponent field half are forbidden
	 *
	 * @return this
	 */
	public PointChecker checkConfirmWithKickOffRules()
	{
		functions.put("kickoff", this::isPointConformWithKickOffRules);
		return this;
	}


	/**
	 * Check if the point is free of other bots.
	 * Use {@link #allMatch(BaseAiFrame, IVector2, BotID)} to ignore the robot in question.
	 *
	 * @return this
	 */
	public PointChecker checkPointFreeOfBots()
	{
		functions.put("freeOfBots", this::isPointFreeOfBots);
		return this;
	}


	/**
	 * Check if the point is free of other bots.
	 * Use {@link #allMatch(BaseAiFrame, IVector2, BotID)} to ignore the robot in question.
	 *
	 * @return this
	 */
	public PointChecker checkPointFreeOfBotsExceptFor(IVector2 freeOfBotsExceptionPoint)
	{
		functions.put("freeOfBotsExceptCompanions", p -> isPointFreeOfBotsExceptCompanions(p, freeOfBotsExceptionPoint));
		return this;
	}


	/**
	 * Add a custom function
	 *
	 * @param function
	 * @return this
	 */
	public PointChecker checkCustom(Function<IVector2, Boolean> function)
	{
		functions.put(function.toString(), function);
		return this;
	}


	/**
	 * Add a custom function
	 *
	 * @param id       the id that describes this function when visualizing
	 * @param function the checker function
	 * @return this
	 */
	public PointChecker checkCustom(String id, Function<IVector2, Boolean> function)
	{
		functions.put(id, function);
		return this;
	}


	/**
	 * Check all functions
	 *
	 * @param aiFrame
	 * @param point
	 * @return
	 */
	public boolean allMatch(BaseAiFrame aiFrame, IVector2 point, BotID botID)
	{
		return findFirstNonMatching(aiFrame, point, botID).isEmpty();
	}


	/**
	 * Check all functions
	 *
	 * @param aiFrame
	 * @param point
	 * @return
	 */
	public Optional<String> findFirstNonMatching(BaseAiFrame aiFrame, IVector2 point, BotID botID)
	{
		this.worldFrame = aiFrame.getWorldFrame();
		this.gameState = aiFrame.getGameState();
		this.refereeMsg = aiFrame.getRefereeMsg();
		this.botID = botID;
		return functions.entrySet().stream()
				.filter(e -> !e.getValue().apply(point))
				.map(Map.Entry::getKey)
				.findFirst();
	}


	/**
	 * Check all functions
	 *
	 * @param aiFrame
	 * @param point
	 * @return
	 */
	public boolean allMatch(BaseAiFrame aiFrame, IVector2 point)
	{
		return allMatch(aiFrame, point, BotID.noBot());
	}


	private boolean insideField(IVector2 point)
	{
		return Geometry.getField().isPointInShape(point, -FIELD_MARGIN);
	}


	private boolean outsideOurPenArea(IVector2 point)
	{
		return !Geometry.getPenaltyAreaOur().isPointInShape(point, ourPenAreaMargin);
	}


	private boolean outsideTheirPenArea(IVector2 point)
	{
		return !Geometry.getPenaltyAreaTheir().isPointInShape(point, theirPenAreaMargin);
	}


	private boolean isPointConformWithBallDistance(IVector2 point)
	{
		double distance = RuleConstraints.getStopRadius() + Geometry.getBallRadius() + Geometry.getBotRadius();
		double maxLength = RuleConstraints.getStopRadius();
		double length = Math.min(maxLength, worldFrame.getBall().getTrajectory().getTravelLineSegment().getLength());
		IVector2 offset = worldFrame.getBall().getVel().scaleToNew(length);
		ILineSegment ballSegment = Lines.segmentFromOffset(worldFrame.getBall().getPos(), offset);
		return gameState.isRunning() || ballSegment.distanceTo(point) >= distance;
	}


	private boolean isPointConformWithBallDistanceStatic(IVector2 point)
	{
		double distance = RuleConstraints.getStopRadius() + Geometry.getBallRadius() + Geometry.getBotRadius();
		return gameState.isRunning()
				|| worldFrame.getBall().getPos().distanceTo(point) >= distance;
	}


	private boolean isPointConformWithKickOffRules(IVector2 point)
	{
		SslGcRefereeMessage.Referee.Stage stage = refereeMsg.getStage();
		boolean isPreStage = (stage == SslGcRefereeMessage.Referee.Stage.EXTRA_FIRST_HALF_PRE)
				|| (stage == SslGcRefereeMessage.Referee.Stage.EXTRA_SECOND_HALF_PRE)
				|| (stage == SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF_PRE)
				|| (stage == SslGcRefereeMessage.Referee.Stage.NORMAL_SECOND_HALF_PRE);
		boolean isKickoffState = gameState.isKickoffOrPrepareKickoff() || isPreStage;

		boolean isInOurHalf = Geometry.getFieldHalfOur()
				.isPointInShape(point, -Geometry.getBotRadius());

		boolean isInCenterCircle = Geometry.getCenterCircle()
				.isPointInShape(point, Geometry.getBotRadius());

		return !isKickoffState || (isInOurHalf && !isInCenterCircle);
	}


	private boolean isPointConformWithBallPlacement(IVector2 point)
	{
		IVector2 ballPlacementPos = gameState.getBallPlacementPositionForUs();
		if (ballPlacementPos == null)
		{
			return true;
		}
		ILineSegment placementLine = Lines.segmentFromPoints(worldFrame.getBall().getPos(), ballPlacementPos);
		return placementLine.distanceTo(point) > RuleConstraints.getStopRadius() + Geometry.getBotRadius() + 10;
	}


	private boolean isPointFreeOfBots(final IVector2 point)
	{
		double distance = Geometry.getBotRadius() * 2 + 10;
		return worldFrame.getBots().values().stream()
				.filter(bot -> bot.getBotId() != botID)
				.noneMatch(bot -> bot.getPosByTime(1).distanceTo(point) < distance);
	}


	private boolean isPointFreeOfBotsExceptCompanions(final IVector2 point, IVector2 exceptionPoint)
	{
		if (point.equals(exceptionPoint))
		{
			// destination may be occupied by companion, but that's fine
			return true;
		}
		return isPointFreeOfBots(point);
	}
}
