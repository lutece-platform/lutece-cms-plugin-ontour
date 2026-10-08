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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Look of the guided tours: values of the CSS custom properties of <code>ontour.css</code>, one set for the back office and one for the front
 * office, edited by the administrators in the site properties (keys <code>ontour.site_property.style.bo.*</code> and
 * <code>ontour.site_property.style.fo.*</code>). Values are checked before being sent to the pages; an empty or invalid value leaves the
 * default of the stylesheet.
 */
@ApplicationScoped
public class TourStyleService
{
    public static final String SETTING_COLOR = "color";
    public static final String SETTING_COLOR_CONTRAST = "color_contrast";
    public static final String SETTING_OFFSET = "offset";
    public static final String SETTING_Z_INDEX = "z_index";

    public static final String VAR_COLOR = "--ontour-color";
    public static final String VAR_COLOR_CONTRAST = "--ontour-color-contrast";
    public static final String VAR_OFFSET = "--ontour-offset";
    public static final String VAR_Z_INDEX = "--ontour-z-index";

    private static final String PREFIX_STYLE = OnTourSitePropertiesGroup.PREFIX + "style.";
    private static final String SUFFIX_BO = "bo.";
    private static final String SUFFIX_FO = "fo.";

    private static final Pattern PATTERN_COLOR = Pattern.compile(
            "#[0-9a-fA-F]{3,8}|(rgb|rgba|hsl|hsla)\\(\\s*[0-9.%,\\s/+-]+\\)|[a-zA-Z]{3,30}" );
    private static final Pattern PATTERN_LENGTH = Pattern.compile( "0|[0-9]{1,4}(\\.[0-9]{1,3})?(px|rem|em|%|vh|vw)" );
    private static final Pattern PATTERN_Z_INDEX = Pattern.compile( "-?[0-9]{1,9}" );

    private static final List<String> SETTINGS = List.of( SETTING_COLOR, SETTING_COLOR_CONTRAST, SETTING_OFFSET, SETTING_Z_INDEX );
    private static final Map<String, String> DEFAULTS = Map.of( SETTING_COLOR, "#1f5fbf", SETTING_COLOR_CONTRAST, "#ffffff", SETTING_OFFSET,
            "1.25rem", SETTING_Z_INDEX, "1040" );
    private static final Map<String, String> VARIABLES = Map.of( SETTING_COLOR, VAR_COLOR, SETTING_COLOR_CONTRAST, VAR_COLOR_CONTRAST,
            SETTING_OFFSET, VAR_OFFSET, SETTING_Z_INDEX, VAR_Z_INDEX );
    private static final Map<String, Pattern> PATTERNS = Map.of( SETTING_COLOR, PATTERN_COLOR, SETTING_COLOR_CONTRAST, PATTERN_COLOR,
            SETTING_OFFSET, PATTERN_LENGTH, SETTING_Z_INDEX, PATTERN_Z_INDEX );

    /**
     * Returns the CSS custom properties to apply on the pages of a target, the valid settings only
     *
     * @param strTarget
     *            the target (BO or FO)
     * @return the values by CSS custom property name
     */
    public Map<String, String> getCssVariables( String strTarget )
    {
        Map<String, String> mapVariables = new LinkedHashMap<>( );

        for ( String strSetting : SETTINGS )
        {
            String strValue = DatastoreService.getDataValue( getKey( strTarget, strSetting ), DEFAULTS.get( strSetting ) );

            if ( isValid( strSetting, strValue ) )
            {
                mapVariables.put( VARIABLES.get( strSetting ), strValue.trim( ) );
            }
        }

        return mapVariables;
    }

    /**
     * Create the style settings that do not exist yet. A value of the former shared setting (<code>ontour.site_property.style.color</code>...)
     * is copied to the back office and front office settings, then the shared setting is removed. Existing values are never changed.
     */
    public void initSettings( )
    {
        for ( String strSetting : SETTINGS )
        {
            String strLegacyKey = PREFIX_STYLE + strSetting;
            String strValue = DatastoreService.existsKey( strLegacyKey ) ? DatastoreService.getDataValue( strLegacyKey, DEFAULTS.get( strSetting ) )
                    : DEFAULTS.get( strSetting );

            DatastoreService.insertDataValueIfAbsent( getKey( Tour.TARGET_BO, strSetting ), strValue );
            DatastoreService.insertDataValueIfAbsent( getKey( Tour.TARGET_FO, strSetting ), strValue );

            if ( DatastoreService.existsKey( strLegacyKey ) )
            {
                DatastoreService.removeData( strLegacyKey );
            }
        }
    }

    /**
     * Returns the datastore key of a style setting for a target
     *
     * @param strTarget
     *            the target (BO or FO)
     * @param strSetting
     *            the setting (color, color_contrast, offset, z_index)
     * @return the datastore key
     */
    public static String getKey( String strTarget, String strSetting )
    {
        return PREFIX_STYLE + ( Tour.TARGET_BO.equals( strTarget ) ? SUFFIX_BO : SUFFIX_FO ) + strSetting;
    }

    /**
     * Tell whether a value is accepted for a style setting
     *
     * @param strSetting
     *            the setting (color, color_contrast, offset, z_index)
     * @param strValue
     *            the value
     * @return true if the value can be applied
     */
    public static boolean isValid( String strSetting, String strValue )
    {
        Pattern pattern = PATTERNS.get( strSetting );

        return pattern != null && strValue != null && pattern.matcher( strValue.trim( ) ).matches( );
    }
}
