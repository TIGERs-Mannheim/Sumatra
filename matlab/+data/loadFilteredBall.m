function [ filteredBall ] = loadFilteredBall( file )
    D = importdata(file);
    if(isstruct(D))
        T = D.data;
    else
        T = D;
    end
    
    % "timestamp", "pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "acc_x", "acc_y", "acc_z", "lastVisibleTimestamp"
    filteredBall.timestamp = T(:,1);
    filteredBall.pos = T(:,2:4);
    filteredBall.vel = T(:,5:7);
    filteredBall.acc = T(:,8:10);
    filteredBall.lastVisibleTimestamp = T(:,11);
    filteredBall.vSwitchToRoll = 0;
    filteredBall.chipped = 0;
    
    % derived 
    filteredBall.time = (filteredBall.timestamp - filteredBall.timestamp(1)) / 1e9;
end

