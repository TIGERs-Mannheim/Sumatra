function [theta, XD, YD, ZD] = kickLearn(varargin)
  verbose = false;
  goalX = 2.9;
%   goalX = 3.9;
  switch (nargin)
    case 0
%       sampleFiles = [ 
%                       '../logs/ballKick/labGoalShots/ballKick1399051320100.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051328900.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051374958.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051413253.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051432560.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051448558.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051468561.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051484560.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051515062.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051547567.csv';
%                       '../logs/ballKick/labGoalShots/ballKick1399051591060.csv';
% 
%                       '../logs/moduli_default/ballKick/KickTrainerData/ballKick1399914788216.csv';
%                       '../logs/moduli_default/ballKick/KickTrainerData/ballKick1399914796597.csv';
%                       '../logs/moduli_default/ballKick/KickTrainerData/ballKick1399914801994.csv';
%                       '../logs/moduli_default/ballKick/KickTrainerData/ballKick1399914811001.csv';
%                       ];
                    
%       directory = '../logs/moduli_sim/ballKick/';
      directory = '../logs/moduli_default/ballKick/';
      dirlist = dir(strcat(directory,'*.csv'));
      for i = 1:length(dirlist)
        sampleFiles(i,:) = strcat(directory, dirlist(i).name);
      end
      verbose = true;
    case 1
      sampleFiles = varargin{1};
    case 2
      sampleFiles = varargin{1};
      goalX = varargin{2};
  end
  
  numSamples = size(sampleFiles, 1);
  maxSamplesPerFile = 500;
  X = zeros(numSamples*maxSamplesPerFile,2);
  Y = zeros(numSamples*maxSamplesPerFile,1);
  nextIndex = 1;
  for i = 1:numSamples
    fileName = sampleFiles(i,:);
    if verbose
      fprintf('%s\n',fileName);
    end
    D = importdata(fileName);
    [kickDist, endVel, ~, ~, data] = kickDistApprox(fileName, goalX);
    
    if kickDist <=0.1
      continue;
    end
    
    dataSize = size(data,1);
    X(nextIndex:nextIndex+dataSize-1,1:2) = data;
    duration = D(end,1);    
    Y(nextIndex:nextIndex+dataSize-1) = ones(dataSize,1)*duration;
    if verbose
      fprintf('    dist %f endVel %f duration %f\n',kickDist, endVel, duration);
    end
    
    nextIndex = nextIndex + dataSize;
  end
  X = X(1:nextIndex-1,:);
  Y = Y(1:nextIndex-1,:);

  XD=X(:,1);
  YD=X(:,2);
  ZD=Y(:);

  [xData, yData, zData] = prepareSurfaceData( XD, YD, ZD );

  % Set up fittype and options.
  ft = fittype( 'poly22' );
  opts = fitoptions( ft );
  opts.Lower = [-Inf -Inf -Inf -Inf -Inf -Inf];
  opts.Upper = [Inf Inf Inf Inf Inf Inf];

  % Fit model to data.
  [fitresult, ~] = fit( [xData, yData], zData, ft, opts );
  theta = coeffvalues(fitresult)';
  
  if verbose
    fprintf('\npoly2:%f;%f;%f;%f;%f;%f\n',theta(1),theta(2),theta(3),theta(4),theta(5),theta(6));
    
    % Plot fit with data.
    figure( 'Name', 'Straight kick fit' );
    h = plot( fitresult, [xData, yData], zData );
    legend( h, 'fittet curve', 'duration vs. dist, endVel', 'Location', 'NorthEast' );
    % Label axes
    xlabel( 'dist' );
    ylabel( 'endVel' );
    zlabel( 'duration' );
    grid on
    view( -74.5, 14 );

    figure('Name', 'durations');
    hist(Y(:));
  end
end

