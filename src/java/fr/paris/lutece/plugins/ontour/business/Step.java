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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A step of a tour, mapped to a Driver.js <code>DriveStep</code> and its <code>popover</code>.
 * Tri-state options use {@link #INHERIT} to fall back on the tour configuration.
 */
public class Step implements Serializable
{
    public static final int INHERIT = -1;
    public static final int NO = 0;
    public static final int YES = 1;

    private static final long serialVersionUID = 1L;

    private int _nId;
    private int _nIdTour;
    private int _nOrder;

    @Size( max = 512, message = "#i18n{ontour.validation.step.element.size}" )
    private String _strElement = "";

    @Size( max = 255, message = "#i18n{ontour.validation.step.title.size}" )
    private String _strTitle = "";

    private String _strDescription;

    @Pattern( regexp = "|top|right|bottom|left", message = "#i18n{ontour.validation.step.side.pattern}" )
    private String _strSide = "";

    @Pattern( regexp = "|start|center|end", message = "#i18n{ontour.validation.step.align.pattern}" )
    private String _strAlign = "";

    private String _strShowButtons;
    private String _strDisableButtons;

    @Min( value = INHERIT, message = "#i18n{ontour.validation.step.triState}" )
    @Max( value = YES, message = "#i18n{ontour.validation.step.triState}" )
    private int _nShowProgress = INHERIT;

    @Size( max = 255, message = "#i18n{ontour.validation.step.popoverClass.size}" )
    private String _strPopoverClass = "";

    @Size( max = 255, message = "#i18n{ontour.validation.step.buttonText.size}" )
    private String _strProgressText = "";

    @Size( max = 100, message = "#i18n{ontour.validation.step.buttonText.size}" )
    private String _strNextBtnText = "";

    @Size( max = 100, message = "#i18n{ontour.validation.step.buttonText.size}" )
    private String _strPrevBtnText = "";

    @Size( max = 100, message = "#i18n{ontour.validation.step.buttonText.size}" )
    private String _strDoneBtnText = "";

    @Min( value = INHERIT, message = "#i18n{ontour.validation.step.triState}" )
    @Max( value = YES, message = "#i18n{ontour.validation.step.triState}" )
    private int _nDisableActiveInteraction = INHERIT;

    @Min( value = INHERIT, message = "#i18n{ontour.validation.step.triState}" )
    @Max( value = YES, message = "#i18n{ontour.validation.step.triState}" )
    private int _nAdvanceOnClick = INHERIT;

    @Min( value = INHERIT, message = "#i18n{ontour.validation.step.triState}" )
    @Max( value = YES, message = "#i18n{ontour.validation.step.triState}" )
    private int _nSkipMissingElement = INHERIT;

    @Min( value = INHERIT, message = "#i18n{ontour.validation.step.waitForElement}" )
    private int _nWaitForElement = INHERIT;

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
     * Returns the identifier of the parent tour
     *
     * @return the tour identifier
     */
    public int getIdTour( )
    {
        return _nIdTour;
    }

    /**
     * Sets the identifier of the parent tour
     *
     * @param nIdTour
     *            the tour identifier
     */
    public void setIdTour( int nIdTour )
    {
        _nIdTour = nIdTour;
    }

    /**
     * Returns the position of the step in the tour
     *
     * @return the order
     */
    public int getOrder( )
    {
        return _nOrder;
    }

    /**
     * Sets the position of the step in the tour
     *
     * @param nOrder
     *            the order
     */
    public void setOrder( int nOrder )
    {
        _nOrder = nOrder;
    }

    /**
     * Driver.js <code>element</code>: CSS selector of the highlighted element. Empty for a centered popover.
     *
     * @return the CSS selector
     */
    public String getElement( )
    {
        return _strElement;
    }

    /**
     * Sets the CSS selector of the highlighted element
     *
     * @param strElement
     *            the CSS selector
     */
    public void setElement( String strElement )
    {
        _strElement = strElement;
    }

    /**
     * Driver.js <code>popover.title</code>
     *
     * @return the title
     */
    public String getTitle( )
    {
        return _strTitle;
    }

    /**
     * Sets the popover title
     *
     * @param strTitle
     *            the title
     */
    public void setTitle( String strTitle )
    {
        _strTitle = strTitle;
    }

    /**
     * Driver.js <code>popover.description</code> (HTML allowed)
     *
     * @return the description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the popover description
     *
     * @param strDescription
     *            the description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Driver.js <code>popover.side</code>: top, right, bottom, left, or empty for automatic placement
     *
     * @return the side
     */
    public String getSide( )
    {
        return _strSide;
    }

    /**
     * Sets the popover side
     *
     * @param strSide
     *            the side
     */
    public void setSide( String strSide )
    {
        _strSide = strSide;
    }

    /**
     * Driver.js <code>popover.align</code>: start, center, end, or empty for the default
     *
     * @return the alignment
     */
    public String getAlign( )
    {
        return _strAlign;
    }

    /**
     * Sets the popover alignment
     *
     * @param strAlign
     *            the alignment
     */
    public void setAlign( String strAlign )
    {
        _strAlign = strAlign;
    }

    /**
     * Driver.js <code>popover.showButtons</code>, comma separated; <code>null</code> inherits the tour value
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
     *            the displayed buttons, or null to inherit
     */
    public void setShowButtons( String strShowButtons )
    {
        _strShowButtons = strShowButtons;
    }

    /**
     * Driver.js <code>popover.disableButtons</code>, comma separated; <code>null</code> inherits the tour value
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
     *            the disabled buttons, or null to inherit
     */
    public void setDisableButtons( String strDisableButtons )
    {
        _strDisableButtons = strDisableButtons;
    }

    /**
     * Driver.js <code>popover.showProgress</code> as a tri-state value
     *
     * @return {@link #INHERIT}, {@link #NO} or {@link #YES}
     */
    public int getShowProgress( )
    {
        return _nShowProgress;
    }

    /**
     * Sets the show progress tri-state value
     *
     * @param nShowProgress
     *            the tri-state value
     */
    public void setShowProgress( int nShowProgress )
    {
        _nShowProgress = nShowProgress;
    }

    /**
     * Driver.js <code>popover.popoverClass</code>
     *
     * @return the popover CSS class
     */
    public String getPopoverClass( )
    {
        return _strPopoverClass;
    }

    /**
     * Sets the popover CSS class
     *
     * @param strPopoverClass
     *            the popover CSS class
     */
    public void setPopoverClass( String strPopoverClass )
    {
        _strPopoverClass = strPopoverClass;
    }

    /**
     * Driver.js <code>popover.progressText</code>
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
     * Driver.js <code>popover.nextBtnText</code>
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
     * Driver.js <code>popover.prevBtnText</code>
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
     * Driver.js <code>popover.doneBtnText</code>
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
     * Driver.js step <code>disableActiveInteraction</code> as a tri-state value
     *
     * @return {@link #INHERIT}, {@link #NO} or {@link #YES}
     */
    public int getDisableActiveInteraction( )
    {
        return _nDisableActiveInteraction;
    }

    /**
     * Sets the disable active interaction tri-state value
     *
     * @param nDisableActiveInteraction
     *            the tri-state value
     */
    public void setDisableActiveInteraction( int nDisableActiveInteraction )
    {
        _nDisableActiveInteraction = nDisableActiveInteraction;
    }

    /**
     * Driver.js step <code>advanceOnClick</code> as a tri-state value
     *
     * @return {@link #INHERIT}, {@link #NO} or {@link #YES}
     */
    public int getAdvanceOnClick( )
    {
        return _nAdvanceOnClick;
    }

    /**
     * Sets the advance on click tri-state value
     *
     * @param nAdvanceOnClick
     *            the tri-state value
     */
    public void setAdvanceOnClick( int nAdvanceOnClick )
    {
        _nAdvanceOnClick = nAdvanceOnClick;
    }

    /**
     * Driver.js step <code>skipMissingElement</code> as a tri-state value
     *
     * @return {@link #INHERIT}, {@link #NO} or {@link #YES}
     */
    public int getSkipMissingElement( )
    {
        return _nSkipMissingElement;
    }

    /**
     * Sets the skip missing element tri-state value
     *
     * @param nSkipMissingElement
     *            the tri-state value
     */
    public void setSkipMissingElement( int nSkipMissingElement )
    {
        _nSkipMissingElement = nSkipMissingElement;
    }

    /**
     * Driver.js step <code>waitForElement</code> (ms), {@link #INHERIT} to use the tour value
     *
     * @return the wait delay
     */
    public int getWaitForElement( )
    {
        return _nWaitForElement;
    }

    /**
     * Sets the wait for element delay
     *
     * @param nWaitForElement
     *            the wait delay, or {@link #INHERIT}
     */
    public void setWaitForElement( int nWaitForElement )
    {
        _nWaitForElement = nWaitForElement;
    }
}
