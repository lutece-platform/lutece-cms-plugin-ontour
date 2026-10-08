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

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * DAO for {@link Tour}
 */
@ApplicationScoped
public class TourDAO implements ITourDAO
{
    private static final String SQL_COLUMNS = "code, lang, title, description, target, page_path, page_parameters, page_selector, trigger_mode, show_launcher, is_enabled, "
            + "animate, duration, overlay_color, overlay_opacity, smooth_scroll, allow_close, allow_scroll, overlay_click_behavior, stage_padding, "
            + "stage_radius, disable_active_interaction, advance_on_click, skip_missing_element, wait_for_element, allow_keyboard_control, "
            + "popover_class, popover_offset, show_buttons, disable_buttons, show_progress, progress_label, next_btn_label, prev_btn_label, done_btn_label";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_tour, " + SQL_COLUMNS + " FROM ontour_tour";
    private static final String SQL_QUERY_SELECT = SQL_QUERY_SELECTALL + " WHERE id_tour = ?";
    private static final String SQL_QUERY_SELECT_BY_CODE = SQL_QUERY_SELECTALL + " WHERE code = ? ORDER BY lang";
    private static final String SQL_QUERY_SELECT_BY_CODE_AND_LANG = SQL_QUERY_SELECTALL + " WHERE code = ? AND lang = ?";
    private static final String SQL_QUERY_SELECT_ENABLED_BY_TARGET = SQL_QUERY_SELECTALL + " WHERE target = ? AND is_enabled = 1 ORDER BY title, lang";
    private static final String SQL_QUERY_ORDER_ALL = " ORDER BY target, code, lang";
    private static final String SQL_QUERY_INSERT = "INSERT INTO ontour_tour ( " + SQL_COLUMNS
            + " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_UPDATE = "UPDATE ontour_tour SET code = ?, lang = ?, title = ?, description = ?, target = ?, page_path = ?, "
            + "page_parameters = ?, page_selector = ?, trigger_mode = ?, show_launcher = ?, is_enabled = ?, animate = ?, duration = ?, overlay_color = ?, "
            + "overlay_opacity = ?, smooth_scroll = ?, allow_close = ?, allow_scroll = ?, overlay_click_behavior = ?, stage_padding = ?, "
            + "stage_radius = ?, disable_active_interaction = ?, advance_on_click = ?, skip_missing_element = ?, wait_for_element = ?, "
            + "allow_keyboard_control = ?, popover_class = ?, popover_offset = ?, show_buttons = ?, disable_buttons = ?, show_progress = ?, "
            + "progress_label = ?, next_btn_label = ?, prev_btn_label = ?, done_btn_label = ? WHERE id_tour = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM ontour_tour WHERE id_tour = ?";

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( Tour tour, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            setColumns( daoUtil, tour );
            daoUtil.executeUpdate( );

            if ( daoUtil.nextGeneratedKey( ) )
            {
                tour.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( Tour tour, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = setColumns( daoUtil, tour );
            daoUtil.setInt( nIndex, tour.getId( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tour> load( int nId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nId );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return Optional.of( dataToObject( daoUtil ) );
            }
        }

        return Optional.empty( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tour> loadByCodeAndLang( String strCode, String strLang, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CODE_AND_LANG, plugin ) )
        {
            daoUtil.setString( 1, strCode );
            daoUtil.setString( 2, nullToEmpty( strLang ) );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return Optional.of( dataToObject( daoUtil ) );
            }
        }

        return Optional.empty( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tour> selectByCode( String strCode, Plugin plugin )
    {
        List<Tour> listTours = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CODE, plugin ) )
        {
            daoUtil.setString( 1, strCode );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listTours.add( dataToObject( daoUtil ) );
            }
        }

        return listTours;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tour> selectAll( Plugin plugin )
    {
        List<Tour> listTours = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL + SQL_QUERY_ORDER_ALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listTours.add( dataToObject( daoUtil ) );
            }
        }

