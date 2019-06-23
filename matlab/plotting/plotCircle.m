function plotCircle( radius, varargin )
%PLOTCIRCLE Plot a circle.
  angle = linspace(0,2*pi,30);
  circle_x  = radius*cos( angle );
  circle_y  = radius*sin( angle );
  plot(circle_x, circle_y, varargin{:});
  axis equal;
end

