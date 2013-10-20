    //Изменение размеров экрана и подгон под responsive версию
    $(window).resize(function() {
      if($(window).width() < 768) {
	    $(".container_right").css('width', '100%').css('margin', '0');
      }
	  else {
	    $(".container_right > a[class='fix-line opened']").css('left', '0').parent().css('width', '75%').css('margin', '0 0 0 25%');
	  }
    }); 

	//Нажатие на панель при большой разрешении экрана
    $(".fix-line").click(function () {
      var line = $(this);  
      var panel = line.parent().find('.left').find('.menu'); 
	  var container = line.parent();
	
      if (line.hasClass("opened")) {  
        line.removeClass('opened').addClass('closed'); 
		line.css('background-image', 'url(imgs/bg-fix-line-closed.png)');		
		$(".fix-line-horizontal").removeClass('opened').addClass('closed').find('div').css('background-image', 'url(imgs/bg-fix-line-horizontal-closed.png)');
		line.css('left', '0');
		container.css('width', '100%');
		container.css('margin', '0');
        panel.hide();
      }  
      else if (line.hasClass("closed")) {  
        line.removeClass('closed').addClass('opened');  
		line.css('background-image', 'url(imgs/bg-fix-line.png)');	
		$(".fix-line-horizontal").removeClass('closed').addClass('opened').find('div').css('background-image', 'url(imgs/bg-fix-line-horizontal.png)');
		line.css('left', '-11px');		
		container.css('width', '75%');
		container.css('margin', '0 0 0 25%');		
		panel.show();
      }  	
    });  
	
    //Нажатие на панель при маленьком разрешении экрана
    $(".fix-line-horizontal").click(function () {
      var line = $(this);  
      var panel = line.parent().find('.menu'); 
	
      if (line.hasClass("opened")) {  
        line.removeClass('opened').addClass('closed'); 
		line.find('div').css('background-image', 'url(imgs/bg-fix-line-horizontal-closed.png)');
		$(".fix-line").removeClass('opened').addClass('closed').css('background-image', 'url(imgs/bg-fix-line-closed.png)');
		panel.hide();          
      }  
      else if (line.hasClass("closed")) {  
        line.removeClass('closed').addClass('opened'); 
		line.find('div').css('background-image', 'url(imgs/bg-fix-line-horizontal.png)');
		$(".fix-line").removeClass('closed').addClass('opened').css('background-image', 'url(imgs/bg-fix-line.png)');
		panel.show();
      }  	
    });  
