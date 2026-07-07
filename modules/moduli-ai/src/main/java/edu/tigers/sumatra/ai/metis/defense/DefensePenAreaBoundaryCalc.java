package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import lombok.Getter;


public class DefensePenAreaBoundaryCalc extends ACalculator
{
	@Configurable(defValue = "80.0", comment = "Extra margin to default penArea margin (must be >0 to avoid bad path planning)", unit = EConfigUnit.DISTANCE_MM)
	private static double penAreaExtraMargin = 80.0;
	@Configurable(defValue = "80.0", comment = "radius of the corners to smoothen the transition", unit = EConfigUnit.DISTANCE_MM)
	private static double penAreaCornerRadius = 80.0;

	@Getter
	private IShapeBoundary penAreaBoundary;


	@Override
	protected void doCalc()
	{
		penAreaBoundary = Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() + penAreaExtraMargin)
				.withRoundedCorners(penAreaCornerRadius)
				.getShapeBoundary();
		super.doCalc();
	}
}
