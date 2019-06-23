function data = metaAnalysis(data)
%metaAnalysis Add some meta data to each given data set

    for i=1:length(data)
        
        data{i}.meta = containers.Map;
        data{i}.meta('consecutiveBallMovingTime') = consecutiveBallMovingTime(data{i}, 0.2);
    end
    
end

function movingTime = consecutiveBallMovingTime(D, minVel)
    x = D.wpBall.velAbs>minVel;
    consecutiveValues = maxConsecutiveValues(x);
    avgDt = mean(diff(D.wpBall.time));
    movingTime = consecutiveValues * avgDt;
end

function result = maxConsecutiveValues(x)
    i = find(diff(x));
    n = [i numel(x)] - [0 i];
    c = arrayfun(@(X) X-1:-1:0, n , 'un',0);
    y = cat(2,c{:});
    y(x==0) = 0;
    result = max(y);
end