/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;


/** */
public class SensorUncertainties
{
	/** */
	private Vector3	vision			= new Vector3();
	/** */
	private Vector3	encoder			= new Vector3();
	/** */
	private Vector2	accelerometer	= new Vector2();
	/** */
	private float		gyroscope;
	/** */
	private Vector3	motor				= new Vector3();
	
	
	/** */
	public SensorUncertainties()
	{
	}
	
	
	/**
	 * 
	 * @param config
	 */
	public SensorUncertainties(SubnodeConfiguration config)
	{
		setConfiguration(config);
	}
	
	
	/**
	 * 
	 * @param config
	 */
	public void setConfiguration(SubnodeConfiguration config)
	{
		vision.setConfiguration(config.configurationAt("vision"));
		encoder.setConfiguration(config.configurationAt("encoder"));
		accelerometer.setConfiguration(config.configurationAt("accelerometer"));
		gyroscope = config.getFloat("gyroscope", 1.0f);
		motor.setConfiguration(config.configurationAt("motor"));
	}
	
	
	/**
	 * 
	 * @return
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final CombinedConfiguration config = new CombinedConfiguration();
		
		final HierarchicalConfiguration gyroCfg = new HierarchicalConfiguration();
		gyroCfg.addProperty("gyroscope", gyroscope);
		
		config.addConfiguration(vision.getConfiguration(), "vision", "vision");
		config.addConfiguration(encoder.getConfiguration(), "encoder", "encoder");
		config.addConfiguration(accelerometer.getConfiguration(), "accelerometer", "accelerometer");
		config.addConfiguration(gyroCfg);
		config.addConfiguration(motor.getConfiguration(), "motor", "motor");
		
		return config;
	}
	
	
	/**
	 * @return the vision
	 */
	public Vector3 getVision()
	{
		return vision;
	}
	
	
	/**
	 * @param vision the vision to set
	 */
	public void setVision(Vector3 vision)
	{
		this.vision = vision;
	}
	
	
	/**
	 * @return the encoder
	 */
	public Vector3 getEncoder()
	{
		return encoder;
	}
	
	
	/**
	 * @param encoder the encoder to set
	 */
	public void setEncoder(Vector3 encoder)
	{
		this.encoder = encoder;
	}
	
	
	/**
	 * @return the accelerometer
	 */
	public Vector2 getAccelerometer()
	{
		return accelerometer;
	}
	
	
	/**
	 * @param accelerometer the accelerometer to set
	 */
	public void setAccelerometer(Vector2 accelerometer)
	{
		this.accelerometer = accelerometer;
	}
	
	
	/**
	 * @return the gyroscope
	 */
	public float getGyroscope()
	{
		return gyroscope;
	}
	
	
	/**
	 * @param gyroscope the gyroscope to set
	 */
	public void setGyroscope(float gyroscope)
	{
		this.gyroscope = gyroscope;
	}
	
	
	/**
	 * @return the motor
	 */
	public Vector3 getMotor()
	{
		return motor;
	}
	
	
	/**
	 * @param motor the motor to set
	 */
	public void setMotor(Vector3 motor)
	{
		this.motor = motor;
	}
}