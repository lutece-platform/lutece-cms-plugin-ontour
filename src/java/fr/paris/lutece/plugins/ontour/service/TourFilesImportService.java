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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.plugins.ontour.business.TourHome;
import fr.paris.lutece.plugins.resource.loader.ResourceNotFoundException;
import fr.paris.lutece.portal.service.init.PostStartUpService;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Imports at startup the tour files shipped by the plugins of the webapp.
 * <p>
 * Any plugin can provide its own tours without a compile-time dependency on onTour: it only has to ship JSON files (onTour export format) in
 * <code>/WEB-INF/plugins/ontour/tours/</code>. A tour whose code already exists is never overwritten, so the changes made by the administrators
 * are kept across restarts.
 * </p>
 */
@ApplicationScoped
public class TourFilesImportService implements PostStartUpService
{

    private static final String PROPERTY_TOURS_DIRECTORY = "ontour.tours.directory";
    private static final String PROPERTY_IMPORT_ENABLED = "ontour.tours.importAtStartup";
    private static final String DEFAULT_TOURS_DIRECTORY = "/WEB-INF/plugins/ontour/tours/";
    private static final String JSON_EXTENSION = ".json";

    @Inject
    private TourService _tourService;

    @Inject
    private TourJsonService _tourJsonService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName( )
    {
        return "onTour tour files import";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process( )
    {
        if ( !PluginService.isPluginEnable( TourHome.PLUGIN_NAME ) || !AppPropertiesService.getPropertyBoolean( PROPERTY_IMPORT_ENABLED, true ) )
        {
            return;
        }

        String strDirectory = AppPropertiesService.getProperty( PROPERTY_TOURS_DIRECTORY, DEFAULT_TOURS_DIRECTORY );
        Set<URL> setFiles;

        try
        {
            setFiles = AppPathService.getResourceURLFromRelativePath( strDirectory );
        }
        catch( ResourceNotFoundException e )
        {
            AppLogService.debug( "onTour: no tour file directory {}", strDirectory, e );

            return;
        }

        setFiles.stream( ).filter( url -> url.getPath( ).endsWith( JSON_EXTENSION ) ).sorted( Comparator.comparing( URL::getPath ) ).forEach( this::importFile );
    }

    /**
     * Import one tour file, without overwriting the existing tours
     *
     * @param url
     *            the URL of the JSON file
     */
    private void importFile( URL url )
    {
        try ( InputStream inputStream = url.openStream( ) )
        {
            List<Tour> listTours = _tourJsonService.parseTours( new String( inputStream.readAllBytes( ), StandardCharsets.UTF_8 ) );
            TourService.ImportResult result = _tourService.importTours( listTours, false );
            AppLogService.info( "onTour: {} imported ({} created, {} already present)", url.getPath( ), result.nCreated( ), result.nSkipped( ) );
        }
        catch( IOException | TourImportException e )
        {
            AppLogService.error( "onTour: unable to import the tour file {}", url, e );
        }
    }
}
