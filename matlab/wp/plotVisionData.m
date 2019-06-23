function plotVisionData( folder )
addpath('plotting');

% global D;
% 
% global rawBall;
% global wpBall;
% global rawBallCorr;
% global wpBallUpd;
% 
% global wpBot;
% global wpBotUpd;
% global rawBot;
% global skillBot;
% global isBot;

global botId;
global botColor;

D = [];

rawBall = [];
wpBall = [];
rawBallCorr = [];
wpBallUpd = [];

wpBot = [];
wpBotUpd = [];
rawBot = [];
skillBot = [];
isBot = [];

lastView = @plotBallVel;

newWindow(0,0)

% if ~exist('folder','var')
%   folder = '../data/vision/';
% end

reload(0,0);
  
function newWindow(~,~)
  if exist('folder','var')
    figure('Name', strcat('VisionDataViewer: ', folder), 'NumberTitle', 'Off');
  else
    figure('Name', strcat('VisionDataViewer: ', ''), 'NumberTitle', 'Off');
  end
  m = uimenu('Label', 'View');
  uimenu(m, 'Label', 'Load File', 'Callback', @loadData);
  uimenu(m, 'Label', 'Reload', 'Callback', @reload);
  uimenu(m, 'Label', 'New Window', 'Callback', @newWindow);
  uimenu(m, 'Label', 'ball acc and timing', 'Callback', @plotAccAndTiming);
  uimenu(m, 'Label', 'bot timing', 'Callback', @plotBotAccAndTiming);
  uimenu(m, 'Label', 'ball vel', 'Callback', @plotBallVel);
  uimenu(m, 'Label', 'ball pos', 'Callback', @plotBallPos);
  uimenu(m, 'Label', 'ball z', 'Callback', @plotBallZ);
  uimenu(m, 'Label', 'bot pos', 'Callback', @plotBotPos);
  uimenu(m, 'Label', 'bot vel', 'Callback', @plotBotVel);
  uimenu(m, 'Label', 'bot vel 2', 'Callback', @plotBotVel2);
  uimenu(m, 'Label', 'bot vel local', 'Callback', @plotBotVelLocal);
  uimenu(m, 'Label', 'bot acc', 'Callback', @plotBotAcc);
end

function loadFolder(folder)
  D = data.loadAll(folder);
  rawBall = D.rawBall;
  wpBall = D.wpBall;
  
  timeOffset = rawBall.timestamp(1);
  rawBall.time = (rawBall.timestamp - timeOffset) / 1e9;
  wpBall.time = (wpBall.timestamp - timeOffset) / 1e9;
  
  if isfield(D,'rawBallCorrected')
    rawBallCorr = D.rawBallCorrected;
    rawBallCorr.time = (rawBallCorr.timestamp - timeOffset) / 1e9;
  else
    rawBallCorr = [];
  end
  if isfield(D,'wpBallUpdated')
    wpBallUpd = D.wpBallUpdated;
    wpBallUpd.time = (wpBallUpd.timestamp - timeOffset) / 1e9;
  else
    wpBallUpd = [];
  end
  
  % filter duplicate balls
  idx = find(rawBall.timestamp(2:end) ~= rawBall.timestamp(1:end-1));
  fields = fieldnames(rawBall);
  for f=1:length(fields)
    rawBall.(fields{f}) = rawBall.(fields{f})(idx,:);
  end
  
  loadBotData
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
%   maps = source.String;

  uIds = getAvailableBots();
  botId = uIds(val,1);
  botColor = uIds(val,2);
  
  loadBotData
  cb(0,0);
end

function uIds = getAvailableBots()
  uIds = unique([D.wpBots.id, D.wpBots.color],'rows');
end

