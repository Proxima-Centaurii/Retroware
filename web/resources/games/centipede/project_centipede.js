var scoreText = "Score: 0";
var hiscoreText = "0";
var newHighScore = false;

var cnv = document.getElementById("screen");
var gfx = cnv.getContext("2d");

var WIDTH = 640;
var HEIGHT = 420;
var BLOCK_SIZE = 20;

cnv.width = 640;
cnv.height = 420;

var lastTime = 0;
var TICK = 160;
var elapse = 0;
var pause = true;
var TOK = 0;

var CLEAR_COLOR = "#A8A8A8";
var currentColor = "#000000";

var pendingDirChanges = ['O','O'];
var playerData = {
	'score': 0,
	'hiscore': 0,
	'size': 3,
	'direction': 'E'
}
var bodyData = [140,300,120,300,100,300];
var appleData = [-20,-20];

var halt = 1; //The game starts paused

function mainLoop(nowTime){
	var deltaTime = nowTime - lastTime;
	
	if(deltaTime < TICK*2){
	
	render();
	update(deltaTime);
	
	}
	//else{
	//	deltaTime = 0;
	//}
	lastTime = nowTime;
	window.requestAnimationFrame(mainLoop);
}
function render(){
	CLEAR_COLOR = "#000000";
	clearScreen();
	
	if(halt == 0){
		for(var i=0; i < bodyData.length; i+=2){
			if(i == 0)
				setColor("#FF0000");
			else
				setColor("#00FF00");

			gfx.fillRect(bodyData[i],bodyData[i+1], BLOCK_SIZE, BLOCK_SIZE);
		}

		setColor("#BBFF00");
		gfx.fillRect(appleData[0],appleData[1], BLOCK_SIZE, BLOCK_SIZE);
	}

	if(halt == 1){
		setColor("#FFFFFF");
		gfx.font = "60px Arial";
		gfx.fillText("PAUSED", WIDTH/2-120, HEIGHT/2);

		gfx.font = "20px Arial";
		gfx.fillText("Press 'P' to resume", WIDTH/2-90, HEIGHT/2+30);
	}
	else if(halt != 0){
		setColor("#FF0000");
		gfx.font = "60px Arial";
		gfx.fillText("GAME OVER", WIDTH/2-180, HEIGHT/2);

		if(newHighScore){
			setColor("#00FF00");
			gfx.font = "60px Arial";
			gfx.fillText("NEW HIGH SCORE!!", 40, 50);

			gfx.font = "30px Arial";
			gfx.fillText("New score: " + hiscoreText, WIDTH/2-100, 80);
		}
	}


}

function update(delta){
	TOK = (TOK+delta)%3000;
	//Clock
	elapse += delta;
	if(elapse < TICK)
		return;
	elapse -= TICK;

	if(halt != 0)
		return;

	processInput();

	//move player 
	for(var i = bodyData.length-2; i >= 2; i-=2){
		bodyData[i] = bodyData[i-2]
		bodyData[i+1] = bodyData[i-1];
	}
	
	switch(playerData.direction){
		case 'N':
			bodyData[1] -= BLOCK_SIZE;
			break;
		case 'S':
			bodyData[1] += BLOCK_SIZE;
			break;
		case 'W':
			bodyData[0] -= BLOCK_SIZE;
			break;
		case 'E':
			bodyData[0] += BLOCK_SIZE;
			break;
	}
	
	//Check if the snake bit itself
	for(var i=6; i < bodyData.length; i+= 2)
		if(bodyData[0] == bodyData[i] && bodyData[1] == bodyData[i+1])
			gameOverState();
	//Check player is out of bounds or collected apple
	if(bodyData[0] < 0 || bodyData[0] >= WIDTH || bodyData[1] < 0 || bodyData[1] > HEIGHT)
		gameOverState();
	else if(bodyData[0] == appleData[0] && bodyData[1] == appleData[1])
		appleCollected();
}

function gameOverState(){
	halt = 2;
	if( playerData.score > playerData.hiscore){
		playerData.hiscore = playerData.score;
		hiscoreText = playerData.score.toString();
		newHighScore = true;
	}
}

function processInput(){
	var d = pendingDirChanges[0];
	
	if(d != 'O')
		playerData.direction = d;
	
	pendingDirChanges[0] = pendingDirChanges[1];
	pendingDirChanges[1] = 'O';
}

function appleCollected(){
	playerData.score++;
	randomApplePosition();

	bodyData[bodyData.length] = bodyData[bodyData.length-2]; //X
	bodyData[bodyData.length] = bodyData[bodyData.length-2]; //Y
	
	scoreText = "Score: " + playerData.score.toString();
}
function resetGame(){
	bodyData = [140,300,120,300,100,300];
	
	playerData.hiscore = playerData.score > playerData.hiscore ? playerData.score : playerData.hiscore;
	playerData.score = 0;
	playerData.direction = 'E';
	
	randomApplePosition();
	
	scoreText = "Score: " + playerData.score.toString();
	hiscoreText = playerData.hiscore.toString();

	halt = 0;
	newHighScore = false;
}
function randomApplePosition(){	
	var randXint = Math.floor(Math.random()*(WIDTH/BLOCK_SIZE));
	var randYint = Math.floor(Math.random()*(HEIGHT/BLOCK_SIZE));
	
	appleData[0] = randXint * BLOCK_SIZE;
	appleData[1] = randYint * BLOCK_SIZE;
}

function clearScreen(){
	gfx.fillStyle = CLEAR_COLOR;
	gfx.fillRect(0,0,WIDTH,HEIGHT);
	gfx.fillStyle = currentColor;
}

function setColor(newColor){
	currentColor = newColor;
	gfx.fillStyle = currentColor;
}

function initialize(){
	randomApplePosition();
	window.requestAnimationFrame(mainLoop);
}

document.addEventListener('keydown', function(event){
	var key = event.keyCode;
	
	if(halt == 2)
		resetGame();

	switch(key){
		case 38: //W
			if(pendingDirChanges[0] == 'O')
				pendingDirChanges[0] = 'N';
			else if(pendingDirChanges[0] != 'S')
				pendingDirChanges[1] = 'N';
			event.preventDefault();
			break;
			
		case 40: //S
			if(pendingDirChanges[0] == 'O')
				pendingDirChanges[0] = 'S';
			else if(pendingDirChanges[0] != 'N')
				pendingDirChanges[1] = 'S';
			event.preventDefault();
			break;
			
		case 37: //A
			if(pendingDirChanges[0] == 'O')
				pendingDirChanges[0] = 'W';
			else if(pendingDirChanges[0] != 'E')
				pendingDirChanges[1] = 'W';
			event.preventDefault()
			break;
			
		case 39: //D
			if(pendingDirChanges[0] == 'O')
				pendingDirChanges[0] = 'E';
			else if(pendingDirChanges[0] != 'W')
				pendingDirChanges[1] = 'E';
			event.preventDefault();
			break;
		case 80: //P
			halt = halt == 1 ? 0 : 1;
	}
});
window.requestAnimationFrame(initialize);
