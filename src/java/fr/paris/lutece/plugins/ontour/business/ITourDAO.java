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
 * Data access interface for {@link Tour}
 */
public interface ITourDAO
{
    /**
     * Insert a new tour
     *
     * @param tour
     *            the tour, its identifier is set after insertion
     * @param plugin
     *            the plugin
     */
    void insert( Tour tour, Plugin plugin );

    /**
     * Update a tour
     *
     * @param tour
     *            the tour
     * @param plugin
     *            the plugin
     */
    void store( Tour tour, Plugin plugin );

    /**
     * Delete a tour
     *
     * @param nId
     *            the tour identifier
     * @param plugin
     *            the plugin
     */
    void delete( int nId, Plugin plugin );

    /**
     * Load a tour, without its steps
     *
     * @param nId
     *            the tour identifier
     * @param plugin
     *            the plugin
     * @return the tour if found
     */
    Optional<Tour> load( int nId, Plugin plugin );

    /**
     * Load the translation of a tour in a language, without its steps
     *
     * @param strCode
     *            the tour code
     * @param strLang
     *            the language, empty for the tour shown whatever the language
     * @param plugin
     *            the plugin
     * @return the tour if found
     */
    Optional<Tour> loadByCodeAndLang( String strCode, String strLang, Plugin plugin );

    /**
     * Load all the translations of a tour, without their steps
     *
     * @param strCode
     *            the tour code
     * @param plugin
     *            the plugin
     * @return the tours sharing this code, ordered by language
     */
    List<Tour> selectByCode( String strCode, Plugin plugin );

    /**
     * Load all the tours, ordered by target then title
     *
     * @param plugin
     *            the plugin
     * @return the tours
     */
    List<Tour> selectAll( Plugin plugin );

    /**
     * Load the enabled tours of a target
     *
     * @param strTarget
     *            the target (BO or FO)
     * @param plugin
     *            the plugin
     * @return the enabled tours
     */
    List<Tour> selectEnabledByTarget( String strTarget, Plugin plugin );
}
