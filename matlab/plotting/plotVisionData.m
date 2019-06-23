function plotVisionData( folder )

global botId;
global botColor;

D = [];

lastView = @plotBallVel;

newWindow(0,0)
reload(0,0);

  
function newWindow(~,~)
  if exist('folder','var')
    figure('Name', strcat('VisionDataViewer: ', folder), 'NumberTitle', 'Off');
  else
    figure('Name', strcat('VisionDataViewer: ', ''), 'NumberTitle', 'Off');
  end
  m = uimenu('Label', 'Category');
  uimenu(m, 'Label', 'Load File', 'Callback', @loadData);
  uimenu(m, 'Label', 'Reload', 'Callback', @reload);
  uimenu(m, 'Label', 'New Window', 'Callback', @newWindow);
  uimenu(m, 'Label', 'ball acc and timing', 'Callback', @plotAccAndTiming);
  uimenu(m, 'Label', 'ball pos', 'Callback', @plotBallPos);
  uimenu(m, 'Label', 'ball height', 'Callback', @plotBallZ);
  uimenu(m, 'Label', 'ball vel', 'Callback', @plotBallVel);
  uimenu(m, 'Label', 'bot timing', 'Callback', @plotBotTiming);
  uimenu(m, 'Label', 'bot delays', 'Callback', @plotBotDelays);
  uimenu(m, 'Label', 'bot pos', 'Callback', @plotBotPos);
  uimenu(m, 'Label', 'bot vel', 'Callback', @plotBotVel);
  uimenu(m, 'Label', 'bot vel local', 'Callback', @plotBotVelLocal);
  uimenu(m, 'Label', 'bot device info', 'Callback', @plotBotDeviceInfo);
  uimenu(m, 'Label', 'bot limits', 'Callback', @plotBotLimits);
end

function loadFolder(folder)
  D = data.loadAll(folder);
end

function setupBotSelection(cb)
  uIds = getAvailableBots();
  uIdsStr = cell(size(uIds,1),1);
  for i=1:size(uIdsStr,1)
    color = 'yellow';
    if uIds(i,2) == 1
      color = 'blue';
    end
    uIdsStr{i} = sprintf('Bot %d %s',uIds(i,1),color);
  end
  
  curVal = 1;
  if ~isempty(botId)
    curVal = find((uIds(:,1)==botId) + (uIds(:,2) == botColor) == 2);
    if isempty(curVal)
      curVal = 1;
    end
  end

  h=findobj('style','popup');
  delete(h)
  uicontrol('Style', 'popup',...
         'String', uIdsStr,...
         'Value', curVal,...
         'Position', [10 1 100 50],...
         'Callback', {@setbot, cb}); 
end

function setbot(source, ~, cb)
  val = source.Value;

  uIds = getAvailableBots();
  botId = uIds(val,1);
  botColor = uIds(val,2);
  
  D = data.addSingleBot(D, botId, botColor);
  cb(0,0);
end

function uIds = getAvailableBots()
  uIds = unique([D.wpBots.id, D.wpBots.color],'rows');
end

function loadData(~,~)
  if exist('folder','var')
    folder = uigetdir(folder, 'select data folder');
  else
    folder = uigetdir('', 'select data folder');
  end
  if folder == 0
    return;
  end
  reload(0,0);
end

function reload(~,~)
  if ~exist('folder','var')
    loadData(0,0);
  else
    if exist(folder, 'dir')
      loadFolder(folder);
    end
    lastView(0,0);
  end
end

