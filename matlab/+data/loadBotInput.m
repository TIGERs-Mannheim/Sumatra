function botInput = loadBotInput(file)
    D = importdata(file);
    if(isstruct(D))
        T = D.data;
    else
        T = D;
    end
    
%     "id", "color", "timestamp", "tSent",
%     "trajVel_x", "trajVel_y", "trajVel_z",
%     "trajPos_x", "trajPos_y", "trajPos_z",
%     "setVel_x", "setVel_y", "setVel_z",
%     "setPos_x", "setPos_y", "setPos_z",
%     "localVel_x", "localVel_y", "localVel_z",
%     "dribbleRpm", "kickSpeed", "kickDevice", "kickMode"
%
%     "velMax", "accMax", "jerkMax", "velMaxW", "accMaxW", "jerkMaxW", "velMaxFast", "accMaxFast", "fastMove"
    
    botInput.id = T(:,1);
    botInput.color = T(:,2);
    botInput.timestamp = T(:,3);
    botInput.tSent = T(:,4);
    botInput.trajVel = T(:,5:7);
    botInput.trajPos = T(:,8:10);
    botInput.setVel = T(:,11:13);
    botInput.setPos = T(:,14:16);
    botInput.localVel = T(:,17:19);
    botInput.dribbleRpm = T(:,20);
    botInput.kickSpeed = T(:,21);
    botInput.kickDevice = T(:,22);
    botInput.kickMode = T(:,23);
    
    o=23;
    botInput.velMax = T(:,o+1);
    botInput.accMax = T(:,o+2);
    botInput.jerkMax = T(:,o+3);
    botInput.velMaxW = T(:,o+4);
    botInput.accMaxW = T(:,o+5);
    botInput.jerkMaxW = T(:,o+6);
    botInput.velMaxFast = T(:,o+7);
    botInput.accMaxFast = T(:,o+8);
    botInput.fastMove = T(:,o+9);
    
end

