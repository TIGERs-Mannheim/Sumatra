figure
folderName = '../data/timeSeries/moduli_nicolai/full/2018-04-05_22-26-28-685_10Y';
D=data.loadAll(folderName);
[redirectAngle, receiveSpeed, kickSpeed, angleQuality, speedQuality] = detectAngle(D, true);
desiredAngle = D.info.desiredRedirectAngle;
if isfield(D.info, 'desiredKickSpeed')
    desiredKickSpeed = D.info.desiredKickSpeed;
else
    desiredKickSpeed = 6.5;
end
fprintf('%s: angle: %6.3f == %6.3f (diff:%6.3f | quality:%6.3f) | recv: %6.3f | speed: %6.3f == %6.3f (diff:%6.3f | quality:%6.3f)\n', ...
              folderName, ...
              desiredAngle, redirectAngle, redirectAngle - desiredAngle, angleQuality, ...
              receiveSpeed, ...
              desiredKickSpeed, kickSpeed, kickSpeed - desiredKickSpeed, speedQuality);

%% plot data set (direction and absolute ball velocity
figure
if isfield(D, 'rawBall')
    ball = D.rawBall;
else
    ball = D.wpBall;
end
direction = atan2(ball.vel(:,2), ball.vel(:,1));
plot(ball.time, direction);
hold all;
plot(ball.time, ball.velAbs);
legend('direction', 'velAbs');
xlabel('time [s]')
ylabel('angle [rad] | speed [m/s]')