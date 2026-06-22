package it.unipa.afam.repository;

import it.unipa.afam.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmailIstituzionale(String email);
    boolean existsByEmailIstituzionale(String email);
}
