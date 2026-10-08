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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.portal.service.rbac.RBACResource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A guided tour: a sequence of steps displayed by Driver.js on a back office or front office page.
 * The tour carries the Driver.js global configuration; each {@link Step} can override part of it.
 * <p>
 * RBAC resource: the resource identifier is the tour <b>code</b>, so that the permissions granted on a tour apply to all its translations.
 * </p>
 */
public class Tour implements Serializable, RBACResource
{
    public static final String RESOURCE_TYPE = "ONTOUR_TOUR";

    public static final String TARGET_BO = "BO";
    public static final String TARGET_FO = "FO";

    public static final String TRIGGER_MANUAL = "manual";
    public static final String TRIGGER_FIRST_VISIT = "first_visit";
    public static final String TRIGGER_ALWAYS = "always";

    public static final String OVERLAY_CLICK_CLOSE = "close";
    public static final String OVERLAY_CLICK_NEXT_STEP = "nextStep";
    public static final String OVERLAY_CLICK_NONE = "none";

    public static final String DEFAULT_SHOW_BUTTONS = "next,previous,close";

    public static final String LANG_ALL = "";

    private static final long serialVersionUID = 1L;

    private int _nId;

    @NotEmpty( message = "#i18n{ontour.validation.tour.code.notEmpty}" )
    @Size( max = 100, message = "#i18n{ontour.validation.tour.code.size}" )
    @Pattern( regexp = "[a-zA-Z0-9_.\\-]*", message = "#i18n{ontour.validation.tour.code.pattern}" )
    private String _strCode;

    @Pattern( regexp = "|[a-z]{2,3}", message = "#i18n{ontour.validation.tour.lang.pattern}" )
    private String _strLang = LANG_ALL;

    @NotEmpty( message = "#i18n{ontour.validation.tour.title.notEmpty}" )
    @Size( max = 255, message = "#i18n{ontour.validation.tour.title.size}" )
    private String _strTitle;

    private String _strDescription;

    @Pattern( regexp = "BO|FO", message = "#i18n{ontour.validation.tour.target.pattern}" )
    private String _strTarget = TARGET_BO;

    @Size( max = 512, message = "#i18n{ontour.validation.tour.pagePath.size}" )
    private String _strPagePath = "";

    @Size( max = 512, message = "#i18n{ontour.validation.tour.pageParameters.size}" )
    private String _strPageParameters = "";

    @Size( max = 255, message = "#i18n{ontour.validation.tour.pageSelector.size}" )
    private String _strPageSelector = "";

    @Pattern( regexp = "manual|first_visit|always", message = "#i18n{ontour.validation.tour.triggerMode.pattern}" )
    private String _strTriggerMode = TRIGGER_MANUAL;

    private boolean _bShowLauncher;
    private boolean _bEnabled;

    private boolean _bAnimate;

    @Min( value = 0, message = "#i18n{ontour.validation.tour.positive}" )
    private int _nDuration = 400;

    @Size( max = 50, message = "#i18n{ontour.validation.tour.overlayColor.size}" )
    private String _strOverlayColor = "#000";

    @Min( value = 0, message = "#i18n{ontour.validation.tour.overlayOpacity.range}" )
    @Max( value = 100, message = "#i18n{ontour.validation.tour.overlayOpacity.range}" )
    private int _nOverlayOpacity = 70;

    private boolean _bSmoothScroll;
    private boolean _bAllowClose;
    private boolean _bAllowScroll;

    @Pattern( regexp = "close|nextStep|none", message = "#i18n{ontour.validation.tour.overlayClickBehavior.pattern}" )
    private String _strOverlayClickBehavior = OVERLAY_CLICK_CLOSE;

    @Min( value = 0, message = "#i18n{ontour.validation.tour.positive}" )
    private int _nStagePadding = 10;

    @Min( value = 0, message = "#i18n{ontour.validation.tour.positive}" )
    private int _nStageRadius = 5;

    private boolean _bDisableActiveInteraction;
    private boolean _bAdvanceOnClick;
    private boolean _bSkipMissingElement;

    @Min( value = 0, message = "#i18n{ontour.validation.tour.positive}" )
    private int _nWaitForElement;

    private boolean _bAllowKeyboardControl;

    @Size( max = 255, message = "#i18n{ontour.validation.tour.popoverClass.size}" )
    private String _strPopoverClass = "";

