function [chipDist, rollDist] = chipDistApprox(varargin)
  verbose = false;
  switch (nargin)
    case 0
      sampleFiles = [ 
%                       '../logs/ballChip/labWithLength/ballChip1399046901549.csv';
%                       '../logs/ballChip/labWithLength/ballChip1399047037047.csv';
%                       '../logs/ballChip/labWithLength/ballChip1399047116050.csv';
%                       '../logs/ballChip/labWithLength/ballChip1399047208556.csv';
%                       '../logs/ballChip/labWithLength/ballChip1399047285892.csv';
%                       '../logs/ballChip/labWithLength/ballChip1399047455548.csv';
%                       '../logs/moduli_default/ballChip/ballChip1399570134123.csv';
                      '../logs/moduli_default/ballChip/ballChip1399570362648.csv';
                      ];

%       dists=[2.4;2.8;2.9;3.3;1.7;2.6];
      dists=zeros(size(sampleFiles,1),1);
      verbose = true;
    case 1
      sampleFiles = varargin{1};
      dists=zeros(size(sampleFiles,1),1);
    case 2
      sampleFiles = varargin{1};
      dists=varargin{2};
  end
  
  if verbose
    close all;
    monitor=1;
    colors = ['b','g','r','m','c','y','k'];
  end

  chipDist = zeros(size(sampleFiles,1),1);
  rollDist = zeros(size(sampleFiles,1),1);

  for file=1:size(sampleFiles, 1)
    disp(sampleFiles(file,:));
    T = importdata(sampleFiles(file,:));

    cam =  T(1:end-1,1);
    time = T(1:end-1,2)/1e3;
    time = time-time(1);
    posx = smooth(T(1:end-1,3)/1000);
    posy = smooth(T(1:end-1,4)/1000);


    velx = zeros(size(posx));
    vely = zeros(size(posy));
    dist = zeros(size(posx));
    vel = zeros(size(posx));
    dist2Line = zeros(size(posx));

    start_xy = mean([posx(1:5),posy(1:5)]);
    end_xy = mean([posx(end-5:end),posy(end-5:end)]);

    tIs = -1;
    for i=2:length(posx)
       dt=time(i)-time(i-1);
       velx(i) = (posx(i) - posx(i-1))*dt;
       vely(i) = (posy(i) - posy(i-1))*dt;
       vel(i) = sqrt(velx(i)*velx(i)+vely(i)*vely(i));
       distx = posx(1) - posx(i);
       disty = posy(1) - posy(i);
       dist(i) = sqrt(distx*distx + disty*disty);

       if tIs == -1 && dist(i) >= dists(file)
         tIs = time(i);
       end

       p = [posx(i);posy(i)];
       lp = leadpoint(p, start_xy, end_xy);
       dist2Line(i) = distancePP(lp, p);
    end

    dist2Line = smooth(dist2Line);
    [peaks,peaksTimeId] = findpeaks(dist2Line, 'MINPEAKHEIGHT', 0.03, 'MINPEAKDISTANCE', 10);
    if length(peaksTimeId)>1
      [valley, tIdValley] = min(dist2Line(peaksTimeId(1):peaksTimeId(2)));
      tIdValley = tIdValley + peaksTimeId(1) - 1;
    else
      valley = 0;
      tIdValley = 1;
      warning('Warn:Invalid','Not enough peaks');
    end
    
    chipDist(file) = dist(tIdValley);
    rollDist(file) = dist(end) - chipDist(file);

    if verbose
      diff = dists(file) - dist(tIdValley);
      fprintf('%s: isDist=%f calcDist=%f diff=%f\n', sampleFiles(file, :), dists(file), dist(tIdValley), diff);
      
      figure(1);
      hold all;
      plot(time, velx);
      title('x');

      figure(2);
      hold all;
      plot(time, vely);
      title('y');

      figure(3);
      hold all;
      plot(posx,posy);
      title('pos x->y');

      figure(4);
      hold all;
      plot(time, posx);
      title('pos x');

      figure(5);
      hold all;
      plot(time, posy);
      title('pos y');

      figure(6);
      hold on;
      plot(time, dist, colors(file));
      plot([time(1),time(end)],[dists(file),dists(file)], colors(file));
      title('dist');

      figure(7);
      hold all;
      plot(time, vel);
      title('vel');

      figure(8);
      hold on;
      plot(time, dist2Line, colors(file));
      plot([tIs,tIs],[0,max(dist2Line)], colors(file));
      plot(time(peaksTimeId), peaks, strcat('*',colors(file)));
      plot(time(tIdValley), valley, strcat('+',colors(file)));
      title('dist2Line');

      figure(9);
      hold all;
      plot(time);
      plot(cam+0.1*file, '*');
      title('time');
    end
  end
  
  if verbose
    placefigure('NW',[1/3 1/2],monitor,figure(1));
    legend(sampleFiles);
    placefigure('N',[1/3 1/2],monitor,figure(2));
    placefigure('NE',[1/3 1/2],monitor,figure(3));
    placefigure('SW',[1/3 1/2],monitor,figure(4));
    placefigure('S',[1/3 1/2],monitor,figure(5));
    placefigure('SE',[1/3 1/2],monitor,figure(6));
    placefigure('N',[1/3 1/2],monitor,figure(7));
    placefigure('C',[1 1],4,figure(8));
    placefigure('S',[1/3 1/2],monitor,figure(9));
  end
end