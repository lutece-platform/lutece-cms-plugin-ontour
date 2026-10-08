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

/**
 * Data access interface for {@link Step}
 */
public interface IStepDAO
{
    /**
     * Insert a new step
     *
     * @param step
     *            the step, its identifier is set after insertion
     * @param plugin
     *            the plugin
     */
    void insert( Step step, Plugin plugin );

    /**
     * Update a step
     *
     * @param step
     *            the step
     * @param plugin
     *            the plugin
     */
    void store( Step step, Plugin plugin );

    /**
     * Update the order of a step
     *
     * @param nIdStep
     *            the step identifier
     * @param nOrder
     *            the new order
     * @param plugin
     *            the plugin
     */
    void storeOrder( int nIdStep, int nOrder, Plugin plugin );

    /**
     * Delete a step
     *
     * @param nId
     *            the step identifier
     * @param plugin
     *            the plugin
     */
    void delete( int nId, Plugin plugin );

    /**
     * Delete all the steps of a tour
     *
     * @param nIdTour
     *            the tour identifier
     * @param plugin
     *            the plugin
     */
    void deleteByTour( int nIdTour, Plugin plugin );

    /**
     * Load a step
     *
     * @param nId
     *            the step identifier
     * @param plugin
     *            the plugin
     * @return the step if found
     */
    Optional<Step> load( int nId, Plugin plugin );

    /**
     * Load the steps of a tour, ordered
     *
     * @param nIdTour
     *            the tour identifier
     * @param plugin
     *            the plugin
     * @return the steps
     */
    List<Step> selectByTour( int nIdTour, Plugin plugin );

    /**
     * Return the highest order of the steps of a tour
     *
     * @param nIdTour
     *            the tour identifier
     * @param plugin
     *            the plugin
     * @return the highest order, 0 when the tour has no step
     */
    int selectMaxOrder( int nIdTour, Plugin plugin );
}