    @Min( value = 0, message = "#i18n{ontour.validation.tour.positive}" )
    private int _nPopoverOffset = 10;

    private String _strShowButtons = DEFAULT_SHOW_BUTTONS;
    private String _strDisableButtons = "";
    private boolean _bShowProgress;

    @Size( max = 255, message = "#i18n{ontour.validation.tour.buttonText.size}" )
    private String _strProgressText = "";

    @Size( max = 100, message = "#i18n{ontour.validation.tour.buttonText.size}" )
    private String _strNextBtnText = "";

    @Size( max = 100, message = "#i18n{ontour.validation.tour.buttonText.size}" )
    private String _strPrevBtnText = "";

    @Size( max = 100, message = "#i18n{ontour.validation.tour.buttonText.size}" )
    private String _strDoneBtnText = "";

    private List<Step> _listSteps = new ArrayList<>( );

    /**
     * Build a tour initialized with the Driver.js default values. Boolean fields of a bare instance are all
     * <code>false</code> so that an unchecked form checkbox binds correctly; this factory is used for new tours.
     *
     * @return a tour with the Driver.js defaults
     */
    public static Tour createWithDefaults( )
    {
        Tour tour = new Tour( );
        tour.setShowLauncher( true );
        tour.setAnimate( true );
        tour.setAllowClose( true );
        tour.setAllowScroll( true );
        tour.setAllowKeyboardControl( true );
        return tour;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceTypeCode( )
    {
        return RESOURCE_TYPE;
    }

    /**
     * {@inheritDoc} The tour code, shared by all the translations of the tour.
     */
    @Override
    public String getResourceId( )
    {
        return _strCode;
    }

    /**
     * Returns the identifier
     *
     * @return the identifier
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the identifier
     *
     * @param nId
     *            the identifier
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the functional code, unique, used to start the tour from a page or from the JS API
     *
     * @return the code
     */
    public String getCode( )
    {
        return _strCode;
    }

    /**
     * Sets the code
     *
     * @param strCode
     *            the code
     */
    public void setCode( String strCode )
    {
        _strCode = strCode;
    }

    /**
     * Returns the language of the tour content (ISO 639 code such as <code>fr</code>), or {@link #LANG_ALL} for a tour shown whatever the
     * language. Several tours can share the same code with different languages: they are the translations of one tour.
     *
     * @return the language
     */
    public String getLang( )
    {
        return _strLang;
    }

    /**
     * Sets the language of the tour content
     *
     * @param strLang
     *            the language, or {@link #LANG_ALL}
     */
    public void setLang( String strLang )
    {
        _strLang = strLang;
    }

    /**
     * Returns the title
     *
     * @return the title
     */
    public String getTitle( )
    {
        return _strTitle;
    }

    /**
     * Sets the title
     *
     * @param strTitle
     *            the title
     */
    public void setTitle( String strTitle )
    {
        _strTitle = strTitle;
    }

    /**
     * Returns the internal description (not displayed to end users)
     *
     * @return the description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the description
     *
     * @param strDescription
     *            the description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the target: {@link #TARGET_BO} or {@link #TARGET_FO}
     *
     * @return the target
     */
    public String getTarget( )
    {
        return _strTarget;
    }

    /**
     * Sets the target
     *
     * @param strTarget
     *            the target
     */
    public void setTarget( String strTarget )
    {
        _strTarget = strTarget;
    }

    /**
     * Returns the page path pattern (relative to the webapp, <code>*</code> wildcard allowed). Empty means the tour is never started
     * automatically.
     *
     * @return the page path pattern
     */
    public String getPagePath( )
    {
        return _strPagePath;
    }

    /**
     * Sets the page path pattern
     *
     * @param strPagePath
     *            the page path pattern
     */
    public void setPagePath( String strPagePath )
    {
        _strPagePath = strPagePath;
    }

    /**
     * Returns the required page parameters, formatted as a query string (<code>view=createBlog&amp;id</code>)
     *
     * @return the page parameters
     */
    public String getPageParameters( )
    {
        return _strPageParameters;
    }

    /**
     * Sets the required page parameters
     *
     * @param strPageParameters
     *            the page parameters
     */
    public void setPageParameters( String strPageParameters )
    {
        _strPageParameters = strPageParameters;
    }

    /**
     * Returns the CSS selector of an element that the page must contain for the tour to apply. It distinguishes the screens that share the same
     * URL (for instance a view reached by a POST form). Empty means no condition.
     *
     * @return the page selector
     */
    public String getPageSelector( )
    {
        return _strPageSelector;
    }

    /**
     * Sets the CSS selector of an element that the page must contain
     *
     * @param strPageSelector
     *            the page selector
     */
    public void setPageSelector( String strPageSelector )
    {
        _strPageSelector = strPageSelector;
    }

    /**
     * Returns the trigger mode: {@link #TRIGGER_MANUAL}, {@link #TRIGGER_FIRST_VISIT} or {@link #TRIGGER_ALWAYS}
     *
     * @return the trigger mode
     */
    public String getTriggerMode( )
    {
        return _strTriggerMode;
    }

    /**
     * Sets the trigger mode
     *
     * @param strTriggerMode
     *            the trigger mode
     */
    public void setTriggerMode( String strTriggerMode )
    {
        _strTriggerMode = strTriggerMode;
    }

    /**
     * Tells if the floating launcher button is displayed on matching pages
     *
     * @return true if the launcher is displayed
     */
    public boolean isShowLauncher( )
    {
        return _bShowLauncher;
    }

    /**
     * Sets the launcher display
     *
     * @param bShowLauncher
     *            true to display the launcher
     */
    public void setShowLauncher( boolean bShowLauncher )
    {
        _bShowLauncher = bShowLauncher;
    }

    /**
     * Tells if the tour is enabled
     *
     * @return true if enabled
     */
    public boolean isEnabled( )
    {
        return _bEnabled;
    }

    /**
     * Sets the enabled status
     *
     * @param bEnabled
     *            the enabled status
     */
    public void setEnabled( boolean bEnabled )
    {
        _bEnabled = bEnabled;
    }

    /**
     * Driver.js <code>animate</code>
     *
     * @return the animate option
     */
    public boolean isAnimate( )
    {
        return _bAnimate;
    }

    /**
     * Sets the Driver.js <code>animate</code> option
     *
     * @param bAnimate
     *            the animate option
     */
    public void setAnimate( boolean bAnimate )
    {
        _bAnimate = bAnimate;
    }

    /**
     * Driver.js <code>duration</code> (ms)
     *
     * @return the animation duration
     */
    public int getDuration( )
    {
        return _nDuration;
    }

    /**
     * Sets the Driver.js <code>duration</code> option
     *
     * @param nDuration
     *            the animation duration
     */
    public void setDuration( int nDuration )
    {
        _nDuration = nDuration;
    }

    /**
     * Driver.js <code>overlayColor</code>
     *
     * @return the overlay color
     */
    public String getOverlayColor( )
    {
        return _strOverlayColor;
    }

    /**
     * Sets the Driver.js <code>overlayColor</code> option
     *
     * @param strOverlayColor
     *            the overlay color
     */
    public void setOverlayColor( String strOverlayColor )
    {
        _strOverlayColor = strOverlayColor;
    }

    /**
     * Driver.js <code>overlayOpacity</code>, stored as a percentage (0-100)
     *
     * @return the overlay opacity percentage
     */
    public int getOverlayOpacity( )
    {
        return _nOverlayOpacity;
    }

    /**
     * Sets the overlay opacity percentage
     *
     * @param nOverlayOpacity
     *            the overlay opacity percentage
     */
    public void setOverlayOpacity( int nOverlayOpacity )
    {
        _nOverlayOpacity = nOverlayOpacity;
    }

    /**
     * Driver.js <code>smoothScroll</code>
     *
     * @return the smooth scroll option
     */
    public boolean isSmoothScroll( )
    {
        return _bSmoothScroll;
    }

    /**
     * Sets the Driver.js <code>smoothScroll</code> option
     *
     * @param bSmoothScroll
     *            the smooth scroll option
     */
    public void setSmoothScroll( boolean bSmoothScroll )
    {
        _bSmoothScroll = bSmoothScroll;
    }

    /**
     * Driver.js <code>allowClose</code>
     *
     * @return the allow close option
     */
    public boolean isAllowClose( )
    {
        return _bAllowClose;
    }

    /**
     * Sets the Driver.js <code>allowClose</code> option
     *
     * @param bAllowClose
     *            the allow close option
     */
    public void setAllowClose( boolean bAllowClose )
    {
        _bAllowClose = bAllowClose;
    }

    /**
     * Driver.js <code>allowScroll</code>
     *
     * @return the allow scroll option
     */
    public boolean isAllowScroll( )
    {
        return _bAllowScroll;
    }

    /**
     * Sets the Driver.js <code>allowScroll</code> option
     *
     * @param bAllowScroll
     *            the allow scroll option
     */
    public void setAllowScroll( boolean bAllowScroll )
    {
        _bAllowScroll = bAllowScroll;
    }

    /**
     * Driver.js <code>overlayClickBehavior</code>: close, nextStep or none (click ignored)
     *
     * @return the overlay click behavior
     */
    public String getOverlayClickBehavior( )
    {
        return _strOverlayClickBehavior;
    }

    /**
     * Sets the overlay click behavior
     *
     * @param strOverlayClickBehavior
     *            the overlay click behavior
     */
    public void setOverlayClickBehavior( String strOverlayClickBehavior )
    {
        _strOverlayClickBehavior = strOverlayClickBehavior;
    }

    /**
     * Driver.js <code>stagePadding</code>
     *
     * @return the stage padding
     */
    public int getStagePadding( )
    {
        return _nStagePadding;
    }

    /**
     * Sets the Driver.js <code>stagePadding</code> option
     *
     * @param nStagePadding
     *            the stage padding
     */
    public void setStagePadding( int nStagePadding )
    {
        _nStagePadding = nStagePadding;
    }

    /**
     * Driver.js <code>stageRadius</code>
     *
     * @return the stage radius
     */
    public int getStageRadius( )
    {
        return _nStageRadius;
    }

    /**
     * Sets the Driver.js <code>stageRadius</code> option
     *
     * @param nStageRadius
     *            the stage radius
     */
    public void setStageRadius( int nStageRadius )
    {
        _nStageRadius = nStageRadius;
    }

    /**
     * Driver.js <code>disableActiveInteraction</code>
     *
     * @return the disable active interaction option
     */
    public boolean isDisableActiveInteraction( )
    {
        return _bDisableActiveInteraction;
    }

    /**
     * Sets the Driver.js <code>disableActiveInteraction</code> option
     *
     * @param bDisableActiveInteraction
     *            the disable active interaction option
     */
    public void setDisableActiveInteraction( boolean bDisableActiveInteraction )
    {
        _bDisableActiveInteraction = bDisableActiveInteraction;
    }

    /**
     * Driver.js <code>advanceOnClick</code>
     *
     * @return the advance on click option
     */
    public boolean isAdvanceOnClick( )
    {
        return _bAdvanceOnClick;
    }

    /**
     * Sets the Driver.js <code>advanceOnClick</code> option
     *
     * @param bAdvanceOnClick
     *            the advance on click option
     */
    public void setAdvanceOnClick( boolean bAdvanceOnClick )
    {
        _bAdvanceOnClick = bAdvanceOnClick;
    }

    /**
     * Driver.js <code>skipMissingElement</code>
     *
     * @return the skip missing element option
     */
    public boolean isSkipMissingElement( )
    {
        return _bSkipMissingElement;
    }

    /**
     * Sets the Driver.js <code>skipMissingElement</code> option
     *
     * @param bSkipMissingElement
     *            the skip missing element option
     */
    public void setSkipMissingElement( boolean bSkipMissingElement )
    {
        _bSkipMissingElement = bSkipMissingElement;
    }

    /**
     * Driver.js <code>waitForElement</code> (ms)
     *
     * @return the wait for element delay
     */
    public int getWaitForElement( )
    {
        return _nWaitForElement;
    }

    /**
     * Sets the Driver.js <code>waitForElement</code> option
     *
     * @param nWaitForElement
     *            the wait for element delay
     */
    public void setWaitForElement( int nWaitForElement )
    {
        _nWaitForElement = nWaitForElement;
    }

    /**
     * Driver.js <code>allowKeyboardControl</code>
     *
     * @return the allow keyboard control option
     */
    public boolean isAllowKeyboardControl( )
    {
        return _bAllowKeyboardControl;
    }

    /**
     * Sets the Driver.js <code>allowKeyboardControl</code> option
     *
     * @param bAllowKeyboardControl
     *            the allow keyboard control option
     */
    public void setAllowKeyboardControl( boolean bAllowKeyboardControl )
    {
        _bAllowKeyboardControl = bAllowKeyboardControl;
    }

    /**
     * Driver.js <code>popoverClass</code>
     *
     * @return the popover CSS class
     */
    public String getPopoverClass( )
    {
        return _strPopoverClass;
    }

    /**
     * Sets the Driver.js <code>popoverClass</code> option
     *
     * @param strPopoverClass
     *            the popover CSS class
     */
    public void setPopoverClass( String strPopoverClass )
    {
        _strPopoverClass = strPopoverClass;
    }

    /**
     * Driver.js <code>popoverOffset</code>
     *
     * @return the popover offset
     */
    public int getPopoverOffset( )
    {
        return _nPopoverOffset;
    }

    /**
     * Sets the Driver.js <code>popoverOffset</code> option
     *
     * @param nPopoverOffset
     *            the popover offset
     */
    public void setPopoverOffset( int nPopoverOffset )
    {
        _nPopoverOffset = nPopoverOffset;
    }

    /**
     * Driver.js <code>showButtons</code>, comma separated among next, previous, close
     *
     * @return the displayed buttons
     */
    public String getShowButtons( )
    {
        return _strShowButtons;
    }

    /**
     * Sets the displayed buttons
     *
     * @param strShowButtons
     *            the displayed buttons, comma separated
     */
    public void setShowButtons( String strShowButtons )
    {
        _strShowButtons = strShowButtons;
    }

    /**
     * Driver.js <code>disableButtons</code>, comma separated among next, previous, close
     *
     * @return the disabled buttons
     */
    public String getDisableButtons( )
    {
        return _strDisableButtons;
    }

    /**
     * Sets the disabled buttons
     *
     * @param strDisableButtons
     *            the disabled buttons, comma separated
     */
    public void setDisableButtons( String strDisableButtons )
    {
        _strDisableButtons = strDisableButtons;
    }

    /**
     * Driver.js <code>showProgress</code>
     *
     * @return the show progress option
     */
    public boolean isShowProgress( )
    {
        return _bShowProgress;
    }

    /**
     * Sets the Driver.js <code>showProgress</code> option
     *
     * @param bShowProgress
     *            the show progress option
     */
    public void setShowProgress( boolean bShowProgress )
    {
        _bShowProgress = bShowProgress;
    }

    /**
     * Driver.js <code>progressText</code> (<code>{{current}}</code> and <code>{{total}}</code> placeholders)
     *
     * @return the progress text
     */
    public String getProgressText( )
    {
        return _strProgressText;
    }

    /**
     * Sets the progress text
     *
     * @param strProgressText
     *            the progress text
     */
    public void setProgressText( String strProgressText )
    {
        _strProgressText = strProgressText;
    }

    /**
     * Driver.js <code>nextBtnText</code>
     *
     * @return the next button text
     */
    public String getNextBtnText( )
    {
        return _strNextBtnText;
    }

    /**
     * Sets the next button text
     *
     * @param strNextBtnText
     *            the next button text
     */
    public void setNextBtnText( String strNextBtnText )
    {
        _strNextBtnText = strNextBtnText;
    }

    /**
     * Driver.js <code>prevBtnText</code>
     *
     * @return the previous button text
     */
    public String getPrevBtnText( )
    {
        return _strPrevBtnText;
    }

    /**
     * Sets the previous button text
     *
     * @param strPrevBtnText
     *            the previous button text
     */
    public void setPrevBtnText( String strPrevBtnText )
    {
        _strPrevBtnText = strPrevBtnText;
    }

    /**
     * Driver.js <code>doneBtnText</code>
     *
     * @return the done button text
     */
    public String getDoneBtnText( )
    {
        return _strDoneBtnText;
    }

    /**
     * Sets the done button text
     *
     * @param strDoneBtnText
     *            the done button text
     */
    public void setDoneBtnText( String strDoneBtnText )
    {
        _strDoneBtnText = strDoneBtnText;
    }

    /**
     * Returns the steps of the tour, ordered
     *
     * @return the steps
     */
    public List<Step> getSteps( )
    {
        return _listSteps;
    }

    /**
     * Sets the steps
     *
     * @param listSteps
     *            the steps
     */
    public void setSteps( List<Step> listSteps )
    {
        _listSteps = ( listSteps != null ) ? listSteps : new ArrayList<>( );
    }
}
