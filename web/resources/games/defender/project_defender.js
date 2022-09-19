var c = document.getElementById("screen");
var g = c.getContext("2d");

var livesText = "";
var scoreText = "";
var highscoreText = "0";
var precisionText = "";
var newHighScore = false;

var WIDTH = 640;
var HEIGHT = 420;
c.width = 640;
c.height = 420;

//Engine variables
var FPS = 60;
var frameTime = 1000/FPS;
var frameElapse = 0;
var lastTime = 0;
var frameCount = 0;

var inputKeysDown = {
	left: false,
	right: false,
	space: false
}

var halt = 1; //The game starts paused

//App variables
var rect = {
x: 0,
y:0,
w: 0,
h: 0
}

var PLAYER_SPEED = 12;
var MAX_BULLETS = 5;
var MIN_BULLETS = 2;
var BULLET_SPEED = 20;
var bulletCap = MIN_BULLETS;
var bulletsActive = [];
var bullets = [];
var bulletsShot = 0;

var MAX_ENEMIES = 10;
var MIN_ENEMIES = 3;
var ENEMY_BASE_SPEED = 2;
var ENEMY_SIZE = 20;
var enemySpeed = ENEMY_BASE_SPEED;
var enemyCap = MIN_ENEMIES;
var enemies = [];
var enemiesActive = [];

var player = {
rect: Object.create(rect),
speed: PLAYER_SPEED,
score: 0,
lives: 0,
highScore: 0,
precision: 0.0
}

function init(){
	player.rect.w = player.rect.h = 20;
	player.rect.x = WIDTH/2-player.rect.w/2;
	player.rect.y = HEIGHT-player.rect.h-2;
	player.score = 0;
	player.lives = 2;
	
	var i;
	for(i=0; i<MAX_BULLETS; i++){
		bulletsActive[i] = false;
		bullets[i] = Object.create(rect);
		
		bullets[i].w = bullets[i].h = 10;
	}
	
	for(i=0; i<MAX_ENEMIES; i++){
		enemiesActive[i] = false;
		enemies[i] = Object.create(rect);
		
		enemies[i].w = enemies[i].h = ENEMY_SIZE;
	}
	
	window.requestAnimationFrame(mainLoop);
}


function restartGame(){
	enemySpeed = ENEMY_BASE_SPEED;
	enemyCap = MIN_ENEMIES;
	bulletCap = MIN_BULLETS;
	
	bulletsShot = 0;
	
	player.rect.x = WIDTH/2-player.rect.w/2;
	player.rect.y = HEIGHT-player.rect.h-2;
	player.score = 0;
	player.lives = 2;
	player.precision = 0.0;
	
	var i;
	for(i=0; i<MAX_BULLETS; i++)
		bulletsActive[i] = false;
	
	for(i=0; i<MAX_ENEMIES; i++)
		enemiesActive[i] = false;
		
	halt = 0;
	newHighScore = false;
	updateHUD();
}

function mainLoop(nowTime){
	var deltaTime = nowTime - lastTime;
	
	if(frameElapse < frameTime)
		frameElapse += deltaTime;
	else{
		if(!halt)
			update(frameTime/deltaTime);
		draw();
		
		frameElapse -= frameTime;
		frameCount = (frameCount+1)%60;
	}
	
	if(frameCount%30 == 0){
		if(bulletsShot != 0)
			player.precision = Math.trunc((player.score/bulletsShot)*100.0);
		else
			player.precision = 0.0;
		updateHUD();
	}
	
	lastTime = nowTime;
	window.requestAnimationFrame(mainLoop);
}

function update(deltaTime){
	processInput(deltaTime);
	spawnEnemy();
	
	//update player
	if(player.rect.x < 0)
		player.rect.x = 0;
	else if(player.rect.x+player.rect.w >= WIDTH)
		player.rect.x = WIDTH-player.rect.w;
		
	//update bullets
	var i;
	for(i=0; i<bulletCap; i++){
		if(!bulletsActive[i])
			continue;
		
		bullets[i].y -= BULLET_SPEED*deltaTime;
		if(bullets[i].y+bullets[i].h <= 0){
			bulletsActive[i] = false;
			bulletsShot++;
		}
	}
	
	//update enemies
	var j;
	for(j=0; j<enemyCap; j++){
		if(!enemiesActive[j])
			continue;
			
		enemies[j].y += enemySpeed*deltaTime;
		if(enemies[j].y >= HEIGHT){
			enemiesActive[j] = false;
			player.lives--;
		}
			
		//Colision detection
		if(rectanglesCollide(enemies[j], player.rect)){
			player.lives--;
			enemiesActive[j] = false;
			continue;
		}
		
		for(i=0; i<bulletCap; i++){
			if(!bulletsActive[i])
				continue;
				
			if(rectanglesCollide(enemies[j], bullets[i])){
				player.score++;
				enemiesActive[j] = bulletsActive[i] = false;
				bulletsShot++;
				
				if((player.score-1) % 60 == 0)
					player.lives++;
			}
		}
	}
	
	//Game logic
	if(player.lives < 0){
		halt = 2;
		if(player.highScore < player.score){
			player.highScore = player.score;
			newHighScore = true;
		}
		//player.highScore = player.highScore < player.score ? player.score : player.highScore;
		updateHUD();
	}
	
	if(player.score/30)
	enemyCap = Math.min(MIN_ENEMIES + Math.trunc(player.score/30),MAX_ENEMIES);
	
	enemySpeed = ENEMY_BASE_SPEED + player.score/500;
	
	if(player.score%80 == 0)
		bulletCap = Math.min(MIN_BULLETS + Math.trunc(player.score/80),MAX_BULLETS);
	
}

