D=importdata('/tmp/shapeMap0.csv');

sums=sum(D.data);
labels=D.colheaders';

sumsWithId=[1:length(sums);sums];
sumsSorted=sortrows([1:length(sums);sums]',2, 'descend');

% sumsFiltered=sumsSorted(sumsSorted(:,2)>0,:);
sumsFiltered=sumsSorted(1:15,:);

figure
pie(sumsFiltered(:,2));
legend(labels{sumsFiltered(:,1)});