function [ wpBall ] = loadWpBall( file )
    D = importdata(file);
    if(isstruct(D))
        T = D.data;
    else
        T = D;
    end
    
    % {'timestamp'  'pos_x'  'pos_y'  'pos_z'  'vel_x'  'vel_y'  'vel_z'  'acc_x'  'acc_y'  'acc_z'  'lastVisibleTimestamp'  'vSwitchToRoll'  'chipped', 'tAssembly'}
    wpBall.timestamp = T(:,1);
    wpBall.pos = T(:,2:4);
    wpBall.vel = T(:,5:7);
    wpBall.acc = T(:,8:10);
    wpBall.lastVisibleTimestamp = T(:,11);
    wpBall.vSwitchToRoll = T(:,12);
    wpBall.chipped = T(:,13);
    if size(T,2) > 13
        wpBall.tAssembly = T(:,14);
    end

    % derived 
    wpBall.time = (wpBall.timestamp - wpBall.timestamp(1)) / 1e9;
end

