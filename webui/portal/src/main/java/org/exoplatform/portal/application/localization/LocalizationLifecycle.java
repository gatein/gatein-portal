/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.portal.application.localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.Constants;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.UserProfileLifecycle;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.LocalePolicy;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationRequestPhaseLifecycle;
import org.exoplatform.web.application.Phase;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * This class takes care of loading / initializing / saving the current Locale. Current Locale is used to create properly
 * localized response to current request.
 *
 * At the beginning of request {@link LocalePolicy} is used to determine the initial Locale to be used for processing the
 * request.
 *
 * This Locale is then set on current {@link org.exoplatform.portal.application.PortalRequestContext} (it's presumed that
 * current {@link org.exoplatform.web.application.RequestContext} is of type PortalRequestContext) by calling
 * {@link org.exoplatform.portal.application.PortalRequestContext#setLocale}.
 *
 * During request processing {@link org.exoplatform.portal.application.PortalRequestContext#getLocale} is the ultimate reference
 * consulted by any rendering code that needs to know about current Locale.
 *
 * When this Locale is changed during action processing, the new Locale choice is saved into user's profile or into browser's
 * cookie in order to be used by future requests.
 *
 * This Lifecycle depends on UserProfileLifecycle being registered before this one, as it relies on it for loading the user
 * profile. See WEB-INF/webui-configuration.xml in web/portal module.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class LocalizationLifecycle implements ApplicationRequestPhaseLifecycle<WebuiRequestContext> {
    private static final String LOCALE_COOKIE = "LOCALE";

    private static final String LOCALE_SESSION_ATTR = "org.gatein.LOCALE";

    private static final String PREV_LOCALE_SESSION_ATTR = "org.gatein.LAST_LOCALE";

    private static final ThreadLocal<Locale> calculatedLocale = new ThreadLocal<Locale>();

    private static Log log = ExoLogger.getLogger("portal:LocalizationLifecycle");

    /**
     * @see org.exoplatform.web.application.ApplicationLifecycle#onInit
     */
    public void onInit(Application app) throws Exception {
    }

    /**
     * Initialize Locale to be used for the processing of current request
     *
     * @see org.exoplatform.web.application.ApplicationLifecycle#onStartRequest
     */
    public void onStartRequest(Application app, WebuiRequestContext context) throws Exception {
        if (context instanceof PortalRequestContext == false)
            throw new IllegalArgumentException("Expected PortalRequestContext, but got: " + context);

        PortalRequestContext reqCtx = (PortalRequestContext) context;
        ExoContainer container = app.getApplicationServiceContainer();

        LocaleConfigService localeConfigService = (LocaleConfigService) container
                .getComponentInstanceOfType(LocaleConfigService.class);
        LocalePolicy localePolicy = (LocalePolicy) container.getComponentInstanceOfType(LocalePolicy.class);

        LocaleContextInfo localeCtx = new LocaleContextInfo();

        Set<Locale> supportedLocales = new HashSet<Locale>();
        for (LocaleConfig lc : localeConfigService.getLocalConfigs()) {
            supportedLocales.add(lc.getLocale());
        }
        localeCtx.setSupportedLocales(supportedLocales);

        HttpServletRequest request = HttpServletRequest.class.cast(context.getRequest());
        localeCtx.setBrowserLocales(Collections.list(request.getLocales()));
        localeCtx.setCookieLocales(getCookieLocales(request));
        localeCtx.setSessionLocale(getSessionLocale(request));
        localeCtx.setUserProfileLocale(getUserProfileLocale(reqCtx));
        localeCtx.setRemoteUser(reqCtx.getRemoteUser());

        DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
        PortalConfig pConfig = null;
        try {
            pConfig = dataStorage.getPortalConfig(SiteType.PORTAL.getName(), reqCtx.getPortalOwner());
            if (pConfig == null)
                log.warn("No UserPortalConfig available! Portal locale set to 'en'");
        } catch (Exception ignored) {
            if (log.isDebugEnabled())
                log.debug("IGNORED: Failed to load UserPortalConfig: ", ignored);
        }

        String portalLocaleName = "en";
        if (pConfig != null)
            portalLocaleName = pConfig.getLocale();

        Locale portalLocale = LocaleContextInfo.getLocale(portalLocaleName);
        localeCtx.setPortalLocale(portalLocale);

        localeCtx.setRequestLocale(reqCtx.getRequestLocale());

        Locale locale = localePolicy.determineLocale(localeCtx);
        boolean supported = supportedLocales.contains(locale);

        if (!supported && !"".equals(locale.getCountry())) {
            locale = new Locale(locale.getLanguage());
            supported = supportedLocales.contains(locale);
        }
        if (!supported) {
            if (log.isWarnEnabled())
                log.warn("Unsupported locale returned by LocalePolicy: " + localePolicy + ". Falling back to 'en'.");
            locale = Locale.ENGLISH;
        }
        reqCtx.setLocale(locale);
        calculatedLocale.set(locale);
        resetOrientation(reqCtx, locale);
    }

    /**
     * @see org.exoplatform.web.application.ApplicationRequestPhaseLifecycle#onStartRequestPhase
     */
    public void onStartRequestPhase(Application app, WebuiRequestContext context, Phase phase) {
    }

    /**
     * Save any locale change - to cookie for anonymous users, to profile for logged-in users
     *
     * @see org.exoplatform.web.application.ApplicationRequestPhaseLifecycle#onEndRequestPhase
     */
    public void onEndRequestPhase(Application app, WebuiRequestContext context, Phase phase) {
        if (phase == Phase.ACTION) {
            // if onStartRequest survived the cast, this one should as well - no check necessary
            PortalRequestContext reqCtx = (PortalRequestContext) context;
            Locale loc = reqCtx.getLocale();
            Locale remembered = calculatedLocale.get();
            calculatedLocale.remove();

            boolean refreshNeeded = false;

            // if locale changed since previous request
            Locale sessLocale = getPreviousLocale(reqCtx.getRequest());
            if (loc != null && sessLocale != null && !loc.equals(sessLocale)) {
                refreshNeeded = true;
            }
            // if locale changed during this request's processing
            if (loc != null && (remembered == null || !loc.equals(remembered))) {
                refreshNeeded = true;
                saveLocale(reqCtx, loc);
            }

            if (refreshNeeded) {
                resetOrientation(reqCtx, loc);
            }

            savePreviousLocale(reqCtx, loc);
        }
    }

    /**
     * @see org.exoplatform.web.application.ApplicationLifecycle#onEndRequest
     */
    public void onEndRequest(Application app, WebuiRequestContext context) throws Exception {
    }

    /**
     * @see org.exoplatform.web.application.ApplicationLifecycle#onFailRequest
     */
    public void onFailRequest(Application app, WebuiRequestContext context, RequestFailure failureType) {
    }

    /**
     * @see org.exoplatform.web.application.ApplicationLifecycle#onDestroy
     */
    public void onDestroy(Application app) throws Exception {
    }

    /**
     * Use {@link UserProfile} already loaded by {@link org.exoplatform.portal.application.UserProfileLifecycle} or load one
     * ourselves.
     *
     * @param context current PortalRequestContext
     * @return Locale from user's profile or null
     */
    private Locale getUserProfileLocale(PortalRequestContext context) {
        String lang = null;

        UserProfile userProfile = getLoadedProfile(context);
        lang = userProfile == null ? null : userProfile.getUserInfoMap().get(Constants.USER_LANGUAGE);
        return (lang != null) ? LocaleContextInfo.getLocale(lang) : null;
    }

    private UserProfile loadUserProfile(ExoContainer container, PortalRequestContext context) {
        UserProfile userProfile = null;
        OrganizationService svc = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);

        String user = context.getRemoteUser();
        if (user != null) {
            try {
                userProfile = svc.getUserProfileHandler().findUserProfileByName(user);
            } catch (Exception ignored) {
                log.error("IGNORED: Failed to load UserProfile for username: " + user, ignored);
            }

            if (userProfile == null && log.isWarnEnabled())
                log.warn("Could not load user profile for " + user + ". Using default portal locale.");

            if(userProfile == null) {
                userProfile = svc.getUserProfileHandler().createUserProfileInstance(user);
            }
        }
        return userProfile;
    }

    private UserProfile getLoadedProfile(PortalRequestContext context) {
        return (UserProfile) context.getAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME);
    }

    public static List<Locale> getCookieLocales(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (LOCALE_COOKIE.equals(cookie.getName())) {
                    List<Locale> locales = new ArrayList<Locale>();
                    locales.add(LocaleContextInfo.getLocale(cookie.getValue()));
                    return locales;
                }
            }
        }
        return Collections.emptyList();
    }

    public static Locale getSessionLocale(HttpServletRequest request) {
        return getLocaleFromSession(request, LOCALE_SESSION_ATTR);
    }

    public static Locale getPreviousLocale(HttpServletRequest request) {
        return getLocaleFromSession(request, PREV_LOCALE_SESSION_ATTR);
    }

    private static Locale getLocaleFromSession(HttpServletRequest request, String attrName) {
        String lang = null;
        HttpSession session = request.getSession(false);
        if (session != null)
            lang = (String) session.getAttribute(attrName);
        return (lang != null) ? LocaleContextInfo.getLocale(lang) : null;
    }

    private void saveLocale(PortalRequestContext context, Locale loc) {
        String user = context.getRemoteUser();
        if (user != null) {
            saveLocaleToUserProfile(context, loc, user);
        } else {
            saveLocaleToCookie(context, loc);
        }

        saveSessionLocale(context, loc);
    }

    private void resetOrientation(PortalRequestContext context, Locale loc) {
        ExoContainer container = context.getApplication().getApplicationServiceContainer();
        LocaleConfigService localeConfigService = (LocaleConfigService) container
                .getComponentInstanceOfType(LocaleConfigService.class);
        LocaleConfig localeConfig = localeConfigService.getLocaleConfig(LocaleContextInfo.getLocaleAsString(loc));
        if (localeConfig == null) {
            if (log.isWarnEnabled())
                log.warn("Locale changed to unsupported Locale during request processing: " + loc);
            return;
        }
        // we presume PortalRequestContext, and UIPortalApplication
        ((UIPortalApplication) context.getUIApplication()).setOrientation(localeConfig.getOrientation());
    }

    private void saveSessionLocale(PortalRequestContext context, Locale loc) {
        saveLocaleToSession(context, LOCALE_SESSION_ATTR, loc);
    }

    private void savePreviousLocale(PortalRequestContext context, Locale loc) {
        saveLocaleToSession(context, PREV_LOCALE_SESSION_ATTR, loc);
    }

    private void saveLocaleToSession(PortalRequestContext context, String attrName, Locale loc) {
        HttpServletRequest res = context.getRequest();
        HttpSession session = res.getSession(false);
        if (session != null)
            session.setAttribute(attrName, LocaleContextInfo.getLocaleAsString(loc));
    }

    private void saveLocaleToCookie(PortalRequestContext context, Locale loc) {
        HttpServletResponse res = context.getResponse();
        Cookie cookie = new Cookie(LOCALE_COOKIE, LocaleContextInfo.getLocaleAsString(loc));
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath("/");
        res.addCookie(cookie);
    }

    private void saveLocaleToUserProfile(PortalRequestContext context, Locale loc, String user) {
        ExoContainer container = context.getApplication().getApplicationServiceContainer();
        OrganizationService svc = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);

        // Don't rely on UserProfileLifecycle loaded UserProfile when doing
        // an update to avoid a potential overwrite of other changes
        UserProfile userProfile = loadUserProfile(container, context);
        if (userProfile != null) {
            userProfile.getUserInfoMap().put(Constants.USER_LANGUAGE, LocaleContextInfo.getLocaleAsString(loc));
            try {
                svc.getUserProfileHandler().saveUserProfile(userProfile, false);
            } catch (Exception ignored) {
                log.error("IGNORED: Failed to save profile for user: " + user, ignored);
                userProfile = null;
            }
        }

        if (userProfile == null) {
            if (log.isWarnEnabled())
                log.warn("Unable to save locale into profile for user: " + user);
        }
    }
}
