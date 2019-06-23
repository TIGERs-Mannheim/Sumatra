clear all; clf;
hold all;
grid on;

X = importdata('move.csv'); %load csv

Y = X.data; 

dataSize = size(Y);
points = dataSize(1);
dims = dataSize(2);
distance = zeros(points, dims);
time = Y(:,1);
time = time-time(1);
time = time*1e-9;

for i = 2:points
    for j = 2:dims
        distance(i, j) = distance(i-1,j)+Y(i,j)*(time(i)-time(i-1));
    end
end

%plotting: first column is timestamp
%then plotting all other columns
for i = 2:points
%    plot(Z(:,1),Z(:,i)) 
    
end

sub(1) = subplot(2,1,1);
sub(2) = subplot(2,1,2);
hold(sub(1),'on');
hold(sub(2),'on');

plot(sub(1), time, Y(:,2), 'r');
plot(sub(1), time, Y(:,3));
plot(sub(1), time, distance(:,2),'g');
plot(sub(1), time, distance(:,3));
plot(sub(2), time, Y(:,4), 'r');
plot(sub(2), time, Y(:,5));
plot(sub(2), time, distance(:,4),'g');
plot(sub(2), time, distance(:,5));
%plot(Z(:,1), Z(:,8));


%and add a lengend
%legend(X.colheaders(2:dims-1))


