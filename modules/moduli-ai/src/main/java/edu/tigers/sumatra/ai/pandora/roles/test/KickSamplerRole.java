/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.KickSampleSkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author AndreR
 */
public class KickSamplerRole extends ARole
{
	private static List<SamplePoint> samples = new ArrayList<>();
	private final boolean halfField;
	
	
	/**
	 * @param halfField
	 * @param chipFromSide
	 * @param minDurationMs
	 * @param maxDurationMs
	 * @param numSamples
	 * @param cont
	 */
	public KickSamplerRole(final boolean halfField, final boolean chipFromSide, final double minDurationMs,
			final double maxDurationMs, final int numSamples, final boolean cont)
	{
		super(ERole.KICK_SAMPLER);
		
		this.halfField = halfField;
		
		setInitialState(new DefaultState());
		
		if (cont && !samples.isEmpty())
		{
			return;
		}
		
		samples.clear();
		
		double step = (maxDurationMs - minDurationMs) / numSamples;
		
		IVector2 kickTarget;
		if (halfField)
		{
			kickTarget = Vector2.fromXY(0, -Geometry.getFieldWidth() / 2);
		} else
		{
			kickTarget = Vector2.fromXY(Geometry.getFieldLength() / 2, -Geometry.getFieldWidth() / 2);
		}
		
		// create straight sample points
		for (double dur = minDurationMs; dur <= maxDurationMs; dur += step)
		{
			SamplePoint p = new SamplePoint();
			
			p.kickPos = Vector2.fromXY((-Geometry.getFieldLength() / 2) + 200, (Geometry.getFieldWidth() / 2) - 200);
			p.targetAngle = kickTarget.subtractNew(p.kickPos).getAngle();
			p.durationMs = dur;
			p.device = EKickerDevice.STRAIGHT;
			
			samples.add(p);
		}
		
		// create chip sample points
		for (double dur = minDurationMs; dur <= maxDurationMs; dur += step)
		{
			SamplePoint p = new SamplePoint();
			
			if (chipFromSide)
			{
				p.kickPos = Vector2.fromXY(0, (-Geometry.getFieldWidth() / 2) + 200);
				p.targetAngle = AngleMath.PI_HALF;
			} else
			{
				p.kickPos = Vector2.fromXY((-Geometry.getFieldLength() / 2) + 200, 0);
				p.targetAngle = 0;
			}
			
			p.durationMs = dur;
			p.device = EKickerDevice.CHIP;
			
			samples.add(p);
		}
	}
	
	private class SamplePoint
	{
		IVector2 kickPos;
		double targetAngle;
		double durationMs;
		EKickerDevice device;
	}
	
	private class DefaultState implements IState
	{
		private KickSampleSkill skill;
		private boolean botLost = false;
		
		
		@SuppressWarnings("unchecked")
		private void startSample(final SamplePoint p)
		{
			skill = new KickSampleSkill(p.kickPos, p.targetAngle, p.device, p.durationMs);
			
			Map<String, Object> jsonMapping = new LinkedHashMap<>();
			
			if (halfField)
			{
				jsonMapping.put("field", Geometry.getFieldHalfOur().toJSON());
			} else
			{
				jsonMapping.put("field", Geometry.getField().toJSON());
			}
			
			Collection<CamCalibration> cams = Geometry.getLastCamGeometry().getCalibrations().values();
			JSONArray camList = new JSONArray();
			for (CamCalibration c : cams)
			{
				camList.add(c.toJSON());
			}
			jsonMapping.put("cams", camList);
			
			skill.setAdditionalJsonData(jsonMapping);
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doEntryActions()
		{
			startSample(samples.get(0));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBot() == null)
			{
				botLost = true;
			}
			
			if (botLost && (getBot() != null))
			{
				botLost = false;
				
				startSample(samples.get(0));
			}
			
			if (skill.getNumSamples() > 0)
			{
				samples.remove(0);
				
				if (samples.isEmpty())
				{
					setCompleted();
				} else
				{
					startSample(samples.get(0));
				}
			}
		}
	}
}