function draw(){
	clearScreen("#000000");
	
	//Draw player
	g.fillStyle = "#00FF00";
	g.fillRect(player.rect.x,player.rect.y,player.rect.w,player.rect.h);

	//Draw bullets
	g.fillStyle = "#0000FF";
	
	var i;
	for(i=0; i<bulletCap; i++)
		if(bulletsActive[i])
			g.fillRect(bullets[i].x, bullets[i].y, bullets[i].w, bullets[i].h);
			
	//Draw enemies
	g.fillStyle = "#FF0000";
	
	for(i=0; i<enemyCap; i++)
		if(enemiesActive[i])
			g.fillRect(enemies[i].x, enemies[i].y, enemies[i].w, enemies[i].h);
			
	if(halt == 1){
		g.fillStyle = "#FFFFFF";
		g.font = "60px Arial";
		g.fillText("PAUSED", WIDTH/2-120, HEIGHT/2+30);

		g.font = "20px Arial";
		g.fillText("Press 'P' to resume", WIDTH/2-90, HEIGHT/2+60);
	}
	else if(halt == 2){
		if(newHighScore){
			g.fillStyle = "#00FF00";
			g.font = "60px Arial";
			g.fillText("NEW HIGHSCORE!!", WIDTH/2 - 280, 60);

			g.font = "30px Arial";
			g.fillText("Score: "+highscoreText, WIDTH/2 - 80, 100);
		}
		else{

		}

		g.fillStyle = "#BB0000";
		g.font = "60px Arial";
		g.fillText("GAME OVER", WIDTH/2-180, HEIGHT/2+30);

		g.fillStyle = "#FFFFFF"
		g.font = "32px Arial";
		g.fillText("Precision: "+precisionText+"%", WIDTH/2-100, HEIGHT/2+70);
		g.fillText("Current high score: "+highscoreText, WIDTH/2 -150, HEIGHT/2+100);
	}
	else{
		g.fillStyle = "#FF0000";
		g.font = "16px Arial";
		g.fillText(livesText, WIDTH - 65,HEIGHT - 5);

		g.fillStyle = "#0000FF";
		g.font = "16px Arial";
		g.fillText(scoreText, 5,HEIGHT - 5);
	}
}
function clearScreen(clearColor){
	g.fillStyle = clearColor;
	g.fillRect(0,0,WIDTH,HEIGHT);
}

function processInput(dT){
	
	if(inputKeysDown.left && !inputKeysDown.right)
		player.rect.x -= player.speed*dT;
	else if(!inputKeysDown.left && inputKeysDown.right)
		player.rect.x += player.speed*dT;

}

function rectanglesCollide(r1,r2){
	var vertical = intervalsCollide(r1.y,r1.h, r2.y,r2.h);
	var horizontal = intervalsCollide(r1.x,r1.w, r2.x,r2.w);
	
	return (vertical && horizontal);
}

//Returns true if intervas colided
function intervalsCollide(o1,s1, o2,s2){
	//(d2^2 - d1^2) < (r1^2 + r2^2) colided
	var r1 = s1/2;
	var x1 = o1+r1;
	
	var r2 = s2/2;
	var x2 = o2+r2;
	
	return Math.abs(x2 - x1) < (r1 + r2);
}

function shootBullet(){
	var i;
	for(i=0;i<bulletCap;i++)
		if(!bulletsActive[i]){
			bulletsActive[i] = true;
			bullets[i].x = player.rect.x+player.rect.w/2-bullets[i].w/2;
			bullets[i].y = player.rect.y+player.rect.h/2-bullets[i].h/2;
			
			break;
		}
}

function spawnEnemy(){
	var i;
	for(i=0; i<enemyCap; i++)
		if(!enemiesActive[i]){
			enemiesActive[i] = true;
			enemies[i].x = Math.trunc((Math.random()*1000)%(WIDTH-ENEMY_SIZE));
			enemies[i].y = -(Math.trunc(Math.random()*10)%5) * ENEMY_SIZE;
		
			break;
		}
}

//It used to display game status in the page's title
//Changed to reflect this into HTML elements instead
function updateHUD(){
//	if(halt != 2)
//		document.title = "♥: " + player.lives + " Score: " + player.score;
//	else
//		document.title = "Score: " + player.score + " Hi-score: "+player.highScore + " Precision: "+player.precision + "%";

	if(halt != 2){
		livesText = player.lives.toString() + " LIVES";
		scoreText = "SCORE:" + player.score.toString();
		precisionText = player.precision.toString();
	}
	else
		highscoreText = player.highScore.toString();
}

//Event Listener
	/*	UP		38
		DOWN	40
		LEFT	37
		RIGHT	39
		SPACE	32	
		P*		80	*/
document.addEventListener("keyup", function(e){
	var keyCode = e.keyCode;
	
	if(keyCode == 37) inputKeysDown.left = false;
	else if(keyCode == 39) inputKeysDown.right = false;
	else if(keyCode == 32){ 
		inputKeysDown.space = false; 
		if(halt == 2)
			restartGame();
		else if(halt == 0)
			shootBullet();
	}	
	else if(keyCode == 80) halt = halt < 2 ? !halt : halt;
});
document.addEventListener("keydown", function(e){
	var keyCode = e.keyCode;
	
	//e.preventDefault() - prevents the browser from scrolling down when space is pressed
	if(keyCode == 37){ inputKeysDown.left = true; e.preventDefault();}
	else if(keyCode == 39){ inputKeysDown.right = true; e.preventDefault();}
	else if(keyCode == 32){ inputKeysDown.space = true; e.preventDefault();} 
	
	 
});

window.requestAnimationFrame(init);
