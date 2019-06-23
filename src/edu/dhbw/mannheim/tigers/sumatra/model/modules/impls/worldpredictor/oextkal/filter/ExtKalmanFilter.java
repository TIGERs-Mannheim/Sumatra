package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter;

import java.security.InvalidParameterException;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.IMotionModel;


/**
 */
public class ExtKalmanFilter implements IFilter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log							= Logger.getLogger(ExtKalmanFilter.class.getName());
	
	/** id of object */
	public int							id;
	
	// time of current state
	private double						time;
	
	// predictor context providing access to global variables
	private PredictionContext		context;
	// motion model handling the processed object
	private IMotionModel				motion;
	
	// number of lookahead predictions
	private int							stepcount;
	// period of time to next lookahead prediction
	private double						stepsize;
	// increment of first lookahead prediction time
	private double						offset;
	
	// arrays for holding fiter data
	// index 0 provides data for the current state
	// other indices provide data for predicted states at 'stepsize*index+predictionGap'
	// state vector
	private Matrix[]					state;
	// covariance matrix
	private Matrix[]					covar;
	
	// control vector
	private Matrix						contr;
	
	private static final boolean	IMPROBABILITY_FILTERING	= false;
	private static final double	IMPROBABILITY_TRESHOLD	= 0.1;
	
	private boolean					keptAlive;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Creates a new KalmanFilter.
	 * 
	 * state_n number of different states in the state vector
	 * meas_n number of different states in the measurement vector
	 */
	public ExtKalmanFilter()
	{
		id = 0;
		time = 0.0;
		context = null;
		motion = null;
		stepcount = 0;
		stepsize = 0.0;
		offset = 0.0;
		contr = null;
		state = null;
		covar = null;
		keptAlive = true;
	}
	
	
	@Override
	public void init(IMotionModel motionModel, PredictionContext context, double firstTimestamp,
			AWPCamObject firstObservation)
	{
		motion = motionModel;
		this.context = context;
		
		id = motion.extraxtObjectID(firstObservation);
		
		time = firstTimestamp;
		offset = 0.0;
		
		stepsize = this.context.stepSize;
		stepcount = this.context.stepCount;
		
		keptAlive = false;
		
		// initialize data arrays for 1 current state and 'stepcount' lookahead predictions
		state = new Matrix[stepcount + 1];
		covar = new Matrix[stepcount + 1];
		
		final Matrix measurement = motion.generateMeasurementMatrix(firstObservation, null);
		contr = motion.generateControlMatrix(null, null);
		state[0] = motion.generateStateMatrix(measurement, contr);
		covar[0] = motion.generateCovarianceMatrix(state[0]);
		
		// no lookahead prediction necessary due to no movement after first observation
		for (int i = 1; i <= stepcount; i++)
		{
			state[i] = state[0].copy();
			covar[i] = covar[0].copy();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- interface methods ----------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public double getTimestamp()
	{
		return time;
	}
	
	
	@Override
	public double getLookaheadTimestamp(int index)
	{
		if ((index < 0) || (index > stepcount))
		{
			log.debug("Lookahead prediction with index " + index + " is out of " + "lookahead bounds" + " (min: 0; max: "
					+ stepcount + ")");
			throw new InvalidParameterException("Passed index (" + index + ") " + "is out of valid scope (0-" + stepcount
					+ ").");
		}
		
		if (index == 0)
		{
			return time;
		}
		return time + offset + (stepsize * index);
	}
	
	
	@Override
	public AMotionResult getLookahead(int index)
	{
		if ((index < 0) || (index > stepcount))
		{
			log.debug("Lookahead prediction with index " + index + " is out of " + "lookahead bounds" + " (min: 0; max: "
					+ stepcount + ")");
			throw new InvalidParameterException("Passed index (" + index + ") " + "is out of valid scope (0-" + stepcount
					+ ").");
		}
		
		performAutoLookahead(index);
		
		return motion.generateMotionResult(id, state[index], !keptAlive);
	}
	
	
	@Override
	public void observation(double timestamp, AWPCamObject observation)
	{
		// observation vector
		final Matrix o = motion.generateMeasurementMatrix(observation, state[0]);
		
		// if the observation is improbable (false positive), drop it
		if (IMPROBABILITY_FILTERING)
		{
			if (obsLikelihood(timestamp, o) <= IMPROBABILITY_TRESHOLD)
			{
				log.debug("False positive detected! Observation is ignored. " + "(ID: " + id + ", t: " + timestamp + ")");
				updateOffset(timestamp);
				return;
			}
		}
		
		keptAlive = false;
		
		// do Update
		update(timestamp, o);
	}
	
	
	@Override
	public void updateOffset(double timestamp)
	{
		offset = timestamp - time;
		deleteLookaheads();
	}
	
	
	@Override
	public void performLookahead(int index)
	{
		if ((index < 1) || (index > stepcount))
		{
			log.debug("Lookahead prediction with index " + index + " is out of " + "lookahead bounds" + " (min: 1; max: "
					+ stepcount + "). " + "Therefore no lookahead was performed.");
			throw new InvalidParameterException("Passed index (" + index + ") " + "is out of valid scope (1-" + stepcount
					+ ").");
		}
		if ((state[index - 1] == null) || (covar[index - 1] == null))
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
		state[index] = predictState(state[index - 1], contr, dt);
		covar[index] = predictCovariance(state[index - 1], covar[index - 1], dt);
	}
	
	
	@Override
	public void handleCollision(int index, IControl effect)
	{
		// TODO WP: implement collision handling
		// delegate work to motion model?
		// reset filter values
	}
	
	
	@Override
	public void setControl(IControl control)
	{
		state[0] = motion.updateStateOnNewControl(control, state[0]);
		covar[0] = motion.updateCovarianceOnNewControl(control, covar[0]);
		contr = motion.generateControlMatrix(control, state[0]);
		// deleteLookaheads();
	}
	
	
	@Override
	public int getId()
	{
		return id;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * Predict state after time-increment of 'dt'.
	 * 
	 * @param state current state (basis for prediction)
	 * @param control current control (basis for prediction)
	 * @param dt period of time to target time
	 * @return vector of the predicted state
	 */
	protected Matrix predictState(Matrix state, Matrix control, double dt)
	{
		// just leave the calculation of the dynamics to the motion model
		return motion.dynamics(state, control, dt);
	}
	
	
	/**
	 * 
	 * Predict covariance after time-increment of 'dt'.
	 * 
	 * @param state current state (basis for prediction)
	 * @param covariance current covariance (basis for prediction)
	 * @param dt period of time to target time
	 * @return matrix of the predicted covariance
	 */
	protected Matrix predictCovariance(Matrix state, Matrix covariance, double dt)
	{
		// handle the object specific matrices by the motion model
		final Matrix a = motion.getDynamicsJacobianWRTstate(state, dt);
		final Matrix w = motion.getDynamicsJacobianWRTnoise(state, dt);
		final Matrix q = motion.getDynamicsCovariance(state, dt);
		
		// calculate the covariance as specified in the prediction step of the EKF
		final Matrix part1 = a.times(covariance).times(a.transpose());
		final Matrix part2 = w.times(q).times(w.transpose());
		return part1.plus(part2);
	}
	
	
	/**
	 * 
	 * Predict the objects state at the specified time.
	 * 
	 * @param targetTime time on which the state is predicted
	 * @return vector of the predicted state
	 */
	protected Matrix predictStateAtTime(double targetTime)
	{
		double dt = targetTime - time;
		
		// get lookahead prediction smaller than targetTime with minimum offset
		int basisState = stepcount;
		while ((dt < ((stepsize * basisState) + offset)) && (basisState > 0))
		{
			basisState--;
		}
		
		if (basisState > 0)
		{
			performAutoLookahead(basisState);
			dt = dt - ((stepsize * basisState) + offset);
		}
		
		// use determined state to predict state at targetTime
		return predictState(state[basisState], contr, dt);
	}
	
	
	/**
	 * 
	 * Predict the objects covariance at the specified time.
	 * 
	 * @param targetTime time on which the covariance is predicted
	 * @return covariance matrix at the predicted time
	 */
	protected Matrix predictCovarianceAtTime(double targetTime)
	{
		double dt = targetTime - time;
		
		// get lookahead prediction smaller than targetTime with minimum offset
		int basisState = stepcount;
		while ((dt < ((stepsize * basisState) + offset)) && (basisState > 0))
		{
			basisState--;
		}
		
		if (basisState > 0)
		{
			performAutoLookahead(basisState);
			dt = dt - ((stepsize * basisState) + offset);
		}
		
		// use determined state to predict covariance at targetTime
		return predictCovariance(state[basisState], covar[basisState], dt);
	}
	
	
	/**
	 * 
	 * Update the filter with a measurement.
	 * 
	 * @param time time of the measurement in wp intern time-unit
	 * @param measurement measurement vector
	 */
	protected void update(double time, final Matrix measurement)
	{
		// do prediction for the time of the measurement
		final Matrix predState = predictStateAtTime(time);
		final Matrix predCov = predictCovarianceAtTime(time);
		
		// just call abstract methods because the specification of these matrices depends on the motion model
		final Matrix h = motion.getMeasurementJacobianWRTstate(measurement);
		final Matrix v = motion.getMeasurementJacobianWRTnoise(measurement);
		final Matrix r = motion.getMeasurementCovariance(measurement);
		
		// calculate kalman gain
		final Matrix a = predCov.times(h.transpose());
		final Matrix b = h.times(predCov).times(h.transpose());
		final Matrix c = v.times(r).times(v.transpose());
		final Matrix k = a.times((b.plus(c)).inverse());
		
		// correct state
		state[0] = predState.plus(k.times(measurement.minus(motion.measurementDynamics(predState))));
		// correct covariance
		final int dim = predCov.getRowDimension();
		covar[0] = (Matrix.identity(dim, dim).minus(k.times(h))).times(predCov);
		
		this.time = time;
		offset = 0.0;
		
		deleteLookaheads();
	}
	
	
	/**
	 * 
	 * Calculate the likelihood of observing a specific state of the object.
	 * 
	 * @param t period of time, the state lies ahead
	 * @param z state whose likelihood should be determined
	 * @return likelihood of observing the passed state
	 */
	protected double obsLikelihood(double t, Matrix z)
	{
		double dt = t - time;
		
		// get lookahead prediction smaller than targetTime with minimum offset
		int basisState = stepcount;
		while ((dt < ((stepsize * basisState) + offset)) && (basisState > 0))
		{
			basisState--;
		}
		
		if (basisState > 0)
		{
			performAutoLookahead(basisState);
			dt = dt - ((stepsize * basisState) + offset);
		}
		
		// project state to time of possible measurement
		final Matrix predState = predictState(state[basisState], contr, dt);
		final Matrix predCovar = predictCovariance(state[basisState], covar[basisState], dt);
		
		// TODO WP: this doues not work as expected. howto fix this?
		double likelihood = 1.0;
		for (int i = 0; i < z.getRowDimension(); i++)
		{
			// 1.0 / Math.sqrt(2*AIMath.PI*pred_covar.get(i, i));
			final double fac1 = 1.0;
			final double fac2 = -0.5 * Math.pow((z.get(i, 0) - predState.get(i, 0)) / Math.sqrt(predCovar.get(i, i)), 2);
			final double prob = fac1 * Math.exp(fac2);
			likelihood *= prob;
		}
		
		return likelihood;
	}
	
	
	private void performAutoLookahead(int index)
	{
		int i = index;
		while (state[i] == null)
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
		for (int i = 1; i <= stepcount; i++)
		{
			state[i] = null;
			covar[i] = null;
		}
	}
	
	
	@Override
	public void keepPositionAliveOnNoObservation()
	{
		keptAlive = true;
		state[0] = motion.getStateOnNoObservation(state[stepcount]);
		covar[0] = motion.getCovarianceOnNoObservation(covar[stepcount]);
		contr = motion.getControlOnNoObservation(contr);
	}
	
	
	@Override
	public boolean positionKeptAlive()
	{
		return keptAlive;
	}
	
}
