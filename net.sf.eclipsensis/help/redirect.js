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
var numberRegExp = /^[1-9][0-9]*$/
function getPrefix()
{
    var prefix;
    var parts;
    var path;

    path = location.pathname;
    if(path.charAt(0) == '/') {
        path = path.substr(1);
    }

    parts = path.split('/');
    if(parts.length >= 2) {
        prefix = '/'+parts[0]+'/'+parts[1];
    }
    else {
        prefix = '/help/topic';
    }
    return prefix;
}

function prependSlash(url)
{
    if(url && url.charAt(0) != '/') {
        url = "/" + url;
    }
    return url;
}

function redirectEclipse(url)
{
    document.location = getPrefix() + prependSlash(url);
}

function _redirectNSIS(path, url)
{
    document.location = getPrefix() + prependSlash(path) + prependSlash(url);
}

function redirectNSIS(url)
{
    _redirectNSIS("/net.sf.eclipsensis/help/NSIS/Docs", url);
}

function redirectNSISContrib(url)
{
    if(nsisContribPath != null) {
        _redirectNSIS(nsisContribPath, url);
        return;
    }
    _redirectNSIS("/net.sf.eclipsensis/help/NSIS/Contrib", url);
}

function redirectNSISKeyword(keyword)
{
    _redirectNSIS("/net.sf.eclipsensis/help/NSIS/keyword", keyword);
}

function redirectNSISSection(section)
{
    var parts;
    var url;

    parts = section.split(".");
    if(parts.length > 1) {
        url = "Section"+parts[0]+"."+parts[1]+".html#"+section;
    }
    else {
        if(numberRegExp.test(parts[0])) {
            url = "Chapter"+parts[0]+".html";
        }
        else {
            url = "Appendix"+parts[0]+".html";
        }
    }
    redirectNSIS(url);
}
//-->
