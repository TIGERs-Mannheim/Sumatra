/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_GeometryCameraCalibration;


/**
 * Data holder for calibration data coming from SSL-Vision (contains {@link SSL_GeometryCameraCalibration}-data for
 * internal use)
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 */
public class CamCalibration
{
	private final int		cameraId;
	
	private final float	focalLength;
	
	private final float	principalPointX;
	private final float	principalPointY;
	
	private final float	distortion;
	
	private final float	q0;
	private final float	q1;
	private final float	q2;
	private final float	q3;
	
	private final float	tx;
	private final float	ty;
	private final float	tz;
	
	private final float	derivedCameraWorldTx;
	private final float	derivedCameraWorldTy;
	private final float	derivedCameraWorldTz;
	
	
	/**
	 * <p>
	 * <i>Implemented being aware of EJSE Item 2; but we prefer performance over readability - at least in this case.
	 * Objects are created at only one point in the system, but needs to be fast (so builder seems to be too much
	 * overhead).</i>
	 * </p>
	 * 
	 * @param cameraId
	 * @param focalLength
	 * @param principalPointX
	 * @param principalPointY
	 * @param distortion
	 * @param q0
	 * @param q1
	 * @param q2
	 * @param q3
	 * @param tx
	 * @param ty
	 * @param tz
	 * @param derivedcameraWorldTx
	 * @param derivedCameraWorldTy
	 * @param derivedCameraWorldTz
	 */
	public CamCalibration(final int cameraId, final float focalLength, final float principalPointX,
			final float principalPointY,
			final float distortion, final float q0, final float q1, final float q2, final float q3, final float tx,
			final float ty, final float tz,
			final float derivedcameraWorldTx, final float derivedCameraWorldTy, final float derivedCameraWorldTz)
	{
		this.cameraId = cameraId;
		this.focalLength = focalLength;
		this.principalPointX = principalPointX;
		this.principalPointY = principalPointY;
		this.distortion = distortion;
		this.q0 = q0;
		this.q1 = q1;
		this.q2 = q2;
		this.q3 = q3;
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
		derivedCameraWorldTx = derivedcameraWorldTx;
		this.derivedCameraWorldTy = derivedCameraWorldTy;
		this.derivedCameraWorldTz = derivedCameraWorldTz;
	}
	
	
	/**
	 * @param cc
	 */
	public CamCalibration(final SSL_GeometryCameraCalibration cc)
	{
		cameraId = cc.getCameraId();
		focalLength = cc.getFocalLength();
		principalPointX = cc.getPrincipalPointX();
		principalPointY = cc.getPrincipalPointY();
		distortion = cc.getDistortion();
		q0 = cc.getQ0();
		q1 = cc.getQ1();
		q2 = cc.getQ2();
		q3 = cc.getQ3();
		tx = cc.getTx();
		ty = cc.getTy();
		tz = cc.getTz();
		derivedCameraWorldTx = cc.getDerivedCameraWorldTx();
		derivedCameraWorldTy = cc.getDerivedCameraWorldTy();
		derivedCameraWorldTz = cc.getDerivedCameraWorldTz();
	}
	
	
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("SSLCameraCalibration [cameraId=");
		builder.append(getCameraId());
		builder.append(", focalLength=");
		builder.append(getFocalLength());
		builder.append(", principalPointX=");
		builder.append(getPrincipalPointX());
		builder.append(", principalPointY=");
		builder.append(getPrincipalPointY());
		builder.append(", distortion=");
		builder.append(getDistortion());
		builder.append(", q0=");
		builder.append(getQ0());
		builder.append(", q1=");
		builder.append(getQ1());
		builder.append(", q2=");
		builder.append(getQ2());
		builder.append(", q3=");
		builder.append(getQ3());
		builder.append(", tx=");
		builder.append(getTx());
		builder.append(", ty=");
		builder.append(getTy());
		builder.append(", tz=");
		builder.append(getTz());
		builder.append(", derivedCameraWorldTx=");
		builder.append(getDerivedCameraWorldTx());
		builder.append(", derivedCameraWorldTy=");
		builder.append(getDerivedCameraWorldTy());
		builder.append(", derivedCameraWorldTz=");
		builder.append(getDerivedCameraWorldTz());
		return builder.toString();
	}
	
	
	/**
	 * @return the cameraId
	 */
	public int getCameraId()
	{
		return cameraId;
	}
	
	
	/**
	 * @return the focalLength
	 */
	public float getFocalLength()
	{
		return focalLength;
	}
	
	
	/**
	 * @return the principalPointX
	 */
	public float getPrincipalPointX()
	{
		return principalPointX;
	}
	
	
	/**
	 * @return the principalPointY
	 */
	public float getPrincipalPointY()
	{
		return principalPointY;
	}
	
	
	/**
	 * @return the distortion
	 */
	public float getDistortion()
	{
		return distortion;
	}
	
	
	/**
	 * @return the q0
	 */
	public float getQ0()
	{
		return q0;
	}
	
	
	/**
	 * @return the q1
	 */
	public float getQ1()
	{
		return q1;
	}
	
	
	/**
	 * @return the q2
	 */
	public float getQ2()
	{
		return q2;
	}
	
	
	/**
	 * @return the q3
	 */
	public float getQ3()
	{
		return q3;
	}
	
	
	/**
	 * @return the tx
	 */
	public float getTx()
	{
		return tx;
	}
	
	
	/**
	 * @return the ty
	 */
	public float getTy()
	{
		return ty;
	}
	
	
	/**
	 * @return the tz
	 */
	public float getTz()
	{
		return tz;
	}
	
	
	/**
	 * @return the derivedCameraWorldTx
	 */
	public float getDerivedCameraWorldTx()
	{
		return derivedCameraWorldTx;
	}
	
	
	/**
	 * @return the derivedCameraWorldTy
	 */
	public float getDerivedCameraWorldTy()
	{
		return derivedCameraWorldTy;
	}
	
	
	/**
	 * @return the derivedCameraWorldTz
	 */
	public float getDerivedCameraWorldTz()
	{
		return derivedCameraWorldTz;
	}
}
