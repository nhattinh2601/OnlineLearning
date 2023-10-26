package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.model.Review;
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
