function [ point ] = intersectionPoint( line1, line2 )
%INTERSECTIONPOINT Intersection point of line1 with line2
%   line1,line2     1x4 [x,y,vx,vy]
%   point           1x2
  M = [ line1(3), -line2(3); line1(4), -line2(4)];
  b = [line2(1) - line1(1); line2(2) - line1(2)];
  
  if rank(M)==1
    warning('math:error', 'Given lines are parallel!');
    return;
  end

  X = M \ b;
  point = [X(1) * line1(3) + line1(1), X(1) * line1(4) + line1(2)];

end

