function ajaxUpdateCompleted(data) {
    if (data.status === 'success') { 
        updateRating();
    }
}

function updateRating(){
        var list = document.getElementsByClassName('game-rating-label');
        var length = list.length;

        console.log("Found: "+length + " elements.");

        for(let i = 0; i < length; i++){
            let value = list[0].getAttribute('value');

            if(value >= 85)
                list[0].classList.add('very-good');
            else if(value >= 55)
                list[0].classList.add('good');
            else if (value >= 30)
                list[0].classList.add('bad');
            else
                list[0].classList.add('very-bad');

            list[0].classList.toggle('game-rating-label');
        }
    }

    updateRating();