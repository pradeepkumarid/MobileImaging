%----------------------------------------------------------------------------------------------------------
%2D Floor plan generation 
%Paper: Capturing indoor scenes with Smartphones-Aditya Sankar and Steve
%Seitz
%Assumes Camera as the origin of the scene and takes as input the angles of
%the rays joining the camera and corners of the room.
%Assumptions: Manhattan world
%The User is able to see all corners
%Corner ray directions are determined from the spatially indexed data 
%This code assumes that you know the angles of the rays emanating from the
%camera
%All angles are measured with respect to the positive x direction 
%-----------------------------------------------------------------------------------------------------------
angleset=input('Enter the angles measured with positive x direction in degrees');%angles of the rays joining camera and corners
angleset=sort(angleset);%sort angle values
angleset=[angleset angleset(1)];%append the first angle as the final one
N=length(angleset);%number of corners corresponding to each ray 
%-------------------------------------------
%Right now not checking for different alphas 
%start with some alpha for now and try to find intersection points
%-------------------------------------------
%Pick a ray-preferably the one with the least angle 
%Try finding the point at unit distance from the origin on the first ray 
x1=cosd(angleset(1));
y1=sind(angleset(1));
%compute the point of intersections of the lines with the rays to find
%corners with a starting angle alpha 
%---alpha=45;
mindistance=100000;
for alpha=0:0.5:360
%parameters of line 1 generated from the point x1,y1 with angle alpha
%respect to first ray
lineparams=[-sind(angleset(1)-alpha) cosd(angleset(1)-alpha) y1*cosd(angleset(1)-alpha)-x1*sind(angleset(1)-alpha)];
%compute intersection function
line1params=lineparams;%initialising line1params
%initialise x and y coordinates of corners with zeros
v1=zeros(N,1);
v2=zeros(N,1);
%initialise entries
v1(1)=x1;
v2(1)=y1;
    for i=2:N
        line2params=[-sind(angleset(i)) cosd(angleset(i)) 0];
        [xinter,yinter]=intersection(line1params,line2params);
        %slope=-line1params(1)/line1params(2);d
        %yaxisinter=yinter+(1/slope)*xinter;
        line1params=[line1params(2) -line1params(1) -yinter*line1params(1)+xinter*line1params(2)];
        v1(i)=xinter;
        v2(i)=yinter;   
    end
    %distance between the final point and the initial point 
    a=[v1 v2];
    distance=norm(a(1,:)-a(end,:));%distance between final point and initial point on the same ray
    if mindistance>distance
        mindistance=distance;
        minangle=alpha;
        finalpointset=a;
    end 
    
end    


%To check output
%twodfloorplan
%[45 135 225 315]
%pdepoly(finalpointset(1:4,1)',finalpointset(1:4,2))








