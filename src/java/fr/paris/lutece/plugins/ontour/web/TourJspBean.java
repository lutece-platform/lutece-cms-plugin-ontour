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
package fr.paris.lutece.plugins.ontour.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.plugins.ontour.business.Step;
import fr.paris.lutece.plugins.ontour.business.Tour;
import fr.paris.lutece.plugins.ontour.service.LauncherPositionService;
import fr.paris.lutece.plugins.ontour.service.PageMatcher;
import fr.paris.lutece.plugins.ontour.service.TourImportException;
import fr.paris.lutece.plugins.ontour.service.TourResourceIdService;
import fr.paris.lutece.plugins.ontour.service.TourJsonService;
import fr.paris.lutece.plugins.ontour.service.TourService;
import fr.paris.lutece.plugins.ontour.service.TourUserStateService;
import fr.paris.lutece.api.user.User;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.binding.BindingResult;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.ModelAttribute;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.utils.MVCUtils;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import fr.paris.lutece.portal.web.util.IPager;
import fr.paris.lutece.portal.web.util.Pager;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.url.UrlItem;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Back office management of the tours and of their steps
 */
@RequestScoped
@Named( "tourJspBean" )
@Controller( controllerJsp = "ManageTours.jsp", controllerPath = "jsp/admin/plugins/ontour/", right = TourJspBean.RIGHT_MANAGE_TOURS, securityTokenEnabled = true )
public class TourJspBean extends MVCAdminJspBean
{
    public static final String RIGHT_MANAGE_TOURS = "ONTOUR_MANAGEMENT";

    private static final long serialVersionUID = 1L;

    private static final String TEMPLATE_MANAGE_TOURS = "/admin/plugins/ontour/manage_tours.html";
    private static final String TEMPLATE_CREATE_TOUR = "/admin/plugins/ontour/create_tour.html";
    private static final String TEMPLATE_MODIFY_TOUR = "/admin/plugins/ontour/modify_tour.html";
    private static final String TEMPLATE_MANAGE_STEPS = "/admin/plugins/ontour/manage_steps.html";
    private static final String TEMPLATE_CREATE_STEP = "/admin/plugins/ontour/create_step.html";
    private static final String TEMPLATE_MODIFY_STEP = "/admin/plugins/ontour/modify_step.html";
    private static final String TEMPLATE_IMPORT_TOURS = "/admin/plugins/ontour/import_tours.html";
    private static final String TEMPLATE_EXPORT_TOURS = "/admin/plugins/ontour/export_tours.html";
    private static final String TEMPLATE_TRANSLATE_TOUR = "/admin/plugins/ontour/translate_tour.html";

    private static final String PROPERTY_PAGE_TITLE_MANAGE_TOURS = "ontour.manage_tours.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_TOUR = "ontour.create_tour.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_TOUR = "ontour.modify_tour.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_STEPS = "ontour.manage_steps.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_STEP = "ontour.create_step.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_STEP = "ontour.modify_step.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_IMPORT_TOURS = "ontour.import_tours.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_EXPORT_TOURS = "ontour.export_tours.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_TRANSLATE_TOUR = "ontour.translate_tour.pageTitle";
    private static final String PROPERTY_ITEMS_PER_PAGE = "ontour.listTours.itemsPerPage";

    private static final String MARK_TOUR_LIST = "tour_list";
    private static final String MARK_TOUR = "tour";
    private static final String MARK_STEP = "step";
    private static final String MARK_TEST_URLS = "test_urls";
    private static final String MARK_TARGET_LIST = "target_list";
    private static final String MARK_TRIGGER_LIST = "trigger_list";
    private static final String MARK_OVERLAY_CLICK_LIST = "overlay_click_list";
    private static final String MARK_SIDE_LIST = "side_list";
    private static final String MARK_ALIGN_LIST = "align_list";
    private static final String MARK_TRI_STATE_LIST = "tri_state_list";
    private static final String MARK_BUTTONS = "buttons";
    private static final String MARK_LANG_LIST = "lang_list";
    private static final String MARK_LANG_LABELS = "lang_labels";
    private static final String MARK_TRANSLATIONS = "translations";
    private static final String MARK_STATE_COUNTS = "state_counts";
    private static final String MARK_LAUNCHER_POSITION_BO = "launcher_position_bo";
    private static final String MARK_LAUNCHER_POSITION_FO = "launcher_position_fo";
    private static final String MARK_CAN_MANAGE_PROPERTIES = "can_manage_properties";
    private static final String MARK_CAN_CREATE = "can_create";
    private static final String MARK_PERMISSIONS = "permissions";
    private static final List<String> TOUR_PERMISSIONS = List.of( TourResourceIdService.PERMISSION_MODIFY, TourResourceIdService.PERMISSION_DELETE,
            TourResourceIdService.PERMISSION_MANAGE_STEPS, TourResourceIdService.PERMISSION_TRANSLATE, TourResourceIdService.PERMISSION_EXPORT,
            TourResourceIdService.PERMISSION_RESET );
    private static final String RIGHT_PROPERTIES_MANAGEMENT = "CORE_PROPERTIES_MANAGEMENT";
    private static final String MARK_JSON = "json";
    private static final String MARK_FILE_NAME = "file_name";
    private static final String MARK_OVERWRITE = "overwrite";

