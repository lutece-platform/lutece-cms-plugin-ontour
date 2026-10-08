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
package fr.paris.lutece.plugins.ontour.business;

import java.util.Map;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Static facade for the state of the tours per user
 */
public final class TourUserStateHome
{
    public static final String STATUS_DONE = "done";
    public static final String STATUS_CLOSED = "closed";

    private static ITourUserStateDAO _dao = CDI.current( ).select( ITourUserStateDAO.class ).get( );
    private static Plugin _plugin = PluginService.getPlugin( TourHome.PLUGIN_NAME );

    /**
     * Private constructor
     */
    private TourUserStateHome( )
    {
    }

    /**
     * Store the state of a tour for a user
     *
     * @param strUserType
     *            the user type (BO or FO)
     * @param strUserId
     *            the user identifier
     * @param strTourCode
     *            the tour code
     * @param strStatus
     *            {@link #STATUS_DONE} or {@link #STATUS_CLOSED}
     */
    public static void store( String strUserType, String strUserId, String strTourCode, String strStatus )
    {
        _dao.store( strUserType, strUserId, strTourCode, strStatus, _plugin );
    }

    /**
     * Find the states of the tours of a user
     *
     * @param strUserType
     *            the user type (BO or FO)
     * @param strUserId
     *            the user identifier
     * @return the status by tour code
     */
    public static Map<String, String> findByUser( String strUserType, String strUserId )
    {
        return _dao.selectByUser( strUserType, strUserId, _plugin );
    }

    /**
     * Count the users by status for every tour
     *
     * @return the number of users by status, by tour code
     */
    public static Map<String, Map<String, Integer>> countByTour( )
    {
        return _dao.countByTour( _plugin );
    }

    /**
     * Remove the states of a tour for every user
     *
     * @param strTourCode
     *            the tour code
     */
    public static void removeByTour( String strTourCode )
    {
        _dao.deleteByTour( strTourCode, _plugin );
    }
}
