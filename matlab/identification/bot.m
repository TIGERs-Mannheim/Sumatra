function params = bot(filename)

%% Load data
Ts = 0.001;
csv = csvread(filename);

%% Calculate data loss
dataLoss = 1-size(csv,1)/((csv(end,2)-csv(1,2))*1e-6*500);

%% Identify unique samples (outVel)
[~, ia] = unique(csv(:,2));
csv = csv(ia, :);
csv(:,2) = csv(:,2)*1e-6;

%% Identify unique vision samples
vis = csv(:,[3 7:9]);
[~, ia] = unique(vis(:,1));
vis = vis(ia, :);
vis(:,1) = vis(:,1)*1e-6;

%% Calc sample times for interpolation
tStart = roundn(csv(1,2)+0.1, -1);
tEnd = roundn(csv(end-1,2)-0.1, -1);

sampleTimes = (tStart:Ts:tEnd)';
numSamples = length(sampleTimes);

%% Multiturn angle correction for vision data
posData = vis(:,4);
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
vis(:,4) = newOrient;

outVel = zeros(numSamples, 3);
visPos = zeros(numSamples, 3);
visVel = zeros(numSamples, 3);

%% Interpolate data to 1kHz and get vision velocity
for i = 1:3
    visPos(:,i) = interp1(vis(:,1), vis(:,i+1), sampleTimes);
    outVel(:,i) = interp1(csv(:,2), csv(:,i+3), sampleTimes);
    visVel(:,i) = [0; diff(visPos(:,i))/Ts];
end

%% Rotate to vision velocity local frame
for i = 1:numSamples
    angle = pi/2-visPos(i,3);
    rotMat = [cos(angle) -sin(angle); sin(angle) cos(angle)];
    visVel(i,1:2) = (rotMat*(visVel(i,1:2)'))';
end

%% Fitting
fOpt = @(x)(botModel2D(outVel(:,1:2), x)-visVel(:,1:2));

opt = optimoptions(@lsqnonlin, 'Display', 'off');
x = lsqnonlin(fOpt, ...
    [0.8, 0.1, 0.01, 1,0.1, 0.01], ...
    [0, 0, 0, 0.1, 0, 0], [], opt);

predVel = botModel2D(outVel(:,1:2), x);

sqErrX = mean(abs(predVel(:,1)-visVel(:,1)));
sqErrY = mean(abs(predVel(:,2)-visVel(:,2)));

predVelAbs = sqrt(predVel(:,1).^2+predVel(:,2).^2);
visVelAbs = sqrt(visVel(:,1).^2+visVel(:,2).^2);
sqErr = mean(abs(predVelAbs-visVelAbs));

params = [x sqErrX sqErrY sqErr dataLoss];

return;

%% Plotting %%
figure(1);
sp1 = subplot(2,1,2);
plot(sampleTimes, predVel(:,2), 'g--', ...
    sampleTimes, smooth(visVel(:,2),50), 'c-', ...
    sampleTimes, outVel(:,2), 'r:');
legend('Model', 'Vision', 'Traj');
title('Y Velocities (Encoders+Accelerometer+Vision)');
xlabel('t [s]');
ylabel('v [m/s]');
axis tight
grid on
grid minor
ylim([-5 5]);

sp2 = subplot(2,1,1);
plot(sampleTimes, predVel(:,1), 'g--', ...
    sampleTimes, smooth(visVel(:,1),50), 'c-', ...
    sampleTimes, outVel(:,1), 'r:');
legend('Model', 'Vision', 'Traj');
title('X Velocities (Encoders+Accelerometer+Vision)');
xlabel('t [s]');
ylabel('v [m/s]');
axis tight
grid on
grid minor
ylim([-5 5]);

linkaxes([sp1, sp2], 'x');

function predVel = botModel2D( trajVel, x)
    predVel = zeros(numSamples, 2);
    prevVel = [0 0];

    for j = 1:numSamples
        v = trajVel(j,:);

        Kx = x(1);
        Tx = norm(v)*x(2)+x(3);
        
        Ky = x(4);
        Ty = norm(v)*x(5)+x(6);

        T2x = 1/((Tx/Ts)+1);
        T2y = 1/((Ty/Ts)+1);

        predVel(j,1) = T2x*(v(1)*Kx-prevVel(1))+prevVel(1);
        predVel(j,2) = T2y*(v(2)*Ky-prevVel(2))+prevVel(2);
        prevVel = predVel(j,:);
    end

end

end
