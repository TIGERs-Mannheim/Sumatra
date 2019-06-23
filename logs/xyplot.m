hold all;
grid on;
box on;

X = importdata('kick/kick_new26.csv'); %load csv
Y = X.data; 

plot(Y(:,2),Y(:,3))