function loadBotData()
  rawBot = [];
  wpBot = [];
  wpBotUpd = [];
  skillBot = [];
  isBot = [];
  
  if isempty(D.wpBots.id)
    return
  end
  if isempty(botId)
    botId = D.wpBots.id(1);
    botColor = D.wpBots.color(1);
  end

  idx = D.rawBots.id == botId & D.rawBots.color == botColor;
  if ~any(idx)
    return
  end
  
  rawBot.pos = D.rawBots.pos(idx,:);
  rawBot.camId = D.rawBots.camId(idx,:);
  rawBot.frameId = D.rawBots.frameId(idx,:);
  rawBot.timestamp = D.rawBots.timestamp(idx,:);
  
  timeOffset = rawBot.timestamp(1);

  rawBot.time = (rawBot.timestamp-timeOffset)/1e9;
  rawBot.vel = calcVel(rawBot.pos, rawBot.timestamp);
  rawBot.velAbs = sqrt( rawBot.vel(:,1).*rawBot.vel(:,1) + rawBot.vel(:,2).*rawBot.vel(:,2));
  rawBot.acc = calcAcc(rawBot.vel, rawBot.timestamp);
  rawBot.accAbs = sqrt( rawBot.acc(:,1).*rawBot.acc(:,1) + rawBot.acc(:,2).*rawBot.acc(:,2));
  
%   windowSize = 5;
%   b = (1/windowSize)*ones(1,windowSize);
%   a = 1;
  rawBot.posIntp = rawBot.pos;
%   rawBot.posIntp(:,3) = filter(b,a,rawBot.pos(:,3));
%   rawBot.velIntp = calcVel(rawBot.posIntp, rawBot.timestamp);
  rawBot.velIntp = rawBot.vel;
  rawBot.timeIntp = rawBot.time;
  
%   a = 5;
%   rawBot.velIntp = calcVel(rawBot.pos(1:a:end,:), rawBot.timestamp(1:a:end,:));
%   rawBot.timeIntp = (rawBot.timestamp(1:a:end,:)-timeOffset)/1e9;
  
  
  wpBot = getWpBot(D.wpBots, timeOffset);
  
  if isfield(D,'wpBotsUpdated')
    wpBotUpd = getWpBot(D.wpBotsUpdated, timeOffset);
%     wpBot = wpBotUpd;
%     wpBotUpd = [];
  end
  
  if isfield(D,'isBots')
    isBot = getWpBot(D.isBots, timeOffset);
  end
  
  if isfield(D,'skillBots')
    idx = D.skillBots.id == botId & D.skillBots.color == botColor;
    if any(idx)
      skillBot.trajVel = D.skillBots.trajVel(idx,:);
      skillBot.trajPos = D.skillBots.trajPos(idx,:);
      skillBot.setVel = D.skillBots.setVel(idx,:);
      skillBot.setPos = D.skillBots.setPos(idx,:);
      skillBot.localVel = D.skillBots.localVel(idx,:);
      skillBot.timestamp = D.skillBots.timestamp(idx,:);

      skillBot.time = (skillBot.timestamp-timeOffset)/1e9;
      skillBot.trajVelAbs = ...
          sqrt( skillBot.trajVel(:,1).*skillBot.trajVel(:,1) ...
        + skillBot.trajVel(:,2).*skillBot.trajVel(:,2));
      skillBot.setVelAbs = ...
          sqrt( skillBot.setVel(:,1).*skillBot.setVel(:,1) ...
        + skillBot.setVel(:,2).*skillBot.setVel(:,2));
      skillBot.localVelAbs = ...
          sqrt( skillBot.localVel(:,1).*skillBot.localVel(:,1) ...
        + skillBot.localVel(:,2).*skillBot.localVel(:,2));
    end
  end
end

function bot = getWpBot(bots, timeOffset)
  idx = bots.id == botId & bots.color == botColor;
  bot = [];
  if size(bots.timestamp(idx,:),1) == 0
    return
  end
  bot.pos = bots.pos(idx,:);
  bot.vel = bots.vel(idx,:);
  bot.acc = bots.acc(idx,:);
  bot.frameId = bots.frameId(idx,:);
  bot.timestamp = bots.timestamp(idx,:);

  bot.time = (bot.timestamp-timeOffset)/1e9;
  bot.velAbs = sqrt( bot.vel(:,1).*bot.vel(:,1) + bot.vel(:,2).*bot.vel(:,2));
  bot.accAbs = sqrt( bot.acc(:,1).*bot.acc(:,1) + bot.acc(:,2).*bot.acc(:,2));
  
%   bot.velTheta = zeros(size(bot.time));
  vxNonZero = find(abs(bot.vel(:,1))>0.05);
  bot.velTheta = atan2(bot.vel(vxNonZero,2), bot.vel(vxNonZero,1));
  bot.timeVelTheta = bot.time(vxNonZero);
