package app.bankcardmanagementsystem.repository;

import app.bankcardmanagementsystem.entity.LimitOnOperationByCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LimitOnOperationByCardRepository extends JpaRepository<LimitOnOperationByCard, Long> {

    List<LimitOnOperationByCard> findByCard_Id(Long cardId);

}
