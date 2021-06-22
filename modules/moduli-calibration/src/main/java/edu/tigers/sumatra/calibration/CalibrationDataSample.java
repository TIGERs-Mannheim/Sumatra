/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration;

import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang.Validate;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


@Value
@Builder
public class CalibrationDataSample implements IExportable
{
	long timestamp;
	IVector2 ballPos;
	IVector2 rawBallPos;
	double ballSpeed;
	double detectedKickSpeed;
	IVector2 botPos;
	double botOrientation;
	IVector2 botKickerPos;
	boolean hasBallContact;
	EKickerDevice kickerDevice;
	double kickSpeed;
	IVector2 ballVel;
	IVector2 kickPos;
	IVector3 kickVel;
	long kickTimestamp;


	public static CalibrationDataSample fromInput(CalibrationData input)
	{
		State botState = input.getBot().getFilteredState().orElse(input.getBot().getBotState());
		return CalibrationDataSample.builder()
				.timestamp(input.getTimestamp())
				.ballPos(input.getBall().getPos())
				.rawBallPos(input.getRawBallPos())
				.ballSpeed(input.getBall().getVel().getLength2())
				.detectedKickSpeed(Optional.ofNullable(input.getKickFitState())
						.map(BallKickFitState::getAbsoluteKickSpeed).orElse(0.0))
				.botPos(botState.getPos())
				.botOrientation(botState.getOrientation())
				.botKickerPos(input.getBot().getBotKickerPos())
				.hasBallContact(input.getBot().getBallContact().hasContact())
				.kickerDevice(input.getKickParams().getDevice())
				.kickSpeed(input.getKickParams().getKickSpeed())
				.ballVel(input.getBall().getVel())
				.kickPos(Optional.ofNullable(input.getKickFitState())
						.map(BallKickFitState::getKickPos).orElse(null))
				.kickVel(Optional.ofNullable(input.getKickFitState())
						.map(BallKickFitState::getKickVel)
						.orElse(null)
				)
				.kickTimestamp(Optional.ofNullable(input.getKickFitState())
						.map(BallKickFitState::getKickTimestamp).orElse(0L))
				.build();
	}


	public static CalibrationDataSample fromValues(List<String> values)
	{
		int i = 0;
		var sampleBuilder = CalibrationDataSample.builder()
				.timestamp(Long.parseLong(values.get(i++)))
				.ballPos(Vector2f.fromXY(
						Double.parseDouble(values.get(i++)),
						Double.parseDouble(values.get(i++))
				))
				.rawBallPos(Vector2f.fromXY(
						Double.parseDouble(values.get(i++)),
						Double.parseDouble(values.get(i++))
				))
				.ballSpeed(Double.parseDouble(values.get(i++)))
				.detectedKickSpeed(Double.parseDouble(values.get(i++)))
				.botPos(Vector2f.fromXY(
						Double.parseDouble(values.get(i++)),
						Double.parseDouble(values.get(i++))
				))
				.botOrientation(Double.parseDouble(values.get(i++)))
				.botKickerPos(Vector2f.fromXY(
						Double.parseDouble(values.get(i++)),
						Double.parseDouble(values.get(i++))
				))
				.hasBallContact("1".equals(values.get(i++)))
				.kickerDevice("1".equals(values.get(i++)) ? EKickerDevice.CHIP : EKickerDevice.STRAIGHT)
				.kickSpeed(Double.parseDouble(values.get(i++)));

		if (values.size() > i)
		{
			sampleBuilder.ballVel(Vector2f.fromXY(
					Double.parseDouble(values.get(i++)),
					Double.parseDouble(values.get(i++))
			));
			Vector2f kickPos = Vector2f.fromXY(
					Double.parseDouble(values.get(i++)),
					Double.parseDouble(values.get(i++))
			);
			if (kickPos.isFinite() && !kickPos.isZeroVector())
			{
				sampleBuilder.kickPos(kickPos);
			}
			Vector3f kickVel = Vector3f.fromXYZ(
					Double.parseDouble(values.get(i++)),
					Double.parseDouble(values.get(i++)),
					Double.parseDouble(values.get(i++))
			);
			if (kickVel.isFinite() && !kickVel.isZeroVector())
			{
				sampleBuilder.kickVel(kickVel);
			}
			if (values.size() > i)
			{
				sampleBuilder.kickTimestamp(Long.parseLong(values.get(i++)));
			}
		}

		Validate.isTrue(values.size() == i, "Read all values: ", i);
		return sampleBuilder.build();
	}


	@Override
	public List<Number> getNumberList()
	{
		return List.of(
				timestamp,
				ballPos.x(),
				ballPos.y(),
				nullSafe(rawBallPos, IVector2::x),
				nullSafe(rawBallPos, IVector2::y),
				ballSpeed,
				detectedKickSpeed,
				botPos.x(),
				botPos.y(),
				botOrientation,
				botKickerPos.x(),
				botKickerPos.y(),
				hasBallContact ? 1 : 0,
				kickerDevice == EKickerDevice.STRAIGHT ? 0 : 1,
				kickSpeed,
				ballVel.x(),
				ballVel.y(),
				nullSafe(kickPos, IVector2::x),
				nullSafe(kickPos, IVector2::y),
				nullSafe(kickVel, IVector3::x),
				nullSafe(kickVel, IVector3::y),
				nullSafe(kickVel, IVector3::z),
				kickTimestamp
		);
	}


	private <T> double nullSafe(T obj, Function<T, Double> getter)
	{
		if (obj == null)
		{
			return Double.NaN;
		}
		return getter.apply(obj);
	}
}
