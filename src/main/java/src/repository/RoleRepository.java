package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.model.Role;

@Repository
public interface RoleRepository  extends JpaRepository<Role, Integer> {
}
