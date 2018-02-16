/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.labkey.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.ApiResponse;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.RedirectAction;
import org.labkey.api.action.SimpleApiJsonForm;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.gwt.client.DefaultValueType;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.RequiresLogin;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.snd.AttributeData;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.EventData;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.Project;
import org.labkey.api.snd.ProjectItem;
import org.labkey.api.snd.SNDSequencer;
import org.labkey.api.snd.SNDService;
import org.labkey.api.snd.SuperPackage;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SNDController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(SNDController.class);
    public static final String NAME = "snd" ;

    public SNDController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresLogin 
    public class BeginAction extends RedirectAction
    {
        @Override
        public URLHelper getSuccessURL(Object o)
        {
            return new ActionURL(NAME, "app", getContainer());
        }

        @Override
        public boolean doAction(Object o, BindException errors) throws Exception
        {
            return true;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class SavePackageAction extends ApiAction<SimpleApiJsonForm>
    {
        private GWTPropertyDescriptor convertJsonToPropertyDescriptor(JSONObject json, BindException errors)
        {
            String rangeUri = json.getString("rangeURI");
            if (rangeUri.equals(SNDManager.RANGE_PARTICIPANTID))
            {
                json.put("conceptURI", "http://cpas.labkey.com/Study#" + SNDManager.RANGE_PARTICIPANTID);
                rangeUri = "string";
            }
            if (rangeUri.equals("string"))
            {
                json.put("scale", 4000);
            }

            json.put("rangeURI", "http://www.w3.org/2001/XMLSchema#" + rangeUri);
            json.put("defaultTypeValue", DefaultValueType.FIXED_EDITABLE.toString());

            String defaultValue = (String) json.get("defaultValue");
            String lookupSchema = (String) json.get("lookupSchema");
            String lookupQuery = (String) json.get("lookupQuery");
            if ((defaultValue != null && !defaultValue.isEmpty())
                    && (lookupSchema != null && !lookupSchema.isEmpty())
                    && (lookupQuery != null && !lookupQuery.isEmpty()))
            {
                Object defPk = SNDManager.get().normalizeLookupDefaultValue(getUser(), getContainer(), lookupSchema, lookupQuery, defaultValue);
                if (defPk == null)
                {
                    errors.reject(ERROR_MSG, "Unable to resolve default value " + defaultValue + " for assigned lookup key.");
                }
                else
                {
                    String defStrPk = "";
                    if (String.class.isInstance(defPk))
                    {
                        defStrPk = (String) defPk;
                    }
                    else if (Integer.class.isInstance(defPk))
                    {
                        defStrPk = Integer.toString((Integer) defPk);
                    }

                    if (!defStrPk.equals(""))
                        json.put("defaultValue", defStrPk);
                }
            }

            return ExperimentService.get().convertJsonToPropertyDescriptor(json);
        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors) throws Exception
        {
            JSONObject json = form.getJsonObject();
            Package pkg = new Package();
            boolean cloneFlag = json.optBoolean("clone");
            Integer testIdNumberStart = json.optInt("testIdNumberStart", -1);
            Integer pkgId = json.optInt("id", -1);
            pkg.setPkgId(pkgId);

            pkg.setDescription(json.getString("description"));
            pkg.setActive(json.getBoolean("active"));
            pkg.setRepeatable(json.getBoolean("repeatable"));
            pkg.setNarrative(json.getString("narrative"));

            // Get extra fields
            JSONArray jsonExtras = json.optJSONArray("extraFields");
            if (null != jsonExtras)
            {
                Map<GWTPropertyDescriptor, Object> extras = new HashMap<>();
                JSONObject extra;
                for (int e = 0; e < jsonExtras.length(); e++)
                {
                    extra = jsonExtras.getJSONObject(e);
                    extras.put(ExperimentService.get().convertJsonToPropertyDescriptor(extra), extra.get("value"));
                }
                pkg.setExtraFields(extras);
            }

            // Get categories
            JSONArray jsonCategories = json.getJSONArray("categories");
            if (null != jsonCategories)
            {
                List<Integer> categories = new ArrayList<>();
                for (int j = 0; j < jsonCategories.length(); j++)
                {
                    categories.add(jsonCategories.getInt(j));
                }
                pkg.setCategories(categories);
            }

            // Get attributes
            JSONArray attribs = json.optJSONArray("attributes");
            List<String> attNames = new ArrayList<>();
            if (null != attribs)
            {
                List<GWTPropertyDescriptor> pds = new ArrayList<>();
                String name;
                for (int i = 0; i < attribs.length(); i++)
                {
                    name = attribs.getJSONObject(i).getString("name");
                    if (attNames.contains(name))
                    {
                        errors.reject(ERROR_MSG, "Attributes must have unique names within a package.");
                        break;
                    }
                    attNames.add(name);
                    pds.add(convertJsonToPropertyDescriptor(attribs.getJSONObject(i), errors));
                }
                pkg.setAttributes(pds);
            }

            if (!errors.hasErrors())
            {
                // Get super packages
                JSONArray jsonSubPackages = json.getJSONArray("subPackages");  // only first-level children (as super package IDs) should be here
                Map<Integer, LinkedList<Integer>> superPkgIdToSortOrdersMap = new HashMap<>();  // uses lists because top-level super package IDs might show up multiple times
                List<Integer> uiSubSuperPkgIds = new ArrayList<>();

                // create super package for root, if needed
                SuperPackage superPackage = SNDManager.getTopLevelSuperPkgForPkg(getContainer(), getUser(), pkg.getPkgId());
                // in cloning case, set package ID to -1 here to force a new package to be created during save
                if (cloneFlag)
                    pkg.setPkgId(-1);
                Integer rootSuperPackageId;
                if ((superPackage == null) || cloneFlag)
                {
                    superPackage = new SuperPackage();
                    if (testIdNumberStart != -1)
                    {
                        rootSuperPackageId = SNDSequencer.SUPERPKGID.ensureId(getContainer(), testIdNumberStart);
                        testIdNumberStart++;
                    }
                    else
                    {
                        rootSuperPackageId = SNDSequencer.SUPERPKGID.ensureId(getContainer(), null);
                    }
                    superPackage.setSuperPkgId(rootSuperPackageId);
                    superPackage.setSuperPkgPath(Integer.toString(rootSuperPackageId));
                }
                else
                {
                    rootSuperPackageId = superPackage.getSuperPkgId();
                }

                if (null != jsonSubPackages && jsonSubPackages.length() > 0)
                {
                    for (int i = 0; i < jsonSubPackages.length(); i++)
                    {
                        JSONObject jsonSubPackage = jsonSubPackages.getJSONObject(i);
                        Integer superPkgId = jsonSubPackage.getInt("superPkgId");

                        if (SNDManager.get().isDescendent(getContainer(), getUser(), superPkgId, pkgId))
                        {
                            errors.reject(ERROR_MSG, "Circular package hierarchy error. Verify assigned packages do not contain this package.");
                            break;
                        }

                        uiSubSuperPkgIds.add(superPkgId);
                        LinkedList<Integer> sortOrders = superPkgIdToSortOrdersMap.get(superPkgId);
                        if (sortOrders == null)
                        {
                            sortOrders = new LinkedList<>();
                            sortOrders.add(jsonSubPackage.getInt("sortOrder"));
                            superPkgIdToSortOrdersMap.put(superPkgId, sortOrders);
                        }
                        else
                        {
                            sortOrders.add(jsonSubPackage.getInt("sortOrder"));
                        }
                    }

                    List<SuperPackage> topLevelSuperPkgs;
                    List<SuperPackage> topLevelChildSuperPackages = new ArrayList<>();
                    if (!errors.hasErrors())
                    {
                        // step 1: process all top level super packages (i.e. super packages that need to have a new child super package created for them)
                        // if cloning, need to make all new child super packages by getting all top-level super packages for this super package
                        if (cloneFlag)
                        {
                            topLevelSuperPkgs = SNDManager.convertToTopLevelSuperPkgs(getContainer(), getUser(), uiSubSuperPkgIds);
                            List<SuperPackage> uiSuperPkgs = SNDManager.getSuperPkgs(getContainer(), getUser(), uiSubSuperPkgIds);
                            Map<Integer, Integer> superPkgIdToPkgIdMap = new HashMap<>();

                            // need to get proper package IDs from db since they're not coming in from UI
                            if (uiSuperPkgs != null)
                            {
                                for (SuperPackage superPkg : uiSuperPkgs)
                                {
                                    superPkgIdToPkgIdMap.put(superPkg.getSuperPkgId(), superPkg.getPkgId());
                                }
                            }

                            if (topLevelSuperPkgs != null)
                            {
                                // now we need to create a list of child super packages based on topLevelSuperPackages and uiSuperPackageIds
                                // (topLevelSuperPkgs is really kind of a list of prototypes to be selected from, sometimes multiple times)

                                // CONSIDER: create maps to speed up lookups here if the number of super packages grows large
                                for (Integer uiSubSuperPkgId : uiSubSuperPkgIds)
                                {
                                    for (SuperPackage topLevelSuperPkg : topLevelSuperPkgs)
                                    {
                                        if (superPkgIdToPkgIdMap.get(uiSubSuperPkgId).equals(topLevelSuperPkg.getPkgId()))
                                        {
                                            // pick a sort order from the UI for this super package ID
                                            LinkedList<Integer> sortOrders = superPkgIdToSortOrdersMap.get(uiSubSuperPkgId);
                                            // set it in the top-level super package
                                            topLevelSuperPkg.setSortOrder(sortOrders.getFirst());
                                            // remove this sort order so we don't use it again (only really useful when there are multiples)
                                            sortOrders.removeFirst();
                                            // use copy constructor to copy this top-level super package into a new child super package
                                            // NOTE: this does not create new grandchild super packages! even in the clone case, this is actually the desired behavior
                                            topLevelChildSuperPackages.add(new SuperPackage(topLevelSuperPkg));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        // if not cloning, just make new child super packages for super packages which were just added (meaning top-level ones)
                        else
                        {
                            topLevelSuperPkgs = SNDManager.filterTopLevelSuperPkgs(getContainer(), getUser(), uiSubSuperPkgIds);

                            if (topLevelSuperPkgs != null)
                            {
                                // now we need to create a list of child super packages based on topLevelSuperPackages and uiSuperPackageIds
                                // (topLevelSuperPkgs is really kind of a list of prototypes to be selected from, sometimes multiple times)

                                // CONSIDER: create maps to speed up lookups here if the number of super packages grows large
                                for (Integer uiSuperPkgId : uiSubSuperPkgIds)
                                {
                                    for (SuperPackage topLevelSuperPkg : topLevelSuperPkgs)
                                    {
                                        if (uiSuperPkgId.equals(topLevelSuperPkg.getSuperPkgId()))
                                        {
                                            // pick a sort order from the UI for this super package ID
                                            LinkedList<Integer> sortOrders = superPkgIdToSortOrdersMap.get(uiSuperPkgId);
                                            // set it in the top-level super package
                                            topLevelSuperPkg.setSortOrder(sortOrders.getFirst());
                                            // remove this sort order so we don't use it again (only really useful when there are multiples)
                                            sortOrders.removeFirst();
                                            // use copy constructor to copy this top-level super package into a new child super package
                                            // NOTE: this does not create new grandchild super packages! even in the clone case, this is actually the desired behavior
                                            topLevelChildSuperPackages.add(new SuperPackage(topLevelSuperPkg));
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (topLevelSuperPkgs != null)
                        {
                            // next we need to transform the top-level super packages from above into new child super packages
                            for (SuperPackage topLevelChildSuperPackage : topLevelChildSuperPackages)
                            {
                                // make new super package ID since we are copying this top-level super package
                                if (testIdNumberStart != -1)
                                {
                                    topLevelChildSuperPackage.setSuperPkgId(SNDSequencer.SUPERPKGID.ensureId(getContainer(), testIdNumberStart));
                                    testIdNumberStart++;
                                }
                                else
                                {
                                    topLevelChildSuperPackage.setSuperPkgId(SNDSequencer.SUPERPKGID.ensureId(getContainer(), null));
                                }
                                // the parent for this child super package is the current super package being saved
                                topLevelChildSuperPackage.setParentSuperPkgId(rootSuperPackageId);
                            }
                        }

                        // step 2: get existing child super packages and set their sort orders
                        List<SuperPackage> regularChildSuperPackages = null;
                        if (!cloneFlag)  // should never be using any existing child super packages in the clone case
                        {
                            regularChildSuperPackages = SNDManager.filterChildSuperPkgs(getContainer(), getUser(), uiSubSuperPkgIds, rootSuperPackageId);
                            if (regularChildSuperPackages != null)
                            {
                                for (SuperPackage childSuperPackage : regularChildSuperPackages)
                                {
                                    // pick a sort order from the UI for this super package ID (there should only be one here in this case)
                                    LinkedList<Integer> sortOrders = superPkgIdToSortOrdersMap.get(childSuperPackage.getSuperPkgId());
                                    childSuperPackage.setSortOrder(sortOrders.getFirst());
                                    // don't bother deleting a sort order from sortOrders after using it (like we did above), since repeats should not be possible
                                }
                            }
                        }

                        // now that both steps are complete, set subPackages to be all the new or modified super packages and save
                        ArrayList<SuperPackage> subSuperPackages = new ArrayList<>();
                        subSuperPackages.addAll(topLevelChildSuperPackages);
                        if (regularChildSuperPackages != null)
                            subSuperPackages.addAll(regularChildSuperPackages);
                        pkg.setSubpackages(subSuperPackages);
                    }
                }
                if (!errors.hasErrors())
                    SNDService.get().savePackage(getViewContext().getContainer(), getUser(), pkg, superPackage, cloneFlag);
            }

            return new ApiSimpleResponse();
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class GetPackagesAction extends ApiAction<SimpleApiJsonForm>
    {
        @Override
        public void validateForm(SimpleApiJsonForm form, Errors errors)
        {
            JSONObject json = form.getJsonObject();
            if (json == null)
            {
                errors.reject(ERROR_MSG, "Missing json parameter.");
                return;
            }

            JSONArray pkgIds = json.has("packages") ? json.getJSONArray("packages") : null;
            if (pkgIds == null)
            {
                errors.reject(ERROR_MSG, "Package IDs not defined.");
            }
            else
            {
                for (int j = 0; j < pkgIds.length(); j++)
                {
                    try
                    {
                        pkgIds.getInt(j);  // just trying to parse as int
                    }
                    catch (Exception e)
                    {
                        errors.reject(ERROR_MSG, "Package IDs are present but not valid integers.");
                    }
                }
            }
        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors) throws Exception
        {
            JSONObject json = form.getJsonObject();
            JSONArray pkgIds = json.getJSONArray("packages");
            boolean includeExtraFields = !json.has("excludeExtraFields") || !json.getBoolean("excludeExtraFields");
            boolean includeLookups = !json.has("excludeLookups") || !json.getBoolean("excludeLookups");
            boolean includeAllAttributes = json.has("includeAllAttributes") && json.getBoolean("includeAllAttributes");
            ApiSimpleResponse response = new ApiSimpleResponse();

            List<Package> pkgs = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();

            // Query on new form to get form metadata
            if (pkgIds.getInt(0) == -1)
            {
                Package newPkg = new Package();
                newPkg = SNDManager.get().addExtraFieldsToPackage(getViewContext().getContainer(), getUser(), newPkg, null);
                newPkg = SNDManager.get().addLookupsToPkg(getViewContext().getContainer(), getUser(), newPkg);
                pkgs.add(newPkg);
            }
            else // Existing pkg
            {
                int id;
                for (int j = 0; j < pkgIds.length(); j++)
                {
                    id = pkgIds.getInt(j);
                    if (id > -1)
                        ids.add(id);
                }

                SNDService sndService = SNDService.get();
                if (ids.size() > 0 && sndService != null)
                    pkgs.addAll(sndService.getPackages(getViewContext().getContainer(), getUser(), ids,
                            includeExtraFields, includeLookups, includeAllAttributes));
            }

            JSONArray jsonOut = new JSONArray();
            JSONObject jsonObj;
            for (Package pkg : pkgs)
            {
                jsonObj = pkg.toJSON(getViewContext().getContainer(), getUser());
                jsonOut.put(jsonObj);
            }

            response.put("json", jsonOut);
            return response;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class SaveProjectAction extends ApiAction<SimpleApiJsonForm>
    {

        public static final String dateFormat = "yyyy-MM-dd";

        @Override
        public void validateForm(SimpleApiJsonForm form, Errors errors)
        {
            JSONObject json = form.getJsonObject();
            if (json == null)
            {
                errors.reject(ERROR_MSG, "Missing json parameter.");
                return;
            }

            if (!json.has("description") || json.getString("description") == null)
            {
                errors.reject(ERROR_MSG, "Missing description.");
            }

            if (!json.has("active"))
            {
                errors.reject(ERROR_MSG, "Missing active.");
            }

            if (!json.has("referenceId"))
            {
                errors.reject(ERROR_MSG, "Missing referenceId.");
            }

            if (!json.has("startDate") || json.getString("startDate") == null || json.getString("startDate").equals(""))
            {
                errors.reject(ERROR_MSG, "Missing startDate.");
            }

            if (!json.has("isEdit"))
            {
                json.put("isEdit", false);
            }

            if (!json.has("isRevision"))
            {
                json.put("isRevision", false);

            }

            if (json.getBoolean("isEdit") && json.getBoolean("isRevision"))
            {
                errors.reject(ERROR_MSG, "isEdit and isRevision cannot both be true.");
            }

            if (json.getBoolean("isEdit"))
            {
                if (!json.has("projectId") || json.getInt("projectId") < 0)
                {
                    errors.reject(ERROR_MSG, "Must provide valid project id when isEdit is true.");
                }
                else
                {
                    if (!json.has("revisionNum") || json.getInt("projectId") < 0)
                    {
                        errors.reject(ERROR_MSG, "Must provide valid revision number when isEdit is true.");
                    }
                }
            }

            if (json.getBoolean("isRevision"))
            {
                if (!json.has("projectId") || json.getInt("projectId") < 0)
                {
                    errors.reject(ERROR_MSG, "Must provide valid project id when isRevision is true.");
                }
                else
                {
                    if (!json.has("revisionNum") || json.getInt("projectId") < 0)
                    {
                        errors.reject(ERROR_MSG, "Must provide valid revision number when isRevision is true.");
                    }
                }
            }

            if (json.has("endDate") && (json.getString("endDate") == null || json.getString("endDate").equals("")))
            {
                json.remove("endDate");
            }

            if (json.has("endDate") && json.getString("endDate") != null)
            {
                SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                try
                {
                    Date start = formatter.parse(json.getString("startDate"));
                    Date end = formatter.parse(json.getString("endDate"));

                    if (end.before(start))
                        errors.reject(ERROR_MSG, "End date must not be before start date.");
                }
                catch (ParseException e)
                {
                    errors.reject(ERROR_MSG, e.getMessage());
                }
            }

            form.bindProperties(json);
        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors) throws Exception
        {
            JSONObject json = form.getJsonObject();

            int id = json.optInt("projectId", -1);
            Project project = new Project(id, json.optInt("revisionNum", 0), json.getBoolean("isEdit"),
                    json.getBoolean("isRevision"), getViewContext().getContainer());

            project.setDescription(json.getString("description"));
            project.setActive(json.getBoolean("active"));
            project.setReferenceId(json.getInt("referenceId"));

            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            if (json.getBoolean("isRevision"))
            {
                project.setCopyRevisedPkgs(json.getBoolean("copyRevisedPkgs"));
                if (json.has("endDateRevised") && json.getString("endDateRevised") != null)
                {
                    project.setEndDateRevised(formatter.parse(json.getString("endDateRevised")));
                }
                else
                {
                    project.setEndDateRevised(null);
                }
            }

            project.setStartDate(formatter.parse(json.getString("startDate")));
            if (json.has("endDate") && json.getString("endDate") != null)
                project.setEndDate(formatter.parse(json.getString("endDate")));
            else
                project.setEndDate(null);

            // Get extra fields
            JSONArray jsonExtras = json.optJSONArray("extraFields");
            if (null != jsonExtras)
            {
                Map<GWTPropertyDescriptor, Object> extras = new HashMap<>();
                JSONObject extra;
                for (int e = 0; e < jsonExtras.length(); e++)
                {
                    extra = jsonExtras.getJSONObject(e);
                    extras.put(ExperimentService.get().convertJsonToPropertyDescriptor(extra), extra.get("value"));
                }
                project.setExtraFields(extras);
            }

            // Parse project items. Not done for revision
            JSONArray jsonPkgs = json.optJSONArray("projectItems");
            if (null != jsonPkgs && !json.getBoolean("isRevision"))
            {
                List<ProjectItem> projectItems = new ArrayList<>();
                JSONObject jsonItem;
                ProjectItem projectItem;
                Object superPkgId;
                for (int i = 0; i < jsonPkgs.length(); i++)
                {
                    jsonItem = jsonPkgs.getJSONObject(i);
                    projectItem = new ProjectItem();
                    projectItem.setActive(jsonItem.getBoolean("active"));
                    superPkgId = jsonItem.get("superPkgId");
                    if (superPkgId == null)
                    {
                        errors.reject(ERROR_MSG, "Project item missing super package id.");
                    }
                    else
                    {
                        projectItem.setSuperPkgId((Integer) superPkgId);
                        projectItem.setParentObjectId(project.getObjectId());
                        projectItems.add(projectItem);
                    }
                }
                project.setProjectItems(projectItems);
            }

            if (!errors.hasErrors())
                SNDService.get().saveProject(getViewContext().getContainer(), getUser(), project, json.getBoolean("isRevision"));

            return new ApiSimpleResponse();
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class GetProjectAction extends ApiAction<SimpleApiJsonForm>
    {
        @Override
        public void validateForm(SimpleApiJsonForm form, Errors errors)
        {
            JSONObject json = form.getJsonObject();
            if (json == null)
            {
                errors.reject(ERROR_MSG, "Missing json parameter.");
                return;
            }

            if (!json.has("projectId") || json.get("projectId") == null)
            {
                errors.reject(ERROR_MSG, "Missing required json parameter: projectId.");
            }

            if (!json.has("revisionNum") || json.get("revisionNum") == null)
            {
                errors.reject(ERROR_MSG, "Missing required json parameter: revisionNum.");
            }
        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors) throws Exception
        {
            JSONObject json = form.getJsonObject();
            ApiSimpleResponse response = new ApiSimpleResponse();

            int projectId = json.getInt("projectId");
            int revisionNum = json.getInt("revisionNum");

            Project project;

            // Query on new form to get form metadata
            if (projectId == -1)
            {
                project = new Project();
                project = SNDManager.get().addExtraFieldsToProject(getViewContext().getContainer(), getUser(), project, null);
            }
            else // Existing project
            {
                project = SNDService.get().getProject(getViewContext().getContainer(), getUser(), projectId, revisionNum);
            }

            if (project != null)
            {
                response.put("json", project.toJSON(getViewContext().getContainer(), getUser()));
            }

            return response;

        }
    }

    @RequiresPermission(AdminPermission.class)
    public class GetEventAction extends ApiAction<SimpleApiJsonForm>
    {
        @Override
        public void validateForm(SimpleApiJsonForm form, Errors errors)
        {
            JSONObject json = form.getJsonObject();
            if (json == null)
            {
                errors.reject(ERROR_MSG, "Missing json parameter.");
                return;
            }

            if (!json.has("eventId") || json.get("eventId") == null)
            {
                errors.reject(ERROR_MSG, "Missing required json parameter: eventId.");
            }
            else
            {
                try
                {
                    json.getInt("eventId");  // just trying to parse as int
                }
                catch (Exception e)
                {
                    errors.reject(ERROR_MSG, "eventId is present but not a valid integer.");
                }
            }

        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors)
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            JSONObject json = form.getJsonObject();

            int eventId = json.getInt("eventId");
            Event event;

            // Query on new form to get form metadata
            if (eventId == -1)
            {
                event = SNDManager.get().getEmptyEvent(getContainer(), getUser());
            }
            else
            {
                event = SNDService.get().getEvent(getContainer(), getUser(), json.getInt("eventId"));
            }
            if (event != null)
            {
                JSONObject eventJson = event.toJSON(getViewContext().getContainer(), getUser());
                response.put("json", eventJson);
            }
            return response;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class SaveEventAction extends ApiAction<SimpleApiJsonForm>
    {
        @Override
        public void validateForm(SimpleApiJsonForm form, Errors errors)
        {
            JSONObject json = form.getJsonObject();
            if (json == null)
            {
                errors.reject(ERROR_MSG, "Missing json parameter.");
                return;
            }

            if (json.has("eventId"))
            {
                try
                {
                    json.getInt("eventId");  // just trying to parse as int
                }
                catch (Exception e)
                {
                    errors.reject(ERROR_MSG, "eventId is present but not a valid integer.");
                }
            }

            if (!json.has("participantId") || json.get("participantId") == null)
            {
                errors.reject(ERROR_MSG, "Missing required json parameter: participantId.");
            }
            if (!json.has("projectIdRev") || json.get("projectIdRev") == null)
            {
                errors.reject(ERROR_MSG, "Missing required json parameter: projectIdRev.");
            }

            JSONArray eventDataJson = json.has("eventData") ? json.getJSONArray("eventData") : null;
            validateEventData(eventDataJson, errors);
        }

        private void validateEventData(JSONArray eventDataJson, Errors errors)
        {
            if (eventDataJson != null)
            {
                for (int i = 0; i < eventDataJson.length(); i++)
                {
                    JSONObject eventDatumJson = (JSONObject)eventDataJson.get(i);

                    if (eventDatumJson.has("eventDataId"))
                    {
                        try
                        {
                            eventDatumJson.getInt("eventDataId");  // just trying to parse as int
                        }
                        catch (Exception e)
                        {
                            errors.reject(ERROR_MSG, "eventDataId is present but not a valid integer.");
                        }
                    }

                    if (!eventDatumJson.has("superPkgId") || eventDatumJson.get("superPkgId") == null)
                    {
                        errors.reject(ERROR_MSG, "Missing required json parameter: superPkgId.");
                    }

                    JSONArray eventDataChildrenJson = eventDatumJson.has("eventData") ? eventDatumJson.getJSONArray("eventData") : null;
                    validateEventData(eventDataChildrenJson, errors);
                    JSONArray attributesJson = eventDatumJson.has("attributes") ? eventDatumJson.getJSONArray("attributes") : null;
                    if ((attributesJson == null))
                    {
                        errors.reject(ERROR_MSG, "Missing json parameter: attributes");
                    }
                    validateAttributes(attributesJson, errors);
                }
            }
        }

        private void validateAttributes(JSONArray attributesDataJson, Errors errors)
        {
            if (attributesDataJson != null)
            {
                for (int i = 0; i < attributesDataJson.length(); i++)
                {
                    JSONObject attributeJson = (JSONObject)attributesDataJson.get(i);

                    if (!attributeJson.has("propertyId") || attributeJson.get("propertyId") == null)
                    {
                        errors.reject(ERROR_MSG, "Missing required json parameter: propertyId.");
                    }
                    if (!attributeJson.has("value") || attributeJson.get("value") == null)
                    {
                        errors.reject(ERROR_MSG, "Missing required json parameter: value.");
                    }
                }
            }
        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors)
        {
            JSONObject json = form.getJsonObject();
            Integer eventId = json.has("eventId") ? json.getInt("eventId") : null;
            int participantId = json.getInt("participantId");
            String dateString = json.getString("date");

            Date date = null;
            try
            {
                date = (dateString == null ? new Date() : Event.dateFormatter.parse(dateString));
            }
            catch (ParseException e)
            {
                errors.reject(ERROR_MSG, e.getMessage());
            }

            String projectIdrev = json.getString("projectIdRev");
            String note = json.getString("note");

            List<EventData> eventData = null;
            JSONArray eventDataJson = json.has("eventData") ? json.getJSONArray("eventData") : null;
            if (eventDataJson != null)
                eventData = parseEventData(eventDataJson);

            Event event = new Event(eventId, participantId, date, projectIdrev, note, eventData, getContainer());

            // Get extra fields
            JSONArray jsonExtras = json.optJSONArray("extraFields");
            if (null != jsonExtras)
            {
                Map<GWTPropertyDescriptor, Object> extras = new HashMap<>();
                JSONObject extra;
                for (int e = 0; e < jsonExtras.length(); e++)
                {
                    extra = jsonExtras.getJSONObject(e);
                    extras.put(ExperimentService.get().convertJsonToPropertyDescriptor(extra), extra.get("value"));
                }
                event.setExtraFields(extras);
            }

            SNDService.get().saveEvent(getContainer(), getUser(), event);
            return new ApiSimpleResponse();
        }

        private List<EventData> parseEventData(JSONArray eventDataJson)
        {
            List<EventData> eventDataList = new ArrayList<>();

            if ((eventDataJson != null) && (eventDataJson.length() > 0))
            {
                for (int i = 0; i < eventDataJson.length(); i++)
                {
                    JSONObject eventDatumJson = (JSONObject)eventDataJson.get(i);

                    Integer eventDataId = eventDatumJson.has("eventDataId") ? eventDatumJson.getInt("eventDataId") : null;
                    int superPackageId = eventDatumJson.getInt("superPkgId");

                    List<EventData> eventDataChildren;
                    JSONArray eventDataChildrenJson = eventDatumJson.has("subPackages") ? eventDatumJson.getJSONArray("subPackages") : null;
                    eventDataChildren = parseEventData(eventDataChildrenJson);

                    List<AttributeData> attributes;
                    JSONArray attributesJson = eventDatumJson.has("attributes") ? eventDatumJson.getJSONArray("attributes") : null;
                    attributes = parseAttributeData(attributesJson);

                    EventData eventData = new EventData(eventDataId, superPackageId, null, eventDataChildren, attributes);

                    // Get extra fields
                    JSONArray jsonExtras = eventDatumJson.optJSONArray("extraFields");
                    if (null != jsonExtras)
                    {
                        Map<GWTPropertyDescriptor, Object> extras = new HashMap<>();
                        JSONObject extra;
                        for (int e = 0; e < jsonExtras.length(); e++)
                        {
                            extra = jsonExtras.getJSONObject(e);
                            extras.put(ExperimentService.get().convertJsonToPropertyDescriptor(extra), extra.get("value"));
                        }
                        eventData.setExtraFields(extras);
                    }

                    // narrative not used for saving, so make it null
                    eventDataList.add(eventData);
                }
            }

            if (!eventDataList.isEmpty())
                return eventDataList;
            else
                return null;
        }

        private List<AttributeData> parseAttributeData(JSONArray attributesDataJson)
        {
            List<AttributeData> attributesDataList = new ArrayList<>();

            if ((attributesDataJson != null) && (attributesDataJson.length() > 0))
            {
                for (int i = 0; i < attributesDataJson.length(); i++)
                {
                    JSONObject attributeJson = (JSONObject)attributesDataJson.get(i);

                    int propertyId = attributeJson.getInt("propertyId");
                    String value = attributeJson.getString("value");

                    // propertyDescriptor not used for saving, so make it null
                    attributesDataList.add(new AttributeData(propertyId, null, value));
                }
            }

            if (!attributesDataList.isEmpty())
                return attributesDataList;
            else
                return null;
        }
    }
}