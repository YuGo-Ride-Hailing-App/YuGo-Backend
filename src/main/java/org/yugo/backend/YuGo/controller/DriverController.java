package org.yugo.backend.YuGo.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yugo.backend.YuGo.annotation.AuthorizeSelfAndAdmin;
import org.yugo.backend.YuGo.dto.*;
import org.yugo.backend.YuGo.mapper.UserDetailedMapper;
import org.yugo.backend.YuGo.mapper.WorkingTimeMapper;
import org.yugo.backend.YuGo.model.*;
import org.yugo.backend.YuGo.service.DocumentService;
import org.yugo.backend.YuGo.service.DriverService;
import org.yugo.backend.YuGo.service.ReportService;
import org.yugo.backend.YuGo.service.RideService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverService driverService;
    private final DocumentService documentService;
    private final RideService rideService;
    private final ReportService reportService;
    @Autowired
    public DriverController(DriverService driverService, DocumentService documentService, RideService rideService,ReportService reportService) {
        this.driverService = driverService;
        this.documentService = documentService;
        this.rideService = rideService;
        this.reportService=reportService;
    }

    @PostMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailedInOut> createDriver(@RequestBody @Valid UserDetailedIn driverIn){
        Driver driver = new Driver(driverIn);
        Driver driverNew = driverService.insertDriver(driver);
        return new ResponseEntity<>(new UserDetailedInOut(driverNew), HttpStatus.OK);
    }

    @GetMapping(
            value = "/status/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER', 'DRIVER')")
    public ResponseEntity<DriverStatus> getDriverStatus(@NotNull(message = "Field (id) is required")
                                                @Positive(message = "Id must be positive")
                                                @PathVariable Integer id){
        Driver driver = driverService.getDriver(id);
        return new ResponseEntity<>(new DriverStatus(driver.isOnline()), HttpStatus.OK);
    }
    @PutMapping(
            value = "/status/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER', 'DRIVER')")
    public ResponseEntity<Void> updateDriverStatus(@NotNull(message = "Field (id) is required")
                                                        @Positive(message = "Id must be positive")
                                                        @PathVariable Integer id,
                                                        @RequestBody @Valid DriverStatus status){
        driverService.updateDriverStatus(id, status.isOnline());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER', 'DRIVER')")
    public ResponseEntity<UserDetailedInOut> getDriver(@NotNull(message = "Field (id) is required")
                                                       @Positive(message = "Id must be positive")
                                                       @PathVariable Integer id){
        Driver driver = driverService.getDriver(id);
        return new ResponseEntity<>(new UserDetailedInOut(driver), HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}/vehicle",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    public ResponseEntity<VehicleOut> getVehicle(@NotNull(message = "Field (id) is required")
                                                 @Positive(message = "Id must be positive")
                                                 @PathVariable Integer id){
        Vehicle vehicle = driverService.getDriverVehicle(id);
        return new ResponseEntity<>(new VehicleOut(vehicle), HttpStatus.OK);
    }

    @PostMapping(
            value = "/{id}/vehicle",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    public ResponseEntity<VehicleOut> createVehicle(@NotNull(message = "Field (id) is required")
                                                    @Positive(message = "Id must be positive")
                                                    @PathVariable Integer id,
                                                    @RequestBody @Valid VehicleIn vehicleIn){
        Vehicle vehicle = new Vehicle(vehicleIn);
        Vehicle vehicleUpdated = driverService.createDriverVehicle(id, vehicle);
        return new ResponseEntity<>(new VehicleOut(vehicleUpdated), HttpStatus.OK);
    }

    @PostMapping(
            value = "/{id}/documents",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    public ResponseEntity<DocumentOut> createDocument(@NotNull(message = "Field (id) is required")
                                                      @Positive(message = "Id must be positive")
                                                      @PathVariable Integer id,
                                                      @RequestBody @Valid DocumentIn documentIn) throws IOException {
        Driver driver = driverService.getDriver(id);
        Document document = new Document(documentIn.getDocumentImage(), driver, DocumentType.DRIVING_LICENCE);
        documentService.insert(document);
        return new ResponseEntity<>(new DocumentOut(document), HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/document/{documentType}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    public ResponseEntity<DocumentOut> uploadDocument(@NotNull(message = "Field (id) is required")
                                                      @Positive(message = "Id must be positive")
                                                      @PathVariable Integer id,
                                                      @NotBlank(message = "Field (documentType) is required")
                                                      @PathVariable String documentType,
                                                      @NotNull(message = "Field (file) is required")
                                                      @RequestParam("image") MultipartFile file) throws IOException {
        Driver driver = driverService.getDriver(id);
        Document document= new Document(Math.abs(driver.hashCode() + file.hashCode()) + ".jpg", driver, DocumentType.valueOf(documentType));
        documentService.upload(document, file);
        return new ResponseEntity<>(new DocumentOut(document), HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}/documents",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<List<DocumentOut>> getDocuments(@NotNull(message = "Field (id) is required")
                                                   @Positive(message = "Id must be positive")
                                                   @PathVariable Integer id){
        Driver driver = driverService.getDriver(id);
        List<DocumentOut> documents = new ArrayList<>();
        for(Document d : driver.getDocuments()){
            documents.add(new DocumentOut(d));
        }

        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @DeleteMapping(
            value = "/document/{document-id}"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    ResponseEntity<Void> deleteDocument(@PathVariable(name = "document-id")
                                        @NotNull(message = "Field (document-id) is required")
                                        @Positive(message = "Document-id must be positive") Integer documentId){
        documentService.delete(documentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<AllUsersOut> getDrivers(@Min(value=0, message = "Page must be 0 or greater")
                                           @NotNull(message = "Field (page) is required")
                                           @RequestParam int page,
                                           @Positive(message = "Size must be positive")
                                           @NotNull(message = "Field (size) is required")
                                           @RequestParam int size){
        Page<User> drivers = driverService.getDriversPage(PageRequest.of(page, size));
        return new ResponseEntity<>(new AllUsersOut(drivers), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    public ResponseEntity<UserDetailedInOut> updateDriver(@NotNull(message = "Field (id) is required")
                                                          @Positive(message = "Id must be positive")
                                                          @PathVariable Integer id,
                                                          @RequestBody @Valid UserDetailedIn driverDTO){
        Driver driverUpdate = UserDetailedMapper.fromDTOtoDriver(driverDTO);
        driverUpdate.setId(id);
        User userUpdated = driverService.updateDriver(driverUpdate);
        if(userUpdated == null){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(UserDetailedMapper.fromUsertoDTO(userUpdated), HttpStatus.OK);
    }

    @PutMapping(
            value = "/{id}/vehicle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<VehicleOut> updateVehicle(@NotNull(message = "Field (id) is required")
                                             @Positive(message = "Id must be positive")
                                             @PathVariable Integer id,
                                             @RequestBody @Valid VehicleIn vehicleIn){
        Vehicle vehicle = new Vehicle(vehicleIn);
        Vehicle vehicleUpdated = driverService.updateDriverVehicle(id, vehicle);

        return new ResponseEntity<>(new VehicleOut(vehicleUpdated), HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}/working-hour",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<AllWorkTimeOut> getWorkingTimes(@NotNull(message = "Field (id) is required")
                                                   @Positive(message = "Id must be positive")
                                                   @PathVariable(value="id") Integer id,
                                                   @Min(value=0, message = "Page must be 0 or greater")
                                                   @NotNull(message = "Field (page) is required")
                                                   @RequestParam(name="page") int page,
                                                   @Positive(message = "Size must be positive")
                                                   @NotNull(message = "Field (size) is required")
                                                   @RequestParam(name="size") int size,
                                                   @NotBlank(message = "Field (from) is required")
                                                   @RequestParam(name = "from") String from,
                                                   @NotBlank(message = "Field (to) is required")
                                                   @RequestParam(name = "to") String to){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime fromTime = LocalDate.parse(from, formatter).atTime(LocalTime.MIDNIGHT);
        LocalDateTime toTime = LocalDate.parse(to, formatter).atTime(LocalTime.MIDNIGHT);
        Page<WorkTime> workTimes = driverService.getDriverWorkingTimesPage(id, PageRequest.of(page, size), fromTime, toTime);
        return new ResponseEntity<>(new AllWorkTimeOut(workTimes.stream()), HttpStatus.OK);
    }

    @PostMapping(
            value = "/{id}/working-hour",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<WorkTimeOut> createWorkTimeForDriver(@NotNull(message = "Field (id) is required")
                                                        @Positive(message = "Id must be positive")
                                                        @PathVariable(value="id") Integer id,
                                                        @RequestBody @Valid WorkTimeIn workTimeIn){
        WorkTime wt = new WorkTime();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        wt.setStartTime(LocalDateTime.parse(workTimeIn.getStart(), formatter));

        WorkTime workTimeNew = driverService.insertWorkTime(id, wt);
        if(workTimeNew == null){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        WorkTimeOut output = new WorkTimeOut(workTimeNew);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping(
            value = "/working-hour/{working-hour-id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<WorkTimeOut> getWorkTimeById(@NotNull(message = "Field (id) is required")
                                                @Positive(message = "Id must be positive")
                                                @PathVariable(value = "working-hour-id") Integer id){
        WorkTime workTime = driverService.getWorkTime(id);
        return new ResponseEntity<>(WorkingTimeMapper.fromWorkTimeToDTO(workTime), HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}/ride",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<AllRidesOut> getRidesForDriver(@NotNull(message = "Field (id) is required")
                                                  @Positive(message = "Id must be positive")
                                                  @PathVariable(value="id") Integer id,
                                                  @Min(value=0, message = "Page must be 0 or greater")
                                                  @NotNull(message = "Field (page) is required")
                                                  @RequestParam(name="page") int page,
                                                  @Positive(message = "Size must be positive")
                                                  @NotNull(message = "Field (size) is required")
                                                  @RequestParam(name="size") int size,
                                                  @RequestParam(name = "sort", required = false) String sort,
                                                  @NotBlank(message = "Field (from) is required")
                                                  @RequestParam(name = "from") String from,
                                                  @NotBlank(message = "Field (to) is required")
                                                  @RequestParam(name = "to") String to){
        PageRequest pageRequest;
        pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,Objects.requireNonNullElse(sort, "id")));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime fromTime = LocalDate.parse(from, formatter).atTime(LocalTime.MIDNIGHT);
        LocalDateTime toTime = LocalDate.parse(to, formatter).atTime(LocalTime.MIDNIGHT);
        Page<Ride> rides = rideService.getRidesByDriverPage(id, pageRequest, fromTime, toTime);
        return new ResponseEntity<>(new AllRidesOut(rides), HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}/rides",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'PASSENGER','DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<AllRidesOut> getRidesForDriverNonPagable(@NotNull(message = "Field (id) is required")
                                                  @Positive(message = "Id must be positive")
                                                  @PathVariable(value="id") Integer id){
        LocalDateTime fromTime=LocalDateTime.of(1753, Month.JANUARY,1,0, 0);
        LocalDateTime toTime=LocalDateTime.of(9998, Month.DECEMBER,31,0,0);
        List<Ride> rides = rideService.getRidesByDriverNonPageable(id,fromTime,toTime);
        return new ResponseEntity<>(new AllRidesOut(rides), HttpStatus.OK);
    }

    @GetMapping(
            value = "/{id}/statistics",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<StatisticsOut> getDriverStatistics(@NotNull(message = "Field (id) is required")
                                                            @Positive(message = "Id must be positive")
                                                            @PathVariable(value="id") Integer id){
        return new ResponseEntity<>(reportService.getStatistics(id), HttpStatus.OK);
    }

    @PutMapping(
            value = "/working-hour/{working-hour-id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    @AuthorizeSelfAndAdmin(pathToUserId = "[0]", message = "User not found!")
    ResponseEntity<WorkTimeOut> updateWorkTime(@NotNull(message = "Field (working-hour-id) is required")
                                               @Positive(message = "Working-hour-id must be positive")
                                               @PathVariable(value = "working-hour-id")  Integer id,
                                               @RequestBody @Valid EndWorkTimeIn workTimeIn){

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime endTime = LocalDateTime.parse(workTimeIn.getEnd(), formatter);
        WorkTime workTimeUpdated = driverService.endWorkTime(id, endTime);
        return new ResponseEntity<>(WorkingTimeMapper.fromWorkTimeToDTO(workTimeUpdated), HttpStatus.OK);
    }
}
