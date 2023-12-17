package com.example.highload.controllers;

import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.services.AdminService;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;
    private final DataTransformer dataTransformer;
    private final PaginationHeadersCreator paginationHeadersCreator;

    @PostMapping("/user-request/approve/{userRequestId}")
    public ResponseEntity approveUserRequest(@PathVariable int userRequestId) {
        adminService.approveUser(userRequestId);
        return ResponseEntity.ok("User approved");
    }

    @PostMapping("/user/delete/{id}")
    public ResponseEntity deleteUser(@PathVariable int id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted");
    }

    @PostMapping("/user/all/delete-expired/{days}")
    public ResponseEntity deleteLogicallyDeletedAccountsExpired(@PathVariable int days) {
        adminService.deleteLogicallyDeletedUsers(days);
        return ResponseEntity.ok("Users deleted");
    }

    @PostMapping("/user/add")
    public ResponseEntity addUser(@Valid @RequestBody UserDto user) {
        if (userService.findByLoginElseNull(user.getLogin()) == null) {
            adminService.addUser(user);
            return ResponseEntity.ok("User added");
        }
        return ResponseEntity.badRequest().body("User already exists!");

    }

    @GetMapping("/user-request/all/{page}")
    public ResponseEntity getAllUserRequests(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 50);
        Page<UserRequest> entityList = userService.getAllUserRequests(pageable);
        List<UserRequestDto> dtoList = dataTransformer.userRequestListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions(){
        return ResponseEntity.badRequest().body("Request body validation failed!");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity handleServiceExceptions(){
        return ResponseEntity.badRequest().body("Wrong ids in path!");
    }

}
