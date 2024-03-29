package org.yugo.backend.YuGo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yugo.backend.YuGo.exception.NotFoundException;
import org.yugo.backend.YuGo.model.Location;
import org.yugo.backend.YuGo.repository.LocationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository){
        this.locationRepository = locationRepository;
    }

    @Override
    public Location insert(Location location){
        return locationRepository.save(location);
    }

    @Override
    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    @Override
    public Location get(Integer id) {
        Optional<Location> locationOptional = locationRepository.findById(id);
        if (locationOptional.isPresent()){
            return locationOptional.get();
        }
        throw new NotFoundException("Location not found!");
    }

    @Override
    public void delete(Integer id){
        if (locationRepository.findById(id).isEmpty())
            throw new NotFoundException("Location does not exist!");
        locationRepository.deleteById(id);
    }
}
