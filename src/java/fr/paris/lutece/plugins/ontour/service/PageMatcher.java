/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.ontour.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.ontour.business.Tour;

/**
 * Tells whether a tour applies to a page.
 * <ul>
 * <li>the page path of the tour is compared to the path of the page, relative to the webapp (<code>jsp/admin/plugins/blog/ManageBlogs.jsp</code>).
 * <code>*</code> matches any sequence of characters. An empty page path never matches: the tour can only be started explicitly.</li>
 * <li>the page parameters of the tour (<code>view=createBlog&amp;id</code>) must all be present in the query string of the page. A parameter without
 * value, or with the <code>*</code> value, only needs to be present. Alternatives are separated by <code>|</code> and <code>!</code> stands for an
 * absent parameter: <code>view=manageBlogs|!</code> matches the default view and the explicit one. <code>!name</code> requires the parameter to be
 * absent.</li>
 * </ul>
 */
public final class PageMatcher
{
    private static final String WILDCARD = "*";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String VALUE_SEPARATOR = "=";
    private static final String PATH_SEPARATOR = "/";
    private static final String SESSION_ID_PREFIX = ";jsessionid=";
    private static final String NEGATION = "!";
    private static final String ALTERNATIVE_SEPARATOR = "|";

    /**
     * Private constructor
     */
    private PageMatcher( )
    {
    }

    /**
     * Tell whether a tour applies to a page
     *
     * @param tour
     *            the tour
     * @param strPath
     *            the path of the page, relative to the webapp
     * @param strQueryString
     *            the query string of the page, may be null
     * @return true if the tour applies to the page
     */
    public static boolean matches( Tour tour, String strPath, String strQueryString )
    {
        return matchesPath( tour.getPagePath( ), strPath ) && matchesParameters( tour.getPageParameters( ), strQueryString );
    }

    /**
     * Tell whether a path matches a path pattern
     *
     * @param strPattern
     *            the pattern, <code>*</code> wildcard allowed
     * @param strPath
     *            the path of the page
     * @return true if the path matches
     */
    public static boolean matchesPath( String strPattern, String strPath )
    {
        if ( StringUtils.isBlank( strPattern ) )
        {
            return false;
        }

        String strNormalizedPattern = normalizePath( strPattern );
        String strNormalizedPath = normalizePath( strPath );

        return toRegex( strNormalizedPattern ).matcher( strNormalizedPath ).matches( );
    }

    /**
     * Tell whether a query string contains all the required parameters
     *
     * @param strRequiredParameters
     *            the required parameters, as a query string
     * @param strQueryString
     *            the query string of the page
     * @return true if every required parameter is present with the expected value
     */
    public static boolean matchesParameters( String strRequiredParameters, String strQueryString )
    {
        if ( StringUtils.isBlank( strRequiredParameters ) )
        {
            return true;
        }

        Map<String, List<String>> mapActual = parseQueryString( strQueryString );

        for ( Map.Entry<String, List<String>> required : parseQueryString( strRequiredParameters ).entrySet( ) )
        {
            String strName = required.getKey( );

            if ( strName.startsWith( NEGATION ) )
            {
                if ( mapActual.containsKey( strName.substring( 1 ) ) )
                {
                    return false;
                }

                continue;
            }

            List<String> listActualValues = mapActual.get( strName );

            for ( String strExpected : required.getValue( ) )
            {
                if ( !matchesValue( strExpected, listActualValues ) )
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Tell whether the values of a parameter match an expected value. The expected value may list alternatives separated by <code>|</code>;
     * <code>!</code> stands for an absent parameter, an empty value or <code>*</code> for any value.
     *
     * @param strExpected
     *            the expected value
     * @param listActualValues
     *            the values of the parameter in the page URL, null when the parameter is absent
     * @return true if one of the alternatives matches
     */
    private static boolean matchesValue( String strExpected, List<String> listActualValues )
    {
        for ( String strAlternative : strExpected.split( Pattern.quote( ALTERNATIVE_SEPARATOR ), -1 ) )
        {
            String strValue = strAlternative.trim( );

            if ( NEGATION.equals( strValue ) )
            {
                if ( listActualValues == null )
                {
                    return true;
                }
            }
            else
                if ( listActualValues != null && ( strValue.isEmpty( ) || WILDCARD.equals( strValue ) || listActualValues.contains( strValue ) ) )
                {
                    return true;
                }
        }

        return false;
    }

    /**
     * Parse a query string
     *
     * @param strQueryString
     *            the query string, with or without its leading <code>?</code>
     * @return the parameter values by name
     */
    public static Map<String, List<String>> parseQueryString( String strQueryString )
    {
        Map<String, List<String>> mapParameters = new HashMap<>( );

        if ( StringUtils.isBlank( strQueryString ) )
        {
            return mapParameters;
        }

        for ( String strPair : StringUtils.removeStart( strQueryString.trim( ), "?" ).split( PARAMETER_SEPARATOR ) )
        {
            if ( StringUtils.isBlank( strPair ) )
            {
                continue;
            }

            String strName = decode( StringUtils.substringBefore( strPair, VALUE_SEPARATOR ).trim( ) );
            String strValue = strPair.contains( VALUE_SEPARATOR ) ? decode( StringUtils.substringAfter( strPair, VALUE_SEPARATOR ).trim( ) ) : "";
            mapParameters.computeIfAbsent( strName, k -> new ArrayList<>( ) ).add( strValue );
        }

        return mapParameters;
    }

    /**
     * Remove the leading slashes and the session identifier of a path
     *
     * @param strPath
     *            the path
     * @return the normalized path
     */
    private static String normalizePath( String strPath )
    {
        String strNormalized = StringUtils.defaultString( strPath ).trim( );
        strNormalized = StringUtils.substringBefore( strNormalized, "?" );
        strNormalized = StringUtils.substringBefore( strNormalized, SESSION_ID_PREFIX );

        while ( strNormalized.startsWith( PATH_SEPARATOR ) )
        {
            strNormalized = strNormalized.substring( 1 );
        }

        return strNormalized;
    }

    /**
     * Convert a wildcard pattern into a regular expression
     *
     * @param strPattern
     *            the wildcard pattern
     * @return the compiled regular expression
     */
    private static Pattern toRegex( String strPattern )
    {
        StringBuilder sbRegex = new StringBuilder( );

        for ( String strPart : strPattern.split( Pattern.quote( WILDCARD ), -1 ) )
        {
            if ( sbRegex.length( ) > 0 )
            {
                sbRegex.append( ".*" );
            }

            sbRegex.append( Pattern.quote( strPart ) );
        }

        return Pattern.compile( sbRegex.toString( ) );
    }

    /**
     * URL-decode a value
     *
     * @param strValue
     *            the value
     * @return the decoded value, or the raw value when it is malformed
     */
    private static String decode( String strValue )
    {
        try
        {
            return URLDecoder.decode( strValue, StandardCharsets.UTF_8 );
        }
        catch( IllegalArgumentException e )
        {
            return strValue;
        }
    }
}
