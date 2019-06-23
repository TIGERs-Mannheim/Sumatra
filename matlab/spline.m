t0 = 0;
tf = 3;
s = 1;
X=t0:0.1:tf;

%% quintic spline
A = [ 1 t0 t0^2   t0^3   t0^4    t0^5; ...
      0 1  2*t0^1 3*t0^2 4*t0^3  5*t0^4; ...
      0 0  2      6*t0   12*t0^2 20*t0^3; ...
      1 tf tf^2   tf^3   tf^4    tf^5; ...
      0 1  2*tf^1 3*tf^2 4*tf^3  5*tf^4; ...
      0 0  2      6*tf   12*tf^2 20*tf^3];

q = [0 0 0 s 0 0]';
a = A\q;

f = @(x) a(1) + a(2).*x + a(3).*x.^2 + a(4).*x.^3 + a(5).*x.^4 + a(6).*x.^5;
fd = @(x) a(2) + 2*a(3).*x + 3*a(4).*x.^2 + 4*a(5).*x.^3 + 5*a(6).*x.^4;
fdd = @(x) 2*a(3) + 6*a(4)*x + 12*a(5)*x.^2 + 20*a(6)*x.^3;

YQ = f(X);
YQd = fd(X);
YQdd = fdd(X);

%% cubic spline
B = [ 1 t0 t0^2   t0^3; ...
      0 1  2*t0^1 3*t0^2; ...
      1 tf tf^2   tf^3; ...
      0 1  2*tf^1 3*tf^2];

q = [0 0 s 0]';
b = B\q;

f = @(x) b(1) + b(2).*x + b(3).*x.^2 + b(4).*x.^3;
fd = @(x) b(2) + 2*b(3).*x + 3*b(4).*x.^2;

YC = f(X);
YCd = fd(X);

%% plot
figure;
hold all;
plot(X,YQ);
plot(X,YQd);
plot(X,YQdd);
plot(X,YC);
plot(X,YCd);

fprintf('spline %.1f %.4f %.4f %.4f\n', tf, a(4), a(5), a(6));

% for t=0:0.1:tf
%     fprintf('%2.4f %2.4f\n', t, fd(t));
% end
 