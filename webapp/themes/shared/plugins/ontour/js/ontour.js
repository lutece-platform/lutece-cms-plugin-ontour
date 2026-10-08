/*
 * onTour - guided tours for Lutece pages, powered by Driver.js (window.driver.js.driver).
 *
 * Loaded on every back office page (plugin.xml <admin-javascript-files>) and every front office page
 * (<javascript-files> with the portal scope). The target is given by the "target" parameter of the script URL.
 *
 * - Tours whose page path matches the current page are fetched from servlet/plugins/ontour/tours.
 *   "first_visit" tours start until the user finishes or closes them, "always" tours start on each visit, "manual" tours wait for the launcher.
 *   The finished / closed state is stored on the user account (BO administrator, connected FO user), in the browser for anonymous visitors.
 * - A floating launcher button lists the tours of the page that enable it. Its position (a corner of the screen, or hidden) is a
 *   site property, one for the back office and one for the front office (Site properties > Guided tour configuration).
 * - Any element carrying data-ontour-start="<tour code>" starts that tour when clicked, so a plugin can offer
 *   a "Take the tour" button without depending on onTour: the attribute is inert when onTour is not installed.
 * - ?ontour=<tour code> in the page URL starts that tour right away (used by the "Test" button of the admin feature).
 * - The tour translation follows the language of the user (?ontour_lang=<lang> forces it, for the "Test" button).
 * - window.LuteceOnTour exposes start( code, overrides, lang ), startConfig( config ), reset( code ), refresh( ).
 */
