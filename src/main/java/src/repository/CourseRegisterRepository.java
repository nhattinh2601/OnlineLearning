package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import src.model.CourseRegister;
import src.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRegisterRepository extends JpaRepository<CourseRegister, Integer> {
    long countByCourseIdAndIsActive(int courseId, boolean isActive);
    Optional<CourseRegister> findByUserIdAndCourseId(int userId, int courseId);

    List<CourseRegister> findByUserId(int userId);

    @Query("SELECT COUNT(DISTINCT r.userByUserId) FROM CourseRegister r WHERE r.courseId = :courseId")
    Long countDistinctUsersByCourseId(int courseId);

}
