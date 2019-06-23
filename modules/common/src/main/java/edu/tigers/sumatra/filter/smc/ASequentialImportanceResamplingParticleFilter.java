/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.filter.smc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.MultivariateRealDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealVector;


/**
 * Particle Filter with Sequential Importance Resampling algorithm (SIR-PF).
 * Information taken from: "A Tutorial on Particle Filtering and Smoothing: Fifteen years later" by
 * Arnaud Doucet and Adam M. Johansen
 * 
 * @author AndreR <andre@ryll.cc>
 */
public abstract class ASequentialImportanceResamplingParticleFilter
{
	private final int						numParticles;
	private final List<RealVector>	particles					= new ArrayList<>();
	private final RealVector			weights;
	private double							effectiveParticleSize	= 0;
	private double							effectiveParticleGate	= 0.8;
	
	private RealVector					lastMean;
	
	
	protected ASequentialImportanceResamplingParticleFilter(final int numParticles)
	{
		this.numParticles = numParticles;
		weights = new ArrayRealVector(numParticles);
	}
	
	
	protected void init(final RealVector mean, final RealVector sigma)
	{
		Validate.isTrue(mean.getDimension() == sigma.getDimension());
		
		MultivariateRealDistribution dist = new MultivariateNormalDistribution(mean.toArray(),
				new DiagonalMatrix(sigma.toArray()).getData());
		
		for (int i = 0; i < numParticles; i++)
		{
			particles.add(new ArrayRealVector(dist.sample()));
		}
		
		lastMean = mean;
	}
	
	
	protected void predict()
	{
		for (int i = 0; i < particles.size(); i++)
		{
			particles.set(i, predictParticle(particles.get(i)));
		}
	}
	
	
	protected void update()
	{
		
		// get weights of all particles
		for (int i = 0; i < numParticles; i++)
		{
			weights.setEntry(i, calculateWeight(particles.get(i)));
		}
		
		// normalize them
		weights.mapMultiplyToSelf(1.0 / weights.getL1Norm());
		
		double normWeight = weights.getNorm();
		effectiveParticleSize = 1.0 / (normWeight * normWeight);
		
		if (effectiveParticleSize > (effectiveParticleGate * numParticles))
		{
			return;
		}
		
		// Calculate the cumulative density function of this discrete distribution
		RealVector cdf = new ArrayRealVector(numParticles);
		cdf.setEntry(0, weights.getEntry(0));
		for (int i = 1; i < numParticles; i++)
		{
			cdf.setEntry(i, cdf.getEntry(i - 1) + weights.getEntry(i));
		}
		
		// Use a uniform distribution to select "surviving" samples
		RealDistribution select = new UniformRealDistribution();
		
		// create offsprings of good particles and discard bad ones
		List<RealVector> newParticles = new ArrayList<>();
		for (int p = 0; p < numParticles; p++)
		{
			double prob = select.sample();
			
			for (int i = 0; i < numParticles; i++)
			{
				if (cdf.getEntry(i) > prob)
				{
					newParticles.add(particles.get(i));
					break;
				}
			}
		}
		
		particles.clear();
		particles.addAll(newParticles);
		
		RealVector mean = new ArrayRealVector(particles.get(0).getDimension());
		
		for (int p = 0; p < numParticles; p++)
		{
			mean = mean.add(particles.get(p));
		}
		
		lastMean = mean.mapMultiplyToSelf(1.0 / particles.size());
	}
	
	
	/**
	 * Get mean of all particles.
	 * 
	 * @return
	 */
	public RealVector getMean()
	{
		return lastMean;
	}
	
	
	/**
	 * This function should predict the state of a particle based on some model.
	 * It can be non-linear and also use additional/environmental information.
	 * 
	 * @param particle Input particle.
	 * @return Predicted particle state.
	 * @note This function should also put appropriate noise on the new state.
	 */
	protected abstract RealVector predictParticle(final RealVector particle);
	
	
	/**
	 * Calculate the weight of a particle depending on some arbitrary measure.
	 * Weights must be positive!
	 * 
	 * @param particle Input particle.
	 * @return Weight of this particle.
	 */
	protected abstract double calculateWeight(final RealVector particle);
	
	
	/**
	 * @return the effectiveParticleGate
	 */
	public double getEffectiveParticleGate()
	{
		return effectiveParticleGate;
	}
	
	
	/**
	 * @param effectiveParticleGate the effectiveParticleGate to set
	 */
	public void setEffectiveParticleGate(final double effectiveParticleGate)
	{
		this.effectiveParticleGate = effectiveParticleGate;
	}
	
	
	/**
	 * @return the particles
	 */
	public List<RealVector> getParticles()
	{
		return particles;
	}
	
	
	/**
	 * @return the weights
	 */
	public RealVector getWeights()
	{
		return weights;
	}
	
	
	/**
	 * @return the effectiveParticleSize
	 */
	public double getEffectiveParticleSize()
	{
		return effectiveParticleSize;
	}
	
	
	/**
	 * @return relative measure of effective particles (0.0 - 1.0)
	 */
	public double getRelativeEffectiveParticleSize()
	{
		return effectiveParticleSize / particles.size();
	}
}
