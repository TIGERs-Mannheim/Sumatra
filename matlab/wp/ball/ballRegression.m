function [chipDist rollDist] = ballRegression()
% function [chipDist rollDist] = ballRegression(data, setVerbose)
  data = importdata('/home/geforce/workspace/Sumatra/logs/ballChip/labWithLength/ballChip1399047037047.csv');
%   data = importdata('/home/geforce/workspace/Sumatra/logs/ballChip/ballChip1396866947826.csv');
  setVerbose=1;
  % 
  % ballData1396631468074 -> mehrere parabeln
  % ballData1396631454394 -> difficult to fit, depends on error threshold
  
  %                       chip max
  % ballData1396629528174 2800 3000 
  % 
  
  if length(data) < 50
    fprintf('not enough input data: %d\n',length(data));
    return;
  end

  camId = data(1:end-1,1);
  timestamp = data(1:end-1,2);
  posx = data(1:end-1,3);
  posy = data(1:end-1,4);

  dt=1/220;
  global polyDegree;
  global maxError;
  global verbose;
  polyDegree = 2;
  maxError = 0.1;
  verbose = setVerbose;
  maxIterations = 1000;
  
  velx = zeros(size(posx));
  vely = zeros(size(posy));
  velOrig = zeros(size(posx));
  for i=2:length(posx)
%     dt = (timestamp(i)-timestamp(i-1))/1000;
    velx(i) = (posx(i) - posx(i-1))*dt;
    vely(i) = (posy(i) - posy(i-1))*dt;
    velOrig(i) = sqrt(velx(i)^2 + vely(i)^2);
  end

  velOrig = filterOutliers(velOrig);
  vel = filter([1/4 1/4 1/4 1/4],1,velOrig);
  
  time = zeros(size(vel));
  for i=1:length(vel)
    time(i) = i;
  end
    
  if verbose
    clf;
    figure(1);
    hold all;
    plot(velOrig,'.');
    plot(vel,'LineWidth',2);

    for i=1:length(camId)
      switch(camId(i))
        case 0
          c = '.b';
          y = -0.02;
        case 1
          c = '.g';
          y = -0.03;
        case 2
          c = '.r';
          y = -0.04;
        case 3
          c = '.k';
          y = -0.05;
      end
      plot(i,y,c);
    end
  end
  tRaise = 1;
  tTouch = length(vel);
  timeMarkers=[tRaise tTouch];
  for a=1:maxIterations
    [vel_fitNew timeMarkersNew, err] = fitDataPoly(time(tRaise:tTouch),vel(tRaise:tTouch));
    
    if err==1
      break;
    end
    
    if tRaise == timeMarkersNew(1) && tTouch == timeMarkersNew(2)
      fprintf('Converged after %d iterations.\n', a);
      break;
    end
    
    if verbose
      vel_fit = vel_fitNew;
      plot(time(tRaise:tTouch), vel_fit);
      plot(timeMarkers,zeros(size(timeMarkers)),'*');
    end
    
    tRaise = timeMarkersNew(1);
    tTouch = timeMarkersNew(2);
    timeMarkers = timeMarkersNew;
  end
  
  posRaise = [posx(tRaise) posy(tRaise)];
  posTouch = [posx(tTouch) posy(tTouch)];
  posStop = [posx(end) posy(end)];
  chipDistVec = posTouch-posRaise;
  rollDistVec = posStop-posTouch;
  fullDistVec = posStop-posRaise;
  chipDist = norm(chipDistVec);
  rollDist = norm(rollDistVec);
  fullDist = norm(fullDistVec);

  if fullDist < chipDist
    rollDist = -rollDist;
  end
  
  dists=zeros(size(posx));
  for i=1:length(posx)
    pos = [posx(i) posy(i)];
    dists(i) = norm(pos-posRaise);
  end
  maxDist = max(dists);

  if verbose
    fprintf('chipDist=%f\n',chipDist);
    fprintf('rollDist=%f\n',rollDist);
    fprintf('fullDist=%f\n',fullDist);
    fprintf('maxDist=%f\n',maxDist);
  end
end

function [vel_fit timeMarkers err] = fitDataPoly(time, vel)
  global polyDegree;
  global maxError;
  err = 0;
  tRaise = time(1);
  tTouch = time(end);
  
  [p,~] = polyfit(time,vel,polyDegree);
  vel_fit = polyval(p,time);

  r = roots(p);
  if length(r) == 2 && isequal(r,real(r)) && isequal(r>0,ones(size(r))) && isequal(r<=time(end),ones(size(r)))
    tRaise = max(time(1), round(min(r)));
    tTouch = min(time(end),max(time(1), round(max(r))));
  end
  
  error = abs(max(vel)-max(vel_fit));
  if tRaise == time(1) && tTouch == time(end) && error > maxError
    [~, maxVelI] = max(vel);
    if maxVelI > length(vel)/2
      tRaise = time(2);
      tTouch = time(end);
    else
      tRaise = time(1);
      tTouch = time(end-1);
    end
  end

  timeMarkers = [tRaise,tTouch];
  for i=1:length(timeMarkers)
    if timeMarkers(i) <= 0 || timeMarkers(i) > time(end)
      disp('invalid timeMarkers.');
      disp(timeMarkers);
      err=1;
      return;
    end
  end
end

function filteredData = filterOutliers(data)
  filteredData = data;
  % Calculate the mean and the standard deviation
  % of each data column in the matrix
  mu = mean(data);
  sigma = std(data);
  [n,~] = size(data);
  % Create a matrix of mean values by
  % replicating the mu vector for n rows
  MeanMat = repmat(mu,n,1);
  % Create a matrix of standard deviation values by
  % replicating the sigma vector for n rows
  SigmaMat = repmat(sigma,n,1);
  % Create a matrix of zeros and ones, where ones indicate
  % the location of outliers
  outliers = abs(data - MeanMat) > 3*SigmaMat;
  % Calculate the number of outliers in each column
%   nout = sum(outliers) 
  filteredData(any(outliers,2),:) = [];
end