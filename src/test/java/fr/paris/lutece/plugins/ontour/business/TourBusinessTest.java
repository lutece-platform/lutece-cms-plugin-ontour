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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.ontour.service.TourResourceIdService;
import fr.paris.lutece.plugins.ontour.service.TourService;
import fr.paris.lutece.test.LuteceTestCase;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Persistence tests of tours and steps
 */
class TourBusinessTest extends LuteceTestCase
{
    private TourService _tourService;

    /**
     * Resolve the service under test
     */
    @BeforeEach
    void setUpService( )
    {
        _tourService = CDI.current( ).select( TourService.class ).get( );
    }

    /**
     * Build a step
     *
     * @param strTitle
     *            the title
     * @return the step
     */
    private static Step buildStep( String strTitle )
    {
        Step step = new Step( );
        step.setTitle( strTitle );
        step.setElement( "#" + strTitle );

        return step;
    }

    /**
     * Tour CRUD, step ordering and page matching
     */
    @Test
    void testBusiness( )
    {
        Tour tour = Tour.createWithDefaults( );
        tour.setCode( "junit-tour" );
        tour.setTitle( "JUnit tour" );
        tour.setTarget( Tour.TARGET_FO );
        tour.setPagePath( "jsp/site/Portal.jsp" );
        tour.setPageParameters( "page=junit" );
        tour.setEnabled( true );
        tour.setOverlayOpacity( 40 );
        tour.setSteps( List.of( buildStep( "a" ), buildStep( "b" ), buildStep( "c" ) ) );

        _tourService.create( tour );
        assertTrue( tour.getId( ) > 0 );

        Tour stored = _tourService.findByCode( "junit-tour", Tour.LANG_ALL ).orElseThrow( );
        assertEquals( "JUnit tour", stored.getTitle( ) );
        assertEquals( 40, stored.getOverlayOpacity( ) );
        assertTrue( stored.isAllowClose( ) );
        assertEquals( 3, stored.getSteps( ).size( ) );
        assertEquals( "a", stored.getSteps( ).get( 0 ).getTitle( ) );

        stored.setTitle( "JUnit tour 2" );
        _tourService.update( stored );
        assertEquals( "JUnit tour 2", _tourService.findById( tour.getId( ) ).orElseThrow( ).getTitle( ) );
        assertTrue( _tourService.isCodeUsed( "junit-tour", Tour.LANG_ALL, 0 ) );
        assertFalse( _tourService.isCodeUsed( "junit-tour", Tour.LANG_ALL, tour.getId( ) ) );

        int nIdStepC = stored.getSteps( ).get( 2 ).getId( );
        _tourService.moveStep( nIdStepC, true );
        assertEquals( List.of( "a", "c", "b" ), titles( tour.getId( ) ) );

        _tourService.removeStep( stored.getSteps( ).get( 0 ).getId( ) );
        assertEquals( List.of( "c", "b" ), titles( tour.getId( ) ) );
        assertEquals( 1, _tourService.findStep( nIdStepC ).orElseThrow( ).getOrder( ) );

        Step added = buildStep( "d" );
        added.setIdTour( tour.getId( ) );
        _tourService.createStep( added );
        assertEquals( 3, added.getOrder( ) );

        assertTrue( _tourService.findMatchingTours( Tour.TARGET_FO, "jsp/site/Portal.jsp", "page=junit", "fr" ).stream( )
                .anyMatch( t -> "junit-tour".equals( t.getCode( ) ) ) );
        assertFalse( _tourService.findMatchingTours( Tour.TARGET_FO, "jsp/site/Portal.jsp", "page=other", "fr" ).stream( )
                .anyMatch( t -> "junit-tour".equals( t.getCode( ) ) ) );
        assertFalse( _tourService.findMatchingTours( Tour.TARGET_BO, "jsp/site/Portal.jsp", "page=junit", "fr" ).stream( )
                .anyMatch( t -> "junit-tour".equals( t.getCode( ) ) ) );
        assertTrue( _tourService.findEnabledByCode( Tour.TARGET_FO, "junit-tour", "fr" ).isPresent( ) );

        _tourService.remove( tour.getId( ) );
        assertTrue( _tourService.findById( tour.getId( ) ).isEmpty( ) );
        assertTrue( StepHome.findByTour( tour.getId( ) ).isEmpty( ) );
    }

