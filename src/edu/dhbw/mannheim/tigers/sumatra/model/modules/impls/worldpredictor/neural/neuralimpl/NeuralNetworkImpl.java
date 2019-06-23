/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.05.2014
 * Author(s): JanE, KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl;

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

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.ACamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.NeuralWP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralStaticConfiguration.BallConfigs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl.NeuralStaticConfiguration.NeuralWPConfigs;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This is the first implementation of a feed-forward Neural network using the
 * "Encog" framework. The training method is back-propagation.
 * WP_TODO:'s NeuralNetwork:
 * ->What should happen with data that has a negative time diff ?
 * ->How to avoid the NaN in the intersection area
 * ->How to avoid the actual high getting over 1.5e7
 * 
 * @author JanE, KaiE
 */
public class NeuralNetworkImpl
{
	/**
	  * 
	  */
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// ---------------------------------------------------------------------------
	/*
	 * These two magic numbers are used to increase the range of the normaliser
	 * to a nice range. If the value is bigger than the current maximum then the
	 * input-value for the network would be actual-high from the function which
	 * is not wanted. Therefore the actual data are multiplied by this value to
	 * allow using the range that is best.
	 */
	private static final double			CONVERT_TO_NICE_RANGE		= 1.5;
	private static final double			CONVERT_FROM_NICE_RANGE		= 2.0 / 3.0;
	// ---------------------------------------------------------------------------
	
	@Configurable
	private static int						numberofTrainingIterations	= 1;
	@Configurable
	private static long						lookaheadMS						= 50;
	private static final Logger			log								= Logger.getLogger(NeuralNetworkImpl.class
																								.getName());
	private BasicNetwork						network							= new BasicNetwork();
	private int									referencedID;
	private final int							numberOutputNeurons;
	private final int							numberInputNeurons;
	private double[]							inputArray						= null;
	private double[]							outputArray						= null;
	private double[]							analysisBarrier				= null;
	private final IACamObjectConverter	dataConverter;
	private final Deque<ACamObject>		networkData;
	private final Deque<Long>				timestamps;
	private NormalizedField[]				normalizer;
	private static final double[]			emptydummy						= new double[0];
	private boolean							dataInputInterrupted			= false;
	
	
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
		timestamps = new ArrayDeque<Long>(NeuralWPConfigs.LastNFrames + 1);
		
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
	 * @param captureTime
	 */
	public void updateRecurrence(final ACamObject obj, final long captureTime)
	{
		Hashtable<Long, ACamObject> mp = new Hashtable<>(timestamps.size() + 1);
		
		Long minimum = Long.MAX_VALUE - 1;
		while (!timestamps.isEmpty())
		{
			Long tmp = timestamps.pollFirst();
			mp.put(tmp, networkData.pollFirst());
			minimum = tmp < minimum ? tmp : minimum;
		}
		
		mp.put(captureTime, obj);
		
		if (mp.size() > (NeuralWPConfigs.LastNFrames + 1))
		{
			mp.remove(minimum);
		}
		SortedSet<Long> keys = new TreeSet<Long>(mp.keySet());
		// System.out.println(keys);
		for (Long itm : keys)
		{
			timestamps.add(itm);
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
			timestamps.clear();
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
		final long lastItemTime = timestamps.pollLast(); // ideal Output
		final ACamObject lastItemObj = networkData.pollLast(); // ideal Output
		double timediff = ((lastItemTime - timestamps.peekLast()) * 1e-6);
		
		
		double[] data = dataConverter.convertInput(networkData, timestamps, timediff);
		double[] idealOutput = dataConverter.createIdealOutput(networkData, lastItemObj, timediff);
		networkData.add(lastItemObj); // re-add the reference data
		timestamps.add(lastItemTime); // re-add the reference data
		
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
		timestamps.pollFirst(); // remove the oldest data
		
		
		inputArray = dataConverter.convertInput(networkData, timestamps, lookaheadMS);
		
		MLData in = new BasicMLData(normalise(inputArray));
		MLData out = network.compute(new BasicMLData(in));
		outputArray = denormalise(out.getData());
		final double[] retArray = dataConverter.convertOutput(outputArray, networkData.peekLast());
		// System.out.println(Arrays.toString(normalizer));
		// System.out.println(Arrays.toString(outputArray));
		// System.out.println(Arrays.toString(inputArray));
		// System.out.println(Arrays.toString(normalise(inputArray)));
		// System.out.println("--------------------------------------");
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
			if (passesBarrierTest(CONVERT_TO_NICE_RANGE * data[i], i))
			{
				normalizer[i % numberOutputNeurons].analyze(CONVERT_TO_NICE_RANGE * data[i]);
			}
		}
	}
	
	
	/**
	 * This Method is used as a barrier to avoid normaliser-destruction when an input value is
	 * too great. Therefore each normaliser has a barrier-field that contains a calculated value
	 * that evens the input. The formula for the calculation of the value is:
	 * 
	 * <pre>
	 * n   := barrier-value-now;
	 * n+1 := barrier-value-new;
	 * data:= current incoming value;
	 * 
	 *       n + data
	 * n+1 = --------
	 *          2
	 * </pre>
	 * 
	 * an example with the assumption that CONVERT_TO_NICE_RANGE is 1.5
	 * 
	 * <pre>
	 * <table>
	 * <tr><th>current barrier value</th><th>incoming data</th><th>actual data</th><th>new barrier value</th><th>passes test</th></tr>
	 * <tr><td>0                    </td><td>50           </td><td>33.333     </td><td>25               </td><td>false      </td></tr>
	 * <tr><td>25						  </td><td>52           </td><td>34,666	  </td><td>38.5				 </td><td>true			</td></tr>
	 * <tr><td>38.5                 </td><td>1500         </td><td>1000		  </td><td>769.25				 </td><td>false		</td></tr>
	 * <tr><td>769.25					  </td><td>75           </td><td>50			  </td><td>422,125			 </td><td>true			</td></tr>
	 * </table>
	 * </pre>
	 * 
	 * @param data current incoming data that should be checked
	 * @param index index which barrier should be used
	 * @return true if the value might be valid to analyse.
	 */
	private boolean passesBarrierTest(final double data, final int index)
	{
		
		double last = analysisBarrier[index % numberOutputNeurons];
		if ((index % numberOutputNeurons) == 0) // this is supposed to be the time
		{
			if (data < 0)
			{
				return false; // and as this is the time it should be always greater 0
			}
		}
		
		analysisBarrier[index % numberOutputNeurons] = (last + data) / 2; // barrier-function
		boolean ret = (Math.abs(analysisBarrier[index % numberOutputNeurons]) > Math.abs(data * CONVERT_FROM_NICE_RANGE));
		if ((data > 1000.0) && ret)
		{
			// System.out.println(ret + "--------------------------------------------------");
		}
		return ret;
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