function plotBallPos(~, ~)  
  subplot(2,2,1); hold off;
  leg = {};
  
  plot(D.wpBall.pos(:,1),D.wpBall.pos(:,2));
  leg{1} = 'WP';
  hold on;
  
  if isfield(D, 'rawBall')
      plot([D.rawBall.pos(1,1);D.rawBall.pos(end,1)],[D.rawBall.pos(1,2);D.rawBall.pos(end,2)]);
      plot(D.rawBall.pos(1,1),D.rawBall.pos(1,2),'*');
      leg{2} = 'raw linear line';
      leg{3} = 'raw start';
      
      for c=unique(D.rawBall.camId)'
        plot(D.rawBall.pos(D.rawBall.camId==c,1),D.rawBall.pos(D.rawBall.camId==c,2),'.');
        leg{end+1} = sprintf('raw cam %d', c);
      end
  end
    
  title('WP and raw pos with cam info');
  legend(leg);
  xlabel('x [mm]'); ylabel('y [mm]');
  axis equal;

  
  subplot(2,2,2); hold off;
  plot(D.wpBall.pos(:,1),D.wpBall.pos(:,2));
  hold on;
  plot(D.wpBall.pos(1,1),D.wpBall.pos(1,2),'*');
  leg = {};
  leg{1} = 'WP';
  
  if isfield(D, 'rawBall')
      allRawBallsCam = D.rawBalls.camId;
      allRawBallsPos = D.rawBalls.pos;
      leg{2} = 'raw start';
      i=3;
      for c=unique(allRawBallsCam)'
        plot(allRawBallsPos(allRawBallsCam==c,1),allRawBallsPos(allRawBallsCam==c,2),'.');
        leg{i} = sprintf('raw cam %d', c);
        i = i + 1;
      end
  end
  
  title('all raw pos with cam info');
  legend(leg);
  xlabel('x [mm]'); ylabel('y [mm]');
  axis equal;


  subplot(2,2,3); hold off;
  plot(D.wpBall.time,D.wpBall.pos(:,1),'.');
  hold on;
  plot(D.wpBall.time,D.wpBall.pos(:,2),'.');
  
  if isfield(D, 'rawBall')
    plot(D.rawBall.time,D.rawBall.pos(:,1),'.');
    plot(D.rawBall.time,D.rawBall.pos(:,2),'.');
    legend('wp x','wp y', 'raw x', 'raw y')
  else
    legend('wp x','wp y')
  end
  
  title('time -> pos');
  xlabel('time [s]');
  ylabel('pos [mm]');

  if isfield(D, 'nearestBot')
    wpBotPos = D.nearestBot.pos;
  else
    wpBotPos = D.wpBots.pos;
  end
  wpVelDir = D.wpBall.vel(:,1:2);
  wpVel = sqrt( wpVelDir(:,1).*wpVelDir(:,1) + wpVelDir(:,2).*wpVelDir(:,2));
  [~,idxMaxVel] = max(wpVel);
  kickerBotPos = wpBotPos(idxMaxVel,:);

  subplot(2,2,4); hold off;
  plot(D.wpBall.pos(:,1),D.wpBall.pos(:,2));
  hold on;
  plotBot(kickerBotPos);
  plotField(0,1000);
  
  if isfield(D, 'rawBall')
    plot(D.rawBall.pos(:,1),D.rawBall.pos(:,2));
    legend('WP', 'raw');
  else
    legend('WP');
  end
  
  title('pos x->y');
  xlabel('x [mm]'); ylabel('y [mm]');
  
  lastView = @plotBallPos;
end

function plotAccAndTiming(~, ~)
  
  subplot(2,2,1); hold off;
  plot(D.wpBall.time, D.wpBall.acc(:,1:2));
  hold on;
  wpAccAbs = sqrt(D.wpBall.acc(:,1).*D.wpBall.acc(:,1) + D.wpBall.acc(:,2).*D.wpBall.acc(:,2));
  plot(D.wpBall.time, wpAccAbs);
  title('time -> WP acc');
  legend('x','y', 'abs');
  xlabel('time [s]'); ylabel('acc [m/s^2]');

  subplot(2,2,2); hold off;
  if isfield(D, 'rawBall')
      tmp = D.rawBall.pos(2:end,:) - D.rawBall.pos(1:end-1,:);
      dists = sqrt(tmp(:,1).*tmp(:,1) + tmp(:,2).*tmp(:,2));
      plot(D.rawBall.time(2:end), dists);
  else
      plot(0,0);
  end
  title('time -> dist');
  xlabel('time [s]'); ylabel('dist [mm]');

  subplot(2,2,3); hold off;
  if isfield(D, 'rawBall')
      rawDts = (D.rawBall.timestamp(2:end) - D.rawBall.timestamp(1:end-1)) * 1e-9;
      plot(rawDts);
  else
      plot(0,0);
  end
  title('dt');
  xlabel('frameId'); ylabel('dt [s]');

  subplot(2,2,4); hold off;
  if isfield(D, 'rawBall')
    plot(dists);
  else
      plot(0,0);
  end
  title('frameId -> dists');
  xlabel('frameId'); ylabel('dist [mm]');
  lastView = @plotAccAndTiming;
