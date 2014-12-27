var main = function(){	
	$("#url-input").keypress(function(event) {
		if(event.which === 13){
			var urlInput = $(this).val();
			if (!isEmpty(urlInput)) {		
				$('#url-form#login').submit();
			}else{
				event.preventDefault();
	      		return false;
			}
		}
	});

	$('#url-form').on('submit',function(event) {
    	var openInType = $('input[name=open-in-type]:checked', '#url-form').val().toLowerCase();
    	$(this).attr('target', openInType);
    });

    $('#image-url-form').on('submit',function(event) {
    	var openInType = $('input[name=open-in-type]:checked', '#url-form').val().toLowerCase();
    	$(this).attr('target', openInType);
    });

	$("#url-input").autoGrowInput({minWidth:220,comfortZone:20});
	$("#image-url-input").autoGrowInput({minWidth:200,comfortZone:20});

	setInterval(showCashedSitesAjax, 20000);
};

function showCashedSitesAjax() {
	var xmlhttp;

	if (window.XMLHttpRequest) {
	  xmlhttp=new XMLHttpRequest();
	}else{
	  // code for IE6, IE5
	  xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	}

	xmlhttp.onreadystatechange=function() {
	  if (xmlhttp.readyState==4 && xmlhttp.status==200) {
	    document.getElementById("div-cashed-sites-list").innerHTML = xmlhttp.responseText;
	  }
	}

	xmlhttp.open("GET", "/getcashedsites", true);
	xmlhttp.send();
}

$(document).ready(main);

function isEmpty(data){
	if(typeof(data) == 'number' || typeof(data) == 'boolean'){
    	return false;
  	}else if(typeof(data) == 'undefined' || data === null){
		return true;
	}else if(typeof(data.length) != 'undefined'){
		return data.length == 0;
	}

	var count = 0;
	for(var i in data){
		if(data.hasOwnProperty(i)){
	  		count ++;
		}
	}

	return count == 0;
}

(function($){

    $.fn.autoGrowInput = function(o) {

        o = $.extend({
            maxWidth: 1000,
            minWidth: 0,
            comfortZone: 70
        }, o);

        this.filter('input:text').each(function(){

            var minWidth = o.minWidth || $(this).width(),
                val = '',
                input = $(this),
                testSubject = $('<tester/>').css({
                    position: 'absolute',
                    top: -9999,
                    left: -9999,
                    width: 'auto',
                    fontSize: input.css('fontSize'),
                    fontFamily: input.css('fontFamily'),
                    fontWeight: input.css('fontWeight'),
                    letterSpacing: input.css('letterSpacing'),
                    whiteSpace: 'nowrap'
                }),
                check = function() {

                    if (val === (val = input.val())) {return;}

                    // Enter new content into testSubject
                    var escaped = val.replace(/&/g, '&amp;').replace(/\s/g,'&nbsp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    testSubject.html(escaped);

                    // Calculate new width + whether to change
                    var testerWidth = testSubject.width(),
                        newWidth = (testerWidth + o.comfortZone) >= minWidth ? testerWidth + o.comfortZone : minWidth,
                        currentWidth = input.width(),
                        isValidWidthChange = (newWidth < currentWidth && newWidth >= minWidth)
                                             || (newWidth > minWidth && newWidth < o.maxWidth);

                    // Animate width
                    if (isValidWidthChange) {
                        input.width(newWidth);
                    }

                };

            testSubject.insertAfter(input);

            $(this).bind('keyup keydown blur update', check);
        });

        return this;
    };

})(jQuery);