var cnv = document.getElementById("screen");
var gfx = cnv.getContext("2d");

var V_SIZE = 500;
var WIDTH = 640;
var HEIGHT = 420;


cnv.width = WIDTH;
cnv.height = HEIGHT;
gfx.scale(WIDTH/V_SIZE, HEIGHT/V_SIZE);

var test_image = new Image();
test_image.onload = function(){ gfx.drawImage(test_image, 10, 10, 150, 150);}
test_image.src ="media/test_default.png";

//------------

function render(){
	//Clear screen
	gfx.fillStyle = "#000000";
	gfx.fillRect(0,0,V_SIZE,V_SIZE);
	
	gfx.drawImage(test_image,10,10,150,150);

	//Display text on the screen
	gfx.fillStyle = "#ff00ff";
	gfx.font = "30px Consolas";
	gfx.fillText("TEST GAME", V_SIZE/2-(gfx.measureText("TEST GAME").width)/2, V_SIZE/8 + 50);
	
}

document.addEventListener("mouseup",function(e){
	render();
});

document.addEventListener("touchend",function(e){
	e.preventDefault();
	render();
},false);

render();
