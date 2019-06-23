function [angle, receiveSpeed, kickSpeed, angleQuality, speedQuality] = detectAngle(D, doPlot)
%DETECTANGLE detect the redirect angle

if ~exist('doPlot', 'var')
    doPlot = false;
end

angle = inf; 
receiveSpeed = 0;
kickSpeed = 0;
angleQuality = -1;
speedQuality = -1;

%% generate directions
if isfield(D, 'rawBall')
    ball = D.rawBall;
else
    ball = D.wpBall;
end

direction = atan2(ball.vel(:,2), ball.vel(:,1));

%% filter out non-relevant data
validIdx = ball.velAbs>1.0;
t = ball.time(validIdx);
dir = direction(validIdx);
vel = ball.velAbs(validIdx);

for i=1:20
validIdx = diff([t;inf]) < 0.1;
t = t(validIdx);
dir = dir(validIdx);
vel = vel(validIdx);
end

s=5;
fw_filter = zeros(length(dir),1);
for i=1:length(dir)-s
    fw_filter(i) = abs(dir(i) - trimmean(dir(i:i+s), 30)) < 0.1;
end
bw_filter = zeros(length(dir),1);
for i=1+s:length(dir)
    bw_filter(i) = abs(dir(i) - trimmean(dir(i-s:i), 30)) < 0.1;
end
full_filter = fw_filter + bw_filter;
t = t(full_filter>0);
dir = dir(full_filter>0);
vel = vel(full_filter>0);

%% find changes in directions
changes = find(abs(diff(dir)) > 0.5);

%% find mean values of directions and detect kick speeds
steps = [changes; length(dir)];
% remove small steps
steps = steps(diff([1; steps]) > 10);
if length(steps) > 1
    start = 1;
    meanDirections = zeros(length(steps), 1);
    initSpeeds = zeros(length(steps),1);
    finalSpeeds = zeros(length(steps),1);
    qualities = zeros(length(steps),1);
    rmses = zeros(length(steps),1);
    fittedDirs = zeros(length(dir), 1);
    fittedSpeeds = zeros(length(dir),1);
    for i=1:length(steps)
        stop = steps(i);
        meanDir = trimmean(dir(start+1:stop-1), 10);
        [initSpeed, finalSpeed, rmse, fittedLines] = detectBallSpeedUsingModel(t(start:stop), vel(start:stop));
        initSpeeds(i) = initSpeed;
        finalSpeeds(i) = finalSpeed;
        rmses(i) = rmse;
        meanDirections(i) = meanDir;
        fittedDirs(start:stop) = meanDir;
        fittedSpeeds(start:stop) = fittedLines;
        qualities(i) = trimmean(abs(meanDir - dir(start+1:stop-1)),10);
        start = stop;
    end
    angle = abs(util.math.shortestRotation(meanDirections(1) + pi, meanDirections(2)));
    receiveSpeed = finalSpeeds(1);
    kickSpeed = initSpeeds(2);
    angleQuality = mean(qualities);
    speedQuality = mean(rmses);
end

%% optional plotting stuff

if doPlot
%% plot it

    clf
    subplot(2,1,1)
    plot(t,dir);
    hold all;
    plot(t(1:end-1),diff(dir))
    plot(t(changes), dir(changes), '*');
    plot(t, fittedDirs);
    legend('direction', 'delta direction', 'changes', 'fitted direction');
    xlabel('time [s]')
    ylabel('angle [rad]');
    
    subplot(2,1,2)
    plot(t,vel);
    hold all;
    plot(t(changes), vel(changes), '*');
    plot(t(changes(1)), receiveSpeed, 'x');
    plot(t(changes(1)), kickSpeed, 'o');
    plot(t, fittedSpeeds)
    legend('vel', 'changes', 'receiveSpeed', 'kickSpeed', 'fitted speeds');
    xlabel('time [s]')
    ylabel('ball speed [m/s]')

%%
end

end

