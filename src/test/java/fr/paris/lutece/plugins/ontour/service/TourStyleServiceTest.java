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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link TourStyleService}
 */
class TourStyleServiceTest
{
    /**
     * Accepted and refused colors
     */
    @Test
    void testColor( )
    {
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_COLOR, "#1f5fbf" ) );
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_COLOR, "#fff" ) );
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_COLOR, "rgb( 31, 95, 191 )" ) );
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_COLOR, "hsl(215 72% 44% / .9)" ) );
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_COLOR_CONTRAST, "white" ) );
        assertFalse( TourStyleService.isValid( TourStyleService.SETTING_COLOR, "red; } body { display: none" ) );
        assertFalse( TourStyleService.isValid( TourStyleService.SETTING_COLOR, "url(javascript:alert(1))" ) );
        assertFalse( TourStyleService.isValid( TourStyleService.SETTING_COLOR, "" ) );
    }

    /**
     * One datastore key per target and setting
     */
    @Test
    void testKeys( )
    {
        org.junit.jupiter.api.Assertions.assertEquals( "ontour.site_property.style.bo.color",
                TourStyleService.getKey( fr.paris.lutece.plugins.ontour.business.Tour.TARGET_BO, TourStyleService.SETTING_COLOR ) );
        org.junit.jupiter.api.Assertions.assertEquals( "ontour.site_property.style.fo.z_index",
                TourStyleService.getKey( fr.paris.lutece.plugins.ontour.business.Tour.TARGET_FO, TourStyleService.SETTING_Z_INDEX ) );
    }

    /**
     * Accepted and refused lengths and stacking levels
     */
    @Test
    void testLengthAndZIndex( )
    {
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_OFFSET, "1.25rem" ) );
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_OFFSET, "24px" ) );
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_OFFSET, "0" ) );
        assertFalse( TourStyleService.isValid( TourStyleService.SETTING_OFFSET, "12" ) );
        assertFalse( TourStyleService.isValid( TourStyleService.SETTING_OFFSET, "calc(100% - 1px)" ) );
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_Z_INDEX, "1040" ) );
        assertTrue( TourStyleService.isValid( TourStyleService.SETTING_Z_INDEX, "-1" ) );
        assertFalse( TourStyleService.isValid( TourStyleService.SETTING_Z_INDEX, "10.5" ) );
        assertFalse( TourStyleService.isValid( "unknown", "1" ) );
    }
}
