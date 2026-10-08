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

/**
 * Data access interface for the state of the tours per user: finished or closed
 */
public interface ITourUserStateDAO
{
    /**
     * Store the state of a tour for a user, replacing the previous one
     *
     * @param strUserType
     *            the user type (BO or FO)
     * @param strUserId
     *            the user identifier
     * @param strTourCode
     *            the tour code
     * @param strStatus
     *            the status
     * @param plugin
     *            the plugin
     */
    void store( String strUserType, String strUserId, String strTourCode, String strStatus, Plugin plugin );

    /**
     * Load the states of the tours of a user
     *
     * @param strUserType
     *            the user type (BO or FO)
     * @param strUserId
     *            the user identifier
     * @param plugin
     *            the plugin
     * @return the status by tour code
     */
    Map<String, String> selectByUser( String strUserType, String strUserId, Plugin plugin );

    /**
     * Count the users by status for every tour
     *
     * @param plugin
     *            the plugin
     * @return the number of users by status, by tour code
     */
    Map<String, Map<String, Integer>> countByTour( Plugin plugin );

    /**
     * Delete the states of a tour, for every user
     *
     * @param strTourCode
     *            the tour code
     * @param plugin
     *            the plugin
     */
    void deleteByTour( String strTourCode, Plugin plugin );
}
