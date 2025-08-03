package Generations.toDoListConTokenBuono.controller;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Generations.toDoListConTokenBuono.model.Task;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    // “Database” in-memory
    private final Map<String, String> utenti = new HashMap<>();
    private final Set<String> sessioniValide = new HashSet<>();
    private final Map<String, String> tokenToUser = new HashMap<>();
    private final Map<String, List<Task>> tasksPerUtente = new HashMap<>();

    public TaskController() {
        // utenti di test
        utenti.put("alice", "1234");
        utenti.put("bob", "abcd");
    }

    // DTO per login
    public static class LoginRequest {
        public String username;
        public String password;
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req) {
        if (utenti.containsKey(req.username) && utenti.get(req.username).equals(req.password)) {
            String token = UUID.randomUUID().toString();
            sessioniValide.add(token);
            tokenToUser.put(token, req.username);
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("LOGIN_FAILED");
    }

    // PROFILO
    @GetMapping("/profilo")
    public ResponseEntity<String> profilo(@RequestHeader("X-Token") String token) {
        if (!sessioniValide.contains(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ACCESSO NEGATO");
        }
        String user = tokenToUser.get(token);
        return ResponseEntity.ok("Sei loggato come: " + user);
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("X-Token") String token) {
        sessioniValide.remove(token);
        tokenToUser.remove(token);
        return ResponseEntity.ok("Logout eseguito");
    }

    // CREATE TASK
    @PostMapping
    public ResponseEntity<?> createTask(
            @RequestHeader("X-Token") String token,
            @RequestBody @Valid Task task) {

        if (!sessioniValide.contains(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        }

        String user = tokenToUser.get(token);
        tasksPerUtente
            .computeIfAbsent(user, u -> new ArrayList<>())
            .add(task);

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(task);
    }

    // READ ALL TASKS
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(@RequestHeader("X-Token") String token) {
        if (!sessioniValide.contains(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String user = tokenToUser.get(token);
        List<Task> list = tasksPerUtente.getOrDefault(user, Collections.emptyList());
        return ResponseEntity.ok(list);
    }

    // UPDATE TASK
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @RequestHeader("X-Token") String token,
            @PathVariable int id,
            @RequestBody @Valid Task updated) {

        if (!sessioniValide.contains(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        }

        String user = tokenToUser.get(token);
        List<Task> list = tasksPerUtente.getOrDefault(user, new ArrayList<>());

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                updated.setId(id);
                list.set(i, updated);
                return ResponseEntity.ok(updated);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("Task con id " + id + " non trovata");
    }

    // DELETE TASK
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(
            @RequestHeader("X-Token") String token,
            @PathVariable int id) {

        if (!sessioniValide.contains(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        }

        String user = tokenToUser.get(token);
        List<Task> list = tasksPerUtente.getOrDefault(user, new ArrayList<>());

        boolean removed = list.removeIf(t -> t.getId() == id);
        if (removed) {
            return ResponseEntity.ok("Task eliminata con successo");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("Task con id " + id + " non trovata");
    }
}
