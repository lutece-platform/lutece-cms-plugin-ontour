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

import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.rbac.RBACHome;
import fr.paris.lutece.portal.business.rbac.RBACRole;
import fr.paris.lutece.portal.business.rbac.RBACRoleHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.user.AdminUserHome;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.init.PostStartUpService;
import fr.paris.lutece.portal.service.util.AppLogService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * One-time RBAC initialization of an existing installation: creates the <code>ontour_manager</code> role granting every permission on every
 * tour, and gives it to the administrators who already have the onTour right, so that nobody loses access when the permissions are
 * enforced. Runs only once (datastore flag), so the choices made later by the administrators are never overridden.
 */
@ApplicationScoped
public class TourRbacInitService implements PostStartUpService
{
    public static final String ROLE_MANAGER = "ontour_manager";

    private static final String ROLE_DESCRIPTION = "Guided tours administrator (onTour)";
    private static final String RIGHT_MANAGE_TOURS = "ONTOUR_MANAGEMENT";
    private static final String KEY_INITIALIZED = "ontour.rbac.initialized";
    private static final String WILDCARD = "*";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName( )
    {
        return "onTour RBAC initialization";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process( )
    {
        if ( DatastoreService.existsKey( KEY_INITIALIZED ) )
        {
            return;
        }

        if ( !RBACRoleHome.checkExistRole( ROLE_MANAGER ) )
        {
            RBACRoleHome.create( new RBACRole( ROLE_MANAGER, ROLE_DESCRIPTION ) );
        }

        boolean bGranted = RBACHome.findResourcesByCode( ROLE_MANAGER ).stream( ).anyMatch( rbac -> Tour.RESOURCE_TYPE.equals( rbac.getResourceTypeKey( ) ) );

        if ( !bGranted )
        {
            RBAC rbac = new RBAC( );
            rbac.setRoleKey( ROLE_MANAGER );
            rbac.setResourceTypeKey( Tour.RESOURCE_TYPE );
            rbac.setResourceId( WILDCARD );
            rbac.setPermissionKey( WILDCARD );
            RBACHome.create( rbac );
        }

        int nUsers = 0;

        for ( AdminUser user : AdminUserHome.findByRight( RIGHT_MANAGE_TOURS ) )
        {
            if ( !AdminUserHome.hasRole( user, ROLE_MANAGER ) )
            {
                AdminUserHome.createRoleForUser( user.getUserId( ), ROLE_MANAGER );
                nUsers++;
            }
        }

        DatastoreService.setDataValue( KEY_INITIALIZED, "true" );
        AppLogService.info( "onTour: RBAC role {} initialized, given to {} administrator(s)", ROLE_MANAGER, nUsers );
    }
}
