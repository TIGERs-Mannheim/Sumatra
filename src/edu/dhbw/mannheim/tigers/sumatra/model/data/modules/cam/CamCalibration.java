/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
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
 * 
 */
public class CamCalibration
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public final int		cameraId;
	
	/** */
	public final float	focalLength;
	
	/** */
	public final float	principalPointX;
	/** */
	public final float	principalPointY;
	
	/** */
	public final float	distortion;
	
	/** */
	public final float	q0;
	/** */
	public final float	q1;
	/** */
	public final float	q2;
	/** */
	public final float	q3;
	
	/** */
	public final float	tx;
	/** */
	public final float	ty;
	/** */
	public final float	tz;
	
	/** */
	public final float	derivedCameraWorldTx;
	/** */
	public final float	derivedCameraWorldTy;
	/** */
	public final float	derivedCameraWorldTz;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
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
	public CamCalibration(int cameraId, float focalLength, float principalPointX, float principalPointY,
			float distortion, float q0, float q1, float q2, float q3, float tx, float ty, float tz,
			float derivedcameraWorldTx, float derivedCameraWorldTy, float derivedCameraWorldTz)
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
	public CamCalibration(SSL_GeometryCameraCalibration cc)
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
		builder.append(cameraId);
		builder.append(", focalLength=");
		builder.append(focalLength);
		builder.append(", principalPointX=");
		builder.append(principalPointX);
		builder.append(", principalPointY=");
		builder.append(principalPointY);
		builder.append(", distortion=");
		builder.append(distortion);
		builder.append(", q0=");
		builder.append(q0);
		builder.append(", q1=");
		builder.append(q1);
		builder.append(", q2=");
		builder.append(q2);
		builder.append(", q3=");
		builder.append(q3);
		builder.append(", tx=");
		builder.append(tx);
		builder.append(", ty=");
		builder.append(ty);
		builder.append(", tz=");
		builder.append(tz);
		builder.append(", derivedCameraWorldTx=");
		builder.append(derivedCameraWorldTx);
		builder.append(", derivedCameraWorldTy=");
		builder.append(derivedCameraWorldTy);
		builder.append(", derivedCameraWorldTz=");
		builder.append(derivedCameraWorldTz);
		return builder.toString();
	}
}
