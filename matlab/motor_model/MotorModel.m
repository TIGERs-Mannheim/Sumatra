classdef(Abstract) MotorModel < handle
  %MOTORMODEL abstract motor model
  
  properties
  end
  
  methods
    function [ws2d_sorted, ws2d_s2_sorted] = getUncertainInputs(obj)
        angleStep = 0.1;
        speedStep = 0.1;
        speedMax = 1.5;

        angleRange = -pi:angleStep:pi;
        speedRange = [0.05, 0.1, 0.15,0.2:speedStep:speedMax];

        [angles, speeds] = meshgrid(angleRange, speedRange);

        vel = zeros(length(speedRange), length(angleRange), 3);
        vel(:,:,1) = cos(angles) .* speeds;
        vel(:,:,2) = sin(angles) .* speeds;
        vel(:,:,3) = 0 .* speeds;

        vel2d = reshape(vel,[size(vel,1)*size(vel,2), 3]);
        [ws2d, ws2d_s2] = obj.getWheelSpeed(vel2d);
        
        [~, I] = sort(sum(ws2d_s2,2),1, 'descend' );
        ws2d_s2_sorted = ws2d_s2(I,:);
        ws2d_sorted = ws2d(I,:);
    end
    
    [ws2d, ws2d_s2] = getWheelSpeed(obj, vel2d)
  end
  
end

