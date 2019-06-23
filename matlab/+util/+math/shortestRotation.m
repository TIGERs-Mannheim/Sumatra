function [ rotateDist ] = shortestRotation( angle1,angle2 )
%SHOTESTROTATION Shortest rotation between both angle
  rotateDist = angle2 - angle1;
  if rotateDist < -pi
    rotateDist = 2*pi + rotateDist;
  end
	if rotateDist > pi
    rotateDist = rotateDist - 2*pi;
	end
end

