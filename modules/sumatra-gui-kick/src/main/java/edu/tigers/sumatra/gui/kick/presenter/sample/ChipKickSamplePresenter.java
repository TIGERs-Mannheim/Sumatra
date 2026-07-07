package edu.tigers.sumatra.gui.kick.presenter.sample;

import edu.tigers.sumatra.gui.kick.presenter.sample.kick.Poly1Fit;
import edu.tigers.sumatra.gui.kick.presenter.sample.kick.Poly2Fit;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.modelidentification.kickspeed.data.KickModelSample;

import java.util.List;
import java.util.Locale;
import java.util.Optional;


public class ChipKickSamplePresenter extends AKickSamplePresenter
{
	public ChipKickSamplePresenter(List<KickModelSample> samples)
	{
		super(samples, true);
	}


	@Override
	protected String computeFit(List<KickModelSample> chipSamples)
	{
		StringBuilder text = new StringBuilder();

		double maxChip = chipSamples.stream()
				.mapToDouble(KickModelSample::getMeasuredKickVel)
				.max().orElse(0);

		List<IVector2> usedChipPoints = chipSamples.stream()
				.filter(s -> (s.getMeasuredKickVel() < (maxChip * 0.95)) && (s.getMeasuredKickVel() > 0.2))
				.map(s -> Vector2.fromXY(s.getMeasuredKickVel(), s.getBotStateAtKick().getKickDuration()))
				.map(IVector2.class::cast)
				.toList();

		plotSamples.removeAllPoints();
		plotFitPoly1.removeAllPoints();
		plotFitPoly2.removeAllPoints();

		Optional<Poly1Fit> chipPoly1 = Poly1Fit.fromPointsList(usedChipPoints);
		if (chipPoly1.isPresent())
		{
			text.append(
							String.format(
									Locale.ENGLISH, "Chip Poly1 Factors (1, x):      % .3f, % .3f         (Err: %.3fms)",
									chipPoly1.get().getOffset(), chipPoly1.get().getLinear(), chipPoly1.get().getAverageError()
							))
					.append(System.lineSeparator());

			for (double x = 0; x <= maxChip; x += 0.1)
			{
				plotFitPoly1.addPoint(x, chipPoly1.get().getYValue(x));
			}
		} else
		{
			text.append("Not enough fit samples for poly1 chip kick model").append(System.lineSeparator());
		}

		Optional<Poly2Fit> chipPoly2 = Poly2Fit.fromPointsList(usedChipPoints);
		if (chipPoly2.isPresent())
		{
			text.append(
							String.format(
									Locale.ENGLISH, "Chip Poly2 Factors (1, x, x^2): % .3f, % .3f, % .3f (Err: %.3fms)",
									chipPoly2.get().getOffset(), chipPoly2.get().getLinear(), chipPoly2.get().getQuadratic(),
									chipPoly2.get().getAverageError()
							))
					.append(System.lineSeparator());

			for (double x = 0; x <= maxChip; x += 0.1)
			{
				plotFitPoly2.addPoint(x, chipPoly2.get().getYValue(x));
			}
		} else
		{
			text.append("Not enough fit samples for poly2 chip kick model").append(System.lineSeparator());
		}

		chipSamples.forEach(s -> plotSamples.addPoint(s.getMeasuredKickVel(), s.getBotStateAtKick().getKickDuration()));
		usedChipPoints.forEach(p -> plotUsedSamples.addPoint(p.x(), p.y()));

		return text.toString();
	}


	@Override
	protected String[] getKickParamNames()
	{
		return new String[] { "Chip Conv. Offset", "Chip Conv. Slope", "Chip Conv. Quadratic" };
	}
}
