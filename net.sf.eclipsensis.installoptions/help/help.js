<!--
//###############################################################################
//# Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
//# All rights reserved.
//# This program is made available under the terms of the Common Public License
//# v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
//#
//# Contributors:
//# Sunil Kamath (IcemanK) - initial API and implementation
//###############################################################################

// these are the scripts that are called in the links.
var ie = document.all;
var ns4 = document.layers;
var mozilla = document.getElementById;
var DHTML = (mozilla || ie || ns4);
var highlightColor = "rgb(153, 204, 204)";
var lastObj = null;
var lastObjBorder = null;

function highlightDef(id)
{
    if(DHTML) {
    	if(lastObj) {
    	    lastObj.style.border = lastObjBorder;
    	    lastObj = null;
    	    lastObjBorder = null;
    	}
    	lastObj = getObj(id);
    	if(lastObj) {
            lastObjBorder = lastObj.style.border;
            lastObj.style.border = "2px solid "+highlightColor;
        }
    }
}

function getObj(name)
{
    var temp = new makeObj(name);
    if(temp.obj) {
        return temp;
    }
    else {
        return null;
    }
}

function makeObj(name)
{
    if (document.getElementById)
    {
  	    this.obj = document.getElementById(name);
  	    if(this.obj) {
    	    this.style = this.obj.style;
        }
    }
    else if (document.all)
    {
	    this.obj = document.all[name];
  	    if(this.obj) {
    	    this.style = this.obj.style;
        }
    }
    else if (document.layers)
    {
   	    this.obj = document.layers[name];
   	    this.style = document.layers[name];
    }
}

function toggleImage(name, index, images)
{
    if(DHTML) {
        index = 1-index;
        document.images[name].src = images[index].src;
    }
    return index;
}

function changeCursor(id, cur)
{
    if(DHTML) {
        var obj = getObj(id);
        obj.style.cursor = cur;
    }
}

function getQueryVariable(variable)
{
  var query = document.location.search.substring(1);
  var vars = query.split("&");
  for (var i=0;i<vars.length;i++) {
    var pair = vars[i].split("=");
    if (pair[0] == variable) {
      return pair[1];
    }
  }
  return null;
}

function checkHighlight()
{
    var id = getQueryVariable("id");
    if(id) {
        highlightDef(id);
    }
}
// -->
