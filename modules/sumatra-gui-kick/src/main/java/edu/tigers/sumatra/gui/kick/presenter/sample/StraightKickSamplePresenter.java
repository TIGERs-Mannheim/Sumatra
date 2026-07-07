package edu.tigers.sumatra.gui.kick.presenter.sample;

import edu.tigers.sumatra.gui.kick.presenter.sample.kick.Poly1Fit;
import edu.tigers.sumatra.gui.kick.presenter.sample.kick.Poly2Fit;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.modelidentification.kickspeed.data.KickModelSample;

import java.util.List;
import java.util.Locale;
import java.util.Optional;


public class StraightKickSamplePresenter extends AKickSamplePresenter
{
	public StraightKickSamplePresenter(List<KickModelSample> samples)
	{

		super(samples, false);
	}


	@Override
	protected String computeFit(List<KickModelSample> straightSamples)
	{
		StringBuilder text = new StringBuilder();

		double maxStraight = straightSamples.stream()
				.mapToDouble(KickModelSample::getMeasuredKickVel)
				.max().orElse(0);

		List<IVector2> usedStraightPoints = straightSamples.stream()
				.filter(s -> s.getMeasuredKickVel() > 0.1)
				.map(s -> Vector2.fromXY(s.getMeasuredKickVel(), s.getBotStateAtKick().getKickDuration()))
				.map(IVector2.class::cast)
				.toList();

		plotSamples.removeAllPoints();
		plotFitPoly1.removeAllPoints();
		plotFitPoly2.removeAllPoints();

		Optional<Poly1Fit> straightPoly1 = Poly1Fit.fromPointsList(usedStraightPoints);
		if (straightPoly1.isPresent())
		{
			text.append(
							String.format(
									Locale.ENGLISH, "Straight Poly1 Factors (1, x):      % .3f, % .3f         (Err: %.3fms)",
									straightPoly1.get().getOffset(), straightPoly1.get().getLinear(),
									straightPoly1.get().getAverageError()
							))
					.append(System.lineSeparator());

			for (double x = 0; x <= maxStraight; x += 0.1)
			{
				plotFitPoly1.addPoint(x, straightPoly1.get().getYValue(x));
			}
		} else
		{
			text.append("Not enough fit samples for poly1 straight kick model").append(System.lineSeparator());
		}

		Optional<Poly2Fit> straightPoly2 = Poly2Fit.fromPointsList(usedStraightPoints);
		if (straightPoly2.isPresent())
		{
			text.append(
							String.format(
									Locale.ENGLISH, "Straight Poly2 Factors (1, x, x^2): % .3f, % .3f, % .3f (Err: %.3fms)",
									straightPoly2.get().getOffset(), straightPoly2.get().getLinear(),
									straightPoly2.get().getQuadratic(), straightPoly2.get().getAverageError()
							))
					.append(System.lineSeparator());

			for (double x = 0; x <= maxStraight; x += 0.1)
			{
				plotFitPoly2.addPoint(x, straightPoly2.get().getYValue(x));
			}
		} else
		{
			text.append("Not enough fit samples for poly2 straight kick model").append(System.lineSeparator());
		}

		straightSamples.forEach(
				s -> plotSamples.addPoint(s.getMeasuredKickVel(), s.getBotStateAtKick().getKickDuration()));
		usedStraightPoints.forEach(p -> plotUsedSamples.addPoint(p.x(), p.y()));

		return text.toString();
	}


	@Override
	protected String[] getKickParamNames()
	{
		return new String[] { "Kick Conv. Offset", "Kick Conv. Slope", "Kick Conv. Quadratic" };
	}
}
