%preparing data for use with system identification toolkbox
%clear;
X = importdata('step17.csv'); %load csv

Y = X.data; 
%Z = [(Y(:,1)-Y(1,1))./1e9,Y(:,2:7)]; %scale timestamp to full seconds

t = Y(:,1);

setx = Y(:,2);
sety = Y(:,3);
setv = Y(:,4);

actx = Y(:,5);
acty = Y(:,6);
actv = Y(:,7);

set = [setx, sety]
act = [actx, acty]


