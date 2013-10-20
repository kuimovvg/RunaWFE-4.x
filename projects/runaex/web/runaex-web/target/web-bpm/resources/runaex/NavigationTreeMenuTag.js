addStyle('categoryitems');
addStyle('subcategoryitems');

ddaccordion.init({
  headerclass: "expandable", 
  contentclass: "categoryitems", 
  revealtype: "click", 
  mouseoverdelay: 200, 
  collapseprev: false,
  defaultexpanded: [0],
  onemustopen: false,
  animatedefault: false, 
  persiststate: false,
  toggleclass: ["", "openheader"], 
  togglehtml: ["prefix", "", ""], 
  animatespeed: "fast",
  oninit:function(headers, expandedindices){ 
    
  },
  onopenclose:function(header, index, state, isuseractivated){ 
    
  }
});

ddaccordion.init({ 
  headerclass: "subexpandable", 
  contentclass: "subcategoryitems", 
  revealtype: "click", 
  mouseoverdelay: 200, 
  collapseprev: false,
  defaultexpanded: [],
  onemustopen: false,
  animatedefault: false, 
  persiststate: false,
  toggleclass: ["opensubheader", "closedsubheader"], 
  togglehtml: ["none", "", ""], 
  animatespeed: "fast",
  oninit:function(headers, expandedindices){ 
    
  },
  onopenclose:function(header, index, state, isuseractivated){ 
    
  }
});

function addStyle(contentclass) {
  document.write('<style type="text/css">\n');
  document.write('.' + contentclass + '{display: none}\n'); //generate CSS to hide contents
  document.write('a.hiddenajaxlink{display: none}\n'); //CSS class to hide ajax link
  document.write('<\/style>');
}