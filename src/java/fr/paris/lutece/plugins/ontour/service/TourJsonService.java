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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.ontour.business.Step;
import fr.paris.lutece.plugins.ontour.business.Tour;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * JSON conversions of tours:
 * <ul>
 * <li>the Driver.js configuration sent to the browser,</li>
 * <li>the export / import format, used by the admin feature and by the tour files shipped by other plugins.</li>
 * </ul>
 */
@ApplicationScoped
public class TourJsonService
{
    public static final int FORMAT_VERSION = 1;

    private static final String KEY_VERSION = "version";
    private static final String KEY_TOURS = "tours";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_POPOVER = "popover";
    private static final String BUTTON_SEPARATOR = ",";
    private static final Set<String> ALLOWED_BUTTONS = Set.of( "next", "previous", "close" );
    private static final String CODE_PATTERN = "[a-zA-Z0-9_.\\-]+";
    private static final String LANG_PATTERN = "|[a-z]{2,3}";

    private final ObjectMapper _mapper;

    /**
     * Export mixin of {@link Tour}: technical fields are not exported
     */
    @JsonIgnoreProperties( value = {
            "id", "resourceTypeCode", "resourceId"
    } )
    @JsonInclude( JsonInclude.Include.NON_NULL )
    private abstract static class TourMixin
    {
    }

    /**
     * Export mixin of {@link Step}: technical fields are not exported, the order is given by the array
     */
    @JsonIgnoreProperties( value = {
            "id", "idTour", "order"
    } )
    @JsonInclude( JsonInclude.Include.NON_NULL )
    private abstract static class StepMixin
    {
    }

    /**
     * Constructor
     */
    public TourJsonService( )
    {
        _mapper = new ObjectMapper( );
        _mapper.addMixIn( Tour.class, TourMixin.class );
        _mapper.addMixIn( Step.class, StepMixin.class );
        _mapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
        _mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
    }

    /**
     * Build the Driver.js configuration of a tour. Only meaningful values are written, so that Driver.js keeps its own defaults otherwise.
     *
     * @param tour
     *            the tour, with its steps
     * @param mapDefaultTexts
     *            localized default texts (progressText, nextBtnText, prevBtnText, doneBtnText) used when the tour does not define them
     * @return the Driver.js configuration
     */
    public Map<String, Object> toDriverConfig( Tour tour, Map<String, String> mapDefaultTexts )
    {
        Map<String, Object> config = new LinkedHashMap<>( );
        config.put( "animate", tour.isAnimate( ) );
        config.put( "duration", tour.getDuration( ) );
        config.put( "overlayColor", StringUtils.defaultIfBlank( tour.getOverlayColor( ), "#000" ) );
        config.put( "overlayOpacity", tour.getOverlayOpacity( ) / 100.0 );
        config.put( "smoothScroll", tour.isSmoothScroll( ) );
        config.put( "allowClose", tour.isAllowClose( ) );
        config.put( "allowScroll", tour.isAllowScroll( ) );
        config.put( "overlayClickBehavior", tour.getOverlayClickBehavior( ) );
        config.put( "stagePadding", tour.getStagePadding( ) );
        config.put( "stageRadius", tour.getStageRadius( ) );
        config.put( "disableActiveInteraction", tour.isDisableActiveInteraction( ) );
        config.put( "advanceOnClick", tour.isAdvanceOnClick( ) );
        config.put( "skipMissingElement", tour.isSkipMissingElement( ) );
        config.put( "waitForElement", tour.getWaitForElement( ) );
        config.put( "allowKeyboardControl", tour.isAllowKeyboardControl( ) );
        putIfNotBlank( config, "popoverClass", tour.getPopoverClass( ) );
        config.put( "popoverOffset", tour.getPopoverOffset( ) );
        config.put( "showButtons", toButtons( tour.getShowButtons( ) ) );
        config.put( "disableButtons", toButtons( tour.getDisableButtons( ) ) );
        config.put( "showProgress", tour.isShowProgress( ) );
        putText( config, "progressText", tour.getProgressText( ), mapDefaultTexts );
        putText( config, "nextBtnText", tour.getNextBtnText( ), mapDefaultTexts );
        putText( config, "prevBtnText", tour.getPrevBtnText( ), mapDefaultTexts );
        putText( config, "doneBtnText", tour.getDoneBtnText( ), mapDefaultTexts );

        List<Map<String, Object>> listSteps = new ArrayList<>( );

        for ( Step step : tour.getSteps( ) )
        {
            listSteps.add( toDriverStep( step ) );
        }

        config.put( KEY_STEPS, listSteps );

        return config;
    }

