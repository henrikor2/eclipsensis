/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.util;

import java.lang.reflect.Method;
import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.Position;
import net.sf.eclipsensis.util.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.RGB;

public abstract class TypeConverter<T>
{
    public abstract String asString(T o);
    public abstract T asType(String s);
    public abstract T makeCopy(T o);

    @SuppressWarnings("unchecked")
    public final String toString(Object o)
    {
        return asString((T)o);
    }

    public String asString(T o, T defaultValue)
    {
        String string;
        try {
            string = asString(o);
        }
        catch(Exception ex) {
            string = null;
        }
        return (string != null?string:asString(defaultValue));
    }

    public T asType(String s, T defaultValue)
    {
        T type;
        try {
            type = asType(s);
        }
        catch(Exception ex) {
            type = null;
        }
        return (type != null?type:makeCopy(defaultValue));
    }

    public static final TypeConverter<Point> POINT_CONVERTER = new TypeConverter<Point>() {
        @Override
        public String asString(Point o)
        {
            return (o==null?null:Common.flatten(new int[]{(o).x,(o).y},','));
        }

        @Override
        public Point asType(String s)
        {
            Point p = null;
            if(s != null) {
                String[] parts = Common.tokenize(s,',');
                if(parts.length == 2) {
                    p = new Point();
                    p.x = Integer.parseInt(parts[0]);
                    p.y = Integer.parseInt(parts[1]);
                }
            }
            return p;
        }

        @Override
        public Point makeCopy(Point o)
        {
            return new Point(o);
        }
    };

