/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.11.2014
 * Author(s): Jannik Abbenseth <jannik.abbenseth@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.learning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.LinearPolicy;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.PolicyParameters.DimensionParams;


/**
 * Gaussian Kernel to evaluate a Gaussian Policy.
 * 
 * @author Jannik Abbenseth <jannik.abbenseth@gmail.com>
 */
public class GaussianPolicy implements IPolicyController
{
	
	static
	{
		Sumatra.touch();
	}
	
	private static final Logger		log			= Logger.getLogger(GaussianPolicy.class.getName());
	private final PolicyParameters	params;
	private final String					dataFile;
	private final Random					rnd;
	private final List<SinglePolicy>	policies		= new ArrayList<SinglePolicy>();
	private final boolean				training;
	private final List<Rollout>		rollouts		= new ArrayList<>();
	
	private float							nextStateDt	= 0f;
	
	private final boolean				useLinear	= false;
	private final LinearPolicy			linPolicy	= new LinearPolicy();
	
	private static class Rollout
	{
		List<DataSet>	data	= new ArrayList<>();
	}
	
	private static class DataSet
	{
		double[]	states		= new double[0];
		double[]	actions		= new double[0];
		double[]	nextStates	= new double[0];
	}
	
	
	private GaussianPolicy(final PolicyParameters params, final String dataFile, final boolean saveData)
	{
		training = saveData;
		this.params = params;
		this.dataFile = dataFile;
		// rnd = new Random(params.getSeed());
		rnd = new Random(SumatraClock.currentTimeMillis());
		if (saveData)
		{
			rollouts.add(new Rollout());
		}
	}
	
	
	/**
	 * Start a new rollout
	 * 
	 * @return
	 */
	public boolean rolloutDone()
	{
		synchronized (rollouts)
		{
			Rollout dl = rollouts.get(rollouts.size() - 1);
			if (dl.data.size() < 3)
			{
				rollouts.remove(rollouts.size() - 1);
			} else
			{
				dl.data.remove(dl.data.size() - 1);
				if (rollouts.size() >= params.getNumSamples())
				{
					saveData();
					return true;
				}
			}
			
			rollouts.add(new Rollout());
		}
		return false;
	}
	
	
	/**
	 * Testing purpose
	 * 
	 * @param args
	 */
	public static void main(final String[] args)
	{
		PolicyParameters p;
		try
		{
			p = PolicyParameters.fromFile("/dev/shm/controller.txt");
		} catch (IOException err)
		{
			err.printStackTrace();
			return;
		}
		GaussianPolicy gp = GaussianPolicy.fromParameters(p, "/dev/shm/rolloutdata.txt", true);
		// gp.getControl(new Matrix(new double[][] { { 1, 2, 3, 4, 5, 6, 7, 8 } }));
		// gp.getControl(new Matrix(new double[][] { { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 } }));
		Matrix control = gp.getControl(new Matrix(new double[][] { { .1, .1 } }));
		control.print(5, 2);
		// gp.getControl(new Matrix(new double[][] { { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 },
		// { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 } }));
	}
	
	
	/**
	 * @param ctrlFile
	 * @param dataFile
	 * @return
	 */
	public static GaussianPolicy createGp(final String ctrlFile, final String dataFile)
	{
		PolicyParameters pParams;
		try
		{
			pParams = PolicyParameters.fromFile(ctrlFile);
			// pParams = PolicyParameters.fromFile("testdata/controller3d2.txt");
		} catch (IOException err)
		{
			log.error("Could not load controller file", err);
			throw new IllegalStateException("controller file not found!", err);
		}
		GaussianPolicy gp = GaussianPolicy.fromParameters(pParams, dataFile, true);
		return gp;
	}
	
	
	/**
	 * generate a Gaussian Policy object from parameters
	 * 
	 * @param params
	 * @param dataFile
	 * @param saveData
	 * @return
	 */
	public static GaussianPolicy fromParameters(final PolicyParameters params, final String dataFile,
			final boolean saveData)
	{
		GaussianPolicy gap = new GaussianPolicy(params, dataFile, saveData);
		
		int dim = 0;
		for (DimensionParams dp : params.getDimParams())
		{
			SinglePolicy policy = new SinglePolicy();
			policy.dp = dp;
			policy.dim = dim++;
			int rows = dp.getStates().length;
			int cols;
			int cols2;
			if (dp.getStates().length == 0)
			{
				cols = 0;
				cols2 = 0;
			} else
			{
				cols = dp.getStates()[0].length;
				cols2 = dp.getActions()[0].length;
			}
			if (dp.getNumPoints() > 0)
			{
				policy.stateMat = new Matrix(rows, cols);
				for (int r = 0; r < rows; r++)
				{
					for (int c = 0; c < cols; c++)
					{
						policy.stateMat.set(r, c, dp.getStates()[r][c]);
					}
				}
				policy.actionMat = new Matrix(rows, cols2);
				for (int r = 0; r < rows; r++)
				{
					for (int c = 0; c < cols2; c++)
					{
						policy.actionMat.set(r, c, dp.getActions()[r][c]);
					}
				}
				
				if (dp.isSquashed())
				{
					policy.kernel = new SquashedExponentialKernel(dp.getSquashingParams());
				}
				else
				{
					policy.kernel = new ExponentialQuadraticKernel();
				}
				
				
				policy.kernel.computeRegularizedGramMatrix(dp, policy);
				try
				{
					policy.invRegGramMatrix = policy.regularizedGramMatrix.inverse();
				} catch (Exception err)
				{
					log.error("Could not invert gram matrix", err);
					policy.stateMat = new Matrix(0, 0);
					policy.actionMat = new Matrix(0, 0);
					dp.setNumPoints(0);
				}
			}
			gap.policies.add(policy);
		}
		
		return gap;
	}
	
