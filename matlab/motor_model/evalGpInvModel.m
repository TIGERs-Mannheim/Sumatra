function evalGpInvModel( gm, motorVel, outVel, setVel )
%

  xywOutVel = gm.getXywSpeed(motorVel);

  error = xywOutVel - outVel;
  sqError = error.^2;
  mse = mean(sqError,1);
  rmse = sqrt(mse);

  fprintf('         RMSE: %f %f %f\n',rmse);

%   figure
%   plot(error);

  outVelFilteredIdx = abs(outVel(:,3)) < 1;
  outVelFiltered = outVel(outVelFilteredIdx,:);
  motorVelFiltered = motorVel(outVelFilteredIdx,:);
  
  xywOutVel = gm.getXywSpeed(motorVelFiltered);

  error = xywOutVel - outVelFiltered;
  sqError = error.^2;
  mse = mean(sqError,1);
  rmse = sqrt(mse);

  fprintf('filtered RMSE: %f %f %f\n',rmse);

  figure
  plot(error);
end

