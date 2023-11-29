package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.model.CourseRegister;
import src.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRegisterRepository extends JpaRepository<CourseRegister, Integer> {
    long countByCourseIdAndIsActive(int courseId, boolean isActive);
    Optional<CourseRegister> findByUserIdAndCourseId(int userId, int courseId);

    Optional<CourseRegister> findByCourseId(int couseId);

    Optional<CourseRegister> findByUserId(int userId);
}
