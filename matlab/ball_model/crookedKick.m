function [ result ] = crookedKick( varargin )
%CROOKEDKICK Plot crooked kicks
%   result = [aSlide aRoll cSwitch lossXY lossZ ...
%             sOffset sFactor sGOF cOffset cFactor cGOF]

folders = varargin;

clc;

numDatasets = length(folders);

kicks = zeros(numDatasets, 4);

for d = 1:numDatasets
    fprintf('[%3.0f%%] %s...\n', d/numDatasets*100, folders{d});
    
    kicks(d,:) = processFolder(folders{d});
end

parts = strsplit(folders{1}, filesep);
radiusPart = parts{end-1};

save(strcat(radiusPart, '.mat'), 'kicks');

result = 0;

% optional plotting
subplot(2,1,1);
plot(kicks(:,1), kicks(:,2)*180/pi, 'x');
xlabel('Offset [mm]');
ylabel('Deviation [deg]');
title('Offset vs. Deviation');
% legend('All Samples', 'Used Samples', 'Fit', 'Location', 'northwest');
axis tight;
grid on;
grid minor;

subplot(2,1,2);
plot(kicks(:,1), kicks(:,4), 'x');
xlabel('Offset [mm]');
ylabel('KickVel [m/s]');
title('Offset vs. Kick Speed');
% legend('All Samples', 'Used Samples', 'Fit', 'Location', 'northwest');
axis tight;
grid on;
grid minor;

end

function [result, valid] = processFolder(folder)
    log = data.loadAll(folder);

    kickOrientation = log.info.orientation;
    offset = log.info.offset;
    kickPos = log.info.kickPos;
    ballPos = log.wpBall.pos;
    duration = log.info.duration;
    fieldExtent = log.info.opt.field.extent;

    ballSamples = size(ballPos, 1);
    
    if ballSamples < 10
        valid = 0;
        result = [0 0 0];
        return;
    end

    for i = 1:ballSamples
        if ballPos(i,1) > fieldExtent(1)/2 || ballPos(i,1) < -fieldExtent(1)/2 ...
                || ballPos(i,2) > fieldExtent(2)/2 || ballPos(i,2) < -fieldExtent(2)/2
            break;
        end
    end
    
    kickVel = max(log.wpBall.velAbs(1:i));

    endPos = ballPos(i, 1:2) - kickPos;
    deviation = atan2(endPos(2), endPos(1)) - kickOrientation;
    
    result = [offset deviation duration kickVel];
    valid = 1;
end