    /**
     * Import without and with overwrite
     */
    @Test
    void testImport( )
    {
        Tour tour = Tour.createWithDefaults( );
        tour.setCode( "junit-import" );
        tour.setTitle( "Import" );
        tour.setSteps( List.of( buildStep( "x" ) ) );

        assertEquals( 1, _tourService.importTours( List.of( tour ), false ).nCreated( ) );

        Tour again = Tour.createWithDefaults( );
        again.setCode( "junit-import" );
        again.setTitle( "Import 2" );
        again.setSteps( List.of( buildStep( "y" ), buildStep( "z" ) ) );

        assertEquals( 1, _tourService.importTours( List.of( again ), false ).nSkipped( ) );
        assertEquals( "Import", _tourService.findByCode( "junit-import", Tour.LANG_ALL ).orElseThrow( ).getTitle( ) );

        assertEquals( 1, _tourService.importTours( List.of( again ), true ).nUpdated( ) );
        Tour replaced = _tourService.findByCode( "junit-import", Tour.LANG_ALL ).orElseThrow( );
        assertEquals( "Import 2", replaced.getTitle( ) );
        assertEquals( 2, replaced.getSteps( ).size( ) );

        _tourService.remove( replaced.getId( ) );
    }

    /**
     * Translations: one tour per language, the best one is served, translations are created disabled
     */
    @Test
    void testTranslations( )
    {
        Tour french = Tour.createWithDefaults( );
        french.setCode( "junit-lang" );
        french.setLang( "fr" );
        french.setTitle( "Visite" );
        french.setTarget( Tour.TARGET_BO );
        french.setPagePath( "jsp/admin/Junit.jsp" );
        french.setEnabled( true );
        french.setSteps( List.of( buildStep( "etape" ) ) );
        _tourService.create( french );

        assertTrue( _tourService.isCodeUsed( "junit-lang", "fr", 0 ) );
        assertFalse( _tourService.isCodeUsed( "junit-lang", "en", 0 ) );

        Tour english = _tourService.createTranslation( french.getId( ), "en" ).orElseThrow( );
        assertFalse( english.isEnabled( ) );
        assertEquals( 1, _tourService.findById( english.getId( ) ).orElseThrow( ).getSteps( ).size( ) );
        assertTrue( _tourService.createTranslation( french.getId( ), "en" ).isEmpty( ) );

        english.setTitle( "Tour" );
        english.setEnabled( true );
        _tourService.update( english );

        assertEquals( "Tour", _tourService.findMatchingTours( Tour.TARGET_BO, "jsp/admin/Junit.jsp", "", "en" ).get( 0 ).getTitle( ) );
        assertEquals( "Visite", _tourService.findMatchingTours( Tour.TARGET_BO, "jsp/admin/Junit.jsp", "", "fr" ).get( 0 ).getTitle( ) );
        assertEquals( 1, _tourService.findMatchingTours( Tour.TARGET_BO, "jsp/admin/Junit.jsp", "", "de" ).size( ) );
        assertEquals( "Tour", _tourService.findEnabledByCode( Tour.TARGET_BO, "junit-lang", "en" ).orElseThrow( ).getTitle( ) );
        assertEquals( 2, _tourService.findTranslations( "junit-lang" ).size( ) );

        _tourService.remove( french.getId( ) );
        _tourService.remove( english.getId( ) );
    }

