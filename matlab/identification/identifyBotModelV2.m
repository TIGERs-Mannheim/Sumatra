function [result] = identifyBotModelV2(varargin)
%IDENTIFYBOTMODELV2 Summary of this function goes here
%   Detailed explanation goes here

if sum(size(varargin)) == 2
    % called via matlab
    filenames = varargin{1};
else
    % called via Sumatra
    filenames = varargin;
end

% data = 
% 1: botId
% 2: timestamp
% 3:5 velX velY velW
% 6:8 encVelX encVelY encVelW 
% 9:11 out force/torque
% 12: effXY (deprecated)
% 13: effW (deperecated)
% 14: modeXY (1: local force, 2: stopped, 3: plateau, 4: accel, 5: deccel)
% 15: modeW
% 16: Robot Model (2016 or 2020)

%% load and classify data
numFiles = length(filenames);

datasets = cell(numFiles,3);

numXSamples = 0;
numYRolloutSamples = 0;
numYSamples = 0;
numWSamples = 0;

figure('Name', 'Identify Bot Model Datasets', 'NumberTitle', 'Off');
m = uimenu('Label', 'Dataset');
uimenu(m, 'Label', 'Results', 'MenuSelectedFcn', @showResults);
m1 = uimenu(m, 'Label', 'Y Move (Rollout)');
m4 = uimenu(m, 'Label', 'Y Move');
m2 = uimenu(m, 'Label', 'X Move');
m3 = uimenu(m, 'Label', 'W Move');

for i = 1:numFiles
    data = csvread(filenames{i});
    [~, fname] = fileparts(filenames{i});

    modeXY = data(:,14);
    modeW = data(:,15);
    stateVel = data(:,3:5);

    type = 0;

    if sum(modeXY == 3) > 10
        meanVel = mean(stateVel);

        if abs(meanVel(1)) > abs(meanVel(2))
            % X move sample
            type = 2;
            numXSamples = numXSamples + 1;
            uimenu(m2, 'Label', strcat(string(i), ': ', fname), 'MenuSelectedFcn', {@showDataset, i});
        else
            % Y move sample
            if sum(modeXY == 1) > 10
                type = 1;
                numYRolloutSamples = numYRolloutSamples + 1;
                uimenu(m1, 'Label', strcat(string(i), ': ', fname), 'MenuSelectedFcn', {@showDataset, i});
            else
                type = 4;
                numYSamples = numYSamples + 1;
                uimenu(m4, 'Label', strcat(string(i), ': ', fname), 'MenuSelectedFcn', {@showDataset, i});
            end
        end
    end

    if sum(modeW == 3) > 10
        % rotation sample
        type = 3;
        numWSamples = numWSamples + 1;
        uimenu(m3, 'Label', strcat(string(i), ': ', fname), 'MenuSelectedFcn', {@showDataset, i});
    end

    datasets{i,1} = data;
    datasets(i,2) = {type};
end

%% Robot parameters
robotVersion = datasets{1,1}(1,16);

if robotVersion == 2016
    botMass = 2.65;
    botRadius = 0.083;
    frontAngle = 30;
    backAngle = 45;
else
    botMass = 2.46;
    botRadius = 0.079;
    frontAngle = 31;
    backAngle = 45;
end

botInertia = 0.55*botMass*botRadius^2;
front = frontAngle*pi/180;
back = backAngle*pi/180;

% motor angles from X-axis (right)
theta = [front, pi-front, pi+back, 2*pi-back];

%% do friction analysis during rollout
fricYParams = zeros(numYRolloutSamples, 6); % [dataset vic cou couOnly errVC errC]
fricWParams = zeros(numYRolloutSamples, 6);
fricYIndex = 1;
fricWIndex = 1;

