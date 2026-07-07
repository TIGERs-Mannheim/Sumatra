package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.EPassType;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.targetrater.RotationTimeHelper;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * This class filters the Pass Targets
 */
@RequiredArgsConstructor
public class PassFilteringCalc extends ACalculator
{
	private final Supplier<Map<KickOrigin, List<RatedPass>>> ratedPasses;

	@Configurable(defValue = "0.08", unit = EConfigUnit.TIME_S)
	private static double alwaysAllowSmallRotationTimeThreshold = 0.08;

	@Configurable(defValue = "0.15", unit = EConfigUnit.TIME_S)
	private static double rotationTimeSafetyMargin = 0.15;

	@Getter
	private Map<KickOrigin, List<RatedPass>> filteredAndRatedPasses;


	@Override
	public void doCalc()
	{
		filteredAndRatedPasses = new HashMap<>();
		for (var entry : ratedPasses.get().entrySet())
		{
			var filtered = filterPasses(entry.getKey(), entry.getValue());
			filtered.forEach(this::drawRatedPass);
			filteredAndRatedPasses.put(entry.getKey(), filtered);
		}
	}


	private List<RatedPass> filterPasses(KickOrigin kickOrigin, List<RatedPass> sortedPasses)
	{
		List<RatedPass> filteredPasses = new ArrayList<>(sortedPasses.size());
		for (var pass : sortedPasses)
		{
			if (isOrientationReachableOnTime(kickOrigin, pass))
			{
				filteredPasses.add(pass);
			}
		}
		return filteredPasses;
	}


	private boolean isOrientationReachableOnTime(KickOrigin kickOrigin, RatedPass pass)
	{
		if (getBall().getVel().getLength() < 1.5)
		{
			// if ball vel is rather small we do not need to filter passes anymore
			return true;
		}

		if (kickOrigin.getShooter().isUninitializedID() || Double.isInfinite(kickOrigin.getImpactTime()))
		{
			return true;
		}

		var kickSource = pass.getPass().getKick().getSource();

		double targetAngle;
		if (pass.getPass().getReceiveMode() == EBallReceiveMode.RECEIVE)
		{
			targetAngle = getBall().getPos().subtractNew(kickSource).getAngle();
		} else
		{
			targetAngle = RedirectConsultantFactory.createDefault()
					.getTargetAngle(
							getBall(), kickSource, pass.getPass().getKick().getTarget(),
							pass.getPass().getKick().getKickVel().getLength2()
					);
		}

		double rotationTime = calcRotationTime(getWFrame().getTiger(kickOrigin.getShooter()), targetAngle);
		if (rotationTime < alwaysAllowSmallRotationTimeThreshold)
		{
			// micro movements are allowed since bot rotate slowly.
			// this is also needed so that a switch to chip kick to the same target is still possible even if the
			// impact time is already very small.
			return true;
		}

		double impactTime = kickOrigin.getImpactTime();
		if (rotationTime + rotationTimeSafetyMargin > impactTime)
		{
			getShapes(EAiShapesLayer.PASS_RATING)
					.add(new DrawableLine(
							kickSource, kickSource.addNew(Vector2.fromAngle(targetAngle).scaleToNew(300)),
							new Color(255, 0, 0, 180)
					));
			return false;
		}
		getShapes(EAiShapesLayer.PASS_RATING)
				.add(new DrawableLine(
						kickSource, kickSource.addNew(Vector2.fromAngle(targetAngle).scaleToNew(300)),
						new Color(71, 255, 0, 180)
				));

		return true;
	}


	private double calcRotationTime(ITrackedBot bot, final double angle)
	{
		return RotationTimeHelper.calcRotationTime(
				bot.getAngularVel(),
				bot.getAngleByTime(0),
				angle,
				bot.getMoveConstraints().getVelMaxW(),
				bot.getMoveConstraints().getAccMaxW()
		);
	}


	private void drawRatedPass(final RatedPass ratedPass)
	{
		var color = ratedPass.getPass().getType() == EPassType.THROUGH ? Color.YELLOW : new Color(0, 120, 100, 150);
		var target = ratedPass.getPass().getKick().getTarget();
		getShapes(EAiShapesLayer.PASS_SELECTION).add(new DrawableCircle(target, 30, color).setFill(true));

		boolean chip = ratedPass.getPass().getKick().getKickParams().getDevice() == EKickerDevice.CHIP;
		var text = (chip ? "ch: " : "st: ") +
				Arrays.stream(EPassRating.values()).map(p -> passRatingToStr(ratedPass, p))
						.collect(Collectors.joining(" | "));

		var receiveMode = ratedPass.getPass().getReceiveMode();
		var opponentPreparationTime = ratedPass.getPass().getOpponentPreparationTime();
		text = String.format(
				"%s | m:%s | pt:%.2f", text, receiveMode == EBallReceiveMode.REDIRECT ? "d" : "c", opponentPreparationTime);
		double offset = switch (ratedPass.getPass().getType())
		{
			case STRAIGHT -> 0.0;
			case CHIP -> 6.0;
			case THROUGH -> 12.0;
		};
		getShapes(EAiShapesLayer.PASS_RATING)
				.add(new DrawableAnnotation(target, text)
						.setColor(Color.black)
						.withFontHeight(6)
						.withCenterHorizontally(true)
						.withOffset(Vector2.fromXY(0, 33 + offset)));
	}


	private String passRatingToStr(RatedPass ratedPass, EPassRating passRating)
	{
		return passRating.getAbbreviation() + ":" + scoreToStr(ratedPass.getScore(passRating));
	}


	private String scoreToStr(final double passScore)
	{
		return Long.toString(Math.round(passScore * 100));
	}

}
