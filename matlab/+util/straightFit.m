function [c_switch, vKick, acc, err, fit] = straightFit(measFrames)
%STRAIGHTFIT Fit a two phase model on the given kick frames
%   measFrames = [t px py pz id |v| event]
%   c_switch = fraction of initial velocity when slide/roll switch occurs
%   vKick = initial velocity
%   acc = [accSlide accRole]

    tSample = measFrames(1,1):0.01:measFrames(end,1);

    % average all frames in an interval of tSmooth and generate uniformly
    % spaced samples
    tc = length(tSample);
    v = zeros(tc,1);
    tSmooth = 0.02;
    for i = 1:tc
        tNow = tSample(i);
        ind = (measFrames(:,1) > tNow-tSmooth/2) + (measFrames(:,1) < tNow+tSmooth/2);
        col = measFrames(ind == 2,:);
        if size(col,1) == 0
            if i == 1
                v(i) = 0;
            else
                v(i) = v(i-1);
            end
        else
            v(i) = mean(col(:,6));
        end
    end

    t = (tSample-tSample(1))';
    mc = length(t);

    ec = mc-20;
    err = zeros(ec,2);
    for splitId = 10:mc-10
        [~, e] = twoLineModel(t, v, splitId);
        err(splitId-9,:) = [splitId e];
    end

    [~, I] = min(err(:,2));
    splitId = err(I,1);

    [fit, ~, match, tSplitPerc] = twoLineModel(t, v, splitId);
    c_switch = fit(2,2)/fit(2,1);
    
    vKick = fit(2,1);
    acc = fit(1,:);
    
    if tSplitPerc > 0.5
        acc(2) = 0;
    end
    
    fit = [t v match];
end

function [x, err, match, tSplitPerc] = twoLineModel(t, v, splitId)
    mc = size(t,1);
    p1Samples = 5:splitId;
    p2Samples = splitId+1:mc;

    b = v(p1Samples);
    A = [t(p1Samples) ones(length(p1Samples),1)];
    x1 = A\b;
    v0 = x1(1)*t(splitId) + x1(2);
    
    if x1(1) > 0
        x1 = [0; 0];
    end

    b = v(p2Samples)-v0;
    A = t(p2Samples)-t(p2Samples(1));
    x2 = [A\b v0];
    
    if x2(1) > 0
        x2 = [0 0];
    end
    
    if x2(1) < x1(1)
        % deceleration of second phase larger => invalid
        x1 = [0; 0];
        x2 = [0 0];
    end

    p1Match = x1(1)*t(1:splitId) + x1(2);
    p2Match = x2(1)*A + x2(2);
    match = [p1Match; p2Match];
    
    tSplitPerc = t(splitId)/t(end);
    
    x = [x1 x2']; % [a0 a1; v0 v1]
    err = mean(abs(v-match));
end