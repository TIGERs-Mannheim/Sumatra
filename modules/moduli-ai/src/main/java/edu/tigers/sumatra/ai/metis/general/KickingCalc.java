/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Optional;


/**
 * This calculator recognise ball kicks
 *
 * @author Stefan Schneyer
 */
public class KickingCalc extends ACalculator
{
    @Configurable(comment = "minKickAcc", defValue = "1.0")
    private static double minKickAcc              = 1.0;
    @Configurable(comment = "maxDeviationBallToOrientationAngle", defValue = "0.3")
    private static double maxDevBallToOrientation = 0.3;
    @Configurable(comment = "minDeviationBallAccelerationToOrientation", defValue = "1.0")
    private static double maxDevAccToOrientation = 1.0;


    private IVector2 lastBallVelocity;

    @Override
    public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
    {
        //Ball
        final ITrackedBall ball = baseAiFrame.getWorldFrame().getBall();
        final IVector2 ballAcc = getBallAcc(ball);

        //Touched Bot
        final BotID touchedBotID = newTacticalField.getBotTouchedBall();
        final ITrackedBot touchedBot = baseAiFrame.getWorldFrame().getBot(touchedBotID);


        Optional<IKickEvent> kickingEvent = Optional.empty();

        boolean ballTouchesBot  = touchedBotID != null;
        boolean ballKickAcc     = ballAcc.getLength() > minKickAcc && ball.getVel().getLength() > lastBallVelocity.getLength();

        if(ballTouchesBot && ballKickAcc)
        {
            double ballAngleToBot = Vector2.fromPoints(touchedBot.getPos(), ball.getPos()).getAngle();
            double devAccToOrientation = Math.abs(AngleMath.normalizeAngle(touchedBot.getOrientation() - ballAcc.getAngle()));
            double devBallToOrientation = Math.abs(AngleMath.normalizeAngle(touchedBot.getOrientation() - ballAngleToBot));

            boolean correctOrientation = devBallToOrientation < maxDevBallToOrientation && devAccToOrientation < maxDevAccToOrientation;
            if(correctOrientation) {
                kickingEvent = Optional.of(new KickEvent(ball.getPos(), touchedBotID, baseAiFrame.getWorldFrame().getTimestamp()));
            }

        }

        newTacticalField.setKicking(kickingEvent);
        lastBallVelocity = ball.getVel();
    }

    private IVector2 getBallAcc(ITrackedBall ball)
    {
        IVector2 ballAcc;
        if(lastBallVelocity == null) {
            ballAcc = Vector2.zero();
            lastBallVelocity = Vector2.zero();
        } else {
            ballAcc = ball.getVel().subtractNew(lastBallVelocity);
        }
        return ballAcc;
    }

    class KickEvent implements IKickEvent
    {
        private IVector2 position;
        private BotID   kickingBot;
        private long    timestamp;

        public KickEvent(IVector2 position, BotID kickingBot, long timestamp)
        {
            this.position = position;
            this.kickingBot = kickingBot;
            this.timestamp = timestamp;
        }

        @Override
        public IVector2 getPosition() {
            return this.position;
        }

        @Override
        public BotID getKickingBot() {
            return this.kickingBot;
        }

        @Override
        public long getTimestamp() {
            return this.timestamp;
        }
    }
}
