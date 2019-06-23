function [measFrames, pKickoff, cam] = getKickFrames(frames, numCams, fieldSize)
%GETKICKFRAMES Isolate frames of a single kick
%   frames = [t px py pz id |v| event]
%   fieldSize = [xExtent yExtent]
%   pKickoff = [xKick yKick]

    cam = cell(numCams,1);

    for i = 1:4
        % split camera frames according to ID
        cam{i} = frames(frames(:,5)==i,:);

        % tracking filter
        cam{i} = camTrackingFilter(cam{i});

        % skip this camera if it did not see a ball
        if size(cam{i},1) == 0
            cam{i} = zeros(1,7);
            continue;
        end

        % determine velocity from positions
        cam{i}(:,6) = calcVel(cam{i}(:,1), cam{i}(:,2:3));

        % determine kick frames
        cam{i}(:,7) = identifyKicks(cam{i}(:,6), 300); % 300 for chip, 400 for straight
        
        % determine ball stop frames (by vel)
        cam{i}(:,7) = cam{i}(:,7) + identifyStops(cam{i}(:,6), [200 500]);
        
        % determine when ball left the field
        cam{i}(:,7) = cam{i}(:,7) + identifyBallOut(cam{i}(:,2:3), fieldSize, 100);
    end
    
    allFrames = vertcat(cam{1:4});
    allFrames = sortrows(allFrames);
%     events = allFrames(allFrames(:,7) > 0, [1 5 7]);
    
    firstFrame = find(allFrames(:,7) == 1,1); % first kick frame
    lastFrame = find(allFrames(:,7) > 1,1); % first stop or out of field frame
    if isempty(lastFrame)
        lastFrame = size(allFrames,1);
    end
    measFrames = allFrames(firstFrame:lastFrame,:);
    
    pKickoff = mean(allFrames(firstFrame-14:firstFrame-4,2:3));
end


function events = identifyKicks(v, vKickThreshold)
    nc = size(v, 1);
    events = zeros(nc,1);
    nVel = 6;
    for j = nVel:nc
        % take the last nVel velocity frames
        b = v(j-nVel+1:j);

        if all(b(4:nVel)-b(3) > vKickThreshold)
            if all(b(4:nVel)-mean(b(1:3)) > vKickThreshold) 
                events(j-3) = 1;
            end
        end
    end
end

function events = identifyStops(v, vBound)
    nc = size(v, 1);
    events = zeros(nc,1);
    nVel = 6;
    rolling = 0;
    for j = nVel:nc
        b = v(j-nVel+1:j);

        if rolling == 0 && mean(b) > vBound(2)
            rolling = 1;
        end

        if rolling == 1 && mean(b) < vBound(1)
            rolling = 0;
            events(j-nVel/2) = 2;
        end
    end
end

function events = identifyBallOut(pos, fieldDims, hysteresis)
    nc = size(pos, 1);
    events = zeros(nc,1);
    dIn = fieldDims-hysteresis;
    dOut = fieldDims;
    in = 0;
    for j = 1:nc
        p = pos(j,:);
        if p(1) > -dIn(1)/2 && p(1) < dIn(1)/2 && ...
                p(2) > -dIn(2)/2 && p(2) < dIn(2)/2 && in == 0
            in = 1;
        end

        if (p(1) < -dOut(1)/2 || p(1) > dOut(1)/2 || ...
                p(2) < -dOut(2)/2 || p(2) > dOut(2)/2) && in == 1
            in = 0;
            events(j) = 3;
        end
    end
end

function vels = calcVel(t, p)
    nc = size(t,1);
    vels = zeros(nc,1);
    for j = 2:nc
        dt = t(j)-t(j-1);
        dp = p(j,:)-p(j-1,:);

        vel = norm(dp/dt);
        vels(j) = vel;

        if j == 2
            vels(1) = vel;
        end
    end
end

function [ frames ] = camTrackingFilter( frames )
% Simple tracking filter
%   Uses a maximum velocity to identify impossible position jumps

% frames = [t px py pz id ...; ...]

    dtCamMin = 0.01; % == 100Hz
    vMax = 10000; % max assumed ball speed in [mm/s]

    numFrames = size(frames,1);
    if numFrames < 1
        return;
    end

    tValid = frames(1,1)-1.0;
    pValid = frames(1,2:3);


    validFrames = zeros(1,numFrames);
    validFrames(1) = 1;

    for f = 1:numFrames
        dtValid = frames(f,1)-tValid;
        if dtValid < dtCamMin
            continue;
        end

        searchRadius = dtValid*vMax;
        if searchRadius > 500
            searchRadius = 500;
        end

        deltaPos = frames(f,2:3)-pValid;

        if norm(deltaPos) < searchRadius
            validFrames(f) = 1;
            pValid = frames(f,2:3);
            tValid = frames(f,1);
        end
    end

    frames(validFrames<1,:) = [];
end
