function [initSpeed, finalSpeed, rmse, fittedLines] = detectBallSpeedUsingModel(time,speed)
%detectBallSpeedUsingModel Detect the initial and final ball speed 
%   
%   Detect the initial and final ball speed by fitting a two phase ball model.
%   The input data time 't' and absolute ball speed 'vel' must have been
%   filtered and should contain only the kick.

    fun = @(x) objectiveFun(x, time, speed);
    [initSpeed, rmse] = fminsearch(fun, 3.0);
    fittedLines = kickModel(initSpeed, time - time(1));
    finalSpeed = fittedLines(end);
end

function rmse = objectiveFun(kickSpeed, time, speed)
    modelSpeed = kickModel(kickSpeed, time - time(1));
    d = modelSpeed(5:end-1) - speed(5:end-1);
    rmse = sqrt(mean(d.*d));
end

function speed = kickModel(kickSpeed, t)
    accRoll = -0.270;
    accSlide = -4.7;
    kSwitch = 0.6644;
    
    tSwitch = (1-kSwitch) * kickSpeed / -accSlide;
    I = find(t>tSwitch);
    switchOffset = length(t);
    if ~isempty(I)
        switchOffset = I(1);
    end
    tSlide = t(1:switchOffset);
    speed = kickSpeed + accSlide * tSlide;
    
    vSwitch = speed(end);
    
    tRoll = t(switchOffset+1:end) - t(switchOffset-1);
    speed = [speed; vSwitch + accRoll * tRoll];    
end