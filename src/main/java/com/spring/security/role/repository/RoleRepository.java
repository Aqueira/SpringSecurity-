package com.spring.security.role.repository;

import com.spring.security.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r.id FROM Role r WHERE r.defaultRole = true")
    Set<Long> findIdsByDefaultRoleTrue();
}