/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 *******************************************************************************/

package net.sf.eclipsensis.util;

import java.beans.*;
import java.lang.reflect.*;
import java.net.URL;
import java.rmi.dgc.VMID;
import java.text.BreakIterator;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.winapi.*;
import net.sf.eclipsensis.util.winapi.WinAPI.HKEY;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * Common class is the Swiss Army Knife of the project. Most miscellaneous utility functions
 * have been dumped in here.
 *
 * @author Sunil.Kamath
 */
public class Common
{
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Integer ZERO = new Integer(0);
    public static final Integer ONE = new Integer(1);

    private Common()
    {
    }

    public static boolean isEmpty(String string)
    {
        return string == null || string.trim().length() == 0;
    }

    /**
     * Flip array
     *
     * @param array       Array to be resized
     */
    public static Object flipArray(Object array)
    {
        if(array != null && array.getClass().isArray()) {
            int len = Array.getLength(array);
            int n = (2*len+1)/4;
            for(int i=0; i<n; i++) {
                int j = len - 1 - i;
                Object temp = Array.get(array,i);
                Array.set(array,i,Array.get(array,j));
                Array.set(array,j,temp);
            }
            return array;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Resize array while keeping existing elements
     *
     * @param array       Array to be resized
     * @param offset      Offset index
     * @param newLength   New length of array
     * @return            New array of different size
     */
    public static Object resizeArray(Object array, int offset, int newLength)
    {
        if (array == null || newLength < 0)
        {
            throw new IllegalArgumentException();
        }
        Class<?> c = array.getClass();
        if ( !c.isArray() ) {
            throw new IllegalArgumentException();
        }
        int oldLength = Array.getLength(array);
        Object newArray;
        if(oldLength == newLength && offset == 0) {
            newArray = array;
        }
        else {
            newArray = Array.newInstance(c.getComponentType(), newLength);
            System.arraycopy(array,offset,newArray,0,Math.min(oldLength - offset, newLength));
        }
        return newArray;
    }

    /**
     * Resize array while keeping existing elements
     *
     * @param array       Array to be resized
     * @param newLength   New length of array
     * @return            New array of different size
     */
    public static Object resizeArray(Object array, int newLength)
    {
        return resizeArray(array,0,newLength);
    }

    /**
     * Get subset of array
     *
     * @param array       Input array
     * @param beginOffset Begin offset (inclusive)
     * @param endOffset   End offset (exclusive)
     * @return            Sub array
     */
    public static Object subArray(Object array, int beginOffset, int endOffset)
    {
        if(array != null && array.getClass().isArray()) {
            int n = Array.getLength(array);
            if(beginOffset >= 0 && endOffset <= n && endOffset >= beginOffset) {
                return resizeArray(array,beginOffset, endOffset-beginOffset);
            }
            else {
                throw new IndexOutOfBoundsException();
            }
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public static Object joinArrays(Object[] arrays)
    {
        Object newArray = null;
        if(!isEmptyArray(arrays)) {
            Class<?> clasz = null;
            int count = 0;
            int[] lengths = new int[arrays.length];
            for (int i = 0; i < arrays.length; i++) {
                if(arrays[i] != null) {
                    Class<?> arrayClass = arrays[i].getClass();
                    if(arrayClass.isArray()) {
                        lengths[i] = Array.getLength(arrays[i]);
                        count += lengths[i];
                        if(clasz == null) {
                            clasz = arrayClass.getComponentType();
                        }
                        else {
                            Class<?> clasz2 = arrayClass.getComponentType();
                            if(!clasz2.equals(clasz)) {
                                if(clasz.isAssignableFrom(clasz2)) {
                                    continue;
                                }
                                else if(clasz2.isAssignableFrom(clasz)) {
                                    clasz = clasz2;
                                }
                                else {
                                    clasz = Object.class;
                                }
                            }
                        }
                    }
                    else {
                        throw new IllegalArgumentException();
                    }
                }
                else {
                    lengths[i] = 0;
                }
            }

            newArray = Array.newInstance(clasz, count);
            int n = 0;
            for (int i = 0; i < arrays.length; i++) {
                if(lengths[i] > 0) {
                    System.arraycopy(arrays[i],0,newArray,n,lengths[i]);
                    n += lengths[i];
                }
            }
        }
        return newArray;
    }

    /**
     * Append one array to another
     *
     * @param oldArray    The target array
     * @param newArray    The array to be appended
     * @return            Appended array
     */
    public static Object appendArray(Object oldArray, Object newArray)
    {
        return appendArray(oldArray, newArray, 0, Array.getLength(newArray));
    }

    /**
     * Append one array to another
     *
     * @param oldArray    The target array
     * @param newArray    The array to be appended
     * @return            Appended array
     */
    public static Object appendArray(Object oldArray, Object newArray, int startIndex, int length)
    {
        if (isEmptyArray(newArray))
        {
            return oldArray;
        }
        Class<?> newClass = newArray.getClass();
        if(!newClass.isArray()) {
            throw new IllegalArgumentException();
        }
        if(isEmptyArray(oldArray)) {
            return cloneArray(newArray);
        }
        Class<?> oldClass = oldArray.getClass();
        if(!oldClass.isArray()) {
            throw new IllegalArgumentException();
        }

        Object appendedArray = null;
        if(newClass.equals(oldClass) || oldClass.isAssignableFrom(newClass)) {
            int oldLength = Array.getLength(oldArray);
            int newLength = Array.getLength(newArray);
            int startIndex2 = Math.min(startIndex,newLength-1);
            newLength = Math.min(newLength,newLength-startIndex2);
            appendedArray = resizeArray(oldArray, oldLength+newLength);
            System.arraycopy(newArray,startIndex2,appendedArray,oldLength,newLength);
        }
        return appendedArray;
    }

    /**
     * Clone an array
     *
     * @param array       Array to be cloned
     * @return            Clone of the array
     */
    public static Object cloneArray(Object array)
    {

        Class<?> clasz = array.getClass();
        if(clasz.isArray()) {
            Object arrayClone = Array.newInstance(clasz.getComponentType(),
                            Array.getLength(array));
            System.arraycopy(array,0,arrayClone,0,Array.getLength(arrayClone));
            return arrayClone;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Check for an empty array
     *
     * @param array       Array to be tested
     * @return            True if the array is null or length is zero
     */
    public static boolean isEmptyArray(Object array)
    {
        if(array != null) {
            if(array.getClass().isArray()) {
                return Array.getLength(array) == 0;
            }
        }
        return true;
    }

    /**
     * Check for an empty collection
     *
     * @param collection       Collection to be tested
     * @return            True if the collection is null or size is zero
     */
    public static boolean isEmptyCollection(Collection<?> collection)
    {
        if(collection != null) {
            return collection.size() == 0;
        }
        return true;
    }

    /**
     * Check for an empty map
     *
     * @param map       Map to be tested
     * @return          True if the map is null or size is zero
     */
    public static boolean isEmptyMap(Map<?,?> map)
    {
        if(map != null) {
            return map.size() == 0;
        }
        return true;
    }

    public static boolean collectionContainsIgnoreCase(Collection<String> collection, String item)
    {
        for (Iterator<String> iter = collection.iterator(); iter.hasNext();) {
            String element = iter.next();
            if(stringsAreEqual(element,item,true)) {
                return true;
            }
        }
        return false;
    }

    public static String leftPad(String text, int length, char padChar)
    {
        String text2 = text;
        if(text2.length() < length) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for(int i=text2.length(); i<length; i++) {
                buf.append(padChar);
            }
            buf.append(text2);
            text2 = buf.toString();
        }
        return text2;
    }

    public static String flatten(Object[] array, char separator)
    {
        return flatten((Object)array, separator);
    }

    public static String flatten(Object array, char separator)
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(array != null && array.getClass().isArray()) {
            int length = Array.getLength(array);
            if(length > 0) {
                Object obj = Array.get(array, 0);
                buf.append(obj==null?"":obj); //$NON-NLS-1$
                for (int i = 1; i < length; i++) {
                    obj = Array.get(array, i);
                    buf.append(separator).append(obj==null?"":String.valueOf(obj)); //$NON-NLS-1$
                }
            }
        }
        return buf.toString();
    }

    public static String flatten(Collection<?> collection, char separator)
    {
        return flatten(collection.toArray(),separator);
    }

    public static String[] tokenize(String text, char separator)
    {
        return tokenize(text, separator, true);
    }

    public static String[] tokenize(String text, char separator, boolean trim)
    {
        return tokenizeToList(text,separator,trim).toArray(EMPTY_STRING_ARRAY);
    }

    public static List<String> tokenizeToList(String text, char separator)
    {
        return tokenizeToList(text, separator, true);
    }

    public static List<String> tokenizeToList(String text, char separator, boolean trim)
    {
        List<String> list = new ArrayList<String>();
        if(text != null && text.length() > 0) {
            char[] chars = text.toCharArray();
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for (int i = 0; i < chars.length; i++) {
                if(chars[i] != separator) {
                    buf.append(chars[i]);
                }
                else {
                    list.add(buf.toString());
                    buf.setLength(0);
                }
            }
            list.add(trim?buf.toString().trim():buf.toString());
        }
        return list;
    }

    public static String[] loadArrayProperty(ResourceBundle bundle, String propertyName)
    {
        return loadListProperty(bundle,propertyName).toArray(EMPTY_STRING_ARRAY);
    }

    public static String[] loadArrayProperty(ResourceBundle bundle, String propertyName, char separator)
    {
        return loadListProperty(bundle,propertyName, separator).toArray(EMPTY_STRING_ARRAY);
    }

    public static List<String> loadListProperty(ResourceBundle bundle, String propertyName)
    {
        return loadListProperty(bundle, propertyName, ',');
    }

    public static List<String> loadListProperty(ResourceBundle bundle, String propertyName, char separator)
    {
        String property = null;
        if(bundle != null) {
            try {
                property = bundle.getString(propertyName);
            }
            catch(MissingResourceException mre) {
                property = null;
            }
        }
        return tokenizeToList(property, separator);
    }

    public static Map<String, String> loadMapProperty(ResourceBundle bundle, String propertyName)
    {
        return loadMapProperty(bundle,propertyName,',');
    }

    public static Map<String, String> loadMapProperty(ResourceBundle bundle, String propertyName, char separator)
    {
        Map<String, String> map = new LinkedHashMap<String, String>();
        List<String> list = loadListProperty(bundle, propertyName, separator);
        for(Iterator<String> iter=list.iterator(); iter.hasNext(); ) {
            String pair = iter.next();
            int n=pair.indexOf("="); //$NON-NLS-1$
            if(n > 0) {
                String key = pair.substring(0,n).trim();
                String value = null;
                if(n < pair.length() - 1) {
                    value = pair.substring(n+1).trim();
                }
                map.put(key, value);
            }
        }
        return map;
    }

    public static String replaceAll(String input, String search, String replace, boolean ignoreCase)
    {
        String input2 = input;
        String search2 = search;
        if(!Common.isEmpty(input2) && !Common.isEmpty(search2) &&
                        search2.length() <= input2.length()) {
            String replace2 = replace == null?"":replace; //$NON-NLS-1$
            String tmp;
            if(ignoreCase) {
                search2 = search2.toLowerCase();
                tmp = input2.toLowerCase();
            }
            else {
                tmp = input2;
            }
            int n = tmp.indexOf(search2);
            if (n >= 0) {
                int start = 0;
                StringBuffer buf = new StringBuffer();
                while(n >= 0) {
                    buf.append(input2.substring(start,n));
                    buf.append(replace2);
                    start = n+search2.length();
                    if(start <= tmp.length()-search2.length()) {
                        n = tmp.indexOf(search2,start);
                    }
                    else {
                        break;
                    }
                }
                if(start < tmp.length()) {
                    buf.append(input2.substring(start));
                }
                input2 = buf.toString();
                if(ignoreCase) {
                    tmp = input2.toLowerCase();
                }
                else {
                    tmp = input2;
                }
            }
        }

        return input2;
    }

    public static void beanToStore(Object bean, IPreferenceStore store, java.util.List<String> properties)
    {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pd = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < pd.length; i++) {
                String name = pd[i].getName();
                if(properties.contains(name)) {
                    Class<?> clasz = pd[i].getPropertyType();
                    Method m = pd[i].getReadMethod();
                    try {
                        if(clasz.equals(Boolean.class) || clasz.equals(Boolean.TYPE)) {
                            Boolean b = (Boolean)m.invoke(bean,(Object[])null);
                            store.setValue(name,(b==null?false:b.booleanValue()));
                        }
                        else if(clasz.equals(Integer.class) || clasz.equals(Integer.TYPE)) {
                            Integer n = (Integer)m.invoke(bean,(Object[])null);
                            store.setValue(name,(n==null?0:n.intValue()));
                        }
                        else if(clasz.equals(Long.class) || clasz.equals(Long.TYPE)) {
                            Long l = (Long)m.invoke(bean,(Object[])null);
                            store.setValue(name,(l==null?0:l.longValue()));
                        }
                        else if(clasz.equals(Double.class) || clasz.equals(Double.TYPE)) {
                            Double d = (Double)m.invoke(bean,(Object[])null);
                            store.setValue(name,(d==null?0:d.doubleValue()));
                        }
                        else if(clasz.equals(Float.class) || clasz.equals(Float.TYPE)) {
                            Float f = (Float)m.invoke(bean,(Object[])null);
                            store.setValue(name,(f==null?0:f.floatValue()));
                        }
                        else if(clasz.equals(String.class)) {
                            String s = (String)m.invoke(bean,(Object[])null);
                            store.setValue(name,(s==null?"":s)); //$NON-NLS-1$
                        }
                    }
                    catch (Exception e1) {
                        EclipseNSISPlugin.getDefault().log(e1);
                    }
                }
            }
        }
        catch (IntrospectionException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    public static void storeToBean(Object bean, IPreferenceStore store, java.util.List<String> properties)
    {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pd = beanInfo.getPropertyDescriptors();
            Object[] args = new Object[1];

            for (int i = 0; i < pd.length; i++) {
                String name = pd[i].getName();
                if(properties.contains(name)) {
                    Class<?> clasz = pd[i].getPropertyType();
                    Method m = pd[i].getWriteMethod();
                    try {
                        if(clasz.equals(Boolean.class) || clasz.equals(Boolean.TYPE)) {
                            args[0] = Boolean.valueOf(store.getBoolean(name));
                        }
                        else if(clasz.equals(Integer.class) || clasz.equals(Integer.TYPE)) {
                            args[0] = new Integer(store.getInt(name));
                        }
                        else if(clasz.equals(Long.class) || clasz.equals(Long.TYPE)) {
                            args[0] = new Long(store.getLong(name));
                        }
                        else if(clasz.equals(Double.class) || clasz.equals(Double.TYPE)) {
                            args[0] = new Double(store.getDouble(name));
                        }
                        else if(clasz.equals(Float.class) || clasz.equals(Float.TYPE)) {
                            args[0] = new Float(store.getFloat(name));
                        }
                        else if(clasz.equals(String.class)) {
                            String value = store.getString(name);
                            args[0] = value==null?"":value; //$NON-NLS-1$
                        }
                        else {
                            continue;
                        }
                        m.invoke(bean, args);
                    }
                    catch (Exception e1) {
                        EclipseNSISPlugin.getDefault().log(e1);
                    }
                }
            }
        }
        catch (IntrospectionException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    public static String getClassSignature(Class<?> clasz)
    {
        if(clasz.isArray()) {
            return "["+getClassSignature(clasz.getComponentType()); //$NON-NLS-1$
        }
        else if(clasz.isPrimitive()) {
            if(Integer.TYPE.equals(clasz)) {
                return "I"; //$NON-NLS-1$
            }
            else if(Long.TYPE.equals(clasz)) {
                return "J"; //$NON-NLS-1$
            }
            else if(Void.TYPE.equals(clasz)) {
                return "V"; //$NON-NLS-1$
            }
            else if(Boolean.TYPE.equals(clasz)) {
                return "Z"; //$NON-NLS-1$
            }
            else if(Byte.TYPE.equals(clasz)) {
                return "B"; //$NON-NLS-1$
            }
            else if(Double.TYPE.equals(clasz)) {
                return "D"; //$NON-NLS-1$
            }
            else if(Float.TYPE.equals(clasz)) {
                return "F"; //$NON-NLS-1$
            }
            else if(Character.TYPE.equals(clasz)) {
                return "C"; //$NON-NLS-1$
            }
            else if(Short.TYPE.equals(clasz)) {
                return "S"; //$NON-NLS-1$
            }
            else {
                throw new IllegalArgumentException(clasz.getName());
            }
        }
        else {
            return new StringBuffer("L").append(clasz.getName().replace('.', '/')).append(";").toString(); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static Object getObjectFieldValue(Object object, String fieldName, Class<?> fieldType)
    {
        Object value;
        try {
            value = WinAPI.INSTANCE.getObjectFieldValue(object, fieldName, getClassSignature(fieldType));
        }
        catch (Throwable t) {
            value = null;
        }
        return value;
    }

    public static boolean objectsAreEqual(Object obj1, Object obj2)
    {
        if(obj1 != obj2) {
            return obj1 == null?false:obj1.equals(obj2);
        }
        return true;
    }

    public static boolean stringsAreEqual(String str1, String str2)
    {
        return stringsAreEqual(str1, str2, false);
    }

    public static boolean stringsAreEqual(String str1, String str2, boolean ignoreCase)
    {
        return str1 == null?str2 == null:ignoreCase?str1.equalsIgnoreCase(str2):str1.equals(str2);
    }

    public static String maybeQuote(String text)
    {
        String text2 = text;
        if(shouldQuote(text2)) {
            text2 = quote(text2);
        }
        return text2;
    }

    public static boolean shouldQuote(String text)
    {
        boolean shouldQuote = false;
        if(text != null) {
            shouldQuote = text.length()==0;
            for(int i=0; i<text.length(); i++) {
                if(Character.isWhitespace(text.charAt(i))) {
                    shouldQuote = true;
                }
            }
        }
        return shouldQuote;
    }

    public static boolean isQuoted(String text)
    {
        return isQuoted(text, '"');
    }

    public static boolean isQuoted(String text, char quote)
    {
        if(text != null && text.length() >= 2) {
            return text.charAt(0)==quote && text.charAt(text.length()-1) == quote;
        }
        return false;
    }

    public static String maybeUnquote(String text)
    {
        String text2 = text;
        if(isQuoted(text2)) {
            text2 = text2.substring(1,text2.length()-1);
        }
        return text2;
    }

    public static String quote(String text)
    {
        return new StringBuffer("\"").append(text).append("\"").toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String[] formatLines(String text, int maxLength)
    {
        List<String> lines = new ArrayList<String>();
        BreakIterator boundary = BreakIterator.getLineInstance();
        boundary.setText(text);
        int start = boundary.first();
        int end = boundary.next();
        int lineLength = 0;

        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        while (end != BreakIterator.DONE) {
            String word = text.substring(start,end);
            lineLength = lineLength + word.length();
            if (lineLength >= maxLength) {
                lines.add(buf.toString());
                buf.setLength(0);
                lineLength = word.length();
            }
            buf.append(word);
            start = end;
            end = boundary.next();
        }
        if(buf.length() > 0) {
            lines.add(buf.toString());
        }
        return lines.toArray(EMPTY_STRING_ARRAY);
    }

    public static String generateUniqueName(String prefix, String suffix)
    {
        StringBuffer name = new StringBuffer(""); //$NON-NLS-1$
        if(!Common.isEmpty(prefix)) {
            name.append(prefix);
        }
        name.append(new VMID().toString().replaceAll("[:-]","")); //$NON-NLS-1$ //$NON-NLS-2$
        if(!Common.isEmpty(suffix)) {
            name.append(suffix);
        }
        return name.toString();
    }

    public static void openError(Shell shell, String message, Image icon)
    {
        openError(shell, EclipseNSISPlugin.getResourceString("error.title"), message, icon); //$NON-NLS-1$
    }

    public static void openWarning(Shell shell, String message, Image icon)
    {
        openWarning(shell, EclipseNSISPlugin.getResourceString("warning.title"), message, icon); //$NON-NLS-1$
    }

    public static boolean openConfirm(Shell shell, String message, Image icon)
    {
        return openConfirm(shell,EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                        message, icon);
    }

    public static boolean openQuestion(Shell shell, String message, Image icon)
    {
        return openQuestion(shell,EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                        message, icon);
    }

    public static Point calculateControlSize(Control control, int chars, int lines)
    {
        Point pt = new Point(0,0);
        GC gc = new GC(control);
        FontMetrics fontMetrics = gc.getFontMetrics();
        if(chars > 0) {
            pt.x = chars*fontMetrics.getAverageCharWidth();
        }
        if(lines > 0) {
            pt.y = lines*fontMetrics.getHeight();
        }
        gc.dispose();
        return pt;
    }

    public static int parseInt(String str, int defaultVal)
    {
        int result;
        try {
            result = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe) {
            result = defaultVal;
        }
        return result;
    }

    public static String padString(String str, int length)
    {
        String str2 = str;
        if(str2 != null) {
            if(str2.length() < length) {
                char[] c = new char[length-str2.length()];
                Arrays.fill(c,' ');
                str2 += new String(c);
            }
        }
        return str2;
    }

    public static void printBundleExtensions(Bundle bundle)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtension[] extensions = registry.getExtensions(bundle.getSymbolicName());
        for (int i = 0; i < extensions.length; i++) {
            System.out.print(extensions[i].getExtensionPointUniqueIdentifier()+" label="); //$NON-NLS-1$
            System.out.print(extensions[i].getLabel()+" simpleId="); //$NON-NLS-1$
            System.out.print(extensions[i].getSimpleIdentifier()+" uniqueId="); //$NON-NLS-1$
            System.out.println(extensions[i].getUniqueIdentifier());
            printChildren("",extensions[i].getConfigurationElements()); //$NON-NLS-1$
        }
    }

    private static void printChildren(String prefix, IConfigurationElement[] elements) {
        if(!Common.isEmptyArray(elements)) {
            String prefix2 = prefix + "\t"; //$NON-NLS-1$
            for (int j = 0; j < elements.length; j++) {
                String[] attr = elements[j].getAttributeNames();
                if(!Common.isEmptyArray(attr)) {
                    for (int i = 0; i < attr.length; i++) {
                        System.out.println(prefix2+attr[i]+"="+elements[j].getAttribute(attr[i])); //$NON-NLS-1$
                    }
                }
                printChildren(prefix2,elements[j].getChildren());
            }
        }
    }

    public static <T> List<T> makeList(T[] array)
    {
        List<T> list = new ArrayList<T>();
        if(!isEmptyArray(array)) {
            for (int i = 0; i < array.length; i++) {
                list.add(array[i]);
            }
        }
        return list;
    }

    public static boolean openConfirm(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                        message, MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0);
        return dialog.open() == 0;
    }

    public static void openError(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                        message, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.open();
    }

    public static void openInformation(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                        message, MessageDialog.INFORMATION,
                        new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.open();
    }

    public static boolean openQuestion(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                        message, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0);
        return dialog.open() == 0;
    }

    public static void openWarning(Shell parent, String title, String message, Image icon)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                        message, MessageDialog.WARNING, new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.open();
    }

    public static int openMessageDialog(Shell parent, String title, String message, Image icon, int dialogImageType, String[] buttons, int defaultIndex)
    {
        MessageDialog dialog = new MessageDialog(parent, title, icon,
                        message, dialogImageType, buttons, defaultIndex);
        return dialog.open();
    }

    public static String trim(String str)
    {
        if(str != null) {
            return str.trim();
        }
        return null;
    }

    public static String escapeQuotes(String text)
    {
        return escapeQuotes(text, "\"'`", "$\\"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String escapeQuotes(String text, String quoteChars, String escape)
    {
        if(!isEmpty(text)) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if(quoteChars.indexOf(chars[i]) != -1)
                {
                    buf.append(escape);
                }
                buf.append(chars[i]);
            }
            return buf.toString();
        }
        return text;
    }

    public static void openExternalBrowser(String url)
    {
        try {
            PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
        }
        catch (Throwable t) {
            EclipseNSISPlugin.getDefault().log(t);
        }
    }

    public static boolean isValid(Control ctl)
    {
        return ctl != null && !ctl.isDisposed();
    }

    public static boolean isTrue(Boolean b)
    {
        return b != null && b.booleanValue();
    }

    public static final String getMyComputerLabel()
    {
        String name = null;
        try {
            name = WinAPI.INSTANCE.regQueryStrValue(HKEY.HKEY_CLASSES_ROOT.getHandle(),
                            "CLSID\\{20D04FE0-3AEA-1069-A2D8-08002B30309D}","LocalizedString"); //$NON-NLS-1$ //$NON-NLS-2$
            if(isEmpty(name)) {
                name = WinAPI.INSTANCE.regQueryStrValue(HKEY.HKEY_CLASSES_ROOT.getHandle(),
                                "CLSID\\{20D04FE0-3AEA-1069-A2D8-08002B30309D}",""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if(!isEmpty(name)) {
                if(name.charAt(0)=='@') {
                    name = name.substring(1);
                    String[] pieces = tokenize(name,',');
                    int lcid = 0;
                    String defaultName=""; //$NON-NLS-1$
                    String library;
                    int id = -1;

                    switch(pieces.length) {
                        case 3:
                            defaultName = pieces[2];
                            //$FALL-THROUGH$
                        case 2:
                            String[] ids = tokenize(pieces[1],'@');
                            switch(ids.length) {
                                case 2:
                                    lcid = parseInt(ids[1],0);
                                    //$FALL-THROUGH$
                                case 1:
                                    id = Math.abs(parseInt(ids[0],-1));
                                    //$FALL-THROUGH$
                                default:
                                    break;
                            }
                            //$FALL-THROUGH$
                        default:
                            library = pieces[0];
                            break;
                    }

                    if(id >= 0) {
                        String resourceString;
                        try {
                            resourceString = WinAPI.INSTANCE.loadResourceString(library, id,lcid);
                        }
                        catch(Exception ex) {
                            resourceString = null;
                        }
                        if(!isEmpty(resourceString)) {
                            name = resourceString;
                        }
                        else if(!isEmpty(defaultName)) {
                            name = defaultName;
                        }
                    }
                }
            }
        }
        catch(Exception ex) {
            name = null;
        }
        finally {
            if(isEmpty(name)) {
                name = EclipseNSISPlugin.getResourceString("my.computer.label"); //$NON-NLS-1$
            }
        }
        return name;
    }

    public static String toString(Object object, String defaultValue)
    {
        if(object == null) {
            return defaultValue;
        }
        else {
            return object.toString();
        }
    }

    public static boolean isWrappedPrimitive(Object obj)
    {
        if(obj != null) {
            Class<?> clasz = obj.getClass();
            return Byte.class.equals(clasz) ||
            Short.class.equals(clasz) ||
            Integer.class.equals(clasz) ||
            Long.class.equals(clasz) ||
            Character.class.equals(clasz) ||
            Float.class.equals(clasz) ||
            Double.class.equals(clasz) ||
            Boolean.class.equals(clasz);
        }
        return false;
    }

    public static Object createDefaultObject(String className)
    {
        try {
            Class<?> clasz = Class.forName(className);
            return createDefaultObject(clasz);
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            return null;
        }
    }

    public static <T> T createDefaultObject(Class<T> clasz)
    {
        try {
            Constructor<T> constructor = clasz.getConstructor((Class[])null);
            return constructor.newInstance((Object[])null);
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            return null;
        }
    }

    public static <T> List<T> makeGenericList(Class<T> clasz, List<?> list)
    {
        List<T> newList = new ArrayList<T>();
        for(Object o : list)
        {
            if(o != null)
            {
                if(clasz.isAssignableFrom(o.getClass()))
                {
                    newList.add(clasz.cast(o));
                }
                continue;
            }
            newList.add(null);
        }
        return newList;
    }

    public static IHandle getControlHandle(Control control)
    {
        return getHandle(control);
    }

    public static IHandle getGraphicsHandle(GC gc)
    {
        return getHandle(gc);
    }

    private static <T> IHandle getHandle(T object)
    {
        IHandle handle = WinAPI.ZERO_HANDLE;
        if (object != null)
        {
            try
            {
                Class<?> clasz = object.getClass();
                Field f = clasz.getField("handle");
                handle = WinAPI.INSTANCE.createHandle(((Number) f.get(object)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return handle;
    }
}
