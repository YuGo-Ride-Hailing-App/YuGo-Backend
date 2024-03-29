package org.yugo.backend.YuGo.service;

import org.yugo.backend.YuGo.model.Location;

import java.util.List;
import java.util.Optional;

public interface LocationService {
    Location insert(Location location);
    List<Location> getAll();
    Location get(Integer id);
    void delete(Integer id);
}
