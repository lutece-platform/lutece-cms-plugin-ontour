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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.ontour.business.Step;
import fr.paris.lutece.plugins.ontour.business.StepHome;
import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.plugins.ontour.business.TourHome;
import fr.paris.lutece.plugins.ontour.business.TourUserStateHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Business service for tours and their steps
 */
@ApplicationScoped
public class TourService
{
    /**
     * Result of an import
     *
     * @param nCreated
     *            number of created tours
     * @param nUpdated
     *            number of replaced tours
     * @param nSkipped
     *            number of tours left untouched because their code already exists
     */
    public record ImportResult( int nCreated, int nUpdated, int nSkipped )
    {
    }

    /**
     * Find all the tours, with their steps
     *
     * @return the tours
     */
    public List<Tour> findAll( )
    {
        List<Tour> listTours = TourHome.findAll( );
        listTours.forEach( this::loadSteps );

        return listTours;
    }

    /**
     * Find a tour with its steps
     *
     * @param nIdTour
     *            the tour identifier
     * @return the tour if found
     */
    public Optional<Tour> findById( int nIdTour )
    {
        return TourHome.findByPrimaryKey( nIdTour ).map( this::loadSteps );
    }

    /**
     * Find the translation of a tour in a language, with its steps
     *
     * @param strCode
     *            the tour code
     * @param strLang
     *            the language, {@link Tour#LANG_ALL} for the tour shown whatever the language
     * @return the tour if found
     */
    public Optional<Tour> findByCode( String strCode, String strLang )
    {
        return TourHome.findByCodeAndLang( strCode, strLang ).map( this::loadSteps );
    }

    /**
     * Find the translations of a tour, without their steps
     *
     * @param strCode
     *            the tour code
     * @return the tours sharing this code
     */
    public List<Tour> findTranslations( String strCode )
    {
        return TourHome.findByCode( strCode );
    }

    /**
     * Find the enabled tour of a target with the given code, in the language that fits best, with its steps
     *
     * @param strTarget
     *            the target (BO or FO)
     * @param strCode
     *            the tour code
     * @param strLang
     *            the language of the user
     * @return the tour if found
     */
    public Optional<Tour> findEnabledByCode( String strTarget, String strCode, String strLang )
    {
        List<Tour> listVariants = TourHome.findByCode( strCode ).stream( ).filter( tour -> tour.isEnabled( ) && tour.getTarget( ).equals( strTarget ) )
                .toList( );

        return selectTranslation( listVariants, strLang ).map( this::loadSteps );
    }

    /**
     * Find the enabled tours of a target that apply to a page, with their steps. When a tour exists in several languages, only the translation
     * that fits the user language best is returned.
     *
     * @param strTarget
     *            the target (BO or FO)
     * @param strPath
     *            the path of the page, relative to the webapp
     * @param strQueryString
     *            the query string of the page
     * @param strLang
     *            the language of the user
     * @return the matching tours
     */
    public List<Tour> findMatchingTours( String strTarget, String strPath, String strQueryString, String strLang )
    {
        Map<String, List<Tour>> mapVariants = new LinkedHashMap<>( );

        for ( Tour tour : TourHome.findEnabledByTarget( strTarget ) )
        {
            if ( PageMatcher.matches( tour, strPath, strQueryString ) )
            {
                mapVariants.computeIfAbsent( tour.getCode( ), k -> new ArrayList<>( ) ).add( tour );
            }
        }

        List<Tour> listTours = new ArrayList<>( );

        for ( List<Tour> listVariants : mapVariants.values( ) )
        {
            selectTranslation( listVariants, strLang ).map( this::loadSteps ).ifPresent( listTours::add );
        }

        return listTours;
    }

