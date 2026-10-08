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

import java.util.List;
import java.util.Optional;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Static facade for {@link Tour} persistence
 */
public final class TourHome
{
    public static final String PLUGIN_NAME = "ontour";

    private static ITourDAO _dao = CDI.current( ).select( ITourDAO.class ).get( );
    private static IStepDAO _daoStep = CDI.current( ).select( IStepDAO.class ).get( );
    private static Plugin _plugin = PluginService.getPlugin( PLUGIN_NAME );

    /**
     * Private constructor
     */
    private TourHome( )
    {
    }

    /**
     * Create a tour, without its steps
     *
     * @param tour
     *            the tour
     * @return the tour with its identifier
     */
    public static Tour create( Tour tour )
    {
        _dao.insert( tour, _plugin );

        return tour;
    }

    /**
     * Update a tour, without its steps
     *
     * @param tour
     *            the tour
     * @return the tour
     */
    public static Tour update( Tour tour )
    {
        _dao.store( tour, _plugin );

        return tour;
    }

    /**
     * Remove a tour and all its steps
     *
     * @param nId
     *            the tour identifier
     */
    public static void remove( int nId )
    {
        _daoStep.deleteByTour( nId, _plugin );
        _dao.delete( nId, _plugin );
    }

    /**
     * Find a tour, without its steps
     *
     * @param nId
     *            the tour identifier
     * @return the tour if found
     */
    public static Optional<Tour> findByPrimaryKey( int nId )
    {
        return _dao.load( nId, _plugin );
    }

    /**
     * Find the translation of a tour in a language, without its steps
     *
     * @param strCode
     *            the tour code
     * @param strLang
     *            the language, empty for the tour shown whatever the language
     * @return the tour if found
     */
    public static Optional<Tour> findByCodeAndLang( String strCode, String strLang )
    {
        return _dao.loadByCodeAndLang( strCode, strLang, _plugin );
    }

    /**
     * Find all the translations of a tour, without their steps
     *
     * @param strCode
     *            the tour code
     * @return the tours sharing this code
     */
    public static List<Tour> findByCode( String strCode )
    {
        return _dao.selectByCode( strCode, _plugin );
    }

    /**
     * Find all the tours, without their steps
     *
     * @return the tours
     */
    public static List<Tour> findAll( )
    {
        return _dao.selectAll( _plugin );
    }

    /**
     * Find the enabled tours of a target, without their steps
     *
     * @param strTarget
     *            the target (BO or FO)
     * @return the enabled tours
     */
    public static List<Tour> findEnabledByTarget( String strTarget )
    {
        return _dao.selectEnabledByTarget( strTarget, _plugin );
    }
}
