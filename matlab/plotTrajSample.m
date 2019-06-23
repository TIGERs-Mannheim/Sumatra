figure(1)
clf

filebase = cell(1,0);
% filebase{end+1} = {'1448984692755',''};
% filebase{end+1} = {'1448984703166',''};
% filebase{end+1} = {'1448984708355',''};

% filebase{end+1} = {'1448984725735',''};
% filebase{end+1} = {'1448984731010',''};

filebase{end+1} = {'1448984747057',''};
filebase{end+1} = {'1448984751824',''};
filebase{end+1} = {'1448984756576',''};
filebase{end+1} = {'1448984761390',''};

for i=1:size(filebase,2)
  T=importdata(sprintf('../data/trajSample/%s.csv', filebase{i}{1}));
  

t=T(:,1);
setVelLocal=T(:,3:5);
setVel=T(:,6:8);
outVel=T(:,9:11);
setPos=T(:,12:14);
outPos=T(:,15:17);


subplot(2,2,1);title('global xy vel')
hold all
plot(t,setVel(:,1:2))
plot(t,outVel(:,1:2))

subplot(2,2,2);title('global w vel')
hold all
plot(t,setVel(:,3))
plot(t,outVel(:,3))

subplot(2,2,3);title('global xy pos')
hold all
plot(t,setPos(:,1:2))
plot(t,outPos(:,1:2))

subplot(2,2,4);title('global orientation')
hold all
plot(t,setPos(:,3))
plot(t,outPos(:,3))

end