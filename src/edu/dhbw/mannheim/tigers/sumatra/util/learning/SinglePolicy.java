/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.02.2015
 * Author(s): Jannik Abbenseth <jannik.abbenseth@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.learning;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.PolicyParameters.DimensionParams;


class SinglePolicy
{
	DimensionParams	dp								= null;
	int					dim							= 0;
	Matrix				regularizedGramMatrix	= new Matrix(new double[0][0]);
	Matrix				invRegGramMatrix			= new Matrix(new double[0][0]);
	Matrix				stateMat						= new Matrix(new double[0][0]);
	Matrix				actionMat					= new Matrix(new double[0][0]);
	IKernel				kernel						= null;
}