    /**
     * Build the Driver.js <code>DriveStep</code> of a step
     *
     * @param step
     *            the step
     * @return the Driver.js step
     */
    public Map<String, Object> toDriverStep( Step step )
    {
        Map<String, Object> driveStep = new LinkedHashMap<>( );
        putIfNotBlank( driveStep, "element", step.getElement( ) );

        Map<String, Object> popover = new LinkedHashMap<>( );
        putIfNotBlank( popover, "title", step.getTitle( ) );
        putIfNotBlank( popover, "description", step.getDescription( ) );
        putIfNotBlank( popover, "side", step.getSide( ) );
        putIfNotBlank( popover, "align", step.getAlign( ) );

        if ( step.getShowButtons( ) != null )
        {
            popover.put( "showButtons", toButtons( step.getShowButtons( ) ) );
        }

        if ( step.getDisableButtons( ) != null )
        {
            popover.put( "disableButtons", toButtons( step.getDisableButtons( ) ) );
        }

        putTriState( popover, "showProgress", step.getShowProgress( ) );
        putIfNotBlank( popover, "popoverClass", step.getPopoverClass( ) );
        putIfNotBlank( popover, "progressText", step.getProgressText( ) );
        putIfNotBlank( popover, "nextBtnText", step.getNextBtnText( ) );
        putIfNotBlank( popover, "prevBtnText", step.getPrevBtnText( ) );
        putIfNotBlank( popover, "doneBtnText", step.getDoneBtnText( ) );

        if ( !popover.isEmpty( ) )
        {
            driveStep.put( KEY_POPOVER, popover );
        }

        putTriState( driveStep, "disableActiveInteraction", step.getDisableActiveInteraction( ) );
        putTriState( driveStep, "advanceOnClick", step.getAdvanceOnClick( ) );
        putTriState( driveStep, "skipMissingElement", step.getSkipMissingElement( ) );

        if ( step.getWaitForElement( ) != Step.INHERIT )
        {
            driveStep.put( "waitForElement", step.getWaitForElement( ) );
        }

        return driveStep;
    }

    /**
     * Serialize an object to JSON
     *
     * @param object
     *            the object
     * @return the JSON string
     * @throws JsonProcessingException
     *             if the object cannot be serialized
     */
    public String toJson( Object object ) throws JsonProcessingException
    {
        return _mapper.writeValueAsString( object );
    }

    /**
     * Export tours to the onTour JSON format
     *
     * @param listTours
     *            the tours, with their steps
     * @return the JSON document
     * @throws JsonProcessingException
     *             if the tours cannot be serialized
     */
    public String exportTours( List<Tour> listTours ) throws JsonProcessingException
    {
        Map<String, Object> document = new LinkedHashMap<>( );
        document.put( KEY_VERSION, FORMAT_VERSION );
        document.put( KEY_TOURS, listTours );

        return _mapper.writeValueAsString( document );
    }

    /**
     * Parse tours from the onTour JSON format. Accepted documents: <code>{ "tours": [ ... ] }</code>, an array of tours or a single tour.
     * Properties that are missing take the Driver.js default values.
     *
     * @param strJson
     *            the JSON document
     * @return the parsed tours, with their steps
     * @throws TourImportException
     *             if the document is malformed or a tour is invalid
     */
    public List<Tour> parseTours( String strJson ) throws TourImportException
    {
        JsonNode root;

        try
        {
            root = _mapper.readTree( StringUtils.defaultString( strJson ) );
        }
        catch( JsonProcessingException e )
        {
            throw new TourImportException( "Malformed JSON: " + e.getOriginalMessage( ), e );
        }

        JsonNode nodeTours = root;

        if ( root != null && root.isObject( ) && root.has( KEY_TOURS ) )
        {
            nodeTours = root.get( KEY_TOURS );
        }

        List<JsonNode> listNodes = new ArrayList<>( );

        if ( nodeTours != null && nodeTours.isArray( ) )
        {
            nodeTours.forEach( listNodes::add );
        }
        else
            if ( nodeTours != null && nodeTours.isObject( ) )
            {
                listNodes.add( nodeTours );
            }
            else
            {
                throw new TourImportException( "No tour found in the JSON document", null );
            }

        List<Tour> listTours = new ArrayList<>( );

        for ( JsonNode node : listNodes )
        {
            listTours.add( parseTour( node ) );
        }

        return listTours;
    }