%   bot.velTheta(vxNonZero) = atan(bot.vel(vxNonZero,2)./bot.vel(vxNonZero,1));
%   bot.velTheta(bot.vel(:,2)~=0 & bot.vel(:,1)==0) = pi/2;
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
  plot(wpBall.pos(:,1),wpBall.pos(:,2));
  hold on;
  plot([rawBall.pos(1,1);rawBall.pos(end,1)],[rawBall.pos(1,2);rawBall.pos(end,2)]);
  plot(rawBall.pos(1,1),rawBall.pos(1,2),'*');
  leg = cell(1,length(unique(rawBall.camId))+3);
  leg{1} = 'WP';
  leg{2} = 'raw linear line';
  leg{3} = 'raw start';
  i=4;
  for c=unique(rawBall.camId)'
    plot(rawBall.pos(rawBall.camId==c,1),rawBall.pos(rawBall.camId==c,2),'.');
    leg{i} = sprintf('raw cam %d', c);
    i = i + 1;
  end
  title('WP and raw pos with cam info');
  legend(leg);
  xlabel('x [mm]'); ylabel('y [mm]');
  axis equal;

  
  allRawBallsCam = D.rawBalls.camId;
  allRawBallsPos = D.rawBalls.pos;
  
  subplot(2,2,2); hold off;
  plot(wpBall.pos(:,1),wpBall.pos(:,2));
  hold on;
  plot(wpBall.pos(1,1),wpBall.pos(1,2),'*');
  leg = cell(1,length(unique(allRawBallsCam))+2);
  leg{1} = 'WP';
  leg{2} = 'raw start';
  i=3;
  for c=unique(allRawBallsCam)'
    plot(allRawBallsPos(allRawBallsCam==c,1),allRawBallsPos(allRawBallsCam==c,2),'.');
    leg{i} = sprintf('raw cam %d', c);
    i = i + 1;
  end
  title('all raw pos with cam info');
  legend(leg);
  xlabel('x [mm]'); ylabel('y [mm]');
  axis equal;


  subplot(2,2,3); hold off;
  plot(wpBall.time,wpBall.pos(:,1),'.');
  hold on;
  plot(wpBall.time,wpBall.pos(:,2),'.');
  plot(rawBall.time,rawBall.pos(:,1),'.');
  plot(rawBall.time,rawBall.pos(:,2),'.');
  title('time -> pos');
  legend('wp x','wp y', 'raw x', 'raw y')
  xlabel('time [s]');
  ylabel('pos [mm]');

  if isfield(D, 'nearestBot')
    wpBotPos = D.nearestBot.pos;
  else
    wpBotPos = D.wpBots.pos;
  end
  wpVelDir = wpBall.vel(:,1:2);
  wpVel = sqrt( wpVelDir(:,1).*wpVelDir(:,1) + wpVelDir(:,2).*wpVelDir(:,2));
  [~,idxMaxVel] = max(wpVel);
  kickerBotPos = wpBotPos(idxMaxVel,:);

  subplot(2,2,4); hold off;
  plot(rawBall.pos(:,1),rawBall.pos(:,2));
  hold on;
  plot(wpBall.pos(:,1),wpBall.pos(:,2));
  plotBot(kickerBotPos);
  plotField(0,1000);
  title('pos x->y');
  legend('raw','WP');
  xlabel('x [mm]'); ylabel('y [mm]');
  
  lastView = @plotBallPos;
end

function plotAccAndTiming(~, ~)
  rawDts = (rawBall.timestamp(2:end) - rawBall.timestamp(1:end-1)) * 1e-9;
  
  tmp = rawBall.pos(2:end,:) - rawBall.pos(1:end-1,:);
  dists = sqrt(tmp(:,1).*tmp(:,1) + tmp(:,2).*tmp(:,2));
  
  wpAccAbs = sqrt(wpBall.acc(:,1).*wpBall.acc(:,1) + wpBall.acc(:,2).*wpBall.acc(:,2));

  subplot(2,2,1); hold off;
  plot(wpBall.time, wpBall.acc(:,1:2));
  hold on;
  plot(wpBall.time, wpAccAbs);
  title('time -> WP acc');
  legend('x','y', 'abs');
  xlabel('time [s]'); ylabel('acc [m/s^2]');

  subplot(2,2,2); hold off;
  plot(rawBall.time(2:end), dists);
  title('time -> dist');
  xlabel('time [s]'); ylabel('dist [mm]');

  subplot(2,2,3); hold off;
  plot(rawDts);
  title('dt');
  xlabel('frameId'); ylabel('dt [s]');

  subplot(2,2,4); hold off;
  plot(dists);
  title('frameId -> dists');
  xlabel('frameId'); ylabel('dist [mm]');
  lastView = @plotAccAndTiming;
