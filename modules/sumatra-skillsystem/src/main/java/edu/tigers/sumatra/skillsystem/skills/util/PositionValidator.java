/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


public class PositionValidator
{
	private static final double INSIDE_FIELD_MARGIN = 20;

	private WorldFrame wFrame;
	private MovementCon moveCon;
	private Map<ETeam, Double> marginToPenArea = new EnumMap<>(ETeam.class);


	public void update(final WorldFrame wFrame, final MovementCon moveCon)
	{
		this.wFrame = wFrame;
		this.moveCon = moveCon;
	}


	public Map<ETeam, Double> getMarginToPenArea()
	{
		return marginToPenArea;
	}


	public IVector2 movePosInFrontOfOpponent(IVector2 pos)
	{
		Optional<ITrackedBot> collidingBot = findCollidingOpponent(pos);
		if (collidingBot.isPresent())
		{
			ILineSegment ballLine = Lines.segmentFromPoints(pos, wFrame.getBall().getPos());
			IVector2 opponentPosOnLine = ballLine.closestPointOnLine(collidingBot.get().getPos());
			// move pos in front of opponent (pos may be the kicker pos, so add 3*botRadius
			return LineMath.stepAlongLine(opponentPosOnLine, wFrame.getBall().getPos(), Geometry.getBotRadius() * 3);
		}
		return pos;
	}


	public Optional<ITrackedBot> findCollidingOpponent(final IVector2 point)
	{
		return wFrame.getFoeBots().values().stream()
				.filter(tBot -> tBot.getPos().distanceTo(point) < Geometry.getBotRadius() * 2)
				.findFirst();
	}


	/**
	 * Check if pos is inside field and adapt position if necessary with respect to the ball position
	 *
	 * @param pos the target position of the robots kicker position
	 * @return the pos, if outside. An adapted pos else
	 */
	public IVector2 movePosInsideFieldWrtBallPos(final IVector2 pos)
	{
		IRectangle field = Geometry.getField().withMargin(-INSIDE_FIELD_MARGIN);
		if (!field.isPointInShape(pos))
		{
			List<IVector2> intersections = field
					.lineIntersections(Line.fromPoints(wFrame.getBall().getPos(), pos));
			if (intersections.isEmpty())
			{
				return field.nearestPointInside(pos);
			}
			return pos.nearestTo(intersections);
		}
		return pos;
	}


	/**
	 * Check if pos is inside field and adapt position if necessary
	 *
	 * @param pos the target position of the robots kicker position
	 * @return the pos, if outside. An adapted pos else
	 */
	public IVector2 movePosInsideField(final IVector2 pos)
	{
		IRectangle field = Geometry.getField().withMargin(-INSIDE_FIELD_MARGIN);
		if (!field.isPointInShape(pos))
		{
			return field.nearestPointInside(pos);
		}
		return pos;
	}


	/**
	 * Check if pos is outside the penalty areas and adapt position if necessary.<br>
	 * The check considers the ball's position and velocity:
	 * <ul>
	 * <li>If ball is moving: Move to nearest point on ball travel line</li>
	 * <li>If ball inside penArea: Move to nearest point to ball</li>
	 * <li>Else: Move to nearest point to given pos</li>
	 * </ul>
	 *
	 * @param pos the target position of the robot
	 * @param penaltyAreas the penalty areas to check
	 * @return the pos, if outside. An adapted pos else
	 */
	public IVector2 movePosOutOfPenAreaWrtBall(
			final IVector2 pos,
			final List<IPenaltyArea> penaltyAreas)
	{
		for (IPenaltyArea penArea : penaltyAreas)
		{
			if (penArea.isPointInShapeOrBehind(pos))
			{
				if (wFrame.getBall().getVel().getLength2() > 0.1)
				{
					return pos.nearestToOpt(penArea.lineIntersections(Lines.lineFromPoints(pos, wFrame.getBall().getPos())))
							.orElseGet(() -> penArea.nearestPointOutside(pos));
				} else if (penArea.isPointInShape(wFrame.getBall().getPos()))
				{
					// move as near as possible to the ball inside penArea
					return penArea.nearestPointOutside(wFrame.getBall().getPos());
				}
				// move to nearest pos outside based on current pos
				return penArea.nearestPointOutside(pos);
			}
		}
		return pos;
	}


	/**
	 * See {@link #movePosOutOfPenAreaWrtBall(IVector2, List)}
	 *
	 * @param pos the target position of the robot
	 * @return the pos, if outside. An adapted pos else
	 */
	public IVector2 movePosOutOfPenAreaWrtBall(final IVector2 pos)
	{
		return movePosOutOfPenAreaWrtBall(pos, Geometry.getBotRadius());
	}


	/**
	 * See {@link #movePosOutOfPenAreaWrtBall(IVector2, List)}
	 *
	 * @param pos the target position of the robot
	 * @return the pos, if outside. An adapted pos else
	 */
	public IVector2 movePosOutOfPenAreaWrtBall(final IVector2 pos, final double margin)
	{
		if (moveCon != null)
		{
			if (moveCon.isPenaltyAreaForbiddenOur() && moveCon.isPenaltyAreaForbiddenTheir())
			{
				return movePosOutOfPenAreaWrtBall(pos, margin, ETeam.BOTH);
			} else if (moveCon.isPenaltyAreaForbiddenTheir())
			{
				return movePosOutOfPenAreaWrtBall(pos, margin, ETeam.OPPONENTS);
			} else if (moveCon.isPenaltyAreaForbiddenOur())
			{
				return movePosOutOfPenAreaWrtBall(pos, margin, ETeam.TIGERS);
			}
		}
		return movePosOutOfPenAreaWrtBall(pos, margin, ETeam.UNKNOWN);
	}


	/**
	 * See {@link #movePosOutOfPenAreaWrtBall(IVector2, List)}
	 *
	 * @param pos the target position of the robot
	 * @param margin the margin to add to both pen areas
	 * @param penAreaTeam the teams for which penalty areas should be considered
	 * @return the pos, if outside. An adapted pos else
	 */
	public IVector2 movePosOutOfPenAreaWrtBall(final IVector2 pos, final double margin, final ETeam penAreaTeam)
	{
		List<IPenaltyArea> penAreas = new ArrayList<>(2);
		if (penAreaTeam == ETeam.TIGERS || penAreaTeam == ETeam.BOTH)
		{
			double customMargin = margin + marginToPenArea.getOrDefault(ETeam.TIGERS, 0.0);
			penAreas.add(Geometry.getPenaltyAreaOur().withMargin(customMargin));
		}
		if (penAreaTeam == ETeam.OPPONENTS || penAreaTeam == ETeam.BOTH)
		{
			double customMargin = margin + marginToPenArea.getOrDefault(ETeam.OPPONENTS, 0.0);
			penAreas.add(Geometry.getPenaltyAreaTheir().withMargin(customMargin));
		}
		return movePosOutOfPenAreaWrtBall(pos, penAreas);
	}
}
