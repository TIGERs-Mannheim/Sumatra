/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot;

import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.microtypes.KickDetection;
import edu.tigers.sumatra.loganalysis.microtypes.LineDetection;


public class PassTypeDetectionFrame extends TypeDetectionFrame
{

	private ShotDetection passingDetection; // reference to the shot detection class to change its states
	private ShotBuilder shotBuilder; // Helps creating a PassingShape object one by one

	private KickDetection kickDetection; // class for detecting kicks
	private LineDetection lineDetection; // class for detecting ball lines


	public PassTypeDetectionFrame(final TypeDetectionFrame f, final ShotBuilder shotBuilder,
			final KickDetection kickDetection, final LineDetection lineDetection,
			final ShotDetection passingDetection)
	{
		super(f.getWorldFrameWrapper(), f.getMemory(), f.getClosestBotToBall(), f.getSecondClosestBotToBall(),
				f.getShapeMap(), f.getFrameId());
		this.kickDetection = kickDetection;
		this.lineDetection = lineDetection;
		this.shotBuilder = shotBuilder;
		this.passingDetection = passingDetection;
	}


	public ShotDetection getPassingDetection()
	{
		return passingDetection;
	}


	public ShotBuilder getShotBuilder()
	{
		return shotBuilder;
	}


	public KickDetection getKickDetection()
	{
		return kickDetection;
	}


	public LineDetection getLineDetection()
	{

		return lineDetection;
	}

}