        return listTours;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tour> selectEnabledByTarget( String strTarget, Plugin plugin )
    {
        List<Tour> listTours = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ENABLED_BY_TARGET, plugin ) )
        {
            daoUtil.setString( 1, strTarget );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listTours.add( dataToObject( daoUtil ) );
            }
        }

        return listTours;
    }

    /**
     * Bind the tour columns, in the {@link #SQL_COLUMNS} order
     *
     * @param daoUtil
     *            the DAOUtil
     * @param tour
     *            the tour
     * @return the next parameter index
     */
    private static int setColumns( DAOUtil daoUtil, Tour tour )
    {
        int nIndex = 1;
        daoUtil.setString( nIndex++, tour.getCode( ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getLang( ) ) );
        daoUtil.setString( nIndex++, tour.getTitle( ) );
        daoUtil.setString( nIndex++, tour.getDescription( ) );
        daoUtil.setString( nIndex++, tour.getTarget( ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getPagePath( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getPageParameters( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getPageSelector( ) ) );
        daoUtil.setString( nIndex++, tour.getTriggerMode( ) );
        daoUtil.setBoolean( nIndex++, tour.isShowLauncher( ) );
        daoUtil.setBoolean( nIndex++, tour.isEnabled( ) );
        daoUtil.setBoolean( nIndex++, tour.isAnimate( ) );
        daoUtil.setInt( nIndex++, tour.getDuration( ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getOverlayColor( ) ) );
        daoUtil.setInt( nIndex++, tour.getOverlayOpacity( ) );
        daoUtil.setBoolean( nIndex++, tour.isSmoothScroll( ) );
        daoUtil.setBoolean( nIndex++, tour.isAllowClose( ) );
        daoUtil.setBoolean( nIndex++, tour.isAllowScroll( ) );
        daoUtil.setString( nIndex++, tour.getOverlayClickBehavior( ) );
        daoUtil.setInt( nIndex++, tour.getStagePadding( ) );
        daoUtil.setInt( nIndex++, tour.getStageRadius( ) );
        daoUtil.setBoolean( nIndex++, tour.isDisableActiveInteraction( ) );
        daoUtil.setBoolean( nIndex++, tour.isAdvanceOnClick( ) );
        daoUtil.setBoolean( nIndex++, tour.isSkipMissingElement( ) );
        daoUtil.setInt( nIndex++, tour.getWaitForElement( ) );
        daoUtil.setBoolean( nIndex++, tour.isAllowKeyboardControl( ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getPopoverClass( ) ) );
        daoUtil.setInt( nIndex++, tour.getPopoverOffset( ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getShowButtons( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getDisableButtons( ) ) );
        daoUtil.setBoolean( nIndex++, tour.isShowProgress( ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getProgressText( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getNextBtnText( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getPrevBtnText( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( tour.getDoneBtnText( ) ) );

        return nIndex;
    }

    /**
     * Build a tour from the current row
     *
     * @param daoUtil
     *            the DAOUtil positioned on a row
     * @return the tour
     */
    private static Tour dataToObject( DAOUtil daoUtil )
    {
        int nIndex = 1;
        Tour tour = new Tour( );
        tour.setId( daoUtil.getInt( nIndex++ ) );
        tour.setCode( daoUtil.getString( nIndex++ ) );
        tour.setLang( daoUtil.getString( nIndex++ ) );
        tour.setTitle( daoUtil.getString( nIndex++ ) );
        tour.setDescription( daoUtil.getString( nIndex++ ) );
        tour.setTarget( daoUtil.getString( nIndex++ ) );
        tour.setPagePath( daoUtil.getString( nIndex++ ) );
        tour.setPageParameters( daoUtil.getString( nIndex++ ) );
        tour.setPageSelector( daoUtil.getString( nIndex++ ) );
        tour.setTriggerMode( daoUtil.getString( nIndex++ ) );
        tour.setShowLauncher( daoUtil.getBoolean( nIndex++ ) );
        tour.setEnabled( daoUtil.getBoolean( nIndex++ ) );
        tour.setAnimate( daoUtil.getBoolean( nIndex++ ) );
        tour.setDuration( daoUtil.getInt( nIndex++ ) );
        tour.setOverlayColor( daoUtil.getString( nIndex++ ) );
        tour.setOverlayOpacity( daoUtil.getInt( nIndex++ ) );
        tour.setSmoothScroll( daoUtil.getBoolean( nIndex++ ) );
        tour.setAllowClose( daoUtil.getBoolean( nIndex++ ) );
        tour.setAllowScroll( daoUtil.getBoolean( nIndex++ ) );
        tour.setOverlayClickBehavior( daoUtil.getString( nIndex++ ) );
        tour.setStagePadding( daoUtil.getInt( nIndex++ ) );
        tour.setStageRadius( daoUtil.getInt( nIndex++ ) );
        tour.setDisableActiveInteraction( daoUtil.getBoolean( nIndex++ ) );
        tour.setAdvanceOnClick( daoUtil.getBoolean( nIndex++ ) );
        tour.setSkipMissingElement( daoUtil.getBoolean( nIndex++ ) );
        tour.setWaitForElement( daoUtil.getInt( nIndex++ ) );
        tour.setAllowKeyboardControl( daoUtil.getBoolean( nIndex++ ) );
        tour.setPopoverClass( daoUtil.getString( nIndex++ ) );
        tour.setPopoverOffset( daoUtil.getInt( nIndex++ ) );
        tour.setShowButtons( daoUtil.getString( nIndex++ ) );
        tour.setDisableButtons( daoUtil.getString( nIndex++ ) );
        tour.setShowProgress( daoUtil.getBoolean( nIndex++ ) );
        tour.setProgressText( daoUtil.getString( nIndex++ ) );
        tour.setNextBtnText( daoUtil.getString( nIndex++ ) );
        tour.setPrevBtnText( daoUtil.getString( nIndex++ ) );
        tour.setDoneBtnText( daoUtil.getString( nIndex ) );

        return tour;
    }

    /**
     * Replace a null string by an empty one, for NOT NULL columns
     *
     * @param strValue
     *            the value
     * @return the value or an empty string
     */
    private static String nullToEmpty( String strValue )
    {
        return ( strValue != null ) ? strValue : "";
    }
}
