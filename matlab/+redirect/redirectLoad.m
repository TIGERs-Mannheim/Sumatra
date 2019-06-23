function [result] = redirectLoad(verbose)
  basePath='/home/geforce/workspace/Sumatra/logs/moduli_default/redirect/';
  files=dir(strcat(basePath,'*-summary.csv'));
%   files=dir(strcat(basePath,'1415213604083-summary.csv'));
  sortedOutDir = 'sortedOut/';
  mkdir(strcat(basePath,sortedOutDir));
  numAccepted = 0;
  numBallDist = 0;
  numAngle = 0;
  numAngleDiff = 0;
  numNed = 0;
  result = [];
  if ~exist('verbose','var')
    verbose = true;
  end
  for file = files'
    summaryFileName = file.name;
    dataFileName = strcat(file.name(1:13),'.csv');
    fprintf('\n%s...',dataFileName);
    T = importdata(strcat(basePath,dataFileName));
    if size(T,1) < 20
      numNed = numNed +1;
      if verbose
        fprintf(' not enough data');
%         warning('redirectBallDataPlot:data', 'not enough data');
      end
      sortOut(basePath,sortedOutDir,dataFileName,summaryFileName);
      continue;
    end
    Ts = importdata(strcat(basePath,summaryFileName));
    data = loadData(T,Ts);
    
%     findBallLines(data, figure(1));
%     fitLines(data, figure(2));
    data = ball2Kicker(data);
    
%     plotBall2Kicker(data,figure(1),figure(2));
%     plotBallVel(data,figure(5));
% %     plotBallSteps(data,figure(6));
%     return
    
    if data.kickerBallDists(data.hitIdx) > 100
      numBallDist = numBallDist +1;
      if verbose
        fprintf(' Distance kicker->ball too high: %f', data.kickerBallDists(data.hitIdx));
%         warning('redirectBallDataPlot:dist','Distance kicker->ball too high: %f', data.kickerBallDists(data.hitIdx));
      end
      sortOut(basePath,sortedOutDir,dataFileName,summaryFileName);
      continue;
    end
    
    hitPoint = data.kicker(data.hitIdx,:);
    hit2Init = data.initBall - hitPoint;
    hit2Target = data.target(data.hitIdx,:) - hitPoint;
    hit2Last = data.lastBall - hitPoint;
    bot2Kicker = data.kicker(data.hitIdx,:)-data.bot(data.hitIdx,:);
    angleInitLast = abs(util.math.angleBetweenVectors(hit2Init,hit2Last));
    angleInitTarget = abs(util.math.angleBetweenVectors(hit2Init,hit2Target));
    ballVel = data.ballVelWPAbs(data.hitIdx);
    rotation = abs(util.math.angleBetweenVectors(bot2Kicker, hit2Last));
    
    if radtodeg(angleInitLast) > 120
      numAngle = numAngle + 1;
      if verbose
        fprintf(' Angle is too high: %.1f', radtodeg(angleInitLast));
      end
      sortOut(basePath,sortedOutDir,dataFileName,summaryFileName);
      continue;
    end
    
    angleDiff = abs(angleInitLast - angleInitTarget);
    if angleDiff > degtorad(40)
      numAngleDiff = numAngleDiff + 1;
      if verbose
        fprintf(' Angle difference too high: %.1f', angleDiff);
      end
      sortOut(basePath,sortedOutDir,dataFileName,summaryFileName);
      continue;
    end
    
%     fprintf('isAngle: %.1f\nshouldAngle: %.1f\nrotation: %.1f\nballVel: %.2f\n',radtodeg(angleInitLast),radtodeg(angleInitTarget), radtodeg(rotation), ballVel);
    numAccepted = numAccepted +1;
    result(end+1,:) = [rotation, angleInitLast, angleInitTarget, ballVel];
    fprintf(' ok');
  end
  fprintf('\nAccepted %d/%d data sets.\nRejected: data: %d, dist: %d, angle: %d, angleDiff: %d\n',numAccepted, size(files,1), numNed, numBallDist, numAngle, numAngleDiff);
  
  
end

function sortOut(baseDir, sortOutDir, dataFileName, summaryFileName)
  movefile(strcat(baseDir,dataFileName),strcat(baseDir,sortOutDir,dataFileName));
  movefile(strcat(baseDir,summaryFileName),strcat(baseDir,sortOutDir,summaryFileName));
end

