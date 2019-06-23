function [kicks, dist, loss, firstFitKickVel] = chipFit(measFrames, camPos, pKickoff)
%CHIPFIT Summary of this function goes here
%   measFrames = [t px py pz id |v| event]
%   camPos = [c1x c1y c1z; c2x ...]
%   pKickoff = [xKick yKick]
%   kicks = [px py pz vx vy vz; ...]'
%   dist = chip distance until first touchdown
%   loss = [lossXY lossZ] impulse loss factors

    maxFitDiff = 200; % 20cm
    
    % do a rough fitting 
    x6 = chipSolve6(measFrames(4:43,:), camPos);
    x1 = getKickoff(x6);
    
    firstFitKickVel = norm(x6(4:6))*1e-3;
    
    diffFromKickoff = norm(pKickoff(1:2)-x1(1:2)');
    
    if diffFromKickoff > maxFitDiff % more than 10cm away from kick off location?
        kicks = [];
        dist = 0;
        loss = [0 0];
        return;
    end

    % take 80% of the flight time and refine fitting with more samples
    tFly = 0.2/981*x1(6) * 0.8;
    id = find(measFrames(:,1) < measFrames(1,1)+tFly, 1, 'last');
    if isempty(id) || id-4 < 10
        kicks = [];
        dist = 0;
        loss = [0 0];
        return;
    end
    x6 = chipSolve6(measFrames(4:id,:), camPos);
    x2 = getKickoff(x6);
    
    tFly = 0.2/981*x2(6);
    pTouchdown = x2(1:2) + x2(4:5)*tFly;
    dist = norm(pTouchdown-x2(1:2));
    
    % calculate touchdown time and do another fit for the bounced off ball
    id = find(measFrames(:,1) < measFrames(1,1)+tFly, 1, 'last');
    if id+24 > size(measFrames,1)
        kicks = x2;
        loss = [0 0];
        return;
    end
    x6 = chipSolve6(measFrames(id+5:id+24,:), camPos);
    x3 = getKickoff(x6);
    
    diffFromTouchdown = norm(pTouchdown-x3(1:2));
    
    if diffFromTouchdown > maxFitDiff
        kicks = x2;
        loss = [0 0];
        return;
    end
    
    impulseLossZ = x3(6)/x2(6);
    impulseLossXY = norm(x3(4:5))/norm(x2(4:5));
    
    kicks = [x2 x3];
    loss = [impulseLossXY impulseLossZ];
end

function [ x, err, ground ] = chipSolve6( frames, camPos )
    % [t px py pz camId u v] (z will always be zero)

    n = size(frames, 1);

    t_zero = frames(1,1);

    a = 9.81*1000;

    A = zeros(n*2, 6);
    b = zeros(n*2, 1);

    for i = 1:n
        id = frames(i,5);
        f = camPos(id,:);
        g = frames(i,2:3);
        t = frames(i,1)-t_zero;

        A(i*2-1, :) = [f(3) 0 (g(1)-f(1)) f(3)*t 0 (g(1)*t-f(1)*t)];
        A(i*2, :) = [0 f(3) (g(2)-f(2)) 0 f(3)*t (g(2)*t-f(2)*t)];

        b(i*2-1) = 0.5*a*t^2*(g(1)-f(1))+g(1)*f(3);
        b(i*2) = 0.5*a*t^2*(g(2)-f(2))+g(2)*f(3);
    end

    x = A\b;

    p = x(1:3);
    v = x(4:6);

    ground = zeros(n,2);

    for i = 1:n
        id = frames(i,5);
        f = camPos(id,:);
        t = frames(i,1)-t_zero;

        k = f(3)/(p(3)+v(3)*t-0.5*a*t^2-f(3));
        gx = f(1) - k*(p(1)+v(1)*t) + k*f(1);
        gy = f(2) - k*(p(2)+v(2)*t) + k*f(2);

        ground(i, :) = [gx gy];
    end

    bc = A*x;
    err = mean(abs(bc-b));
end

function [ kickoff ] = getKickoff( x )
% Backtrack a flying ball to get its kick off location

% x = [px py pz vx vy vz]

v_0 = x(6);
p_0 = x(3);
g = -9.81*1000;

sqr = v_0^2-2*g*p_0;
if sqr < 0
    % that means the ball z location is < 0 and it will not hit z = 0
    % (velocity too low)
    warning('Ball kickoff location below zero, no intersection with ground');
    kickoff = ones(6,1)*inf;
end

t = (sqrt(sqr)-v_0)/g;

kickoff = zeros(6,1);
kickoff(1:3) = x(1:3) + x(4:6)*t + [0 0 0.5*g*t^2]';
kickoff(4:6) = x(4:6) + [0 0 g*t]';

end
