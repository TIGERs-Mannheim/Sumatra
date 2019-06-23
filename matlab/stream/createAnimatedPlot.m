function params = createAnimatedPlot()  
     
  figure
  params.h = cell(3,2);
  params.tOffset = 0;
  for i=1:3
    subplot(3,1,i);
    params.h{i,1} = animatedline('Color','b','MaximumNumPoints',1000);
    params.h{i,2} = animatedline('Color','r','MaximumNumPoints',1000);
    legend('setpoint','state','Location','northwest');
  end


end