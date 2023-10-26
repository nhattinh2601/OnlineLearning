package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
}
