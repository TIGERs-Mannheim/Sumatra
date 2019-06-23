package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter;

import java.security.InvalidParameterException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.uncommons.maths.random.ContinuousUniformGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.IMotionModel;


public class ParticleFilter implements IFilter
{
	private final Logger				log	= Logger.getLogger(getClass());
	
	public int							id;
	protected double					time;
	
	protected PredictionContext	context;
	protected IMotionModel			motion;
	
	protected int						stepcount;
	protected double					stepsize;
	protected double					offset;
	
	protected Particle[]				particle;
	
	protected int						nParticle;
	protected double					essThreshold;
	
	enum Resampler
	{
		STRATIFIED,
		RANDOM
	}; // to be extended...maybe
	
	protected Resampler							resampler;
	protected Random								rng;
	protected ContinuousUniformGenerator	generator;
	private boolean keptAlive;	
	
	public ParticleFilter()
	{
		this.id = 0;
		this.time = 0.0;
		this.context = null;
		this.motion = null;
		this.stepcount = 0;
		this.stepsize = 0.0;
		this.offset = 0.0;
		
		this.nParticle = 0;
		this.essThreshold = 0.0;
		this.resampler = Resampler.STRATIFIED;
		
		this.rng = new MersenneTwisterRNG();
		this.generator = new ContinuousUniformGenerator(0.0, 1.0, rng);
		
		this.keptAlive = true;
	}
	

	@Override
	public void init(IMotionModel motionModel, PredictionContext context, double firstTimestamp,
			AWPCamObject firstObservation)
	{
		this.motion = motionModel;
		this.context = context;
		
		this.id = this.motion.extraxtObjectID(firstObservation);
		
		this.time = firstTimestamp;
		this.offset = 0.0;
		
		this.stepsize = this.context.stepSize;
		this.stepcount = this.context.stepCount;
		
		this.keptAlive = false;
		
		this.nParticle = context.numberParticle;
		this.particle = new Particle[nParticle];
		this.essThreshold = context.pfESS;
		
		if (context.resampler.equalsIgnoreCase("stratified"))
			resampler = Resampler.STRATIFIED;
		else if (context.resampler.equalsIgnoreCase("random"))
			resampler = Resampler.RANDOM;
		else
			resampler = Resampler.STRATIFIED;
		
		Matrix measurement = this.motion.generateMeasurementMatrix(firstObservation, null);
		Matrix control		 = this.motion.generateControlMatrix(null,null);
		for(int i=0; i<nParticle; i++) 
		{
			particle[i] = new Particle();
			particle[i].init(measurement);
			for (int j = 0; j <= this.stepcount; j++)
			{
				particle[i].state[j] = motion.sample(particle[i].state[0], particle[i].contr);
			}
			particle[i].contr = control;
		}
		
	}
	

	@Override
	public double getTimestamp()
	{
		return time;
	}
	

	@Override
	public double getLookaheadTimestamp(int index)
	{
		if (index < 0 || index > stepcount)
		{
			log.debug("Lookahead prediction with index " + index + " is out of " + "lookahead bounds" + " (min: 0; max: "
					+ stepcount + ")");
			throw new InvalidParameterException("Passed index (" + index + ") " + "is out of valid scope (0-" + stepcount
					+ ").");
		}
		
		if (index == 0)
		{
			return time;
		} else
		{
			return time + offset + stepsize * index;
		}
	}
	

	@Override
	public AMotionResult getLookahead(int index)
	{
		if (index < 0 || index > stepcount)
		{
			log.debug("Lookahead prediction with index " + index + " is out of " + "lookahead bounds" + " (min: 0; max: "
					+ stepcount + ")");
			throw new InvalidParameterException("Passed index (" + index + ") " + "is out of valid scope (0-" + stepcount
					+ ").");
		}
		
		performAutoLookahead(index);
//		Matrix res = histogramm(index);
//		System.out.println(res.get(3,0) + " " + res.get(4,0) + " " + res.get(5,0));
		
		return motion.generateMotionResult(id, histogramm(index), !keptAlive);
	}
	

