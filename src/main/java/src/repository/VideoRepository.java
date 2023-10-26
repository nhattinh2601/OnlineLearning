package src.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import src.model.Video;
@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {
}
