function plotField( fieldSize, scale )
%PLOTFIELDBORDERS Plots all the field borders
% fieldSize 0: small field, 1: grSim field
% scale: default field dimension is m, so to get mm, use 1000

  if ~exist('scale','var')
    scale = 1;
  end

  ballRadius = 0.0215 * scale;
  switch(fieldSize)
    case 0
     length = 6.05 * scale;
     width = 4.05 * scale;
     centerRadius = 0.5 * scale;
     penAreaFrontLineWidthHalf = 0.35 * scale;
     goalWidth = 0.7 * scale;
     goalDepth = 0.15 * scale;
     penAreaRadius = 0.8 * scale;
    case 1
     length = 8.09 * scale;
     width = 6.05 * scale;
     centerRadius = 1 * scale;
     penAreaFrontLineWidthHalf = 0.5 * scale;
     goalWidth = 1 * scale;
     goalDepth = 0.15 * scale;
     penAreaRadius = 1 * scale;
    otherwise
      error('Invalid fieldSize id: %d', fieldSize);
  end

  % borders
  hold on;
  w = width/2;
  l = length/2;
  coords = [0 -w; -l -w; -l w; l w; l -w; 0 -w; 0 w; 0 0; -l 0; l 0];
  plot(coords(:,1), coords(:,2),'k');
  
  % center circle
  plotCircle([0,0], centerRadius, 'k');
  
  % penalty areas
  penX = length/2 - penAreaRadius;
  plot([penX, penX], [penAreaFrontLineWidthHalf, -penAreaFrontLineWidthHalf], 'k');
  plot([-penX, -penX], [penAreaFrontLineWidthHalf, -penAreaFrontLineWidthHalf], 'k');
  plot( penAreaRadius * cos( linspace(0,pi/2,20) ) - l, penAreaRadius * sin( linspace(0,pi/2,20) ) + penAreaFrontLineWidthHalf, 'k');
  plot( penAreaRadius * cos( linspace(pi/2,pi,20) ) + l, penAreaRadius * sin( linspace(pi/2,pi,20) ) + penAreaFrontLineWidthHalf, 'k');
  plot( penAreaRadius * cos( linspace(-pi/2,0,20) ) - l, penAreaRadius * sin( linspace(-pi/2,0,20) ) - penAreaFrontLineWidthHalf, 'k');
  plot( penAreaRadius * cos( linspace(-pi,-pi/2,20) ) + l, penAreaRadius * sin( linspace(-pi,-pi/2,20) ) - penAreaFrontLineWidthHalf, 'k');

  % goals
  plot([l,l+goalDepth,l+goalDepth,l], [-goalWidth/2, -goalWidth/2, goalWidth/2, goalWidth/2],'k');
  plot([-l,-l-goalDepth,-l-goalDepth,-l], [-goalWidth/2, -goalWidth/2, goalWidth/2, goalWidth/2],'k');
  plot([l+goalDepth-ballRadius, l+goalDepth-ballRadius], [-goalWidth/2, goalWidth/2],'--k');
  plot([-l-goalDepth+ballRadius, -l-goalDepth+ballRadius], [-goalWidth/2, goalWidth/2],'--k');
  
  
  axis equal;
end

