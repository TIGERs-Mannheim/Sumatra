/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBallThreatSourceType;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class KeeperPassThreatCalc extends ACalculator
{
	private final Supplier<Pass> keeperPass;

	private DefenseBallThreat keeperPassThreat;


	public Optional<DefenseBallThreat> getKeeperPassThreat()
	{
		return Optional.ofNullable(keeperPassThreat);
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return keeperPass.get() != null;
	}


	@Override
	protected void reset()
	{
		keeperPassThreat = null;
	}


	@Override
	protected void doCalc()
	{
		keeperPassThreat = threatFromPass(keeperPass.get());
		getShapes(EAiShapesLayer.DEFENSE_BALL_THREAT)
				.add(new DrawableLine(keeperPassThreat.getThreatLine(), Color.PINK).setStrokeWidth(10));
	}


	private DefenseBallThreat threatFromPass(Pass keeperPass)
	{
		if (keeperPass == null)
		{
			return null;
		}
		// Reversed direction as only the pass target is outside pen area and following defense parts assume threat line start is outside
		var threatLine = Lines.segmentFromPoints(keeperPass.getKick().getTarget(), Geometry.getGoalOur().getCenter());
		return new DefenseBallThreat(
				Vector2.zero(),
				threatLine,
				null,
				null,
				EDefenseBallThreatSourceType.KEEPER_PASS
		);
	}
}
