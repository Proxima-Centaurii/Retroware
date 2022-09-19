var cnv = document.getElementById("screen");
var gfx = cnv.getContext("2d");

var V_SIZE = 500;
var WIDTH = 640;
var HEIGHT = 420;


cnv.width = WIDTH;
cnv.height = HEIGHT;
gfx.scale(WIDTH/V_SIZE, HEIGHT/V_SIZE);

//Game variables
var sumPlr = 0;

//AI
var AI_RESET_CYCLE = 15;
var sumAi = 0;
var stopAi = false;

var good_avg = 1.0;
var good_avg_fact = 1;

var bad_avg = 1.0;
var bad_avg_fact = 1;

var averageCard = drawCard();
var cardsDrawn = 1;
//------------

var endGameText = ["You won!", "You lost!", "It's a draw..."];
var endGameTextColor = ["#00CC00", "#FF0000", "#EEFF00"];
var outcome = 2;
var gameEnded = false;
var gamesPlayed = 0;

var scoreColorCode = ["#A9A9A9","#009900", "#CCCC00", "#CC6600", "#0000CC","#CC0000"];

function render(){
	//Clear screen
	gfx.fillStyle = "#000000";
	gfx.fillRect(0,0,V_SIZE,V_SIZE);
	
	//player score text
	gfx.fillStyle = "#A9A9A9";
	if(sumPlr <= 5)
		gfx.fillStyle = scoreColorCode[0];
	else if(sumPlr <= 10)
		gfx.fillStyle = scoreColorCode[1];
	else if(sumPlr <= 15)
		gfx.fillStyle = scoreColorCode[2];
	else if(sumPlr <= 20)
		gfx.fillStyle = scoreColorCode[3];
	else if(sumPlr == 21)
		gfx.fillStyle = scoreColorCode[4];
	else
		gfx.fillStyle = scoreColorCode[5];

	gfx.font = "60px Consolas";
	gfx.fillText(""+sumPlr, V_SIZE/2-(gfx.measureText(String(sumPlr)).width)/2, V_SIZE/2);
	
	gfx.font = "50px Impact"
	//Draw card button
	gfx.fillStyle = "#00FF00";
	gfx.fillRect(0,V_SIZE-70,V_SIZE/2,70);
	
	gfx.fillStyle = "#006600";
	gfx.fillText("DRAW", V_SIZE/4-(gfx.measureText("DRAW").width)/2, V_SIZE-15);
	
	//Cancel button
	gfx.fillStyle = "#CC0000";
	gfx.fillRect(V_SIZE/2,V_SIZE-70,V_SIZE/2,70);
	
	gfx.fillStyle = "#660000";
	gfx.fillText("STOP", 3*V_SIZE/4-(gfx.measureText("STOP").width)/2, V_SIZE-15);
	
	if(!gameEnded)
		return;
	//Announce text
	gfx.fillStyle = endGameTextColor[outcome];
	gfx.font = "80px Impact";
	gfx.fillText(endGameText[outcome], V_SIZE/2-(gfx.measureText(endGameText[outcome]).width)/2, V_SIZE/8 + 10);
	
	gfx.fillStyle = "#0066cc";
	gfx.font = "30px Consolas";
	gfx.fillText("Bot's score: "+ sumAi, V_SIZE/2-(gfx.measureText("Bot's score: "+sumAi).width)/2, V_SIZE/8 + 50);
	
	//console.log(WIDTH+" "+HEIGHT+"\n");
}

