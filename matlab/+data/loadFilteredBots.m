function [ filteredBots ] = loadFilteredBots( file )
    D = importdata(file);
    if(isstruct(D))
        T = D.data;
    else
        T = D;
    end
    
    %  "id", "color", "timestamp", "pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "acc_x", "acc_y", "acc_z", "quality"
    filteredBots.id = T(:,1);
    filteredBots.color = T(:,2);
    filteredBots.timestamp = T(:,3);
    filteredBots.pos = T(:,4:6);
    filteredBots.vel = T(:,7:9);
    filteredBots.acc = T(:,10:12);
    filteredBots.quality = T(:,13);

    % derived 
    filteredBots.time = (filteredBots.timestamp - filteredBots.timestamp(1)) / 1e9;
end

