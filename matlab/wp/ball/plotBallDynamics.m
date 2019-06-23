baseFolder = '../data/ball/ballDynamicsTest/';
rawBots = data.loadRawBots(strcat(baseFolder, 'rawBots.csv'));
simBall = data.loadWpBall(strcat(baseFolder, 'simBall.csv'));

close all

time = simBall.timestamp;
pos = simBall.pos;
vel = simBall.vel;
acc = simBall.acc;

velAbs = sqrt(vel(:,1) .* vel(:,1) + vel(:,2) .* vel(:,2));
accAbs = sqrt(acc(:,1) .* acc(:,1) + acc(:,2) .* acc(:,2));

botPos = rawBots.pos(1,:);

figure
hold all;
plot(pos(:,1),pos(:,2), '.');
% plot([pos(1,1) pos(end,1)], [pos(1,2), pos(end,2)]);
plotBot(botPos);
axis equal
title('pos');

%%
figure
hold all;
plot(vel(:,1),vel(:,2), '.');
axis equal
title('vel');

%%
figure
hold all;
plot(time, vel(:,1:2));
plot(time, velAbs);
legend('x','y','abs');
title('time -> vel');

%%
figure
hold all;
plot(time, acc(:,1:2));
plot(time, accAbs);
legend('x','y','abs');
title('time -> acc');