( () => {
    'use strict';

    const ENDPOINT = 'servlet/plugins/ontour/tours';
    const STORAGE_PREFIX = 'lutece.ontour.seen.';
    const TEST_PARAMETER = 'ontour';
    const TEST_LANG_PARAMETER = 'ontour_lang';
    const TRIGGER_FIRST_VISIT = 'first_visit';
    const TRIGGER_ALWAYS = 'always';

    const script = document.currentScript;
    const scriptParams = script ? new URL( script.src, document.baseURI ).searchParams : new URLSearchParams( );
    const target = ( scriptParams.get( 'target' ) || ( window.location.pathname.includes( '/jsp/admin/' ) ? 'BO' : 'FO' ) ).toUpperCase( );

    const state = {
        tours: [],
        labels: {},
        byCode: new Map( ),
        active: null,
        launcher: null,
        launcherPosition: 'bottom_right',
        persistent: false
    };

    /**
     * Return the Driver.js factory, or null when the library is not loaded.
     */
    const driverFactory = ( ) => ( window.driver && window.driver.js && window.driver.js.driver ) || null;

    /**
     * Path of the current page relative to the webapp base (the <base href> set by Lutece).
     */
    const relativePath = ( ) => {
        const basePath = new URL( document.baseURI ).pathname;
        const path = window.location.pathname;
        return ( path.startsWith( basePath ) ? path.substring( basePath.length ) : path ).replace( /^\/+/, '' );
    };

    /**
     * Query string of the current page, without the test parameter.
     */
    const relativeQuery = ( ) => {
        const params = new URLSearchParams( window.location.search );
        params.delete( TEST_PARAMETER );
        params.delete( TEST_LANG_PARAMETER );
        return params.toString( );
    };

    /**
     * Call the onTour endpoint.
     */
    const fetchTours = async ( params ) => {
        const url = new URL( ENDPOINT, document.baseURI );
        url.searchParams.set( 'target', target );
        Object.entries( params ).forEach( ( [ key, value ] ) => url.searchParams.set( key, value ) );
        const response = await fetch( url, { credentials: 'same-origin', headers: { Accept: 'application/json' } } );
        if ( !response.ok ) {
            throw new Error( `onTour: HTTP ${response.status}` );
        }
        const body = await response.text( );
        if ( !body.trim( ) ) {
            /* the core answers with an empty body when the onTour plugin is disabled: no tour, no error */
            return [];
        }
        let data;
        try {
            data = JSON.parse( body );
        } catch ( e ) {
            throw new Error( 'onTour: unexpected answer from the tours endpoint' );
        }
        state.labels = Object.assign( {}, state.labels, data.labels || {} );
        state.persistent = data.persistent === true;
        if ( typeof data.launcherPosition === 'string' ) {
            state.launcherPosition = data.launcherPosition;
        }
        applyStyle( data.style );
        ( data.tours || [] ).forEach( ( tour ) => state.byCode.set( tour.code, tour ) );
        return data.tours || [];
    };

    /**
     * Apply the look chosen in the site properties: values of the --ontour-* CSS custom properties, checked by the server.
     */
    const applyStyle = ( style ) => {
        if ( !style || typeof style !== 'object' ) {
            return;
        }
        Object.entries( style )
            .filter( ( [ name ] ) => name.startsWith( '--ontour-' ) )
            .forEach( ( [ name, value ] ) => document.documentElement.style.setProperty( name, String( value ) ) );
    };

    const storageKey = ( code ) => STORAGE_PREFIX + target + '.' + code;

    const isSeen = ( code ) => {
        try {
            return window.localStorage.getItem( storageKey( code ) ) !== null;
        } catch ( e ) {
            return false;
        }
    };

    /**
     * Remember that a tour was finished or closed: in the browser, and on the user account when the user is known
     * (back office administrator, connected site user), so that the state is kept across browsers and sessions.
     */
    const markSeen = ( code, done ) => {
        try {
            window.localStorage.setItem( storageKey( code ), new Date( ).toISOString( ) );
        } catch ( e ) {
            /* storage unavailable: the account state, when there is one, still applies */
        }
        const body = new URLSearchParams( { target, code, status: done ? 'done' : 'closed' } );
        fetch( new URL( ENDPOINT, document.baseURI ), {
            method: 'POST',
            credentials: 'same-origin',
            keepalive: true,
            headers: { 'X-OnTour': '1' },
            body
        } ).catch( ( e ) => console.warn( 'onTour: unable to record the state of the tour', e ) );
    };

    /**
     * Tell whether a tour was already finished or closed by the user: the account state when the user is known,
     * the browser state for an anonymous visitor.
     */
    const wasSeen = ( tour ) => ( state.persistent ? Boolean( tour.seen ) : isSeen( tour.code ) );

    /**
     * Translate the JSON configuration into a Driver.js configuration.
     * "none" as overlay click behavior means that a click on the overlay does nothing.
     */
    const toDriverConfig = ( config, overrides ) => {
        const driverConfig = Object.assign( {}, config, overrides || {} );
        if ( driverConfig.overlayClickBehavior === 'none' ) {
            driverConfig.overlayClickBehavior = ( ) => {};
        }
        return driverConfig;
    };

    /**
     * Start a Driver.js tour from a raw configuration.
     */
    const startConfig = ( config, onDestroyed ) => {
        const factory = driverFactory( );
        if ( !factory ) {
            console.warn( 'onTour: Driver.js is not loaded' );
            return null;
        }
        if ( state.active && state.active.isActive( ) ) {
            state.active.destroy( );
        }
        const userOnDestroyed = config.onDestroyed;
        const userOnHighlighted = config.onHighlighted;
        let reachedEnd = false;
        const driverObj = factory( Object.assign( {}, config, {
            onHighlighted: ( element, step, opts ) => {
                if ( opts && opts.driver && opts.driver.isLastStep( ) ) {
                    reachedEnd = true;
                }
                if ( typeof userOnHighlighted === 'function' ) {
                    userOnHighlighted( element, step, opts );
                }
            },
            onDestroyed: ( element, step, opts ) => {
                state.active = null;
                if ( onDestroyed ) {
                    onDestroyed( reachedEnd );
                }
                if ( typeof userOnDestroyed === 'function' ) {
                    userOnDestroyed( element, step, opts );
                }
            }
        } ) );
        state.active = driverObj;
        driverObj.drive( );
        return driverObj;
    };

    /**
     * Start a tour already fetched.
     */
    const startTour = ( tour, overrides ) => {
        if ( !tour || !tour.config || !Array.isArray( tour.config.steps ) || tour.config.steps.length === 0 ) {
            return null;
        }
        return startConfig( toDriverConfig( tour.config, overrides ), ( done ) => {
            tour.seen = done ? 'done' : 'closed';
            markSeen( tour.code, done );
        } );
    };

    /**
     * Start a tour by its code, fetching it when it is not known yet.
     */
    const start = async ( code, overrides, lang ) => {
        let tour = lang ? null : state.byCode.get( code );
        if ( !tour ) {
            const tours = await fetchTours( lang ? { code, lang } : { code } );
            tour = tours[ 0 ];
        }
        if ( !tour ) {
            console.warn( `onTour: unknown or disabled tour "${code}"` );
            return null;
        }
        return startTour( tour, overrides );
    };

    /**
     * Close the launcher menu.
     */
    const closeMenu = ( ) => {
        if ( !state.launcher ) {
            return;
        }
        const menu = state.launcher.querySelector( '.ontour-launcher-menu' );
        const button = state.launcher.querySelector( '.ontour-launcher-button' );
        menu.hidden = true;
        button.setAttribute( 'aria-expanded', 'false' );
    };

    /**
     * Render the floating launcher for the tours that enable it.
     */
    const renderLauncher = ( tours ) => {
        if ( state.launcher ) {
            state.launcher.remove( );
            state.launcher = null;
        }
        const launchable = tours.filter( ( tour ) => tour.launcher );
        if ( launchable.length === 0 || state.launcherPosition === 'hidden' ) {
            return;
        }

        const container = document.createElement( 'div' );
        container.className = `ontour-launcher ontour-launcher--${state.launcherPosition.replace( /_/g, '-' )}`;

        const button = document.createElement( 'button' );
        button.type = 'button';
        button.className = 'ontour-launcher-button';
        button.setAttribute( 'aria-haspopup', 'true' );
        button.setAttribute( 'aria-expanded', 'false' );
        button.title = state.labels.launcher || 'Guided tour';
        button.setAttribute( 'aria-label', button.title );
        button.innerHTML = '<span aria-hidden="true">?</span>';
        container.appendChild( button );

        const menu = document.createElement( 'div' );
        menu.className = 'ontour-launcher-menu';
        menu.hidden = true;
        menu.setAttribute( 'role', 'menu' );

        const title = document.createElement( 'p' );
        title.className = 'ontour-launcher-title';
        title.textContent = state.labels.menuTitle || '';
        menu.appendChild( title );

        launchable.forEach( ( tour ) => {
            const item = document.createElement( 'button' );
            item.type = 'button';
            item.className = 'ontour-launcher-item';
            item.setAttribute( 'role', 'menuitem' );
            item.textContent = tour.title;
            item.addEventListener( 'click', ( ) => {
                closeMenu( );
                startTour( tour );
            } );
            menu.appendChild( item );
        } );
        container.appendChild( menu );

        button.addEventListener( 'click', ( ) => {
            if ( launchable.length === 1 ) {
                startTour( launchable[ 0 ] );
                return;
            }
            const open = menu.hidden;
            menu.hidden = !open;
            button.setAttribute( 'aria-expanded', String( open ) );
        } );

        document.body.appendChild( container );
        state.launcher = container;
    };

    /**
     * Start the first tour that should start automatically.
     */
    const autoStart = ( tours ) => {
        const pageParams = new URLSearchParams( window.location.search );
        const testCode = pageParams.get( TEST_PARAMETER );
        if ( testCode ) {
            start( testCode, null, pageParams.get( TEST_LANG_PARAMETER ) ).catch( ( e ) => console.warn( e ) );
            return;
        }
        const tour = tours.find( ( t ) => t.trigger === TRIGGER_ALWAYS || ( t.trigger === TRIGGER_FIRST_VISIT && !wasSeen( t ) ) );
        if ( tour ) {
            startTour( tour );
        }
    };

    /**
     * Tell whether the page contains the element required by a tour (its "page selector").
     * It distinguishes screens sharing the same URL, such as views reached by a POST form.
     */
    const isApplicable = ( tour ) => {
        if ( !tour.requires ) {
            return true;
        }
        try {
            return document.querySelector( tour.requires ) !== null;
        } catch ( e ) {
            console.warn( `onTour: invalid page selector for tour "${tour.code}"`, e );
            return false;
        }
    };

    /**
     * Load the tours of the current page, render the launcher and start the automatic tour.
     */
    const refresh = async ( ) => {
        try {
            state.tours = ( await fetchTours( { path: relativePath( ), query: relativeQuery( ) } ) ).filter( isApplicable );
        } catch ( e ) {
            state.tours = [];
            console.warn( e );
        }
        renderLauncher( state.tours );
        autoStart( state.tours );
    };

    document.addEventListener( 'keydown', ( event ) => {
        if ( event.key === 'Escape' ) {
            closeMenu( );
        }
    } );

    document.addEventListener( 'click', ( event ) => {
        if ( state.launcher && !state.launcher.contains( event.target ) ) {
            closeMenu( );
        }
        const trigger = event.target.closest( '[data-ontour-start]' );
        if ( !trigger ) {
            return;
        }
        event.preventDefault( );
        start( trigger.getAttribute( 'data-ontour-start' ) ).catch( ( e ) => console.warn( e ) );
    } );

    window.LuteceOnTour = {
        target,
        start,
        startConfig: ( config ) => startConfig( toDriverConfig( config ) ),
        reset: ( code ) => {
            try {
                if ( code ) {
                    window.localStorage.removeItem( storageKey( code ) );
                } else {
                    Object.keys( window.localStorage ).filter( ( key ) => key.startsWith( STORAGE_PREFIX ) ).forEach( ( key ) => window.localStorage.removeItem( key ) );
                }
            } catch ( e ) {
                /* storage unavailable */
            }
        },
        refresh
    };

    if ( document.readyState === 'loading' ) {
        document.addEventListener( 'DOMContentLoaded', refresh );
    } else {
        refresh( );
    }
} )( );
