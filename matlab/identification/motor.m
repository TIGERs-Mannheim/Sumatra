function params = motor(filename)

%% Load data %%
Ts = 0.00125;
csv = csvread(filename);

%% Calculate data loss
dataLoss = 1-size(csv,1)/((csv(end,2)-csv(1,2))*1e-6*800);

[~, ia] = unique(csv(:,2));
csv = csv(ia, :);
csv(:,2) = csv(:,2)*1e-6;

tStart = roundn(csv(1,2)+0.1, -1);
tEnd = roundn(csv(end-1,2)-0.1, -1);

sampleTimes = (tStart:Ts:tEnd)';
numSamples = length(sampleTimes);

inter = zeros(numSamples, 8);

for i = 3:10
    inter(:,i-2) = interp1(csv(:,2), csv(:,i), sampleTimes);
end

vol = inter(:,1:4);
vel = inter(:,5:8);

%% process data %%
numSamples = size(vel, 1);

params = zeros(1,8);

for m = 1:4
    volX = smooth(vol(:,m),20);
    velX = smooth(vel(:,m),20);
    
    fOptX = @(x)(funcPT1(volX, x, 0)-velX);

    opt = optimoptions(@lsqnonlin, 'Display', 'off');
    params([m m+4]) = lsqnonlin(fOptX, [30.0, 0.1], [0.001, 0.001], [], opt);
end

medK = median(params(1:4));
medT = median(params(5:8));

params = [params medK medT dataLoss];

function [ predVel ] = funcPT1( vol, x, prevVel )
    K = x(1);
    T = x(2);

    predVel = zeros(numSamples,1);

    T2 = 1/(T/Ts+1);

    for j = 1:numSamples
        u = K*vol(j);
        predVel(j) = T2*(u - prevVel) + prevVel;
        prevVel = predVel(j);
    end
end

end
