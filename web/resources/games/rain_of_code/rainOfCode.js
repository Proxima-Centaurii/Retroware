var playBtn = document.getElementById("playBtn");
var c = document.getElementById("screen");
var g = c.getContext("2d");

var WIDTH = 640	; //modified to reflect the canvas width
var HEIGHT = 420; //modified to reflect the canvas height
var VIRTUAL_HEIGHT = 420;
var VIRTUAL_WIDTH = 600;

//Engine variables
var lastTime = 0;
var FPS = 24.0;
var frameTime = 1000.0/FPS;
var frameElapse = 0;

//App variables
var charSizes = [3,5,15,25];
var charsSimple = "qQweAMrZtXyuiCoDpSWaEsdfgRFVBhGjklzGTxcYHNvbJUnmIKLOP";
var charsSigns = "qQweAMr!ZtXy@uiCoDp&SWaEsdf#g%R<=FV:BhG$jklzG*Tx+cYHNvbJUnmIKLOP";
var charsKorean = "ㅊㅋㅌㅍㅎㄷㅍㄲㄳㄵㄾㄿㅄ";
var charsBinary = "01";
var currentChars = charsSigns;
var charBlock = [];
var charBlockSize = 100;
var charStrip = {
x: -1,
y: -1,
start: -1,
size: -1,
len: -1,
tail: -1,
available: true
};
var stripArray = [];
var stripArrayLen = 140	;
var stripSize = 10;

var randomSpawnY = [];
var rowCooldown = [];
var xDivs = VIRTUAL_WIDTH/stripSize;

var paused = false;

var hue = 0;

function switchColour(col){
	hue = col;
	if(paused)
		draw();
	
}

function changeCharType(typeId){
	switch(typeId){
		case 0:
			currentChars = charsSigns;
			break;
		case 1:
			currentChars = charsSimple;
			break;
		case 2:
			currentChars = charsKorean;
			break;
		case 3:
			currentChars = charsBinary;
			break;
	}
	
	init();
}

function playStatus(){
	if(paused){
		playBtn.innerHTML = "Pause";
		paused = false;
	}
	else{
		playBtn.innerHTML = "Play";
		paused = true;
	}
}

function mainLoop(nowTime){
	var deltaTime = nowTime - lastTime;

	if(frameElapse < frameTime)
		frameElapse += deltaTime;
	else{
		if(!paused){
			update();
			draw();
		}
		frameElapse -= frameTime;
		
		//If the app fell behind significantly then reset
		if(frameElapse >= 5*frameTime)
			frameElapse = 0;
	}
	
	lastTime = nowTime;
	window.requestAnimationFrame(mainLoop);
}

function init(){
//Initialize canvas
c.width = WIDTH;
c.height = HEIGHT;

var sw = WIDTH/VIRTUAL_WIDTH;
var sh = HEIGHT/VIRTUAL_HEIGHT;
g.scale(sw,sh);

//Initialize visual objects
generateRandomTextBlock(currentChars, charBlockSize);

var i;
for(i=0; i<xDivs; i++){
	rowCooldown[i] = 0;
	randomSpawnY[i] = Math.floor(Math.random()*10)*stripSize;
}

stripArray = generateStripPool(stripSize,stripArrayLen);
stripArrayLen = stripArray.length;

window.requestAnimationFrame(mainLoop);
}
function update(){
	var i;
	for(i=stripArrayLen-1; i>=0; i--)
		if(!stripArray[i].available)
			updateStrip(stripArray[i]);
			
	spawnStrips();
}
function draw(){
	clearScreen("#000000");

	var i;
	for(i=stripArrayLen-1; i>=0; i--)
		if(!stripArray[i].available)
			drawStrip(stripArray[i]);
}

//TODO fix this function
function updateStrip(st){
	st.y += st.size;
	st.tail++;
	
	if(st.y > VIRTUAL_HEIGHT+st.size*st.len)
		st.available = true;
}

function drawStrip(st){
	//Draw the head
	g.fillStyle = "#FFBBBB";
	g.font = st.size+"px Arial";
	g.fillText(charBlock[(st.tail+st.len)%charBlockSize],st.x,st.y);
	//Draw the body
	var decay = 2.5* (22	/st.len);
	var i;
	for(i=st.len-2; i>0; i--){
		//g.fillStyle = "rgb("+Math.max(0, colR-i*decay)+","+Math.max(0, colG-i*decay)+","+Math.max(0, colB-i*decay)+")";
		//g.fillStyle = "rgb(0,"+Math.max(0,255-i*decay)+",0)";
		g.fillStyle = "hsl("+hue+",100%,"+Math.max(0,50-i*decay)+"%)";
		g.fillText(charBlock[(st.tail+st.len-i)%charBlockSize],st.x,st.y-i*st.size);
	}
}

function clearScreen(clearColor){
	g.fillStyle = clearColor;
	g.fillRect(0,0,VIRTUAL_WIDTH,VIRTUAL_HEIGHT);
}

function spawnStrips(){
	var i,j;
	for(i=0; i<xDivs; i++)
		if(rowCooldown[i] <= 0){
			for(j=stripArray.length-1; j>=0; j--)
				if(stripArray[j].available){
					stripArray[j].available = false;
					stripArray[j].x = i*stripSize;
					stripArray[j].y = -randomSpawnY[j%xDivs];
					
					rowCooldown[i] = Math.floor(randomSpawnY[j%xDivs]/stripSize) + stripArray[j].len + Math.floor(Math.random()*10+1);
					break;
				}
		}
		else{
			rowCooldown[i]--;
		}				
}

function generateStripPool(size, arraySize){
	var stripArray = [];
	
	var i;
	for(i=0; i<arraySize; i++){
		var str = Object.create(charStrip);
		
		str.size = size;
		str.tail = Math.floor(Math.random()*1000)%charBlockSize;
		str.len = (Math.floor(Math.random()*100)%20+ 15);
		
		stripArray[i] = str;
	}
	
	return stripArray;
}

function generateRandomStrip(size,xDiv){
	var str = Object.create(charStrip);
	str.size = size;
	str.y = str.start = -(Math.floor(Math.random()*100)%10)*str.size;
	str.x = str.size * (Math.floor(Math.random()*100)%xDiv); //Math.floor(VIRTUAL_WIDTH/str.size));
	str.tail = Math.floor(Math.random()*1000)%charBlockSize;
	str.len = (Math.floor(Math.random()*100)%15+5)%20;
	
	return str;
}

function generateRandomTextBlock(charSeed,blockSize){
	var seedLen = charSeed.length;
	
	var i;
	for(i=0; i<blockSize; i++){
		var x = Math.floor((Math.random()*1000)%seedLen);
		charBlock[i] = charSeed[x];
	}
}

window.requestAnimationFrame(init);