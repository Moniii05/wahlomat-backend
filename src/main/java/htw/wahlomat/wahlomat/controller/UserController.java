package htw.wahlomat.wahlomat.controller;

import htw.wahlomat.wahlomat.dto.UserResponse;
import htw.wahlomat.wahlomat.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")  //gesamter Controller nur für admin
@Tag( name = "User", description = "User management API")
public class UserController
{
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* User-Endpunkte werden nie genutzt, deshalb vorerst auskommentiert


    // Get all Users als Liste
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> userResponseList = this.userService.getAllUsers();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userResponseList);
    }

    // Get one User by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getOneUserById(@PathVariable Long id) {
        Optional<UserResponse> userResponse = this.userService.getUserById(id);

        if (userResponse.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userResponse.get());
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

     */
}