end

function plotBallVel(~, ~) 
      
  subplot(2,2,1); hold off;
  plot(D.wpBall.time, D.wpBall.velAbs);
  hold on;
  if isfield(D, 'rawBall')
      plot(D.rawBall.time, D.rawBall.velAbs);
      plot(D.rawBall.time, -1-D.rawBall.camId);
      legend('WP','raw', '-camId-1');
  else
      legend('WP')
  end
  title('time -> absolute velocity');
  xlabel('time [s]'); ylabel('vel [m/s]');

  
  subplot(2,2,2); hold off;
  wpVelDir = D.wpBall.vel(:,1:2);    
  wpVelTheta = zeros(size(D.wpBall.time));
  vxNonZero = find(wpVelDir(:,1)~=0);
  wpVelTheta(vxNonZero) = atan(wpVelDir(vxNonZero,2)./wpVelDir(vxNonZero,1));
  wpVelTheta(wpVelDir(:,2)~=0 & wpVelDir(:,1)==0) = pi/2;
  plot(D.wpBall.time, wpVelTheta)
  legend('theta');
  title('wp vel orientation');
  xlabel('time [s]'); ylabel('angle [rad]');

  
  subplot(2,2,3); hold off;
  plot(D.wpBall.time, D.wpBall.vel(:,1:2));
  legend('x','y')
  title('time -> wp vel xy');
  xlabel('time [s]'); ylabel('vel [m/s]');

  subplot(2,2,4); hold off;
  plot(D.wpBall.vel(:,1),D.wpBall.vel(:,2),'.');
  hold on;
  [~, maxVeli] = max(D.wpBall.velAbs);
  plot([0,D.wpBall.vel(maxVeli,1)],[0,D.wpBall.vel(maxVeli,2)]);
  title('wp vel x->y');
  legend('xy','0 to maxVel');
  xlabel('vel x [m/s]'); ylabel('vel y [m/s]');
  lastView = @plotBallVel;
end

function plotBallZ(~, ~)
  
  subplot(2,2,1); hold off;
  plot(D.wpBall.time, D.wpBall.vel(:,3));
  hold on;
  if isfield(D, 'rawBall')
    plot(D.rawBall.time, D.rawBall.vel(:,3));
    plot(D.rawBall.time, -1-D.rawBall.camId);
    legend('WP Vel','raw vel','-camId-1');
  else
    legend('WP Vel');
  end
  title('time -> z vel');
  xlabel('time [s]'); ylabel('vel [m/s]');

  subplot(2,2,2); hold off;
  plot(D.wpBall.time,D.wpBall.pos(:,3));
  hold on;
  if isfield(D, 'rawBall')
    plot(D.rawBall.time,D.rawBall.pos(:,3));
    legend('wp', 'raw')
  else
    legend('wp')
  end
  title('time -> height');
  xlabel('time [s]');
  ylabel('pos [mm]');
  
  f=subplot(2,2,3); hold off;
  if isfield(D, 'rawBall')
    plot3(D.rawBall.pos(:,1), D.rawBall.pos(:,2), D.rawBall.pos(:,3));
    hold on;
    plot3(D.rawBall.pos(:,1), D.rawBall.pos(:,2), D.rawBall.pos(:,3));
    legend('wp','raw');
    title('x -> y -> z');
    xlabel('x [mm]'); ylabel('y [mm]'); zlabel('z [mm]');
  else
    delete(f);
  end
  
  
  f=subplot(2,2,4);
  delete(f);
  
  lastView = @plotBallZ;
