/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 4, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.control.motor.InterpolationMotorModel;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InterpolationMotorModelTest
{
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@Test
	public void testCreation()
	{
		MatrixMotorModel mm = new MatrixMotorModel();
		InterpolationMotorModel imm = InterpolationMotorModel.fromMotorModel(mm, 2.0, 0.2, AngleMath.PI_QUART / 4);
		
		boolean valid = true;
		for (double angle = -AngleMath.PI; angle <= (AngleMath.PI + 0.1); angle += AngleMath.PI_QUART)
		{
			for (double speed = 0; speed < 3.0; speed += 0.1)
			{
				IVector3 targetVel = new Vector3(new Vector2(angle).scaleTo(speed), 0);
				IVectorN vecRef = mm.getWheelSpeed(targetVel);
				IVectorN vec = imm.getWheelSpeed(targetVel);
				if (!vecRef.equals(vec, 0.01))
				{
					valid = false;
					System.out.println("Model check failed for " + targetVel + ": " + vec + " != " + vecRef);
				}
			}
		}
		Assert.assertTrue("Model not valid.", valid);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@Test
	@Ignore
	public void export()
	{
		MatrixMotorModel mm = new MatrixMotorModel();
		InterpolationMotorModel imm = InterpolationMotorModel.fromMotorModel(mm, 2.0, 0.2, AngleMath.PI_QUART / 4);
		
		CSVExporter exp_mm = new CSVExporter("logs/mm", false);
		CSVExporter exp_imm = new CSVExporter("logs/imm", false);
		
		boolean valid = true;
		for (double angle = -AngleMath.PI; angle <= (AngleMath.PI + 0.1); angle += AngleMath.PI_QUART / 4)
		{
			for (double speed = 0; speed < 2.5; speed += 0.05)
			{
				IVector3 targetVel = new Vector3(new Vector2(angle).scaleTo(speed), 0);
				IVectorN motors_mm = mm.getWheelSpeed(targetVel);
				IVectorN motors_imm = imm.getWheelSpeed(targetVel);
				
				List<Number> nbrs_mm = new ArrayList<>();
				nbrs_mm.addAll(motors_mm.getNumberList());
				nbrs_mm.addAll(targetVel.getNumberList());
				nbrs_mm.addAll(targetVel.getNumberList());
				exp_mm.addValues(nbrs_mm);
				
				List<Number> nbrs_imm = new ArrayList<>();
				nbrs_imm.addAll(motors_imm.getNumberList());
				nbrs_imm.addAll(targetVel.getNumberList());
				nbrs_imm.addAll(targetVel.getNumberList());
				exp_imm.addValues(nbrs_imm);
			}
		}
		Assert.assertTrue("Model not valid.", valid);
	}
	
}
