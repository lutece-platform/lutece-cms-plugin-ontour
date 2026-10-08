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
package fr.paris.lutece.plugins.ontour.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.plugins.ontour.business.TourHome;
import fr.paris.lutece.plugins.ontour.service.LauncherPositionService;
import fr.paris.lutece.plugins.ontour.service.TourJsonService;
import fr.paris.lutece.plugins.ontour.service.TourService;
import fr.paris.lutece.plugins.ontour.service.TourStyleService;
import fr.paris.lutece.plugins.ontour.service.TourUserStateService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JSON endpoint read by <code>ontour.js</code>: returns the Driver.js configuration of the tours that apply to a page, or of one tour by its code.
 * <p>
 * Parameters: <code>target</code> (BO or FO), <code>path</code> and <code>query</code> (page path relative to the webapp, and its query string), or
 * <code>code</code> (a tour code). A POST with <code>code</code> and <code>status</code> records that the user finished or closed a tour. <code>lang</code> forces the language of the tours, the language of the user is used otherwise. Back office tours are only served to an authenticated administrator.
 * </p>
 */
public class TourServlet extends HttpServlet
{
    public static final String URL_SERVLET = "servlet/plugins/ontour/tours";

    private static final long serialVersionUID = 1L;

    private static final String PARAMETER_TARGET = "target";
    private static final String PARAMETER_PATH = "path";
    private static final String PARAMETER_QUERY = "query";
    private static final String PARAMETER_CODE = "code";
    private static final String PARAMETER_LANG = "lang";
    private static final String LANG_PATTERN = "[a-z]{2,3}";

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String CACHE_CONTROL_NO_STORE = "no-store";

    private static final String KEY_TOURS = "tours";
    private static final String KEY_LABELS = "labels";
    private static final String KEY_CODE = "code";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TRIGGER = "trigger";
    private static final String KEY_LAUNCHER = "launcher";
    private static final String KEY_REQUIRES = "requires";
    private static final String KEY_SEEN = "seen";
    private static final String KEY_PERSISTENT = "persistent";
    private static final String KEY_LAUNCHER_POSITION = "launcherPosition";
    private static final String KEY_STYLE = "style";
    private static final String PARAMETER_STATUS = "status";
    private static final String STATUS_DONE = "done";
    private static final String STATUS_CLOSED = "closed";
    private static final String HEADER_ONTOUR = "X-OnTour";
    private static final String KEY_CONFIG = "config";

    private static final String PREFIX_I18N_LABELS = "ontour.js.";
    private static final String [ ] LABELS = {
            "launcher", "menuTitle", "close"
    };
    private static final String [ ] DRIVER_TEXTS = {
            "progressText", "nextBtnText", "prevBtnText", "doneBtnText"
    };

    private transient TourService _tourService;
    private transient TourJsonService _tourJsonService;
    private transient TourUserStateService _tourUserStateService;
    private transient LauncherPositionService _launcherPositionService;
    private transient TourStyleService _tourStyleService;

    /**
     * Returns the style service, resolved on first use (see {@link #getTourService( )})
     *
     * @return the style service
     */
    private TourStyleService getTourStyleService( )
    {
        if ( _tourStyleService == null )
        {
            _tourStyleService = CDI.current( ).select( TourStyleService.class ).get( );
        }

        return _tourStyleService;
    }

    /**
     * Returns the launcher position service, resolved on first use (see {@link #getTourService( )})
     *
     * @return the launcher position service
     */
    private LauncherPositionService getLauncherPositionService( )
    {
        if ( _launcherPositionService == null )
        {
            _launcherPositionService = CDI.current( ).select( LauncherPositionService.class ).get( );
        }

        return _launcherPositionService;
    }

    /**
     * Returns the user state service, resolved on first use (see {@link #getTourService( )})
     *
     * @return the user state service
     */
    private TourUserStateService getTourUserStateService( )
    {
        if ( _tourUserStateService == null )
        {
            _tourUserStateService = CDI.current( ).select( TourUserStateService.class ).get( );
        }

        return _tourUserStateService;
    }

