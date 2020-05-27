<%
  ui.includeJavascript("registrationapp", "field/picture/dropzone.js") 
  ui.includeJavascript("registrationapp", "field/picture/webcam.js")  
  ui.includeCss("registrationapp","basic.css")
  ui.includeCss("registrationapp","dropzone.css")
%>
<style>
    .camera {
      margin-left: auto;
      margin-right: auto;
      overflow: auto;
    }

    .camcontrols {
      width: 50%;
      margin: 0 auto;
      text-align: center;
    }
  </style>

<p  id="dropezoneId" class="left dropzone">
 <label for="${ config.id }-field">
        ${ config.label } 
    </label>

<input type="hidden" id="${ config.id }-field" name="${ config.formFieldName }" />

 <div id="camsection">  
    <div class='camera' id="camera"></div>
    <div class="camcontrols">
      <a  value="Close" style="display: none;" id="close" onclick="closeNow()" ><i class="icon-remove"></i></a>
      <a value="Accept" style="display: none;" id="accept" onclick="acceptImage()"><i class="icon-ok"></i></a>
      <a value="Take a Snap" style="display: none;" id="snap" onclick="takeSnapShot()"><i class="icon-camera"></i></a>
    </div>
  </div>
  <a id="showCam" onclick="showCam()"><i class="icon-camera"></i></a>
</p>

<script>

Dropzone.autoDiscover = false;

var myDropzone = new Dropzone("#dropezoneId", {
        paramName: "file", 
        maxFilesize: 2, // MB
        url: '/openmrs/ws/patientimage',
        thumbnailHeight: 100,
        thumbnailWidth: 100,
        maxFiles: 1,
        dictDefaultMessage: "Drop picture here",
        autoProcessQueue: false,
        renameFile: function (file) {
          file.name;
        },
        init: function () {
          this.on(
            'addedfile', function (file) {
              if (this.files[1] != null) {
                this.removeFile(this.files[0]);
              }
               document.getElementById("${ config.id }-field").value = file.name;
            },
            'sending', function (file, xhr, formData) {
            },
            'success', function (file, response) {
              emr.navigateTo({"applicationUrl": response.redirectUrl});
            },
            'error', function (file, response, xhr) {
              console.log(response);
            }
          );
        }

});

//Start of methods for camera
  var camDataUri;
  function showCam() {
    var myDropzone = Dropzone.forElement(".dropzone");
    myDropzone.removeAllFiles(true);
    document.getElementById('camsection').style.display = 'block';
    // CAMERA SETTINGS.
    Webcam.set({
      width: 450,
      height: 300,
      image_format: 'jpeg',
      jpeg_quality: 100
    });
    Webcam.attach('#camera');
    document.getElementById('close').style.display = 'inline-block';
    document.getElementById('snap').style.display = 'inline-block'; 
    document.getElementById('showCam').style.display = 'none';
  }

  function closeNow() {
    Webcam.reset();
    document.getElementById('camsection').style.display = 'none';
    document.getElementById('showCam').style.display = 'block';
    document.getElementById('accept').style.display = 'none';
  }

  // SHOW THE SNAPSHOT.
  takeSnapShot = function () {
    Webcam.snap(function (data_uri) {
      this.camDataUri = data_uri;
      document.getElementById('camera').innerHTML =
        '<img src="' + data_uri + '" width="400" height="300" />';
    });
    document.getElementById('snap').style.display = 'none';
    document.getElementById('accept').style.display = 'inline-block';
  }

  function acceptImage() {
    var myDropzone = Dropzone.forElement(".dropzone");
    var file = dataUritoBlob(camDataUri);
    myDropzone.addFile(file);
    document.getElementById("${ config.id }-field").value = "Camera Image";
    closeNow();
    document.getElementById('accept').style.display = 'none';
  }
  //helper method
  function dataUritoBlob(dataUri) {
    // convert base64/URLEncoded data component to raw binary data held in a string
    var byteString;
    if (dataUri.split(',')[0].indexOf('base64') >= 0)
      byteString = atob(dataUri.split(',')[1]);
    else
      byteString = unescape(dataUri.split(',')[1]);

    // separate out the MIME type String
    var mimeType = dataUri.split(',')[0].split(':')[1].split(';')[0];

    // write the bytes of the string to a typed array
    var ia = new Uint8Array(byteString.length);
    for (var i = 0; i < byteString.length; i++) {
      ia[i] = byteString.charCodeAt(i);
    }

    return new Blob([ia], {
      type: mimeType
    });
  }
//End of cam methods 
</script>
