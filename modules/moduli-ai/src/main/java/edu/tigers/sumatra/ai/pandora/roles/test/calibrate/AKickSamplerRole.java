/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test.calibrate;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.test.KickSampleSkill;
import edu.tigers.sumatra.statemachine.AState;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author AndreR
 */
public abstract class AKickSamplerRole extends ARole
{
	protected static List<SamplePoint> samples = new ArrayList<>();
	private final boolean halfField;


	/**
	 * Constructor.
	 *
	 * @param role
	 * @param halfField
	 */
	protected AKickSamplerRole(final ERole role, final boolean halfField)
	{
		super(role);

		this.halfField = halfField;

		setInitialState(new DefaultState());
	}


	/**
	 * Get folder name for data collector.
	 *
	 * @return
	 */
	protected abstract String getFolderName();

	protected class SamplePoint
	{
		IVector2 kickPos;
		double targetAngle;
		double durationMs;
		EKickerDevice device;
		double rightOffset;
	}

	private class DefaultState extends AState
	{
		private KickSampleSkill skill;
		private boolean botLost = false;


		@SuppressWarnings("unchecked")
		private void startSample(final SamplePoint p)
		{
			skill = new KickSampleSkill(p.kickPos, p.targetAngle, p.device, p.durationMs, p.rightOffset);

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
			skill.setFolderName(getFolderName());

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