end

function plotBotAccAndTiming(~, ~)
  rawDts = (rawBot.timestamp(2:end) - rawBot.timestamp(1:end-1)) * 1e-9;
  
  tmp = rawBot.pos(2:end,:) - rawBot.pos(1:end-1,:);
  dists = sqrt(tmp(:,1).*tmp(:,1) + tmp(:,2).*tmp(:,2));
  
  subplot(2,2,1); hold off;
  plot(diff(rawBot.frameId));
  title('frame id diff')
    
  subplot(2,2,2); hold off;
  plot(rawBot.time(2:end), dists);
  title('time -> dist');
  xlabel('time [s]'); ylabel('dist [mm]');

  subplot(2,2,3); hold off;
  plot(rawDts);
  title('dt');
  xlabel('frameId'); ylabel('dt [s]');

  subplot(2,2,4); hold off;
  plot(dists);
  title('frameId -> dists');
  xlabel('frameId'); ylabel('dist [mm]');
  lastView = @plotBotAccAndTiming;
end

function plotBallVel(~, ~)  
  tmp = rawBall.pos(2:end,:) - rawBall.pos(1:end-1,:);
  dists = sqrt(tmp(:,1).*tmp(:,1) + tmp(:,2).*tmp(:,2));
  dts = (rawBall.timestamp(2:end) - rawBall.timestamp(1:end-1)) * 1e-9;
  vel = [0; dists ./ (1000 * dts)];
  lastVel = 0;
  for i=1:length(dts)
    if dts(i) < 1e-4
      vel(i+1) = lastVel;
    else
      lastVel = vel(i);
    end
  end
  
  wpVelDir = wpBall.vel(:,1:2);
  wpVel = sqrt( wpVelDir(:,1).*wpVelDir(:,1) + wpVelDir(:,2).*wpVelDir(:,2));
  
  if ~isempty(wpBallUpd)
    wpUpdVelDir = wpBallUpd.vel(:,1:2);
    wpUpdVel = sqrt( wpUpdVelDir(:,1).*wpUpdVelDir(:,1) + wpUpdVelDir(:,2).*wpUpdVelDir(:,2));
  end
  
  wpVelTheta = zeros(size(wpBall.time));
  vxNonZero = find(wpVelDir(:,1)~=0);
  wpVelTheta(vxNonZero) = atan(wpVelDir(vxNonZero,2)./wpVelDir(vxNonZero,1));
  wpVelTheta(wpVelDir(:,2)~=0 & wpVelDir(:,1)==0) = pi/2;
  
  if ~isempty(wpBallUpd)
    wpUpdVelTheta = zeros(size(wpBallUpd.time));
    vxNonZero = find(wpUpdVelDir(:,1)~=0);
    wpUpdVelTheta(vxNonZero) = atan(wpUpdVelDir(vxNonZero,2)./wpUpdVelDir(vxNonZero,1));
    wpUpdVelTheta(wpUpdVelDir(:,2)~=0 & wpUpdVelDir(:,1)==0) = pi/2;
  end
  
  subplot(2,2,1); hold off;
  plot(rawBall.time, -1-rawBall.camId);
  hold on;
  plot(rawBall.time, vel);
  plot(wpBall.time, wpVel);
  if ~isempty(wpBallUpd)
    plot(wpBallUpd.time, wpUpdVel);
    plot(wpBallUpd.time, -wpBallUpd.confidence/2);
    legend('-camId-1','raw vel','WP Vel','WP Vel upd', 'conf');
  else
    legend('-camId-1','raw vel','WP Vel');
  end
  title('time -> vel');
  xlabel('time [s]'); ylabel('vel [m/s]');

  subplot(2,2,2); hold off;
%   polar(wpVelTheta, wpBall.time);
  plot(wpBall.time, wpVelTheta)
  hold on
  if ~isempty(wpBallUpd)
    plot(wpBallUpd.time, wpUpdVelTheta)
    legend('theta','theta upd');
  else
    legend('theta');
  end
  title('wp vel orientation');
  xlabel('time [s]'); ylabel('angle [rad]');
