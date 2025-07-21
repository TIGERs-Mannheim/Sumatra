/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.modelidentification.ball;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.kick.estimators.IBallModelIdentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.ToDoubleFunction;


@Log4j2
@RequiredArgsConstructor
public class BallObserver
{
	private static final IShapeLayerIdentifier SHAPE_LAYER_ID = ShapeLayerIdentifier.builder().id("Ball observer")
			.category("Movement Observer").visibleByDefault(false).orderId(0).build();

	@Configurable(defValue = "31", comment = "Max buffer size for which the median of the ball model is determined")
	private static int medianBufferSize = 31;

	@Configurable(defValue = "0.2", comment = "Warn if a ball parameter deviates more than the given fraction.")
	private static double warnOnDeviation = 0.2;

	private static final Map<String, ToDoubleFunction<BallParameters>> parameterGetter = new HashMap<>();

	static
	{
		ConfigRegistration.registerClass("wp", BallObserver.class);

		parameterGetter.put("accSlide", BallParameters::getAccSlide);
		parameterGetter.put("accRoll", BallParameters::getAccRoll);
		parameterGetter.put("kSwitch", BallParameters::getKSwitch);
		parameterGetter.put("dampXYFirst", BallParameters::getChipDampingXYFirstHop);
		parameterGetter.put("dampXYOther", BallParameters::getChipDampingXYOtherHops);
		parameterGetter.put("dampZ", BallParameters::getChipDampingZ);
		parameterGetter.put("spinFactor", BallParameters::getRedirectSpinFactor);
	}

	private final Map<String, TreeSet<Double>> parameterLists = new HashMap<>();
	private final Map<String, Double> parameters = new HashMap<>();

	private final Set<String> mismatchWarningSent = new HashSet<>();


	public void onBallModelIdentificationResult(final IBallModelIdentResult ident)
	{
		ident.getModelParameters().forEach(this::updateParam);
	}


	private void updateParam(String id, double value)
	{
		TreeSet<Double> values = parameterLists.computeIfAbsent(id, key -> new TreeSet<>());
		values.add(value);

		while (values.size() > medianBufferSize)
		{
			values.removeFirst();
			values.removeLast();
		}

		ToDoubleFunction<BallParameters> getter = parameterGetter.get(id);
		if (values.size() < medianBufferSize || getter == null)
		{
			return;
		}

		double median = values.stream().sorted().skip(values.size() / 2).findFirst().orElseThrow();
		parameters.put(id, median);

		double current = getter.applyAsDouble(Geometry.getBallParameters());
		if (Math.abs(current - median) >= warnOnDeviation * Math.abs(current))
		{
			if (mismatchWarningSent.add(id))
			{
		 		log.warn("Ball parameter {} deviation: Live: {} Current: {}", id, median, current);
			}
		} else
		{
			mismatchWarningSent.remove(id);
		}
	}


	public void fillShapeMap(ShapeMap shapeMap)
	{
		int y = 12;
		for (Map.Entry<String, Double> entry : parameters.entrySet())
		{
			shapeMap.get(SHAPE_LAYER_ID).add(new DrawableBorderText(
					Vector2.fromXY(12, y++),
					String.format("%s: %.2f", entry.getKey(), entry.getValue())
			));
		}
	}
}