    /**
     * States of the tours per user: recorded, replaced, counted, reset, and removed with the last translation of the tour
     */
    @Test
    void testUserStates( )
    {
        Tour tour = Tour.createWithDefaults( );
        tour.setCode( "junit-state" );
        tour.setTitle( "State" );
        tour.setSteps( List.of( buildStep( "s" ) ) );
        _tourService.create( tour );

        TourUserStateHome.store( Tour.TARGET_BO, "1", "junit-state", TourUserStateHome.STATUS_CLOSED );
        TourUserStateHome.store( Tour.TARGET_BO, "1", "junit-state", TourUserStateHome.STATUS_DONE );
        TourUserStateHome.store( Tour.TARGET_BO, "2", "junit-state", TourUserStateHome.STATUS_CLOSED );
        TourUserStateHome.store( Tour.TARGET_FO, "1", "junit-state", TourUserStateHome.STATUS_CLOSED );

        assertEquals( TourUserStateHome.STATUS_DONE, TourUserStateHome.findByUser( Tour.TARGET_BO, "1" ).get( "junit-state" ) );
        assertEquals( TourUserStateHome.STATUS_CLOSED, TourUserStateHome.findByUser( Tour.TARGET_FO, "1" ).get( "junit-state" ) );
        assertTrue( TourUserStateHome.findByUser( Tour.TARGET_BO, "3" ).isEmpty( ) );

        Map<String, Integer> counts = TourUserStateHome.countByTour( ).get( "junit-state" );
        assertEquals( 1, counts.get( TourUserStateHome.STATUS_DONE ) );
        assertEquals( 2, counts.get( TourUserStateHome.STATUS_CLOSED ) );

        TourUserStateHome.removeByTour( "junit-state" );
        assertTrue( TourUserStateHome.findByUser( Tour.TARGET_BO, "1" ).isEmpty( ) );

        TourUserStateHome.store( Tour.TARGET_BO, "1", "junit-state", TourUserStateHome.STATUS_DONE );
        _tourService.remove( tour.getId( ) );
        assertTrue( TourUserStateHome.findByUser( Tour.TARGET_BO, "1" ).isEmpty( ) );
    }

    /**
     * RBAC resources: one per tour code, whatever the number of translations
     */
    @Test
    void testResourceIdService( )
    {
        Tour french = Tour.createWithDefaults( );
        french.setCode( "junit-rbac" );
        french.setLang( "fr" );
        french.setTitle( "Visite RBAC" );
        _tourService.create( french );
        Tour english = _tourService.createTranslation( french.getId( ), "en" ).orElseThrow( );

        assertEquals( Tour.RESOURCE_TYPE, french.getResourceTypeCode( ) );
        assertEquals( "junit-rbac", english.getResourceId( ) );

        TourResourceIdService service = new TourResourceIdService( );
        assertEquals( 1, service.getResourceIdList( java.util.Locale.FRENCH ).stream( ).filter( item -> "junit-rbac".equals( item.getCode( ) ) ).count( ) );
        assertEquals( "Visite RBAC", service.getTitle( "junit-rbac", java.util.Locale.FRENCH ) );
        assertEquals( "unknown", service.getTitle( "unknown", java.util.Locale.FRENCH ) );

        _tourService.remove( french.getId( ) );
        _tourService.remove( english.getId( ) );
    }

    /**
     * Selection of the translation fitting a language
     */
    @Test
    void testSelectTranslation( )
    {
        Tour all = new Tour( );
        all.setLang( Tour.LANG_ALL );
        Tour english = new Tour( );
        english.setLang( "en" );

        assertEquals( english, TourService.selectTranslation( List.of( all, english ), "en" ).orElseThrow( ) );
        assertEquals( all, TourService.selectTranslation( List.of( all, english ), "de" ).orElseThrow( ) );
        assertEquals( english, TourService.selectTranslation( List.of( english ), "de" ).orElseThrow( ) );
        assertTrue( TourService.selectTranslation( List.of( ), "fr" ).isEmpty( ) );
    }

    /**
     * Titles of the steps of a tour, in order
     *
     * @param nIdTour
     *            the tour identifier
     * @return the titles
     */
    private static List<String> titles( int nIdTour )
    {
        return StepHome.findByTour( nIdTour ).stream( ).map( Step::getTitle ).toList( );
    }
}
