package app.bankcardmanagementsystem.repository;

import app.bankcardmanagementsystem.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findAllByUser_Email(String email);
}