function [ result ] = learnBallAndKickModel( varargin )
%IDENTBALLKICK Identify ball model and kicker conversion
%   result = [aSlide aRoll cSwitch lossXY lossZ ...
%             sOffset sFactor sGOF cOffset cFactor cGOF]

folders = varargin;

clc;

numDatasets = length(folders);

kicks = zeros(numDatasets,11);

for d = 1:numDatasets
    fprintf('[%3.0f%%] %s...\n', d/numDatasets*100, folders{d});
    
    kicks(d,:) = processFolder(folders{d});
end

% get ball parameters
% [aSlide aRoll cSwitch lossXY lossZ]
ballParams = zeros(1,5);

ballParams(1) = mean(kicks(kicks(:,5) > 1000,6))*1e-3;
ballParams(2) = mean(kicks(kicks(:,7) < 0,7))*1e-3;
ballParams(3) = mean(kicks(kicks(:,4) > 0,4));
ballParams(4) = mean(kicks(kicks(:,8) > 1000,9));
ballParams(5) = mean(kicks(kicks(:,8) > 1000,10));

kicks(:,2) = kicks(:,2)*1e3;
kicks(:,[5 8]) = kicks(:,[5 8])*1e-3;

% get straight kick parameters
% [duration speed]
straight = kicks(kicks(:,1)==0,[2 5]);
validId = (straight(:,2) < (max(straight(:,2))*0.95)) & (straight(:,2) > 0.1);
usedStraight = straight(validId,:);


sufficientStraightData = size(usedStraight,1) > 1;
if sufficientStraightData
    % Fit model to data.
    ft = fittype( 'poly1' );
    [fitStraight, gofStraight] = fit(usedStraight(:,2), usedStraight(:,1), ft);
else
    fitStraight.p1 = 0;
    fitStraight.p2 = 0;
    gofStraight.adjrsquare = 0;    
end

% get chip kick parameters
% [duration distance kickVelOfInitialFit]
chip = kicks(kicks(:,1)>0,[2 8 11]);
beta = 45*pi/180;
chip(:,2) = sqrt((chip(:,2)*9.81)/sin(2*beta));
validId = (chip(:,2) < (max(chip(:,2))*0.95)) & (chip(:,2) > 0.2);
usedChip = chip(validId,:);

sufficientChipData = size(usedChip,1) > 1;
if sufficientChipData
    [fitChip, gofChip] = fit(usedChip(:,2), usedChip(:,1), ft);
else
    fitChip.p1 = 0;
    fitChip.p2 = 0;
    gofChip.adjrsquare = 0;
end
kickParams = [fitStraight.p2 fitStraight.p1 gofStraight.adjrsquare ...
    fitChip.p2 fitChip.p1 gofChip.adjrsquare];

result = [ballParams kickParams];

% optional plotting
figure
subplot(2,1,1);
hold off;
plot(straight(:,2), straight(:,1), 'x', ...
    usedStraight(:,2), usedStraight(:,1), 'o');
hold on;
if sufficientStraightData
    plot(fitStraight);
    legend('All Samples', 'Used Samples', 'Fit', 'Location', 'northwest');
else
    legend('All Samples', 'Used Samples', 'Location', 'northwest');
end
xlabel('Kick Speed [m/s]');
ylabel('Duration [ms]');
title('Straight Kick');
axis tight;
grid on;
grid minor;

subplot(2,1,2);
hold off;
plot(chip(:,2), chip(:,1), 'x', ...
    usedChip(:,2), usedChip(:,1), 'o');
hold on;
if sufficientChipData
    plot(fitChip);
    legend('All Samples', 'Used Samples', 'Fit', 'Location', 'northwest');
else
    legend('All Samples', 'Used Samples', 'Location', 'northwest');
end
xlabel('Kick Speed [m/s]');
ylabel('Duration [ms]');
title('Chip Kick');
axis tight;
grid on;
grid minor;

end

function [result, valid] = processFolder(folder)
    log = data.loadAll(folder);

    pos = log.rawBalls.pos;
    id = log.rawBalls.camId;
    tCapture = log.rawBalls.timestamp;

    frames = [tCapture pos(:,1:3) id+1];

    [~, ia] = unique(frames(:,1));
    frames = frames(ia, :);
    frames(:,1) = frames(:,1)-frames(1,1);
    frames(:,1) = frames(:,1)*1e-9;

    n = size(frames,1);
    
    % [t px py pz id |v| event]
    frames = [frames zeros(n,2)];
    
    % parse log info
    numCams = size(log.info.opt.cams,2);
    camPos = zeros(numCams, 3);
    for i = 1:numCams
        camPos(i,:) = [log.info.opt.cams{i}.derivedCameraWorldTx ...
            log.info.opt.cams{i}.derivedCameraWorldTy ...
            log.info.opt.cams{i}.derivedCameraWorldTz];
    end
    fieldSize = log.info.opt.field.extent;
    
    pKickoff = log.info.kickPos;

    measFrames = util.getKickFrames(frames, numCams, fieldSize);
    
    mc = size(measFrames,1);
    
    result = zeros(1,11);
    result(1) = log.info.isChip;
    result(2) = log.info.duration;
    result(3) = log.info.voltage;
    
    if mc < 25
        disp('       No useable data in this file');
        valid = 0;
        return;
    else
        if(log.info.isChip)
            % [isChip duration voltage c_switch vKick aSlide aRoll dist lossXY lossZ]
            [~, dist, loss, firstFitSpeed] = util.chipFit(measFrames, camPos, pKickoff);
            result(8:11) = [dist loss firstFitSpeed];
        else
            % [c_switch vKick aSlide aRoll]
            [c_switch, vKick, acc] = util.straightFit(measFrames);
            result(4:7) = [c_switch vKick acc];
        end
        valid = 1;
    end
end