    private static final String VIEW_MANAGE_TOURS = "manageTours";
    private static final String VIEW_CREATE_TOUR = "createTour";
    private static final String VIEW_MODIFY_TOUR = "modifyTour";
    private static final String VIEW_CONFIRM_REMOVE_TOUR = "confirmRemoveTour";
    private static final String VIEW_MANAGE_STEPS = "manageSteps";
    private static final String VIEW_CREATE_STEP = "createStep";
    private static final String VIEW_MODIFY_STEP = "modifyStep";
    private static final String VIEW_CONFIRM_REMOVE_STEP = "confirmRemoveStep";
    private static final String VIEW_IMPORT_TOURS = "importTours";
    private static final String VIEW_EXPORT_TOURS = "exportTours";
    private static final String VIEW_TRANSLATE_TOUR = "translateTour";
    private static final String VIEW_CONFIRM_RESET_TOUR = "confirmResetTour";

    private static final String ACTION_CREATE_TOUR = "createTour";
    private static final String ACTION_MODIFY_TOUR = "modifyTour";
    private static final String ACTION_REMOVE_TOUR = "removeTour";
    private static final String ACTION_MOVE_STEP = "moveStep";
    private static final String ACTION_CREATE_STEP = "createStep";
    private static final String ACTION_MODIFY_STEP = "modifyStep";
    private static final String ACTION_REMOVE_STEP = "removeStep";
    private static final String ACTION_IMPORT_TOURS = "importTours";
    private static final String ACTION_TRANSLATE_TOUR = "translateTour";
    private static final String ACTION_RESET_TOUR = "resetTour";

    private static final String PARAMETER_ID_TOUR = "id";
    private static final String PARAMETER_ID_STEP = "id_step";
    private static final String PARAMETER_DIRECTION = "direction";
    private static final String PARAMETER_SHOW_BUTTONS = "show_buttons_list";
    private static final String PARAMETER_DISABLE_BUTTONS = "disable_buttons_list";
    private static final String PARAMETER_OVERRIDE_SHOW_BUTTONS = "override_show_buttons";
    private static final String PARAMETER_OVERRIDE_DISABLE_BUTTONS = "override_disable_buttons";
    private static final String PARAMETER_JSON = "json";
    private static final String PARAMETER_OVERWRITE = "overwrite";
    private static final String PARAMETER_TEST_TOUR = "ontour";
    private static final String PARAMETER_TEST_LANG = "ontour_lang";
    private static final String PARAMETER_LANG = "lang";
    private static final String DIRECTION_UP = "up";

    private static final String MESSAGE_CONFIRM_REMOVE_TOUR = "ontour.message.confirmRemoveTour";
    private static final String MESSAGE_CONFIRM_REMOVE_STEP = "ontour.message.confirmRemoveStep";
    private static final String MESSAGE_ERROR_CODE_USED = "ontour.message.error.codeUsed";
    private static final String MESSAGE_ERROR_IMPORT = "ontour.message.error.import";
    private static final String MESSAGE_ERROR_TRANSLATION = "ontour.message.error.translation";
    private static final String INFO_TOUR_TRANSLATED = "ontour.info.tour.translated";
    private static final String INFO_TOUR_RESET = "ontour.info.tour.reset";
    private static final String MESSAGE_CONFIRM_RESET_TOUR = "ontour.message.confirmResetTour";
    private static final String LABEL_ALL_LANGUAGES = "ontour.model.entity.tour.lang.all";
    private static final String INFO_TOUR_CREATED = "ontour.info.tour.created";
    private static final String INFO_TOUR_UPDATED = "ontour.info.tour.updated";
    private static final String INFO_TOUR_REMOVED = "ontour.info.tour.removed";
    private static final String INFO_STEP_CREATED = "ontour.info.step.created";
    private static final String INFO_STEP_UPDATED = "ontour.info.step.updated";
    private static final String INFO_STEP_REMOVED = "ontour.info.step.removed";
    private static final String INFO_TOURS_IMPORTED = "ontour.info.tours.imported";

    private static final String PREFIX_LABEL_TARGET = "ontour.model.entity.tour.target.";
    private static final String PREFIX_LABEL_TRIGGER = "ontour.model.entity.tour.triggerMode.";
    private static final String PREFIX_LABEL_OVERLAY_CLICK = "ontour.model.entity.tour.overlayClickBehavior.";
    private static final String PREFIX_LABEL_SIDE = "ontour.model.entity.step.side.";
    private static final String PREFIX_LABEL_ALIGN = "ontour.model.entity.step.align.";
    private static final String PREFIX_LABEL_TRI_STATE = "ontour.model.entity.step.triState.";
    private static final String LABEL_AUTO = "auto";
    private static final String BUTTON_SEPARATOR = ",";
    private static final String EXPORT_FILE_NAME_ALL = "ontour-tours.json";
    private static final String EXPORT_FILE_EXTENSION = ".json";
    private static final List<String> BUTTONS = List.of( "next", "previous", "close" );

    @Inject
    private TourService _tourService;

    @Inject
    private TourJsonService _tourJsonService;

    @Inject
    private TourUserStateService _tourUserStateService;

    @Inject
    private LauncherPositionService _launcherPositionService;

    @Inject
    @Pager( listBookmark = MARK_TOUR_LIST, defaultItemsPerPage = PROPERTY_ITEMS_PER_PAGE, baseUrl = "jsp/admin/plugins/ontour/ManageTours.jsp" )
    private IPager<Tour, Void> _pager;

