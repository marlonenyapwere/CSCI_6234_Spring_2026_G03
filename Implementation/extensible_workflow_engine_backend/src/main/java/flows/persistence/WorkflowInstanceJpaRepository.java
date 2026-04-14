package flows.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowInstanceJpaRepository extends JpaRepository<WorkflowInstanceEntity, String>{
}
