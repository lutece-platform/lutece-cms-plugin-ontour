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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * DAO for the state of the tours per user
 */
@ApplicationScoped
public class TourUserStateDAO implements ITourUserStateDAO
{
    private static final String SQL_QUERY_DELETE = "DELETE FROM ontour_user_tour WHERE user_type = ? AND user_id = ? AND tour_code = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO ontour_user_tour ( user_type, user_id, tour_code, status, date_update ) VALUES ( ?, ?, ?, ?, ? )";
    private static final String SQL_QUERY_SELECT_BY_USER = "SELECT tour_code, status FROM ontour_user_tour WHERE user_type = ? AND user_id = ?";
    private static final String SQL_QUERY_COUNT_BY_TOUR = "SELECT tour_code, status, COUNT(*) FROM ontour_user_tour GROUP BY tour_code, status";
    private static final String SQL_QUERY_DELETE_BY_TOUR = "DELETE FROM ontour_user_tour WHERE tour_code = ?";

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( String strUserType, String strUserId, String strTourCode, String strStatus, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, strUserType );
            daoUtil.setString( nIndex++, strUserId );
            daoUtil.setString( nIndex, strTourCode );
            daoUtil.executeUpdate( );
        }

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, strUserType );
            daoUtil.setString( nIndex++, strUserId );
            daoUtil.setString( nIndex++, strTourCode );
            daoUtil.setString( nIndex++, strStatus );
            daoUtil.setTimestamp( nIndex, new Timestamp( System.currentTimeMillis( ) ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> selectByUser( String strUserType, String strUserId, Plugin plugin )
    {
        Map<String, String> mapStates = new HashMap<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_USER, plugin ) )
        {
            daoUtil.setString( 1, strUserType );
            daoUtil.setString( 2, strUserId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                mapStates.put( daoUtil.getString( 1 ), daoUtil.getString( 2 ) );
            }
        }

        return mapStates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Map<String, Integer>> countByTour( Plugin plugin )
    {
        Map<String, Map<String, Integer>> mapCounts = new HashMap<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_COUNT_BY_TOUR, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                mapCounts.computeIfAbsent( daoUtil.getString( 1 ), k -> new HashMap<>( ) ).put( daoUtil.getString( 2 ), daoUtil.getInt( 3 ) );
            }
        }

        return mapCounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByTour( String strTourCode, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_BY_TOUR, plugin ) )
        {
            daoUtil.setString( 1, strTourCode );
            daoUtil.executeUpdate( );
        }
    }
}