%   ylim([-pi,pi]);

  subplot(2,2,3); hold off;
  plot(wpBall.time, wpVelDir);
  hold on
  if ~isempty(wpBallUpd)
    plot(wpBallUpd.time, wpUpdVelDir);
    legend('x','y','x upd','y upd')
  else
    legend('x','y')
  end
  title('time -> wp vel xy');
  xlabel('time [s]'); ylabel('vel [m/s]');

  subplot(2,2,4); hold off;
  plot(wpVelDir(:,1),wpVelDir(:,2),'.');
  hold on;
  [~, maxVeli] = max(wpVel);
  plot([0,wpVelDir(maxVeli,1)],[0,wpVelDir(maxVeli,2)]);
  title('wp vel x->y');
  legend('xy','0 to maxVel');
  xlabel('vel [m/s]'); ylabel('vel [m/s]');
  lastView = @plotBallVel;
end

function plotBallZ(~, ~)
  dists = rawBall.pos(2:end,3) - rawBall.pos(1:end-1,3);
  dts = (rawBall.timestamp(2:end) - rawBall.timestamp(1:end-1)) * 1e-9;
  vel = [0; dists ./ (1000 * dts)];
  
  subplot(2,2,1); hold off;
  plot(wpBall.time, wpBall.vel(:,3));
  hold on;
  plot(rawBall.time, vel);
  plot(rawBall.time, -1-rawBall.camId);
  title('time -> z vel');
  legend('WP Vel','raw vel','-camId-1');
  xlabel('time [s]'); ylabel('vel [m/s]');

  subplot(2,2,2); hold off;
  plot(wpBall.time,wpBall.pos(:,3));
  hold on;
  plot(rawBall.time,rawBall.pos(:,3));
  title('time -> height');
  legend('wp', 'raw')
  xlabel('time [s]');
  ylabel('pos [mm]');
  
  subplot(2,2,3); hold off;
  plot3(rawBall.pos(:,1), rawBall.pos(:,2), rawBall.pos(:,3));
  hold on;
  plot3(rawBall.pos(:,1), rawBall.pos(:,2), rawBall.pos(:,3));
  title('x -> y -> z');
  legend('wp','raw');
  xlabel('x [mm]'); ylabel('y [mm]'); zlabel('z [mm]');
  lastView = @plotBallZ;
end

function plotBotPos(~, ~)
  setupBotSelection(@plotBotPos)
  if isempty(wpBot)
    subplot(1,1,1); hold off; title('Select bot');
    return
  end
  
  subplot(2,2,1); hold off;
  leg = {'raw', 'wp'};
  plot(rawBot.time,rawBot.pos(:,1));
  hold on;
  plot(wpBot.time,wpBot.pos(:,1));
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.time, wpBotUpd.pos(:,1))
    leg{end+1} = 'WP upd';
  end
  if ~isempty(isBot)
    plot(isBot.time, isBot.pos(:,1));
    leg{end+1} = 'internal';
  end
  if ~isempty(skillBot)
%     plot(skillBot.time,skillBot.trajPos(:,1:2),'.');
    plot(skillBot.time,skillBot.setPos(:,1));
    leg{end+1} = 'set';
  end
  legend(leg)
  xlabel('time [s]');
  ylabel('pos [mm]');
  title('time -> pos x');

  subplot(2,2,3); hold off;
  leg = {'raw', 'wp'};
  plot(rawBot.time,rawBot.pos(:,2));
  hold on;
  plot(wpBot.time,wpBot.pos(:,2));
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.time, wpBotUpd.pos(:,2))
    leg{end+1} = 'WP upd';
  end
  if ~isempty(isBot)
    plot(isBot.time, isBot.pos(:,2));
    leg{end+1} = 'internal';
  end
  if ~isempty(skillBot)
%     plot(skillBot.time,skillBot.trajPos(:,1:2),'.');
    plot(skillBot.time,skillBot.setPos(:,2));
    leg{end+1} = 'set';
  end
  legend(leg)
  xlabel('time [s]');
  ylabel('pos [mm]');
  title('time -> pos y');

  subplot(2,2,2); hold off;
  leg = {'raw'};
  plot(rawBot.time, rawBot.pos(:,3));
  hold on