	@Override
	public void observation(double timestamp, AWPCamObject observation)
	{
		this.keptAlive = false;
		
		Matrix measurement = motion.generateMeasurementMatrix(observation, null); //TODO WP: pass former state for continuous angles of bots
		
		double[] weight = new double[nParticle];
		double sumW = 0;
		double dt = timestamp - time;
		int index = stepcount;
		
		while (dt < (stepsize * index + offset) && index > 0)
		{
			index--;
		}
		Matrix old = histogramm(index);
		double vx = 0.0;
		double vy = 0.0;
		if(dt != 0.0) {
			vx = (measurement.get(0,0) - old.get(0, 0))/dt;
			vy = (measurement.get(1,0) - old.get(1, 0))/dt;
		}
		
		for (int i = 0; i < nParticle; i++)
		{
			particle[i].state[index].set(3, 0, vx);
			particle[i].state[index].set(4, 0, vy);
			Matrix stateNew = predictStateAtTime(i, timestamp);
			stateNew = motion.sample(stateNew, particle[i].contr);
			particle[i].state[0] = stateNew.copy();
			
			double prob = motion.measurementProbability(stateNew, measurement, dt);
			weight[i] = prob;//particle[i].weight * prob;
			sumW += weight[i];
		}
		
		deleteLookaheads();
		
		for (int i = 0; i < nParticle; i++)
		{
			weight[i] /= sumW;
			particle[i].weight = weight[i];
		}
		if (getESS() <= this.essThreshold)
		{
			resample();
		}
		
		this.time = timestamp;
		this.offset = 0.0;
	}
	

	@Override
	public void updateOffset(double timestamp)
	{
		// convert timestamp in ns to wp intern time-unit
		offset = timestamp - time;
		
		deleteLookaheads();
		
	}
	

	@Override
	public void performLookahead(int index)
	{
		if (index < 1 || index > stepcount)
		{
			log.debug("Lookahead prediction with index " + index + " is out of " + "lookahead bounds" + " (min: 1; max: "
					+ stepcount + "). " + "Therefore no lookahead was performed.");
			throw new InvalidParameterException("Passed index (" + index + ") " + "is out of valid scope (1-" + stepcount
					+ ").");
		}
		if (particle[0].state[index - 1] == null)
		{
			log.debug("Lookahead prediction with index " + index + " could not "
					+ "be performed because there is no lookahead lookahead " + "prediction with index " + (index - 1)
					+ " as basis.");
			throw new IllegalArgumentException("No basis for lookahead " + "prediction with index " + index + ".");
		}
		
		double dt = stepsize;
		if (index == 1)
		{
			dt = stepsize + offset;
		}
		
		for (int i = 0; i < nParticle; i++)
		{
			particle[i].state[index] = motion.dynamics(particle[i].state[index - 1], particle[i].contr, dt);
			particle[i].state[index] = motion.sample(particle[i].state[index], particle[i].contr);
		}
		
	}
	

	@Override
	public void handleCollision(int index, IControl effect)
	{
		//TODO WP: implement collision handling!		
	}
	

	@Override
	public void setControl(IControl control)
	{
		for (int i = 0; i < nParticle; i++)
		{
			particle[i].contr = motion.generateControlMatrix(control, particle[i].state[0]);
		}
		
		deleteLookaheads();
	}
	

