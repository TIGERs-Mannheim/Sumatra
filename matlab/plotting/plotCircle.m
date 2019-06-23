function plotCircle( center, radius, varargin )
%PLOTCIRCLE Plot a circle.
  angle = linspace(0,2*pi,100);
  circle_x  = center(1) + radius*cos( angle );
  circle_y  = center(2) + radius*sin( angle );
  plot(circle_x, circle_y, varargin{:});
  axis equal;
end

