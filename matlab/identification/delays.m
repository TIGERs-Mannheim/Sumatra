function params = delays(filename)

%% Load data
Ts = 0.00125;
csv = csvread(filename);

%% Calculate data loss
dataLoss = 1-size(csv,1)/((csv(end,2)-csv(1,2))*1e-6*800);

%% Identify unique samples (outVel and gyrVel)
[~, ia] = unique(csv(:,2));
csv = csv(ia, :);
csv(:,2) = csv(:,2)*1e-6;

%% Identify unique vision samples
% vis = [tCapture orient tNow]
vis = csv(:,[3 5 2]);
[~, ia] = unique(vis(:,1));
vis = vis(ia, :);
vis(:,1) = vis(:,1)*1e-6;

%% Calculate maximum vision/BS processing and transfer delay
tVisionProc = vis(:,3)-vis(:,1);
% Calculate 98 percentile of vision processing delay and add 5ms
visProcDelayMax = ceil(prctile(tVisionProc, 98)*1e3)+5;

%% Calc sample times for interpolation
tStart = roundn(csv(1,2)+0.1, -1);
tEnd = roundn(csv(end-1,2)-0.1, -1);

sampleTimes = (tStart:Ts:tEnd)';

%% Multiturn angle correction for vision data
posData = vis(:,2);
correctionTurns = 0;
newOrient = zeros(length(posData), 1);
newOrient(1) = posData(1);
for i = 2:length(posData)
    angleChange = posData(i-1)-posData(i);
    if angleChange > 1.5*pi
        correctionTurns = correctionTurns + 1;
    end
    if angleChange < -1.5*pi
        correctionTurns = correctionTurns - 1;
    end

    newOrient(i) = posData(i)+correctionTurns*2*pi;
end
vis(:,2) = newOrient;

%% Interpolate data to 1kHz
visPos = interp1(vis(:,1), vis(:,2), sampleTimes);
outVel = interp1(csv(:,2), csv(:,4), sampleTimes);
gyrVel = interp1(csv(:,2), csv(:,6), sampleTimes);

%% Get vision velocity
visVel = [0; diff(visPos)]/0.00125;

%% Shift data with 1ms steps to align sensor data to outVel
maxShift = 50;
shiftFit = zeros(maxShift,2);

for shift = 0:maxShift-1
    gyrVelShifted = circshift(gyrVel,-shift);
    visVelShifted = circshift(visVel,-shift);
    
    shiftFit(shift+1,1) = sum(abs(gyrVelShifted(100:end-100)-outVel(100:end-100)));
    shiftFit(shift+1,2) = sum(abs(visVelShifted(100:end-100)-outVel(100:end-100)));
end

%% Scale fit to 0-1
shiftFit(:,1) = shiftFit(:,1)./max(shiftFit(:,1));
shiftFit(:,2) = shiftFit(:,2)./max(shiftFit(:,2));

%% get minimum error index (=> sensor delay)
% delays are [ms] from control output to sensor reaction
[~, gyrDelay] = min(shiftFit(:,1));
[~, visDelay] = min(shiftFit(:,2));

params = [visDelay visProcDelayMax gyrDelay dataLoss];

%% optional plotting
% plot(sampleTimes, outVel, 'r-', sampleTimes, gyrVel, 'g-', sampleTimes, smooth(visVel,50), 'c-');
% axis tight;
% grid on;
% grid minor;
% 
% plot(sampleTimes, outVel, 'r-', ...
%     sampleTimes, circshift(gyrVel,-gyrDelay), 'g-', ...
%     sampleTimes, smooth(circshift(visVel,-visDelay),50), 'c-');
% axis tight;
% grid on;
% grid minor;
% 
% plotyy(1:maxShift, shiftFit(:,1), 1:maxShift, shiftFit(:,2));