end

function plotBotTiming(~, ~)
  setupBotSelection(@plotBotPos)    
  if ~isfield(D, 'rawBot')
      f=subplot(1,1,1); hold off;
      delete(f);
      return
  end
      
  subplot(2,2,1); hold off;
  plot(diff(D.rawBot.frameId));
  title('frame id diff')
    
  subplot(2,2,2); hold off;
  tmp = D.rawBot.pos(2:end,:) - D.rawBot.pos(1:end-1,:);
  dists = sqrt(tmp(:,1).*tmp(:,1) + tmp(:,2).*tmp(:,2));
  plot(D.rawBot.time(2:end), dists);
  title('time -> dist');
  xlabel('time [s]'); ylabel('dist [mm]');

  subplot(2,2,3); hold off;
  rawDts = (D.rawBot.timestamp(2:end) - D.rawBot.timestamp(1:end-1)) * 1e-9;
  plot(rawDts);
  title('dt');
  xlabel('frameId'); ylabel('dt [s]');

  subplot(2,2,4); hold off;
  plot(dists);
  title('frameId -> dists');
  xlabel('frameId'); ylabel('dist [mm]');
  lastView = @plotBotTiming;
end

function plotBotDelays(~,~)

    setupBotSelection(@plotBotPos)

    subplot(2,1,1); hold off;
    x = unique([D.botInput.time; D.wpBot.time]);
    x = x(x>=0);
    tSent = interp1(D.botInput.time, D.botInput.tSent, x);
    tAssembly = interp1(D.wpBot.time, D.wpBot.tAssembly, x);
    tDiff = (tSent - tAssembly) / 1e6;
    avgDiff = mean(tDiff);
    plot(x, tDiff);
    hold on
    plot(x, repmat(avgDiff, size(x)));
    xlabel('time [s]')
    ylabel('Δtime [ms]')
    title('botInput.tSent - wpBot.assembly')
    
    if isfield(D, 'rawBot')
        subplot(2,1,2); hold off;
        x = unique([D.rawBot.time; D.wpBot.time]);
        tRaw = interp1(D.rawBot.time, D.rawBot.tAssembly, x);
        tWp= interp1(D.wpBot.time, D.wpBot.tAssembly, x);
        tDiff = (tWp - tRaw) / 1e6;
        avgDiff = mean(tDiff);
        plot(x, tDiff);
        hold on
        plot(x, repmat(avgDiff, size(x)));
        xlabel('time [s]')
        ylabel('Δtime [ms]')
        title('wp - raw assembly time')        
    end

    lastView = @plotBotDelays;
end

function plotBotPos(~, ~)
  setupBotSelection(@plotBotPos)
  if isempty(D.wpBot)
    subplot(1,1,1); hold off; title('Select bot');
    return
  end
  
  plotId = {1,3,2};
  titleName = {'time -> pos x', 'time -> pos y', 'time -> orientation'};
  for i = 1:3
      subplot(2,2,plotId{i}); hold off;
      leg = {'wp'};
      plot(D.wpBot.time,D.wpBot.pos(:,i));
      hold on;
      if isfield(D, 'rawBot')
        plot(D.rawBot.time,D.rawBot.pos(:,i));
        leg{end+1} = 'raw';
      end
      if isfield(D, 'botOutput')
        plot(D.botOutput.time, D.botOutput.pos(:,i));
        leg{end+1} = 'internal';
      end
      if isfield(D, 'botInput')
        plot(D.botInput.time, D.botInput.trajPos(:,i),'.');
        leg{end+1} = 'traj';
        plot(D.botInput.time, D.botInput.setPos(:,i),'.');
        leg{end+1} = 'set';
      end
      legend(leg)
      xlabel('time [s]');
      ylabel('pos [mm]');
      title(titleName{i});
  end

  subplot(2,2,4); hold off;
  leg = {'wp'};
  plot(D.wpBot.pos(:,1),D.wpBot.pos(:,2));
  hold on;
  if isfield(D, 'rawBot')
    plot(D.rawBot.pos(:,1),D.rawBot.pos(:,2));
    leg{end+1} = 'raw';
  end
  if isfield(D, 'botOutput')
    plot(D.botOutput.pos(:,1), D.botOutput.pos(:,2));
    leg{end+1} = 'internal';
  end
  if isfield(D, 'botInput')
    plot(D.botInput.trajPos(:,1), D.botInput.trajPos(:,2),'.');
    leg{end+1} = 'traj';
    plot(D.botInput.setPos(:,1), D.botInput.setPos(:,2),'.');
    leg{end+1} = 'set';
  end
  fieldSize = 1;
  plotField(fieldSize,1000);
  legend(leg);
  xlabel('x [mm]'); ylabel('y [mm]');
  title('pos x->y');
  
  lastView = @plotBotPos;
