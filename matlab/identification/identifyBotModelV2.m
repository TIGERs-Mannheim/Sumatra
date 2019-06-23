function [result] = identifyBotModelV2(varargin)
%IDENTIFYBOTMODELV2 Summary of this function goes here
%   Detailed explanation goes here

filenames = varargin;

%% Robot parameters
botMass = 2.65;
botRadius = 0.083;
botInertia = 0.55*botMass*botRadius^2;

frontAngle = 30;
backAngle = 45;
front = frontAngle*pi/180;
back = backAngle*pi/180;

% motor angles from X-axis (right)
theta = [front, pi-front, pi+back, 2*pi-back];

%% load and classify data
numFiles = length(filenames);

datasets = cell(numFiles,2);

numXSamples = 0;
numYSamples = 0;
numWSamples = 0;

for i = 1:numFiles
    data = csvread(filenames{i});
    
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
        else
            % Y move sample
            type = 1;
            numYSamples = numYSamples + 1;
        end
    end
    
    if sum(modeW == 3) > 10
        % rotation sample
        type = 3;
        numWSamples = numWSamples + 1;
    end
    
    datasets{i,1} = data;
    datasets(i,2) = {type};
end

%% do friction analysis during rollout
fricYViscous = 0;
fricYCoulomb = 0;
fricWViscous = 0;
fricWCoulomb = 0;
for i = 1:numFiles
    data = datasets{i,1};
    
    timestamp = data(:,2)*1e-6;
    stateVel = data(:,3:5);
    modeXY = data(:,14);
    modeW = data(:,15);

    switch datasets{i,2}
        case 1
            [viscousFric, coulombFric] = fricXY(modeXY, timestamp, stateVel);
            fricYViscous = fricYViscous + viscousFric;
            fricYCoulomb = fricYCoulomb + coulombFric;
        case 2
        case 3
            [viscousFric, coulombFric] = fricW(modeW, timestamp, stateVel);
            fricWViscous = fricWViscous + viscousFric;
            fricWCoulomb = fricWCoulomb + coulombFric;
    end
end

fricYViscous = fricYViscous / numYSamples;
fricYCoulomb = fricYCoulomb / numYSamples;
fricWViscous = fricWViscous / numWSamples;
fricWCoulomb = fricWCoulomb / numWSamples;

