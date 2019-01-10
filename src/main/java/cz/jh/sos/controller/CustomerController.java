package cz.jh.sos.controller;

import cz.jh.sos.model.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

;

@RestController
public class CustomerController {

    static final int PAGE_SIZE = 3;

    JdbcTemplate jdbcTemplate;

    public CustomerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test")
    public String test() {
        return "Hello world!";
    }

    @GetMapping("/customer/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT id, name, city, grade FROM customer WHERE id = ?",
                new Object[]{id},
                new BeanPropertyRowMapper<>(Customer.class));
    }

    @GetMapping("/customer")
    public List<Customer> getCustomers(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo) {
        return jdbcTemplate.query(
                "SELECT id, name, city, grade FROM customer ORDER BY id LIMIT ?,?",
                new Object[]{getHowMuchRowsToSkip(pageNo), PAGE_SIZE},
                new BeanPropertyRowMapper<>(Customer.class));
    }

    @PostMapping("/customer")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO customer (name, city, grade) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, customer.getName());
            preparedStatement.setString(2, customer.getCity());
            preparedStatement.setInt(3, customer.getGrade());

            return preparedStatement;
        }, keyHolder);

        long newCustomerId = keyHolder.getKey().longValue();
        return new ResponseEntity<>(getCustomer(newCustomerId),
                HttpStatus.CREATED);
    }

    @PutMapping("/customer/{id}")
    public Customer updateCustomer(@PathVariable Long id,
                                   @RequestBody Customer customer) {
         jdbcTemplate.update(
        "UPDATE customer SET name = ?, city = ?, grade = ? WHERE id = ?",
            customer.getName(), customer.getCity(), customer.getGrade(), id);

         return getCustomer(id);
    }

    @DeleteMapping("/customer/{id}")
    public ResponseEntity deleteCustomer(@PathVariable Long id) {
         jdbcTemplate.update("DELETE FROM customer WHERE id = ?", id);
         return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private int getHowMuchRowsToSkip(int pageNo) {
        if (pageNo <= 0) {
            throw new IllegalArgumentException("invalid number of page");
        }

        return (pageNo - 1) * PAGE_SIZE;
    }

}
