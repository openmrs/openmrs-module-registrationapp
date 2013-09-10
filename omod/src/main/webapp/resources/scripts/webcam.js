jq = jQuery;

jq(function() {
	var streaming = false,
	video        = document.querySelector('#video'),
	cover        = document.querySelector('#cover'),
	canvas       = document.querySelector('#canvas'),
	startbutton  = document.querySelector('#startbutton'),
	width = 200,
	height = 200;
	
	var enablePhoto = true;
	
	navigator.getMedia = ( navigator.getUserMedia || 
			navigator.webkitGetUserMedia ||
			navigator.mozGetUserMedia ||
			navigator.msGetUserMedia);

	navigator.getMedia(
			{ 
				video: true, 
				audio: false 
			},
			function(stream) {
				if (navigator.mozGetUserMedia) { 
					video.mozSrcObject = stream;
				} else {
					var vendorURL = window.URL || window.webkitURL;
					video.src = vendorURL ? vendorURL.createObjectURL(stream) : stream;
					jq("#message-error").hide();
					jq("#startbutton").removeAttr('disabled');
				}
				video.play();
			},
			function(err) {
				console.log("An error occured! " + err);	
				jq("#video").hide();
				jq("#startbutton").addClass("disabled");
				enablePhoto = false;
			}
	);

	video.addEventListener('canplay', function(ev){
		if (!streaming) {
			height = video.videoHeight / (video.videoWidth/width);
			video.setAttribute('width', width);
			video.setAttribute('height', height);
			canvas.setAttribute('width', width);
			canvas.setAttribute('height', height);
			streaming = true;
		}
	}, false);

	function takepicture() {
		canvas.width = width;
		canvas.height = height;
		canvas.getContext('2d').drawImage(video, 0, 0, width, height);
		var data = canvas.toDataURL('image/png');
		photo.setAttribute('src', data);
		confirmPhoto.setAttribute('src', data);
		patientPhoto.setAttribute('value', data);
	}

	startbutton.addEventListener('click', function(ev){
		if(enablePhoto){
			takepicture();
			ev.preventDefault();
			jq(".default-patient-photo").hide();
		}
	}, false);


	document.addEventListener("keydown", keyDownTextField, false);

	function keyDownTextField(e) {
		var selected = jQuery('fieldset.photo').hasClass('focused');
		var keyCode = e.keyCode;
		if(keyCode==32 && selected && enablePhoto) {
			takepicture();
			jq("#photo-field").val('true');
			e.preventDefault();
			jq(".default-patient-photo").hide();
		}
	}
	
});
