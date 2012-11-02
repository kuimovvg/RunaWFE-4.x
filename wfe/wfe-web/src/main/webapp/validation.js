function clearErrorMessages(form) {
    var elements = getErrorImgElements(form, "img");
    // now delete the rows
    for (var i = 0; i < elements.length; i++) {
        var elem = elements[i];
	elem.parentNode.removeChild(elem);
    }
}

function getErrorImgElements(node, tag) {
	var classElements = new Array();
	if ( node == null ) {
		node = document;
	}
	if ( tag == null ) {
		tag = '*';
	}
	var els = node.getElementsByTagName(tag);
	var elsLen = els.length;
	for (i = 0, j = 0; i < elsLen; i++) {
		if (els[i].getAttribute("errorFor") != null) {
			classElements[j] = els[i];
			j++;
		}
	}
	return classElements;
}

function insertAfter(referenceNode, node) {
	referenceNode.parentNode.insertBefore(node, referenceNode.nextSibling);
}

function addError(field, errorText) {
    //try {
    	var toolTipId = field.getAttribute("name") + "_tt";
    	var toolTipDiv = document.createElement("div");
    	toolTipDiv.setAttribute("id", toolTipId);
    	toolTipDiv.className = "field-hint";
    	toolTipDiv.style.display = 'none';
        //toolTipDiv.setAttribute("errorFor", "yes");
    	insertAfter(field, toolTipDiv);

    	var innerDiv = document.createElement("div");
    	innerDiv.innerHTML = errorText;
    	toolTipDiv.appendChild(innerDiv);
    	
        var errorImg = document.createElement("img");
    	attachMyEvent(errorImg, "mouseover", function (event) { showTip(event, toolTipId) });
    	attachMyEvent(errorImg, "mouseout", function (event) { hideTip(toolTipId) });
        //errorImg.setAttribute("alt", errorText);
        //errorImg.setAttribute("title", errorText);
        errorImg.setAttribute("src", "/wfe/images/error.gif");
        errorImg.setAttribute("errorFor", "yes");
		insertAfter(field, errorImg);
//    } catch (e) {
  //      alert(e);
    //}
}

function attachMyEvent(elem, eventName, fListener) {
	if (elem.addEventListener) {
		elem.addEventListener (eventName, fListener, false);
	} else if (elem.attachEvent) {
		elem.attachEvent("on" + eventName, fListener);
	} else {
		elem.setAttribute("on" + eventName, fListener);
	}
}


