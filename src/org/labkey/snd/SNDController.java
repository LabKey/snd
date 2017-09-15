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
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.SNDService;
import org.labkey.api.view.NavTree;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
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
        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors) throws Exception
        {
            JSONObject json = form.getJsonObject();
            Package pkg = new Package();
            pkg.setPkgId(json.optInt("id", 0));

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
                    pds.add(ExperimentService.get().convertJsonToPropertyDescriptor(attribs.getJSONObject(i)));
                }
                pkg.setAttributes(pds);
            }

            SNDService.get().savePackage(getViewContext().getContainer(), getUser(), pkg);

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
            JSONArray pkgIds = json.getJSONArray("packages");
            if (pkgIds == null)
                errors.reject(ERROR_MSG, "Package IDs not defined.");
        }

        @Override
        public ApiResponse execute(SimpleApiJsonForm form, BindException errors) throws Exception
        {
            JSONObject json = form.getJsonObject();
            JSONArray pkgIds = json.getJSONArray("packages");
            ApiSimpleResponse response = new ApiSimpleResponse();

            List<Package> pkgs = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();

            if (pkgIds.getInt(0) == -1)
            {
                Package newPkg = new Package();
                newPkg = SNDManager.get().addExtraFields(getViewContext().getContainer(), getUser(), newPkg, null);
                pkgs.add(newPkg);
            }
            else
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
                    pkgs.addAll(sndService.getPackages(getViewContext().getContainer(), getUser(), ids));
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