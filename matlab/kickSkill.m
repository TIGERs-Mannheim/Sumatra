function [] = kickSkill(varargin) 
    D = importdata(varargin{1});
    time = D(:,1);
    dist = D(:,2);
    speed = D(:,3);
    ballSpeed = D(:,4);

    figure(1);
    clf;
    hold on;

    plot(time(1:end,1),dist(1:end,1),'r');
    plot(time(1:end,1),speed(1:end,1),'g');
    plot(time(1:end,1),ballSpeed(1:end,1),'b');
        
    xlabel('Time in [ns]');
    ylabel('Distance [IVector2.length2]');
    set(gca,'xgrid','on','ygrid','on');
    set(gcf,'PaperPositionMode','auto');
    set(gcf,'InvertHardcopy','off');
    set(gcf,'PaperUnits','inches','PaperSize',[6,2],'PaperPosition',[0 0 6 3]);

    legend('DistanceToBall', 'Location', 'NorthEast','Bot speed * 1000', 'Location', 'NorthEast','Ball speed * 1000', 'Location', 'NorthEast');

    print('-dpng','-r300',strcat('./kickdata/',varargin{2}));
end