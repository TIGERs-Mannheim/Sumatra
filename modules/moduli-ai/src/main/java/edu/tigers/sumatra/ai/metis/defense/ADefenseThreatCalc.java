package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;

import java.awt.Color;
import java.util.List;


public abstract class ADefenseThreatCalc extends ACalculator
{
	protected void drawThreat(IDefenseThreat threat)
	{
		final List<IDrawableShape> shapes = getShapes(EAiShapesLayer.DEFENSE_THREATS);
		if (threat.getProtectionLine().isPresent())
		{
			DrawableLine dProtectionLine = new DrawableLine(threat.getProtectionLine().get(), Color.red);
			dProtectionLine.setStrokeWidth(30);
			shapes.add(dProtectionLine);
		}

		shapes.add(
				new DrawableLine(threat.getThreatLine(), Color.BLACK));
		shapes.add(
				new DrawableLine(Line.fromPoints(threat.getPos(), Geometry.getGoalOur().getLeftPost()), Color.BLACK));
		shapes.add(
				new DrawableLine(Line.fromPoints(threat.getPos(), Geometry.getGoalOur().getRightPost()), Color.BLACK));
	}


	protected ILineSegment centerBackProtectionLine(final ILineSegment threatLine, double margin)
	{
		final ILineSegment threatDefendingLine = DefenseMath.getThreatDefendingLine(
				threatLine,
				margin,
				DefenseConstants.getMinGoOutDistance(),
				DefenseConstants.getMaxGoOutDistance());
		if (threatDefendingLine.getLength() < 1)
		{
			return null;
		}
		return threatDefendingLine;
	}
}
