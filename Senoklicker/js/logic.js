var seno = 0;
var adding = 1;
var in_addingpersec = 0;

var b_Kadets = 0;
var b_Otdeleniya = 0;
var b_Vzvods = 0;
var b_Rots = 0;

function addSeno(){
	seno = seno + adding;
	update();
}

function buySenoer(senoerId){
	switch(senoerId){
		case 0:
			if (seno < 5) { alert("Сена не хватает! Иди работай, сопляк!"); break; }
			b_Kadets++;
			in_addingpersec++;
			$('div.b_Kadets').html(b_Kadets);
			seno = seno - 5;
			break;
		case 1:
			if (seno < 30) { alert("Недостаточно сена! Иди за туманы, кадет!"); break; }
			b_Otdeleniya++;
			in_addingpersec = in_addingpersec + 6;
			$('div.b_Otdeleniya').html(b_Otdeleniya);
			seno = seno - 30;
			break;
		case 2:
			if (seno < 600) { alert("Ты бы ещё кефира мне принёс! Иди коси, щегол!"); break; }
			b_Vzvods++;
			in_addingpersec = in_addingpersec + 20;
			$('div.b_Vzvods').html(b_Vzvods);
			seno = seno - 600;
			break;
		case 3:
			if (seno < 48000) { alert("Ты совсем охерел в атаке, чувак? Где сено? Иди работай!"); break; }
			b_Rots++;
			in_addingpersec = in_addingpersec + 80;
			$('div.b_Rots').html(b_Rots);
			seno = seno - 48000;
			break;
		case 4:
			if (seno < 10000) { alert("Ты у меня сейчас по-пластунски ползать будешь! Где сено, козёл ёбаный?"); break; }
			adding = adding + 5;
			seno = seno - 10000;
			break;
	}
	update();
}

function update(){
	$('div.counter').html(seno);
	$('div.senopc').html(adding);
	$('div.senops').html(in_addingpersec);
	var kadcount = b_Kadets + (b_Otdeleniya*6) + (b_Vzvods*20) + (b_Rots*80);
	updateStatus(kadcount);
	phraze();
}

function bg(){
    seno = seno + in_addingpersec;
    $('div.counter').html(seno);
}

function updateStatus(kadcount){
	if (kadcount > 9999999999) $('div.coloniastatus').html("Моя оборона"); else
	if (kadcount > 999999999) $('div.coloniastatus').html("Алькатрас"); else
	if (kadcount > 99999999) $('div.coloniastatus').html("Чёрный дельфин"); else
	if (kadcount > 9999999) $('div.coloniastatus').html("Бутырка"); else
	if (kadcount > 999999) $('div.coloniastatus').html("Обученыйе армейкой"); else
	if (kadcount > 99999) $('div.coloniastatus').html("Армия <s>зэков</s> кадет"); else
	if (kadcount > 10799) $('div.coloniastatus').html("Кадетство"); else
	if (kadcount > 1079) $('div.coloniastatus').html("ККК"); else
	if (kadcount > 119) $('div.coloniastatus').html("Рота"); else
	if (kadcount > 29) $('div.coloniastatus').html("Взвод"); else
	if (kadcount > 9) $('div.coloniastatus').html("Пачаны с галёрки"); else
	if (kadcount >= 0) $('div.coloniastatus').html("Парашники");
}

function phraze(){
	alert(random);
}
/*
function sleep(msec) {
    var k = function_continuation;
    setTimeout(function() { resume k <- mesc; }, msec);
    suspend;
}*/
