classdef MatrixModel < MotorModel
  %
   properties 
      D,D_inv
      wheelRadius = 0.025
   end 
   methods 
      function obj = MatrixModel()
        % specify "real" front and back motor angles in degree
        frontAngle = 30;
        backAngle = 45;
        botRadius = 0.076;

        % convert to radian
        frontAngle = frontAngle * pi / 180.0;
        backAngle = backAngle * pi / 180.0;

        % construct angle vector
        theta = [ frontAngle, pi - frontAngle, pi + backAngle, (2 * pi) - backAngle ]';

        % construct matrix for conversion from XYW to M1..M4
        obj.D = [-sin(theta), cos(theta), ones(4,1) * botRadius];
        obj.D_inv = pinv(obj.D);
      end
      
      % xywVel 3xN
      function [wheelSpeed, uncertainty] = getWheelSpeed(obj, xywVel)
          wheelSpeed = obj.D * xywVel / obj.wheelRadius;
          uncertainty = ones(size(wheelSpeed))*2;
      end
      
      % wheelSpeed 4xN
      function xywSpeed = getXywSpeed(obj, wheelSpeed)
        xywSpeed = obj.D_inv * wheelSpeed * obj.wheelRadius;
      end
   end
end

