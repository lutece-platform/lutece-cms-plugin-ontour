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

import java.util.List;

import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Position of the floating tour launcher, one setting for the back office and one for the front office. The settings are site properties
 * (datastore keys under {@link OnTourSitePropertiesGroup#PREFIX}), edited by the administrators in the site properties screen.
 */
@ApplicationScoped
public class LauncherPositionService
{
    public static final String POSITION_BOTTOM_RIGHT = "bottom_right";
    public static final String POSITION_BOTTOM_LEFT = "bottom_left";
    public static final String POSITION_BOTTOM_CENTER = "bottom_center";
    public static final String POSITION_TOP_CENTER = "top_center";
    public static final String POSITION_TOP_RIGHT = "top_right";
    public static final String POSITION_TOP_LEFT = "top_left";
    public static final String POSITION_HIDDEN = "hidden";
    public static final List<String> POSITIONS = List.of( POSITION_BOTTOM_RIGHT, POSITION_BOTTOM_CENTER, POSITION_BOTTOM_LEFT, POSITION_TOP_RIGHT,
            POSITION_TOP_CENTER, POSITION_TOP_LEFT, POSITION_HIDDEN );

    public static final String KEY_POSITION_BO = OnTourSitePropertiesGroup.PREFIX + "launcher.bo.select";
    public static final String KEY_POSITION_FO = OnTourSitePropertiesGroup.PREFIX + "launcher.fo.select";

    private static final String SUFFIX_OPTIONS = ".options";
    private static final String OPTIONS_SEPARATOR = "|";

    /**
     * Returns the launcher position of a target. An unknown or missing value falls back on {@link #POSITION_BOTTOM_RIGHT}.
     *
     * @param strTarget
     *            the target (BO or FO)
     * @return the position, one of {@link #POSITIONS}
     */
    public String getPosition( String strTarget )
    {
        return normalize( DatastoreService.getDataValue( getKey( strTarget ), POSITION_BOTTOM_RIGHT ) );
    }

    /**
     * Create the launcher settings that do not exist yet, with their list of options. The values chosen by the administrators are never
     * changed; the lists of options are technical keys, refreshed at each startup so that new positions become available.
     */
    public void initSettings( )
    {
        String strOptions = String.join( OPTIONS_SEPARATOR, POSITIONS );

        for ( String strKey : List.of( KEY_POSITION_BO, KEY_POSITION_FO ) )
        {
            DatastoreService.insertDataValueIfAbsent( strKey, POSITION_BOTTOM_RIGHT );
            DatastoreService.setDataValue( strKey + SUFFIX_OPTIONS, strOptions );
        }
    }

    /**
     * Returns the datastore key of the launcher position of a target
     *
     * @param strTarget
     *            the target (BO or FO)
     * @return the datastore key
     */
    public static String getKey( String strTarget )
    {
        return Tour.TARGET_BO.equals( strTarget ) ? KEY_POSITION_BO : KEY_POSITION_FO;
    }

    /**
     * Map a stored value on a known position
     *
     * @param strValue
     *            the stored value
     * @return the position, {@link #POSITION_BOTTOM_RIGHT} when the value is unknown
     */
    public static String normalize( String strValue )
    {
        String strPosition = ( strValue == null ) ? "" : strValue.trim( ).toLowerCase( java.util.Locale.ROOT ).replace( '-', '_' );

        return POSITIONS.contains( strPosition ) ? strPosition : POSITION_BOTTOM_RIGHT;
    }
}