swX = sum(abs(cos(theta')));
swY = sum(abs(sin(theta')));
y2xScale = swX/swY;

fricXViscous = fricYViscous*y2xScale;
fricXCoulomb = fricYCoulomb*y2xScale;

%% efficiency calculation during acc/plateau
newEffY = 0;
newEffW = 0;
for i = 1:numFiles
    data = datasets{i,1};
    
    timestamp = data(:,2)*1e-6;
    stateVel = data(:,3:5);
    outForce = data(:,9:11);
    modeXY = data(:,14);
    modeW = data(:,15);

    switch datasets{i,2}
        case 1
            effY = efficiencyXY(modeXY, timestamp, stateVel, outForce, fricYViscous, fricYCoulomb);
            newEffY = newEffY + effY;
        case 2
        case 3
            effW = efficiencyW(modeW, timestamp, stateVel, outForce, fricWViscous, fricWCoulomb);
            newEffW = newEffW + effW;
    end
end

newEffY = newEffY / numYSamples;
newEffW = newEffW / numWSamples;

%% analyze force influence on encoder velocity
dataX = [];
dataY = [];
for i = 1:numFiles
    data = datasets{i,1};
    
    stateVel = data(:,3:5);
    encVel = data(:,6:8);
    outForce = data(:,9:11);
    effXY = data(1,12);
    modeXY = data(:,14);

    switch datasets{i,2}
        case 1
            data = forceToVelPrepare(modeXY, stateVel, encVel, outForce, effXY);
            dataY = [dataY; data];
        case 2
            data = forceToVelPrepare(modeXY, stateVel, encVel, outForce, effXY);
            dataX = [dataX; data];
        case 3
    end
end

[xNum, xDenom, yNum, yDenom] = velToForceAnalyze(dataX, dataY);

result = [fricXViscous fricXCoulomb fricWViscous fricWCoulomb newEffY newEffW ...
    xNum xDenom yNum yDenom];

%% processing functions

function [xNum, xDenom, yNum, yDenom] = velToForceAnalyze(dataX, dataY)
    forceX = dataX(:,3);
    scaleX = dataX(:,1)./dataX(:,2);

    forceY = dataY(:,3);
    scaleY = dataY(:,1)./dataY(:,2);

    xFit = fit(forceX, scaleX, 'rat01', 'StartPoint', [10 10]);
    yFit = fit(forceY, scaleY, 'rat01', 'StartPoint', [10 10]);
    
    xNum = xFit.p1;
    xDenom = xFit.q1;
    yNum = yFit.p1;
    yDenom = yFit.q1;
    
    %optional plotting
    evalFit = 0:0.1:18;

    figure(1);
    subplot(2,1,1);
    plot(forceX, scaleX, 'b.', evalFit, xFit(evalFit), 'rx', ...
        evalFit, yFit(evalFit), 'gx');
    title('X Force to Vel Error');
    ylabel('v_{err} [m/s]');
    xlabel('F [N]');
    axis tight;
    ylim([0.1 1.2]);
    grid on;
    grid minor;

    subplot(2,1,2);
    plot(forceY, scaleY, 'b.', evalFit, xFit(evalFit), 'rx', ...
        evalFit, yFit(evalFit), 'gx');
    title('Y Force to Vel Error');
    ylabel('v_{err} [m/s]');
    xlabel('F [N]');
    axis tight;
    ylim([0.1 1.2]);
    grid on;
    grid minor;
end

function data = forceToVelPrepare(modeXY, stateVel, encVel, outForce, effXY)
    plateauRange = find(modeXY == 3);
    ignorePlateau = 10;
    plateauRange = plateauRange(ignorePlateau:end);

    accRange = find(modeXY == 4);
    ignoreAcc = 3;
    accRange = accRange(ignoreAcc:end);

    stateVelXY = stateVel(plateauRange, 1:2);
    encVelXY = encVel(plateauRange, 1:2);
    forceXY = outForce(plateauRange, 1:2) * effXY;
    
    stateVelAbs = sqrt(stateVelXY(:,1).^2 + stateVelXY(:,2).^2);
    encVelAbs = sqrt(encVelXY(:,1).^2 + encVelXY(:,2).^2);
    forceAbs = sqrt(forceXY(:,1).^2 + forceXY(:,2).^2);

    stateVelAccXY = stateVel(accRange, 1:2);
    encVelAccXY = encVel(accRange, 1:2);
    forceAccXY = outForce(accRange, 1:2) * effXY;
    
    stateVelAccAbs = sqrt(stateVelAccXY(:,1).^2 + stateVelAccXY(:,2).^2);
    encVelAccAbs = sqrt(encVelAccXY(:,1).^2 + encVelAccXY(:,2).^2);
    forceAccAbs = sqrt(forceAccXY(:,1).^2 + forceAccXY(:,2).^2);

    data = [stateVelAbs encVelAbs forceAbs; stateVelAccAbs encVelAccAbs forceAccAbs];
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
    accRange = find(modeXY == 4);
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

    effPlateau = (viscousFric * meanVel + coulombFric)/meanForce;
    
    % acc phase
    accRange = find(modeW == 4);
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

function [viscousFric, coulombFric] = fricXY(modeXY, timestamp, stateVel)
    velXY = stateVel(:,1:2);
    velXYAbs = sqrt(velXY(:,1).^2 + velXY(:,2).^2);

    % repeat from here
    ignoreFront = 10;
    rollOutRange = find(modeXY == 1 & velXYAbs > 0.05);
    rollOutRange = rollOutRange(ignoreFront:end);

    velXY = stateVel(rollOutRange,1:2);
    velXYAbs = sqrt(velXY(:,1).^2 + velXY(:,2).^2);

    dt = diff(timestamp(rollOutRange));
    dv = diff(velXYAbs);
    
    acc = dv./dt;
    velXYAbs = velXYAbs(2:end);

    b = acc * botMass;
    A = [velXYAbs ones(length(dt),1)];
    
    x = A\b;
    viscousFric = -x(1);
    coulombFric = -x(2);
end

function [viscousFric, coulombFric] = fricW(modeW, timestamp, stateVel)
    velXYAbs = abs(stateVel(:,3));

    % repeat from here
    ignoreFront = 10;
    rollOutRange = find(modeW == 1 & velXYAbs > 0.05);
    rollOutRange = rollOutRange(ignoreFront:end);

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
end

end
