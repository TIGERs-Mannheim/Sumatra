function plotCovariance(covariance, X0, Y0)

  if size(covariance,1) ~= size(covariance,2)
    dim = sqrt(size(covariance,1) * size(covariance,2));
    covariance = reshape(covariance, [dim, dim]);
  end

  [eigenvec, eigenval ] = eig(covariance);

  % Get the index of the largest eigenvector
  [largest_eigenvec_ind_c, ~] = find(eigenval == max(max(eigenval)));
  largest_eigenvec = eigenvec(:, largest_eigenvec_ind_c);

  % Get the largest eigenvalue
  largest_eigenval = max(max(eigenval));

  % Get the smallest eigenvector and eigenvalue
  if(largest_eigenvec_ind_c == 1)
      smallest_eigenval = max(eigenval(:,2));
%       smallest_eigenvec = eigenvec(:,2);
  else
      smallest_eigenval = max(eigenval(:,1));
%       smallest_eigenvec = eigenvec(1,:);
  end

  % Calculate the angle between the x-axis and the largest eigenvector
  angle = atan2(largest_eigenvec(2), largest_eigenvec(1));

  % This angle is between -pi and pi.
  % Let's shift it such that the angle is between 0 and 2pi
  if(angle < 0)
      angle = angle + 2*pi;
  end

  % Get the coordinates of the data mean
  % avg = mean(data);

  % Get the 95% confidence interval error ellipse
  chisquare_val = 2.4477;
  theta_grid = linspace(0,2*pi,30);
  phi = angle;
  % X0=avg(1);
  % Y0=avg(2);
%   X0 = 0; Y0 = 0;
  a=chisquare_val*sqrt(largest_eigenval);
  b=chisquare_val*sqrt(smallest_eigenval);

  % the ellipse in x and y coordinates 
  ellipse_x_r  = a*cos( theta_grid );
  ellipse_y_r  = b*sin( theta_grid );

  %Define a rotation matrix
  R = [ cos(phi) sin(phi); -sin(phi) cos(phi) ];

  %let's rotate the ellipse to some angle phi
  r_ellipse = [ellipse_x_r;ellipse_y_r]' * R;

  % Draw the error ellipse
  plot(r_ellipse(:,1) + X0,r_ellipse(:,2) + Y0,'-')
%   hold on;
%   axis equal;

  % Plot the original data
  % plot(data(:,1), data(:,2), '.');
  % mindata = min(min(data));
  % maxdata = max(max(data));
  % xlim([mindata-3, maxdata+3]);
  % ylim([mindata-3, maxdata+3]);
  % hold on;

  % Plot the eigenvectors
  % quiver(X0, Y0, largest_eigenvec(1)*sqrt(largest_eigenval), largest_eigenvec(2)*sqrt(largest_eigenval), '-m', 'LineWidth',2);
  % quiver(X0, Y0, smallest_eigenvec(1)*sqrt(smallest_eigenval), smallest_eigenvec(2)*sqrt(smallest_eigenval), '-g', 'LineWidth',2);

  % Set the axis labels
%   hXLabel = xlabel('x');
%   hYLabel = ylabel('y');
end


function example()
%   close all;
  % Create some random data
%   s = [2 2];
%   x = randn(334,1);
%   y1 = normrnd(s(1).*x,1);
%   y2 = normrnd(s(2).*x,1);
%   data = [y1 y2];
  % covariance = cov(data);
  
  dt = 0.016;
  aMin = 10;
  velMax = 500;
  aMax = 4000;
  
%   v = [0,0; 0,1; 1,0; 1,1; 0.2,1; -2,1];
  v = [1.1,-0.7];
  
  for i=1:size(v,1)
    vx = v(i,1);
    vy = v(i,2);
    vel = norm([vx,vy])*1000;
    if vx ~= 0
      theta = atan(vy/vx);
    else
      if vy ~= 0
        theta = acot(0);
      else
        theta = 0;
      end
    end
    scale_x = max(aMax, vel);
    scale_y = aMin+(max(0,1-vel/velMax)*(aMax-aMin));
    R = [cos(theta) -sin(theta);
          sin(theta) cos(theta)];
    S = [ scale_x , 0      ;
             0    , scale_y]*dt*dt;
    T = R*S;
    T = [cos(theta)*scale_x,-sin(theta)*scale_y; sin(theta)*scale_x, cos(theta)*scale_y]*dt*dt; 
    covariance = T*T';
    showCovariance(covariance, 0, 0);
    quiver(0, 0, vx, vy);
  end
end

