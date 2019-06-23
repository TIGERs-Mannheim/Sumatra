function FD = filter(MD)
%FILTER Apply some filters to given data and return only accepted data

    filterValues = {{'consecutiveBallMovingTime', 0.3}};
    FD = {};
    n = length(MD);
    for i=1:n
        
        fprintf('%s\nwpBallSamples: %5d\n', MD{i}.info.filename, size(MD{i}.wpBall.time,1));
        
        for j=1:length(filterValues)
            value = MD{i}.meta(filterValues{j}{1});
            if value < filterValues{j}{2}
                result = 'filtered';
            else
                result = 'accepted';
                FD{end+1} = MD{i};
            end
            fprintf('%s: %f -> %s\n', filterValues{j}{1}, value, result);
        end
        
        fprintf('\n');
    end
end

