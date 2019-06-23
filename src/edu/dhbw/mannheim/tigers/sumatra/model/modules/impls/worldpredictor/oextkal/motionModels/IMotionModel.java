package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import Jama.Matrix;

public interface IMotionModel {
 
	public abstract Matrix dynamics(Matrix state, Matrix control, double dt);
	public abstract Matrix sample(Matrix state, Matrix control);
	public abstract double transitionProbability(Matrix stateNew, Matrix stateOld, Matrix control);
	public abstract double measurementProbability(Matrix state, Matrix measurement, double dt);
	public abstract Matrix getDynamicsJacobianWRTstate(Matrix state, double dt);
	public abstract Matrix getDynamicsJacobianWRTnoise(Matrix state, double dt);
	public abstract Matrix getDynamicsCovariance(Matrix state, double dt);
	public abstract AMotionResult generateMotionResult(int id, Matrix state, boolean onCam);
	public abstract Matrix generateMeasurementMatrix(AWPCamObject observation, Matrix state);
	public abstract Matrix generateStateMatrix(Matrix measurement, Matrix control);
	public abstract Matrix updateStateOnNewControl(IControl control, Matrix state);
	public abstract Matrix updateCovarianceOnNewControl(IControl control, Matrix covariance);
	public abstract Matrix generateControlMatrix(IControl control, Matrix state);
	public abstract Matrix generateCovarianceMatrix(Matrix state);
	public abstract int extraxtObjectID(AWPCamObject observation);	

	public abstract Matrix measurementDynamics(Matrix state);
	public abstract Matrix getMeasurementJacobianWRTstate(Matrix state);
	public abstract Matrix getMeasurementJacobianWRTnoise(Matrix state);
	public abstract Matrix getMeasurementCovariance(Matrix measurement);
	
	public abstract Matrix getStateOnNoObservation(Matrix state);
	public abstract Matrix getCovarianceOnNoObservation(Matrix covariance);
	public abstract Matrix getControlOnNoObservation(Matrix control);
}
 