    /**
     * Returns the tour service. Resolved on first use: the core only calls <code>init()</code> on the servlets of the plugins enabled at
     * startup, so a plugin enabled afterwards gets requests on a servlet that was never initialized.
     *
     * @return the tour service
     */
    private TourService getTourService( )
    {
        if ( _tourService == null )
        {
            _tourService = CDI.current( ).select( TourService.class ).get( );
        }

        return _tourService;
    }

    /**
     * Returns the JSON service, resolved on first use (see {@link #getTourService( )})
     *
     * @return the JSON service
     */
    private TourJsonService getTourJsonService( )
    {
        if ( _tourJsonService == null )
        {
            _tourJsonService = CDI.current( ).select( TourJsonService.class ).get( );
        }

        return _tourJsonService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        response.setHeader( HEADER_CACHE_CONTROL, CACHE_CONTROL_NO_STORE );

        if ( !PluginService.isPluginEnable( TourHome.PLUGIN_NAME ) )
        {
            response.sendError( HttpServletResponse.SC_NOT_FOUND );

            return;
        }

        String strTarget = Tour.TARGET_BO.equalsIgnoreCase( request.getParameter( PARAMETER_TARGET ) ) ? Tour.TARGET_BO : Tour.TARGET_FO;
        Locale locale;

        if ( Tour.TARGET_BO.equals( strTarget ) )
        {
            AdminUser user = AdminUserService.getAdminUser( request );

            if ( user == null )
            {
                response.sendError( HttpServletResponse.SC_FORBIDDEN );

                return;
            }

            locale = user.getLocale( );
        }
        else
        {
            locale = LocaleService.getContextUserLocale( request );
        }

        String strLang = request.getParameter( PARAMETER_LANG );

        if ( strLang == null || !strLang.matches( LANG_PATTERN ) )
        {
            strLang = locale.getLanguage( );
        }

        String strJson;

        try
        {
            strJson = getTourJsonService( ).toJson( buildPayload( request, strTarget, findTours( request, strTarget, strLang ), locale ) );
        }
        catch( IOException | RuntimeException e )
        {
            AppLogService.error( "onTour: unable to build the tours of the page", e );
            response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );

            return;
        }

