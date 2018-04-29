package org.labkey.snd.security;



import org.labkey.api.data.Container;
import org.labkey.api.security.Group;
import org.labkey.api.security.MutableSecurityPolicy;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.SecurityPolicyManager;
import org.labkey.api.security.User;
import org.labkey.api.security.roles.Role;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.snd.Category;
import org.labkey.api.snd.SNDService;

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

    public Map<String, Role> getAllSecurityRoles()
    {
        return EnumSet.allOf(SecurityRolesEnum.class).stream().map(SecurityRolesEnum::getRole).collect(Collectors.toMap(
                Role::getName, role -> role));
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

}
