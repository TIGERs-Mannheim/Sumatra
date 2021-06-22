D = importdata('../data/redirect-samples/2020-11-26_19-00-25-525_live.csv');

dirIn = D(:,1);
dirOut = D(:,2);
angle = D(:,3);
angleIn = D(:,4);
angleOut = D(:,5);
angleDiff = D(:,6);
angleDiffFactor = D(:,7);
kickSpeed = D(:,8);
speedIn = D(:,9);
speedOut = D(:,10);
meanDiffPre = D(:,11);
meanDiffPost = D(:,12);
stdDevDiffPre = D(:,13);
stdDevDiffPost = D(:,14);

angles=linspace(0, pi/2);
speeds=linspace(0, 4);

p = [0.0;0.20143;0.0;0.0];

f = @(angle,vIn,vOut) p(1) + p(2) .* angle + p(3) .* vIn + p(4) .* vOut;

avgSpeedOut = mean(speedOut);
avgSpeedIn = mean(speedIn);

%%

figure(1)
clf

subplot(2,2,1)
plot(rad2deg(angle), rad2deg(angleOut), '.')
hold on
plot(rad2deg(angles), f(rad2deg(angles), 0, 0))
hold off
title('angle -> angle_{out}')

subplot(2,2,2)
plot(speedIn, rad2deg(angleOut), '.')
title('speed -> angle_{out}')

subplot(2,2,3)
plot3(rad2deg(angle), speedIn, rad2deg(angleOut), '.')
hold on
[X,Y] = meshgrid(angles,speeds);
Z = f(X, Y, avgSpeedOut);
surf(rad2deg(X),Y,rad2deg(Z))
hold off
title('fixed speedOut')
xlabel('angle in')
ylabel('speed')
zlabel('angle out')


subplot(2,2,4)
plot3(rad2deg(angle), speedOut, rad2deg(angleOut), '.')
hold on
[X,Y] = meshgrid(angles,speeds);
Z = f(X, avgSpeedIn, Y);
surf(rad2deg(X),Y,rad2deg(Z))
hold off
title('fixed speedIn')
xlabel('angle in')
ylabel('speed')
zlabel('angle out')

%%

figure(2)

subplot(2,1,1)
plot(rad2deg(angle))
hold on
plot(rad2deg(angleOut))
hold off
title('angle')
legend('angle', 'angleOut')

subplot(2,1,2)
plot(kickSpeed)
hold on
plot(speedIn)
plot(speedOut)
plot([1, size(D,1)], [avgSpeedIn, avgSpeedIn])
plot([1, size(D,1)], [avgSpeedOut, avgSpeedOut])
hold off
title('speed')
legend('kick', 'in', 'out', 'in_{avg}', 'out_{avg}')

%%

figure(3)
subplot(2,1,1)
plot(angle./angleOut)
title('angle / angle_{out}')

subplot(2,1,2)
speedDiff=kickSpeed-speedOut;
speedDiffAvg=mean(speedDiff);
plot(speedDiff)
hold on
plot([0, size(speedDiff,1)], [speedDiffAvg, speedDiffAvg])
hold off
title('kickSpeed - speed_{out}')


%%
figure(4)
plot(meanDiffPre)
hold on
plot(meanDiffPost)
hold off
title('mean diff ball pos to line')
legend('pre', 'post')

%%
figure(5)
plot(speedOut, kickSpeed, '.')
hold on
plot(speedIn, speedOut, '.')
plot(speedIn, kickSpeed, '.')
plot(speedIn+speedOut, kickSpeed, '.')
hold off
