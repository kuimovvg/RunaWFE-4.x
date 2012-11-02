
function initialize() {
	var ip1 = document.getElementById("infoPath1");
	ip1.DocumentCookie = document.cookie;
	ip1.DocumentURL = document.URL;
}

function try_submit() {
	var ip1 = document.getElementById("infoPath1");
	if(!ip1.ValidateForm())
		return false;

	var redirect = ip1.SubmitForm();
	if ("" != redirect)
		window.location = redirect;
	else
		alert("Request failed");
	return false;
}

function inject(data) {
	document.getElementById("infoPathData1").innerHTML = data;
}