package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.kicking.EPassType;
import edu.tigers.sumatra.ai.metis.kicking.Pass;


public class ReceivabilityRater implements IPassRater
{
	@Configurable(comment = "Allow the experimental through pass feature", defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean allowThroughPasses = false;

	@Configurable(comment = "ALWAYS KEEP THIS ON - Deactivating this will disallow normal passes", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean allowStoppingPasses = true;

	static
	{
		ConfigRegistration.registerClass("metis", ReceivabilityRater.class);
	}

	@Override
	public double rate(Pass pass)
	{
		if (pass.getType() == EPassType.THROUGH)
		{
			return allowThroughPasses ? 1.0 : 0.0;
		}

		return allowStoppingPasses ? 1.0 : 0.0;
	}
}