    public static final TypeConverter<Position> POSITION_CONVERTER = new TypeConverter<Position>() {
        @Override
        public String asString(Position o)
        {
            String s = null;
            if(o != null ) {
                Rectangle rect = (o).getBounds();
                s = Common.flatten(new int[]{rect.x, rect.y, rect.width, rect.height},',');
            }
            return s;
        }

        @Override
        public Position asType(String s)
        {
            Position p = null;
            if(s != null) {
                String[] parts = Common.tokenize(s,',');
                if(parts.length == 4) {
                    p = new Position();
                    p.setLocation(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    p.setSize(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                }
            }
            return p;
        }

        @Override
        public Position makeCopy(Position o)
        {
            return (o).getCopy();
        }
    };

    public static final TypeConverter<Boolean> BOOLEAN_CONVERTER = new TypeConverter<Boolean>() {
        @Override
        public String asString(Boolean o)
        {
            return (o != null?(o).toString():null);
        }

        @Override
        public Boolean asType(String s)
        {
            return (s == null?null:Boolean.valueOf(s));
        }

        @Override
        public Boolean makeCopy(Boolean o)
        {
            return o;
        }
    };

    public static final TypeConverter<RGB> RGB_CONVERTER = new TypeConverter<RGB>() {
        //There is a bug in InstallOptions where R & B are reversed
        private RGB flip(RGB rgb)
        {
            return new RGB(rgb.blue, rgb.green, rgb.red);
        }

        @Override
        public String asString(RGB o)
        {
            if(o != null) {
                StringBuffer buf = new StringBuffer("0x"); //$NON-NLS-1$
                RGB rgb = flip(o);
                buf.append(ColorManager.rgbToHex(rgb));
                return buf.toString();
            }
            return null;
        }

        @Override
        public RGB asType(String s)
        {
            if(s != null && s.startsWith("0x") && s.length()==8) { //$NON-NLS-1$
                RGB rgb = ColorManager.hexToRGB(s.substring(2));
                if( (rgb.red >= 0 && rgb.red <= 255) &&
                    (rgb.green >= 0 && rgb.green <= 255) &&
                    (rgb.blue >= 0 && rgb.blue <= 255)) {
                    return flip(rgb);
                }
            }
            return null;
        }

        @Override
        public RGB makeCopy(RGB o)
        {
            RGB rgb = o;
            return new RGB(rgb.red,rgb.green,rgb.blue);
        }
    };

    public static final TypeConverter<Integer> INTEGER_CONVERTER = new TypeConverter<Integer>() {
        @Override
        public String asString(Integer o)
        {
            return (o==null?null:(o).toString());
        }

        @Override
        public Integer asType(String s)
        {
            return (Common.isEmpty(s)?null:Integer.valueOf(s));
        }

        @Override
        public Integer makeCopy(Integer o)
        {
            return o;
        }
    };

    public static final TypeConverter<Integer> HEX_CONVERTER = new TypeConverter<Integer>() {
        @Override
        public String asString(Integer o)
        {
            return (o == null?null:"0x"+Integer.toHexString((o).intValue())); //$NON-NLS-1$
        }

        @Override
        public Integer asType(String s)
        {
            return (Common.isEmpty(s)?null:Integer.valueOf(s.substring(2), 16));
        }

        @Override
        public Integer makeCopy(Integer o)
        {
            return o;
        }
    };

    public static final TypeConverter<String[]> STRING_ARRAY_CONVERTER = new TypeConverter<String[]>() {
        @Override
        public String asString(String[] o)
        {
            return (o==null?null:Common.flatten(o,IInstallOptionsConstants.LIST_SEPARATOR));
        }

        @Override
        public String[] asType(String s)
        {
            return (s==null?null:Common.tokenize(s,IInstallOptionsConstants.LIST_SEPARATOR,false));
        }

        @Override
        public String[] makeCopy(String[] o)
        {
            return (o).clone();
        }
    };

    public static final TypeConverter<List<String>> STRING_LIST_CONVERTER = new TypeConverter<List<String>>() {
        @Override
        public String asString(List<String> o)
        {
            return (o==null?null:Common.flatten(o.toArray(Common.EMPTY_STRING_ARRAY),IInstallOptionsConstants.LIST_SEPARATOR));
        }

        @Override
        public List<String> asType(String s)
        {
            return (s==null?null:Common.tokenizeToList(s,IInstallOptionsConstants.LIST_SEPARATOR,false));
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> makeCopy(List<String> o)
        {
            if(o instanceof Cloneable) {
                try {
                    Method method = o.getClass().getMethod("clone",(Class[])null); //$NON-NLS-1$
                    return (List<String>) method.invoke(o,(Object[])null);
                }
                catch (Exception e) {
                    InstallOptionsPlugin.getDefault().log(e);
                }
            }
            return new ArrayList<String>(o);
        }
    };

    public static final TypeConverter<String> STRING_CONVERTER = new TypeConverter<String>() {
        @Override
        public String asString(String o)
        {
            return o;
        }

        @Override
        public String asType(String s)
        {
            return s;
        }

        @Override
        public String makeCopy(String o)
        {
            return o;
        }
    };

    public static final TypeConverter<String> ESCAPED_STRING_CONVERTER = new TypeConverter<String>() {
        @Override
        public String asString(String o)
        {
            if(o != null) {
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                char[] chars = (o).toCharArray();
                boolean escaped = false;
                for (int i = 0; i < chars.length; i++) {
                    if(escaped) {
                        switch(chars[i]) {
                            case 'r':
                                buf.append("\r"); //$NON-NLS-1$
                                break;
                            case 'n':
                                buf.append("\n"); //$NON-NLS-1$
                                break;
                            case 't':
                                buf.append("\t"); //$NON-NLS-1$
                                break;
                            case '\\':
                                buf.append("\\"); //$NON-NLS-1$
                                break;
                            default:
                                buf.append("\\").append(chars[i]); //$NON-NLS-1$
                        }
                        escaped = false;
                    }
                    else {
                        if(chars[i] == '\\') {
                            escaped = true;
                        }
                        else {
                            buf.append(chars[i]);
                        }
                    }
                }
                if(escaped) {
                    buf.append("\\"); //$NON-NLS-1$
                }
                return buf.toString();
            }

            return null;
        }

        @Override
        public String asType(String s)
        {
            if(s != null) {
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                char[] chars = s.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    switch(chars[i]) {
                        case '\r':
                            buf.append("\\r"); //$NON-NLS-1$
                            break;
                        case '\n':
                            buf.append("\\n"); //$NON-NLS-1$
                            break;
                        case '\t':
                            buf.append("\\t"); //$NON-NLS-1$
                            break;
                        case '\\':
                            buf.append("\\\\"); //$NON-NLS-1$
                            break;
                        default:
                            buf.append(chars[i]);
                    }
                }
                return buf.toString();
            }

            return null;
        }

        @Override
        public String makeCopy(String o)
        {
            return o;
        }
    };

    public static final TypeConverter<Dimension> DIMENSION_CONVERTER = new TypeConverter<Dimension>() {
        @Override
        public String asString(Dimension o)
        {
            return (o==null?null:Common.flatten(new int[]{(o).width,(o).height},','));
        }

        @Override
        public Dimension asType(String s)
        {
            Dimension d = null;
            if (s != null) {
                String[] parts = Common.tokenize(s,',');
                if(parts.length == 2) {
                    d = new Dimension();
                    d.width = Integer.parseInt(parts[0]);
                    d.height = Integer.parseInt(parts[1]);
                }
            }
            return d;
        }

        @Override
        public Dimension makeCopy(Dimension o)
        {
            return new Dimension(o);
        }
    };
}
