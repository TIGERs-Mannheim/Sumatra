/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import com.github.cliftonlabs.json_simple.JsonObject;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry.SSL_GeometryCameraCalibration;
import edu.tigers.sumatra.export.IJsonString;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Data holder for calibration data coming from SSL-Vision.
 */
@Value
@Builder
@AllArgsConstructor
public class CamCalibration implements IJsonString
{
	int cameraId;
	double focalLength;
	double distortion;
	IVector2 principalPoint;
	Quaternion rotationQuaternion;
	IVector3 translation;
	IVector3 cameraPosition;


	/**
	 * New calibration
	 *
	 * @param cameraId
	 * @param focalLength
	 * @param distortion
	 * @param principalPoint
	 * @param q
	 * @param t
	 */
	public CamCalibration(
			int cameraId,
			double focalLength,
			double distortion,
			IVector2 principalPoint,
			Quaternion q,
			IVector3 t
	)
	{
		this.cameraId = cameraId;
		this.focalLength = focalLength;
		this.principalPoint = principalPoint;
		this.distortion = distortion;
		rotationQuaternion = q;
		translation = t;
		cameraPosition = transformToWorld(Vector3f.ZERO_VECTOR);
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


	/**
	 * Is this calibration similar to another one?
	 *
	 * @param other
	 * @return true if similar enough
	 */
	public boolean similarTo(final CamCalibration other)
	{
		boolean samePoints = principalPoint.isCloseTo(other.principalPoint) &&
				translation.isCloseTo(other.translation) &&
				cameraPosition.isCloseTo(other.cameraPosition);

		boolean sameQuaternion = SumatraMath.isEqual(rotationQuaternion.getQ0(), other.rotationQuaternion.getQ0()) &&
				SumatraMath.isEqual(rotationQuaternion.getQ1(), other.rotationQuaternion.getQ1()) &&
				SumatraMath.isEqual(rotationQuaternion.getQ2(), other.rotationQuaternion.getQ2()) &&
				SumatraMath.isEqual(rotationQuaternion.getQ3(), other.rotationQuaternion.getQ3());

		boolean sameParameters = SumatraMath.isEqual(focalLength, other.focalLength) &&
				SumatraMath.isEqual(distortion, other.distortion) &&
				(cameraId == other.cameraId);

		return samePoints && sameQuaternion && sameParameters;
	}


	@Override
	public JsonObject toJSON()
	{
		Map<String, Object> top = new LinkedHashMap<>();
		top.put("id", cameraId);
		top.put("focalLength", focalLength);
		top.put("principalPointX", principalPoint.x());
		top.put("principalPointY", principalPoint.y());
		top.put("distortion", distortion);
		top.put("q0", rotationQuaternion.getQ1());
		top.put("q1", rotationQuaternion.getQ2());
		top.put("q2", rotationQuaternion.getQ3());
		top.put("q3", rotationQuaternion.getQ0());
		top.put("tx", translation.x());
		top.put("ty", translation.y());
		top.put("tz", translation.z());
		top.put("derivedCameraWorldTx", cameraPosition.x());
		top.put("derivedCameraWorldTy", cameraPosition.y());
		top.put("derivedCameraWorldTz", cameraPosition.z());
		return new JsonObject(top);
	}


	/**
	 * Convert to protobuf.
	 *
	 * @return
	 */
	public SSL_GeometryCameraCalibration toProto()
	{
		return SSL_GeometryCameraCalibration.newBuilder()
				.setCameraId(cameraId)
				.setFocalLength((float) focalLength)
				.setPrincipalPointX((float) principalPoint.x())
				.setPrincipalPointY((float) principalPoint.y())
				.setDistortion((float) distortion)
				.setQ0((float) rotationQuaternion.getQ0())
				.setQ1((float) rotationQuaternion.getQ1())
				.setQ2((float) rotationQuaternion.getQ2())
				.setQ3((float) rotationQuaternion.getQ3())
				.setTx((float) translation.x())
				.setTy((float) translation.y())
				.setTz((float) translation.z())
				.setDerivedCameraWorldTx((float) cameraPosition.x())
				.setDerivedCameraWorldTy((float) cameraPosition.y())
				.setDerivedCameraWorldTz((float) cameraPosition.z())
				.build();
	}


	/**
	 * Rotation matrix to rotate from world coordinate system to camera coordinate system.
	 * v_cam = R*v_world.
	 *
	 * @return R
	 */
	private RealMatrix getRotationMatrix()
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
	private IVector3 transformToCamera(final IVector3 world)
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
	private IVector3 transformToWorld(final IVector3 camera)
	{
		RealMatrix rot = getRotationMatrix();
		RealMatrix t = new Array2DRowRealMatrix(translation.toArray());
		RealMatrix in = new Array2DRowRealMatrix(camera.toArray());

		RealMatrix pos = rot.transpose().multiply(in.subtract(t));
		return Vector3.fromArray(pos.getColumn(0));
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
		double b = (-9.0 * a * a * ru) + (a * SumatraMath.sqrt(a * (12.0 + (81.0 * a * ru * ru))));
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
	private IVector2 undistort(final IVector2 in)
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
	 * @param im           Image pixel coordinates.
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
}
