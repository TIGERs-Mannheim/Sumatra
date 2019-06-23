/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

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
	public final int		camera_id;
	
	public final float	focal_length;
	
	public final float	principal_point_x;
	public final float	principal_point_y;
	
	public final float	distortion;
	
	public final float	q0;
	public final float	q1;
	public final float	q2;
	public final float	q3;
	
	public final float	tx;
	public final float	ty;
	public final float	tz;
	
	public final float	derived_camera_world_tx;
	public final float	derived_camera_world_ty;
	public final float	derived_camera_world_tz;
	
	
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
	 * @param camera_id
	 * @param focal_length
	 * @param principal_point_x
	 * @param principal_point_y
	 * @param distortion
	 * @param q0
	 * @param q1
	 * @param q2
	 * @param q3
	 * @param tx
	 * @param ty
	 * @param tz
	 * @param derived_camera_world_tx
	 * @param derived_camera_world_ty
	 * @param derived_camera_world_tz
	 */
	public CamCalibration(int camera_id, float focal_length, float principal_point_x, float principal_point_y,
			float distortion, float q0, float q1, float q2, float q3, float tx, float ty, float tz,
			float derived_camera_world_tx, float derived_camera_world_ty, float derived_camera_world_tz)
	{
		this.camera_id = camera_id;
		this.focal_length = focal_length;
		this.principal_point_x = principal_point_x;
		this.principal_point_y = principal_point_y;
		this.distortion = distortion;
		this.q0 = q0;
		this.q1 = q1;
		this.q2 = q2;
		this.q3 = q3;
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
		this.derived_camera_world_tx = derived_camera_world_tx;
		this.derived_camera_world_ty = derived_camera_world_ty;
		this.derived_camera_world_tz = derived_camera_world_tz;
	}
	

	public CamCalibration(SSL_GeometryCameraCalibration cc)
	{
		this.camera_id = cc.getCameraId();
		this.focal_length = cc.getFocalLength();
		this.principal_point_x = cc.getPrincipalPointX();
		this.principal_point_y = cc.getPrincipalPointY();
		this.distortion = cc.getDistortion();
		this.q0 = cc.getQ0();
		this.q1 = cc.getQ1();
		this.q2 = cc.getQ2();
		this.q3 = cc.getQ3();
		this.tx = cc.getTx();
		this.ty = cc.getTy();
		this.tz = cc.getTz();
		this.derived_camera_world_tx = cc.getDerivedCameraWorldTx();
		this.derived_camera_world_ty = cc.getDerivedCameraWorldTy();
		this.derived_camera_world_tz = cc.getDerivedCameraWorldTz();
	}
	

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SSLCameraCalibration [camera_id=");
		builder.append(camera_id);
		builder.append(", focal_length=");
		builder.append(focal_length);
		builder.append(", principal_point_x=");
		builder.append(principal_point_x);
		builder.append(", principal_point_y=");
		builder.append(principal_point_y);
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
		builder.append(", derived_camera_world_tx=");
		builder.append(derived_camera_world_tx);
		builder.append(", derived_camera_world_ty=");
		builder.append(derived_camera_world_ty);
		builder.append(", derived_camera_world_tz=");
		builder.append(derived_camera_world_tz);
		return builder.toString();
	}
}
