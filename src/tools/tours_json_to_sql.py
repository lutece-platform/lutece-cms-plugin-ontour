#!/usr/bin/env python3
"""
Convert an onTour JSON document (export format) into a Liquibase formatted SQL script.

Usage: python3 src/tools/tours_json_to_sql.py <changeset id> <tours.json> [<tours.json>...] > <script.sql>

Only the values that differ from the column defaults are written. Steps reference their tour by code and language
(INSERT ... SELECT), so the script does not depend on the generated identifiers.
"""
import json
import re
import sys

COLUMNS = {
    'lang': 'lang', 'title': 'title', 'description': 'description', 'target': 'target',
    'pagePath': 'page_path', 'pageParameters': 'page_parameters', 'pageSelector': 'page_selector',
    'triggerMode': 'trigger_mode', 'showLauncher': 'show_launcher', 'enabled': 'is_enabled',
    'animate': 'animate', 'duration': 'duration', 'overlayColor': 'overlay_color', 'overlayOpacity': 'overlay_opacity',
    'smoothScroll': 'smooth_scroll', 'allowClose': 'allow_close', 'allowScroll': 'allow_scroll',
    'overlayClickBehavior': 'overlay_click_behavior', 'stagePadding': 'stage_padding', 'stageRadius': 'stage_radius',
    'disableActiveInteraction': 'disable_active_interaction', 'advanceOnClick': 'advance_on_click',
    'skipMissingElement': 'skip_missing_element', 'waitForElement': 'wait_for_element',
    'allowKeyboardControl': 'allow_keyboard_control', 'popoverClass': 'popover_class', 'popoverOffset': 'popover_offset',
    'showButtons': 'show_buttons', 'disableButtons': 'disable_buttons', 'showProgress': 'show_progress',
    'progressText': 'progress_label', 'nextBtnText': 'next_btn_label', 'prevBtnText': 'prev_btn_label',
    'doneBtnText': 'done_btn_label',
}
STEP_COLUMNS = {
    'element': 'element', 'title': 'title', 'description': 'description', 'side': 'side', 'align': 'align',
    'showButtons': 'show_buttons', 'disableButtons': 'disable_buttons', 'showProgress': 'show_progress',
    'popoverClass': 'popover_class', 'progressText': 'progress_label', 'nextBtnText': 'next_btn_label',
    'prevBtnText': 'prev_btn_label', 'doneBtnText': 'done_btn_label',
    'disableActiveInteraction': 'disable_active_interaction', 'advanceOnClick': 'advance_on_click',
    'skipMissingElement': 'skip_missing_element', 'waitForElement': 'wait_for_element',
}


def literal( value ):
    """SQL literal of a JSON value"""
    if isinstance( value, bool ):
        return '1' if value else '0'
    if isinstance( value, int ):
        return str( value )
    return "'" + str( value ).replace( "'", "''" ) + "'"


def main( ):
    changeset = sys.argv[ 1 ]
    tours = [ ]
    for path in sys.argv[ 2: ]:
        tours.extend( json.load( open( path, encoding = 'utf-8' ) )[ 'tours' ] )
    lines = [ '-- liquibase formatted sql', '-- changeset ontour:' + changeset, '-- preconditions onFail:MARK_RAN onError:WARN', '' ]

    for tour in tours:
        code = tour[ 'code' ]
        lang = tour.get( 'lang', '' )
        where = 'code = %s AND lang = %s' % ( literal( code ), literal( lang ) )
        lines.append( '--' )
        lines.append( '-- Tour %s (%s)' % ( code, lang or 'all languages' ) )
        lines.append( '--' )
        lines.append( 'DELETE FROM ontour_step WHERE id_tour IN ( SELECT id_tour FROM ontour_tour WHERE %s );' % where )
        lines.append( 'DELETE FROM ontour_tour WHERE %s;' % where )
        columns = [ 'code' ] + [ COLUMNS[ k ] for k in tour if k in COLUMNS ]
        values = [ literal( code ) ] + [ literal( tour[ k ] ) for k in tour if k in COLUMNS ]
        if 'lang' not in tour:
            columns.append( 'lang' )
            values.append( "''" )
        lines.append( 'INSERT INTO ontour_tour ( %s ) VALUES ( %s );' % ( ', '.join( columns ), ', '.join( values ) ) )

        for order, step in enumerate( tour.get( 'steps', [ ] ), start = 1 ):
            step_columns = [ 'id_tour', 'step_order' ] + [ STEP_COLUMNS[ k ] for k in step if k in STEP_COLUMNS ]
            step_values = [ 'id_tour', str( order ) ] + [ literal( step[ k ] ) for k in step if k in STEP_COLUMNS ]
            lines.append( 'INSERT INTO ontour_step ( %s ) SELECT %s FROM ontour_tour WHERE %s;'
                          % ( ', '.join( step_columns ), ', '.join( step_values ), where ) )
        lines.append( '' )

    sys.stdout.write( '\n'.join( lines ) )


if __name__ == '__main__':
    main( )
