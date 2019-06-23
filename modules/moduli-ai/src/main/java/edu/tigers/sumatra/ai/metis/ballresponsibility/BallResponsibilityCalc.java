/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballresponsibility;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.awt.Color;
import java.util.List;
import java.util.Optional;


/**
 * Determine who is responsible for handling the ball
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallResponsibilityCalc extends ACalculator
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		List<IPassTarget> handledPassTargets = newTacticalField.getAiInfoFromPrevFrame().getActivePassTargets();
		if (ballNearOurPenArea()
				&& !newTacticalField.getGameState().isStandardSituationForUs()
				&& criticalBallMovement()
				&& handledPassTargets.isEmpty())
		{
			newTacticalField.setBallResponsibility(EBallResponsibility.DEFENSE);
		} else
		{
			newTacticalField.setBallResponsibility(EBallResponsibility.OFFENSE);
		}
		
		double teamOffset = baseAiFrame.getTeamColor() == ETeamColor.BLUE ? 13 : 0;
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_BALL_RESPONSIBILITY)
				.add(new DrawableBorderText(Vector2.fromXY(10, 100 + teamOffset),
						"Ball Responsibility: " + newTacticalField.getBallResponsibility(),
						baseAiFrame.getTeamColor().getColor()));
	}
	
	
	private boolean ballNearOurPenArea()
	{
		double margin = Geometry.getBotRadius() * 4;
		if (getAiFrame().getGamestate().isStoppedGame())
		{
			margin += RuleConstraints.getStopRadius() * 2;
		}
		
		if (getAiFrame().getPrevFrame().getTacticalField()
				.getBallResponsibility() == EBallResponsibility.DEFENSE)
		{
			// hysteresis
			margin += 100;
		}
		
		IPenaltyArea penaltyArea = Geometry.getPenaltyAreaOur().withMargin(margin);
		boolean nearPenArea = Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(), margin);
		if (nearPenArea)
		{
			final List<IDrawableShape> drawableShapes = penaltyArea.getDrawableShapes();
			drawableShapes.forEach(d -> d.setFill(true));
			drawableShapes.forEach(d -> d.setColor(new Color(255, 0, 0, 100)));
			getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_BALL_RESPONSIBILITY).addAll(drawableShapes);
		}
		return nearPenArea;
	}
	
	
	private boolean criticalBallMovement()
	{
		if (getBall().getVel().getLength2() < 1
				|| getAiFrame().getPrevFrame().getTacticalField().getBallResponsibility() == EBallResponsibility.DEFENSE)
		{
			return true;
		}
		
		ILineSegment penAreaGoalLine = Lines.segmentFromPoints(
				Vector2.fromXY(-Geometry.getFieldLength() / 2, Geometry.getFieldWidth() / 2),
				Vector2.fromXY(-Geometry.getFieldLength() / 2, -Geometry.getFieldWidth() / 2));
		
		final Optional<IVector2> intersection = penAreaGoalLine
				.intersectHalfLine(Lines.halfLineFromDirection(getBall().getPos(), getBall().getVel()));
		if (intersection.isPresent())
		{
			ITriangle triangle = Triangle.fromCorners(
					getBall().getPos(),
					penAreaGoalLine.getStart(),
					penAreaGoalLine.getEnd());
			getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_BALL_RESPONSIBILITY)
					.add(new DrawableTriangle(triangle, new Color(255, 0, 0, 150)));
		}
		return intersection.isPresent();
	}
}