for i = 1:numFiles
    data = datasets{i,1};

    timestamp = data(:,2)*1e-6;
    stateVel = data(:,3:5);
    modeXY = data(:,14);
    modeW = data(:,15);

    switch datasets{i,2}
        case 1
            fricYParams(fricYIndex,1) = i;
            [viscousFric, coulombFric, coulombOnly, errVC, errC] = ...
                fricXY(modeXY, timestamp, stateVel);
            fricYParams(fricYIndex,2:end) = [viscousFric, coulombFric, coulombOnly, errVC, errC];
            fricYIndex = fricYIndex + 1;
        case 2
        case 3
            fricWParams(fricWIndex,1) = i;
            [viscousFric, coulombFric, coulombOnly, errVC, errC] = ...
                fricW(modeW, timestamp, stateVel);
            fricWParams(fricWIndex,2:end) = [viscousFric, coulombFric, coulombOnly, errVC, errC];
            fricWIndex = fricWIndex + 1;
        case 4
    end
end

meanErrVCY = mean(fricYParams(:,5));
meanErrCY = mean(fricYParams(:,6));
meanErrYDiff = meanErrVCY/meanErrCY;

fprintf('Y Friction Model Error Difference: %f%%\n', (1-meanErrYDiff)*100);

if meanErrYDiff < 1.05 && meanErrYDiff > 0.95
    fricYViscous = 0;
    fricYCoulomb = mean(fricYParams(:,4));
    disp('Y: Using viscous free model');
else
    fricYViscous = mean(fricYParams(:,2));
    fricYCoulomb = mean(fricYParams(:,3));
    disp('Y: Significant viscous friction detected');
end

meanErrVCW = mean(fricWParams(:,5));
meanErrCW = mean(fricWParams(:,6));
meanErrWDiff = meanErrVCW/meanErrCW;

fprintf('W Friction Model Error Difference: %f%%\n', (1-meanErrWDiff)*100);

if meanErrWDiff < 1.05 && meanErrWDiff > 0.95
    fricWViscous = 0;
    fricWCoulomb = mean(fricWParams(:,4));
    disp('W: Using viscous free model');
else
    fricWViscous = mean(fricWParams(:,2));
    fricWCoulomb = mean(fricWParams(:,3));
    disp('W: Significant viscous friction detected');
end

