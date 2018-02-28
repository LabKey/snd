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

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.action.ApiUsageException;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.exp.api.ExperimentService;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.PackageDomainKind;
import org.labkey.api.snd.Project;
import org.labkey.api.snd.SNDSequencer;
import org.labkey.api.snd.SNDService;
import org.labkey.api.snd.SuperPackage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by marty on 8/4/2017.
 */
public class SNDServiceImpl implements SNDService
{
    public static final SNDServiceImpl INSTANCE = new SNDServiceImpl();
    private final ReentrantLock lock = new ReentrantLock();

    private SNDServiceImpl()
    {
    }

    @Override
    public Lock getWriteLock()
    {
        return lock;
    }

    @Override
    public void savePackage(Container c, User u, Package pkg)
    {
        savePackage(c, u, pkg, null, false);
    }

    @Override
    public void savePackage(Container c, User u, Package pkg, SuperPackage superPkg, boolean cloneFlag)
    {
        BatchValidationException errors = new BatchValidationException();
        Domain domain = null;

        if (null != pkg.getPkgId() && pkg.getPkgId() > -1)
        {
            String domainURI = PackageDomainKind.getDomainURI(PackageDomainKind.getPackageSchemaName(), SNDManager.getPackageName(pkg.getPkgId()), c, u);
            domain = PropertyService.get().getDomain(c, domainURI);
        }

        try (DbScope.Transaction tx = SNDSchema.getInstance().getSchema().getScope().ensureTransaction(lock))
        {
            if ((null != domain) && !cloneFlag)  // clone case is basically creation
            {
                SNDManager.get().updatePackage(u, c, pkg, superPkg, errors);
            }
            else
            {
                pkg.setPkgId(SNDSequencer.PKGID.ensureId(c, pkg.getPkgId()));

                if (superPkg != null)
                    superPkg.setPkgId(pkg.getPkgId());

                SNDManager.get().createPackage(u, c, pkg, superPkg, errors);
            }
            tx.commit();
        }
        if (errors.hasErrors())
            throw new ApiUsageException(errors);
    }

    @Override
    public void saveSuperPackages(Container c, User u, List<SuperPackage> superPkgs)
    {
        BatchValidationException errors = new BatchValidationException();

        SNDManager.get().saveSuperPackages(u, c, superPkgs, errors);

        if (errors.hasErrors())
            throw new ApiUsageException(errors);
    }

    @Override
    public List<Package> getPackages(Container c, User u, List<Integer> pkgIds, boolean includeExtraFields, boolean includeLookups, boolean includeAllAttributes)
    {
        BatchValidationException errors = new BatchValidationException();

        List<Package> pkgs = SNDManager.get().getPackages(c, u, pkgIds, includeExtraFields, includeLookups, includeAllAttributes, errors);
        if (errors.hasErrors())
            throw new ApiUsageException(errors);

        return pkgs;
    }

    @Override
    public Project getProject(Container c, User u, int projectId, int revNum)
    {
        return SNDManager.get().getProject(c, u, projectId, revNum);
    }

    @Override
    public void registerAttributeLookup(Container c, User u, String schema, @Nullable String table)
    {
        SNDManager.get().registerAttributeLookups(c, u, schema, table);
    }

    @Override
    public Map<String, String> getAttributeLookups(Container c, User u)
    {
        return SNDManager.get().getAttributeLookups(c, u);
    }

    @Override
    public Object getDefaultLookupDisplayValue(User u, Container c, String schema, String table, Object key)
    {
        return SNDManager.get().getDefaultLookupDisplayValue(u, c, schema, table, key);
    }

    public void saveProject(Container c, User u, Project project, boolean isRevision)
    {
        BatchValidationException errors = new BatchValidationException();

        String objectId = SNDManager.get().getProjectObjectId(c, u, project, errors);

        try (DbScope.Transaction tx = SNDSchema.getInstance().getSchema().getScope().ensureTransaction(lock))
        {
            if (objectId != null)
            {
                project.updateObjectId(objectId);
                if (isRevision)
                {
                    SNDManager.get().reviseProject(c, u, project, errors);
                }
                else
                {
                    SNDManager.get().updateProject(c, u, project, errors);
                }
            }
            else
            {
                project.setRevisionNum(0);
                SNDManager.get().createProject(c, u, project, errors);
            }
            tx.commit();
        }
        if (errors.hasErrors())
            throw new ApiUsageException(errors);
    }

    @Override
    public Event getEvent(Container c, User u, int eventId)
    {
        BatchValidationException errors = new BatchValidationException();

        Event event = SNDManager.get().getEvent(c, u, eventId, errors);

        if (errors.hasErrors())
            throw new ApiUsageException(errors);

        return event;
    }

    @Override
    public void saveEvent(Container c, User u, Event event)
    {
        BatchValidationException errors = new BatchValidationException();

        if (event.getEventId() != null && SNDManager.get().eventExists(c, u, event.getEventId()))
        {
            SNDManager.get().updateEvent(c, u, event, errors);
        }
        else
        {
            SNDManager.get().createEvent(c, u, event, errors);
        }

        if (errors.hasErrors())
            throw new ApiUsageException(errors);
    }

    public JSONObject convertPropertyDescriptorToJson(Container c, User u, GWTPropertyDescriptor pd, boolean resolveLookupValues)
    {
        JSONObject json = ExperimentService.get().convertPropertyDescriptorToJson(pd);

        if (pd.getLookupSchema() != null && pd.getLookupQuery() != null && pd.getDefaultValue() != null)
        {
            json.put("defaultValue", SNDService.get().getDefaultLookupDisplayValue(u, c, pd.getLookupSchema(), pd.getLookupQuery(), pd.getDefaultValue()));
        }

        if (resolveLookupValues && (pd.getLookupSchema() != null) && (pd.getLookupQuery() != null))
        {
            json.put("lookupValues", lookupValuesToJson(c, u, pd.getLookupSchema(), pd.getLookupQuery()));
        }

        // Not passing in full range URI also need to handle participantid
        String type = pd.getRangeURI().split("#")[1];
        String conceptUri = pd.getConceptURI();
        if (conceptUri != null && conceptUri.contains(SNDManager.RANGE_PARTICIPANTID))
        {
            type = SNDManager.RANGE_PARTICIPANTID;
        }
        json.put("rangeURI", type);

        return json;
    }

    public JSONArray lookupValuesToJson(Container c, User u, String schema, String query)
    {
        JSONArray array = new JSONArray();

        UserSchema userSchema = QueryService.get().getUserSchema(u, c, schema);
        TableInfo table = userSchema.getTable(query);

        if (null != table)
        {
            // Use the title column for the actual lookup value
            String title = table.getTitleColumn();
            if (title == null)
            {
                title = table.getPkColumnNames().get(0);
            }

            TableSelector ts = new TableSelector(table);
            Object value;
            try(ResultSet rs = ts.getResultSet())
            {
                while (rs.next())
                {
                    value = rs.getObject(title);
                    array.put(value);
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }

        return array;
    }
}
