package org.yugo.backend.YuGo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.yugo.backend.YuGo.model.Path;

public interface PathRepository extends JpaRepository<Path,Integer> {
}
