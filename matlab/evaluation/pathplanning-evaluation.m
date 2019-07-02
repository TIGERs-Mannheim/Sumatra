%% Load data
D_random = importdata('rnd.csv');
D_systematic = importdata('sys.csv');

%% filter first runs (warm ups)
D_random = D_random(4:end,:);
D_systematic = D_systematic(4:end,:);

%% plot data
figure
subplot(2,1,1)
title('simulation time')
plot(D_random(:,3))
hold on
plot(D_systematic(:,3))

subplot(2,1,2)
title('avg/max execution time')
plot(D_random(:,4))
hold on
plot(D_random(:,5))
plot(D_systematic(:,4))
plot(D_systematic(:,5))

%% hist

figure
hist(D_systematic(:,5))
ylabel('count')
xlabel('max execution time [ms]')

%% hist
figure
hist(D_systematic(:,4))
ylabel('count')
xlabel('avg execution time [ms]')

%% print statistics
fprintf('random\n')
fprintf('sim time -> %.2f (%.2f) | min: %.2f | max: %.2f\n', mean(D_random(:,3)), std(D_random(:,3)), min(D_random(:,3)), max(D_random(:,3)))
fprintf('processing time -> max: %.4f (%.4f) | avg: %.4f (%.4f)\n', mean(D_random(:,4)), std(D_random(:,4)), mean(D_random(:,5)), std(D_random(:,5)))

fprintf('\nsystematic\n')
fprintf('sim time -> %.2f (%.2f) | min: %.2f | max: %.2f\n', mean(D_systematic(:,3)), std(D_systematic(:,3)), min(D_systematic(:,3)), max(D_systematic(:,3)))
fprintf('processing time -> max: %.4f (%.4f) | avg: %.4f (%.4f)\n', mean(D_systematic(:,4)), std(D_systematic(:,4)), mean(D_systematic(:,5)), std(D_systematic(:,5)))
