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
import org.labkey.api.action.SimpleApiJsonForm;
import org.labkey.api.action.SimpleViewAction;
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
import org.labkey.api.view.NavTree;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SNDController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(SNDController.class);
    public static final String NAME = "snd";

    public SNDController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresPermission(ReadPermission.class)
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            return null;
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class SavePackageAction extends ApiAction<SimpleApiJsonForm>
    {
        private GWTPropertyDescriptor convertJsonToPropertyDescriptor(JSONObject json)
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

            json.put("defaultTypeValue", DefaultValueType.FIXED_EDITABLE.toString());
            json.put("rangeURI", "http://www.w3.org/2001/XMLSchema#" + rangeUri);

            return ExperimentService.get().convertJsonToPropertyDescriptor(json);
        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors) throws Exception
        {
            JSONObject json = form.getJsonObject();
            Package pkg = new Package();
            boolean jsonCloneFlag = json.optBoolean("isCloning");
            Integer jsonPkgId = json.optInt("id", -1);
            if(jsonCloneFlag)
                pkg.setPkgId(-1);
            else
                pkg.setPkgId(jsonPkgId);

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
            if (null != attribs)
            {
                List<GWTPropertyDescriptor> pds = new ArrayList<>();
                for (int i = 0; i < attribs.length(); i++)
                {
                    pds.add(convertJsonToPropertyDescriptor(attribs.getJSONObject(i)));
                }
                pkg.setAttributes(pds);
            }

            // Get super packages
            JSONArray jsonSubPackages = json.getJSONArray("subPackages");  // only first-level children (as super package IDs) should be here
            Map<Integer, Integer> superPkgIdToSortOrderMap = new HashMap<>();
            // create super package for root, if needed

            SuperPackage superPackage = SNDManager.getTopLevelSuperPkg(getContainer(), getUser(), pkg.getPkgId());
            Integer rootSuperPackageId;
            if(superPackage == null)
            {
                superPackage = new SuperPackage();
                rootSuperPackageId = SNDManager.get().ensureSuperPkgId(getContainer(), null);
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
                    superPkgIdToSortOrderMap.put(jsonSubPackage.getInt("superPkgId"), jsonSubPackage.getInt("sortOrder"));
                }

                // get top-level packages that correspond to children

                Set<Integer> superPackageIds = superPkgIdToSortOrderMap.keySet();
                List<SuperPackage> topLevelSuperPackages;

                // if cloning, need to make all new child super packages by getting all top-level super packages for this super package
                if(jsonCloneFlag)
                {
                    topLevelSuperPackages = SNDManager.getTopLevelSuperPkgs(getContainer(), getUser(), jsonPkgId);
                }
                else
                {
                    topLevelSuperPackages = SNDManager.getTopLevelSuperPkgs(getContainer(), getUser(), superPackageIds);
                }

                if (topLevelSuperPackages != null)
                {
                    // sort order is only thing we need from UI, so set it here in the mostly-complete super packages
                    for (SuperPackage topLevelSuperPackage : topLevelSuperPackages)
                    {
                        topLevelSuperPackage.setSortOrder(superPkgIdToSortOrderMap.get(topLevelSuperPackage.getSuperPkgId()));
                        // now set new super package ID (so that this can be a properly-created child super package later)
                        topLevelSuperPackage.setSuperPkgId(SNDManager.get().ensureSuperPkgId(getContainer(), null));
                        // the parent for this child super package is the current super package being saved
                        topLevelSuperPackage.setParentSuperPkgId(rootSuperPackageId);
                    }
                }

                // topLevelSuperPackages is now actually a collection of child super packages
                // next get existing child super packages and set their sort orders

                List<SuperPackage> childSuperPackages = null;
                if(!jsonCloneFlag)
                {
                    childSuperPackages = SNDManager.getChildSuperPkgs(getContainer(), getUser(), superPackageIds, rootSuperPackageId);
                    if (childSuperPackages != null)
                    {
                        for (SuperPackage childSuperPackage : childSuperPackages)
                        {
                            childSuperPackage.setSortOrder(superPkgIdToSortOrderMap.get(childSuperPackage.getSuperPkgId()));
                        }
                    }
                }

                ArrayList<SuperPackage> subPackages = new ArrayList<>();
                if (topLevelSuperPackages != null)
                    subPackages.addAll(topLevelSuperPackages);
                if (childSuperPackages != null)
                    subPackages.addAll(childSuperPackages);
                pkg.setSubpackages(subPackages);
            }

            SNDService.get().savePackage(getViewContext().getContainer(), getUser(), pkg, superPackage, jsonCloneFlag);

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