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

import java.util.Map;
import java.util.Optional;

import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.plugins.ontour.business.TourUserStateHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpServletRequest;

/**
 * State of the tours per user account, so that a finished or closed tour is not offered again automatically, whatever the browser or the
 * session. The user is the connected administrator for the back office tours, the connected site user (MyLutece) for the front office ones;
 * anonymous visitors have no account state, the browser storage is used instead.
 */
@ApplicationScoped
public class TourUserStateService
{
    /**
     * Identify the user of a request for a target
     *
     * @param request
     *            the request
     * @param strTarget
     *            the target (BO or FO)
     * @return the user identifier, empty for an anonymous visitor
     */
    public Optional<String> getUserId( HttpServletRequest request, String strTarget )
    {
        if ( Tour.TARGET_BO.equals( strTarget ) )
        {
            AdminUser user = AdminUserService.getAdminUser( request );

            return ( user != null ) ? Optional.of( String.valueOf( user.getUserId( ) ) ) : Optional.empty( );
        }

        if ( !SecurityService.isAuthenticationEnable( ) )
        {
            return Optional.empty( );
        }

        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( request );

        return ( user != null ) ? Optional.ofNullable( user.getName( ) ) : Optional.empty( );
    }

    /**
     * Find the states of the tours of the user of a request
     *
     * @param request
     *            the request
     * @param strTarget
     *            the target (BO or FO)
     * @return the status by tour code, empty for an anonymous visitor
     */
    public Map<String, String> getStates( HttpServletRequest request, String strTarget )
    {
        return getUserId( request, strTarget ).map( strUserId -> TourUserStateHome.findByUser( strTarget, strUserId ) ).orElseGet( Map::of );
    }

    /**
     * Record that the user of a request finished or closed a tour
     *
     * @param request
     *            the request
     * @param strTarget
     *            the target (BO or FO)
     * @param strTourCode
     *            the tour code
     * @param bDone
     *            true when the last step was reached, false when the tour was closed before
     * @return true if the state was recorded, false for an anonymous visitor
     */
    public boolean record( HttpServletRequest request, String strTarget, String strTourCode, boolean bDone )
    {
        Optional<String> userId = getUserId( request, strTarget );
        userId.ifPresent( strUserId -> TourUserStateHome.store( strTarget, strUserId, strTourCode,
                bDone ? TourUserStateHome.STATUS_DONE : TourUserStateHome.STATUS_CLOSED ) );

        return userId.isPresent( );
    }

    /**
     * Forget the states of a tour, for every user: the tour is offered again as a first visit
     *
     * @param strTourCode
     *            the tour code
     */
    public void reset( String strTourCode )
    {
        TourUserStateHome.removeByTour( strTourCode );
    }

    /**
     * Count the users by status for every tour
     *
     * @return the number of users by status, by tour code
     */
    public Map<String, Map<String, Integer>> countByTour( )
    {
        return TourUserStateHome.countByTour( );
    }
}
