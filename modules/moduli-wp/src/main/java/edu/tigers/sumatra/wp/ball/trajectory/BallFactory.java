/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.trajectory;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.ball.prediction.IChipBallConsultant;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory.FixedLossPlusRollingParameters;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelBallTrajectory.TwoPhaseDynamicVelParameters;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseFixedVelBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseFixedVelBallTrajectory.TwoPhaseFixedVelParameters;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseFixedVelConsultant;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;


/**
 * Factory class for creating classes for the configured ball models.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author AndreR <andre@ryll.cc>
 */
public final class BallFactory
{
	@Configurable(comment = "Type of model that will be created for flat balls", spezis = { "",
			"SUMATRA" }, defValueSpezis = { "TWO_PHASE_DYNAMIC_VEL", "TWO_PHASE_DYNAMIC_VEL", })
	private static EFlatBallModel ballModelTypeFlat = EFlatBallModel.TWO_PHASE_DYNAMIC_VEL;
	
	@Configurable(comment = "Type of model that will be created for chipped balls", defValue = "FIXED_LOSS_PLUS_ROLLING")
	private static EChipBallModel ballModelTypeChip = EChipBallModel.FIXED_LOSS_PLUS_ROLLING;
	
	static
	{
		ConfigRegistration.registerClass("wp", BallFactory.class);
	}
	
	
	@SuppressWarnings("unused")
	private BallFactory()
	{
	}
	
	
	/**
	 * Update configs
	 */
	public static void updateConfigs()
	{
		ConfigRegistration.applySpezi("wp", SumatraModel.getInstance().getEnvironment());
	}
	
	
	/**
	 * Create a ball trajectory with the default configured implementation
	 * 
	 * @param state the ball state on which the trajectory is based
	 * @return a new ball trajectory
	 */
	public static ABallTrajectory createTrajectory(final BallTrajectoryState state)
	{
		final ABallTrajectory trajectory;
		
		if (state.isChipped())
		{
			switch (ballModelTypeChip)
			{
				case FIXED_LOSS_PLUS_ROLLING:
					trajectory = FixedLossPlusRollingBallTrajectory.fromState(
							state.getPos().getXYZVector(),
							state.getVel().getXYZVector(),
							state.getSpin(),
							new FixedLossPlusRollingParameters());
					break;
				default:
					throw new UnsupportedOperationException();
			}
		} else
		{
			switch (ballModelTypeFlat)
			{
				case TWO_PHASE_FIXED_VEL:
					trajectory = TwoPhaseFixedVelBallTrajectory.fromState(
							state.getPos().getXYVector(),
							state.getVel().getXYVector(),
							new TwoPhaseFixedVelParameters());
					break;
				case TWO_PHASE_DYNAMIC_VEL:
					trajectory = TwoPhaseDynamicVelBallTrajectory.fromState(
							state.getPos().getXYVector(),
							state.getVel().getXYVector(),
							state.getvSwitchToRoll(),
							new TwoPhaseDynamicVelParameters());
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}
		
		return trajectory;
	}
	
	
	/**
	 * Create a ball trajectory with 2d velocity vector and given chip angle
	 * 
	 * @param chip
	 * @param chipAngle [rad]
	 * @param kickPos [mm/mm]
	 * @param kickVel [mm/s]
	 * @return
	 */
	public static ABallTrajectory createTrajectoryFrom2DKick(final IVector2 kickPos, final IVector2 kickVel,
			final double chipAngle, final boolean chip)
	{
		
		IVector2 xyVector = kickVel;
		double partVelz = 0;
		if (chip)
		{
			partVelz = SumatraMath.cos(chipAngle) * kickVel.getLength2();
			double partVelxy = SumatraMath.sin(chipAngle) * kickVel.getLength2();
			
			xyVector = kickVel.scaleToNew(partVelxy);
		}
		return createTrajectoryFromKick(kickPos, Vector3.from2d(xyVector, partVelz), chip);
	}
	
	
	/**
	 * Create a ball trajectory based on a kick
	 * <br>
	 * Consider using {@link #createTrajectoryFromChipKick(IVector2, IVector3)} or
	 * {@link #createTrajectoryFromStraightKick(IVector2, IVector)}
	 * 
	 * @param kickPos [mm]
	 * @param kickVel [mm/s]
	 * @param chip
	 * @return
	 */
	public static ABallTrajectory createTrajectoryFromKick(final IVector2 kickPos, final IVector kickVel,
			final boolean chip)
	{
		if (chip)
		{
			return createTrajectoryFromChipKick(kickPos, kickVel.getXYZVector());
		}
		return createTrajectoryFromStraightKick(kickPos, kickVel);
	}
	
	
	/**
	 * Create a ball trajectory based on a chip kick
	 *
	 * @param kickPos [mm]
	 * @param kickVel [mm/s]
	 * @return
	 */
	public static ABallTrajectory createTrajectoryFromStraightKick(final IVector2 kickPos, final IVector kickVel)
	{
		final ABallTrajectory trajectory;
		switch (ballModelTypeFlat)
		{
			case TWO_PHASE_FIXED_VEL:
				trajectory = TwoPhaseFixedVelBallTrajectory.fromKick(kickPos, kickVel.getXYVector(),
						new TwoPhaseFixedVelParameters());
				break;
			case TWO_PHASE_DYNAMIC_VEL:
				trajectory = TwoPhaseDynamicVelBallTrajectory.fromKick(kickPos, kickVel.getXYVector(),
						new TwoPhaseDynamicVelParameters());
				break;
			default:
				throw new UnsupportedOperationException();
		}
		return trajectory;
	}
	
	
	/**
	 * Create a ball trajectory based on a straight kick
	 *
	 * @param kickPos [mm]
	 * @param kickVel [mm/s]
	 * @return
	 */
	public static ABallTrajectory createTrajectoryFromChipKick(final IVector2 kickPos, final IVector3 kickVel)
	{
		final ABallTrajectory trajectory;
		switch (ballModelTypeChip)
		{
			case FIXED_LOSS_PLUS_ROLLING:
				trajectory = FixedLossPlusRollingBallTrajectory.fromKick(kickPos, kickVel,
						0, new FixedLossPlusRollingParameters());
				break;
			default:
				throw new UnsupportedOperationException();
		}
		return trajectory;
	}
	
	
	/**
	 * Create a consultant for straight kicks with the default configured implementation
	 * 
	 * @return a new ball consultant for straight kicks
	 */
	public static IStraightBallConsultant createStraightConsultant()
	{
		switch (ballModelTypeFlat)
		{
			case TWO_PHASE_FIXED_VEL:
				return new TwoPhaseFixedVelConsultant(new TwoPhaseFixedVelParameters());
			case TWO_PHASE_DYNAMIC_VEL:
				return new TwoPhaseDynamicVelConsultant(new TwoPhaseDynamicVelParameters());
			default:
				throw new UnsupportedOperationException();
		}
	}
	
	
	/**
	 * Create a consultant for chip kicks with the default configured implementation
	 *
	 * @return a new ball consultant for chip kicks
	 */
	public static IChipBallConsultant createChipConsultant()
	{
		switch (ballModelTypeChip)
		{
			case FIXED_LOSS_PLUS_ROLLING:
				return new FixedLossPlusRollingConsultant(new FixedLossPlusRollingParameters());
			default:
				throw new UnsupportedOperationException();
		}
	}
}
