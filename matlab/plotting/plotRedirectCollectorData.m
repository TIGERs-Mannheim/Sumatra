% D = importdata('../data/redirect-data/2020-11-26_19-00-12-764_live.csv');
D = importdata('../data/redirect-data-labeled/2020-11-26_19-08-48-918_runner.csv');

timestamp = (D(:,1) - D(1,1)) / 1e9;
ballPos_x = D(:,2);
ballPos_y = D(:,3);
rawBallPos_x = D(:,4);
rawBallPos_y = D(:,5);
ballSpeed = D(:,6);
detectedKickSpeed = D(:,7);
botPos_x = D(:,8);
botPos_y = D(:,9);
botOrientation = D(:,10);
botKickerPos_x = D(:,11);
botKickerPos_y = D(:,12);
hasBallContact = D(:,13);
kickerDevice = D(:,14);
kickSpeed = D(:,15);
ballVel_x = D(:,16);
ballVel_y = D(:,17);
kickPos_x = D(:,18);
kickPos_y = D(:,19);
kickVel_x = D(:,20);
kickVel_y = D(:,21);
kickVel_z = D(:,22);
kickTimestamp = D(:,23);

if size(D,2) > 23
    sample_id = D(:,24);
    sample_side = D(:,25);
else
    sample_id = zeros(size(D, 1), 1);
    sample_side = zeros(size(D, 1), 1);
end

%%

figure(2)
subplot(2,2,1)
plot(timestamp, kickSpeed)
hold on
plot(timestamp, ballSpeed)
plot(timestamp, detectedKickSpeed)
hold off
title('t -> speed')
legend('kick', 'speed', 'detectedKick')

subplot(2,2,2)
plot(ballPos_x, ballPos_y, '.', 'MarkerSize', 3)
hold on
plot(rawBallPos_x, rawBallPos_y, '.', 'MarkerSize', 3)
plot(kickPos_x, kickPos_y, '*', 'MarkerSize', 3)
hold off
axis equal
title('x -> y')

subplot(2,1,2)
plot(timestamp, kickPos_x)
hold on
plot(timestamp, kickPos_y)
hold off
title('kick pos')

%%
figure(3)
clf
for i=0:max(sample_id)
   idx_pre = (sample_id == i) & (sample_side == 0);
   idx_post = (sample_id == i) & (sample_side == 1);
   ballPos_x_pre = ballPos_x(idx_pre);
   ballPos_y_pre = ballPos_y(idx_pre);
   ballPos_x_post = ballPos_x(idx_post);
   ballPos_y_post = ballPos_y(idx_post);
   plot(ballPos_x_pre, ballPos_y_pre, '.', 'MarkerSize', 2)
   hold on
   plot(ballPos_x_post, ballPos_y_post, '.', 'MarkerSize', 2)
%    plot(rawBallPos_x(idx_pre), rawBallPos_y(idx_pre), '.', 'MarkerSize', 3)
%    plot(rawBallPos_x(idx_post), rawBallPos_y(idx_post), '.', 'MarkerSize', 3)
   kickX = kickPos_x(idx_post);
   kickY = kickPos_y(idx_post);
   plot(kickX(end), kickY(end), '*', 'MarkerSize', 3)
   plot(kickX(1), kickY(1), '+', 'MarkerSize', 3)
   botPosX = botPos_x(idx_pre);
   botPosY = botPos_y(idx_pre);
   botAngle = botOrientation(idx_pre);
   plotBot([botPosX(end), botPosY(end), botAngle(end)]);
   plot([ballPos_x_pre(1), ballPos_x_pre(end)], [ballPos_y_pre(1), ballPos_y_pre(end)])
   plot([ballPos_x_post(1), ballPos_x_post(end)], [ballPos_y_post(1), ballPos_y_post(end)])
   hold off
   pause
end

%%
figure(4)
plot(timestamp, ballPos_x)