        response.setContentType( CONTENT_TYPE_JSON );
        response.setCharacterEncoding( StandardCharsets.UTF_8.name( ) );
        response.getWriter( ).write( strJson );
    }

    /**
     * Find the requested tours
     *
     * @param request
     *            the request
     * @param strTarget
     *            the target
     * @param strLang
     *            the language of the user
     * @return the tours, with their steps
     */
    private List<Tour> findTours( HttpServletRequest request, String strTarget, String strLang )
    {
        String strCode = request.getParameter( PARAMETER_CODE );

        if ( StringUtils.isNotBlank( strCode ) )
        {
            return getTourService( ).findEnabledByCode( strTarget, strCode, strLang ).map( List::of ).orElseGet( List::of );
        }

        return getTourService( ).findMatchingTours( strTarget, request.getParameter( PARAMETER_PATH ), request.getParameter( PARAMETER_QUERY ), strLang );
    }

    /**
     * Record that the user finished (<code>status=done</code>) or closed (<code>status=closed</code>) a tour, so that it is not started
     * automatically again for this account. Answers 204 when recorded, 202 for an anonymous visitor (nothing to record server side).
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws IOException
     *             if the response cannot be written
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        response.setHeader( HEADER_CACHE_CONTROL, CACHE_CONTROL_NO_STORE );

        String strTarget = Tour.TARGET_BO.equalsIgnoreCase( request.getParameter( PARAMETER_TARGET ) ) ? Tour.TARGET_BO : Tour.TARGET_FO;
        String strCode = request.getParameter( PARAMETER_CODE );
        String strStatus = request.getParameter( PARAMETER_STATUS );

        if ( !PluginService.isPluginEnable( TourHome.PLUGIN_NAME ) || request.getHeader( HEADER_ONTOUR ) == null || StringUtils.isBlank( strCode )
                || !( STATUS_DONE.equals( strStatus ) || STATUS_CLOSED.equals( strStatus ) ) )
        {
            response.sendError( HttpServletResponse.SC_BAD_REQUEST );

            return;
        }

        if ( Tour.TARGET_BO.equals( strTarget ) && AdminUserService.getAdminUser( request ) == null )
        {
            response.sendError( HttpServletResponse.SC_FORBIDDEN );

            return;
        }

        try
        {
            if ( getTourService( ).findTranslations( strCode ).isEmpty( ) )
            {
                response.sendError( HttpServletResponse.SC_NOT_FOUND );

                return;
            }

            boolean bRecorded = getTourUserStateService( ).record( request, strTarget, strCode, STATUS_DONE.equals( strStatus ) );
            response.setStatus( bRecorded ? HttpServletResponse.SC_NO_CONTENT : HttpServletResponse.SC_ACCEPTED );
        }
        catch( RuntimeException e )
        {
            AppLogService.error( "onTour: unable to record the state of the tour {}", strCode, e );
            response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
    }

    /**
     * Build the JSON payload
     *
     * @param request
     *            the request
     * @param strTarget
     *            the target
     * @param listTours
     *            the tours
     * @param locale
     *            the locale of the labels
     * @return the payload
     */
    private Map<String, Object> buildPayload( HttpServletRequest request, String strTarget, List<Tour> listTours, Locale locale )
    {
        Map<String, String> mapStates = getTourUserStateService( ).getStates( request, strTarget );
        Map<String, String> mapDefaultTexts = localize( DRIVER_TEXTS, locale );
        List<Map<String, Object>> listJsonTours = new ArrayList<>( );

        for ( Tour tour : listTours )
        {
            Map<String, Object> jsonTour = new LinkedHashMap<>( );
            jsonTour.put( KEY_CODE, tour.getCode( ) );
            jsonTour.put( KEY_TITLE, tour.getTitle( ) );
            jsonTour.put( KEY_TRIGGER, tour.getTriggerMode( ) );
            jsonTour.put( KEY_LAUNCHER, tour.isShowLauncher( ) );
            jsonTour.put( KEY_REQUIRES, StringUtils.defaultString( tour.getPageSelector( ) ) );
            jsonTour.put( KEY_SEEN, mapStates.get( tour.getCode( ) ) );
            jsonTour.put( KEY_CONFIG, getTourJsonService( ).toDriverConfig( tour, mapDefaultTexts ) );
            listJsonTours.add( jsonTour );
        }

        Map<String, Object> payload = new LinkedHashMap<>( );
        payload.put( KEY_TOURS, listJsonTours );
        payload.put( KEY_LABELS, localize( LABELS, locale ) );
        payload.put( KEY_PERSISTENT, getTourUserStateService( ).getUserId( request, strTarget ).isPresent( ) );
        payload.put( KEY_LAUNCHER_POSITION, getLauncherPositionService( ).getPosition( strTarget ) );
        payload.put( KEY_STYLE, getTourStyleService( ).getCssVariables( strTarget ) );

        return payload;
    }

    /**
     * Localize a set of <code>ontour.js.*</code> keys
     *
     * @param keys
     *            the key suffixes
     * @param locale
     *            the locale
     * @return the localized texts by key suffix
     */
    private static Map<String, String> localize( String [ ] keys, Locale locale )
    {
        Map<String, String> mapTexts = new LinkedHashMap<>( );

        for ( String strKey : keys )
        {
            mapTexts.put( strKey, I18nService.getLocalizedString( PREFIX_I18N_LABELS + strKey, locale ) );
        }

        return mapTexts;
    }
}