end
  
function plotBotVel(~,~)
  setupBotSelection(@plotBotVel)
  if isempty(D.wpBot)
    subplot(1,1,1); hold off; title('Select bot');
    return
  end
  subplot(2,2,1); hold off;
  leg = {'WP'};
  plot(D.wpBot.time, D.wpBot.velAbs);
  hold on;
  if isfield(D, 'rawBot')
    plot(D.rawBot.time, D.rawBot.velAbs);
    leg{end+1} = 'raw';
  end
  if isfield(D, 'botOutput')
    plot(D.botOutput.time, D.botOutput.velAbs);
    leg{end+1} = 'internal';
  end
  if isfield(D, 'botInput')
    plot(D.botInput.time, D.botInput.trajVelAbs);
    leg{end+1} = 'traj';
    plot(D.botInput.time, D.botInput.setVelAbs);
    leg{end+1} = 'set';
    plot(D.botInput.time, D.botInput.localVelAbs);
    leg{end+1} = 'local';
  end
  if isfield(D, 'rawBot')
    plot(D.rawBot.time, -0.1-0.1*D.rawBot.camId);
    leg{end+1} = '-camId-1';
  end
  legend(leg);
  xlabel('time [s]'); ylabel('vel [m/s]');
  title('time -> vel');  
  
  
  plotId = {2,4,3};
  titleName = {'time -> vel x', 'time -> vel y', 'time -> rotation'};
  yLabelValue = {'vel x [m/s]', 'vel y [m/s]', 'rotation [rad/s]'};
  for i = 1:3
      subplot(2,2,plotId{i}); hold off;
      leg = {'wp'};
      plot(D.wpBot.time,D.wpBot.vel(:,i));
      hold on;
      if isfield(D, 'rawBot')
        plot(D.rawBot.time,D.rawBot.vel(:,i));
        leg{end+1} = 'raw';
      end
      if isfield(D, 'botOutput')
        plot(D.botOutput.time, D.botOutput.vel(:,i));
        leg{end+1} = 'internal';
      end
      if isfield(D, 'botInput')
        plot(D.botInput.time, D.botInput.trajVel(:,i),'.');
        leg{end+1} = 'traj';
        plot(D.botInput.time, D.botInput.setVel(:,i),'.');
        leg{end+1} = 'set';
      end
      legend(leg)
      xlabel('time [s]');
      ylabel(yLabelValue{i});
      title(titleName{i});
  end
  
  
  lastView = @plotBotVel;
end

function lvel = global2Local(gvel, angle)
  a=-[angle(1); angle(1:end-1)];
  lvel = [cos(a).*gvel(:,1)-sin(a).*gvel(:,2), sin(a).*gvel(:,1) + cos(a).*gvel(:,2), gvel(:,3)];
end

