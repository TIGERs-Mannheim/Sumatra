yData = [1500;3000;5000;6000];
xData = [300;2500;3500;4500];

% Set up fittype and options.
ft = fittype( 'poly1' );

% Fit model to data.
[fitresult, gof] = fit( xData, yData, ft );

p1=fitresult.p1;
p2=fitresult.p2;
x=0:100:4500;

f = @(x) (p1 * x + p2);

figure
hold all;
plot(xData,yData);
plot(x,f(x));

fitresult