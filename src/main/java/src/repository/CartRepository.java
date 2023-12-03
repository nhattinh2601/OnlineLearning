package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import src.model.Cart;
import src.model.Review;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository  extends JpaRepository<Cart, Integer> {

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.courseId = :courseId AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    List<Cart> findCart(@Param("userId") int userId, @Param("courseId") int courseId);

    List<Cart> findByUserId(int userId);




}
