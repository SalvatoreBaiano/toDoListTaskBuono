package Generations.toDoListConTokenBuonoController;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Generations.toDoListConTokenBuonoModel.Task;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class UserController {

    private Map<String, String> utenti = new HashMap<>(); // username -> password
    private Map<String, String> tokenUtente = new HashMap<>(); // token -> username
    private Map<String, List<Task>> taskPerUtente = new HashMap<>(); // username -> lista di task

    // REGISTRAZIONE
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        if (utenti.containsKey(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Utente già registrato");
        }
        utenti.put(username, password);
        return ResponseEntity.ok("Registrazione completata per: " + username);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        if (!utenti.containsKey(username) || !utenti.get(username).equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali non valide");
        }
        String token = UUID.randomUUID().toString();
        tokenUtente.put(token, username);
        return ResponseEntity.ok(token);
    }

    // CREAZIONE TASK
    @PostMapping("/newTask")
    public ResponseEntity<?> newTask(
        @RequestHeader(value = "X-Token", required = false) String token,
        @RequestBody @Valid Task task) {

        String username = tokenUtente.get(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        }

        taskPerUtente.computeIfAbsent(username, k -> new ArrayList<>()).add(task);
        return ResponseEntity.ok("Task aggiunta correttamente per " + username + ": " + task);
    }

    // VISUALIZZA TASK
    @GetMapping("/seeTask")
    public ResponseEntity<?> seeTask(@RequestHeader(value = "X-Token", required = false) String token) {
        String username = tokenUtente.get(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        }

        List<Task> tasks = taskPerUtente.getOrDefault(username, new ArrayList<>());
        return ResponseEntity.ok(tasks);
    }

    // MODIFICA TASK
    @PutMapping("/updateTask/{id}")
    public ResponseEntity<?> updateTask(
        @RequestHeader(value = "X-Token", required = false) String token,
        @PathVariable int id,
        @RequestBody @Valid Task updatedTask) {

        String username = tokenUtente.get(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        }

        List<Task> tasks = taskPerUtente.getOrDefault(username, new ArrayList<>());

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == id) {
                tasks.set(i, updatedTask);
                return ResponseEntity.ok("Task modificata: " + updatedTask);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task non trovata");
    }

    // ELIMINA TASK
    @DeleteMapping("/deleteTask/{id}")
    public ResponseEntity<?> deleteTask(@RequestHeader(value = "X-Token", required = false) String token,
                                        @PathVariable int id) {

        String username = tokenUtente.get(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        }

        List<Task> tasks = taskPerUtente.getOrDefault(username, new ArrayList<>());

        boolean removed = tasks.removeIf(task -> task.getId() == id);

        if (removed) {
            return ResponseEntity.ok("Task eliminata con successo");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task non trovata");
        }
    }
}
