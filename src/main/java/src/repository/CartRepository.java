package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import src.model.Cart;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository  extends JpaRepository<Cart, Integer> {

    @Query("SELECT c FROM Cart c WHERE c.userId = ?1")
    List<Cart> findCartsByUserId(int id);
}
