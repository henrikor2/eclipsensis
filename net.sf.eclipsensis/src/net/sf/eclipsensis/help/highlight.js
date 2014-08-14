function highlight( s, regex, o ) {
  if( !s ) { return; }
  var d = document;
  if( !regex ) {
    s = s.replace( /([\\|^$()[\]{}.*+?])/g, '\\$1' );
  }
  if( /^\s*$/.test(s) ) {
    return;
  }
  o = [ o || d.documentElement || d.body ];
  var r = new RegExp( "\\b("+s+")\\b", 'gi' ),
      h = d.createElement('span'),
      i = 0, j, k, l, m, n=0, t;
  h.style.color = 'HighlightText';
  h.style.backgroundColor = 'Highlight';
  do {
    m = o[i];
    if( m.nodeType===3 ) {
      r.lastIndex = 0;
      l = r.exec(m.nodeValue);
      if( l !== null ) {
        k = l[1].length;
        if( r.lastIndex > k ) {
          m.splitText( r.lastIndex - k );
          m = m.nextSibling;
        }
        if( m.nodeValue.length > k ) {
          m.splitText(k);
          o[i++] = m.nextSibling;
        }
        t = h.cloneNode( true );
        t.appendChild( d.createTextNode( l[1] ) );n++;
        m.parentNode.replaceChild( t, m );
      }
    }
    else {
      j = m.childNodes.length;
      while (j) { o[i++] = m.childNodes.item(--j); }
    }
  } while(i--); return;
}

for(i=0; i<keywords.length; i++) {
  highlight(keywords[i],regex[i]);
}