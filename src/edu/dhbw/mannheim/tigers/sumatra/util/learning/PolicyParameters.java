/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 24, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.learning;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 * Parameters from policysearchtoolbox for learning bot movement
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PolicyParameters
{
	private final String				fileName;
	private List<Float>				minValues	= new ArrayList<Float>();
	private List<Float>				maxValues	= new ArrayList<Float>();
	private List<Float>				stdDevInit	= new ArrayList<Float>();
	private List<Float>				minContexts	= new ArrayList<Float>();
	private List<Float>				maxContexts	= new ArrayList<Float>();
	private float						resetProb;
	private float						dim;
	private float						numSamples;
	private float						dt;
	private float						seed;
	
	private List<DimensionParams>	dimParams	= new ArrayList<>();
	
	
	private PolicyParameters(final String fileName)
	{
		this.fileName = fileName;
	}
	
	
	/**
	 * Load params from file
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException if file could not be parsed
	 */
	public static PolicyParameters fromFile(final String fileName) throws IOException
	{
		PolicyParameters params = new PolicyParameters(fileName);
		params.load();
		return params;
	}
	
	
	/**
	 * Load params from file
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException if file could not be parsed
	 */
	private void load() throws IOException
	{
		Stream<String> linesStream = Files.lines(Paths.get(fileName));
		String[] lines = linesStream.toArray(size -> new String[size]);
		linesStream.close();
		if (lines.length < 2)
		{
			throw new IllegalArgumentException("Not enough lines");
		}
		int numGlobParams = readNum(lines[0]);
		Map<String, List<Float>> globalParams = new HashMap<String, List<Float>>(numGlobParams);
		for (int i = 0; i < numGlobParams; i++)
		{
			Map.Entry<String, List<Float>> pl = readParamLine(lines[i + 1]);
			globalParams.put(pl.getKey(), pl.getValue());
		}
		minValues = globalParams.get("minvalues");
		maxValues = globalParams.get("maxvalues");
		stdDevInit = globalParams.get("stdevInit");
		minContexts = globalParams.get("mincontexts");
		maxContexts = globalParams.get("maxcontexts");
		resetProb = globalParams.get("resetProb").get(0);
		dim = globalParams.get("dim").get(0);
		numSamples = globalParams.get("numSamples").get(0);
		dt = globalParams.get("dt").get(0);
		seed = globalParams.getOrDefault("seed", Arrays.asList(new Float[] { 0f })).get(0);
		
		int offset = numGlobParams + 1;
		for (int d = 0; d < dim; d++)
		{
			int numParams = readNum(lines[offset++]);
			Map<String, List<Float>> params = new HashMap<String, List<Float>>(numGlobParams);
			for (int i = 0; i < numParams; i++)
			{
				Map.Entry<String, List<Float>> pl = readParamLine(lines[offset++]);
				params.put(pl.getKey(), pl.getValue());
			}
			DimensionParams dp = new DimensionParams();
			dp.numPoints = readNum(lines[offset++]);
			dp.states = readData(lines, offset, dp.numPoints);
			offset += dp.numPoints;
			dp.actions = readData(lines, offset, dp.numPoints);
			offset += dp.numPoints;
			dp.weights = readData(lines, offset, dp.numPoints);
			offset += dp.numPoints;
			dp.bandwidth = params.get("bandwidth");
			dp.scale = params.get("scale").get(0);
			dp.lambda = params.get("lambda").get(0);
			dp.squashed = params.getOrDefault("squashed", Arrays.asList(new Float[] { 0f })).get(0) > 0;
			if (dp.squashed)
			{
				dp.squashingParams = params.get("squashingParams");
			}
			dimParams.add(dp);
		}
	}
	
	
	private int readNum(final String line)
	{
		String[] items = line.split(" ");
		float f = Float.valueOf(items[1]);
		return (int) f;
	}
	
	
	private Map.Entry<String, List<Float>> readParamLine(final String line)
	{
		String[] items = line.split(" ");
		List<Float> data = new ArrayList<Float>(items.length - 1);
		for (int i = 1; i < items.length; i++)
		{
			data.add(Float.valueOf(items[i]));
		}
		return new AbstractMap.SimpleEntry<String, List<Float>>(items[0], data);
	}
	
	
	private float[][] readData(final String[] lines, final int offset, final int numPoints)
	{
		float[][] data = new float[numPoints][];
		for (int i = 0; i < numPoints; i++)
		{
			String[] items = lines[offset + i].split(",");
			float[] floats = new float[items.length];
			for (int j = 0; j < items.length; j++)
			{
				floats[j] = Float.valueOf(items[j]);
			}
			data[i] = floats;
		}
		return data;
	}
	
	
	/**
	 * @return
	 */
	public int getStateDim()
	{
		return (int) (dim * 2);
	}
	
	
	/**
	 * @return
	 */
	public int getActionDim()
	{
		return getStateDim() / 2;
	}
	
	
	/**
	 * @return the minValues
	 */
	public List<Float> getMinValues()
	{
		return minValues;
	}
	
	
	/**
	 * @return the maxValues
	 */
	public List<Float> getMaxValues()
	{
		return maxValues;
	}
	
	
	/**
	 * @return the stdDevInit
	 */
	public List<Float> getStdDevInit()
	{
		return stdDevInit;
	}
	
	
	/**
	 * @return the resetProp
	 */
	public float getResetProb()
	{
		return resetProb;
	}
	
	
	/**
	 * @return the numSamples
	 */
	public final int getNumSamples()
	{
		return (int) numSamples;
	}
	
	
	/**
	 * @return the dim
	 */
	public final int getDim()
	{
		return (int) dim;
	}
	
	
	/**
	 * @return the dimParams
	 */
	public final List<DimensionParams> getDimParams()
	{
		return dimParams;
	}
	
	
	/**
	 * @return the dt
	 */
	public final float getDt()
	{
		return dt;
	}
	
	
	/**
	 * @return
	 */
	public final int getSeed()
	{
		return (int) seed;
	}
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class DimensionParams
	{
		private List<Float>	bandwidth			= new ArrayList<Float>();
		private float			scale;
		private float			lambda;
		private int				numPoints;
		private boolean		squashed;
		
		private float[][]		states;
		private float[][]		actions;
		private float[][]		weights;
		private List<Float>	squashingParams	= new ArrayList<Float>();
		
		
		/**
		 * @return the squashingParams
		 */
		public List<Float> getSquashingParams()
		{
			return squashingParams;
		}
		
		
		/**
		 * @param squashingParams the squashingParams to set
		 */
		public void setSquashingParams(final List<Float> squashingParams)
		{
			this.squashingParams = squashingParams;
		}
		
		
		/**
		 * @return the bandwidth
		 */
		public List<Float> getBandwidth()
		{
			return bandwidth;
		}
		
		
		/**
		 * @return the scale
		 */
		public float getScale()
		{
			return scale;
		}
		
		
		/**
		 * @return the lambda
		 */
		public float getLambda()
		{
			return lambda;
		}
		
		
		/**
		 * @return the states
		 */
		public float[][] getStates()
		{
			return states;
		}
		
		
		/**
		 * @return the actions
		 */
		public float[][] getActions()
		{
			return actions;
		}
		
		
		/**
		 * @return the weights
		 */
		public float[][] getWeights()
		{
			return weights;
		}
		
		
		/**
		 * @return the numPoints
		 */
		public int getNumPoints()
		{
			return numPoints;
		}
		
		
		/**
		 * @param numPoints the numPoints to set
		 */
		public final void setNumPoints(final int numPoints)
		{
			this.numPoints = numPoints;
		}
		
		
		/**
		 * @return the squashed
		 */
		public boolean isSquashed()
		{
			return squashed;
		}
		
		
		/**
		 * @param squashed the squashed to set
		 */
		public void setSquashed(final boolean squashed)
		{
			this.squashed = squashed;
		}
	}
	
	
	/**
	 * @return the minContexts
	 */
	public final List<Float> getMinContexts()
	{
		return minContexts;
	}
	
	
	/**
	 * @return the maxContexts
	 */
	public final List<Float> getMaxContexts()
	{
		return maxContexts;
	}
	
	
	/**
	 * @return the fileName
	 */
	public String getFileName()
	{
		return fileName;
	}
}
