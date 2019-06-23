function [initSpeed, finalSpeed, rmse, fittedLines] = detectBallSpeed(time,speed)
%detectBallSpeed Detect the initial and final ball speed 
%   
%   Detect the initial and final ball speed by fitting two linear lines.
%   The input data time 't' and absolute ball speed 'vel' must have been
%   filtered and should contain only the kick.

    robustFitting = 'On';
    t = time(5:end-1);
    vel = speed(5:end-1);
        
    mid = floor(length(t)/2);
    step = floor(mid/2);
    
    while step > 1
        [fit_low, gof_low] = fit(t(1:mid), vel(1:mid), 'poly1', 'Robust', robustFitting);
        [fit_high, gof_high] = fit(t(mid:end), vel(mid:end), 'poly1', 'Robust', robustFitting);
        
        if gof_low.rmse > gof_high.rmse
            mid = mid - step;
        else
            mid = mid + step;
        end
        step = floor(step / 2);
    end
    
    initSpeed = fit_low(time(1));
    finalSpeed = fit_high(time(end));
    rmse = gof_low.rmse + gof_high.rmse;
    
    fittedLines = zeros(size(time));
    fittedLines(1:mid-1) = fit_low(time(1:mid-1));
    fittedLines(mid:end) = fit_high(time(mid:end));
end
