function botOutput = loadBotOutput(file)
    D = importdata(file);
    if(isstruct(D))
        T = D.data;
    else
        T = D;
    end
    
%     "id", "color", "timestamp"
%     "pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "pos_valid", "vel_valid",
%     "kickerLevel", "dribbleSpeed", "batteryPercentage",
%     "barrierInterrupted", "kickCounter", "dribblerTemp",
%     "features"
    botOutput.id = T(:,1);
    botOutput.color = T(:,2);
    botOutput.timestamp = T(:,3);
    botOutput.pos = T(:,4:6);
    botOutput.vel = T(:,7:9);
    botOutput.posValid = T(:,10);
    botOutput.velValid = T(:,11);
    botOutput.kickerLevel = T(:,12);
    botOutput.dribbleSpeed = T(:,13);
    botOutput.batteryPercentage = T(:,14);
    botOutput.barrierInterrupted = T(:,15);
    botOutput.kickCounter = T(:,16);
    botOutput.dribblerTemp = T(:,17);
    botOutput.features = T(:,18);
    
    % position feedback is in [m], but we use [mm]
    pos_xy = botOutput.pos(:,1:2)*1000;
    botOutput.pos = [pos_xy, botOutput.pos(:,3)];
end

