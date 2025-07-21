/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.drawable.animated.AnimationTimerSine;
import edu.tigers.sumatra.drawable.animated.AnimationTimerUp;
import edu.tigers.sumatra.drawable.animated.ColorAnimatorFixed;
import edu.tigers.sumatra.drawable.animated.NumberAnimatorMinMax;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Add a pass receiver if required
 */
@RequiredArgsConstructor
public class PassReceiverCalc extends ACalculator
{
	@Configurable(comment = "[s] time to keep pass receivers active", defValue = "0.3")
	private static double timeToKeepPastPassReceiversActive = 0.3;

	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;
	private final Supplier<Pass> keeperPass;
	private Map<BotID, PassWithTimestamp> passReceivingRobotStartTimes = new HashMap<>();

	/**
	 * This is the main offensive pass receiver that has a high role priority.
	 * Usually only one offensive pass receiver is active, since only one announced pass exists (most of the time).
	 * In some rare cases multiple passes and thus pass receivers may exist. E.g. if multiple
	 * ball intercepting offensive bots try to catch a rolling ball!
	 */
	@Getter
	private List<BotID> passReceivers;

	/**
	 * These pass receivers will activate a SupportBehavior. Meaning Support
	 * robots will prepare themselves to receive these passes. Supporters have
	 * low role priority.
	 */
	@Getter
	private Map<BotID, Pass> supportPassReceivers = new HashMap<>();


	@Override
	public void doCalc()
	{
		var newPassReceivers = new ArrayList<BotID>();
		Optional.ofNullable(keeperPass.get()).ifPresent(pass -> newPassReceivers.add(pass.getReceiver()));

		var passList = offensiveActions.get().values().stream()
				.filter(this::isPassAction)
				.map(e -> e.getAction().getPass())
				.filter(e -> e.getReceiver().isBot())
				.toList();

		passList.forEach(e -> getShapes(EAiShapesLayer.OFFENSE_PASSING).add(
				new AnimatedCrosshair(e.getKick().getTarget(),
						new NumberAnimatorMinMax(30, 150, new AnimationTimerSine(Math.PI)),
						new NumberAnimatorMinMax(0, Math.PI * 2.0, new AnimationTimerUp(2)),
						new ColorAnimatorFixed(Color.BLACK),
						new ColorAnimatorFixed(new Color(255, 255, 255, 0))
				)));
		passList.forEach(e -> getShapes(EAiShapesLayer.OFFENSE_PASSING).add(
				new AnimatedCrosshair(e.getKick().getTarget(),
						new NumberAnimatorMinMax(40, 210, new AnimationTimerSine(Math.PI)),
						new NumberAnimatorMinMax(0, Math.PI * 2.0, new AnimationTimerUp(2)),
						new ColorAnimatorFixed(Color.RED),
						new ColorAnimatorFixed(new Color(255, 255, 255, 0))
				)));

		for (var pass : passList)
		{
			passReceivingRobotStartTimes.put(pass.getReceiver(),
					new PassWithTimestamp(pass, getWFrame().getTimestamp()));
		}
		passList.stream().map(Pass::getReceiver).forEach(newPassReceivers::add);
		passReceivingRobotStartTimes.keySet().removeIf(id -> !getWFrame().getBots().containsKey(id));

		supportPassReceivers = passReceivingRobotStartTimes.entrySet()
				.stream()
				.filter(e -> getWFrame().getTimestamp() - e.getValue().timestamp < timeToKeepPastPassReceiversActive * 1e9)
				.collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> e.getValue().pass()));

		// draw support pass receivers
		supportPassReceivers.keySet().forEach(
				e -> getShapes(EAiShapesLayer.SUPPORT_PASS_RECEIVER).add(new DrawableCircle(
						Circle.createCircle(getWFrame().getBot(e).getPos(), 150)).setFill(true).setColor(new Color(195, 0,
						255, 119))
				));

		passReceivers = Collections.unmodifiableList(newPassReceivers);
	}


	private boolean isPassAction(RatedOffensiveAction a)
	{
		return a.getAction().getPass() != null;
	}


	private record PassWithTimestamp(Pass pass, Long timestamp)
	{

	}


}