	private Matrix	u				= new Matrix(1, 6);
	private long	lastUpdate	= 0;
	
	
	/**
	 * execute learned controller
	 * 
	 * @param state
	 * @return
	 */
	@Override
	public Matrix getControl(final Matrix state)
	{
		float dt = (SumatraClock.nanoTime() - lastUpdate) * 1e-9f;
		
		synchronized (rollouts)
		{
			int iRollout = rollouts.size() - 1;
			if ((nextStateDt > 0) && training && (dt > nextStateDt) && !rollouts.get(iRollout).data.isEmpty())
			{
				DataSet ds = rollouts.get(iRollout).data.get(rollouts.get(iRollout).data.size() - 1);
				if (ds.nextStates.length == 0)
				{
					ds.nextStates = state
							.getColumnPackedCopy();
				}
			}
			
			// skip update if we get too many frames
			if ((dt < getDt()))
			{
				return u;
			}
			lastUpdate = SumatraClock.nanoTime();
			
			List<Float> stdevf = params.getStdDevInit();
			Matrix mean = new Matrix(1, stdevf.size());
			Matrix stdev = new Matrix(stdevf.size(), stdevf.size());
			for (SinglePolicy policy : policies)
			{
				if (policy.dp.getNumPoints() == 0)
				{
					mean.set(0, policy.dim, 0);
					stdev.set(policy.dim, policy.dim, stdevf.get(policy.dim));
				}
				else
				{
					Matrix kvec = new Matrix(policy.dp.getNumPoints(), 1);
					for (int i = 0; i < policy.dp.getNumPoints(); i++)
					{
						{
					
					kvec.set(
							i,
							0,
							policy.kernel.kernelFn(policy.dp,
									policy.stateMat.getMatrix(i, i, 0, policy.stateMat.getColumnDimension() - 1), state));
					
				}
						
					}
					mean.set(0, policy.dim,
							((kvec.transpose()).times(policy.invRegGramMatrix)).times(policy.actionMat).get(0, 0));
					if (training)
					{
						double exp = policy.kernel.kernelFn(policy.dp, state, state);
					
						double k = ((kvec.transpose()).times(policy.invRegGramMatrix)).times(kvec).get(0, 0);
						double c = (exp + policy.dp.getLambda()) - k;
						if (c < 0)
						{
							// FIXME workaround to avoid NaNs
							stdev.set(policy.dim, policy.dim, stdevf.get(policy.dim));
							log.warn("c is negative: c=" + c + " exp=" + exp + " k=" + k + " lambda=" + policy.dp.getLambda()
									+ ".\n"
									+ "Avoiding NaNs by using default stdev: " + stdevf.get(policy.dim) + ".\n"
									+ "state: "
									+ Arrays.toString(state.getColumnPackedCopy()));
						} else
						{
							stdev.set(policy.dim, policy.dim, Math.sqrt(c));
						}
					}
				}
			}
			Matrix uRaw = mean;
			if (useLinear)
			{
				u = linPolicy.getControl(state);
			} else
			{
				if (training)
				{
					Matrix eps = new Matrix(1, mean.getColumnDimension());
					for (int j = 0; j < eps.getColumnDimension(); j++)
					{
						eps.set(0, j, rnd.nextGaussian());
						// eps.set(i, j, 1);
					}
					uRaw = mean.plus(eps.times(stdev));
				}
				u = new Matrix(uRaw.getArray());
			}
			
			
			for (int i = 0; i < u.getColumnDimension(); i++)
			{
				float maxAction = params.getMaxValues().get(i);
				float minAction = params.getMinValues().get(i);
				double action = u.get(0, i);
				if ((action > maxAction) || (action < minAction))
				{
					u.set(0, i, Math.signum(action) * maxAction);
				}
			}
			
			if (log.isDebugEnabled())
			{
				StringBuilder sb = new StringBuilder();
				sb.append("\nStates: [");
				for (int i = 0; i < state.getColumnDimension(); i++)
				{
					sb.append(String.format("%5.2f ", state.get(0, i)));
				}
				sb.append("] -> [");
				for (int i = 0; i < u.getColumnDimension(); i++)
				{
					sb.append(String.format("%5.2f ", u.get(0, i)));
				}
				sb.append("]");
				for (int i = 0; i < u.getColumnDimension(); i++)
				{
					sb.append(String.format("%nAction %d: u=%5.2f, mu=%5.2f, sig=%5.2f", i, uRaw.get(0, i),
							mean.get(0, i), stdev.get(i, i)));
				}
				log.debug(sb.toString());
			}
			
			if (training)
			{
				DataSet ds = new DataSet();
				ds.states = state.getColumnPackedCopy();
				ds.actions = u.getColumnPackedCopy();
				// int iRollout = rollouts.size() - 1;
				if ((nextStateDt <= 0) && !rollouts.get(iRollout).data.isEmpty())
				{
					rollouts.get(iRollout).data.get(rollouts.get(iRollout).data.size() - 1).nextStates = ds.states;
				}
				
				rollouts.get(iRollout).data.add(ds);
			}
			
			return u;
		}
	}
	
	
	/**
	 * Write collected data to file
	 */
	private void saveData()
	{
		if (!isSaveData())
		{
			return;
		}
		if (rollouts.isEmpty())
		{
			return;
		}
		File file = new File(dataFile);
		if (file.exists())
		{
			boolean deleted = file.delete();
			if (!deleted)
			{
				log.error("Could not delete " + file.getAbsolutePath());
			}
		}
		BufferedWriter fileWriter = null;
		int nRollouts = rollouts.size();
		try
		{
			fileWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"
					));
			
