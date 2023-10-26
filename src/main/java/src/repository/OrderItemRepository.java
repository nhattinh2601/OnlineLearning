package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.model.OrderItem;
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}