	@Override
	public int getId()
	{
		return id;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	protected Matrix histogramm(int index)
	{
		Matrix sumState = particle[0].state[index].times(particle[0].weight);
		for (int i = 1; i < nParticle; i++)
		{
			sumState = sumState.plus(particle[i].state[index].times(particle[i].weight));
		}
		return sumState;
	}

	protected double getESS()
	{
		double sumsq = 0;
		
		for (int i = 0; i < nParticle; i++)
		{
			sumsq += Math.pow(particle[i].weight, 2);
		}
		return (1.0 / sumsq) / nParticle;
	}
	
	protected void resample()
	{
		int[] rsCount = new int[nParticle], rsIndices = new int[nParticle];
		int j = 1;
		
		switch (resampler)
		{
			case RANDOM:
				j = 0;
				while (j < nParticle)
				{
					for (int i = 0; i < nParticle && j < nParticle; i++)
					{
						if (particle[i].weight > generator.nextValue())
						{
							rsIndices[j] = i;
							j++;
						}
					}
				}
				break;
			
			case STRATIFIED:
			default:
				int k = 0;
				double weightCumulative = particle[0].weight;
				
				for (int i = 0; i < nParticle; i++)
					rsCount[i] = 0;
				int cntPartikel = 0;
				while (j < nParticle)
				{
					while (weightCumulative > (((double) j) / nParticle) && j < nParticle)
					{
						rsCount[k]++;
						cntPartikel++;
						j++;
					}
					k++;
					if (k == nParticle || j == nParticle)
					{
						rsCount[k - 1] = nParticle - cntPartikel + 1;
						break;
					}
					weightCumulative += particle[k].weight;
				}
				
				j = 0;
				
				for (int i = 0; i < nParticle; ++i)
				{
					if (rsCount[i] > 0)
					{
						rsIndices[i] = i;
						
						while (rsCount[i] > 1)
						{
							while (rsCount[j] > 0)
								++j; // find next free spot
							rsIndices[j++] = i; // assign index
							--rsCount[i]; // decrement number of remaining offsprings
						}
						
					}
				}
				break;
			
		} // resampler switch-case
		
		Particle[] tmpParticle = new Particle[nParticle];
		tmpParticle = particle.clone();
		
		for (int i = 0; i < nParticle; ++i)
		{
			if (rsIndices[i] != i)
			{
				particle[i].state[0] = tmpParticle[rsIndices[i]].state[0].copy();
				particle[i].contr = null;
				if (tmpParticle[rsIndices[i]].contr != null)
				{
					particle[i].contr = tmpParticle[rsIndices[i]].contr.copy();
				}
			}
			particle[i].weight = (1.0 / (double) nParticle);
		}
	}
	
	protected Matrix predictStateAtTime(int index, double targetTime)
	{
		double dt = targetTime - time;
		
		int basisState = stepcount;
		while (dt < (stepsize * basisState + offset) && basisState > 0)
		{
			basisState--;
		}
		
		if (basisState > 0)
		{
			performAutoLookahead(basisState);
			dt = dt - (stepsize * basisState + offset);
		}
		return motion.dynamics(particle[index].state[basisState], particle[index].contr, dt);
	}
	
	private void performAutoLookahead(int index)
	{
		int i = index;
		while (particle[0].state[i] == null)
		{
			i--;
		}
		i++;
		while (i <= index)
		{
			performLookahead(i);
			i++;
		}
	}
	
	private void deleteLookaheads()
	{
		for (int j = 0; j < nParticle; j++)
		{
			particle[j].contr = null;
			for (int i = 1; i <= stepcount; i++)
			{
				particle[j].state[i] = null;
			}
		}
	}
	
	private class Particle
	{
		public Matrix	contr;	// control vector
		public Matrix[]	state;	// state vector
											
		public double		weight;
		
		
		public void init(Matrix measurement)
		{
			this.weight = 1.0 / nParticle;
			this.state = new Matrix[stepcount + 1];
			
			this.contr = motion.generateControlMatrix(null, null);
			this.state[0] = motion.generateStateMatrix(measurement, this.contr);
		}
		

		public String toString()
		{
			return new String(state[0].get(0, 0) + " " + state[0].get(1, 0) + " " + weight);
		}
	}

	@Override
	public void keepPositionAliveOnNoObservation()
	{
		this.keptAlive = true;
		
		// TODO Auto-generated method stub
		// Implement what happens when we got no observation... 
		// The best would be to keep the last most probable state.
		
	}

	@Override
	public boolean positionKeptAlive()
	{
		return keptAlive;
	}
}
