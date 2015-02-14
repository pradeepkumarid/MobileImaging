function [xout,yout] = intersection(lineparams1,lineparams2)
%given parameters a,b,c of a two lines of the form ax+by=c
%computes intersection between them 
%solve for intersection using matrices 
intersectpoints=zeros(2,1);
A=[lineparams1(1) lineparams1(2);lineparams2(1) lineparams2(2)];
b=[lineparams1(3);lineparams2(3)];
intersectpoints=inv(A)*b;
xout=intersectpoints(1);
yout=intersectpoints(2);
end