function drawCard(){return Math.floor(Math.random()*100) % 13;}
function draw(){
	if(gameEnded){
		gameEnded = false;
		sumPlr = sumAi = 0;
		
		if(++gamesPlayed % AI_RESET_CYCLE == 0)
			resetAi();
	}
	
	sumPlr += drawCard();
	
	aiTurn();
	
	render();
}
function stop(){
	if(gameEnded)
		return;
	gameEnded = true;
	
	while(!stopAi)
		aiTurn();
	
	if(sumPlr > 21 && sumAi > 21){
		//Win,loss,draw
		if(sumPlr < sumAi){outcome=0;}
		else if(sumPlr > sumAi){outcome=1;}
		else{outcome = 2;}
	}
	else if(sumPlr <= 21 && sumAi <= 21){
		//Win,loss, draw
		if(sumPlr > sumAi){outcome=0;}
		else if(sumPlr < sumAi){outcome=1;}
		else{outcome=2;}
	}
	else{
		if(sumPlr > 21){outcome=1;}
		else if(sumAi > 21){outcome=0;}
		else{outcome=2;}
	}
	
	aiUpdate();
	render();
}
function resetAi(){
	console.log("Resetting A.I.\n");
	
	good_avg = 1.0;
	good_avg_fact = 1;

	bad_avg = 1.0;
	bad_avg_fact = 1;
}
function aiTurn(){
	if(aiThink()){
		var drawnCard = drawCard();
		sumAi += drawnCard;
		averageCard = computeAverage(averageCard,++cardsDrawn,drawnCard);
	}
	else
		stopAi = true;
}
function aiThink(){
	if(sumAi >= 21){
		return false;
	}
	
	var curPos = sumAi/21;
	
	var curBadDist = Math.abs(bad_avg - curPos);
	var curGoodDist = Math.abs(good_avg - curPos);

	var nextBadDist = Math.abs(bad_avg - (sumAi+averageCard)/21);
	var nextGoodDist = Math.abs(good_avg -(sumAi+averageCard)/21);
	
	//inner segment
	if((curPos >= good_avg && curPos <= bad_avg) || (curPos >= bad_avg && curPos <= good_avg))
		if(curBadDist/nextBadDist > curGoodDist/nextGoodDist)
			return false;
	
	//Outter segment
	
	return true;
}
function aiUpdate(){
	switch(outcome){
		case 0: //AI LOSES
			bad_avg = computeAverage(bad_avg, ++bad_avg_fact, sumAi/21);
			good_avg = computeAverage(good_avg, ++good_avg_fact, sumPlr/21)
			break;
		case 1: //AI WINS
			good_avg = computeAverage(good_avg, ++good_avg_fact, sumAi/21);
			bad_avg = computeAverage(bad_avg, ++bad_avg_fact, sumPlr/21);
			break;
		case 2:	//DRAW
			good_avg = computeAverage(good_avg, ++good_avg_fact, sumAi/21);
			bad_avg = computeAverage(bad_avg, ++bad_avg_fact, sumAi/21);
			break;
	}
	
	
	console.log("Updated good: "+good_avg+" ("+Math.floor(21*good_avg)+")\n");
	console.log("Updated bad: "+bad_avg+" ("+Math.floor(21*bad_avg)+")\n");	
}
function computeAverage(avg,newSize,newFactor){
	return (avg + (newFactor-avg)/newSize);
}

/*Since this used to be full screen the input listeners below had to be rewritten*/

document.addEventListener("mouseup",function(e){
	var rect = cnv.getBoundingClientRect();
	var windowX = e.clientX - rect.left;
	var windowY = e.clientY - rect.top;
	
	if(windowX >= WIDTH || windowY >= HEIGHT)
		return;
	//Draw button
	if(windowX < WIDTH/2 && windowY >= 360)
		draw()
	else if(windowX >= WIDTH/2 && windowY >= 360)
		stop();

 console.log("X: "+e.clientX+" ("+windowX+")\nY: "+e.clientY+" ("+windowY+")\n");
	
	render();
});

document.addEventListener("touchend",function(e){
	var touch = e.changedTouches[0];
	
	var rect = cnv.getBoundingClientRect();
	var windowX = touch.clientX - rect.left;
	var windowY = touch.clientY - rect.top;
	
	if(windowX >= WIDTH || windowY >= HEIGHT)
		return;
	//Draw button
	if(windowX < WIDTH/2 && windowY >= 360)
		draw()
	else if(windowX >= WIDTH/2 && windowY >= 360)
		stop();
	
	e.preventDefault();
	
	render();
},false);

render();