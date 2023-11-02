package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.model.Rating;
@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
    Iterable<Rating> findByCourseId(int courseId);
}
