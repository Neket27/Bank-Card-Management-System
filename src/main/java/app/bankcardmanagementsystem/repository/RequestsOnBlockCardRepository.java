package app.bankcardmanagementsystem.repository;

import app.bankcardmanagementsystem.entity.RequestOnBlockCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestsOnBlockCardRepository extends JpaRepository<RequestOnBlockCard, Long> {
}
