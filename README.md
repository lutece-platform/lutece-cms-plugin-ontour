# Plugin onTour

Guided tours and onboarding for any Lutece 8 page, in the back office (BO) and the front office (FO), powered by
[Driver.js](https://github.com/nilbuild/driver.js) 1.8.0 (vanilla JS, MIT, shipped in `webapp/themes/shared/plugins/ontour/js/driver/` and `css/driver/`).

## Features

* **Admin feature** `ONTOUR_MANAGEMENT` (*Site* group): create, modify, delete tours, then add, modify, reorder and delete their steps.
  JSON import/export of one or all tours, and a **Test** button that opens the target page and starts the tour.
* **Every Driver.js option** is editable:
  * on the tour: `animate`, `duration`, `overlayColor`, `overlayOpacity`, `smoothScroll`, `allowClose`, `allowScroll`,
    `overlayClickBehavior` (close / nextStep / nothing), `stagePadding`, `stageRadius`, `disableActiveInteraction`, `advanceOnClick`,
    `skipMissingElement`, `waitForElement`, `allowKeyboardControl`, `popoverClass`, `popoverOffset`, `showButtons`, `disableButtons`,
    `showProgress`, `progressText`, `nextBtnText`, `prevBtnText`, `doneBtnText`
  * on each step: `element` (CSS selector, empty for a centered popover), `popover.title`, `popover.description` (HTML), `side`,
    `align`, `showButtons`, `disableButtons`, `showProgress`, `popoverClass`, and the button texts, plus the step-level `disableActiveInteraction`,
    `advanceOnClick`, `skipMissingElement` and `waitForElement`. Each step option can *inherit* the tour value.
  * Driver.js hooks (`onNextClick`, `onHighlighted`…) are JavaScript functions: they can't be stored, but they can be passed through the JS API
    (`LuteceOnTour.start( code, { onHighlighted: … } )`).
* **Triggers**: *manual* (launcher, button, JS API), *automatic until finished or closed*, *automatic on every visit*.
* **State kept per user account**: when a user finishes a tour or closes it, the state is stored in `ontour_user_tour` for their account
  (BO administrator, or FO user connected through MyLutece), so the tour doesn't start again, whatever the browser, device or session.
  Anonymous FO visitors fall back on the browser storage. The list of tours shows how many accounts finished / closed each tour, and
  *Show again to everyone* resets them (for instance after reworking a tour). The state is shared by all the translations of a tour.
* **Floating launcher** (`?` button) listing the tours of the current page.
* **Multilingual**: each tour has a language (`fr`, `en`… or *All languages*). Several tours share the same code, one per language: the user gets
  their language, then the *All languages* one, then the default language of the site, then any translation. The *Translations* screen copies
  a tour and its steps into another language (created disabled, until translated). The **Test** button forces the language (`?ontour_lang=`).
* Default button texts are localized (en, fr).
* **Launcher position**: two site properties, *Site properties › Guided tour configuration* (`ontour.site_property.launcher.bo.select` and `…fo.select`):
  `bottom_right` (default), `bottom_center`, `bottom_left`, `top_right`, `top_center`, `top_left` or `hidden` (no button; tours still start automatically, from a
  `data-ontour-start` button or from the JS API). Missing properties are created at startup, and the lists of options are refreshed, so existing sites get them without SQL.
  The tours screen shows the current positions, with a link to the properties for the administrators who have the right.
* **Look**: CSS custom properties of `ontour.css`, one set for the back office and one for the front office (*Site properties ›
  Guided tour configuration › Back office look / Front office look*): `style.bo.color` / `style.fo.color` → `--ontour-color` (launcher,
  and the "Next" / "Done" and "Previous" buttons of the popovers), `style.*.color_contrast` → `--ontour-color-contrast` (both with a color
  picker), `style.*.offset` → `--ontour-offset` (CSS length) and `style.*.z_index` → `--ontour-z-index`. The values are checked by the server
  (color, length, integer) and applied by `ontour.js`; an empty or invalid value keeps the stylesheet default.

## Permissions (RBAC)

Resource type `ONTOUR_TOUR` (*Manage roles*). A resource is a **tour code**: the permissions granted on a tour apply to all its translations.

| Permission | Allows |
|---|---|
| `VIEW` | seeing the tour in the list and its steps |
| `CREATE` (on `*`) | creating and importing tours |
| `MODIFY` | modifying the tour properties, replacing it on import |
| `DELETE` | deleting the tour |
| `MANAGE_STEPS` | adding, modifying, ordering and deleting its steps |
| `TRANSLATE` | creating a translation |
| `EXPORT` | exporting it (*Export all* exports the tours allowed) |
| `RESET` | showing it again to every user |

Every view and action checks its permission (`AccessDeniedException`), and the screens only show the allowed actions. The `ONTOUR_MANAGEMENT`
right is still required to open the feature.

The `ontour_manager` role grants every permission on every tour. A new installation gives it to `admin` (`init_core_ontour.sql`). On an
existing installation, a one-time startup service (`TourRbacInitService`, flag `ontour.rbac.initialized` in the datastore) creates the role and
gives it to the administrators who have the `ONTOUR_MANAGEMENT` right, so that nobody loses access; it never runs again, so later changes made
in *Manage roles* are kept. Renaming the code of a tour detaches it from the permissions granted on its former code.

## How it works

```
webapp/themes/shared/plugins/ontour/
    css/ontour.css, css/driver/driver.css
    js/ontour.js, js/driver/driver.js.iife.js (+ LICENSE)

plugin.xml ─┬─ <admin-javascript-files>  driver.js.iife.js + ontour.js?target=BO   → every BO page
            └─ <javascript-files> (portal scope) driver.js.iife.js + ontour.js?target=FO → every FO page

ontour.js ── GET servlet/plugins/ontour/tours?target=BO|FO&path=<page path>&query=<query string>
              └─ TourServlet → TourService.findMatchingTours → Driver.js configuration (JSON)
          ── starts the automatic tour, renders the launcher, listens to [data-ontour-start]
```

* BO tours are only served to an authenticated administrator (403 otherwise). FO tours are public.
* Page matching (`PageMatcher`):
  * **Page**: path relative to the webapp, `*` wildcard (`jsp/admin/plugins/blog/ManageBlogs.jsp`, `jsp/site/Portal.jsp`).
    An empty page means the tour never starts by itself: only a button or the JS API can start it.
  * **Page parameters**: query string that the URL must contain (`page=blog`, `view=createBlog&id`).
    `*` or no value: the parameter only needs to be present. `a|b`: alternatives. `!`: absent parameter
    (`view=manageBlogs|!` covers the default view and the explicit one). `!name`: the parameter must be absent.
  * **Required element** (checked in the browser): CSS selector of an element the page must contain. It tells apart screens that share
    the same URL, typically views opened by a POST form: plugin-blog opens its create form with a POST `view_createBlog` field, so list,
    creation and edit all live at `ManageBlogs.jsp` and are told apart by `form[name="form-manage"]`, `form[name="create_blog"]` and
    `form[name="modify_blog"]`.

## Including a tour in a plugin (BO and FO): the proposed convention

The integration relies on **three independent levels**. A plugin never gets a compile-time or runtime dependency on onTour:
if onTour isn't installed, none of this has any effect.

### 1. Nothing to code: a tour tied to a page

An administrator creates the tour in *Guided tours*, sets the page (and its parameters), and picks CSS selectors from the page.
The plugin itself doesn't change. The `blog` sample below uses only selectors that already exist in plugin-blog.

### 2. Stable anchors in the plugin's templates (recommended)

CSS classes and ids change with the templates. A plugin that wants durable tours exposes anchors dedicated to onboarding,
with a `data-ontour` attribute, and keeps them stable from one version to the next:

```html
<#-- BO, with the Tabler macros -->
<@button type='submit' name='view_createBlog' buttonIcon='plus' title='#i18n{blog.manage_blogs.buttonAdd}' params='data-ontour="blog-create"' />
<@manageFeature id='blog-list' params='data-ontour="blog-list"'>

<#-- FO -->
<@cTitle level=1 class='blog-title' params='data-ontour="blog-title"'>${blog.contentLabel}</@cTitle>
```

The step selector then becomes `[data-ontour="blog-create"]`.

### 3. Tours shipped by the plugin + an explicit "Guided tour" button

* **Shipping**: the plugin adds a JSON file (onTour export format) in **`webapp/WEB-INF/plugins/ontour/tours/<plugin>.json`**.
  At startup (`PostStartUpService`), onTour imports the tours whose **code and language** don't exist yet. It never overwrites a tour, so the changes made by
  the administrators survive restarts and plugin upgrades. The file is inert when onTour isn't installed.
  Configuration: `ontour.tours.importAtStartup`, `ontour.tours.directory` (`ontour.properties`).
* **Button**: any element carrying `data-ontour-start="<code>"` starts the tour when clicked. Without onTour, the attribute does nothing.

```html
<#-- BO, in the pageHeader of manage_blogs.html -->
<@button buttonIcon='help' title='#i18n{blog.manage_blogs.tour}' hideTitle=['all'] color='secondary' params='data-ontour-start="blog-manage-blogs"' />

<#-- FO -->
<button type="button" class="btn btn-link" data-ontour-start="blog-read-post">Visite guidée</button>
```

* **JS API** (for dynamic pages):

```js
window.LuteceOnTour?.start( 'blog-manage-blogs' );          // tour stored in onTour
window.LuteceOnTour?.start( 'blog-manage-blogs', { onHighlighted: ( el ) => … } );
window.LuteceOnTour?.startConfig( { steps: [ { element: '#x', popover: { title: 'Hi' } } ] } ); // ad hoc tour
window.LuteceOnTour?.reset( );                                // forget the "already seen" tours
```

### Example: plugin-blog (sample data)

[`src/site/resources/samples/blog.json`](src/site/resources/samples/blog.json) describes the plugin-blog features, in French and English, with
selectors that already exist in plugin-blog 4.0.x:

| Code | Target | Page | Required element | Start | Steps |
|---|---|---|---|---|---|
| `blog-manage-blogs` | BO | `jsp/admin/plugins/blog/ManageBlogs.jsp` | `form[name="form-manage"]` | first visit | 10: intro, search, advanced search, add, a post, publication, history, duplicate, preview, bulk actions |
| `blog-create-blog` | BO | `jsp/admin/plugins/blog/ManageBlogs.jsp` | `form[name="create_blog"]` | first visit | 5: title, description, content, properties, create |
| `blog-modify-blog` | BO | `jsp/admin/plugins/blog/ManageBlogs.jsp` | `form[name="modify_blog"]` | first visit | 8: lock, content, properties, publication, history, preview, update, save |
| `blog-manage-tags` | BO | `jsp/admin/plugins/blog/ManageTags.jsp` | `#search_tag` | first visit | 4: tags, filter, create, a tag |
| `blog-read-post` | FO | `jsp/site/Portal.jsp` + `page=blog` | `.blog-title` | manual (launcher) | 3: post, categories and date, author |

### Example: lutece-core admin features (sample data)

[`src/site/resources/samples/core.json`](src/site/resources/samples/core.json) does the same for core features, in French and English:

| Code | Page | Required element | Steps |
|---|---|---|---|
| `core-admin-home` | `jsp/admin/AdminMenu.jsp` | `#dashboard-widgets` | 8: welcome, menu, dashboard, a widget, customize, view the site, your account, tour launcher |
| `core-manage-users` | `jsp/admin/user/ManageUsers.jsp` | `#btn-search-users` | 6: intro, add, search, import/export, advanced parameters, account actions |
| `core-create-user` | `jsp/admin/user/CreateUser.jsp` | `form[name="create_user"]` | 6: identity, login, password, notification, account, create |
| `core-user-rights` | `jsp/admin/user/ModifyUserRights.jsp` | `form[action*="DoModifyUserRights"]` | 4: account tabs, a right, select all, save |
| `core-site-properties` | `jsp/admin/ManageProperties.jsp` | `#search_prop` | 5: intro, filter, groups, a group of properties, save |

The site properties are split into tabs: a hidden element can't be highlighted, so the tour only targets the tab bar, the group of the
**active** tab (`.tab-pane.active [data-prop]`) and the save bar. The property ids contain dots: use attribute selectors
(`[data-property="portal.site.site_property.name"]`) rather than `#…`.

### Example: front office home page of the demo site (sample data)

[`src/site/resources/samples/site-demo.json`](src/site/resources/samples/site-demo.json), tour `site-demo-home` (FO, fr/en, first visit):
12 steps on the logo, the menu, the language switcher, the tagline, the four calls to action (demo, documentation, GitHub, back office),
the highlights, the social footer and the tour launcher. The home page is reachable through several URLs (`/`, `jsp/site/Portal.jsp`,
`?page_id=1`), so the tour targets every page (`*`) and requires the hero buttons (`.hero__cta`). Anonymous visitors: the "seen" state is
kept by the browser.

### Sample script

Both samples are loaded by **`src/sql/plugins/ontour/plugin/init_db_ontour_sample.sql`** (22 tours, 142 steps), run by Liquibase on a new
installation like the other `init_db_*` scripts. The script is generated from the JSON files, so they stay identical:

```bash
python3 src/tools/tours_json_to_sql.py init_db_ontour_sample.sql \
    src/site/resources/samples/core.json src/site/resources/samples/blog.json src/site/resources/samples/site-demo.json \
    > src/sql/plugins/ontour/plugin/init_db_ontour_sample.sql
```

Steps reference their tour by code and language (`INSERT … SELECT`), so the script depends on no generated identifier.

To integrate it into plugin-blog:

1. copy the file to `lutece-cms-plugin-blog/webapp/WEB-INF/plugins/ontour/tours/blog.json`;
2. (recommended) add the `data-ontour` anchors to `manage_blogs.html` and `view_blog.html`, then switch the selectors to them;
3. (optional) add a *Guided tour* button `data-ontour-start="blog-manage-blogs"` in the header of `manage_blogs.html`.

To try it without touching blog: *Guided tours › Import*, then load `blog.json`.

## Installation

```xml
<dependency>
    <groupId>fr.paris.lutece.plugins</groupId>
    <artifactId>plugin-ontour</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <type>lutece-plugin</type>
</dependency>
```

Tables `ontour_tour` and `ontour_step` are created by Liquibase. The `ONTOUR_MANAGEMENT` right is given to the `admin` user.
onTour ships its own tour, `ontour-admin`, which presents the management screen.

## Build

```bash
mvn clean install -Dmaven.test.skip=true
mvn clean lutece:exploded antrun:run -Dlutece-test-hsql test -q
```

## Limits and possible improvements

* The user states are not removed when a user account is deleted (a few rows keyed by the user id).
* No cache on the endpoint: one light SQL query per page view. An `AbstractCacheableService` invalidated when a tour changes can be added
  if front office traffic calls for it.
* Step descriptions are HTML written by administrators and rendered as is by Driver.js (trusted content).
