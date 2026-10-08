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
 * Static facade for {@link Step} persistence
 */
public final class StepHome
{
    private static IStepDAO _dao = CDI.current( ).select( IStepDAO.class ).get( );
    private static Plugin _plugin = PluginService.getPlugin( TourHome.PLUGIN_NAME );

    /**
     * Private constructor
     */
    private StepHome( )
    {
    }

    /**
     * Create a step
     *
     * @param step
     *            the step
     * @return the step with its identifier
     */
    public static Step create( Step step )
    {
        _dao.insert( step, _plugin );

        return step;
    }

    /**
     * Update a step
     *
     * @param step
     *            the step
     * @return the step
     */
    public static Step update( Step step )
    {
        _dao.store( step, _plugin );

        return step;
    }

    /**
     * Update the order of a step
     *
     * @param nIdStep
     *            the step identifier
     * @param nOrder
     *            the new order
     */
    public static void updateOrder( int nIdStep, int nOrder )
    {
        _dao.storeOrder( nIdStep, nOrder, _plugin );
    }

    /**
     * Remove a step
     *
     * @param nId
     *            the step identifier
     */
    public static void remove( int nId )
    {
        _dao.delete( nId, _plugin );
    }

    /**
     * Find a step
     *
     * @param nId
     *            the step identifier
     * @return the step if found
     */
    public static Optional<Step> findByPrimaryKey( int nId )
    {
        return _dao.load( nId, _plugin );
    }

    /**
     * Find the steps of a tour, ordered
     *
     * @param nIdTour
     *            the tour identifier
     * @return the steps
     */
    public static List<Step> findByTour( int nIdTour )
    {
        return _dao.selectByTour( nIdTour, _plugin );
    }

    /**
     * Return the highest order of the steps of a tour
     *
     * @param nIdTour
     *            the tour identifier
     * @return the highest order, 0 when the tour has no step
     */
    public static int findMaxOrder( int nIdTour )
    {
        return _dao.selectMaxOrder( nIdTour, _plugin );
    }
}
