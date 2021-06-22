/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballresponsibility;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;

import java.awt.Color;


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
	private static double marginForNoOffensiveWhenBallNearOpponentPen = 20;

	@Configurable(defValue = "50.0")
	private static double hysteresisForNoOffensiveWhenBallNearOpponentPen = 50;

	@Configurable(defValue = "false")
	private static boolean noOffensiveWhenBallInOpponentPenArea = false;

	@Configurable(defValue = "1300.0")
	private static double minMarginToOurPenAreaForIntercept = 1300;

	@Getter
	private EBallResponsibility ballResponsibility;

	private boolean isDefensiveSituationOld = false;
	private boolean isNearOpponentPenOld = false;


	@Override
	public void doCalc()
	{
		ballResponsibility = ballIsOrWillStopInsidePenaltyArea() || noInterceptorRequired()
				? EBallResponsibility.DEFENSE
				: EBallResponsibility.OFFENSE;

		double teamOffset = getAiFrame().getTeamColor() == ETeamColor.BLUE ? 13 : 0;
		getShapes(EAiShapesLayer.AI_BALL_RESPONSIBILITY).add(
				new DrawableBorderText(Vector2.fromXY(10, 100 + teamOffset),
						"Ball Responsibility: " + ballResponsibility,
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
		boolean inOpponentPen = false;
		if (noOffensiveWhenBallInOpponentPenArea)
		{
			margin = marginForNoOffensiveWhenBallNearOpponentPen;
			if (isNearOpponentPenOld)
			{
				margin += hysteresisForNoOffensiveWhenBallNearOpponentPen;
			}
			inOpponentPen = ballStaysInsidePenArea(Geometry.getPenaltyAreaTheir().withMargin(margin));
			isNearOpponentPenOld = inOpponentPen;
		}

		return defSituation || inOpponentPen;
	}


	private boolean ballStaysInsidePenArea(IPenaltyArea area)
	{
		boolean isAndWillStopInsidePenArea = area.isPointInShapeOrBehind(getBall().getPos())
				&& area.isPointInShapeOrBehind(getBall().getTrajectory().getPosByVel(0).getXYVector());

		DrawableRectangle dr = new DrawableRectangle(area.getRectangle(), new Color(4, 100, 156, 100));
		dr.setFill(isAndWillStopInsidePenArea);
		getShapes(EAiShapesLayer.AI_BALL_RESPONSIBILITY).add(dr);
		return isAndWillStopInsidePenArea;
	}


	private boolean noInterceptorRequired()
	{
		return getAiFrame().getGameState().isStandardSituationForThem()
				&& tooNearToOurPenAreaForIntercept();
	}


	private boolean tooNearToOurPenAreaForIntercept()
	{
		return Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(), minMarginToOurPenAreaForIntercept);
	}
}
