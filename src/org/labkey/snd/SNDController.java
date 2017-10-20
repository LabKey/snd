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
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.SNDService;
import org.labkey.api.snd.SuperPackage;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SNDController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(SNDController.class);
    public static final String NAME = "snd";

    public SNDController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresPermission(ReadPermission.class)
    public class BeginAction extends RedirectAction
    {
        @Override
        public URLHelper getSuccessURL(Object o)
        {
            return new ActionURL("snd", "app", getContainer());
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
            if (rangeUri.equals(Package.RANGE_PARTICIPANTID))
            {
                json.put("conceptURI", "http://cpas.labkey.com/Study#" + Package.RANGE_PARTICIPANTID);
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
                        errors.reject("Attributes must have unique names within a package.");
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
                List<SuperPackage> uiSubSuperPkgs = new ArrayList<>();

                // create super package for root, if needed
                SuperPackage superPackage = SNDManager.getTopLevelSuperPkg(getContainer(), getUser(), pkg.getPkgId());
                // in cloning case, set package ID to -1 here to force a new package to be created during save
                if (cloneFlag)
                    pkg.setPkgId(-1);
                Integer rootSuperPackageId;
                Integer originalSuperPackageId = null;
                if ((superPackage != null) && cloneFlag)
                    originalSuperPackageId = superPackage.getSuperPkgId();  // will need this later
                if ((superPackage == null) || cloneFlag)
                {
                    superPackage = new SuperPackage();
                    if(testIdNumberStart != -1)
                    {
                        rootSuperPackageId = SNDManager.get().ensureSuperPkgId(getContainer(), testIdNumberStart);
                        testIdNumberStart++;
                    }
                    else
                    {
                        rootSuperPackageId = SNDManager.get().ensureSuperPkgId(getContainer(), null);
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
                        SuperPackage superPkg = new SuperPackage();
                        superPkg.setSuperPkgId(superPkgId);

                        uiSubSuperPkgs.add(superPkg);
                    }

                    List<SuperPackage> topLevelSuperPkgs;
                    List<SuperPackage> cloneSuperPkgs = null;
                    List<SuperPackage> topLevelChildSuperPackages = new ArrayList<>();

                    // step 1: process all top level super packages (i.e. super packages that need to have a new child super package created for them)
                    // if cloning, need to make all new child super packages by getting all top-level super packages for this super package
                    if (cloneFlag)
                    {
                        topLevelSuperPkgs = SNDManager.getTopLevelSuperPkgs(getContainer(), getUser(), originalSuperPackageId);
                        // when cloning, overwrite UI super packages with child super packages from super package being cloned
                        cloneSuperPkgs = SNDManager.getChildSuperPkgs(getContainer(), getUser(), originalSuperPackageId);
                    }
                    // if not cloning, just make new child super packages for super packages which were just added (meaning top-level ones)
                    else
                    {
                        topLevelSuperPkgs = SNDManager.filterTopLevelSuperPkgs(getContainer(), getUser(), uiSubSuperPkgs);
                    }

                    if ((!cloneFlag && (uiSubSuperPkgs != null) && (topLevelSuperPkgs != null))
                            || (cloneFlag && (cloneSuperPkgs != null) && (topLevelSuperPkgs != null)))
                    {
                        // now we need to create a list of child super packages based on topLevelSuperPackages and uiSuperPackageIds
                        // (topLevelSuperPackages is really kind of a list of prototypes to be selected from, sometimes multiple times)
                        if(!cloneFlag)
                        {
                            // CONSIDER: create maps to speed up lookups here if the number of super packages grows large
                            for (SuperPackage uiSuperPackage : uiSubSuperPkgs)
                            {
                                for (SuperPackage topLevelSuperPackage : topLevelSuperPkgs)
                                {
                                    if (uiSuperPackage.getSuperPkgId().equals(topLevelSuperPackage.getSuperPkgId()))
                                    {
                                        // pick a sort order from the UI for this super package ID
                                        LinkedList<Integer> sortOrders = superPkgIdToSortOrdersMap.get(uiSuperPackage.getSuperPkgId());
                                        // set it in the top-level super package
                                        topLevelSuperPackage.setSortOrder(sortOrders.getFirst());
                                        // remove this sort order so we don't use it again (only really useful when there are multiples)
                                        sortOrders.removeFirst();
                                        // use copy constructor to copy this top-level super package into a new child super package
                                        // NOTE: this does not create new grandchild super packages! even in the clone case, this is actually the desired behavior
                                        topLevelChildSuperPackages.add(new SuperPackage(topLevelSuperPackage));
                                        break;
                                    }
                                }
                            }
                        }
                        else // cloning
                        {
                            // CONSIDER: create maps to speed up lookups here if the number of super packages grows large
                            for (SuperPackage cloneSuperPkg : cloneSuperPkgs)
                            {
                                for (SuperPackage topLevelSuperPackage : topLevelSuperPkgs)
                                {

                                    if (cloneSuperPkg.getPkgId().equals(topLevelSuperPackage.getPkgId()))
                                    {
                                        // set sort order in the top-level super package
                                        topLevelSuperPackage.setSortOrder(cloneSuperPkg.getSortOrder());
                                        // use copy constructor to copy this top-level super package into a new child super package
                                        // NOTE: this does not create new grandchild super packages! even in the clone case, this is actually the desired behavior
                                        topLevelChildSuperPackages.add(new SuperPackage(topLevelSuperPackage));
                                        break;
                                    }
                                }
                            }
                        }

                        // next we need to transform the top-level super packages from above into new child super packages
                        for (SuperPackage topLevelChildSuperPackage : topLevelChildSuperPackages)
                        {
                            // make new super package ID since we are copying this top-level super package
                            if (testIdNumberStart != -1)
                            {
                                topLevelChildSuperPackage.setSuperPkgId(SNDManager.get().ensureSuperPkgId(getContainer(), testIdNumberStart));
                                testIdNumberStart++;
                            }
                            else
                            {
                                topLevelChildSuperPackage.setSuperPkgId(SNDManager.get().ensureSuperPkgId(getContainer(), null));
                            }
                            // the parent for this child super package is the current super package being saved
                            topLevelChildSuperPackage.setParentSuperPkgId(rootSuperPackageId);
                        }
                    }

                    // step 2: get existing child super packages and set their sort orders
                    List<SuperPackage> regularChildSuperPackages = null;
                    if (!cloneFlag)  // should never be using any existing child super packages in the clone case
                    {
                        regularChildSuperPackages = SNDManager.filterChildSuperPkgs(getContainer(), getUser(), uiSubSuperPkgs, rootSuperPackageId);
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
                errors.reject(ERROR_MSG, "Package IDs not defined.");
        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors) throws Exception
        {
            JSONObject json = form.getJsonObject();
            JSONArray pkgIds = json.getJSONArray("packages");
            boolean includeExtraFields = !json.has("excludeExtraFields") || !json.getBoolean("excludeExtraFields");
            boolean includeLookups = !json.has("excludeLookups") || !json.getBoolean("excludeLookups");
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
                    pkgs.addAll(sndService.getPackages(getViewContext().getContainer(), getUser(), ids, includeExtraFields, includeLookups));
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
}