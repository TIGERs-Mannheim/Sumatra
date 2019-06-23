package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter;

import java.security.InvalidParameterException;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.IMotionModel;

public class ExtKalmanFilter implements IFilter {
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger				log	= Logger.getLogger(getClass());

	public    int						id;			// id of object
	protected double					time;			// time of current state

	protected PredictionContext	context;		// predictor context providing access to global variables
	protected IMotionModel			motion;		// motion model handling the processed object
	
	protected int						stepcount;	// number of lookahead predictions
	protected double					stepsize;	// period of time to next lookahead prediction
	protected double					offset;		// increment of first lookahead prediction time
	
	// arrays for holding fiter data
	// index 0 provides data for the current state
	// other indices provide data for predicted states at 'stepsize*index+predictionGap'
	protected Matrix[]				state;			// state vector
	protected Matrix[]				covar;			// covariance matrix

	protected Matrix					contr;			// control vector
	
	protected final static boolean improbabilityFiltering = false;
	private final static double improbabilityThreshold = 0.1;
	
	private boolean keptAlive;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Creates a new KalmanFilter.
	 * 
	 * @param state_n number of different states in the state vector
	 * @param meas_n number of different states in the measurement vector
	 */
	public ExtKalmanFilter()
	{
		this.id = 0;
		this.time = 0.0;
		this.context = null;
		this.motion = null;
		this.stepcount = 0;
		this.stepsize = 0.0;
		this.offset = 0.0;
		this.contr = null;
		this.state = null;
		this.covar = null;
		this.keptAlive = true;
	}
	
