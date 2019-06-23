/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.json.simple.JSONObject;

import edu.tigers.sumatra.MessagesRobocupSslGeometry.SSL_GeometryCameraCalibration;
import edu.tigers.sumatra.export.IJsonString;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Data holder for calibration data coming from SSL-Vision.
 * 
 * @author AndreR
 */
public class CamCalibration implements IJsonString
{
	private final int				cameraId;
	private final double			focalLength;
	private final double			distortion;
	private final IVector2		principalPoint;
	private final Quaternion	rotationQuaternion;
	private final IVector3		translation;
	private final IVector3		cameraPosition;
	
	
	/**
	 * New calibration
	 * 
	 * @param cameraId
	 * @param focalLength
	 * @param principalPoint
	 * @param distortion
	 * @param q
	 * @param t
	 */
	public CamCalibration(final int cameraId, final double focalLength, final IVector2 principalPoint,
			final double distortion, final Quaternion q, final IVector3 t)
	{
		this.cameraId = cameraId;
		this.focalLength = focalLength;
		this.principalPoint = principalPoint;
		this.distortion = distortion;
		rotationQuaternion = q;
		translation = t;
		cameraPosition = transformToWorld(AVector3.ZERO_VECTOR);
	}
	
	
	/**
	 * Create camera from SSL camera calibration data.
	 * 
	 * @param cc
	 */
	public CamCalibration(final SSL_GeometryCameraCalibration cc)
	{
		cameraId = cc.getCameraId();
		focalLength = cc.getFocalLength();
		principalPoint = Vector2.fromXY(cc.getPrincipalPointX(), cc.getPrincipalPointY());
		distortion = cc.getDistortion();
		rotationQuaternion = new Quaternion(cc.getQ3(), cc.getQ0(), cc.getQ1(), cc.getQ2());
		translation = Vector3.fromXYZ(cc.getTx(), cc.getTy(), cc.getTz());
		cameraPosition = Vector3.fromXYZ(cc.getDerivedCameraWorldTx(), cc.getDerivedCameraWorldTy(),
				cc.getDerivedCameraWorldTz());
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
		builder.append(principalPoint.x());
		builder.append(", principalPointY=");
		builder.append(principalPoint.y());
		builder.append(", distortion=");
		builder.append(distortion);
		builder.append(", q0=");
		builder.append(rotationQuaternion.getQ3());
		builder.append(", q1=");
		builder.append(rotationQuaternion.getQ0());
		builder.append(", q2=");
		builder.append(rotationQuaternion.getQ1());
		builder.append(", q3=");
		builder.append(rotationQuaternion.getQ2());
		builder.append(", tx=");
		builder.append(translation.x());
		builder.append(", ty=");
		builder.append(translation.y());
		builder.append(", tz=");
		builder.append(translation.z());
		builder.append(", derivedCameraWorldTx=");
		builder.append(cameraPosition.x());
		builder.append(", derivedCameraWorldTy=");
		builder.append(cameraPosition.y());
		builder.append(", derivedCameraWorldTz=");
		builder.append(cameraPosition.z());
		return builder.toString();
	}
	
	
	@Override
	public JSONObject toJSON()
	{
		Map<String, Object> top = new LinkedHashMap<>();
		top.put("id", cameraId);
		top.put("focalLength", focalLength);
		top.put("principalPointX", principalPoint.x());
		top.put("principalPointY", principalPoint.y());
		top.put("distortion", distortion);
		top.put("q0", rotationQuaternion.getQ3());
		top.put("q1", rotationQuaternion.getQ0());
		top.put("q2", rotationQuaternion.getQ1());
		top.put("q3", rotationQuaternion.getQ2());
		top.put("tx", translation.x());
		top.put("ty", translation.y());
		top.put("tz", translation.z());
		top.put("derivedCameraWorldTx", cameraPosition.x());
		top.put("derivedCameraWorldTy", cameraPosition.y());
		top.put("derivedCameraWorldTz", cameraPosition.z());
		return new JSONObject(top);
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
	public double getFocalLength()
	{
		return focalLength;
	}
	
	
	/**
	 * @return the distortion
	 */
	public double getDistortion()
	{
		return distortion;
	}
	
	
	/**
	 * @return the principalPoint
	 */
	public IVector2 getPrincipalPoint()
	{
		return principalPoint;
	}
	
	
	/**
	 * @return the rotationQuaternion
	 */
	public Quaternion getRotationQuaternion()
	{
		return rotationQuaternion;
	}
	
	
	/**
	 * Translation vector to translate from world coordinate system to camera coordinate system.
	 * 
	 * @return the translation
	 */
	public IVector3 getTranslation()
	{
		return translation;
	}
	
	
	/**
	 * Rotation matrix to rotate from world coordinate system to camera coordinate system.
	 * v_cam = R*v_world.
	 * 
	 * @return R
	 */
	public RealMatrix getRotationMatrix()
	{
		Rotation rotation = new Rotation(rotationQuaternion.getQ0(),
				rotationQuaternion.getQ1(), rotationQuaternion.getQ2(), rotationQuaternion.getQ3(), false);
		
		return MatrixUtils.createRealMatrix(rotation.getMatrix()).transpose();
	}
	
	
	/**
	 * Transform a vector/point in world coordinate system to camera coordinate system.
	 * 
	 * @param world input vector
	 * @return R*world+t
	 */
	public IVector3 transformToCamera(final IVector3 world)
	{
		RealMatrix rot = getRotationMatrix();
		RealMatrix t = new Array2DRowRealMatrix(translation.toArray());
		RealMatrix in = new Array2DRowRealMatrix(world.toArray());
		
		RealMatrix pos = rot.multiply(in).add(t);
		return Vector3.fromArray(pos.getColumn(0));
	}
	
	
	/**
	 * Transform a vector/point in camera coordinate system to world coordinate system.
	 * 
	 * @param camera input vector
	 * @return R'*(camera-t)
	 */
	public IVector3 transformToWorld(final IVector3 camera)
	{
		RealMatrix rot = getRotationMatrix();
		RealMatrix t = new Array2DRowRealMatrix(translation.toArray());
		RealMatrix in = new Array2DRowRealMatrix(camera.toArray());
		
		RealMatrix pos = rot.transpose().multiply(in.subtract(t));
		return Vector3.fromArray(pos.getColumn(0));
	}
	
	
	/**
	 * Get the camera position in world coordinates.
	 * 
	 * @return camera position
	 */
	public IVector3 getCameraPosition()
	{
		return cameraPosition;
	}
	
	
	/**
	 * Apply radial distortion.
	 * 
	 * @param in 2D input vector
	 * @return distorted output vector
	 */
	public IVector2 distort(final IVector2 in)
	{
		double ru = in.getLength();
		
		if ((distortion <= 0.000001) || (ru < 0.000001))
		{
			// no need to distort
			return in;
		}
		
		double a = distortion;
		double b = (-9.0 * a * a * ru) + (a * Math.sqrt(a * (12.0 + (81.0 * a * ru * ru))));
		if (b < 0.0)
		{
			b = -Math.pow(b, 1.0 / 3.0);
		} else
		{
			b = Math.pow(b, 1.0 / 3.0);
		}
		
		double rd = (Math.pow(2.0 / 3.0, 1.0 / 3.0) / b) - (b / (Math.pow(2.0 * 3.0 * 3.0, 1.0 / 3.0) * a));
		
		return in.multiplyNew(rd / ru);
	}
	
	
	/**
	 * Undistort a vector from radial distortion.
	 * 
	 * @param in distorted input vector
	 * @return undistorted vector
	 */
	public IVector2 undistort(final IVector2 in)
	{
		double rd = in.getLength();
		if (SumatraMath.isZero(rd))
		{
			return in;
		}
		
		double ru = rd * (1.0 + (rd * rd * distortion));
		return in.multiplyNew(ru / rd);
	}
	
	
	/**
	 * Transform a pixel location to a projected position on the field.
	 * 
	 * @param im Image pixel coordinates.
	 * @param objectHeight Height in [mm] of the detected object.
	 * @return Projected and undistorted location on the field.
	 */
	public IVector2 imageToField(final IVector2 im, final double objectHeight)
	{
		// Undo scaling and offset
		IVector2 pD = im.subtractNew(principalPoint).multiply(1.0 / focalLength);
		
		// Compensate for distortion (undistort)
		IVector2 pUn = undistort(pD);
		
		// Now we got a ray on the z axis
		IVector3 v = Vector3.from2d(pUn, 1);
		
		// Transform this ray into world coordinates
		RealMatrix rot = getRotationMatrix().transpose();
		IVector3 vInWorld = Vector3.fromArray(rot.operate(v.toArray()));
		
		IVector3 zeroInWorld = Vector3.fromArray(rot.operate(translation.multiplyNew(-1.0).toArray()))
				.subtract(Vector3.fromXYZ(0, 0, objectHeight));
		IVector3 p = zeroInWorld.addNew(vInWorld);
		
		return p.projectToGroundNew(zeroInWorld);
	}
	
	
	/**
	 * Transform a location in world coordinates to a pixel position
	 * 
	 * @param field world location
	 * @return Pixel location on camera image
	 */
	public IVector2 fieldToImage(final IVector3 field)
	{
		// First transform the point from the field into the coordinate system of the camera
		IVector3 pCam = transformToCamera(field);
		
		// project on image plane
		IVector2 pUn = Vector2.fromXY(pCam.x() / pCam.z(), pCam.y() / pCam.z());
		
		// apply distortion
		IVector2 pDist = distort(pUn);
		
		// convert to pixel location
		return pDist.multiplyNew(focalLength).add(principalPoint);
	}
	
	
	/**
	 * Combined camera projection matrix.
	 * 
	 * @return
	 * @note Legacy support for some old WP code.
	 */
	public RealMatrix getCombinedCameraMatrix()
	{
		RealMatrix rot = getRotationMatrix();
		RealMatrix h = MatrixUtils.createRealMatrix(3, 4);
		
		// H = [obj.R obj.t]
		h.setSubMatrix(rot.getData(), 0, 0);
		h.setColumn(3, translation.toArray());
		
		// K = [obj.f 0 obj.p(1); 0 obj.f obj.p(2); 0 0 1]
		RealMatrix k = MatrixUtils
				.createRealMatrix(new double[][] { { focalLength, 0, principalPoint.x() },
						{ 0, focalLength, principalPoint.y() },
						{ 0, 0, 1 } });
		
		return k.multiply(h);
	}
	
	
	/**
	 * Return an axes-aligned rectangle that reflects the inner viewport of a sensor with a given pixel size;
	 * 
	 * @param sensorPixelSize
	 * @return
	 */
	public IRectangle getApproximatedViewport(final IVector2 sensorPixelSize)
	{
		List<IVector2> points = new ArrayList<>();
		points.add(imageToField(Vector2.fromXY(sensorPixelSize.x() / 2, sensorPixelSize.y()), 150));
		points.add(imageToField(Vector2.fromXY(sensorPixelSize.x() / 2, 0), 150));
		points.add(imageToField(Vector2.fromXY(sensorPixelSize.x(), sensorPixelSize.y() / 2), 150));
		points.add(imageToField(Vector2.fromXY(0, sensorPixelSize.y() / 2), 150));
		
		double minX = points.stream()
				.mapToDouble(IVector2::x)
				.min().orElse(0);
		double maxX = points.stream()
				.mapToDouble(IVector2::x)
				.max().orElse(0);
		double minY = points.stream()
				.mapToDouble(IVector2::y)
				.min().orElse(0);
		double maxY = points.stream()
				.mapToDouble(IVector2::y)
				.max().orElse(0);
		
		return Rectangle.fromPoints(Vector2.fromXY(minX, minY), Vector2.fromXY(maxX, maxY));
	}
}
