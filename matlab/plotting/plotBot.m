function plotBot( pos, varargin )
%plotBot Plot a bot shape into a x->y plot
% Coordinates should be in mm, orienation in rad.
% pos must be a 3-dim vector (row or col) with [x,y,w]

  if size(pos,1) ~= 1
    pos = pos';
  end
  if size(pos,1) ~= 1 || size(pos,2) ~= 3
    error('Wrong dimensions.');
  end

  r = 90;
  center2Dribbler = 75;
  ballr = 21.5;
  
  points = genPoints(pos,r,center2Dribbler);
  plot(points(:,1),points(:,2), varargin{:});
  
  pointsBallBorder = genPoints(pos, r + ballr, center2Dribbler+ballr);
  plot(pointsBallBorder(:,1),pointsBallBorder(:,2), '--', varargin{:});
  
  axis equal
end

function points = genPoints(pos, r, center2Dribbler)
  h = r - center2Dribbler;
  theta = acos((h - r) / -r);

  res = 16;
  step = ((2*pi)-2*theta)/(res-1);
  points = zeros(res+1, 2);
  for i=1:res
    angle = pos(3) + theta + (i-1)*step;
    points(i,:) = pos(1:2) + [cos(angle), sin(angle)]*r;
  end
  points(end,:) = points(1,:);
end