    /**
     * Parse and check one tour
     *
     * @param node
     *            the tour node
     * @return the tour
     * @throws TourImportException
     *             if the tour is invalid
     */
    private Tour parseTour( JsonNode node ) throws TourImportException
    {
        Tour tour;

        try
        {
            tour = _mapper.readerForUpdating( Tour.createWithDefaults( ) ).readValue( node );
        }
        catch( IOException e )
        {
            throw new TourImportException( "Invalid tour: " + e.getMessage( ), e );
        }

        if ( StringUtils.isBlank( tour.getCode( ) ) || !tour.getCode( ).matches( CODE_PATTERN ) || tour.getCode( ).length( ) > 100 )
        {
            throw new TourImportException( "Invalid or missing tour code: " + tour.getCode( ), null );
        }

        tour.setLang( StringUtils.defaultString( tour.getLang( ) ).trim( ).toLowerCase( java.util.Locale.ROOT ) );

        if ( !tour.getLang( ).matches( LANG_PATTERN ) )
        {
            throw new TourImportException( "Invalid language for tour " + tour.getCode( ) + ": " + tour.getLang( ), null );
        }

        if ( StringUtils.isBlank( tour.getTitle( ) ) )
        {
            throw new TourImportException( "Missing title for tour " + tour.getCode( ), null );
        }

        if ( !Tour.TARGET_BO.equals( tour.getTarget( ) ) && !Tour.TARGET_FO.equals( tour.getTarget( ) ) )
        {
            throw new TourImportException( "Invalid target for tour " + tour.getCode( ) + ": " + tour.getTarget( ), null );
        }

        tour.setShowButtons( normalizeButtons( tour.getShowButtons( ) ) );
        tour.setDisableButtons( normalizeButtons( tour.getDisableButtons( ) ) );

        for ( Step step : tour.getSteps( ) )
        {
            if ( step.getShowButtons( ) != null )
            {
                step.setShowButtons( normalizeButtons( step.getShowButtons( ) ) );
            }

            if ( step.getDisableButtons( ) != null )
            {
                step.setDisableButtons( normalizeButtons( step.getDisableButtons( ) ) );
            }
        }

        return tour;
    }

    /**
     * Keep only the allowed Driver.js button names of a comma separated list
     *
     * @param strButtons
     *            the comma separated buttons
     * @return the normalized comma separated buttons
     */
    public static String normalizeButtons( String strButtons )
    {
        return String.join( BUTTON_SEPARATOR, toButtons( strButtons ) );
    }

    /**
     * Split a comma separated list of buttons, keeping only the allowed Driver.js button names
     *
     * @param strButtons
     *            the comma separated buttons
     * @return the button names
     */
    public static List<String> toButtons( String strButtons )
    {
        if ( StringUtils.isBlank( strButtons ) )
        {
            return new ArrayList<>( );
        }

        return Arrays.stream( strButtons.split( BUTTON_SEPARATOR ) ).map( String::trim ).filter( ALLOWED_BUTTONS::contains ).distinct( ).toList( );
    }

    /**
     * Put a value when it is not blank
     *
     * @param map
     *            the target map
     * @param strKey
     *            the key
     * @param strValue
     *            the value
     */
    private static void putIfNotBlank( Map<String, Object> map, String strKey, String strValue )
    {
        if ( StringUtils.isNotBlank( strValue ) )
        {
            map.put( strKey, strValue );
        }
    }

    /**
     * Put a text of the tour, or its localized default
     *
     * @param map
     *            the target map
     * @param strKey
     *            the key
     * @param strValue
     *            the value defined on the tour
     * @param mapDefaultTexts
     *            the localized default texts
     */
    private static void putText( Map<String, Object> map, String strKey, String strValue, Map<String, String> mapDefaultTexts )
    {
        String strText = StringUtils.isNotBlank( strValue ) ? strValue : ( mapDefaultTexts != null ? mapDefaultTexts.get( strKey ) : null );
        putIfNotBlank( map, strKey, strText );
    }

    /**
     * Put a tri-state value unless it is {@link Step#INHERIT}
     *
     * @param map
     *            the target map
     * @param strKey
     *            the key
     * @param nValue
     *            the tri-state value
     */
    private static void putTriState( Map<String, Object> map, String strKey, int nValue )
    {
        if ( nValue != Step.INHERIT )
        {
            map.put( strKey, nValue == Step.YES );
        }
    }
}