	@Override
	public void init(IMotionModel motionModel, PredictionContext context, 
			double firstTimestamp,	AWPCamObject firstObservation)
	{
		this.motion = motionModel;
		this.context = context;
		
		this.id = this.motion.extraxtObjectID(firstObservation);
		
		this.time = firstTimestamp;
		this.offset = 0.0;
		
		this.stepsize = this.context.stepSize;
		this.stepcount = this.context.stepCount;

		this.keptAlive = false;
		
		// initialize data arrays for 1 current state and 'stepcount' lookahead predictions
		this.state = new Matrix[stepcount+1];
		this.covar = new Matrix[stepcount+1];
		
		Matrix measurement = this.motion.generateMeasurementMatrix(firstObservation, null);
		this.contr = this.motion.generateControlMatrix(null,null);
		this.state[0] = this.motion.generateStateMatrix(measurement, this.contr);
		this.covar[0] = this.motion.generateCovarianceMatrix(this.state[0]);
		
		// no lookahead prediction necessary due to no movement after first observation
		for (int i = 1; i <= this.stepcount; i++)
		{
			this.state[i] = this.state[0].copy();
			this.covar[i] = this.covar[0].copy();
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
		if (index < 0 || index > stepcount)
		{
			log.debug("Lookahead prediction with index " + index + " is out of " +
					"lookahead bounds" + " (min: 0; max: "	+ stepcount + ")");
			throw new InvalidParameterException("Passed index ("+index+") " +
					"is out of valid scope (0-"+stepcount+").");
		}
		
		if (index == 0)
		{
			return time;
		} else
		{
			return time + offset + stepsize*index;
		}
	}

	@Override
	public AMotionResult getLookahead(int index)
	{
		if (index < 0 || index > stepcount)
		{
			log.debug("Lookahead prediction with index " + index + " is out of " +
					"lookahead bounds" + " (min: 0; max: "	+ stepcount + ")");
			throw new InvalidParameterException("Passed index ("+index+") " +
					"is out of valid scope (0-"+stepcount+").");
		}
		
		performAutoLookahead(index);
		
		return motion.generateMotionResult(id, state[index], !keptAlive);
	}

	@Override
	public void observation(double timestamp, AWPCamObject observation)
	{		
		// observation vector
		Matrix o = motion.generateMeasurementMatrix(observation, state[0]);
		
		// if the observation is improbable (false positive), drop it
		if (improbabilityFiltering)
		{
			if (obs_likelihood(timestamp, o) <= improbabilityThreshold)
			{
				log.debug("False positive detected! Observation is ignored. " +
						"(ID: "+id+", t: "+timestamp+")");
				updateOffset(timestamp);
				return;
			}
		}

		this.keptAlive = false;
		
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
		if (index < 1 || index > stepcount)
		{
			log.debug("Lookahead prediction with index " + index + " is out of " +
					"lookahead bounds" + " (min: 1; max: " + stepcount + "). " +
					"Therefore no lookahead was performed.");
			throw new InvalidParameterException("Passed index ("+index+") " +
					"is out of valid scope (1-"+stepcount+").");
		}
		if (state[index-1] == null || covar[index-1] == null)
		{
			log.debug("Lookahead prediction with index " + index + " could not " +
					"be performed because there is no lookahead lookahead " +
					"prediction with index " + (index-1) + " as basis.");
			throw new IllegalArgumentException("No basis for lookahead " +
					"prediction with index "+index+".");			
		}
		
		double dt = stepsize;
		if (index == 1)
		{
			dt = stepsize + offset;
		}
		state[index] = predict_state(state[index - 1], contr, dt);
		covar[index] = predict_covariance(state[index - 1], covar[index - 1], dt);
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
		//deleteLookaheads();
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
	protected Matrix predict_state(Matrix state, Matrix control, double dt)
	{
		// just leave the calculation of the dynamics to the motion model
		return motion.dynamics(state, control, dt);
	}
	 
	/**
	 * 
	 * Predict covariance after time-increment of 'dt'.
	 * 
	 * @param state current state (basis for prediction)
	 * @param control current control (basis for prediction)
	 * @param covariance current covariance (basis for prediction)
	 * @param dt period of time to target time
	 * @return matrix of the predicted covariance
	 */
	protected Matrix predict_covariance(
			Matrix state, Matrix covariance, double dt)
	{
		// handle the object specific matrices by the motion model
		Matrix a = motion.getDynamicsJacobianWRTstate(state, dt);
		Matrix w = motion.getDynamicsJacobianWRTnoise(state, dt);
		Matrix q = motion.getDynamicsCovariance(state, dt);
				
		// calculate the covariance as specified in the prediction step of the EKF
		Matrix part1 = a.times(covariance).times(a.transpose());
		Matrix part2 = w.times(q).times(w.transpose());
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
		while (dt < (stepsize * basisState + offset) && basisState > 0)
		{
			basisState--;
		}
		
		if (basisState > 0)
		{
			performAutoLookahead(basisState);
			dt = dt - (stepsize * basisState + offset);
		}
		
		// use determined state to predict state at targetTime
		return predict_state(state[basisState], contr, dt);
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
		while (dt < (stepsize * basisState + offset) && basisState > 0)
		{
			basisState--;
		}
		
		if (basisState > 0)
		{
			performAutoLookahead(basisState);
			dt = dt - (stepsize * basisState + offset);
		}

		// use determined state to predict covariance at targetTime
		return predict_covariance(state[basisState], covar[basisState], dt);
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
		Matrix pred_state = predictStateAtTime(time);
		Matrix pred_cov = predictCovarianceAtTime(time);
		
		// just call abstract methods because the specification of these matrices depends on the motion model
		Matrix h = motion.getMeasurementJacobianWRTstate(measurement);
		Matrix v = motion.getMeasurementJacobianWRTnoise(measurement);
		Matrix r = motion.getMeasurementCovariance(measurement);
		
		// calculate kalman gain
		Matrix a = pred_cov.times(h.transpose());
		Matrix b = h.times(pred_cov).times(h.transpose());
		Matrix c = v.times(r).times(v.transpose());		
		Matrix k = a.times((b.plus(c)).inverse());
				
		// correct state
		state[0] = pred_state.plus(k.times(
				measurement.minus(motion.measurementDynamics(pred_state))));
		// correct covariance
		int dim = pred_cov.getRowDimension();
		covar[0] = (Matrix.identity(dim, dim).minus(k.times(h))).times(pred_cov);
		
		this.time = time;
		offset = 0.0;
				
		deleteLookaheads();
	}
	
	/**
	 * 
	 * Calculate the likelihood of observing a specific state of the object.
	 * 
	 * @param dt period of time, the state lies ahead
	 * @param z state whose likelihood should be determined
	 * @return likelihood of observing the passed state
	 */
	protected double obs_likelihood(double t, Matrix z)
	{
		double dt = t - time;
		
		// get lookahead prediction smaller than targetTime with minimum offset
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
				
		// project state to time of possible measurement
		Matrix pred_state = predict_state(state[basisState], contr, dt);
		Matrix pred_covar = predict_covariance(state[basisState], covar[basisState], dt);
		
		//TODO WP: this doues not work as expected. howto fix this?
		double likelihood = 1.0;
		for (int i = 0; i < z.getRowDimension(); i++)
		{
			double fac1 = 1.0;//1.0 / Math.sqrt(2*AIMath.PI*pred_covar.get(i, i));
			double fac2 = -0.5 * Math.pow((z.get(i, 0)-pred_state.get(i, 0))/Math.sqrt(pred_covar.get(i, i)), 2);
			double prob = fac1*Math.exp(fac2);
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
			state[i]=null;
			covar[i]=null;
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
 