    /**
     * Choose the translation of a tour for a language: the exact language, then the tour shown whatever the language, then the default language
     * of the site, then the first translation.
     *
     * @param listVariants
     *            the translations of one tour
     * @param strLang
     *            the language of the user
     * @return the chosen translation, empty when the list is empty
     */
    public static Optional<Tour> selectTranslation( List<Tour> listVariants, String strLang )
    {
        String strDefaultLang = I18nService.getDefaultLocale( ).getLanguage( );

        for ( String strCandidate : new String [ ] {
                StringUtils.defaultString( strLang ), Tour.LANG_ALL, strDefaultLang
        } )
        {
            Optional<Tour> tour = listVariants.stream( ).filter( t -> strCandidate.equals( StringUtils.defaultString( t.getLang( ) ) ) ).findFirst( );

            if ( tour.isPresent( ) )
            {
                return tour;
            }
        }

        return listVariants.stream( ).findFirst( );
    }

    /**
     * Tell whether a code is already used by another tour in the same language
     *
     * @param strCode
     *            the code
     * @param strLang
     *            the language
     * @param nIdTour
     *            the identifier of the tour being edited, 0 for a new tour
     * @return true if another tour already uses the code in this language
     */
    public boolean isCodeUsed( String strCode, String strLang, int nIdTour )
    {
        return TourHome.findByCodeAndLang( strCode, strLang ).filter( tour -> tour.getId( ) != nIdTour ).isPresent( );
    }

    /**
     * Create the translation of a tour in another language: a copy of the tour and of its steps, disabled until it is translated
     *
     * @param nIdTour
     *            the identifier of the tour to translate
     * @param strLang
     *            the language of the translation
     * @return the created translation, empty when the tour does not exist or already has this translation
     */
    public Optional<Tour> createTranslation( int nIdTour, String strLang )
    {
        Optional<Tour> source = findById( nIdTour );

        if ( source.isEmpty( ) || isCodeUsed( source.get( ).getCode( ), strLang, 0 ) )
        {
            return Optional.empty( );
        }

        Tour translation = source.get( );
        translation.setLang( strLang );
        translation.setEnabled( false );

        return Optional.of( create( translation ) );
    }

    /**
     * Create a tour, with the steps it carries
     *
     * @param tour
     *            the tour
     * @return the created tour
     */
    public Tour create( Tour tour )
    {
        TourHome.create( tour );
        createSteps( tour );

        return tour;
    }

    /**
     * Update the properties of a tour, its steps are left untouched
     *
     * @param tour
     *            the tour
     * @return the updated tour
     */
    public Tour update( Tour tour )
    {
        return TourHome.update( tour );
    }

    /**
     * Remove a tour and its steps. When its last translation is removed, the states recorded for its users are removed too.
     *
     * @param nIdTour
     *            the tour identifier
     */
    public void remove( int nIdTour )
    {
        Optional<Tour> tour = TourHome.findByPrimaryKey( nIdTour );
        TourHome.remove( nIdTour );

        tour.filter( t -> TourHome.findByCode( t.getCode( ) ).isEmpty( ) ).ifPresent( t -> TourUserStateHome.removeByTour( t.getCode( ) ) );
    }

    /**
     * Find a step
     *
     * @param nIdStep
     *            the step identifier
     * @return the step if found
     */
    public Optional<Step> findStep( int nIdStep )
    {
        return StepHome.findByPrimaryKey( nIdStep );
    }

    /**
     * Add a step at the end of its tour
     *
     * @param step
     *            the step, its tour identifier must be set
     * @return the created step
     */
    public Step createStep( Step step )
    {
        step.setOrder( StepHome.findMaxOrder( step.getIdTour( ) ) + 1 );

        return StepHome.create( step );
    }

    /**
     * Update a step, keeping its tour and its position
     *
     * @param step
     *            the step
     * @return the updated step, or empty when it does not exist
     */
    public Optional<Step> updateStep( Step step )
    {
        return StepHome.findByPrimaryKey( step.getId( ) ).map( stored -> {
            step.setIdTour( stored.getIdTour( ) );
            step.setOrder( stored.getOrder( ) );

            return StepHome.update( step );
        } );
    }

