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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.ontour.business.Step;
import fr.paris.lutece.plugins.ontour.business.Tour;

/**
 * Tests of {@link TourJsonService}
 */
class TourJsonServiceTest
{
    private final TourJsonService _service = new TourJsonService( );

    /**
     * Build a tour with two steps
     *
     * @return the tour
     */
    private static Tour buildTour( )
    {
        Tour tour = Tour.createWithDefaults( );
        tour.setCode( "blog-manage" );
        tour.setTitle( "Blog" );
        tour.setTarget( Tour.TARGET_BO );
        tour.setPagePath( "jsp/admin/plugins/blog/ManageBlogs.jsp" );
        tour.setOverlayOpacity( 50 );
        tour.setShowButtons( "next,close" );

        Step intro = new Step( );
        intro.setTitle( "Welcome" );
        intro.setDescription( "<p>Hello</p>" );

        Step search = new Step( );
        search.setElement( "#search_text_a" );
        search.setTitle( "Search" );
        search.setSide( "bottom" );
        search.setShowProgress( Step.YES );
        search.setShowButtons( "previous" );
        search.setWaitForElement( 500 );

        tour.setSteps( List.of( intro, search ) );

        return tour;
    }

    /**
     * Driver.js configuration
     */
    @Test
    @SuppressWarnings( "unchecked" )
    void testToDriverConfig( )
    {
        Map<String, Object> config = _service.toDriverConfig( buildTour( ), Map.of( "nextBtnText", "Suivant" ) );

        assertEquals( 0.5, config.get( "overlayOpacity" ) );
        assertEquals( List.of( "next", "close" ), config.get( "showButtons" ) );
        assertEquals( "Suivant", config.get( "nextBtnText" ) );
        assertFalse( config.containsKey( "popoverClass" ) );

        List<Map<String, Object>> steps = (List<Map<String, Object>>) config.get( "steps" );
        assertEquals( 2, steps.size( ) );
        assertFalse( steps.get( 0 ).containsKey( "element" ) );
        assertFalse( steps.get( 0 ).containsKey( "waitForElement" ) );

        Map<String, Object> popover = (Map<String, Object>) steps.get( 1 ).get( "popover" );
        assertEquals( "#search_text_a", steps.get( 1 ).get( "element" ) );
        assertEquals( 500, steps.get( 1 ).get( "waitForElement" ) );
        assertEquals( "bottom", popover.get( "side" ) );
        assertEquals( Boolean.TRUE, popover.get( "showProgress" ) );
        assertEquals( List.of( "previous" ), popover.get( "showButtons" ) );
        assertFalse( popover.containsKey( "align" ) );
    }

    /**
     * Export then import gives back the same tour
     *
     * @throws Exception
     *             if the conversion fails
     */
    @Test
    void testExportImport( ) throws Exception
    {
        String strJson = _service.exportTours( List.of( buildTour( ) ) );
        assertFalse( strJson.contains( "\"idTour\"" ) );

        List<Tour> listTours = _service.parseTours( strJson );
        assertEquals( 1, listTours.size( ) );

        Tour tour = listTours.get( 0 );
        assertEquals( "blog-manage", tour.getCode( ) );
        assertEquals( 50, tour.getOverlayOpacity( ) );
        assertEquals( 2, tour.getSteps( ).size( ) );
        assertEquals( "#search_text_a", tour.getSteps( ).get( 1 ).getElement( ) );
        assertEquals( Step.YES, tour.getSteps( ).get( 1 ).getShowProgress( ) );
        assertNull( tour.getSteps( ).get( 0 ).getShowButtons( ) );
    }

    /**
     * Missing properties take the Driver.js defaults, unknown button names are dropped
     *
     * @throws Exception
     *             if the conversion fails
     */
    @Test
    void testImportDefaults( ) throws Exception
    {
        List<Tour> listTours = _service.parseTours( "[ { \"code\": \"t1\", \"title\": \"T1\", \"showButtons\": \"next,foo\", \"steps\": [ { \"title\": \"s\" } ] } ]" );

        Tour tour = listTours.get( 0 );
        assertTrue( tour.isAnimate( ) );
        assertTrue( tour.isAllowClose( ) );
        assertEquals( Tour.TARGET_BO, tour.getTarget( ) );
        assertEquals( "next", tour.getShowButtons( ) );
        assertEquals( Step.INHERIT, tour.getSteps( ).get( 0 ).getAdvanceOnClick( ) );
    }

    /**
     * Invalid documents are rejected
     */
    @Test
    void testImportErrors( )
    {
        assertThrows( TourImportException.class, ( ) -> _service.parseTours( "{ not json" ) );
        assertThrows( TourImportException.class, ( ) -> _service.parseTours( "{ \"tours\": [ { \"title\": \"no code\" } ] }" ) );
        assertThrows( TourImportException.class, ( ) -> _service.parseTours( "{ \"code\": \"bad code\", \"title\": \"t\" }" ) );
        assertThrows( TourImportException.class, ( ) -> _service.parseTours( "{ \"code\": \"c\", \"title\": \"t\", \"target\": \"XX\" }" ) );
        assertThrows( TourImportException.class, ( ) -> _service.parseTours( "42" ) );
        assertThrows( TourImportException.class, ( ) -> _service.parseTours( "{ \"code\": \"c\", \"title\": \"t\", \"lang\": \"french\" }" ) );
    }

    /**
     * The tour files shipped with the plugin are valid
     *
     * @throws Exception
     *             if a file cannot be read or parsed
     */
    @Test
    void testShippedTourFiles( ) throws Exception
    {
        for ( String strFile : List.of( "webapp/WEB-INF/plugins/ontour/tours/ontour.json", "src/site/resources/samples/blog.json", "src/site/resources/samples/core.json", "src/site/resources/samples/site-demo.json" ) )
        {
            List<Tour> listTours = _service.parseTours( java.nio.file.Files.readString( java.nio.file.Path.of( strFile ) ) );
            assertFalse( listTours.isEmpty( ), strFile );
            listTours.forEach( tour -> assertFalse( tour.getSteps( ).isEmpty( ), tour.getCode( ) ) );
        }
    }
}
