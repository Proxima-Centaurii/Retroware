var colourBox = document.getElementById("colourSelected");

function changeColour(val){
	colourBox.style.background = "hsl("+val+",100%,50%)";
}