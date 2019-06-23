hold all;
grid on;

X = importdata('move.csv'); %load csv

Y = X.data; 
Z = [(Y(:,1)-Y(1,1))./1e9,Y(:,2:5)]; %scale timestamp to full seconds

%plotting: first column is timestamp
%then plotting all other columns
s =size(Z);
s = s(2);
for i = 2:s
    plot(Z(:,1),Z(:,i)) 
    
end
%and add a lengend
legend(X.colheaders(2:i))