function plotBotVelLocal(~,~)
  setupBotSelection(@plotBotVelLocal)
  if isempty(D.wpBot)
    subplot(1,1,1); hold off; title('Select bot');
    return
  end
    
  for i=1:3
      subplot(3,1,i); hold off;
      leg = {'wp'};
      lvel_wp = global2Local(D.wpBot.vel, D.wpBot.pos(:,3));
      plot(D.wpBot.time, lvel_wp(:,i));
      hold on;
      
      if isfield(D, 'rawBot')
        lvel_raw = global2Local(D.rawBot.vel, D.rawBot.pos(:,3));
        plot(D.rawBot.time, lvel_raw(:,i));
        leg{end+1} = 'raw';          
      end
      if isfield(D, 'botOutput')
        lvel_output = global2Local(D.botOutput.vel, D.botOutput.pos(:,3));
        plot(D.botOutput.time, lvel_output(:,i));
        leg{end+1} = 'internal';          
      end
      if isfield(D, 'botInput')
        lvel_set = global2Local(D.botInput.setVel, D.botInput.setPos(:,3));
        plot(D.botInput.time, lvel_set(:,i));
        leg{end+1} = 'global set';
        lvel_local = D.botInput.localVel(:,[2,1,3]);
        plot(D.botInput.time, lvel_local(:,i));
        leg{end+1} = 'local set';
      end
      legend(leg);
  end
  
  subplot(3,1,1);
  xlabel('time [s]'); ylabel('vel [m/s]');
  title('local vel x');
  ylim([-5,5]);

  subplot(3,1,2);
  xlabel('time [s]'); ylabel('vel [m/s]');
  title('local vel y');
  ylim([-5,5]);

  subplot(3,1,3);
  xlabel('time [s]'); ylabel('vel [rad/s]');
  title('local vel w');
  ylim([-10,10]);

  lastView = @plotBotVelLocal;
end

function plotBotDeviceInfo(~,~)
    
    subplot(3,1,1); hold off;
    plot(D.botInput.time, D.botInput.kickSpeed);
    xlabel('time [s]')
    ylabel('speed [m/s]')
    title('Set Kickspeed')
    
    subplot(3,1,2); hold off;
    plot(D.botInput.time, D.botInput.kickMode);
    hold on
    plot(D.botInput.time, D.botInput.kickDevice);
    legend('kick mode', 'kick device')
    xlabel('time [s]')
    ylabel('selection')
    title('Set Device + Mode')
    
    subplot(3,1,3); hold off;
    plot(D.botInput.time, D.botInput.dribbleRpm);
    xlabel('time [s]')
    ylabel('dribble speed [RPM]')
    title('Set dribble RPM')
    
    lastView = @plotBotDeviceInfo;
end

function plotBotLimits(~,~)
    
    subplot(2,2,1); hold off;
    plot(D.botInput.time, D.botInput.velMax)
    hold on
    plot(D.botInput.time, D.botInput.velMaxFast)
    plot(D.botInput.time, D.botInput.fastMove)
    legend('velMax', 'velMaxFast', 'fastMove')
    xlabel('time [s]')
    ylabel('vel [m/s]')
    title('Vel xy limits')
    
    subplot(2,2,2); hold off;
    plot(D.botInput.time, D.botInput.velMaxW)
    hold on
    plot(D.botInput.time, D.botInput.fastMove)
    legend('velMaxW', 'fastMove')
    xlabel('time [s]')
    ylabel('vel [rad/s]')
    title('Vel w limits')
    
    subplot(2,2,3); hold off;
    plot(D.botInput.time, D.botInput.accMax)
    hold on
    plot(D.botInput.time, D.botInput.accMaxFast)
    plot(D.botInput.time, D.botInput.fastMove)
    legend('accMax', 'accMaxFast', 'fastMove')
    xlabel('time [s]')
    ylabel('acc [m/s^2]')
    title('Acc xy limits')
    
    subplot(2,2,4); hold off;
    plot(D.botInput.time, D.botInput.accMaxW)
    hold on
    plot(D.botInput.time, D.botInput.fastMove)
    legend('accMaxW', 'fastMove')
    xlabel('time [s]')
    ylabel('acc [m/s^2]')
    title('Acc w limits')
    
    lastView = @plotBotLimits;
end

end