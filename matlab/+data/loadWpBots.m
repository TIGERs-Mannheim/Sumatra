function [ wpBots ] = loadWpBots( file )
    D = importdata(file);
    if(isstruct(D))
        T = D.data;
    else
        T = D;
    end
    
    %  {'id'  'color'  'timestamp'  'pos_x'  'pos_y'  'pos_z'  'vel_x'  'vel_y'  'vel_z'  'acc_x'  'acc_y'  'acc_z'  
    % 'visible'  'kickSpeed'  'isChip'  'dribbleRpm'  'barrierInterrupted', 'tAssembly', 
    % "buffered_pos_x", "buffered_pos_y", "buffered_pos_z", "buffered_vel_x", "buffered_vel_y", "buffered_vel_z"}
    wpBots.id = T(:,1);
    wpBots.color = T(:,2);
    wpBots.timestamp = T(:,3);
    wpBots.pos = T(:,4:6);
    wpBots.vel = T(:,7:9);
    wpBots.acc = T(:,10:12);
    wpBots.visible = T(:,13);
    wpBots.kickSpeed = T(:,14);
    wpBots.isChip = T(:,15);
    wpBots.dribbleRpm = T(:,16);
    wpBots.barrierInterrupted = T(:,17);
    if size(T,2) > 17
        wpBots.tAssembly = T(:,18);
    end
    if size(T,2) > 18
        wpBots.buffered_pos = T(:,19:21);
        wpBots.buffered_vel = T(:,22:24);
        wpBots.dist2Traj = T(:,25);
    end
    if size(T,2) > 25
        wpBots.feedback_pos = T(:,26:28);
        wpBots.feedback_vel = T(:,29:31);
    end

    % derived 
    wpBots.time = (wpBots.timestamp - wpBots.timestamp(1)) / 1e9;
end

