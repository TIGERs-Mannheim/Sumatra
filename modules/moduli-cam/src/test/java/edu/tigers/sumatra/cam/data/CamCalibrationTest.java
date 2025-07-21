/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import org.apache.commons.math3.complex.Quaternion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


class CamCalibrationTest
{
	@Test
	void cameraToWorldTest()
	{
		CamCalibration cam = getCamera();
		
		IVector3 pos = cam.getCameraPosition();

		assertTrue(pos.isCloseTo(getKnownDerivedCameraWorld(), 1.0));
	}
	
	
	private CamCalibration getCamera()
	{
		return new CamCalibration(0, 603.4305419921875,
				0.28, Vector2.fromXY(384.8830871582031, 324.7497863769531),
				new Quaternion(0.0443589985370636, 0.01445200014859438, -0.9989029765129089, -0.00419999985024333),
				Vector3.fromXYZ(1529.866943359375, -1090.3587646484375, 2506.0));
	}
	
	
	private IVector3 getKnownDerivedCameraWorld()
	{
		return Vector3.fromXYZ(1269.5, 1109.2, 2639.6);
	}
}
