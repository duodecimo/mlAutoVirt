load('y.dat');
y = y(:, 2:end);
printf("size y= ");
size(y)
printf("size(y,2)= ");
size(y,2)
m = size(y,1);
m
for t=1:m
 printf("y(t)= ");
 y(t)
endfor