			List<String> strData = new ArrayList<>();
			
			for (Rollout d : rollouts)
			{
				
				if (d.data.isEmpty())
				{
					nRollouts--;
					continue;
				}
				if (d.data.get(d.data.size() - 1).nextStates.length == 0)
				{
					d.data.remove(d.data.size() - 1);
					if (d.data.isEmpty())
					{
						nRollouts--;
						continue;
					}
				}
				strData.add("nSamples: " + d.data.size());
				strData.addAll(d.data.stream().limit(d.data.size())
						.map(ds -> data2String(ds.states) + data2String(ds.nextStates) + data2String(ds.actions))
						.collect(Collectors.toList()));
				
				
			}
			fileWriter.write("nrollouts: " + nRollouts + "\n");
			for (String s : strData)
			{
				fileWriter.write(s);
				fileWriter.write('\n');
			}
		} catch (IOException err)
		{
			log.error("Can not write data file", err);
			return;
		} finally
		{
			if (fileWriter != null)
			{
				try
				{
					fileWriter.close();
				} catch (IOException err)
				{
					log.error("Failed to close data file writer", err);
				}
			}
		}
		
		try
		{
			Files.write(Paths.get(dataFile + ".comm"), String.valueOf(SumatraClock.currentTimeMillis()).getBytes());
		} catch (IOException err)
		{
			log.error("Could not write communication file", err);
		}
		rollouts.clear();
		rollouts.add(new Rollout());
	}
	
	
	private String data2String(final double[] data)
	{
		StringBuilder sb = new StringBuilder();
		for (double d : data)
		{
			sb.append(d);
			sb.append(' ');
		}
		return sb.toString();
	}
	
	
	/**
	 * @return the saveData
	 */
	public boolean isSaveData()
	{
		return training;
	}
	
	
	/**
	 * @return the params
	 */
	public PolicyParameters getParams()
	{
		return params;
	}
	
	
	/**
	 * @return
	 */
	public int getNumRollouts()
	{
		return rollouts.size() - 1;
	}
	
	
	@Override
	public int getStateDimension()
	{
		return params.getStateDim();
	}
	
	
	@Override
	public float getDt()
	{
		if (training)
		{
			return params.getDt();
		}
		return 0.1f;
	}
	
	
	/**
	 * @return the dataFile
	 */
	public String getDataFile()
	{
		return dataFile;
	}
	
	
	/**
	 * @return the ctrlFile
	 */
	public String getCtrlFile()
	{
		return params.getFileName();
	}
	
	
	/**
	 * @return the rnd
	 */
	public Random getRnd()
	{
		return rnd;
	}
}
