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

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.ontour.business.Tour;

/**
 * Tests of {@link LauncherPositionService}
 */
class LauncherPositionServiceTest
{
    /**
     * Stored values are mapped on a known position
     */
    @Test
    void testNormalize( )
    {
        assertEquals( LauncherPositionService.POSITION_TOP_LEFT, LauncherPositionService.normalize( "top_left" ) );
        assertEquals( LauncherPositionService.POSITION_TOP_LEFT, LauncherPositionService.normalize( " Top-Left " ) );
        assertEquals( LauncherPositionService.POSITION_HIDDEN, LauncherPositionService.normalize( "hidden" ) );
        assertEquals( LauncherPositionService.POSITION_BOTTOM_CENTER, LauncherPositionService.normalize( "bottom-center" ) );
        assertEquals( LauncherPositionService.POSITION_TOP_CENTER, LauncherPositionService.normalize( "top_center" ) );
        assertEquals( LauncherPositionService.POSITION_BOTTOM_RIGHT, LauncherPositionService.normalize( "middle" ) );
        assertEquals( LauncherPositionService.POSITION_BOTTOM_RIGHT, LauncherPositionService.normalize( null ) );
    }

    /**
     * One datastore key per target, under the onTour site properties prefix
     */
    @Test
    void testKeys( )
    {
        assertEquals( "ontour.site_property.launcher.bo.select", LauncherPositionService.getKey( Tour.TARGET_BO ) );
        assertEquals( "ontour.site_property.launcher.fo.select", LauncherPositionService.getKey( Tour.TARGET_FO ) );
    }
}