%   polar(rawBot.posIntp(:,3), rawBot.timeIntp);
%   leg{end+1} = {'intp'};
  plot(wpBot.time,wpBot.pos(:,3));
  leg{end+1} = 'wp';
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.time, wpBotUpd.pos(:,3));
    leg{end+1} = 'WP upd';
  end
  if ~isempty(isBot)
    plot(isBot.time, isBot.pos(:,3));
    leg{end+1} = 'internal';
  end
  if ~isempty(skillBot)
    plot(skillBot.time,skillBot.setPos(:,3));
    leg{end+1} = 'set';
    plot(skillBot.time,skillBot.trajPos(:,3));
    leg{end+1} = 'traj';
  end
  legend(leg);
  xlabel('time [s]');
  ylabel('orientation [rad]');
  title('WP and raw orientation');

  subplot(2,2,4); hold off;
  leg = {'raw', 'wp'};
  plot(rawBot.pos(:,1),rawBot.pos(:,2));
  hold on;
  plot(wpBot.pos(:,1),wpBot.pos(:,2));
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.pos(:,1),wpBotUpd.pos(:,2))
    leg{end+1} = 'WP upd';
  end
  if ~isempty(isBot)
    plot(isBot.pos(:,1), isBot.pos(:,2));
    leg{end+1} = 'internal';
  end
  if ~isempty(skillBot)
    plot(skillBot.setPos(:,1),skillBot.setPos(:,2));
    leg{end+1} = 'set';
%     plot(skillBot.trajPos(:,1),skillBot.trajPos(:,2));
%     leg{end+1} = 'traj';
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
  if isempty(wpBot)
    subplot(1,1,1); hold off; title('Select bot');
    return
  end
  subplot(2,2,1); hold off;
  leg = {'raw vel','WP Vel'};
  plot(rawBot.time, rawBot.velAbs);
  hold on;
  plot(wpBot.time, wpBot.velAbs);
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.time, wpBotUpd.velAbs)
    leg{end+1} = 'WP upd';
  end
  if ~isempty(isBot)
    plot(isBot.time, isBot.velAbs)
    leg{end+1} = 'internal';
  end
  if ~isempty(skillBot)
%     plot(skillBot.time, skillBot.trajVelAbs);
%     leg{end+1} = 'traj vel';
    plot(skillBot.time, skillBot.setVelAbs);
    leg{end+1} = 'set vel';
    plot(skillBot.time, skillBot.localVelAbs);
    leg{end+1} = 'local set vel';
  end
  plot(rawBot.time, -0.1-0.1*rawBot.camId);
  leg{end+1} = '-camId-1';
  legend(leg);
  xlabel('time [s]'); ylabel('vel [m/s]');
  title('time -> vel');  

  subplot(2,2,3); hold off;
  plot(rawBot.time, rawBot.vel(:,3));
  hold on;
  plot(wpBot.time, wpBot.vel(:,3));
  leg = {'raw','wp'};
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.time, wpBotUpd.vel(:,3))
    leg{end+1} = 'WP upd';
  end
  if ~isempty(isBot)
    plot(isBot.time, isBot.vel(:,3))
    leg{end+1} = 'internal';
  end
  if ~isempty(skillBot)
    plot(skillBot.time, skillBot.setVel(:,3));
    leg{end+1} = 'set';
    plot(skillBot.time, skillBot.localVel(:,3));
    leg{end+1} = 'local set';
  end
%   plot(rawBot.timeIntp, rawBot.velIntp(:,3));
%   leg{end+1} = 'intp';
  xlabel('time [s]'); ylabel('rotation [rad/s]');
  ylim([-pi,pi]);
  legend(leg);
  title('time -> rotation');

  for d=1:2
    subplot(2,2,2 + (d-1)*2); hold off; 
    plot(rawBot.time, rawBot.vel(:,d));
    hold on
    plot(wpBot.time, wpBot.vel(:,d));
    leg = {'raw','wp'};
    if ~isempty(wpBotUpd)
      plot(wpBotUpd.time, wpBotUpd.vel(:,d))
      leg{end+1} = 'WP upd';
    end
    if ~isempty(isBot)
      plot(isBot.time, isBot.vel(:,d))
      leg{end+1} = 'internal';
    end
    if ~isempty(skillBot)
  %     plot(skillBot.time, skillBot.trajVel(:,d));
  %     leg{end+1} = 'traj';
      plot(skillBot.time, skillBot.setVel(:,d));
      leg{end+1} = 'set';
    end
    legend(leg);
    xlabel('time [s]'); ylabel('vel [m/s]');
    if d==1
      title('time -> vel x');
    else
      title('time -> vel y');
    end
  end
  
  lastView = @plotBotVel;