    /**
     * List the tours
     *
     * @param request
     *            the request
     * @param model
     *            the model
     * @return the page
     */
    @View( value = VIEW_MANAGE_TOURS, defaultView = true )
    public String getManageTours( HttpServletRequest request, Models model )
    {
        List<Tour> listTours = _tourService.findAll( ).stream( ).filter( t -> isAuthorized( t.getCode( ), TourResourceIdService.PERMISSION_VIEW ) )
                .toList( );
        Map<String, String> mapTestUrls = new HashMap<>( );

        for ( Tour tour : listTours )
        {
            buildTestUrl( tour ).ifPresent( strUrl -> mapTestUrls.put( String.valueOf( tour.getId( ) ), strUrl ) );
        }

        _pager.withListItem( listTours ).populateModels( request, model, getLocale( ) );
        model.put( MARK_TEST_URLS, mapTestUrls );
        model.put( MARK_LANG_LABELS, buildLangList( getLocale( ) ).toMap( ) );
        model.put( MARK_STATE_COUNTS, _tourUserStateService.countByTour( ) );
        model.put( MARK_LAUNCHER_POSITION_BO, _launcherPositionService.getPosition( Tour.TARGET_BO ) );
        model.put( MARK_LAUNCHER_POSITION_FO, _launcherPositionService.getPosition( Tour.TARGET_FO ) );
        model.put( MARK_CAN_MANAGE_PROPERTIES, getUser( ).checkRight( RIGHT_PROPERTIES_MANAGEMENT ) );
        model.put( MARK_CAN_CREATE, isAuthorized( RBAC.WILDCARD_RESOURCES_ID, TourResourceIdService.PERMISSION_CREATE ) );
        model.put( MARK_PERMISSIONS, buildPermissions( listTours ) );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_TOURS, TEMPLATE_MANAGE_TOURS, model );
    }

    /**
     * Display the tour creation form
     *
     * @param model
     *            the model
     * @return the page
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( VIEW_CREATE_TOUR )
    public String getCreateTour( Models model ) throws AccessDeniedException
    {
        checkPermission( RBAC.WILDCARD_RESOURCES_ID, TourResourceIdService.PERMISSION_CREATE );

        return getTourForm( Tour.createWithDefaults( ), model, PROPERTY_PAGE_TITLE_CREATE_TOUR, TEMPLATE_CREATE_TOUR );
    }

    /**
     * Create a tour
     *
     * @param tour
     *            the tour bound from the form
     * @param bindingResult
     *            the binding and validation result
     * @param model
     *            the model
     * @param request
     *            the request
     * @return the redirect URL, or the form with its errors
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_CREATE_TOUR )
    public String doCreateTour( @Valid @ModelAttribute Tour tour, BindingResult bindingResult, Models model, HttpServletRequest request ) throws AccessDeniedException
    {
        checkPermission( RBAC.WILDCARD_RESOURCES_ID, TourResourceIdService.PERMISSION_CREATE );

        bindTourButtons( tour, request );

        if ( bindingResult.isFailed( ) )
        {
            model.put( MVCUtils.MARK_ERRORS, bindingResult.getAllErrors( ) );

            return getTourForm( tour, model, PROPERTY_PAGE_TITLE_CREATE_TOUR, TEMPLATE_CREATE_TOUR );
        }

        if ( _tourService.isCodeUsed( tour.getCode( ), tour.getLang( ), 0 ) )
        {
            addError( MESSAGE_ERROR_CODE_USED, getLocale( ) );

            return getTourForm( tour, model, PROPERTY_PAGE_TITLE_CREATE_TOUR, TEMPLATE_CREATE_TOUR );
        }

        _tourService.create( tour );
        addInfo( INFO_TOUR_CREATED, getLocale( ) );

        return redirect( request, VIEW_MANAGE_STEPS, PARAMETER_ID_TOUR, tour.getId( ) );
    }

    /**
     * Display the tour modification form
     *
     * @param request
     *            the request
     * @param model
     *            the model
     * @return the page
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( VIEW_MODIFY_TOUR )
    public String getModifyTour( HttpServletRequest request, Models model ) throws AccessDeniedException
    {
        Optional<Tour> tour = findTour( request.getParameter( PARAMETER_ID_TOUR ) );

        if ( tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_MODIFY );

        return getTourForm( tour.get( ), model, PROPERTY_PAGE_TITLE_MODIFY_TOUR, TEMPLATE_MODIFY_TOUR );
    }

    /**
     * Update a tour
     *
     * @param tour
     *            the tour bound from the form
     * @param bindingResult
     *            the binding and validation result
     * @param model
     *            the model
     * @param request
     *            the request
     * @return the redirect URL, or the form with its errors
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_MODIFY_TOUR )
    public String doModifyTour( @Valid @ModelAttribute Tour tour, BindingResult bindingResult, Models model, HttpServletRequest request ) throws AccessDeniedException
    {
        bindTourButtons( tour, request );

        Optional<Tour> stored = _tourService.findById( tour.getId( ) );

        if ( stored.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( stored.get( ).getCode( ), TourResourceIdService.PERMISSION_MODIFY );

        if ( bindingResult.isFailed( ) )
        {
            model.put( MVCUtils.MARK_ERRORS, bindingResult.getAllErrors( ) );

            return getTourForm( tour, model, PROPERTY_PAGE_TITLE_MODIFY_TOUR, TEMPLATE_MODIFY_TOUR );
        }

        if ( _tourService.isCodeUsed( tour.getCode( ), tour.getLang( ), tour.getId( ) ) )
        {
            addError( MESSAGE_ERROR_CODE_USED, getLocale( ) );

            return getTourForm( tour, model, PROPERTY_PAGE_TITLE_MODIFY_TOUR, TEMPLATE_MODIFY_TOUR );
        }

        _tourService.update( tour );
        addInfo( INFO_TOUR_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_TOURS );
    }

    /**
     * Ask the confirmation of a tour removal
     *
     * @param request
     *            the request
     * @return the confirmation message URL
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( value = VIEW_CONFIRM_REMOVE_TOUR, securityTokenAction = ACTION_REMOVE_TOUR )
    public String getConfirmRemoveTour( HttpServletRequest request ) throws AccessDeniedException
    {
        Optional<Tour> tour = findTour( request.getParameter( PARAMETER_ID_TOUR ) );

        if ( tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_DELETE );

        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_TOUR ) );
        url.addParameter( PARAMETER_ID_TOUR, tour.get( ).getId( ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_TOUR, new Object [ ] {
                tour.get( ).getTitle( )
        }, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Remove a tour and its steps
     *
     * @param request
     *            the request
     * @return the redirect URL
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_REMOVE_TOUR )
    public String doRemoveTour( HttpServletRequest request ) throws AccessDeniedException
    {
        Optional<Tour> tour = findTour( request.getParameter( PARAMETER_ID_TOUR ) );

        if ( tour.isPresent( ) )
        {
            checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_DELETE );
            _tourService.remove( tour.get( ).getId( ) );
            addInfo( INFO_TOUR_REMOVED, getLocale( ) );
        }

        return redirectView( request, VIEW_MANAGE_TOURS );
    }

    /**
     * Ask the confirmation of the reset of the users states of a tour
     *
     * @param request
     *            the request
     * @return the confirmation message URL
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( value = VIEW_CONFIRM_RESET_TOUR, securityTokenAction = ACTION_RESET_TOUR )
    public String getConfirmResetTour( HttpServletRequest request ) throws AccessDeniedException
    {
        Optional<Tour> tour = findTour( request.getParameter( PARAMETER_ID_TOUR ) );

        if ( tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_RESET );

        UrlItem url = new UrlItem( getActionUrl( ACTION_RESET_TOUR ) );
        url.addParameter( PARAMETER_ID_TOUR, tour.get( ).getId( ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_RESET_TOUR, new Object [ ] {
                tour.get( ).getCode( )
        }, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Forget which users finished or closed a tour, in every language: it starts again automatically for them
     *
     * @param request
     *            the request
     * @return the redirect URL
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_RESET_TOUR )
    public String doResetTour( HttpServletRequest request ) throws AccessDeniedException
    {
        Optional<Tour> tour = findTour( request.getParameter( PARAMETER_ID_TOUR ) );

        if ( tour.isPresent( ) )
        {
            checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_RESET );
            _tourUserStateService.reset( tour.get( ).getCode( ) );
            addInfo( INFO_TOUR_RESET, getLocale( ) );
        }

        return redirectView( request, VIEW_MANAGE_TOURS );
    }

    /**
     * List the steps of a tour. Every form of the page moves a step, hence the token of the move action.
     *
     * @param request
     *            the request
     * @param model
     *            the model
     * @return the page
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( value = VIEW_MANAGE_STEPS, securityTokenAction = ACTION_MOVE_STEP )
    public String getManageSteps( HttpServletRequest request, Models model ) throws AccessDeniedException
    {
        Optional<Tour> tour = findTour( request.getParameter( PARAMETER_ID_TOUR ) );

        if ( tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_VIEW );

        model.put( MARK_TOUR, tour.get( ) );
        model.put( MARK_PERMISSIONS, buildPermissions( List.of( tour.get( ) ) ) );
        buildTestUrl( tour.get( ) ).ifPresent( strUrl -> model.put( MARK_TEST_URLS, Map.of( String.valueOf( tour.get( ).getId( ) ), strUrl ) ) );
        model.put( MARK_LANG_LABELS, buildLangList( getLocale( ) ).toMap( ) );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_STEPS, TEMPLATE_MANAGE_STEPS, model );
    }

    /**
     * Move a step up or down
     *
     * @param request
     *            the request
     * @return the redirect URL
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_MOVE_STEP )
    public String doMoveStep( HttpServletRequest request ) throws AccessDeniedException
    {
        Optional<Step> step = findStep( request.getParameter( PARAMETER_ID_STEP ) );

        if ( step.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkStepPermission( step.get( ) );

        _tourService.moveStep( step.get( ).getId( ), DIRECTION_UP.equals( request.getParameter( PARAMETER_DIRECTION ) ) );

        return redirect( request, VIEW_MANAGE_STEPS, PARAMETER_ID_TOUR, step.get( ).getIdTour( ) );
    }

    /**
     * Display the step creation form
     *
     * @param request
     *            the request
     * @param model
     *            the model
     * @return the page
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( VIEW_CREATE_STEP )
    public String getCreateStep( HttpServletRequest request, Models model ) throws AccessDeniedException
    {
        Optional<Tour> tour = findTour( request.getParameter( PARAMETER_ID_TOUR ) );

        if ( tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_MANAGE_STEPS );

        Step step = new Step( );
        step.setIdTour( tour.get( ).getId( ) );

        return getStepForm( step, tour.get( ), model, PROPERTY_PAGE_TITLE_CREATE_STEP, TEMPLATE_CREATE_STEP );
    }

    /**
     * Create a step at the end of its tour
     *
     * @param step
     *            the step bound from the form
     * @param bindingResult
     *            the binding and validation result
     * @param model
     *            the model
     * @param request
     *            the request
     * @return the redirect URL, or the form with its errors
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_CREATE_STEP )
    public String doCreateStep( @Valid @ModelAttribute Step step, BindingResult bindingResult, Models model, HttpServletRequest request ) throws AccessDeniedException
    {
        bindStepButtons( step, request );

        Optional<Tour> tour = _tourService.findById( step.getIdTour( ) );

        if ( tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_MANAGE_STEPS );

        if ( bindingResult.isFailed( ) )
        {
            model.put( MVCUtils.MARK_ERRORS, bindingResult.getAllErrors( ) );

            return getStepForm( step, tour.get( ), model, PROPERTY_PAGE_TITLE_CREATE_STEP, TEMPLATE_CREATE_STEP );
        }

        _tourService.createStep( step );
        addInfo( INFO_STEP_CREATED, getLocale( ) );

        return redirect( request, VIEW_MANAGE_STEPS, PARAMETER_ID_TOUR, step.getIdTour( ) );
    }

    /**
     * Display the step modification form
     *
     * @param request
     *            the request
     * @param model
     *            the model
     * @return the page
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( VIEW_MODIFY_STEP )
    public String getModifyStep( HttpServletRequest request, Models model ) throws AccessDeniedException
    {
        Optional<Step> step = findStep( request.getParameter( PARAMETER_ID_STEP ) );
        Optional<Tour> tour = step.flatMap( s -> _tourService.findById( s.getIdTour( ) ) );

        if ( step.isEmpty( ) || tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_MANAGE_STEPS );

        return getStepForm( step.get( ), tour.get( ), model, PROPERTY_PAGE_TITLE_MODIFY_STEP, TEMPLATE_MODIFY_STEP );
    }

    /**
     * Update a step
     *
     * @param step
     *            the step bound from the form
     * @param bindingResult
     *            the binding and validation result
     * @param model
     *            the model
     * @param request
     *            the request
     * @return the redirect URL, or the form with its errors
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_MODIFY_STEP )
    public String doModifyStep( @Valid @ModelAttribute Step step, BindingResult bindingResult, Models model, HttpServletRequest request ) throws AccessDeniedException
    {
        bindStepButtons( step, request );

        Optional<Step> stored = _tourService.findStep( step.getId( ) );
        Optional<Tour> tour = stored.flatMap( s -> _tourService.findById( s.getIdTour( ) ) );

        if ( stored.isEmpty( ) || tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_MANAGE_STEPS );

        if ( bindingResult.isFailed( ) )
        {
            model.put( MVCUtils.MARK_ERRORS, bindingResult.getAllErrors( ) );
            step.setIdTour( stored.get( ).getIdTour( ) );

            return getStepForm( step, tour.get( ), model, PROPERTY_PAGE_TITLE_MODIFY_STEP, TEMPLATE_MODIFY_STEP );
        }

        _tourService.updateStep( step );
        addInfo( INFO_STEP_UPDATED, getLocale( ) );

        return redirect( request, VIEW_MANAGE_STEPS, PARAMETER_ID_TOUR, stored.get( ).getIdTour( ) );
    }

    /**
     * Ask the confirmation of a step removal
     *
     * @param request
     *            the request
     * @return the confirmation message URL
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( value = VIEW_CONFIRM_REMOVE_STEP, securityTokenAction = ACTION_REMOVE_STEP )
    public String getConfirmRemoveStep( HttpServletRequest request ) throws AccessDeniedException
    {
        Optional<Step> step = findStep( request.getParameter( PARAMETER_ID_STEP ) );

        if ( step.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkStepPermission( step.get( ) );

        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_STEP ) );
        url.addParameter( PARAMETER_ID_STEP, step.get( ).getId( ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_STEP, new Object [ ] {
                step.get( ).getOrder( )
        }, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Remove a step
     *
     * @param request
     *            the request
     * @return the redirect URL
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_REMOVE_STEP )
    public String doRemoveStep( HttpServletRequest request ) throws AccessDeniedException
    {
        Optional<Step> step = findStep( request.getParameter( PARAMETER_ID_STEP ) );

        if ( step.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkStepPermission( step.get( ) );

        _tourService.removeStep( step.get( ).getId( ) );
        addInfo( INFO_STEP_REMOVED, getLocale( ) );

        return redirect( request, VIEW_MANAGE_STEPS, PARAMETER_ID_TOUR, step.get( ).getIdTour( ) );
    }

    /**
     * Display the form that creates the translation of a tour in another language
     *
     * @param request
     *            the request
     * @param model
     *            the model
     * @return the page
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( VIEW_TRANSLATE_TOUR )
    public String getTranslateTour( HttpServletRequest request, Models model ) throws AccessDeniedException
    {
        Optional<Tour> tour = findTour( request.getParameter( PARAMETER_ID_TOUR ) );

        if ( tour.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_TRANSLATE );

        List<String> listExisting = _tourService.findTranslations( tour.get( ).getCode( ) ).stream( ).map( Tour::getLang ).toList( );
        ReferenceList listLangs = new ReferenceList( );
        buildLangList( getLocale( ) ).stream( ).filter( item -> !listExisting.contains( item.getCode( ) ) ).forEach( listLangs::add );

        model.put( MARK_TOUR, tour.get( ) );
        model.put( MARK_TRANSLATIONS, _tourService.findTranslations( tour.get( ).getCode( ) ) );
        model.put( MARK_LANG_LIST, listLangs );
        model.put( MARK_LANG_LABELS, buildLangList( getLocale( ) ).toMap( ) );

        return getPage( PROPERTY_PAGE_TITLE_TRANSLATE_TOUR, TEMPLATE_TRANSLATE_TOUR, model );
    }

    /**
     * Create the translation of a tour: a disabled copy of the tour and its steps in another language
     *
     * @param request
     *            the request
     * @return the redirect URL to the new translation
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_TRANSLATE_TOUR )
    public String doTranslateTour( HttpServletRequest request ) throws AccessDeniedException
    {
        String strIdTour = request.getParameter( PARAMETER_ID_TOUR );
        String strLang = StringUtils.defaultString( request.getParameter( PARAMETER_LANG ) );

        if ( !NumberUtils.isDigits( strIdTour ) || !buildLangList( getLocale( ) ).toMap( ).containsKey( strLang ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        Optional<Tour> source = findTour( strIdTour );

        if ( source.isEmpty( ) )
        {
            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        checkPermission( source.get( ).getCode( ), TourResourceIdService.PERMISSION_TRANSLATE );

        Optional<Tour> translation = _tourService.createTranslation( source.get( ).getId( ), strLang );

        if ( translation.isEmpty( ) )
        {
            addError( MESSAGE_ERROR_TRANSLATION, getLocale( ) );

            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        addInfo( INFO_TOUR_TRANSLATED, getLocale( ) );

        return redirect( request, VIEW_MANAGE_STEPS, PARAMETER_ID_TOUR, translation.get( ).getId( ) );
    }

    /**
     * Display the import form
     *
     * @param model
     *            the model
     * @return the page
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( VIEW_IMPORT_TOURS )
    public String getImportTours( Models model ) throws AccessDeniedException
    {
        checkPermission( RBAC.WILDCARD_RESOURCES_ID, TourResourceIdService.PERMISSION_CREATE );

        return getPage( PROPERTY_PAGE_TITLE_IMPORT_TOURS, TEMPLATE_IMPORT_TOURS, model );
    }

    /**
     * Import tours from a JSON document
     *
     * @param request
     *            the request
     * @param model
     *            the model
     * @return the redirect URL, or the form with its errors
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @Action( ACTION_IMPORT_TOURS )
    public String doImportTours( HttpServletRequest request, Models model ) throws AccessDeniedException
    {
        checkPermission( RBAC.WILDCARD_RESOURCES_ID, TourResourceIdService.PERMISSION_CREATE );

        String strJson = request.getParameter( PARAMETER_JSON );
        boolean bOverwrite = request.getParameter( PARAMETER_OVERWRITE ) != null;

        try
        {
            List<Tour> listTours = _tourJsonService.parseTours( strJson );
            List<Tour> listAllowed = listTours.stream( )
                    .filter( t -> _tourService.findTranslations( t.getCode( ) ).isEmpty( ) || isAuthorized( t.getCode( ), TourResourceIdService.PERMISSION_MODIFY ) )
                    .toList( );
            TourService.ImportResult result = _tourService.importTours( listAllowed, bOverwrite );
            addInfo( I18nService.getLocalizedString( INFO_TOURS_IMPORTED, new Object [ ] {
                    result.nCreated( ), result.nUpdated( ), result.nSkipped( ) + listTours.size( ) - listAllowed.size( )
            }, getLocale( ) ) );
        }
        catch( TourImportException e )
        {
            addError( I18nService.getLocalizedString( MESSAGE_ERROR_IMPORT, new Object [ ] {
                    e.getMessage( )
            }, getLocale( ) ) );
            model.put( MARK_JSON, strJson );
            model.put( MARK_OVERWRITE, bOverwrite );

            return getPage( PROPERTY_PAGE_TITLE_IMPORT_TOURS, TEMPLATE_IMPORT_TOURS, model );
        }

        return redirectView( request, VIEW_MANAGE_TOURS );
    }

    /**
     * Display the JSON export of one tour, or of all the tours when no identifier is given
     *
     * @param request
     *            the request
     * @param model
     *            the model
     * @return the page
     * @throws AccessDeniedException
     *             if the user lacks the RBAC permission
     */
    @View( VIEW_EXPORT_TOURS )
    public String getExportTours( HttpServletRequest request, Models model ) throws AccessDeniedException
    {
        String strIdTour = request.getParameter( PARAMETER_ID_TOUR );
        List<Tour> listTours;
        String strFileName = EXPORT_FILE_NAME_ALL;

        if ( StringUtils.isNotBlank( strIdTour ) )
        {
            Optional<Tour> tour = findTour( strIdTour );

            if ( tour.isEmpty( ) )
            {
                return redirectView( request, VIEW_MANAGE_TOURS );
            }

            checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_EXPORT );
            listTours = List.of( tour.get( ) );
            strFileName = tour.get( ).getCode( ) + EXPORT_FILE_EXTENSION;
        }
        else
        {
            listTours = _tourService.findAll( ).stream( ).filter( t -> isAuthorized( t.getCode( ), TourResourceIdService.PERMISSION_EXPORT ) )
                    .toList( );
        }

        try
        {
            model.put( MARK_JSON, _tourJsonService.exportTours( listTours ) );
        }
        catch( JsonProcessingException e )
        {
            AppLogService.error( "onTour: unable to export the tours", e );

            return redirectView( request, VIEW_MANAGE_TOURS );
        }

        model.put( MARK_FILE_NAME, strFileName );

        return getPage( PROPERTY_PAGE_TITLE_EXPORT_TOURS, TEMPLATE_EXPORT_TOURS, model );
    }

    /**
     * Render the tour form
     *
     * @param tour
     *            the tour
     * @param model
     *            the model
     * @param strPageTitle
     *            the page title property
     * @param strTemplate
     *            the template
     * @return the page
     */
    private String getTourForm( Tour tour, Models model, String strPageTitle, String strTemplate )
    {
        Locale locale = getLocale( );
        model.put( MARK_TOUR, tour );
        model.put( MARK_LANG_LIST, buildLangList( locale ) );
        model.put( MARK_TARGET_LIST, buildReferenceList( PREFIX_LABEL_TARGET, locale, Tour.TARGET_BO, Tour.TARGET_FO ) );
        model.put( MARK_TRIGGER_LIST, buildReferenceList( PREFIX_LABEL_TRIGGER, locale, Tour.TRIGGER_MANUAL, Tour.TRIGGER_FIRST_VISIT, Tour.TRIGGER_ALWAYS ) );
        model.put( MARK_OVERLAY_CLICK_LIST,
                buildReferenceList( PREFIX_LABEL_OVERLAY_CLICK, locale, Tour.OVERLAY_CLICK_CLOSE, Tour.OVERLAY_CLICK_NEXT_STEP, Tour.OVERLAY_CLICK_NONE ) );
        model.put( MARK_BUTTONS, BUTTONS );

        return getPage( strPageTitle, strTemplate, model );
    }

    /**
     * Render the step form
     *
     * @param step
     *            the step
     * @param tour
     *            the tour of the step
     * @param model
     *            the model
     * @param strPageTitle
     *            the page title property
     * @param strTemplate
     *            the template
     * @return the page
     */
    private String getStepForm( Step step, Tour tour, Models model, String strPageTitle, String strTemplate )
    {
        Locale locale = getLocale( );
        model.put( MARK_STEP, step );
        model.put( MARK_TOUR, tour );
        model.put( MARK_SIDE_LIST, buildReferenceList( PREFIX_LABEL_SIDE, locale, "", "top", "right", "bottom", "left" ) );
        model.put( MARK_ALIGN_LIST, buildReferenceList( PREFIX_LABEL_ALIGN, locale, "", "start", "center", "end" ) );
        model.put( MARK_TRI_STATE_LIST, buildTriStateList( locale ) );
        model.put( MARK_BUTTONS, BUTTONS );

        return getPage( strPageTitle, strTemplate, model );
    }

    /**
     * Set the buttons of a tour from the checkboxes of the form
     *
     * @param tour
     *            the tour
     * @param request
     *            the request
     */
    private static void bindTourButtons( Tour tour, HttpServletRequest request )
    {
        tour.setShowButtons( joinButtons( request.getParameterValues( PARAMETER_SHOW_BUTTONS ) ) );
        tour.setDisableButtons( joinButtons( request.getParameterValues( PARAMETER_DISABLE_BUTTONS ) ) );
    }

    /**
     * Set the buttons of a step from the checkboxes of the form. Without override, the step inherits the buttons of the tour.
     *
     * @param step
     *            the step
     * @param request
     *            the request
     */
    private static void bindStepButtons( Step step, HttpServletRequest request )
    {
        step.setShowButtons( ( request.getParameter( PARAMETER_OVERRIDE_SHOW_BUTTONS ) != null )
                ? joinButtons( request.getParameterValues( PARAMETER_SHOW_BUTTONS ) )
                : null );
        step.setDisableButtons( ( request.getParameter( PARAMETER_OVERRIDE_DISABLE_BUTTONS ) != null )
                ? joinButtons( request.getParameterValues( PARAMETER_DISABLE_BUTTONS ) )
                : null );
    }

    /**
     * Join checked button names
     *
     * @param buttons
     *            the checked buttons, may be null
     * @return the comma separated allowed buttons
     */
    private static String joinButtons( String [ ] buttons )
    {
        return ( buttons == null ) ? "" : TourJsonService.normalizeButtons( String.join( BUTTON_SEPARATOR, buttons ) );
    }

    /**
     * Build the URL that opens the page of a tour and starts it. The URL can only be built when the page path has no wildcard and every
     * required parameter has a concrete value.
     *
     * @param tour
     *            the tour
     * @return the test URL, relative to the webapp
     */
    private static Optional<String> buildTestUrl( Tour tour )
    {
        String strPath = StringUtils.stripStart( StringUtils.defaultString( tour.getPagePath( ) ).trim( ), "/" );

        if ( StringUtils.isBlank( strPath ) || strPath.contains( "*" )
                || ( StringUtils.isNotBlank( tour.getPageSelector( ) ) && StringUtils.isBlank( tour.getPageParameters( ) ) ) )
        {
            return Optional.empty( );
        }

        UrlItem url = new UrlItem( strPath );

        for ( Map.Entry<String, List<String>> parameter : PageMatcher.parseQueryString( tour.getPageParameters( ) ).entrySet( ) )
        {
            if ( parameter.getKey( ).startsWith( "!" ) )
            {
                continue;
            }

            for ( String strExpected : parameter.getValue( ) )
            {
                Optional<String> value = Arrays.stream( strExpected.split( "\\|" ) ).map( String::trim ).filter( v -> !"!".equals( v ) ).findFirst( );

                if ( value.isPresent( ) && ( value.get( ).isEmpty( ) || "*".equals( value.get( ) ) ) )
                {
                    return Optional.empty( );
                }

                value.ifPresent( v -> url.addParameter( parameter.getKey( ), v ) );
            }
        }

        url.addParameter( PARAMETER_TEST_TOUR, tour.getCode( ) );

        if ( StringUtils.isNotBlank( tour.getLang( ) ) )
        {
            url.addParameter( PARAMETER_TEST_LANG, tour.getLang( ) );
        }

        return Optional.of( url.getUrl( ) );
    }

    /**
     * Build a localized reference list
     *
     * @param strPrefix
     *            the i18n key prefix of the labels
     * @param locale
     *            the locale
     * @param codes
     *            the codes, an empty code is labelled <code>auto</code>
     * @return the reference list
     */
    private static ReferenceList buildReferenceList( String strPrefix, Locale locale, String... codes )
    {
        ReferenceList list = new ReferenceList( );

        for ( String strCode : codes )
        {
            list.addItem( strCode, I18nService.getLocalizedString( strPrefix + ( strCode.isEmpty( ) ? LABEL_AUTO : strCode ), locale ) );
        }

        return list;
    }

    /**
     * Build the list of the languages a tour can be written in: all languages, then the back office and front office languages of the site
     *
     * @param locale
     *            the locale of the labels
     * @return the reference list, codes are ISO 639 languages
     */
    private static ReferenceList buildLangList( Locale locale )
    {
        Map<String, String> mapLangs = new LinkedHashMap<>( );
        mapLangs.put( Tour.LANG_ALL, I18nService.getLocalizedString( LABEL_ALL_LANGUAGES, locale ) );

        List<Locale> listLocales = new ArrayList<>( I18nService.getAdminAvailableLocales( ) );
        listLocales.addAll( LocaleService.getSupportedLangList( ) );

        for ( Locale lang : listLocales )
        {
            mapLangs.putIfAbsent( lang.getLanguage( ), StringUtils.capitalize( lang.getDisplayLanguage( locale ) ) );
        }

        ReferenceList list = new ReferenceList( );
        mapLangs.forEach( list::addItem );

        return list;
    }

    /**
     * Build the list of the tri-state values of a step option
     *
     * @param locale
     *            the locale
     * @return the reference list
     */
    private static ReferenceList buildTriStateList( Locale locale )
    {
        ReferenceList list = new ReferenceList( );
        list.addItem( Step.INHERIT, I18nService.getLocalizedString( PREFIX_LABEL_TRI_STATE + "inherit", locale ) );
        list.addItem( Step.YES, I18nService.getLocalizedString( PREFIX_LABEL_TRI_STATE + "yes", locale ) );
        list.addItem( Step.NO, I18nService.getLocalizedString( PREFIX_LABEL_TRI_STATE + "no", locale ) );

        return list;
    }

    /**
     * Tell whether the connected administrator has a permission on a tour
     *
     * @param strCode
     *            the tour code, or {@link RBAC#WILDCARD_RESOURCES_ID}
     * @param strPermission
     *            the permission
     * @return true if authorized
     */
    private boolean isAuthorized( String strCode, String strPermission )
    {
        return RBACService.isAuthorized( Tour.RESOURCE_TYPE, strCode, strPermission, (User) getUser( ) );
    }

    /**
     * Check that the connected administrator has a permission on a tour
     *
     * @param strCode
     *            the tour code, or {@link RBAC#WILDCARD_RESOURCES_ID}
     * @param strPermission
     *            the permission
     * @throws AccessDeniedException
     *             if the permission is not granted
     */
    private void checkPermission( String strCode, String strPermission ) throws AccessDeniedException
    {
        if ( !isAuthorized( strCode, strPermission ) )
        {
            throw new AccessDeniedException( "onTour: permission " + strPermission + " denied on tour " + strCode );
        }
    }

    /**
     * Check that the connected administrator can manage the steps of the tour of a step
     *
     * @param step
     *            the step
     * @throws AccessDeniedException
     *             if the permission is not granted
     */
    private void checkStepPermission( Step step ) throws AccessDeniedException
    {
        Optional<Tour> tour = _tourService.findById( step.getIdTour( ) );

        if ( tour.isEmpty( ) )
        {
            throw new AccessDeniedException( "onTour: unknown tour of step " + step.getId( ) );
        }

        checkPermission( tour.get( ).getCode( ), TourResourceIdService.PERMISSION_MANAGE_STEPS );
    }

    /**
     * Compute the permissions of the connected administrator on some tours, for the display of the actions
     *
     * @param listTours
     *            the tours
     * @return the granted state of each permission, by tour code
     */
    private Map<String, Map<String, Boolean>> buildPermissions( List<Tour> listTours )
    {
        Map<String, Map<String, Boolean>> mapPermissions = new HashMap<>( );

        for ( Tour tour : listTours )
        {
            mapPermissions.computeIfAbsent( tour.getCode( ), strCode -> {
                Map<String, Boolean> mapTour = new HashMap<>( );
                TOUR_PERMISSIONS.forEach( strPermission -> mapTour.put( strPermission, isAuthorized( strCode, strPermission ) ) );

                return mapTour;
            } );
        }

        return mapPermissions;
    }

    /**
     * Find a tour from a request parameter
     *
     * @param strIdTour
     *            the identifier parameter
     * @return the tour, with its steps
     */
    private Optional<Tour> findTour( String strIdTour )
    {
        return NumberUtils.isDigits( strIdTour ) ? _tourService.findById( NumberUtils.toInt( strIdTour ) ) : Optional.empty( );
    }

    /**
     * Find a step from a request parameter
     *
     * @param strIdStep
     *            the identifier parameter
     * @return the step
     */
    private Optional<Step> findStep( String strIdStep )
    {
        return NumberUtils.isDigits( strIdStep ) ? _tourService.findStep( NumberUtils.toInt( strIdStep ) ) : Optional.empty( );
    }
}
