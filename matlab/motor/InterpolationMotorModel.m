classdef InterpolationMotorModel < MotorModel
  %INTERPOLATIONMOTORMODEL Summary of this class goes here
  %   Detailed explanation goes here
  
  properties
    data
  end
  
  methods(Static)
      function [imm] = fromMotorModel(mm, angleStep)
        angles = -pi:angleStep:(pi-angleStep);
%         speeds = [0, 0.1, 0.2, 0.3, 0.5, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0];
        speeds = 0:0.05:2.5;
        
        data = zeros(length(angles) * (length(speeds)), 6);
        xyw = zeros(length(angles) * (length(speeds)), 3);
        
        i = 1;
        for angle = angles
          for speed = speeds
            xyw(i,:) = [util.math.vectorOfAngle(angle) * speed, 0];
            data(i,1:2) = [angle, speed];
            i = i + 1;
          end
        end
        
        nonZero = data(:,2) > 0;
        zero = data(:,2) <= 0;
        data(nonZero,3:6) = mm.getWheelSpeed(xyw(nonZero,:));
        data(zero,3:6) = 0;
        
        imm = InterpolationMotorModel(data);      
      end
  end
  
  methods 
      function obj = InterpolationMotorModel(data)
        obj.data = data;
      end
      
      function save(obj, file)
        f = fopen(file, 'w');
        fprintf(f, '%f %f %f %f %f %f\n', obj.data');
        fclose(f);
      end
      
      % xywVel 3xN
      function [wheelSpeed, uncertainty] = getWheelSpeed(obj, xywVel)
          
      end
      
      % wheelSpeed 4xN
      function xywSpeed = getXywSpeed(obj, wheelSpeed)
        
      end
   end
  
end

