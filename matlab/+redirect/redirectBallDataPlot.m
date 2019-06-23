function fitresult = redirectBallDataPlot()
  verbose = false;
%   result = redirect.redirectLoad(verbose);
%   save('result.mat','result');
  load('result.mat');
  
  
  figure( 'Name', 'Redirect fit' );
  hold all;

  % fit
  % ballvel
  X=result(:,4);
  % angle
  Y=result(:,2);
  % rotation
  Z=result(:,1);
  [xData, yData, zData] = prepareSurfaceData( X, Y, Z );

  % Set up fittype and options.
  ft = fittype( 'poly22' );
  opts = fitoptions( ft );
  opts.Lower = [-Inf -Inf -Inf];
  opts.Upper = [Inf Inf Inf];

  % Fit model to data.
  [fitresult, gof] = fit( [xData, yData], zData, ft, opts );
  
  ballSpeeds = (0:0.5:3)';
  angles = (0:0.1:degtorad(150))';
  Z = zeros(size(angles,1), size(ballSpeeds,1));
  for x=1:size(ballSpeeds,1)
    for y=1:size(angles,1)
      Z(y,x) = redirect.redirectPhysical(ballSpeeds(x),angles(y));
    end
  end
  
  [X, Y] = meshgrid(ballSpeeds, angles);
  surf(X,Y,Z);

  % Plot fit with data.
  h = plot( fitresult, [xData, yData], zData );
  legend( h, 'Redirect fit', 'Z vs. X, Y', 'Location', 'NorthEast' );
  % Label axes
  xlabel('ballVel')
  ylabel('angle')
  zlabel('rotation')
  grid on
end