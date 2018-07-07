/*
 * Copyright 2017 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.security;

import eu.europa.ec.leos.domain.common.LeosAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
class LeosHierarchicalRolePermissionVoter extends RoleHierarchyVoter {
    private static final Logger LOG = LoggerFactory.getLogger(LeosHierarchicalRolePermissionVoter.class);
    
    private RoleHierarchy roleHierarchy;

    @Autowired
    public LeosHierarchicalRolePermissionVoter(RoleHierarchy roleHierarchy) {
        super(roleHierarchy);
        this.roleHierarchy = roleHierarchy;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
       return true;// reject all as string
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true; // does not support secured objects
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        int result = ACCESS_ABSTAIN;
        LeosPermission permission = retrievePermission(attributes);

        // Attempt to find a matching granted authority
        for (GrantedAuthority authority : retrieveAuthorities(authentication)) {
                Set<LeosPermission> authorityPermissions = LeosPermissionAuthorityMap.getPermissions(getLeosAuthority(authority.getAuthority()));
                if (authorityPermissions != null && authorityPermissions.contains(permission)) {
                    result = ACCESS_GRANTED;
                    break;
                }
        }
        return result;
    }

    private Collection<? extends GrantedAuthority> retrieveAuthorities(Authentication authentication) {
        return this.roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities());
    }

    private LeosPermission retrievePermission(Collection<ConfigAttribute> attributes) {
        LeosPermission permission = null;
        if (attributes != null) {
            Iterator<ConfigAttribute> iterator = attributes.iterator();
            if (iterator.hasNext()) {// we expect only one permission check
                permission = LeosPermission.valueOf(iterator.next().getAttribute());
            }
        }
        return permission;
    }
    
    private LeosAuthority getLeosAuthority(String authority) {
        try {
            return LeosAuthority.valueOf(authority);
        } catch(IllegalArgumentException e) {
            LOG.debug("Illegal Argument in LeosAuthority...ignoring & returning null: " + e);
            return null;
        } 
    }
}
