classdef GpModel < MotorModel
  %GPMODEL Gaussian Process motor model
 
  
  properties
    p,pInv;
%     meanfunc, covfunc, likfunc;
%     hyp=cell(4,1);
%     x,y;
%     post=cell(4,1);
  end
  
  methods
      function obj = GpModel(xywVel, wheelSpeed)
        mm = MatrixModel();
        
        obj.p.hyp=cell(4,1);
        obj.p.post=cell(4,1);
        
        xDir = mm.getWheelSpeed([1,0,0]');
        yDir = mm.getWheelSpeed([0,1,0]');
        wDir = mm.getWheelSpeed([0,0,1]');
        for i=1:4
          obj.p.hyp{i}.mean = [xDir(i); yDir(i); wDir(i)];
        end
        obj.p.meanfunc = {@meanLinear}; 
%         obj.p.meanfunc = {@meanZero};
        
        obj.p.covfunc = {@covMaterniso, 3}; ell = 1/1; sf = 2;
        obj.p.hyp{1}.cov = log([ell; sf]);
        obj.p.hyp{2}.cov = log([ell; sf]);
        obj.p.hyp{3}.cov = log([ell; sf]);
        obj.p.hyp{4}.cov = log([ell; sf]);
        
        obj.p.likfunc = @likGauss; sn = 0.1;
        obj.p.hyp{1}.lik = log(sn);
        obj.p.hyp{2}.lik = log(sn);
        obj.p.hyp{3}.lik = log(sn);
        obj.p.hyp{4}.lik = log(sn);
        
        obj.p.x = xywVel;
        obj.p.y = wheelSpeed;
        
        
        obj.pInv.hyp=cell(3,1);
        obj.pInv.post=cell(3,1);
        
        wsMean = zeros(3,4);
        wsMean(:,1) = mm.getXywSpeed([1,0,0,0]');
        wsMean(:,2) = mm.getXywSpeed([0,1,0,0]');
        wsMean(:,3) = mm.getXywSpeed([0,0,1,0]');
        wsMean(:,4) = mm.getXywSpeed([0,0,0,1]');
        
        for i=1:3
          obj.pInv.hyp{i}.mean = wsMean(i,:)';
        end
        obj.pInv.meanfunc = {@meanLinear}; 
        
        obj.pInv.covfunc = {@covMaterniso, 3}; ell = 1/4; sf = 5;
        obj.pInv.hyp{1}.cov = log([ell; sf]);
        obj.pInv.hyp{2}.cov = log([ell; sf]);
        obj.pInv.hyp{3}.cov = log([ell; sf]);
        obj.pInv.hyp{4}.cov = log([ell; sf]);
        
        obj.pInv.likfunc = @likGauss; sn = 0.1;
        obj.pInv.hyp{1}.lik = log(sn);
        obj.pInv.hyp{2}.lik = log(sn);
        obj.pInv.hyp{3}.lik = log(sn);
        obj.pInv.hyp{4}.lik = log(sn);
        
        obj.pInv.x = wheelSpeed;
        obj.pInv.y = xywVel;
        
      end
      
      function train(obj)
        hyp = cell(4,1);
        for w=1:4
          hyp{w} = minimize(obj.p.hyp{w}, @gp, -100, @infExact, obj.p.meanfunc, obj.p.covfunc, obj.p.likfunc, obj.p.x, obj.p.y(:,w));
          [~, ~, obj.p.post{w}] = gp(hyp{w}, @infExact, obj.p.meanfunc, obj.p.covfunc, obj.p.likfunc, obj.p.x, obj.p.y(:,w));
        end
        for w=1:4
          fprintf('hyp %d:\n', w);
          fprintf('Mean: ');
          fprintf('%f ', obj.p.hyp{w}.mean);
          fprintf(' -> ');
          fprintf('%f ', hyp{w}.mean);
          fprintf('\n');
          fprintf('Cov: ');
          fprintf('%f ', obj.p.hyp{w}.cov);
          fprintf(' -> ');
          fprintf('%f ', hyp{w}.cov);
          fprintf('\n');
          fprintf('lik: ');
          fprintf('%f ', obj.p.hyp{w}.lik);
          fprintf(' -> ');
          fprintf('%f ', hyp{w}.lik);
          fprintf('\n');
          obj.p.hyp{w} = hyp{w};
        end
%         for w=1:3
%           obj.pInv.hyp{w} = minimize(obj.pInv.hyp{w}, @gp, -100, @infExact, obj.pInv.meanfunc, obj.pInv.covfunc, obj.pInv.likfunc, obj.pInv.x, obj.pInv.y(:,w));
%           [~, ~, obj.pInv.post{w}] = gp(obj.pInv.hyp{w}, @infExact, obj.pInv.meanfunc, obj.pInv.covfunc, obj.pInv.likfunc, obj.pInv.x, obj.pInv.y(:,w));
%         end
      end
    
      function [wheelSpeed, uncertainty] = getWheelSpeed(obj, xywVel)
          assert(size(xywVel,2)==3);
          wheelSpeed = zeros(size(xywVel,1),4);
          uncertainty = zeros(size(xywVel,1),4);
          for w=1:4
            [mu, s2] = gp(obj.p.hyp{w}, @infKL, obj.p.meanfunc, obj.p.covfunc, obj.p.likfunc, obj.p.x, obj.p.post{w}, xywVel);
            wheelSpeed(:,w) = mu;
            uncertainty(:,w) = s2;
          end          
      end
      
      function [xywSpeed, uncertainty] = getXywSpeed(obj, wheelSpeed)
          assert(size(wheelSpeed,2)==4);
          xywSpeed = zeros(size(wheelSpeed,1), 3);
          uncertainty = zeros(size(wheelSpeed,1), 3);
          for w=1:3
            [mu, s2] = gp(obj.pInv.hyp{w}, @infKL, obj.pInv.meanfunc, obj.pInv.covfunc, obj.pInv.likfunc, obj.pInv.x, obj.pInv.post{w}, wheelSpeed);
            xywSpeed(:,w) = mu;
            uncertainty(:,w) = s2;
          end  
      end
  end
end

