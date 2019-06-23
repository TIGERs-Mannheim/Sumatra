/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.penarea;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public abstract class APenAreaTest
{
	
	protected double depth = 400;
	protected double length = 800;
	protected double goalX = -1000;
	protected double borderX = goalX + depth;
	protected IDefensePenArea penaltyArea;
	protected IVector2 goalCenter = Vector2.fromXY(goalX, 0);
	
	protected IVector2 center = Vector2f.ZERO_VECTOR;
	protected IVector2 penBorderCenter = Vector2.fromXY(borderX, 0);
	
	
	@Test
	public void projectPointOnPenaltyAreaLine()
	{
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(0, 0)))
				.as("Center does not project to front border center")
				.isEqualTo(penBorderCenter);
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(goalCenter))
				.as("Goal center does not project to front border center")
				.isEqualTo(penBorderCenter);
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX - 1, 0)))
				.as("Behind goal center does not project to front border center")
				.isEqualTo(penBorderCenter);
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(borderX + 1, 0)))
				.as("In front of penArea does not project to front border center")
				.isEqualTo(penBorderCenter);
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth * 2, depth)))
				.as("Does not project to positive front from outside")
				.isEqualTo(Vector2.fromXY(borderX, length / 2 / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth * 2, -depth)))
				.as("Does not project to negative front from outside")
				.isEqualTo(Vector2.fromXY(borderX, -length / 2 / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth * 2, depth * 2)))
				.as("Does not project to positive corner from outside")
				.isEqualTo(Vector2.fromXY(borderX, length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth * 2, -depth * 2)))
				.as("Does not project to negative corner from outside")
				.isEqualTo(Vector2.fromXY(borderX, -length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth / 2, depth / 2)))
				.as("Does not project to positive corner from inside")
				.isEqualTo(Vector2.fromXY(borderX, length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth / 2, -depth / 2)))
				.as("Does not project to negative corner from inside")
				.isEqualTo(Vector2.fromXY(borderX, -length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth, length)))
				.as("Does not project to positive side from outside")
				.isEqualTo(Vector2.fromXY(borderX - depth / 2, length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth, -length)))
				.as("Does not project to negative side from outside")
				.isEqualTo(Vector2.fromXY(borderX - depth / 2, -length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth / 4, length / 4)))
				.as("Does not project to positive side from inside")
				.isEqualTo(Vector2.fromXY(borderX - depth / 2, length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX + depth / 4, -length / 4)))
				.as("Does not project to negative side from inside")
				.isEqualTo(Vector2.fromXY(borderX - depth / 2, -length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX, length)))
				.as("Does not project to lower positive corner")
				.isEqualTo(Vector2.fromXY(goalX, length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX, -length)))
				.as("Does not project to lower negative corner")
				.isEqualTo(Vector2.fromXY(goalX, -length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX - 100, length)))
				.as("Does not project to lower positive corner from outside field")
				.isEqualTo(Vector2.fromXY(goalX, length / 2));
		
		assertThat(penaltyArea.projectPointOnPenaltyAreaLine(Vector2.fromXY(goalX - 100, -length)))
				.as("Does not project to lower negative corner from outside field")
				.isEqualTo(Vector2.fromXY(goalX, -length / 2));
	}
	
	
	@Test
	public void nearestPointOutside()
	{
		assertThat(penaltyArea.nearestPointOutside(center))
				.as("Center is outside and should be returned as-is")
				.isEqualTo(center);
		
		assertThat(penaltyArea.nearestPointOutside(goalCenter))
				.as("Goal center should be moved to border")
				.isIn(Vector2.fromXY(goalX, length / 2),
						Vector2.fromXY(goalX, -length / 2),
						Vector2.fromXY(goalX + depth, 0));
		
		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX + 1, 0)))
				.as("Point inside should be outside")
				.isEqualTo(Vector2.fromXY(borderX, 0));
		
		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX, 1)))
				.as("Point inside should be outside")
				.isEqualTo(Vector2.fromXY(goalX, length / 2));
		
		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX, -1)))
				.as("Point inside should be outside")
				.isEqualTo(Vector2.fromXY(goalX, -length / 2));
		
		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(borderX - 1, 1)))
				.as("Point inside should be outside")
				.returns(true, iVector2 -> iVector2.distanceTo(Vector2.fromXY(borderX, 1)) < 1);
		
		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(borderX - 1, -1)))
				.as("Point inside should be outside")
				.returns(true, iVector2 -> iVector2.distanceTo(Vector2.fromXY(borderX, -1)) < 1);
		
		assertThat(penaltyArea.nearestPointOutside(Vector2.fromXY(goalX - 1, 0)))
				.as("Point outside should be outside")
				.isEqualTo(Vector2.fromXY(goalX - 1, 0));
	}
	
	
	@Test
	public void lineIntersections()
	{
		assertThat(penaltyArea.lineIntersections(Line.fromPoints(center, goalCenter)))
				.as("Center to goal center should have exactly one intersection")
				.hasSize(1);
		
		assertThat(penaltyArea.lineIntersections(Line.fromPoints(center, goalCenter)).get(0))
				.as("Center to goal center should be on border")
				.isEqualTo(Vector2.fromXY(borderX, center.y()));
		
		assertThat(
				penaltyArea.lineIntersections(Line.fromPoints(Vector2.fromXY(borderX + 1, length / 2 + 1), goalCenter)))
						.as("Upper positive corner to goal should have exactly one intersection")
						.hasSize(1);
		
		assertThat(
				penaltyArea.lineIntersections(Line.fromPoints(Vector2.fromXY(borderX + 1, -(length / 2 + 1)), goalCenter)))
						.as("Lower positive corner to goal should have exactly one intersection")
						.hasSize(1);
		
		assertThat(
				penaltyArea.lineIntersections(Line.fromPoints(Vector2.fromXY(goalX - 1, -(length / 2 + 1)), goalCenter)))
						.as("Point outside field to goal should have intersection on opposite side")
						.element(0).matches(iVector2 -> Math.signum(iVector2.y()) > 0);
	}
	
}
