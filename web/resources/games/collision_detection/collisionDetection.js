var logLevel = 3;
var screen = document.getElementById("screen");
var gfx = screen.getContext('2d');
var clearColor, currentColor;
var sWidth = 640;
var sHeight = 420;

var OBJECT_TYPES = {
	RECTANGLE: 0,
	CIRCLE: 1,
	LINE: 2
}

var LOG_LEVELS = {
	NO_LOG: 0,
	NORMAL: 1,
	DEBUG: 2,
	NORMAL_AND_DEBUG: 3
}

var MOUSE = {
	flag: 0,
	x: 0,
	y: 0
}

//World objects
var intersectionPoint;
var handles = [];
var line1, line2;

var selectedHandle = -1;
//--------------

//Event listeners
window.addEventListener("resize",function(){resize(0,0);clearScreen()});
screen.addEventListener("mousemove",function(event){
	//Get the coordinate relative to the inner window (the canvas)
	var x = screen.getBoundingClientRect();
	
	MOUSE.x = event.clientX - x.left;
	MOUSE.y = event.clientY - x.top;
	
	if(!MOUSE.flag)return; //Is the mouse button down?
	
	if(selectedHandle != -1){
		//Update lines
		handles[selectedHandle].pos.x = MOUSE.x;
		handles[selectedHandle].pos.y = MOUSE.y;
		
		//Calculate the new intersection
		intersectionPoint.pos = lineIntersection(line1,line2);
	}

	drawUpdate();
	
});
screen.addEventListener("mousedown",function(event){
	MOUSE.flag = 1;
	
	//Check if a handle is being dragged
	for(i = 0; i<4; i++)
		if(pointCircleCollision(MOUSE.x,MOUSE.y, handles[i])){
			selectedHandle = i;
			setColor(handles[selectedHandle],"#FF0000");
			break;
		}

		
		drawUpdate();
});
screen.addEventListener("mouseup",function(event){
	MOUSE.flag = 0;
	if(selectedHandle != -1){
		setColor(handles[selectedHandle],"#00FF00");
		selectedHandle = -1;
	}
	
	drawUpdate();
});

//Graphics and window
function initialize(){
	/*INITIALIZING THE SYSTEM*/
	clearColor = "#000000";
	
	resize(0,0);
	clearScreen();
	
	/*INITIALIZING THE WORLD*/
	//Intersection point
	intersectionPoint = createObject(OBJECT_TYPES.CIRCLE);
	setColor(intersectionPoint,"#0000FF");
	
	//Lines
	line1 = createObject(OBJECT_TYPES.LINE);
	line1.A = {x: sWidth/2, y: 50};
	line1.B = {x: sWidth/2, y: 250};
	
	line2 = createObject(OBJECT_TYPES.LINE);
	line2.A = {x: 50, y: sHeight/2};
	line2.B = {x: 250, y: sHeight/2};
	
	//Lines handles
	for(i = 0; i<4; i++){
		handles[i] = createObject(OBJECT_TYPES.CIRCLE);
		setColor(handles[i], "#00FF00");
	}
	
	handles[0].pos = line1.A;
	handles[1].pos = line1.B;
	handles[2].pos = line2.A;
	handles[3].pos = line2.B;
	
	//Initial draw
	drawObject(line1);
	drawObject(line2);
	if(intersectionPoint.pos != null)
		drawObject(intersectionPoint);
	
	for(i = 0; i<4; i++)
		drawObject(handles[i]);
}

function drawUpdate(){
	//Update and draw the world
	clearScreen();
	
	drawObject(line1);
	drawObject(line2);
	if(intersectionPoint.pos != null && intersectionPoint != null)
		drawObject(intersectionPoint);
	
	for(i = 0; i<4; i++)
		drawObject(handles[i]);
}

function resize(diffX, diffY){
	
	screen.width = sWidth;
	screen.height = sHeight;
}

function clearScreen(){
	gfx.fillStyle = clearColor;
	gfx.fillRect(0,0,sWidth,sHeight);
}
function setClearColor(color){clearColor = color;}
function setColor(obj,newColor){obj.color = newColor;}
function setPosition(obj,newX,newY){
	obj.pos.x = newX;
	obj.pos.y = newY;
}

function drawObject(ob){
	
	switch(ob.type){
		case OBJECT_TYPES.RECTANGLE:
			gfx.fillStyle = ob.color;
			gfx.fillRect(ob.pos.x,ob.pos.y,ob.size.w,ob.size.h);
			break;
		case OBJECT_TYPES.CIRCLE:
			gfx.beginPath();
			gfx.fillStyle = ob.color;
			gfx.arc(ob.pos.x,ob.pos.y, ob.radius,0, 2*Math.PI);
			gfx.fill();
			gfx.closePath();
			break;
		case OBJECT_TYPES.LINE:
			gfx.beginPath();
			gfx.strokeStyle = ob.color;
			gfx.lineWidth = ob.size;
			gfx.moveTo(ob.A.x,ob.A.y);
			gfx.lineTo(ob.B.x,ob.B.y)
			gfx.stroke();
			gfx.closePath();
			break;
	}
}
	
//Objects
function createObject(obType){
	var obj;
	
	switch(obType){
		case OBJECT_TYPES.RECTANGLE:
			obj = {type:OBJECT_TYPES.RECTANGLE,color: "#FFFFFF", pos: {x:0,y:0}, size:{w:20,h:20}};
			break;
		case OBJECT_TYPES.CIRCLE:
			obj = {type:OBJECT_TYPES.CIRCLE,color: "#FFFFFF" ,pos: {x:0,y:0}, radius: 10};
			break;
		case OBJECT_TYPES.LINE:
			obj = {type:OBJECT_TYPES.LINE,color: "#FFFFFF" ,A: {x:0,y:0}, B:{x:10,y:10}, size: 2};
			break;
	}
	
	return obj;
}

//Math
function inRange(x,a,b, endsIncluded){
	if(endsIncluded)
		return (x >= 0 && x <=b);
	else
		return (x > a && x < b);
}

//Collision detection
function lineIntersection(l1,l2){
	var intersection;
	var A1 = l1.B.y - l1.A.y,
		B1 = l1.A.x - l1.B.x,
		C1 = l1.A.x*l1.B.y - l1.B.x*l1.A.y,
		A2 = l2.B.y - l2.A.y,
		B2 = l2.A.x - l2.B.x,
		C2 = l2.A.x*l2.B.y - l2.B.x*l2.A.y,
		denominator = A1*B2 - A2*B1;
		
		if(denominator == 0){return null;}
		else intersection = {
			x: (C1*B2 - C2*B1)/denominator,
			y: (C2*A1 - C1*A2)/denominator
		}
		
		var rx1 = (intersection.x - l1.A.x)/(l1.B.x-l1.A.x),
			ry1 = (intersection.y - l1.A.y)/(l1.B.y-l1.A.y),
			rx2 = (intersection.x - l2.A.x)/(l2.B.x-l2.A.x),
			ry2 = (intersection.y - l2.A.y)/(l2.B.y-l2.A.y);
			
		if( (inRange(rx1,0,1) || inRange(ry1,0,1)) && (inRange(rx2,0,1) || inRange(ry2,0,1)) )
			return intersection;
		else
			return null;
}
function pointCircleCollision(px,py,circle){
	return Math.sqrt( Math.pow(circle.pos.x-px,2) + Math.pow(circle.pos.y-py,2)) <= circle.radius;
}

initialize();
