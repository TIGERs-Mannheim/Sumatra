/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.common;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * A dynamic and extensible point checker
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PointChecker
{
	private final Set<Function<IVector2, Boolean>> functions = new HashSet<>();
	
	private WorldFrame worldFrame;
	private GameState gameState;
	private RefereeMsg refereeMsg;
	
	private double theirPenAreaMargin = 200;
	private double ourPenAreaMargin = 200;
	private double fieldMargin = 100;
	
	
	/**
	 * Use all internal function that enforce the rules
	 */
	public void useRuleEnforcement()
	{
		functions.add(this::insideField);
		functions.add(this::outsideOurPenArea);
		functions.add(this::outsideTheirPenArea);
		functions.add(this::isPointConformWithBallDistance);
		functions.add(this::isPointConformWithBallPlacement);
	}
	
	
	/**
	 * During and before kickoff (the game stage is used), positions in the opponent field half are forbidden
	 */
	public void useKickOffRuleEnforcement()
	{
		functions.add(this::isPointConformWithKickOffRules);
	}
	
	
	/**
	 * Add a custom function
	 * 
	 * @param function
	 */
	public void addFunction(Function<IVector2, Boolean> function)
	{
		functions.add(function);
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
		this.worldFrame = aiFrame.getWorldFrame();
		this.gameState = aiFrame.getGamestate();
		this.refereeMsg = aiFrame.getRefereeMsg();
		return functions.stream().allMatch(f -> f.apply(point));
	}
	
	
	private boolean insideField(IVector2 point)
	{
		return Geometry.getField().isPointInShape(point, -fieldMargin);
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
		return gameState.isRunning()
				|| Lines.segmentFromLine(worldFrame.getBall().getTrajectory().getTravelLine())
						.distanceTo(point) >= RuleConstraints.getStopRadius()
								+ Geometry.getBallRadius() + Geometry.getBotRadius();
		
	}
	
	
	private boolean isPointConformWithKickOffRules(IVector2 point)
	{
		Referee.SSL_Referee.Stage stage = refereeMsg.getStage();
		boolean isPreStage = (stage == Referee.SSL_Referee.Stage.EXTRA_FIRST_HALF_PRE)
				|| (stage == Referee.SSL_Referee.Stage.EXTRA_SECOND_HALF_PRE)
				|| (stage == Referee.SSL_Referee.Stage.NORMAL_FIRST_HALF_PRE)
				|| (stage == Referee.SSL_Referee.Stage.NORMAL_SECOND_HALF_PRE);
		boolean isKickoffState = gameState.isKickoffOrPrepareKickoff() || isPreStage;
		
		boolean isInOurHalf = Geometry.getFieldHalfOur()
				.isPointInShape(point, -Geometry.getBotRadius());
		
		boolean isInCenterCircle = Geometry.getCenterCircle()
				.isPointInShape(point, Geometry.getBotRadius());
		
		return !isKickoffState || (isInOurHalf && !isInCenterCircle);
	}
	
	
	private boolean isPointConformWithBallPlacement(IVector2 point)
	{
		if (!gameState.isBallPlacement())
		{
			return true;
		}
		ILineSegment placementLine = Lines.segmentFromPoints(worldFrame.getBall().getPos(),
				gameState.getBallPlacementPositionForUs());
		return placementLine.distanceTo(point) > RuleConstraints.getStopRadius() + Geometry.getBotRadius();
	}
	
	
	public void setTheirPenAreaMargin(final double theirPenAreaMargin)
	{
		this.theirPenAreaMargin = theirPenAreaMargin;
	}
	
	
	public void setOurPenAreaMargin(final double ourPenAreaMargin)
	{
		this.ourPenAreaMargin = ourPenAreaMargin;
	}
	
	
	public void setFieldMargin(final double fieldMargin)
	{
		this.fieldMargin = fieldMargin;
	}
	
	
	public double getOurPenAreaMargin()
	{
		return ourPenAreaMargin;
	}
	
}
