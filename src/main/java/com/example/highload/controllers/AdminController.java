package com.example.highload.controllers;

import com.example.highload.model.inner.UserRequest;
import com.example.highload.model.network.UserDto;
import com.example.highload.model.network.UserRequestDto;
import com.example.highload.services.AdminService;
import com.example.highload.services.UserService;
import com.example.highload.utils.DataTransformer;
import com.example.highload.utils.PaginationHeadersCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/api/app/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;
    private final DataTransformer dataTransformer;
    private final PaginationHeadersCreator paginationHeadersCreator;

    @PostMapping("/user-request/approve/{userRequestId}")
    @CrossOrigin
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity approveUserRequest(@PathVariable int userRequestId) {
        adminService.approveUser(userRequestId);
        return ResponseEntity.ok("User approved");
    }

    @PostMapping("/user/delete/{id}")
    @CrossOrigin
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity deleteUser(@PathVariable int id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted");
    }

    @PostMapping("/user/all/delete-expired")
    @CrossOrigin
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity deleteLogicallyDeletedAccountsExpired(@RequestBody int days) {
        adminService.deleteLogicallyDeletedUsers(days);
        return ResponseEntity.ok("User deleted");
    }

    @PostMapping("/user/add/")
    @CrossOrigin
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity addUser(@RequestBody UserDto user) {
        if (userService.findByLogin(user.getLogin()) == null) {
            adminService.addUser(user);
            return ResponseEntity.ok("User added");
        }
        return ResponseEntity.badRequest().body("User already exists!");

    }

    @GetMapping("/user-request/all/{page}")
    @CrossOrigin
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity getAllUserRequests(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 50);
        Page<UserRequest> entityList = userService.getAllUserRequests(pageable);
        List<UserRequestDto> dtoList = dataTransformer.userRequestListToDto(entityList.getContent());
        HttpHeaders responseHeaders = paginationHeadersCreator.pageWithTotalElementsHeadersCreate(entityList);
        return ResponseEntity.ok().headers(responseHeaders).body(dtoList);
    }

}
