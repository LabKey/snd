package org.labkey.snd.security;



import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.Group;
import org.labkey.api.security.MutableSecurityPolicy;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.SecurityPolicyManager;
import org.labkey.api.security.User;
import org.labkey.api.security.roles.Role;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.snd.Category;
import org.labkey.api.snd.SNDService;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SNDSecurityManager
{
    private static final SNDSecurityManager _instance = new SNDSecurityManager();

    public static SNDSecurityManager get()
    {
        return _instance;
    }

    public void updatePermissions(Container c, User u, Map props)
    {
        String groupCat;
        String[] parts;

        Map<Integer, Category> categories = SNDService.get().getAllCategories(c, u);
        Category category;

        SecurityManager.getGroups(c.getProject(), true);

        Map<Integer, MutableSecurityPolicy> policyMap = new HashMap<>();  // Policy cache
        MutableSecurityPolicy policy;
        Group group;
        Role role;
        SecurityPolicy existingPolicy;
        String roleName;

        for (Object key : props.keySet())
        {
            // parse incoming string
            parts = ((String)key).split("\\|");

            if (parts.length < 2)
                continue;

            groupCat = parts[1];
            parts = groupCat.split("_");

            if (parts.length < 2)
                continue;

            // get/create policy
            category = categories.get(Integer.parseInt(parts[1]));
            policy = policyMap.get(category.getCategoryId());
            if (policy == null)
            {
                existingPolicy = SecurityPolicyManager.getPolicy(c, category.getResourceId());
                if (existingPolicy != null)
                {
                    policy = new MutableSecurityPolicy(existingPolicy);
                }
            }

            if (policy == null)
            {
                policy = new MutableSecurityPolicy(category);
            }

            // add policy role
            group = SecurityManager.getGroup(Integer.parseInt(parts[0]));
            if (group != null)
            {
                roleName = (String) props.get(key);
                role = getRoleByName(roleName);

                // Clear role first
                if (role != null || roleName.equals("None"))
                {
                    policy.clearAssignedRoles(group);
                }

                if (role != null)
                {
                    policy.addRoleAssignment(group, role);
                }

                policyMap.put(category.getCategoryId(), policy);
            }
        }

        // save all policies
        for (MutableSecurityPolicy savedPolicy : policyMap.values())
        {
            SecurityPolicyManager.savePolicy(savedPolicy);
        }
    }

    private Role getRoleByName(String name)
    {
        return EnumSet.allOf(SecurityRolesEnum.class).stream().map(SecurityRolesEnum::getRole).filter(role -> role.getName().equals(name)).findFirst().orElse(null);
    }

    public Map<String, Role> getAllSecurityRoles()
    {
        return EnumSet.allOf(SecurityRolesEnum.class).stream().map(SecurityRolesEnum::getRole).collect(Collectors.toMap(
                Role::getName, role -> role));
    }

    public void populateQCStates(Container c, User u)
    {
        UserSchema coreSchema = QueryService.get().getUserSchema(u, c, "core");
        DbSchema coreDbSchema = coreSchema.getDbSchema();
        TableInfo qcStateTi = coreDbSchema.getTable("QCState");

        Object[][] states = EnumSet.allOf(QCStateEnum.class).stream().map(qcStateEnum -> new Object[]{qcStateEnum.getName(), qcStateEnum.getDescription(), qcStateEnum.isPublicData()}).toArray(Object[][]::new);

        try (DbScope.Transaction transaction = coreSchema.getDbSchema().getScope().ensureTransaction())
        {
            // check if QCStates exist, if not insert them
            for (Object[] qc : states)
            {
                SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Label"), qc[0]);
                TableSelector ts = new TableSelector(qcStateTi, Collections.singleton("RowId"), filter, null);
                Integer[] rowIds = ts.getArray(Integer.class);

                Map<String, Object> row = new CaseInsensitiveHashMap<>();
                row.put("Container", c.getId());
                row.put("Label", qc[0]);
                row.put("Description", qc[1]);
                row.put("PublicData", qc[2]);

                if (rowIds.length < 1)
                {
                    Table.insert(u, qcStateTi, row);

                }
                else
                {
                    Table.update(u, qcStateTi, row, rowIds);
                }
            }

            transaction.commit();
        }
    }

}