    /**
     * Remove a step and renumber the remaining steps of its tour
     *
     * @param nIdStep
     *            the step identifier
     */
    public void removeStep( int nIdStep )
    {
        StepHome.findByPrimaryKey( nIdStep ).ifPresent( step -> {
            StepHome.remove( nIdStep );
            renumberSteps( step.getIdTour( ) );
        } );
    }

    /**
     * Move a step one position up or down in its tour
     *
     * @param nIdStep
     *            the step identifier
     * @param bUp
     *            true to move the step up, false to move it down
     */
    public void moveStep( int nIdStep, boolean bUp )
    {
        StepHome.findByPrimaryKey( nIdStep ).ifPresent( step -> {
            List<Step> listSteps = StepHome.findByTour( step.getIdTour( ) );
            int nPosition = indexOf( listSteps, nIdStep );
            int nOther = bUp ? nPosition - 1 : nPosition + 1;

            if ( nPosition >= 0 && nOther >= 0 && nOther < listSteps.size( ) )
            {
                listSteps.add( nOther, listSteps.remove( nPosition ) );
                storeOrders( listSteps );
            }
        } );
    }

    /**
     * Import tours. A tour whose code already exists in the same language is replaced when overwrite is requested, skipped otherwise.
     *
     * @param listTours
     *            the tours to import, with their steps
     * @param bOverwrite
     *            true to replace the existing tours
     * @return the import counters
     */
    public ImportResult importTours( List<Tour> listTours, boolean bOverwrite )
    {
        int nCreated = 0;
        int nUpdated = 0;
        int nSkipped = 0;

        for ( Tour tour : listTours )
        {
            Optional<Tour> existing = TourHome.findByCodeAndLang( tour.getCode( ), tour.getLang( ) );

            if ( existing.isEmpty( ) )
            {
                create( tour );
                nCreated++;
            }
            else
                if ( bOverwrite )
                {
                    TourHome.remove( existing.get( ).getId( ) );
                    create( tour );
                    nUpdated++;
                }
                else
                {
                    nSkipped++;
                }
        }

        return new ImportResult( nCreated, nUpdated, nSkipped );
    }

    /**
     * Load the steps of a tour
     *
     * @param tour
     *            the tour
     * @return the tour, with its steps
     */
    private Tour loadSteps( Tour tour )
    {
        tour.setSteps( StepHome.findByTour( tour.getId( ) ) );

        return tour;
    }

    /**
     * Persist the steps carried by a freshly created tour
     *
     * @param tour
     *            the tour
     */
    private void createSteps( Tour tour )
    {
        int nOrder = 1;

        for ( Step step : tour.getSteps( ) )
        {
            step.setIdTour( tour.getId( ) );
            step.setOrder( nOrder++ );
            StepHome.create( step );
        }
    }

    /**
     * Renumber the steps of a tour from 1
     *
     * @param nIdTour
     *            the tour identifier
     */
    private void renumberSteps( int nIdTour )
    {
        storeOrders( StepHome.findByTour( nIdTour ) );
    }

    /**
     * Store the orders of a list of steps, following the list order
     *
     * @param listSteps
     *            the ordered steps
     */
    private void storeOrders( List<Step> listSteps )
    {
        int nOrder = 1;

        for ( Step step : listSteps )
        {
            if ( step.getOrder( ) != nOrder )
            {
                StepHome.updateOrder( step.getId( ), nOrder );
            }

            nOrder++;
        }
    }

    /**
     * Find the position of a step in a list
     *
     * @param listSteps
     *            the steps
     * @param nIdStep
     *            the step identifier
     * @return the position, -1 when absent
     */
    private static int indexOf( List<Step> listSteps, int nIdStep )
    {
        for ( int i = 0; i < listSteps.size( ); i++ )
        {
            if ( listSteps.get( i ).getId( ) == nIdStep )
            {
                return i;
            }
        }

        return -1;
    }
}
