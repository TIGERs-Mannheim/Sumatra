/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration.redirect;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;


@Log4j2
@RequiredArgsConstructor
public class RedirectModelEstimator
{
	private final List<RedirectSample> samples;


	public void estimateModelForAngle()
	{
		int n = samples.size();
		double[] y = new double[n+1];
		double[][] x = new double[n+1][3];
		for (int i = 0; i < n; i++)
		{
			var sample = samples.get(i);
			y[i] = sample.getAngleOut();
			x[i][0] = sample.getAngle();
			x[i][1] = sample.getSpeedIn();
			x[i][2] = sample.getSpeedOut();
		}
		y[n] = 0;
		x[n][0] = 0;
		x[n][1] = 0;
		x[n][2] = 0;

		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(y, x);
		var parameters = regression.estimateRegressionParameters();
		var parametersStdErrors = regression.estimateRegressionParametersStandardErrors();

		DecimalFormat df = new DecimalFormat("#.#####");
		log.info("Parameters: [{};{};{};{}]",
				df.format(parameters[0]),
				df.format(parameters[1]),
				df.format(parameters[2]),
				df.format(parameters[3]));
		log.info("Parameters StdErr: {}", Arrays.toString(parametersStdErrors));
	}
}
