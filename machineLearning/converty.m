function r = converty(yv, ny)
%CONVERTY returns a ny X 1 matriz with all lines = 0, but the yvTh line =1

r = zeros(ny, 1);
% duo 18 Out 2016
% avoid setting a zero matrix index
if(yv > 0)
r(yv) = 1;
endif
end

