package edu.tigers.sumatra.planarcurve;

/**
 * @author AndreR <andre@ryll.cc>
 */
@FunctionalInterface
public interface IPlanarCurveProvider
{
	/**
	 * Return this "whatever" as a planar curve, consisting of one or more planar curve segments.
	 * 
	 * @return
	 */
	PlanarCurve getPlanarCurve();
}
