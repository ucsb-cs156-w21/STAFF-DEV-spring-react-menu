package edu.ucsb.menumanager.repositories;

import edu.ucsb.menumanager.entities.AppUser;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends CrudRepository<AppUser, Long> {
  public List<AppUser> findByEmail(String email);
}
