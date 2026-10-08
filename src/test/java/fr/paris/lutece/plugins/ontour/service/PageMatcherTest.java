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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.ontour.business.Tour;

/**
 * Tests of {@link PageMatcher}
 */
class PageMatcherTest
{
    private static final String MANAGE_BLOGS = "jsp/admin/plugins/blog/ManageBlogs.jsp";

    /**
     * Path matching, with and without wildcard
     */
    @Test
    void testMatchesPath( )
    {
        assertTrue( PageMatcher.matchesPath( MANAGE_BLOGS, MANAGE_BLOGS ) );
        assertTrue( PageMatcher.matchesPath( "/" + MANAGE_BLOGS, MANAGE_BLOGS ) );
        assertTrue( PageMatcher.matchesPath( MANAGE_BLOGS, "/" + MANAGE_BLOGS + ";jsessionid=ABC" ) );
        assertTrue( PageMatcher.matchesPath( "jsp/admin/plugins/blog/*", MANAGE_BLOGS ) );
        assertTrue( PageMatcher.matchesPath( "*ManageBlogs.jsp", MANAGE_BLOGS ) );
        assertFalse( PageMatcher.matchesPath( "jsp/admin/plugins/blog/ManageTags.jsp", MANAGE_BLOGS ) );
        assertFalse( PageMatcher.matchesPath( "jsp/admin/plugins/blog", MANAGE_BLOGS ) );
        assertFalse( PageMatcher.matchesPath( "jsp/admin/plugins/blog/Manage.Blogs.jsp", "jsp/admin/plugins/blog/ManageXBlogs.jsp" ) );
        assertFalse( PageMatcher.matchesPath( "", MANAGE_BLOGS ) );
        assertFalse( PageMatcher.matchesPath( null, MANAGE_BLOGS ) );
    }

    /**
     * Required parameters matching
     */
    @Test
    void testMatchesParameters( )
    {
        assertTrue( PageMatcher.matchesParameters( "", "view=createBlog" ) );
        assertTrue( PageMatcher.matchesParameters( null, null ) );
        assertTrue( PageMatcher.matchesParameters( "view=createBlog", "view=createBlog&id=3" ) );
        assertTrue( PageMatcher.matchesParameters( "?view=createBlog", "?id=3&view=createBlog" ) );
        assertFalse( PageMatcher.matchesParameters( "view=createBlog", "view=modifyBlog" ) );
        assertFalse( PageMatcher.matchesParameters( "view=createBlog", null ) );
        assertTrue( PageMatcher.matchesParameters( "id", "view=modifyBlog&id=3" ) );
        assertTrue( PageMatcher.matchesParameters( "id=*", "view=modifyBlog&id=3" ) );
        assertFalse( PageMatcher.matchesParameters( "id", "view=modifyBlog" ) );
        assertTrue( PageMatcher.matchesParameters( "page=blog&view=*", "page=blog&view=documentDetails&id=1" ) );
        assertTrue( PageMatcher.matchesParameters( "title=a%20b", "title=a+b" ) );
    }

    /**
     * Alternatives and absent parameters
     */
    @Test
    void testAlternativesAndAbsence( )
    {
        assertTrue( PageMatcher.matchesParameters( "view=manageBlogs|!", "" ) );
        assertTrue( PageMatcher.matchesParameters( "view=manageBlogs|!", "view=manageBlogs" ) );
        assertFalse( PageMatcher.matchesParameters( "view=manageBlogs|!", "view=createBlog" ) );
        assertTrue( PageMatcher.matchesParameters( "view=createBlog|modifyBlog", "view=modifyBlog" ) );
        assertTrue( PageMatcher.matchesParameters( "!view", "plugin_name=blog" ) );
        assertFalse( PageMatcher.matchesParameters( "!view", "view=createBlog" ) );
    }

    /**
     * Whole tour matching
     */
    @Test
    void testMatches( )
    {
        Tour tour = new Tour( );
        tour.setPagePath( "jsp/site/Portal.jsp" );
        tour.setPageParameters( "page=blog" );

        assertTrue( PageMatcher.matches( tour, "jsp/site/Portal.jsp", "page=blog&id=12" ) );
        assertFalse( PageMatcher.matches( tour, "jsp/site/Portal.jsp", "page_id=3" ) );
        assertFalse( PageMatcher.matches( tour, "jsp/admin/AdminMenu.jsp", "page=blog" ) );
    }

    /**
     * Query string parsing
     */
    @Test
    void testParseQueryString( )
    {
        assertEquals( 2, PageMatcher.parseQueryString( "a=1&a=2&b" ).get( "a" ).size( ) );
        assertEquals( "", PageMatcher.parseQueryString( "a=1&a=2&b" ).get( "b" ).get( 0 ) );
        assertTrue( PageMatcher.parseQueryString( "" ).isEmpty( ) );
    }
}
