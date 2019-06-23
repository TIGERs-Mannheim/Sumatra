/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.05.2014
 * Author(s): JanE, KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.wp.neural;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Hashtable;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.IllegalClassException;
import org.apache.log4j.Logger;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;
import org.encog.util.obj.SerializeObject;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.ACamObject;
import edu.tigers.sumatra.wp.neural.NeuralStaticConfiguration.BallConfigs;
import edu.tigers.sumatra.wp.neural.NeuralStaticConfiguration.NeuralWPConfigs;


/**
 * This is the first implementation of a feed-forward Neural network using the
 * "Encog" framework. The training method is back-propagation.
 * WP_TODO:'s NeuralNetwork:
 * 
 * @author JanE, KaiE
 */
public class NeuralNetworkImpl
{
	
	// ---------------------------------------------------------------------------
	/*
	 * These two magic numbers are used to increase the range of the normaliser
	 * to a nice range. If the value is bigger than the current maximum then the
	 * input-value for the network would be actual-high from the function which
	 * is not wanted. Therefore the actual data are multiplied by this value to
	 * allow using the range that is best.
	 */
	private static final double			CONVERT_TO_NICE_RANGE		= 5;
	// private static final double CONVERT_FROM_NICE_RANGE = 1.0 / CONVERT_TO_NICE_RANGE;
	// ---------------------------------------------------------------------------
	
