function [kickDist, endVel, avgVel, initVel, distVelSamples] = kickDistApprox(varargin)
  verbose = false;
  goalX = 2.9;
%   goalX = 4.05;
  switch (nargin)
    case 0
      sampleFiles = [ 
                      '../logs/moduli_2015/ballKick/ballKick1429261373677.csv';
                      ];
%       directory = '../logs/moduli_sim/ballKick/';
%       directory = '../logs/moduli_2015/ballKick/';
%       dirlist = dir(strcat(directory,'*.csv'));
%       for i = 1:length(dirlist)
%         sampleFiles(i,:) = strcat(directory, dirlist(i).name);
%       end
      verbose = true;
    case 1
      sampleFiles = varargin{1};
    case 2
      sampleFiles = varargin{1};
      goalX = varargin{2};
  end
  
  if verbose
    close all;
    monitor=[2 1];
    colors = distinguishable_colors(size(sampleFiles, 1));
  end
  
  kickDist = zeros(size(sampleFiles,1),1);
  endVel = zeros(size(sampleFiles,1),1);
  avgVel = zeros(size(sampleFiles,1),1);
  initVel = zeros(size(sampleFiles,1),1);
  maxVel = zeros(size(sampleFiles,1),1);
  
  maxDistVelSamples = 500;
  distVelSamples = zeros(maxDistVelSamples*size(sampleFiles,1),2);
  numDistVelSamples = 0;

  for file=1:size(sampleFiles, 1)
    T = importdata(sampleFiles(file,:));

    cam =  T(1:end-1,1);
    % only use first cam
%     usedCam = mode(cam(1:5));
%     camIdxs = find(cam == usedCam);
%     cam =  T(camIdxs,1);
    camIdxs = 1:length(cam)-1;
    
    time = T(camIdxs,2)/1e9;
    time = time-time(1);
    p = polyfit((1:length(time))',time,1);
    time = p(1)*(1:length(time))'+p(2);
    posx = smooth(T(camIdxs,4)/1000);
    posy = smooth(T(camIdxs,5)/1000);
    
    wpVelDir = T(camIdxs,6:7);
    wpVel = T(camIdxs,8);
    
    duration = T(end,1);
    
    useWpVel = true;

    velx = zeros(size(posx));
    vely = zeros(size(posx));
    dist = zeros(size(posx));
    vel = zeros(size(posx));

    tIdMax = -1;
    for i=2:length(posx)
      % ball crossed goal line?
      if abs(posx(i)) > abs(goalX)
        [~,idx] = max(abs(posx(i:i+10,:)));
        tIdMax = i+idx-1;
        break;
      end
    end
    
    % goalLine not crossed?
    if tIdMax == -1
      % assume: ball stopped before crossing goalLine, so use last point
      tIdMax = length(posx);
      % but filter out data where ball is lying still
      for i=tIdMax-1:-1:1
        if abs(posx(i)-posx(i+1)) > 1e-4
          tIdMax = i;
          break;
        end
      end
    end
    
    % go backwards from tIdMax and find data point without change (ball
    % still)
    tIdMin = -1;
    for i=tIdMax-10:-1:1
      dx=abs(posx(i)-posx(i+1));
      if dx < 1e-4
        % use this, but go ahead some steps
        tIdMin=i+5;
        break;
      end
    end
    
    if tIdMin == -1
      warning('Warn:TooShot','No tIdMin found, distance probably too short');
      continue
    end
    
    if tIdMax-tIdMin < 5
      warning('Warn:TooLessData','Not enough useable data: %d',tIdMax-tIdMin);
      continue;
    end
    
    for i=tIdMin:tIdMax
       dt=time(i)-time(i-1);
       if dt <=0
         warning('Warn:Time','dt<=0 (%f)',dt);
         dt=0.01;
       end
       
       distx = posx(1) - posx(i);
       disty = posy(1) - posy(i);
       dist(i) = sqrt(distx*distx + disty*disty);
       if ~useWpVel
         velx(i) = (posx(i) - posx(i-1))/dt;
         vely(i) = (posy(i) - posy(i-1))/dt;
  %        vel(i) = abs(dist(i)-dist(i-1))/dt;
         vel(i) = sqrt(velx(i)^2+vely(i)^2);
         if vel(i)>10
           warning('Warn:velCutOff', 'vel cut off: %f', vel(i));
           vel(i)=10;
         end
       end
    end
    
    if useWpVel
      vel = wpVel;
      velx = wpVelDir(:,1);
      vely = wpVelDir(:,2);
    else
      vel = smooth(vel);
    end
    dist = smooth(dist);
    
    % how many samples to take (increase to have less samples)
    step = -1;
    interval = tIdMax:step:tIdMin;
    distVelSamples(numDistVelSamples+1:numDistVelSamples+length(interval), :) = ...
      [dist(interval) vel(interval)];
    numDistVelSamples = numDistVelSamples + length(interval);
    
    kickDist(file) = dist(tIdMax);
    endVel(file) = vel(tIdMax);
    avgVel(file) = kickDist(file)/(time(tIdMax)-time(tIdMin));
    maxVel(file) = max(vel);
    initVelIdx = min(length(dist),tIdMin+10);
    initVel(file) = (dist(initVelIdx)-dist(tIdMin))/(time(initVelIdx)-time(tIdMin));
    
    if verbose
      fprintf('%s duration %d dist %.3f endVel %.2f avgVel %.2f initVel %.2f maxVel %.2f\n', sampleFiles(file,:), duration, kickDist(file), endVel(file), avgVel(file), initVel(file), maxVel(file));
      
      doPlot = @(X,Y) plot(X,Y, ...
        [time(tIdMax) time(tIdMax)], ...
        [min(Y)-1 max(Y)+1],[time(tIdMin) time(tIdMin)],[min(Y)-1 max(Y)+1],'Color',colors(file,:));
      
      figure(1);
      hold all;
      plot([time(1),time(end)], [goalX, goalX], 'k');
      plot([time(1),time(end)], [-goalX, -goalX], 'k');
      doPlot(time,posx);
      title('pos x');

      figure(2);
      hold all;
      doPlot(time,posy);
      title('pos y');

      figure(3);
      hold all;
      plot(posx,posy);
      title('pos x->y');
      
      figure(4);
      hold all;
      doPlot(time,velx);
      title('vel x');

      figure(5);
      hold all;
      doPlot(time,vely);
      title('vel y');

      figure(6);
      hold all;
      doPlot(time,vel);
      title('vel');

      figure(7);
      hold all;
      doPlot(time,dist);
      title('dist');

      figure(8);
      hold all;
      plot(time);
      plot(-cam+0.2*file, '*');
      plot([tIdMax tIdMax],[min(time) max(time)]);
      title('time');
    end
  end
  
  distVelSamples = distVelSamples(1:numDistVelSamples,:);
  
  if verbose

    figure(9);
    hold all;
    plot(distVelSamples);
    title('dist vel samples');
    legend('dist','vel');
      
    placefigure('NW',[1/3 1/2],monitor(1),figure(1));
    placefigure('N',[1/3 1/2],monitor(1),figure(2));
    placefigure('NE',[1/3 1/2],monitor(1),figure(3));
    placefigure('SW',[1/3 1/2],monitor(1),figure(4));
    placefigure('S',[1/3 1/2],monitor(1),figure(5));
    placefigure('SE',[1/3 1/2],monitor(1),figure(6));
    
    placefigure('NW',[1/3 1/2],monitor(2),figure(7));
    placefigure('N',[1/3 1/2],monitor(2),figure(8));
    placefigure('NE',[1/3 1/2],monitor(2),figure(9));
  end
end