swX = sum(abs(cos(theta')));
swY = sum(abs(sin(theta')));
y2xScale = swX/swY;

fricXViscous = fricYViscous*y2xScale;
fricXCoulomb = fricYCoulomb*y2xScale;

%% efficiency calculation during acc/plateau
effYParams = zeros(numYRolloutSamples,2);
effWParams = zeros(numWSamples,2);
effYIndex = 1;
effWIndex = 1;

for i = 1:numFiles
    data = datasets{i,1};

    timestamp = data(:,2)*1e-6;
    stateVel = data(:,3:5);
    outForce = data(:,9:11);
    modeXY = data(:,14);
    modeW = data(:,15);

    switch datasets{i,2}
        case 1
            effYParams(effYIndex,1) = i;
            effYParams(effYIndex,2) = efficiencyXY(modeXY, timestamp, stateVel, outForce, fricYViscous, fricYCoulomb);
            effYIndex = effYIndex + 1;
        case 2
        case 3
            effWParams(effWIndex,1) = i;
            effWParams(effWIndex,2) = efficiencyW(modeW, timestamp, stateVel, outForce, fricWViscous, fricWCoulomb);
            effWIndex = effWIndex + 1;
        case 4
    end
end

newEffY = mean(effYParams(:,2));
newEffW = mean(effWParams(:,2));

%% non-linear model parameter estimation for encoder velocity => real velocity
encXParams = zeros(numXSamples, 3);
encYParams = zeros(numYSamples, 3);
encXIndex = 1;
encYIndex = 1;

for i = 1:numFiles
    data = datasets{i,1};

    timestamp = data(:,2)*1e-6;
    stateVel = data(:,3:5);
    encVel = data(:,6:8);

    switch datasets{i,2}
        case 1
        case 2
            [K, T, est] = encModel(timestamp, stateVel, encVel, 1);
            datasets{i,3} = est;
            encXParams(encXIndex,:) = [i K T];
            encXIndex = encXIndex + 1;
        case 3
        case 4
            [K, T, est] = encModel(timestamp, stateVel, encVel, 2);
            datasets{i,3} = est;
            encYParams(encYIndex,:) = [i K T];
            encYIndex = encYIndex + 1;
    end
end

modelEncX = mean(encXParams(:,2:3));
modelEncY = mean(encYParams(:,2:3));

result = [fricXViscous fricXCoulomb fricWViscous fricWCoulomb ...
    newEffY newEffW modelEncX(1) modelEncY(1) modelEncX(2) modelEncY(2)];

showResults(0, 0);

%% plotting
function showResults(~, ~)
    subplot(3, 2, 1);
    plot(fricYParams(:,1), fricYParams(:,2), 'ro', ...
        fricYParams(:,1), fricYParams(:,3), 'bo', ...
        fricYParams(:,1), fricYParams(:,4), 'kx', ...
        fricYParams(:,1), ones(size(fricYParams,1),1)*mean(fricYParams(:,2)), 'r--', ...
        fricYParams(:,1), ones(size(fricYParams,1),1)*mean(fricYParams(:,3)), 'b--', ...
        fricYParams(:,1), ones(size(fricYParams,1),1)*mean(fricYParams(:,4)), 'k--');
    legend('Viscous [N/(m/s)]', 'Coulomb [N]', 'Coulomb Only [N]', 'Location', 'east');
    grid on;
    grid minor;
    title('Friction Y');
    xlabel('Dataset');

    subplot(3, 2, 2);
    plot(fricWParams(:,1), fricWParams(:,2), 'ro', ...
        fricWParams(:,1), fricWParams(:,3), 'bo', ...
        fricWParams(:,1), fricWParams(:,4), 'kx', ...
        fricWParams(:,1), ones(size(fricWParams,1),1)*mean(fricWParams(:,2)), 'r--', ...
        fricWParams(:,1), ones(size(fricWParams,1),1)*mean(fricWParams(:,3)), 'b--', ...
        fricWParams(:,1), ones(size(fricWParams,1),1)*mean(fricWParams(:,4)), 'k--');
    legend('Viscous [N/(rad/s)]', 'Coulomb [Nm]', 'Coulomb Only [Nm]', 'Location', 'east');
    grid on;
    grid minor;
    title('Friction W');
    xlabel('Dataset');
    
    subplot(3, 2, 3);
    plot(effYParams(:,1), effYParams(:,2), 'ro', ...
        effYParams(:,1), ones(size(effYParams,1))*mean(effYParams(:,2)), 'r--');
    grid on;
    grid minor;
    title('Output Efficiency Y');
    xlabel('Dataset');
    ylabel('Factor');
    ylim([0 1]);

    subplot(3, 2, 4);
    plot(effWParams(:,1), effYParams(:,2), 'bo', ...
        effWParams(:,1), ones(size(effYParams,1))*mean(effYParams(:,2)), 'b--');
    grid on;
    grid minor;
    title('Output Efficiency W');
    xlabel('Dataset');
    ylabel('Factor');
    ylim([0 1]);

    subplot(3, 2, 5);
    plot(encYParams(:,1), encYParams(:,2), 'bo', ...
        encXParams(:,1), encXParams(:,2), 'ro', ...
        encXParams(:,1), ones(size(encXParams,1))*mean(encXParams(:,2)), 'r--', ...
        encYParams(:,1), ones(size(encYParams,1))*mean(encYParams(:,2)), 'b--');
    legend('Y', 'X');
    grid on;
    grid minor;
    title('Encoder Model Gain');
    xlabel('Dataset');
    ylabel('K');
    ylim([0.8 1.2]);

    subplot(3, 2, 6);
    plot(encYParams(:,1), encYParams(:,3), 'bo', ...
        encXParams(:,1), encXParams(:,3), 'ro', ...
        encXParams(:,1), ones(size(encXParams,1))*mean(encXParams(:,3)), 'r--', ...
        encYParams(:,1), ones(size(encYParams,1))*mean(encYParams(:,3)), 'b--');
    legend('Y', 'X');
    grid on;
    grid minor;
    title('Encoder Model Time Constant');
    xlabel('Dataset');
    ylabel('T [s]');
    ylim([0 0.2]);
end

function showDataset(~, ~, index)
    data = datasets{index,1};
    est = datasets{index,3};
    
    timestamp = data(:,2)*1e-6;
    stateVel = data(:,3:5);
    encVel = data(:,6:8);
    
    if isempty(est)
        est = zeros(length(timestamp), 3);
    end

    subplot(3, 1, 1);
    plot(timestamp, stateVel(:,1), 'r-', ...
         timestamp, encVel(:,1), 'b-', ...
         timestamp, est(:,1), 'g-');
    axis tight
    grid on
    grid minor
    legend('State', 'Encoder', 'Encoder Model');
    title('X Velocity');
    xlabel('t [s]');
    ylabel('v [m/s]');

    subplot(3, 1, 2);
    plot(timestamp, stateVel(:,2), 'r-', ...
         timestamp, encVel(:,2), 'b-', ...
         timestamp, est(:,2), 'g-');
    axis tight
    grid on
    grid minor
    legend('State', 'Encoder', 'Encoder Model');
    title('Y Velocity');
    xlabel('t [s]');
    ylabel('v [m/s]');
    
    subplot(3, 1, 3);
    plot(timestamp, stateVel(:,3), 'r-', ...
         timestamp, encVel(:,3), 'b-');
    axis tight
    grid on
    grid minor
    legend('State', 'Encoder');
    title('W Velocity');
    xlabel('t [s]');
    ylabel('v [rad/s]');
end

%% processing functions
function out = evalEncModel(x, dt, in, dim)
    numSamples = length(in);
    K = x(1);
    T = x(2);
    
    state = 0;
    out = zeros(numSamples,1);
    
    for ix = 1:numSamples
        velNorm = norm(in(ix,1:2));
        scaledT = T*velNorm + 0.001;
        
        vDot = K/scaledT * in(ix,dim) - 1.0/scaledT*state;
        state = state + vDot*dt(ix);

        out(ix) = state;
    end
end

function [K, T, est] = encModel(timestamp, stateVel, encVel, dim)
    dt = [0; diff(timestamp)];
    
    func = @(x)evalEncModel(x, dt, encVel, dim) - stateVel(:,dim);
    
    options = optimoptions('lsqnonlin', 'Display', 'none');
    x = lsqnonlin(func, [1 1], [], [], options);
    
    K = x(1);
    T = x(2);
    
    est = zeros(length(dt),3);
    est(:,dim) = evalEncModel(x, dt, encVel, dim);
end

function eff = efficiencyXY(modeXY, timestamp, stateVel, outForce, viscousFric, coulombFric)
    % plateau phase
    plateauRange = find(modeXY == 3);
    ignorePlateau = 10;
    plateauRange = plateauRange(ignorePlateau:end);

    forceXY = outForce(plateauRange,1:2);
    velXY = stateVel(plateauRange,1:2);

    meanForce = mean(sqrt(forceXY(:,1).^2 + forceXY(:,2).^2));
    meanVel = mean(sqrt(velXY(:,1).^2 + velXY(:,2).^2));

    effPlateau = (viscousFric * meanVel + coulombFric)/meanForce;

    % acc phase
    accRange = find(modeXY == 4 & outForce(:,1)+outForce(:,2) ~= 0);
    ignoreAcc = 3;
    accRange = accRange(ignoreAcc:end);

    forceXY = outForce(accRange,1:2);
    velXY = stateVel(accRange,1:2);

    velXYAbs = sqrt(velXY(:,1).^2 + velXY(:,2).^2);
    forceXYAbs = sqrt(forceXY(:,1).^2 + forceXY(:,2).^2);

    dt = diff(timestamp(accRange));
    dv = diff(velXYAbs);

    acc = dv./dt;
    velXYAbs = velXYAbs(2:end);
    forceXYAbs = forceXYAbs(2:end);

    effAcc = mean((acc * botMass + viscousFric * velXYAbs + coulombFric)./forceXYAbs);

    eff = mean([effPlateau effAcc]);
end

function eff = efficiencyW(modeW, timestamp, stateVel, outForce, viscousFric, coulombFric)
    % plateau phase
    plateauRange = find(modeW == 3);
    ignorePlateau = 10;
    plateauRange = plateauRange(ignorePlateau:end);

    forceW = outForce(plateauRange,3);
    velW = stateVel(plateauRange,3);

    meanForce = mean(forceW);
    meanVel = mean(velW);

    effPlateau = (viscousFric * meanVel + coulombFric*sign(meanVel))/meanForce;

    % acc phase
    accRange = find(modeW == 4 & outForce(:,3) ~= 0);
    ignoreAcc = 3;
    accRange = accRange(ignoreAcc:end);

    forceW = outForce(accRange,3);
    velW = stateVel(accRange,3);

    velWAbs = abs(velW);
    forceWAbs = abs(forceW);

    dt = diff(timestamp(accRange));
    dv = diff(velWAbs);

    acc = dv./dt;
    velWAbs = velWAbs(2:end);
    forceWAbs = forceWAbs(2:end);

    effAcc = mean((acc * botInertia + viscousFric * velWAbs + coulombFric)./forceWAbs);

    eff = mean([effPlateau effAcc]);
end

function [viscousFric, coulombFric, coulombOnly, errVC, errC] = ...
        fricXY(modeXY, timestamp, stateVel)
    
    velXY = stateVel(:,1:2);
    velXYAbs = sqrt(velXY(:,1).^2 + velXY(:,2).^2);

    ignoreFront = 10;
    
    rollOutStart = find(modeXY == 1,1) + ignoreFront;
    rollOutEnd = find(velXYAbs(rollOutStart:end) < 0.05, 1);
    rollOutRange = rollOutStart:rollOutStart+rollOutEnd;

    velXY = stateVel(rollOutRange,1:2);
    velXYAbs = sqrt(velXY(:,1).^2 + velXY(:,2).^2);

    dt = diff(timestamp(rollOutRange));
    dv = diff(velXYAbs);

    acc = dv./dt;
    velXYAbs = velXYAbs(2:end);

    % model to fit is: F = m*a + B*v + C*sgn(v)
    % during rollout F == 0
    
    b = acc * botMass;
    A = [velXYAbs ones(length(dt),1)];

    x = A\b;
    viscousFric = -x(1);
    coulombFric = -x(2);
    
    coulombOnly = mean(b);
    
    % compute residuals
    resVC = A*x-b;
    resC = coulombOnly - b;
    
    numSamples = length(b);
    
    errVC = sqrt(sum(resVC.^2)) / numSamples;
    errC = sqrt(sum(resC.^2)) / numSamples;
    
    coulombOnly = -coulombOnly;
end

function [viscousFric, coulombFric, coulombOnly, errVC, errC] = ...
        fricW(modeW, timestamp, stateVel)
    velXYAbs = abs(stateVel(:,3));

    ignoreFront = 10;
    rollOutStart = find(modeW == 1,1) + ignoreFront;
    rollOutEnd = find(velXYAbs(rollOutStart:end) < 0.05, 1);
    rollOutRange = rollOutStart:rollOutStart+rollOutEnd;

    velXYAbs = abs(stateVel(rollOutRange,3));

    dt = diff(timestamp(rollOutRange));
    dv = diff(velXYAbs);

    acc = dv./dt;
    velXYAbs = velXYAbs(2:end);

    b = acc * botInertia;
    A = [velXYAbs ones(length(dt),1)];

    x = A\b;
    viscousFric = -x(1);
    coulombFric = -x(2);
    
    coulombOnly = mean(b);
    
    % compute residuals
    resVC = A*x-b;
    resC = coulombOnly - b;
    
    numSamples = length(b);
    
    errVC = sqrt(sum(resVC.^2)) / numSamples;
    errC = sqrt(sum(resC.^2)) / numSamples;
    
    coulombOnly = -coulombOnly;
end

end
