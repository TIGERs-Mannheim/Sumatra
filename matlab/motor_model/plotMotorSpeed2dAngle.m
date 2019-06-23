function plotMotorSpeed2dAngle( motorModel, motorVel, outVel, setVel )
%
  angleRange = -pi:0.1:pi;
  
  % angle -> motors
  targetSpeeds = [0.1, 1.0, 2.0]';
  for i=1:size(targetSpeeds,1);
    vel = zeros(length(angleRange),3);
    for sr=1:length(angleRange)
      vel(sr,1:2) = util.math.vectorOfAngle(angleRange(sr)) * targetSpeeds(i);
    end
    
    [ws, ws_s2] = motorModel.getWheelSpeed(vel);
    
    targetSpeed = targetSpeeds(i);
    
%     if exist('outVel','var')
%       angles = util.math.angleOfVector(setVel(:,1:2));
%       validIdx = (abs(util.math.normalizeAngle( angles - targetAngle )) < 0.1) ...
%               |(abs(util.math.normalizeAngle( angles + pi - targetAngle )) < 0.1);
%       invIdx = (abs(util.math.normalizeAngle( angles(validIdx) + pi - targetAngle )) < 0.1);
%         weight = sum(abs(outVel(validIdx,3)),2);
%         x = util.math.vectorNorm(outVel(validIdx,1:2));
%         x(invIdx) = -x(invIdx);
%     end
    
    hFig = figure;
    for w=1:4
      subplot(2,2,w); title(sprintf('speed %.2f - wheel %d', targetSpeed, w));
      hold all;
      plot(angleRange, ws(:,w));
      plot(angleRange, ws(:,w)-2*ws_s2(:,w),'M:');
      plot(angleRange, ws(:,w)+2*ws_s2(:,w),'M:');

%       if exist('outVel','var')
%         scatter(x, motorVel(validIdx,w),50,weight,'.');
%       end
      
      xlabel('angle [rad]');
      ylabel('motor speed');
      ylim([-150,150]);
    end
    set(hFig, 'Position', [10 10 1600 800])
  end
end
