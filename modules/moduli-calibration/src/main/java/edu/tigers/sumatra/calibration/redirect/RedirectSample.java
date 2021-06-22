/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration.redirect;

import edu.tigers.sumatra.calibration.CalibrationDataSample;
import edu.tigers.sumatra.data.collector.IExportable;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.lang.Validate;

import java.util.List;


@Value
@Builder
@ToString(exclude = { "samplesPre", "samplesPost" })
public class RedirectSample implements IExportable
{
	double dirIn;
	double dirOut;
	double angle;
	double angleIn;
	double angleOut;
	double angleDiff;
	double angleDiffFactor;
	double kickSpeed;
	double speedIn;
	double speedOut;
	double meanDiffPre;
	double meanDiffPost;
	double stdDevDiffPre;
	double stdDevDiffPost;

	List<CalibrationDataSample> samplesPre;
	List<CalibrationDataSample> samplesPost;


	@Override
	public List<Number> getNumberList()
	{
		return List.of(
				dirIn,
				dirOut,
				angle,
				angleIn,
				angleOut,
				angleDiff,
				angleDiffFactor,
				kickSpeed,
				speedIn,
				speedOut,
				meanDiffPre,
				meanDiffPost,
				stdDevDiffPre,
				stdDevDiffPost
		);
	}


	@Override
	public List<String> getHeaders()
	{
		return List.of(
				"dirIn",
				"dirOut",
				"angle",
				"angleIn",
				"angleOut",
				"angleDiff",
				"angleDiffFactor",
				"kickSpeed",
				"speedIn",
				"speedOut",
				"meanDiffPre",
				"meanDiffPost",
				"stdDevDiffPre",
				"stdDevDiffPost"
		);
	}


	public static RedirectSample fromValues(List<String> values)
	{
		int i = 0;
		var sample = RedirectSample.builder()
				.dirIn(Double.parseDouble(values.get(i++)))
				.dirOut(Double.parseDouble(values.get(i++)))
				.angle(Double.parseDouble(values.get(i++)))
				.angleIn(Double.parseDouble(values.get(i++)))
				.angleOut(Double.parseDouble(values.get(i++)))
				.angleDiff(Double.parseDouble(values.get(i++)))
				.angleDiffFactor(Double.parseDouble(values.get(i++)))
				.kickSpeed(Double.parseDouble(values.get(i++)))
				.speedIn(Double.parseDouble(values.get(i++)))
				.speedOut(Double.parseDouble(values.get(i++)))
				.meanDiffPre(Double.parseDouble(values.get(i++)))
				.meanDiffPost(Double.parseDouble(values.get(i++)))
				.stdDevDiffPre(Double.parseDouble(values.get(i++)))
				.stdDevDiffPost(Double.parseDouble(values.get(i++)))
				.build();
		Validate.isTrue(values.size() == i, "Read all values: ", i);
		return sample;
	}
}
