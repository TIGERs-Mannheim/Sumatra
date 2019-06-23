function result = learnBallModel(varargin)
  folders = varargin;
  T = zeros(0,3);
  off=0;
  useWp = 1; % note: raw not useable atm...
  for f=1:length(folders)
    if useWp == 1
      wpBall = data.loadWpBall(strcat(folders{f}, '/wpBall.csv'));
      time = (wpBall.timestamp - wpBall.timestamp(1)) / 1e6;
      vel = sqrt( wpBall.vel(:,1) .* wpBall.vel(:,1) + wpBall.vel(:,2) .* wpBall.vel(:,2) ) * 1000;
      pos = wpBall.pos;
    else
      rawBall = data.loadRawBall(strcat(folders{f}, '/rawBall.csv'));
      time = (rawBall.timestamp - rawBall.timestamp(1)) / 1e6;
      tmp = rawBall.pos(2:end,:) - rawBall.pos(1:end-1,:);
      dists = sqrt(tmp(:,1).*tmp(:,1) + tmp(:,2).*tmp(:,2));
      dts = (time(2:end) - time(1:end-1)) / 1e3 + 1e-10;
      vel = [0; dists ./ dts];
      pos = rawBall.pos;
    end
    velDiff = abs(vel(2:end) - vel(1:end-1));
    cuts = find(velDiff > 400);
    
    for c=1:length(cuts)
      iStart = cuts(c) + 10;
      if c == length(cuts)
        iEnd = length(vel);
      else
        iEnd = cuts(c+1)-1;
      end
      if iEnd - iStart < 10
        continue;
      end
      cuttime = time(iStart:iEnd);
      cutvel = vel(iStart:iEnd);
      distVec = pos(iStart:iEnd,1:2) - ones(iEnd-iStart+1,1) * pos(iStart,1:2);
      dist = sqrt( distVec(:,1) .* distVec(:,1) + distVec(:,2) .* distVec(:,2) );
         
      for j=1:10:length(cuttime)
        l = length(cuttime(j:end));
        T(off+1:off+l,:) = [cutvel(j)*ones(l,1) cuttime(j:end)-cuttime(j) dist(j:end)-dist(j)];
        off = off + l;
      end
    end
  end
  
  X=T(:,1);
  Y=T(:,2);
  Z=T(:,3);
      
  [xData, yData, zData] = prepareSurfaceData( X, Y, Z );
  ft = fittype( 'poly22' );
  opts = fitoptions( 'Method', 'LinearLeastSquares' );
  opts.Robust = 'Bisquare';
  [fitresult, ~] = fit( [xData, yData], zData, ft, opts );
   
  result = coeffvalues(fitresult);
   
  figure( 'Name', 'BallMovementApproximation' );
  plot( fitresult, [xData, yData], zData );
  title 'BallMovementApproximation'
  xlabel 'Velocity [mm / s]'
  ylabel 'Time [ms]'
  zlabel 'Traveled Distance [mm]'
  grid on

  %figure(1)
  %clf;
  %hold on;

  %plot(time(1:end,1),dist(1:end,1),'r');
  %plot(time(1:end,1),speed(1:end,1),'g');
  %plot(time(1:end,1),estimatedDist(1:end,1),'b');

  %xlabel('Time in [ns]');
  %ylabel('Distance [IVector2.length2]');
  %set(gca,'xgrid','on','ygrid','on');
  %set(gcf,'PaperPositionMode','auto');
  %set(gcf,'InvertHardcopy','off');
  %set(gcf,'PaperUnits','inches','PaperSize',[6,2],'PaperPosition',[0 0 6 3]);
  %legend('TraveledDistance', 'Location', 'NorthEast','Ball vel * 1000', 'Location', 'NorthEast');

    
    
    
