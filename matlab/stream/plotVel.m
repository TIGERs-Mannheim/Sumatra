function [ params ] = plotVel( params, t, setpoint, state )
%PLOTVEL Summary of this function goes here
%   Detailed explanation goes here

  delay = 0.2;  
  tsec = t / 1e9;
  if params.tOffset == 0
    params.tOffset = tsec;
  end
  tsec = tsec - params.tOffset;

  for i=1:3
    addpoints(params.h{i,1}, tsec, setpoint(i));
    addpoints(params.h{i,2}, tsec-delay, state(i));
  end
  drawnow
  
  
end

