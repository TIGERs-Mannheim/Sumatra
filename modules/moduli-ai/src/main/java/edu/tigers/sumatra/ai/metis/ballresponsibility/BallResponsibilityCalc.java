/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballresponsibility;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Determine who is responsible for handling the ball.
 */
public class BallResponsibilityCalc extends ACalculator
{
	@Configurable(defValue = "200.0")
	private static double marginForNoOffensiveWhenBallNearOurPen = 200;

	@Configurable(defValue = "150.0")
	private static double hysteresisForNoOffensiveWhenBallNearOurPen = 150;

	@Configurable(defValue = "20.0")
	private static double marginForNoOffensiveWhenBallNearEnemyPen = 20;

	@Configurable(defValue = "50.0")
	private static double hysteresisForNoOffensiveWhenBallNearEnemyPen = 50;

	@Configurable(defValue = "false")
	private static boolean noOffensiveWhenBallInEnemyPenArea = false;


	private boolean isDefensiveSituationOld = false;
	private boolean isNearEnemyPenOld = false;


	@Override
	public void doCalc()
	{
		EBallResponsibility responsibility = ballIsOrWillStopInsidePenaltyArea()
				? EBallResponsibility.DEFENSE
				: EBallResponsibility.OFFENSE;
		getNewTacticalField().setBallResponsibility(responsibility);

		double teamOffset = getAiFrame().getTeamColor() == ETeamColor.BLUE ? 13 : 0;
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_BALL_RESPONSIBILITY)
				.add(new DrawableBorderText(Vector2.fromXY(10, 100 + teamOffset),
						"Ball Responsibility: " + responsibility,
						getAiFrame().getTeamColor().getColor()));
	}


	private boolean ballIsOrWillStopInsidePenaltyArea()
	{
		// check for our penArea
		boolean defSituation;
		double margin = marginForNoOffensiveWhenBallNearOurPen;
		if (isDefensiveSituationOld)
		{
			margin += hysteresisForNoOffensiveWhenBallNearOurPen;
		}
		defSituation = ballStaysInsidePenArea(Geometry.getPenaltyAreaOur().withMargin(margin));
		isDefensiveSituationOld = defSituation;

		// check for opponent penArea.
		boolean inEnemyPen = false;
		if (noOffensiveWhenBallInEnemyPenArea)
		{
			margin = marginForNoOffensiveWhenBallNearEnemyPen;
			if (isNearEnemyPenOld)
			{
				margin += hysteresisForNoOffensiveWhenBallNearEnemyPen;
			}
			inEnemyPen = ballStaysInsidePenArea(Geometry.getPenaltyAreaTheir().withMargin(margin));
			isNearEnemyPenOld = inEnemyPen;
		}

		return defSituation || inEnemyPen;
	}


	private boolean ballStaysInsidePenArea(IPenaltyArea area)
	{
		boolean isAndWillStopInsidePenArea = area.isPointInShape(getBall().getPos())
				&& area.isPointInShapeOrBehind(getBall().getTrajectory().getPosByVel(0).getXYVector());

		DrawableRectangle dr = new DrawableRectangle(area.getRectangle(), new Color(4, 100, 156, 100));
		dr.setFill(isAndWillStopInsidePenArea);
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_BALL_RESPONSIBILITY).add(dr);
		return isAndWillStopInsidePenArea;
	}
}
