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

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.plugins.ontour.business.TourHome;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

/**
 * RBAC resource type of the guided tours. A resource is a tour <b>code</b>: the permissions apply to all the translations of the tour.
 */
public class TourResourceIdService extends ResourceIdService
{
    public static final String PERMISSION_VIEW = "VIEW";
    public static final String PERMISSION_CREATE = "CREATE";
    public static final String PERMISSION_MODIFY = "MODIFY";
    public static final String PERMISSION_DELETE = "DELETE";
    public static final String PERMISSION_MANAGE_STEPS = "MANAGE_STEPS";
    public static final String PERMISSION_TRANSLATE = "TRANSLATE";
    public static final String PERMISSION_EXPORT = "EXPORT";
    public static final String PERMISSION_RESET = "RESET";

    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "ontour.permission.resourceType.tour.label";
    private static final String PREFIX_LABEL_PERMISSION = "ontour.permission.resourceType.tour.";
    private static final String [ ] PERMISSIONS = {
            PERMISSION_VIEW, PERMISSION_CREATE, PERMISSION_MODIFY, PERMISSION_DELETE, PERMISSION_MANAGE_STEPS, PERMISSION_TRANSLATE,
            PERMISSION_EXPORT, PERMISSION_RESET
    };

    /**
     * Constructor
     */
    public TourResourceIdService( )
    {
        setPluginName( TourHome.PLUGIN_NAME );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register( )
    {
        ResourceType resourceType = new ResourceType( );
        resourceType.setResourceIdServiceClass( TourResourceIdService.class.getName( ) );
        resourceType.setPluginName( getPluginName( ) );
        resourceType.setResourceTypeKey( Tour.RESOURCE_TYPE );
        resourceType.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        for ( String strPermission : PERMISSIONS )
        {
            Permission permission = new Permission( );
            permission.setPermissionKey( strPermission );
            permission.setPermissionTitleKey( PREFIX_LABEL_PERMISSION + strPermission.toLowerCase( Locale.ROOT ) );
            resourceType.registerPermission( permission );
        }

        ResourceTypeManager.registerResourceType( resourceType );
    }

    /**
     * {@inheritDoc} One entry per tour code, labelled with the title of one of its translations.
     */
    @Override
    public ReferenceList getResourceIdList( Locale locale )
    {
        Map<String, String> mapTours = new TreeMap<>( );

        for ( Tour tour : TourHome.findAll( ) )
        {
            mapTours.putIfAbsent( tour.getCode( ), tour.getTitle( ) + " (" + tour.getCode( ) + ")" );
        }

        ReferenceList list = new ReferenceList( );
        mapTours.forEach( list::addItem );

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle( String strId, Locale locale )
    {
        return TourHome.findByCode( strId ).stream( ).findFirst( ).map( Tour::getTitle ).orElse( strId );
    }
}
