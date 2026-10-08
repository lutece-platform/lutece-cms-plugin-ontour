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
 * DAO for {@link Step}
 */
@ApplicationScoped
public class StepDAO implements IStepDAO
{
    private static final String SQL_COLUMNS = "id_tour, step_order, element, title, description, side, align, show_buttons, disable_buttons, "
            + "show_progress, popover_class, progress_label, next_btn_label, prev_btn_label, done_btn_label, disable_active_interaction, "
            + "advance_on_click, skip_missing_element, wait_for_element";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_step, " + SQL_COLUMNS + " FROM ontour_step";
    private static final String SQL_QUERY_SELECT = SQL_QUERY_SELECTALL + " WHERE id_step = ?";
    private static final String SQL_QUERY_SELECT_BY_TOUR = SQL_QUERY_SELECTALL + " WHERE id_tour = ? ORDER BY step_order, id_step";
    private static final String SQL_QUERY_SELECT_MAX_ORDER = "SELECT MAX( step_order ) FROM ontour_step WHERE id_tour = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO ontour_step ( " + SQL_COLUMNS
            + " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_UPDATE = "UPDATE ontour_step SET id_tour = ?, step_order = ?, element = ?, title = ?, description = ?, "
            + "side = ?, align = ?, show_buttons = ?, disable_buttons = ?, show_progress = ?, popover_class = ?, progress_label = ?, "
            + "next_btn_label = ?, prev_btn_label = ?, done_btn_label = ?, disable_active_interaction = ?, advance_on_click = ?, "
            + "skip_missing_element = ?, wait_for_element = ? WHERE id_step = ?";
    private static final String SQL_QUERY_UPDATE_ORDER = "UPDATE ontour_step SET step_order = ? WHERE id_step = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM ontour_step WHERE id_step = ?";
    private static final String SQL_QUERY_DELETE_BY_TOUR = "DELETE FROM ontour_step WHERE id_tour = ?";

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( Step step, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            setColumns( daoUtil, step );
            daoUtil.executeUpdate( );

            if ( daoUtil.nextGeneratedKey( ) )
            {
                step.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( Step step, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = setColumns( daoUtil, step );
            daoUtil.setInt( nIndex, step.getId( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeOrder( int nIdStep, int nOrder, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_ORDER, plugin ) )
        {
            daoUtil.setInt( 1, nOrder );
            daoUtil.setInt( 2, nIdStep );
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
    public void deleteByTour( int nIdTour, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_BY_TOUR, plugin ) )
        {
            daoUtil.setInt( 1, nIdTour );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Step> load( int nId, Plugin plugin )
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
    public List<Step> selectByTour( int nIdTour, Plugin plugin )
    {
        List<Step> listSteps = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_TOUR, plugin ) )
        {
            daoUtil.setInt( 1, nIdTour );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listSteps.add( dataToObject( daoUtil ) );
            }
        }

        return listSteps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int selectMaxOrder( int nIdTour, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_MAX_ORDER, plugin ) )
        {
            daoUtil.setInt( 1, nIdTour );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
        }

        return 0;
    }

    /**
     * Bind the step columns, in the {@link #SQL_COLUMNS} order
     *
     * @param daoUtil
     *            the DAOUtil
     * @param step
     *            the step
     * @return the next parameter index
     */
    private static int setColumns( DAOUtil daoUtil, Step step )
    {
        int nIndex = 1;
        daoUtil.setInt( nIndex++, step.getIdTour( ) );
        daoUtil.setInt( nIndex++, step.getOrder( ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getElement( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getTitle( ) ) );
        daoUtil.setString( nIndex++, step.getDescription( ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getSide( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getAlign( ) ) );
        daoUtil.setString( nIndex++, step.getShowButtons( ) );
        daoUtil.setString( nIndex++, step.getDisableButtons( ) );
        daoUtil.setInt( nIndex++, step.getShowProgress( ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getPopoverClass( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getProgressText( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getNextBtnText( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getPrevBtnText( ) ) );
        daoUtil.setString( nIndex++, nullToEmpty( step.getDoneBtnText( ) ) );
        daoUtil.setInt( nIndex++, step.getDisableActiveInteraction( ) );
        daoUtil.setInt( nIndex++, step.getAdvanceOnClick( ) );
        daoUtil.setInt( nIndex++, step.getSkipMissingElement( ) );
        daoUtil.setInt( nIndex++, step.getWaitForElement( ) );

        return nIndex;
    }

    /**
     * Build a step from the current row
     *
     * @param daoUtil
     *            the DAOUtil positioned on a row
     * @return the step
     */
    private static Step dataToObject( DAOUtil daoUtil )
    {
        int nIndex = 1;
        Step step = new Step( );
        step.setId( daoUtil.getInt( nIndex++ ) );
        step.setIdTour( daoUtil.getInt( nIndex++ ) );
        step.setOrder( daoUtil.getInt( nIndex++ ) );
        step.setElement( daoUtil.getString( nIndex++ ) );
        step.setTitle( daoUtil.getString( nIndex++ ) );
        step.setDescription( daoUtil.getString( nIndex++ ) );
        step.setSide( daoUtil.getString( nIndex++ ) );
        step.setAlign( daoUtil.getString( nIndex++ ) );
        step.setShowButtons( daoUtil.getString( nIndex++ ) );
        step.setDisableButtons( daoUtil.getString( nIndex++ ) );
        step.setShowProgress( daoUtil.getInt( nIndex++ ) );
        step.setPopoverClass( daoUtil.getString( nIndex++ ) );
        step.setProgressText( daoUtil.getString( nIndex++ ) );
        step.setNextBtnText( daoUtil.getString( nIndex++ ) );
        step.setPrevBtnText( daoUtil.getString( nIndex++ ) );
        step.setDoneBtnText( daoUtil.getString( nIndex++ ) );
        step.setDisableActiveInteraction( daoUtil.getInt( nIndex++ ) );
        step.setAdvanceOnClick( daoUtil.getInt( nIndex++ ) );
        step.setSkipMissingElement( daoUtil.getInt( nIndex++ ) );
        step.setWaitForElement( daoUtil.getInt( nIndex ) );

        return step;
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