end
  
function plotBotVel2(~,~)
  setupBotSelection(@plotBotVel2)
  if isempty(wpBot)
    subplot(1,1,1); hold off; title('Select bot');
    return
  end

  subplot(1,2,1); hold off; 
  plot(wpBot.vel(:,1),wpBot.vel(:,2),'.');
  hold on;
  [~, maxVeli] = max(wpBot.velAbs);
  plot([0,wpBot.vel(maxVeli,1)],[0,wpBot.vel(maxVeli,2)]);
  legend('xy','0 to maxVel');
  xlabel('vel [m/s]'); ylabel('vel [m/s]');
  title('wp vel x->y');
  
  subplot(1,2,2); hold off;
  leg = {'wp'};
  plot(wpBot.timeVelTheta, wpBot.velTheta);
  hold on;
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.timeVelTheta, wpBotUpd.velTheta);
    leg{end+1} = 'wp upd';
  end
  legend(leg);
  xlabel('time [s]');
  ylabel('rotation [rad/s]');
  title('vel dir');
  
  lastView = @plotBotVel2;
end

function lvel = global2Local(gvel, angle)
  a=-[angle(1); angle(1:end-1)];
  lvel = [cos(a).*gvel(:,1)-sin(a).*gvel(:,2), sin(a).*gvel(:,1) + cos(a).*gvel(:,2), gvel(:,3)];
end

function plotBotVelLocal(~,~)
  setupBotSelection(@plotBotVelLocal)
  if isempty(wpBot)
    subplot(1,1,1); hold off; title('Select bot');
    return
  end
    
  lvel_raw = global2Local(rawBot.vel, rawBot.pos(:,3));
  lvel_wp = global2Local(wpBot.vel, wpBot.pos(:,3));
  lvel_internal = global2Local(isBot.vel, isBot.pos(:,3));
  lvel_set = global2Local(skillBot.setVel, skillBot.setPos(:,3));
  lvel_local = skillBot.localVel(:,[2,1,3]);
  
  subplot(3,1,1); hold off;
  plot(rawBot.time, lvel_raw(:,1));
  hold on;
  plot(wpBot.time, lvel_wp(:,1));
  plot(isBot.time, lvel_internal(:,1));
  plot(skillBot.time, lvel_set(:,1));
  plot(skillBot.time, lvel_local(:,1));
  legend('raw','wp','internal','set','skill');
  xlabel('time [s]'); ylabel('vel [m/s]');
  title('local vel x');
  ylim([-5,5]);

  subplot(3,1,2); hold off;
  plot(rawBot.time, lvel_raw(:,2));
  hold on;
  plot(wpBot.time, lvel_wp(:,2));
  plot(isBot.time, lvel_internal(:,2));
  plot(skillBot.time, lvel_set(:,2));
  plot(skillBot.time, lvel_local(:,2));
  legend('raw','wp','internal','set','skill');
  xlabel('time [s]'); ylabel('vel [m/s]');
  title('local vel y');
  ylim([-5,5]);

  subplot(3,1,3); hold off;
  plot(rawBot.time, lvel_raw(:,3));
  hold on;
  plot(wpBot.time, lvel_wp(:,3));
  plot(isBot.time, lvel_internal(:,3));
  plot(skillBot.time, lvel_set(:,3));
  plot(skillBot.time, lvel_local(:,3));
  legend('raw','wp','internal','set','skill');
  xlabel('time [s]'); ylabel('vel [rad/s]');
  title('local vel w');
  ylim([-10,10]);

  lastView = @plotBotVelLocal;
end
  
function plotBotAcc(~,~)
  setupBotSelection(@plotBotAcc)
  if isempty(wpBot)
    subplot(1,1,1); hold off; title('Select bot');
    return
  end
  subplot(2,2,1); hold off;
  leg = {'raw','WP'};
  plot(rawBot.time, rawBot.accAbs);
  hold on;
  plot(wpBot.time, wpBot.accAbs);
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.time, wpBotUpd.accAbs)
    leg{end+1} = 'WP upd';
  end
