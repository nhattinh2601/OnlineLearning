package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.model.Cart;
@Repository
public interface CartRepository  extends JpaRepository<Cart, Integer> {
}