	@Configurable
	private static int						numberofTrainingIterations	= 1;
	@Configurable
	private static long						lookaheadMS						= 50;
	private static final Logger			log								= Logger.getLogger(NeuralNetworkImpl.class
																								.getName());
	private BasicNetwork						network							= new BasicNetwork();
	private final int							referencedID;
	private final int							numberOutputNeurons;
	private final int							numberInputNeurons;
	private double[]							inputArray						= null;
	private double[]							outputArray						= null;
	private double[]							analysisBarrier				= null;
	private final IACamObjectConverter	dataConverter;
	private final Deque<ACamObject>		networkData;
	private NormalizedField[]				normalizer;
	private static final double[]			emptydummy						= new double[0];
	private boolean							dataInputInterrupted			= false;
																						
																						
	static
	{
		ConfigRegistration.registerClass("wp", NeuralNetworkImpl.class);
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This will create a neural network with 6 input
	 * neurons, 1 hidden layer with 10 neurons and 6 output neurons
	 * and a {@link CamRobotConverter}
	 *
	 * @param referencedID
	 */
	public NeuralNetworkImpl(final int referencedID)
	{
		this(6, 1, 10, 6, referencedID, new CamRobotConverter());
	}
	
	
	/**
	 * this will create a new neural network with number of inputNeurons;
	 * number of hidden layers consisting of several hiddenNeurons
	 * and having an output-Layer with outputNeurons output Neurons
	 * 
	 * @param inputNeurons
	 * @param noOfHiddenlayer
	 * @param hiddenNeurons
	 * @param outputNeurons
	 * @param referencedID
	 * @param converter
	 */
	public NeuralNetworkImpl(final int inputNeurons, final int noOfHiddenlayer, final int hiddenNeurons,
			final int outputNeurons,
			final int referencedID, final IACamObjectConverter converter)
	{
		dataConverter = converter;
		numberInputNeurons = inputNeurons;
		numberOutputNeurons = outputNeurons;
		inputArray = new double[numberInputNeurons];
		outputArray = new double[numberOutputNeurons];
		
		networkData = new ArrayDeque<ACamObject>(NeuralWPConfigs.LastNFrames + 1);
		
		// Initialise Network
		network.addLayer(new BasicLayer(null, true, inputNeurons));
		for (int i = 0; i < noOfHiddenlayer; ++i)
		{
			network.addLayer(new BasicLayer(ActivationFunctionFactory.create(), true, hiddenNeurons));
		}
		
		network.addLayer(new BasicLayer(ActivationFunctionFactory.create(), false, outputNeurons));
		network.getStructure().finalizeStructure();
		
		network.reset();
		this.referencedID = referencedID;
		
		normalizer = new NormalizedField[numberOutputNeurons];
		for (int i = 0; i < normalizer.length; ++i)
		{
			normalizer[i] = new NormalizedField(NormalizationAction.Normalize, "(" + referencedID + ")->" + i,
					1, -1, ActivationFunctionFactory.getNormalizedHigh(),
					ActivationFunctionFactory.getNormalizedLow());
			normalizer[i].init();
		}
		
		analysisBarrier = new double[numberOutputNeurons];
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * update the data
	 * 
	 * @param obj
	 */
	public void updateRecurrence(final ACamObject obj)
	{
		Hashtable<Long, ACamObject> mp = new Hashtable<Long, ACamObject>(networkData.size() + 1);
		
		Long minimum = Long.MAX_VALUE - 1;
		while (!networkData.isEmpty())
		{
			ACamObject tmp = networkData.pollFirst();
			mp.put(tmp.getTimestamp(), tmp);
			minimum = tmp.getTimestamp() < minimum ? tmp.getTimestamp() : minimum;
		}
		
		mp.put(obj.getTimestamp(), obj);
		
		if (mp.size() > (NeuralWPConfigs.LastNFrames + 1))
		{
			mp.remove(minimum);
		}
		SortedSet<Long> keys = new TreeSet<Long>(mp.keySet());
		for (Long itm : keys)
		{
			networkData.add(mp.get(itm));
		}
		
	}
	
	
	/**
	 * Resets the recurrence of the network and waits for restart
	 */
	public void interruptRecurrence()
	{
		if (!dataInputInterrupted)
		{
			System.out.println("neural network interrupted");
			networkData.clear();
			dataInputInterrupted = true;
		}
	}
	
	
	/**
	 * This method uses the back-propagation algorithm for conditioning the
	 * network.
	 */
	public void train()
	{
		if (networkData.size() <= NeuralWPConfigs.LastNFrames)
		{
			return;
		}
		final ACamObject lastItemObj = networkData.pollLast(); // ideal Output
		double timediff = ((lastItemObj.getTimestamp() - networkData.peekLast().getTimestamp()) * 1e-6);
		
		
		double[] data = dataConverter.convertInput(networkData, timediff);
		double[] idealOutput = dataConverter.createIdealOutput(networkData, lastItemObj, timediff);
		networkData.add(lastItemObj); // re-add the reference data
		
		if (data.length != numberInputNeurons)
		{
			log.fatal("required an IDataConverter that returns an array of length " + numberInputNeurons
					+ " not " + data.length);
			throw new IllegalClassException("required an IDataConverter that returns an array of length "
					+ numberOutputNeurons + " not " + data.length);
		}
		
		analyzeData(data);
		analyzeData(idealOutput);
		
		
		double[][] castIdealData = new double[][] {
				normalise(idealOutput)
		};
		double[][] castInputData = new double[][] {
				normalise(data)
		};
		NeuralDataSet trainSet = new BasicNeuralDataSet(castInputData, castIdealData);
		Train train = new Backpropagation(network, trainSet);
		train.iteration(numberofTrainingIterations);
	}
	
	
	/**
	 * Generates a prediction output for the next frame. Therefore the last n frames are used to create the
	 * history of the last positions. The prediction is as an array of values that can be converted by an
	 * {@link IACamObjectConverter}
	 * 
	 * @return an array with the values of the output-layer
	 */
	public double[] generateOutput()
	{
		if (networkData.size() <= NeuralWPConfigs.LastNFrames)
		{
			return emptydummy;
		}
		networkData.pollFirst(); // remove the oldest data
		
		
		inputArray = dataConverter.convertInput(networkData, lookaheadMS);
		
		MLData in = new BasicMLData(normalise(inputArray));
		MLData out = network.compute(new BasicMLData(in));
		outputArray = denormalise(out.getData());
		final double[] retArray = dataConverter.convertOutput(outputArray, networkData.peekLast());
		if (referencedID == -1)
		{
			// System.out.println(Arrays.toString(normalizer));
			// System.out.println(Arrays.toString(outputArray));
			// System.out.println(Arrays.toString(inputArray));
			// System.out.println(Arrays.toString(normalise(inputArray)));
			// System.out.println("--------------------------------------");
		}
		dataInputInterrupted = false;
		return retArray;
		
	}
	
	
	/**
	 * creates a file with the weight informations of this network
	 * saved in Sumatra/data/neuralsave/(filenameStub)(id).eg
	 * and the normaliser as *.egn
	 * 
	 * @param filenameStub
	 */
	public void saveNeuralConfig(final String filenameStub)
	{
		String filePath = NeuralWP.baseDirPathToFiles;
		boolean created = new File(filePath).mkdirs();
		if (created)
		{
			log.info("created directories :" + NeuralWP.baseDirPathToFiles);
		}
		String saveFilename = filePath + filenameStub + referencedID + ".eg";
		log.info("Save neural network to file: " + saveFilename);
		try
		{
			SerializeObject.save(new File(saveFilename), network);
			SerializeObject.save(new File(saveFilename.replaceAll(".eg", ".egn")), normalizer);
		} catch (IOException err)
		{
			log.warn("Could not save network for " + saveFilename, err);
		}
	}
	
	
	/**
	 * This method allows to load a neuralConfig for a defined reference id.
	 * Therefore not only the file network has to exist (*.eg) but also its corresponding normaliser (*egn)
	 * 
	 * @param pathToFile the complete path and the filename of the network.
	 */
	public void loadNeuralConfig(final String pathToFile)
	{
		try
		{
			network = (BasicNetwork) SerializeObject.load(new File(pathToFile));
			normalizer = (NormalizedField[]) SerializeObject.load(new File(pathToFile.replaceAll(".eg", ".egn")));
		} catch (ClassNotFoundException | IOException err)
		{
			log.warn("Could not load network: " + pathToFile);
		}
	}
	
	
	/**
	 * Getter to acquire the set reference ID. The Ball has the ID set in the static
	 * configuration {@link BallConfigs}
	 * 
	 * @return the referencedID
	 */
	public int getReferencedID()
	{
		return referencedID;
	}
	
	
	/**
	 * normalises the data by using the normaliser array. The values are normalised
	 * to the values so that the neural network can handle them.
	 * 
	 * @param data that should be converted
	 * @return a new array object that contains the converted data
	 */
	private double[] normalise(final double[] data)
	{
		double[] ret = new double[data.length];
		for (int i = 0; i < data.length; ++i)
		{
			ret[i] = normalizer[i % numberOutputNeurons].normalize(data[i]);
		}
		
		return ret;
	}
	
	
	/**
	 * analyzes the data and changes the normaliser-array accordingly
	 * 
	 * @param data
	 */
	private void analyzeData(final double[] data)
	{
		for (int i = 0; i < data.length; ++i)
		{
			double val = passesBarrierTest(CONVERT_TO_NICE_RANGE * data[i], i);
			normalizer[i % numberOutputNeurons].analyze(val);
			
			if (referencedID == -1)
			{
				if (i == 4)
				{
					max = Math.max(max, val);
					maxd = Math.max(maxd, data[i]);
					// System.out.println(max + " " + maxd);
					
				}
				if (data[i] > 100)
				{
					// System.out.println("now :" + data[i] + " :> " + i);
				}
			}
		}
	}
	
	private double	max	= Double.MIN_VALUE;
	private double	maxd	= Double.MIN_VALUE;
								
								
	/**
	 * This Method is used as a barrier to avoid normaliser-destruction when an input value is
	 * too great. Therefore each normaliser has a barrier-field that contains a calculated value
	 * that evens the input. The formula for the calculation of the value is:
	 * 
	 * <pre>
	 * F(N)   := new barrier-value;
	 * F(N-1) := last barrier-value;
	 * D(N)   := current incoming value;
	 * 
	 *        F(N-1) + 2*|D(N)|*F(N-1) + |D(N)|
	 * F(N) = ---------------------------------
	 *                 2*(|D(N)| +1)
	 * </pre>
	 * 
	 * @param data current incoming data that should be checked
	 * @param index index which barrier should be used
	 * @return barrier value if the value might be valid to analyse.
	 */
	private double passesBarrierTest(final double data, final int index)
	{
		
		double f = analysisBarrier[index % numberOutputNeurons];
		
		double d = Math.abs(data);
		
		analysisBarrier[index % numberOutputNeurons] = (f + (2 * f * d) + d) / (2.0 * (d + 1));
		// boolean ret = ((analysisBarrier[index % numberOutputNeurons]) > (d * CONVERT_FROM_NICE_RANGE));
		return analysisBarrier[index % numberOutputNeurons] * Math.signum(data);
	}
	
	
	/**
	 * unnormalises the given data via the normaliser-Array
	 * 
	 * @param data the data that should be unnormalised
	 * @return a new array object that contains the unnormalised data
	 */
	private double[] denormalise(final double[] data)
	{
		double[] ret = new double[data.length];
		for (int i = 0; i < data.length; ++i)
		{
			ret[i] = normalizer[i % numberOutputNeurons].deNormalize(data[i]);
		}
		
		return ret;
	}
	
}
