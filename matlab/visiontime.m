close all; clear;

% ls ../logs/visiontime*
T=importdata('../logs/visiontime12.csv');
T = T(200:end,:);

dt = (T(2:end,1) - T(1:end-1,1)) /1e6;

time = (T(:,1) - T(1,1)) / 1e9;

rate = ((T(end,4)-T(1,4))/1e6) / ((T(end,1)-T(1,1))/1e9);
fprintf('%f ms/s\n',rate);

figure
plot(dt)
% plot(T(:,1),T(:,2))
ylabel('dt [ms]');

figure
plot(time, T(:,3));
hold all;
plot(time, T(:,4));
title('diff');
ylabel('diff [ns]');
xlabel('time [s]')