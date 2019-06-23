function evalGpModel( gm, motorVel, outVel, setVel )
%
  motorOutVel = gm.getWheelSpeed(outVel);

  error = motorOutVel - motorVel;
  sqError = error.^2;
  mse = mean(sqError,1);
  rmse = sqrt(mse);

  fprintf('         RMSE: %f %f %f %f\n',rmse);

%   figure
%   plot(error);

  outVelFilteredIdx = abs(outVel(:,3)) < 1;
  outVelFiltered = outVel(outVelFilteredIdx,:);
  motorVelFiltered = motorVel(outVelFilteredIdx,:);
  
  motorOutVel = gm.getWheelSpeed(outVelFiltered);

  error = motorOutVel - motorVelFiltered;
  sqError = error.^2;
  mse = mean(sqError,1);
  rmse = sqrt(mse);

  fprintf('filtered RMSE: %f %f %f %f\n',rmse);

  figure
  plot(error);
end