%   if ~isempty(isBot)
%     plot(isBot.time, isBot.accAbs)
%     leg{end+1} = 'internal';
%   end
%   if ~isempty(skillBot)
% %     plot(skillBot.time, skillBot.trajVelAbs);
% %     leg{end+1} = 'traj vel';
%     plot(skillBot.time, skillBot.setVelAbs);
%     leg{end+1} = 'set vel';
%     plot(skillBot.time, skillBot.localVelAbs);
%     leg{end+1} = 'local set vel';
%   end
  plot(rawBot.time, -0.1-0.1*rawBot.camId);
  leg{end+1} = '-camId-1';
  legend(leg);
  xlabel('time [s]'); ylabel('acc [m/s²]');
  title('time -> acc');
  ylim([-1, 10]);

  subplot(2,2,3); hold off;
  plot(rawBot.time, rawBot.acc(:,3));
  hold on;
  plot(wpBot.time, wpBot.acc(:,3));
  leg = {'raw','wp'};
  if ~isempty(wpBotUpd)
    plot(wpBotUpd.time, wpBotUpd.acc(:,3))
    leg{end+1} = 'WP upd';
  end
%   if ~isempty(isBot)
%     plot(isBot.time, isBot.acc(:,3))
%     leg{end+1} = 'internal';
%   end
%   if ~isempty(skillBot)
%     plot(skillBot.time, skillBot.setVel(:,3));
%     leg{end+1} = 'set';
%     plot(skillBot.time, skillBot.localVel(:,3));
%     leg{end+1} = 'local set';
%   end
%   plot(rawBot.timeIntp, rawBot.velIntp(:,3));
%   leg{end+1} = 'intp';
  xlabel('time [s]'); ylabel('rotation acc [rad/s²]');
  ylim([-pi,pi]);
  legend(leg);
  title('time -> rotation acc');
  ylim([-10, 10]);

  for d=1:2
    subplot(2,2,2 + (d-1)*2); hold off; 
    plot(rawBot.time, rawBot.acc(:,d));
    hold on
    plot(wpBot.time, wpBot.acc(:,d));
    leg = {'raw','wp'};
    if ~isempty(wpBotUpd)
      plot(wpBotUpd.time, wpBotUpd.acc(:,d))
      leg{end+1} = 'WP upd';
    end
%     if ~isempty(isBot)
%       plot(isBot.time, isBot.acc(:,d))
%       leg{end+1} = 'internal';
%     end
%     if ~isempty(skillBot)
%   %     plot(skillBot.time, skillBot.trajVel(:,d));
%   %     leg{end+1} = 'traj';
%       plot(skillBot.time, skillBot.setVel(:,d));
%       leg{end+1} = 'set';
%     end
    legend(leg);
    xlabel('time [s]'); ylabel('acc [m/s²]');
    if d==1
      title('time -> acc x');
    else
      title('time -> acc y');
    end
  end
  
  lastView = @plotBotAcc;
end

function [vel] = calcVel(pos, timeNs)
  vel = zeros(size(pos));

  tmp = pos(2:end,:) - pos(1:end-1,:);
  dts = [0; (timeNs(2:end) - timeNs(1:end-1)) * 1e-9];
  vel(2:end,1:2) = tmp(:,1:2) ./ repmat(1000 * dts(2:end), [1,2]);
  vel(2:end,3) = util.math.normalizeAngle(tmp(:,3)) ./ dts(2:end);
  lastVel = 0;
  for i=2:size(vel,1)
    if dts(i) < 1e-4
      vel(i,:) = lastVel;
    else
      lastVel = vel(i,:);
    end
  end
end

function [acc] = calcAcc(vel, timeNs)
  acc = zeros(size(vel));

  tmp = vel(2:end,:) - vel(1:end-1,:);
  dts = [0; (timeNs(2:end) - timeNs(1:end-1)) * 1e-9];
  acc(2:end,:) = tmp(:,:) ./ repmat(dts(2:end), [1,3]);
  lastAcc = zeros(1, size(vel,2));
  for i=2:size(acc,1)
    if dts(i) < 1e-4 
      acc(i,:) = lastAcc;
      continue
    end
    if all(abs(acc(i,1:2)) > 10)
      acc(i,1:2) = lastAcc(1:2);
      continue
    end
    if acc(i,3) > 100
      acc(i,3) = lastAcc(3);
      continue
    end
    
    lastAcc = acc(i,:);
  end
end

end