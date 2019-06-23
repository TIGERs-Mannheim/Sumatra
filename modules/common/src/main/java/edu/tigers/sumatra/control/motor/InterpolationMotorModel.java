/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.control.motor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InterpolationMotorModel extends AMotorModel
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(InterpolationMotorModel.class.getName());
	
	private static final String	FOLDER			= "data/interpolationModel/";
	
	private List<Angle>				supportAngles	= new ArrayList<>();
	private final double[]			in_Z				= new double[] { 3, 3, 3, 3 };
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public InterpolationMotorModel()
	{
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param file
	 */
	public InterpolationMotorModel(final String file)
	{
		readFromFile(file);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param mm
	 * @param maxSpeed
	 * @param speedStep
	 * @param angleStep
	 * @return
	 */
	public static InterpolationMotorModel fromMotorModel(final IMotorModel mm, final double maxSpeed,
			final double speedStep, final double angleStep)
	{
		InterpolationMotorModel imm = new InterpolationMotorModel();
		
		for (double angle = -AngleMath.PI; angle < AngleMath.PI; angle += angleStep)
		{
			for (double speed = 0.0; speed <= (maxSpeed + 1e-4); speed += speedStep)
			{
				IVectorN vec = mm.getWheelSpeed(Vector3.from2d(Vector2.fromAngle(angle).scaleTo(speed), 0));
				imm.addSupport(angle, speed, vec.toArray());
			}
		}
		
		return imm;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param name
	 */
	public void writeToFile(final String name)
	{
		String filename = FOLDER + name;
		if (Files.exists(Paths.get(filename + ".csv")))
		{
			try
			{
				Files.move(Paths.get(filename + ".csv"),
						Paths.get(FOLDER, name + "-" + System.currentTimeMillis() + ".csv"));
			} catch (IOException err)
			{
				log.error("Could not move file.", err);
			}
		}
		
		CSVExporter exp = new CSVExporter(filename, false);
		
		for (Angle angle : supportAngles)
		{
			for (Speed speed : angle.supSpeeds)
			{
				List<Number> nbrs = new ArrayList<>();
				nbrs.add(angle.angle);
				nbrs.add(speed.speed);
				for (int i = 0; i < 4; i++)
				{
					nbrs.add(speed.motors[i]);
				}
				for (double element : speed.debug)
				{
					nbrs.add(element);
				}
				exp.addValues(nbrs);
			}
		}
		
		exp.close();
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param file
	 */
	public void readFromFile(final String file)
	{
		try
		{
			Files
					.lines(Paths.get(FOLDER + file))
					.filter(line -> !line.startsWith("#"))
					.map(line -> line.split("[, ]"))
					.map(arr -> Arrays.asList(arr).stream()
							.map(s -> Double.valueOf(s))
							.collect(Collectors.toList()))
					.filter(l -> l.size() >= 6)
					.forEach(
							l -> addSupport(l.get(0), l.get(1),
									new double[] { l.get(2), l.get(3), l.get(4), l.get(5) }));
		} catch (NumberFormatException | IOException err)
		{
			log.error("Could not read " + file, err);
		}
	}
	
	
	@Override
	protected VectorN getWheelSpeedInternal(final IVector3 xyw)
	{
		double velAngle = 0;
		if (!xyw.getXYVector().isZeroVector())
		{
			velAngle = xyw.getXYVector().getAngle();
		}
		double speed = xyw.getXYVector().getLength2();
		
		double[] in = new double[4];
		
		// find angle pair
		Angle sa1 = supportAngles.get(supportAngles.size() - 1);
		Angle sa2 = supportAngles.get(0);
		for (int s = 1; s < (supportAngles.size()); s++)
		{
			if (velAngle <= supportAngles.get(s).angle)
			{
				sa1 = supportAngles.get(s - 1);
				sa2 = supportAngles.get(s);
				break;
			}
		}
		
		double[] interpolateSpeed1 = null;
		double[] interpolateSpeed2 = null;
		
		for (int j = 1; j < sa1.supSpeeds.size(); j++)
		{
			if ((speed <= sa1.supSpeeds.get(j).speed) || (j == (sa1.supSpeeds.size() - 1)))
			{
				Speed sup11 = sa1.supSpeeds.get(j - 1);
				Speed sup12 = sa1.supSpeeds.get(j);
				interpolateSpeed1 = interpolateLinear(speed, sup11.speed, sup12.speed, sup11.motors,
						sup12.motors);
				break;
			}
		}
		if (interpolateSpeed1 == null)
		{
			log.warn("No support vector for speed: " + speed);
			interpolateSpeed1 = sa1.supSpeeds.get(sa1.supSpeeds.size() - 1).motors;
		}
		
		for (int i = 1; i < sa2.supSpeeds.size(); i++)
		{
			if ((speed <= sa2.supSpeeds.get(i).speed) || (i == (sa2.supSpeeds.size() - 1)))
			{
				Speed sup21 = sa2.supSpeeds.get(i - 1);
				Speed sup22 = sa2.supSpeeds.get(i);
				interpolateSpeed2 = interpolateLinear(speed, sup21.speed, sup22.speed, sup21.motors,
						sup22.motors);
				break;
			}
		}
		if (interpolateSpeed2 == null)
		{
			log.warn("No support vector for speed: " + speed);
			interpolateSpeed2 = sa2.supSpeeds.get(sa2.supSpeeds.size() - 1).motors;
		}
		
		
		double[] motors = interpolateAngle(velAngle, sa1.angle, sa2.angle, interpolateSpeed1,
				interpolateSpeed2);
		
		for (int k = 0; k < 4; k++)
		{
			in[k] = motors[k]
					+ (xyw.z() * in_Z[k]);
		}
		
		return VectorN.from(in);
	}
	
	
	@Override
	protected Vector3 getXywSpeedInternal(final IVectorN wheelSpeed)
	{
		throw new NotImplementedException();
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param angle
	 * @param speed
	 * @param motors
	 * @param debug
	 */
	public void addSupport(final double angle, final double speed, final double[] motors, final double... debug)
	{
		Angle support = getSupportAngle(angle);
		Optional<Speed> existingSpeed = getSpeed(support.supSpeeds, speed);
		if (existingSpeed.isPresent())
		{
			log.warn("Duplicate support vector: angle=" + angle + ", speed=" + speed);
		} else
		{
			support.supSpeeds.add(new Speed(speed, motors, debug));
			Collections.sort(support.supSpeeds);
		}
	}
	
	
	private Angle getSupportAngle(final double angle)
	{
		for (Angle sa : supportAngles)
		{
			if (Math.abs(AngleMath.difference(sa.angle, angle)) < 0.01)
			{
				return sa;
			}
		}
		
		Angle sa = new Angle(angle);
		supportAngles.add(sa);
		Collections.sort(supportAngles);
		return sa;
	}
	
	
	private Optional<Speed> getSpeed(final List<Speed> speeds, final double speed)
	{
		for (Speed s : speeds)
		{
			if (SumatraMath.isEqual(speed, s.speed))
			{
				return Optional.of(s);
			}
		}
		return Optional.empty();
	}
	
	
	private double[] interpolateAngle(final double v, final double v1, final double v2, final double[] m1,
			final double[] m2)
	{
		double[] m0 = new double[4];
		double relMax = AngleMath.difference(v, v1) / AngleMath.difference(v2, v1);
		
		for (int i = 0; i < 4; i++)
		{
			m0[i] = (m1[i] * (1 - relMax)) + (m2[i] * relMax);
		}
		return m0;
	}
	
	
	private double[] interpolateLinear(final double v, final double v1, final double v2, final double[] m1,
			final double[] m2)
	{
		double[] m0 = new double[4];
		double relMax = (v - v1) / (v2 - v1);
		
		for (int i = 0; i < 4; i++)
		{
			m0[i] = (m1[i] * (1 - relMax)) + (m2[i] * relMax);
		}
		return m0;
	}
	
	private static class Angle implements Comparable<Angle>
	{
		final double		angle;
		final List<Speed>	supSpeeds	= new ArrayList<>();
		
		
		/**
		 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
		 * @param angle
		 */
		public Angle(final double angle)
		{
			this.angle = angle;
		}
		
		
		@Override
		public int compareTo(final Angle o)
		{
			return Double.compare(angle, o.angle);
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(angle);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			result = (prime * result) + supSpeeds.hashCode();
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			Angle other = (Angle) obj;
			if (Double.doubleToLongBits(angle) != Double.doubleToLongBits(other.angle))
			{
				return false;
			}
			return supSpeeds.equals(other.supSpeeds);
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("Angle [angle=");
			builder.append(angle);
			builder.append(", supSpeeds=\n");
			for (Speed s : supSpeeds)
			{
				builder.append("\t");
				builder.append(s);
				builder.append("\n");
			}
			builder.append("]");
			return builder.toString();
		}
	}
	
	private static class Speed implements Comparable<Speed>
	{
		final double	speed;
		final double[]	motors;
		final double[]	debug;
		
		
		/**
		 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
		 * @param speed
		 * @param motors
		 * @param debug
		 */
		public Speed(final double speed, final double[] motors, final double[] debug)
		{
			this.speed = speed;
			this.motors = Arrays.copyOf(motors, motors.length);
			this.debug = debug;
		}
		
		
		@Override
		public int compareTo(final Speed o)
		{
			return Double.compare(speed, o.speed);
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = (prime * result) + Arrays.hashCode(motors);
			long temp;
			temp = Double.doubleToLongBits(speed);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			Speed other = (Speed) obj;
			if (!Arrays.equals(motors, other.motors))
			{
				return false;
			}
			return Double.doubleToLongBits(speed) == Double.doubleToLongBits(other.speed);
		}
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("[speed=");
			builder.append(speed);
			builder.append(", motors=");
			builder.append(Arrays.toString(motors));
			builder.append(", debug=");
			builder.append(Arrays.toString(debug));
			builder.append("]");
			return builder.toString();
		}
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("InterpolationMotorModel [supportAngles=\n");
		for (Angle angle : supportAngles)
		{
			builder.append(angle);
			builder.append("\n");
		}
		builder.append("]");
		return builder.toString();
	}
	
	
	@Override
	public EMotorModel getType()
	{
		return EMotorModel.INTERPOLATION;
	}
}