function data = loadData(T, Ts)
    data.camId = T(1:end,1);
    data.timestamp = T(1:end-1,2);
    data.timeNs = T(1:end-1,3);
    data.ballx = T(1:end-1,4);
    data.bally = T(1:end-1,5);
    data.ball = [data.ballx,data.bally];
    data.botx = T(1:end-1,6);
    data.boty = T(1:end-1,7);
    data.botw = T(1:end-1,8);
    data.bot = [data.botx, data.boty];
    data.targetx = T(1:end-1,9);
    data.targety = T(1:end-1,10);
    data.target = [data.targetx,data.targety];
    data.kickerx = T(1:end-1,11);
    data.kickery = T(1:end-1,12);
    data.kicker = [data.kickerx,data.kickery];
    data.ballWPx = T(1:end-1,13);
    data.ballWPy = T(1:end-1,14);
    data.ballWP = [data.ballWPx,data.ballWPy];
    data.ballVelWPx = T(1:end-1,15);
    data.ballVelWPy = T(1:end-1,16);
    data.ballVelWP = [data.ballVelWPx,data.ballVelWPy];

    data.initBall = [Ts(1,1), Ts(1,2)];
    data.lastBall = [Ts(1,3), Ts(1,4)];
    data.duration = T(end,1);
    
    % ball vel
    data.ballVel = zeros(size(data.ball));
    data.ballVelAbs = zeros(size(data.ball,1),1);
    data.ballVelWPAbs = zeros(size(data.ball,1),1);
    for i=2:length(data.ballx)
      dt = (data.timeNs(i)-data.timeNs(i-1))/1e9;
      data.ballVel(i,:) = (data.ball(i,:) - data.ball(i-1,:))*dt;
      data.ballVelAbs(i) = norm(data.ballVel(i,:)');
      data.ballVelWPAbs(i) = norm(data.ballVelWP(i,:)');
    end
%     data.ballVelAbs = smooth(data.ballVelAbs);
    
%     all_idx = 1:length(data.ballVelAbs);
%     % Find outlier idx
%     outlier_idx = abs(data.ballVelAbs - median(data.ballVelAbs)) > 2*std(data.ballVelAbs);
%     data.ballVelAbs(outlier_idx) = interp1(all_idx(~outlier_idx), data.ballVelAbs(~outlier_idx), all_idx(outlier_idx))'; % Linearly interpolate over outlier idx for x
        
end

function findBallLines(data, fig)
  if exist('fig','var')
    figure(fig);
    clf;
    hold all;
  end
  [lineIn,foundIn] = util.findBallLine(data.ball,200);
  [lineOut,foundOut] = util.findBallLine(data.ball(end:-1:1,:),200);
  if ~foundIn || ~foundOut
    fprintf('ball lines not detected: %d %d\n', foundIn, foundOut);
  else
    intersec = intersectionPoint(lineIn, lineOut);
    if exist('fig','var')
      plot(data.ballx,data.bally,'.');
      plot(data.ballWPx,data.ballWPy,'+');
      plot(data.botx,data.boty,'.r');
      plot(data.kickerx,data.kickery,'*r');
      plot(data.targetx,data.targety,'.g');
      plot(intersec(1),intersec(2),'y');
    end
  end
end

function fitLines(data, fig)
  c = round(size(data.ball,1)/2);
  i=1;
  while true
    [line1,~] = polyfit(data.ball(1:c-1,1),data.ball(1:c-1,2),1);
    [line2,~] = polyfit(data.ball(c:end,1),data.ball(c:end,2),1);

    f1 = polyval(line1,data.ball(1:c-1,1));
    e1 = abs(data.ball(1:c-1,2) - f1);
    err1 = mean(e1);
    f2 = polyval(line2,data.ball(c:end,1));
    e2 = abs(data.ball(c:end,2) - f2);
    err2 = mean(e2);
    errDiff = err1-err2;
    if abs(errDiff) < .01
      break;
    end
    if i>=100
      break;
    end
    if err1 > err2
      c = c - 1;
    else
      c = c + 1;
    end
    i=i+1;
  end
  %   table = [ball(1:c-1,1), ball(1:c-1,2), f1, e1]
  %   table = [ball(c:end,1), ball(c:end,2), f2, e2]
  
  if exist('fig','var')
    figure(fig);
    clf;
    hold all;
    if data.initBall(1) > data.kickerx(1)
      x1 = round(data.kickerx(1)):5:round(data.initBall(1));
    else
      x1 = round(data.initBall(1)):5:round(data.kickerx(1));
    end
    if data.lastBall(1) > data.kickerx(1)
      x2 = round(data.kickerx(1)):5:round(data.lastBall(1));
    else
      x2 = round(data.lastBall(1)):5:round(data.kickerx(1));
    end
    plot(data.ballx,data.bally,'.');
    plot(x1,line1(1) * x1 + line1(2));
    plot(x2,line2(1) * x2 + line2(2));
    plot(data.ballx(c),data.bally(c),'+');
  end
end

function data = ball2Kicker(data)
  ball2Kicker = data.kicker - data.ball;
  data.kickerBallDists = sqrt(sum(ball2Kicker.^2,2));
  [~,hitIdxs] = sort(data.kickerBallDists,1);
  data.hitIdx = hitIdxs(1);
end

function plotBall2Kicker(data, fig1, fig2)
  if exist('fig1','var')
    figure(fig1);
    clf;
    hold all;
    plot(data.ballx,data.bally,'.k');
    plot(data.initBall(1),data.initBall(2),'+g');
    plot(data.lastBall(1),data.lastBall(2),'+b');
    plot(data.target(data.hitIdx,1),data.target(data.hitIdx,2),'+m');
    plot(data.kickerx,data.kickery,'.b');
    plot(data.botx,data.boty,'*b');
    plot(data.ball(data.hitIdx,1),data.ball(data.hitIdx,2),'+k');
    plot(data.kicker(data.hitIdx,1),data.kicker(data.hitIdx,2),'+r');
  end
  if exist('fig2','var')
    figure(fig2);
    clf;
    plot(data.kickerBallDists);
  end
end

function plotBallVel(data, fig)
  figure(fig);
  clf;
  hold all;
  plot(data.ballVelAbs);
  plot(data.ballVelWPAbs);
  title('Ball vel');
  legend('raw','WP');
end

function plotBallSteps(data, fig)
  % plot ball pos step by step
  figure(fig)
  clf;
  hold all;
  step=5;
  for i=1:step:size(data.ball,1)-step
    plot(data.ballx(i:i+step),data.bally(i:i+step),'.');
    pause;
  end
end