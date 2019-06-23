function chipRegression
  % chipDist
  chipDist=[0 2.4 2.8 2.9 3.3 1.7 2.6];
  % rollDist
  rollDist=[0 1 2 1.2 1.5 0.5 0.1];
  %duration
  duration=[0 5000 5000 7000 5000 3000 10000];
  % dribble speed
  dribbleSpeed=[0 5000 10000 10000 1000 5000 10000];

  doRegression(chipDist, rollDist, duration);
  zlabel('duration');
  doRegression(chipDist, rollDist, dribbleSpeed);
  zlabel('dribble speed');
end

function doRegression(X,Y,Z)
  [xData, yData, zData] = prepareSurfaceData( X,Y,Z );

  % Set up fittype and options.
  ft = fittype( 'poly22' );
  opts = fitoptions( ft );
  opts.Lower = [-Inf -Inf -Inf -Inf -Inf -Inf];
  opts.Upper = [Inf Inf Inf Inf Inf Inf];

  % Fit model to data.
  [fitresult, ~] = fit( [xData, yData], zData, ft, opts );
  theta = coeffvalues(fitresult)';

  fprintf('poly2:%f;%f;%f;%f;%f;%f\n',theta(1),theta(2),theta(3),theta(4),theta(5),theta(6));

  % Plot fit with data.
  figure;
  plot( fitresult, [xData, yData], zData );

  % Label axes
  xlabel( 'chipDist' );
  ylabel( 'rollDist' );
  zlabel( 'duration' );
  grid on
  view( -74.5, 14 );
end